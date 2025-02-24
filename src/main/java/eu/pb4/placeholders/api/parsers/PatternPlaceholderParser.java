package eu.pb4.placeholders.api.parsers;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.DirectTextNode;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.TranslatedNode;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import eu.pb4.placeholders.impl.placeholder.PlaceholderNode;
import net.minecraft.network.chat.Component;
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
	public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))%(?<id>[^%]+:[^%]+)%");
	public static final Pattern ALT_PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))[{](?<id>[^{}]+:[^{}]+)[}]");

	public static final Pattern PLACEHOLDER_PATTERN_CUSTOM = Pattern.compile("(?<!((?<!(\\\\))\\\\))%(?<id>[^%]+)%");
	public static final Pattern ALT_PLACEHOLDER_PATTERN_CUSTOM = Pattern.compile("(?<!((?<!(\\\\))\\\\))[{](?<id>[^{}]+)[}]");

	public static final Pattern PREDEFINED_PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))\\$[{](?<id>[^}]+)}");

	public static PatternPlaceholderParser of(Pattern pattern, ParserContext.Key<PlaceholderContext> contextKey, Placeholders.PlaceholderGetter placeholders) {
		return new PatternPlaceholderParser(pattern, (arg) -> {
			String[] args = arg.split(" ", 2);
			return placeholders.exists(args[0]) ? new PlaceholderNode(contextKey, args[0], placeholders, placeholders.isContextOptional(), args.length == 2 ? args[1] : null) : null;
		});
	}

	public static PatternPlaceholderParser ofNodeMap(Pattern pattern, Map<String, TextNode> map) {
		return new PatternPlaceholderParser(pattern, map::get);
	}

	public static PatternPlaceholderParser ofTextMap(Pattern pattern, Map<String, Component> map) {
		return new PatternPlaceholderParser(pattern, (arg) -> {
			Component x = map.get(arg);
			return x != null ? new DirectTextNode(x) : null;
		});
	}

	@Override
	public TextNode[] parseNodes(TextNode text) {
		if (text instanceof TranslatedNode translatedNode) {
			return new TextNode[]{translatedNode.transform(this)};
		} else if (text instanceof LiteralNode(String value)) {
			ArrayList<TextNode> out = new ArrayList<>();

			Matcher matcher = this.pattern.matcher(value);

			int previousEnd = 0;

			while (matcher.find()) {
				String placeholder = matcher.group("id");
				int start = matcher.start();
				int end = matcher.end();

				TextNode output = this.placeholderProvider.apply(placeholder);

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
			ArrayList<TextNode> out = new ArrayList<>();

			for (TextNode text1 : parentNode.getChildren()) {
				out.add(TextNode.asSingle(this.parseNodes(text1)));
			}

			return new TextNode[]{parentNode.copyWith(out.toArray(new TextNode[0]), this)};
		}

		return new TextNode[]{text};
	}
}
