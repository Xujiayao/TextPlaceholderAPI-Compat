package com.xujiayao.placeholder_api_compat.api.parsers;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import com.xujiayao.placeholder_api_compat.api.Placeholders;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.tag.TagRegistry;
import com.xujiayao.placeholder_api_compat.impl.textparser.MultiTagLikeParser;
import com.xujiayao.placeholder_api_compat.impl.textparser.SingleTagLikeParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Allows you to create stacked parser in most "correct" and compatible way.
 */
public class ParserBuilder {
	private final Map<TagLikeParser.Format, TagLikeParser.Provider> tagLike = new HashMap<>();
	private final List<NodeParser> parserList = new ArrayList<>();
	private final List<ChatFormatting> legacyFormatting = new ArrayList<>();
	private boolean hasLegacy = false;
	private boolean legacyRGB = false;
	private boolean simplifiedTextFormat;
	private boolean quickText;
	private boolean safeOnly;
	private TagRegistry customTagRegistry;
	private boolean staticPreParsing;

	public static ParserBuilder of() {
		return new ParserBuilder();
	}

	/**
	 * Enables parsing of Global Placeholders (aka {@link Placeholders})
	 */
	public ParserBuilder globalPlaceholders() {
		return this.add(Placeholders.DEFAULT_PLACEHOLDER_PARSER);
	}

	/**
	 * Enables parsing of Global Placeholders, but with a custom format
	 */
	public ParserBuilder globalPlaceholders(TagLikeParser.Format format) {
		return this.customTags(format, TagLikeParser.Provider.placeholder(PlaceholderContext.KEY, Placeholders.DEFAULT_PLACEHOLDER_GETTER));
	}

	/**
	 * Enables parsing of Global Placeholder, but with a custom format and context source
	 */
	public ParserBuilder globalPlaceholders(TagLikeParser.Format format, ParserContext.Key<PlaceholderContext> contextKey) {
		return this.customTags(format, TagLikeParser.Provider.placeholder(contextKey, Placeholders.DEFAULT_PLACEHOLDER_GETTER));
	}

	/**
	 * Enables parsing of custom placeholder with a custom format and context source
	 */
	public ParserBuilder placeholders(TagLikeParser.Format format, ParserContext.Key<PlaceholderContext> contextKey, Placeholders.PlaceholderGetter getter) {
		return this.customTags(format, TagLikeParser.Provider.placeholder(contextKey, getter));
	}

	/**
	 * Enables parsing of custom placeholder with a functional provider
	 */
	public ParserBuilder placeholders(TagLikeParser.Format format, Function<String, TextNode> function) {
		return this.customTags(format, TagLikeParser.Provider.placeholder(function));
	}

	/**
	 * Enables parsing of custom, context dependent placeholders
	 */
	public ParserBuilder placeholders(TagLikeParser.Format format, ParserContext.Key<Function<String, Component>> key) {
		return this.customTags(format, TagLikeParser.Provider.placeholder(key));
	}

	/**
	 * Enables parsing of custom, context dependent placeholders
	 */
	public ParserBuilder placeholders(TagLikeParser.Format format, Set<String> tags, ParserContext.Key<Function<String, Component>> key) {
		return this.customTags(format, TagLikeParser.Provider.placeholder(tags, key));
	}

	/**
	 * Enables QuickText format.
	 */
	public ParserBuilder quickText() {
		this.quickText = true;
		return this;
	}

	/**
	 * Enables Simplified Text Format.
	 */
	public ParserBuilder simplifiedTextFormat() {
		this.simplifiedTextFormat = true;
		return this;
	}

	/**
	 * Forces usage of safe tags for tag parsers.
	 */
	public ParserBuilder requireSafe() {
		this.safeOnly = true;
		return this;
	}

	/**
	 * Forces usage of custom registry for tag parsers.
	 */
	public ParserBuilder customTagRegistry(TagRegistry registry) {
		this.customTagRegistry = registry;
		return this;
	}

	/**
	 * Enables Markdown.
	 */
	public ParserBuilder markdown() {
		return this.add(MarkdownLiteParserV1.ALL);
	}

	/**
	 * Enables Markdown with limited formatting.
	 */
	public ParserBuilder markdown(MarkdownLiteParserV1.MarkdownFormat... formats) {
		return this.add(new MarkdownLiteParserV1(formats));
	}

	/**
	 * Enables Markdown with limited formatting.
	 */
	public ParserBuilder markdown(Collection<MarkdownLiteParserV1.MarkdownFormat> formats) {
		return this.add(new MarkdownLiteParserV1(formats.toArray(new MarkdownLiteParserV1.MarkdownFormat[0])));
	}

