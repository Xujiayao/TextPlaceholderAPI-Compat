package com.xujiayao.placeholder_api_compat.api.parsers;


import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import com.xujiayao.placeholder_api_compat.api.Placeholders;
import com.xujiayao.placeholder_api_compat.api.node.DirectTextNode;
import com.xujiayao.placeholder_api_compat.api.node.LiteralNode;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.TranslatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentTextNode;
import com.xujiayao.placeholder_api_compat.impl.placeholder.PlaceholderNode;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Replaced with {@link TagLikeParser}
 */
@Deprecated
public record PatternPlaceholderParser(Pattern pattern,
                                       Function<String, @Nullable TextNode> placeholderProvider) implements NodeParser {
	public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))[%](?<id>[^%]+:[^%]+)[%]");
	public static final Pattern ALT_PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))[{](?<id>[^{}]+:[^{}]+)[}]");

	public static final Pattern PLACEHOLDER_PATTERN_CUSTOM = Pattern.compile("(?<!((?<!(\\\\))\\\\))[%](?<id>[^%]+)[%]");
	public static final Pattern ALT_PLACEHOLDER_PATTERN_CUSTOM = Pattern.compile("(?<!((?<!(\\\\))\\\\))[{](?<id>[^{}]+)[}]");

	public static final Pattern PREDEFINED_PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))\\$[{](?<id>[^}]+)}");

	public static PatternPlaceholderParser of(Pattern pattern, ParserContext.Key<PlaceholderContext> contextKey, Placeholders.PlaceholderGetter placeholders) {
		return new PatternPlaceholderParser(pattern, (arg) -> {
			var args = arg.split(" ", 2);

			if (placeholders.exists(args[0])) {
				return new PlaceholderNode(contextKey, args[0], placeholders, placeholders.isContextOptional(), args.length == 2 ? args[1] : null);
			} else {
				return null;
			}
		});
	}

	public static PatternPlaceholderParser ofNodeMap(Pattern pattern, Map<String, TextNode> map) {
		return new PatternPlaceholderParser(pattern, map::get);
	}

	public static PatternPlaceholderParser ofTextMap(Pattern pattern, Map<String, Text> map) {
		return new PatternPlaceholderParser(pattern, arg -> {
			var x = map.get(arg);
			return x != null ? new DirectTextNode(x) : null;
		});
	}

	@Override
	public TextNode[] parseNodes(TextNode text) {
		if (text instanceof TranslatedNode translatedNode) {
			return new TextNode[]{translatedNode.transform(this)};
		} else if (text instanceof LiteralNode(String value)) {
			var out = new ArrayList<TextNode>();

			Matcher matcher = pattern.matcher(value);
			int start;
			int end;

			int previousEnd = 0;

			while (matcher.find()) {
				var placeholder = matcher.group("id");
				start = matcher.start();
				end = matcher.end();

				var output = this.placeholderProvider.apply(placeholder);

				if (output != null) {
					if (start != 0) {
						out.add(new LiteralNode(value.substring(previousEnd, start)));
					}
					out.add(output);

					previousEnd = end;
				} else {
					matcher.region(start + 1, value.length());
				}
			}

			if (previousEnd != value.length()) {
				out.add(new LiteralNode(value.substring(previousEnd)));
			}

			return out.toArray(new TextNode[0]);
		}


		if (text instanceof ParentTextNode parentNode) {
			var out = new ArrayList<TextNode>();

			for (var text1 : parentNode.getChildren()) {
				out.add(TextNode.asSingle(this.parseNodes(text1)));
			}

			return new TextNode[]{parentNode.copyWith(out.toArray(new TextNode[0]), this)};
		}

		return new TextNode[]{text};
	}
}
