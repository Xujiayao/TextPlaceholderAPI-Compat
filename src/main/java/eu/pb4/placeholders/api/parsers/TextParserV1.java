package eu.pb4.placeholders.api.parsers;

import com.google.common.collect.ImmutableList;
import eu.pb4.placeholders.api.arguments.StringArgs;
import eu.pb4.placeholders.api.node.EmptyNode;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.parent.ParentNode;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import eu.pb4.placeholders.impl.GeneralUtils;
import eu.pb4.placeholders.impl.textparser.TextParserImpl;
import eu.pb4.placeholders.impl.textparser.TextTagsV1;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Original parser implementing Simple Text Format
 * <a href="https://placeholders.pb4.eu/user/text-format/">Format documentation</a>
 * <p>
 * Regex-based text parsing implementation. Should be always used first.
 * Loosely based on MiniMessage, with some degree of compatibility with it.
 *
 * @Deprecated Replaced with {@link TagParser}
 */
@Deprecated
public class TextParserV1 implements NodeParser {

	public static final TextParserV1 DEFAULT = new TextParserV1();
	public static final TextParserV1 SAFE = new TextParserV1();

	static {
		TextTagsV1.register();
	}

	private final List<TextTag> tags = new ArrayList<>();
	private final Map<String, TextTag> byName = new HashMap<>();
	private final Map<String, TextTag> byNameAlias = new HashMap<>();

	public static TextParserV1 createDefault() {
		return DEFAULT.copy();
	}

	public static TextParserV1 createSafe() {
		return SAFE.copy();
	}

	public static void registerDefault(TextTag tag) {
		DEFAULT.register(tag);

		if (tag.userSafe()) {
			SAFE.register(tag);
		}
	}

	public static TextNode[] parseNodesWith(TextNode input, TagParserGetter getter) {
		if (input instanceof LiteralNode(String value)) {
			return TextParserImpl.parse(value, getter);
		} else if (input instanceof ParentTextNode parentTextNode) {
			ArrayList<TextNode> list = new ArrayList<>();

			for (TextNode child : parentTextNode.getChildren()) {
				list.add(new ParentNode(parseNodesWith(child, getter)));
			}

			return list.toArray(new TextNode[0]);
		}

		return new TextNode[]{input};
	}

	public static NodeList parseNodesWith(String input, TagParserGetter handlers, @Nullable String endingTag) {
		return TextParserImpl.recursiveParsing(input, handlers, endingTag);
	}

	public void register(TextTag tag) {
		if (this.byName.containsKey(tag.name())) {
			throw new RuntimeException("Duplicate tag identifier!");
		}

		this.byName.put(tag.name(), tag);
		this.tags.add(tag);

		this.byNameAlias.put(tag.name(), tag);

		if (tag.aliases() != null) {
			for (int i = 0; i < tag.aliases().length; ++i) {
				String alias = tag.aliases()[i];
				TextTag old = this.byNameAlias.get(alias);
				if (old == null || !old.name().equals(alias)) {
					this.byNameAlias.put(alias, tag);
				}
			}
		}
	}

	public List<TextTag> getTags() {
		return ImmutableList.copyOf(this.tags);
	}

	@Override
	public TextNode[] parseNodes(TextNode input) {
		return parseNodesWith(input, this::getTagParser);
	}

	public TextParserV1 copy() {
		TextParserV1 parser = new TextParserV1();
		for (TextTag tag : this.tags) {
			parser.register(tag);
		}
		return parser;
	}

	public @Nullable TagNodeBuilder getTagParser(String name) {
		TextTag o = this.byNameAlias.get(name);
		return o != null ? o.parser() : null;
	}

	public @Nullable TextTag getTag(String name) {
		return this.byNameAlias.get(name);
	}

	@FunctionalInterface
	public interface TagNodeBuilder {
		static TagNodeBuilder selfClosing(SelfTagParsedCreator selfTagCreator) {
			return (tag, data, input, handlers, endAt) -> new TagNodeValue(selfTagCreator.createTextNode(data, new TagParserGetterParser(handlers)), 0);
		}