	/**
	 * Enables Markdown with limited formatting.
	 */
	public ParserBuilder markdown(Function<TextNode[], TextNode> spoilerFormatting, Function<TextNode[], TextNode> quoteFormatting, BiFunction<TextNode[], TextNode, TextNode> urlFormatting, MarkdownLiteParserV1.MarkdownFormat... formatting) {
		return this.add(new MarkdownLiteParserV1(spoilerFormatting, quoteFormatting, urlFormatting, formatting));
	}

	/**
	 * Enables Markdown with limited formatting.
	 */
	public ParserBuilder markdown(Function<TextNode[], TextNode> spoilerFormatting, Function<TextNode[], TextNode> quoteFormatting, BiFunction<TextNode[], TextNode, TextNode> urlFormatting, Collection<MarkdownLiteParserV1.MarkdownFormat> formatting) {
		return this.add(new MarkdownLiteParserV1(spoilerFormatting, quoteFormatting, urlFormatting, formatting.toArray(new MarkdownLiteParserV1.MarkdownFormat[0])));
	}

	/**
	 * Enables legacy color tags (&X) with rgb extension.
	 */
	public ParserBuilder legacyColor() {
		return this.add(LegacyFormattingParser.COLORS);
	}

	/**
	 * Enables legacy color tags (&X).
	 */
	public ParserBuilder legacyVanillaColor() {
		return this.add(LegacyFormattingParser.BASE_COLORS);
	}

	/**
	 * Enables all legacy formatting (&X) with rgb extension.
	 */
	public ParserBuilder legacyAll() {
		return this.add(LegacyFormattingParser.ALL);
	}

	/**
	 * Enables legacy formatting.
	 */
	public ParserBuilder legacy(boolean allowRGB, ChatFormatting... formatting) {
		this.hasLegacy = true;
		this.legacyRGB = allowRGB;
		this.legacyFormatting.addAll(List.of(formatting));

		return this;
	}

	/**
	 * Enables legacy formatting.
	 */
	public ParserBuilder legacy(boolean allowRGB, Collection<ChatFormatting> formatting) {
		this.hasLegacy = true;
		this.legacyRGB = allowRGB;
		this.legacyFormatting.addAll(formatting);

		return this;
	}

	/**
	 * Adds custom tag like parser
	 */
	public ParserBuilder customTags(TagLikeParser.Format format, TagLikeParser.Provider provider) {
		this.tagLike.put(format, provider);
		return this;
	}

	/**
	 * Enables pre-parsing for static elements.
	 * This should only be used if you don't convert to {@link Component} right away, but also don't transform
	 * it further yourself (aka you use TextNode's as a template with custom placeholders)
	 */
	public ParserBuilder staticPreParsing() {
		this.staticPreParsing = true;
		return this;
	}

	public ParserBuilder add(NodeParser parser) {
		if (parser instanceof TagLikeWrapper wrapper) {
			TagLikeParser x = wrapper.asTagLikeParser();
			if (x instanceof SingleTagLikeParser p) {
				return this.customTags(p.format(), p.provider());
			}

			if (x instanceof MultiTagLikeParser p) {
				this.tagLike.putAll(Map.ofEntries(p.pairs()));
				return this;
			}
		} else if (parser instanceof LegacyFormattingParser legacyFormattingParser) {
			this.hasLegacy = true;
			this.legacyFormatting.addAll(legacyFormattingParser.formatting());
			this.legacyRGB |= legacyFormattingParser.allowRGB();
		}

		return this.forceAdd(parser);
	}

	public ParserBuilder forceAdd(NodeParser parser) {
		this.parserList.add(parser);
		return this;
	}

	public NodeParser build() {
		ArrayList<NodeParser> list = new ArrayList<>(this.parserList.size() + 1);
		if (!this.tagLike.isEmpty()) {
			list.add(TagLikeParser.of(this.tagLike));
		}

		TagRegistry reg = this.customTagRegistry != null ? this.customTagRegistry : (this.safeOnly ? TagRegistry.SAFE : TagRegistry.DEFAULT);

		if (this.quickText && this.simplifiedTextFormat) {
			list.add(TagParser.createQuickTextWithSTF(reg));
		} else if (this.quickText) {
			list.add(TagParser.createQuickText(reg));
		} else if (this.simplifiedTextFormat) {
			list.add(TagParser.createSimplifiedTextFormat(reg));
		}

		list.addAll(this.parserList);

		if (this.hasLegacy) {
			list.add(new LegacyFormattingParser(this.legacyRGB, this.legacyFormatting.toArray(new ChatFormatting[0])));
		}

		if (this.staticPreParsing) {
			list.add(StaticPreParser.INSTANCE);
		}

		return NodeParser.merge(list);
	}
}
