package com.xujiayao.placeholder_api_compat.api;

import com.google.common.collect.ImmutableMap;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.api.parsers.NodeParser;
import com.xujiayao.placeholder_api_compat.api.parsers.PatternPlaceholderParser;
import com.xujiayao.placeholder_api_compat.api.parsers.TagLikeParser;
import com.xujiayao.placeholder_api_compat.impl.placeholder.builtin.PlayerPlaceholders;
import com.xujiayao.placeholder_api_compat.impl.placeholder.builtin.ServerPlaceholders;
import com.xujiayao.placeholder_api_compat.impl.placeholder.builtin.WorldPlaceholders;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class Placeholders {
	@Deprecated
	public static final Pattern PLACEHOLDER_PATTERN = PatternPlaceholderParser.PLACEHOLDER_PATTERN;
	@Deprecated
	public static final Pattern ALT_PLACEHOLDER_PATTERN = PatternPlaceholderParser.ALT_PLACEHOLDER_PATTERN;
	@Deprecated
	public static final Pattern PLACEHOLDER_PATTERN_CUSTOM = PatternPlaceholderParser.PLACEHOLDER_PATTERN_CUSTOM;
	@Deprecated
	public static final Pattern ALT_PLACEHOLDER_PATTERN_CUSTOM = PatternPlaceholderParser.ALT_PLACEHOLDER_PATTERN_CUSTOM;
	@Deprecated
	public static final Pattern PREDEFINED_PLACEHOLDER_PATTERN = PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN;

	private static final HashMap<Identifier, PlaceholderHandler> PLACEHOLDERS = new HashMap<>();
	public static final PlaceholderGetter DEFAULT_PLACEHOLDER_GETTER = new PlaceholderGetter() {
		@Override
		public PlaceholderHandler getPlaceholder(String placeholder) {
			return PLACEHOLDERS.get(Identifier.tryParse(placeholder));
		}

		@Override
		public boolean isContextOptional() {
			return false;
		}
	};
	public static final NodeParser DEFAULT_PLACEHOLDER_PARSER = TagLikeParser.placeholder(TagLikeParser.PLACEHOLDER, PlaceholderContext.KEY, DEFAULT_PLACEHOLDER_GETTER);
	private static final List<PlaceholderListChangedCallback> CHANGED_CALLBACKS = new ArrayList<>();

	static {
		PlayerPlaceholders.register();
		ServerPlaceholders.register();
		WorldPlaceholders.register();
	}

	/**
	 * Parses PlaceholderContext, can be used for parsing by hand
	 *
	 * @return PlaceholderResult
	 */
	public static PlaceholderResult parsePlaceholder(Identifier identifier, String argument, PlaceholderContext context) {
		if (PLACEHOLDERS.containsKey(identifier)) {
			return PLACEHOLDERS.get(identifier).onPlaceholderRequest(context, argument);
		} else {
			return PlaceholderResult.invalid("Placeholder doesn't exist!");
		}
	}

	/**
	 * Parses placeholders in nodes, without getting their final values
	 * Placeholders have format of {@code %namespace:placeholder argument%}
	 *
	 * @return Text
	 */
	public static ParentNode parseNodes(TextNode node) {
		return asSingleParent(DEFAULT_PLACEHOLDER_PARSER.parseNodes(node));
	}

	public static ParentNode parseNodes(TextNode node, ParserContext.Key<PlaceholderContext> contextKey) {
		return asSingleParent(TagLikeParser.placeholder(TagLikeParser.PLACEHOLDER, contextKey, DEFAULT_PLACEHOLDER_GETTER).parseNodes(node));
	}

	/**
	 * Parses placeholders in text
	 * Placeholders have format of {@code %namespace:placeholder argument%}
	 *
	 * @return Text
	 */
	public static Text parseText(Text text, PlaceholderContext context) {
		return parseNodes(TextNode.convert(text)).toText(ParserContext.of(PlaceholderContext.KEY, context));
	}

	public static Text parseText(TextNode textNode, PlaceholderContext context) {
		return parseNodes(textNode).toText(ParserContext.of(PlaceholderContext.KEY, context));
	}

	@Deprecated
	public static ParentNode parseNodes(TextNode node, Pattern pattern) {
		return parseNodes(node, pattern, PlaceholderContext.KEY);
	}

	@Deprecated
	public static ParentNode parseNodes(TextNode node, Pattern pattern, ParserContext.Key<PlaceholderContext> contextKey) {
		return asSingleParent(PatternPlaceholderParser.of(pattern, contextKey, DEFAULT_PLACEHOLDER_GETTER).parseNodes(node));
	}

	@Deprecated
	public static ParentNode parseNodes(TextNode node, Pattern pattern, PlaceholderGetter placeholderGetter) {
		return parseNodes(node, pattern, placeholderGetter, PlaceholderContext.KEY);
	}

	@Deprecated
	public static ParentNode parseNodes(TextNode node, Pattern pattern, PlaceholderGetter placeholderGetter, ParserContext.Key<PlaceholderContext> contextKey) {
		return asSingleParent(PatternPlaceholderParser.of(pattern, contextKey, placeholderGetter).parseNodes(node));
	}

	@Deprecated
	public static ParentNode parseNodes(TextNode node, Pattern pattern, Map<String, Text> placeholders) {
		return asSingleParent(PatternPlaceholderParser.ofTextMap(pattern, placeholders).parseNodes(node));
	}

	@Deprecated
	public static ParentNode parseNodes(TextNode node, Pattern pattern, Set<String> placeholders, ParserContext.Key<PlaceholderGetter> key) {
		return parseNodes(node, pattern, placeholders, key, PlaceholderContext.KEY);
	}

	@Deprecated
	public static ParentNode parseNodes(TextNode node, Pattern pattern, Set<String> placeholders, ParserContext.Key<PlaceholderGetter> key, ParserContext.Key<PlaceholderContext> contextKey) {
		return asSingleParent(PatternPlaceholderParser.of(pattern, contextKey, new PlaceholderGetter() {
			@Override
			public PlaceholderHandler getPlaceholder(String placeholder, ParserContext context) {
				var get = context.get(key);
				return get != null ? get.getPlaceholder(placeholder, context) : null;
			}

			@Override
			public PlaceholderHandler getPlaceholder(String placeholder) {
				return placeholders.contains(placeholder) ? PlaceholderHandler.EMPTY : null;
			}

			@Override
			public boolean isContextOptional() {
				return true;
			}
		}).parseNodes(node));
	}

	@Deprecated
	public static Text parseText(Text text, PlaceholderContext context, Pattern pattern) {
		return parseNodes(TextNode.convert(text), pattern).toText(ParserContext.of(PlaceholderContext.KEY, context));
	}

	@Deprecated
	public static Text parseText(Text text, PlaceholderContext context, Pattern pattern, PlaceholderGetter placeholderGetter) {
		return parseNodes(TextNode.convert(text), pattern, placeholderGetter).toText(ParserContext.of(PlaceholderContext.KEY, context));
	}

	@Deprecated
	public static Text parseText(Text text, Pattern pattern, Map<String, Text> placeholders) {
		return parseNodes(TextNode.convert(text), pattern, placeholders).toText(ParserContext.of());
	}

	@Deprecated
	public static Text parseText(Text text, Pattern pattern, Set<String> placeholders, ParserContext.Key<PlaceholderGetter> key) {
		return parseNodes(TextNode.convert(text), pattern, placeholders, key).toText(ParserContext.of());
	}

	@Deprecated
	public static Text parseText(TextNode textNode, PlaceholderContext context, Pattern pattern) {
		return parseNodes(textNode, pattern).toText(ParserContext.of(PlaceholderContext.KEY, context));
	}

	@Deprecated
	public static Text parseText(TextNode textNode, PlaceholderContext context, Pattern pattern, PlaceholderGetter placeholderGetter) {
		return parseNodes(textNode, pattern, placeholderGetter).toText(ParserContext.of(PlaceholderContext.KEY, context));
	}

	@Deprecated
	public static Text parseText(TextNode textNode, PlaceholderContext context, Pattern pattern, Map<String, Text> placeholders) {
		return parseNodes(textNode, pattern, placeholders).toText(ParserContext.of(PlaceholderContext.KEY, context));
	}

	@Deprecated
	public static Text parseText(TextNode textNode, Pattern pattern, Map<String, Text> placeholders) {
		return parseNodes(textNode, pattern, placeholders).toText();
	}

	@Deprecated
	public static Text parseText(TextNode textNode, Pattern pattern, Set<String> placeholders, ParserContext.Key<PlaceholderGetter> key) {
		return parseNodes(textNode, pattern, placeholders, key).toText();
	}

	/**
	 * Registers new placeholder for identifier
	 */
	public static void register(Identifier identifier, PlaceholderHandler handler) {
		PLACEHOLDERS.put(identifier, handler);
		for (var e : CHANGED_CALLBACKS) {
			e.onPlaceholderListChange(identifier, false);
		}
	}

	/**
	 * Removes placeholder
	 */
	public static void remove(Identifier identifier) {
		if (PLACEHOLDERS.remove(identifier) != null) {
			for (var e : CHANGED_CALLBACKS) {
				e.onPlaceholderListChange(identifier, true);
			}
		}
	}

	public static ImmutableMap<Identifier, PlaceholderHandler> getPlaceholders() {
		return ImmutableMap.copyOf(PLACEHOLDERS);
	}

	public static void registerChangeEvent(PlaceholderListChangedCallback callback) {
		CHANGED_CALLBACKS.add(callback);
	}

	private static ParentNode asSingleParent(TextNode... textNodes) {
		if (textNodes.length == 1 && textNodes[0] instanceof ParentNode) {
			return (ParentNode) textNodes[0];
		} else {
			return new ParentNode(textNodes);
		}
	}

	public interface PlaceholderListChangedCallback {
		void onPlaceholderListChange(Identifier identifier, boolean removed);
	}

	public interface PlaceholderGetter {
		@Nullable
		PlaceholderHandler getPlaceholder(String placeholder);

		@Nullable
		default PlaceholderHandler getPlaceholder(String placeholder, ParserContext context) {
			return getPlaceholder(placeholder);
		}

		default boolean isContextOptional() {
			return false;
		}

		default boolean exists(String placeholder) {
			return this.getPlaceholder(placeholder) != null;
		}
	}
}