		static TagNodeBuilder wrapping(FormattingTagParsedCreator formattingTagCreator) {
			return (tag, data, input, handlers, endAt) -> {
				NodeList out = parseNodesWith(input, handlers, endAt);
				return new TextParserV1.TagNodeValue(formattingTagCreator.createTextNode(out.nodes(), data, new TagParserGetterParser(handlers)), out.length());
			};
		}

		static TagNodeBuilder selfClosing(SelfTagCreator selfTagCreator) {
			return (tag, data, input, handlers, endAt) -> new TagNodeValue(selfTagCreator.createTextNode(data), 0);
		}

		static TagNodeBuilder wrapping(FormattingTagCreator formattingTagCreator) {
			return (tag, data, input, handlers, endAt) -> {
				NodeList out = parseNodesWith(input, handlers, endAt);
				return new TextParserV1.TagNodeValue(formattingTagCreator.createTextNode(out.nodes(), data), out.length());
			};
		}

		static TagNodeBuilder wrappingBoolean(BooleanFormattingTagCreator formattingTagCreator) {
			return (tag, data, input, handlers, endAt) -> {
				NodeList out = parseNodesWith(input, handlers, endAt);
				return new TextParserV1.TagNodeValue(formattingTagCreator.createTextNode(out.nodes(), data == null || !data.equals("false")), out.length());
			};
		}

		TagNodeValue parseString(String tag, String data, String input, TagParserGetter tags, String endAt);

		interface SelfTagCreator {
			TextNode createTextNode(String arg);
		}

		interface FormattingTagCreator {
			TextNode createTextNode(TextNode[] nodes, String arg);
		}

		interface SelfTagParsedCreator {
			TextNode createTextNode(String arg, NodeParser parser);
		}

		interface FormattingTagParsedCreator {
			TextNode createTextNode(TextNode[] nodes, String arg, NodeParser parser);
		}

		interface BooleanFormattingTagCreator {
			TextNode createTextNode(TextNode[] nodes, boolean arg);
		}
	}

	@FunctionalInterface
	public interface TagParserGetter {
		@Nullable TagNodeBuilder getTagParser(String name);
	}

	public record TextTag(String name, String[] aliases, String type, boolean userSafe, TagNodeBuilder parser) {
		public static TextTag of(String name, String type, TagNodeBuilder parser) {
			return of(name, type, true, parser);
		}

		public static TextTag of(String name, String type, boolean userSafe, TagNodeBuilder parser) {
			return of(name, List.of(), type, userSafe, parser);
		}

		public static TextTag of(String name, List<String> aliases, String type, boolean userSafe, TagNodeBuilder parser) {
			return new TextTag(name, aliases.toArray(new String[0]), type, userSafe, parser);
		}

		public static TextTag from(eu.pb4.placeholders.api.parsers.tag.TextTag tag) {
			return new TextTag(tag.name(), tag.aliases(), tag.type(), tag.userSafe(), tag.selfContained() ? TextParserV1.TagNodeBuilder.selfClosing((a, b) -> tag.nodeCreator().createTextNode(GeneralUtils.CASTER, StringArgs.ordered(a, ':'), b)) : TextParserV1.TagNodeBuilder.wrapping((a, b, c) -> tag.nodeCreator().createTextNode(a, StringArgs.ordered(b, ':'), c)));
		}
	}

	public record TagNodeValue(TextNode node, int length) {
		public static final TagNodeValue EMPTY = new TagNodeValue(EmptyNode.INSTANCE, 0);
	}

	public record NodeList(TextNode[] nodes, int length) {
		public static final NodeList EMPTY = new NodeList(new TextNode[0], 0);

		public TagNodeValue value(TextNode node) {
			return new TagNodeValue(node, this.length);
		}

		public TagNodeValue value(Function<TextNode[], TextNode> function) {
			return new TagNodeValue(function.apply(this.nodes), this.length);
		}
	}

	private record TagParserGetterParser(TagParserGetter getter) implements NodeParser {
		@Override
		public TextNode[] parseNodes(TextNode input) {
			return parseNodesWith(input, this.getter);
		}
	}
}
