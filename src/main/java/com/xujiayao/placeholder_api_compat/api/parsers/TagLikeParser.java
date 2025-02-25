package com.xujiayao.placeholder_api_compat.api.parsers;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import com.xujiayao.placeholder_api_compat.api.Placeholders;
import com.xujiayao.placeholder_api_compat.api.node.DirectTextNode;
import com.xujiayao.placeholder_api_compat.api.node.DynamicTextNode;
import com.xujiayao.placeholder_api_compat.api.node.LiteralNode;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.TranslatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentTextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.format.MultiCharacterFormat;
import com.xujiayao.placeholder_api_compat.api.parsers.format.SingleCharacterFormat;
import com.xujiayao.placeholder_api_compat.impl.placeholder.PlaceholderNode;
import com.xujiayao.placeholder_api_compat.impl.textparser.MultiTagLikeParser;
import com.xujiayao.placeholder_api_compat.impl.textparser.SingleTagLikeParser;
import com.xujiayao.placeholder_api_compat.impl.textparser.providers.LenientFormat;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class TagLikeParser implements NodeParser, TagLikeWrapper {
	public static final Format TAGS = TagLikeParser.Format.of('<', '>', ' ');
	public static final Format TAGS_LENIENT = new LenientFormat();
	public static final Format TAGS_LEGACY = new SingleCharacterFormat('<', '>', ':', new char[]{'\''});
	public static final Format PLACEHOLDER = TagLikeParser.Format.of('%', '%', ' ');
	public static final Format PLACEHOLDER_ALTERNATIVE = TagLikeParser.Format.of('{', '}', ' ');
	public static final Format PLACEHOLDER_ALTERNATIVE_DOUBLE = TagLikeParser.Format.of("{{", "}}", " ");
	public static final Format PLACEHOLDER_USER = TagLikeParser.Format.of("${", "}", "");
	private static final TextNode[] EMPTY = new TextNode[0];

	public static TagLikeParser placeholder(Format format, ParserContext.Key<PlaceholderContext> contextKey, Placeholders.PlaceholderGetter placeholders) {
		return new SingleTagLikeParser(format, TagLikeParser.Provider.placeholder(contextKey, placeholders));
	}

	public static TagLikeParser placeholder(Format format, Function<String, @Nullable TextNode> placeholders) {
		return new SingleTagLikeParser(format, TagLikeParser.Provider.placeholder(placeholders));
	}

	public static TagLikeParser placeholderText(Format format, Function<String, @Nullable Component> placeholders) {
		return new SingleTagLikeParser(format, TagLikeParser.Provider.placeholderText(placeholders));
	}

	public static TagLikeParser placeholderText(Format format, ParserContext.Key<Function<String, @Nullable Component>> key) {
		return new SingleTagLikeParser(format, TagLikeParser.Provider.placeholder(key));
	}

	public static TagLikeParser placeholderText(Format format, Set<String> validIds, ParserContext.Key<Function<String, @Nullable Component>> key) {
		return new SingleTagLikeParser(format, TagLikeParser.Provider.placeholder(validIds, key));
	}

	public static TagLikeParser of(Format format, Provider provider) {
		return new SingleTagLikeParser(format, provider);
	}

	@SuppressWarnings("unchecked")
	public static TagLikeParser of(Pair<Format, Provider>... formatsAndProviders) {
		return new MultiTagLikeParser(formatsAndProviders);
	}

	@SuppressWarnings("unchecked")
	public static TagLikeParser of(Map<Format, Provider> formatsAndProviders) {
		ArrayList<Pair<Format, Provider>> list = new ArrayList<>(formatsAndProviders.size());

		for (Map.Entry<Format, Provider> entry : formatsAndProviders.entrySet()) {
			list.add(Pair.of(entry));
		}
		return new MultiTagLikeParser(list.toArray(new Pair[0]));
	}

	@Override
	public TextNode[] parseNodes(TextNode input) {
		Context context = new Context(this, "");
		this.parse(input, context);
		return context.toTextNode();
	}

	private void parse(TextNode node, Context context) {
		switch (node) {
			case LiteralNode(String value) -> {
				context.input = value;
				this.handleLiteral(value, context);
			}
			case TranslatedNode translatedNode -> context.addNode(translatedNode.transform(this));
			case ParentTextNode parent -> {
				int size = context.size();
				context.pushWithParser(null, parent::copyWith);
				for (TextNode x : parent.getChildren()) {
					this.parse(x, context);
				}

				context.pop(context.size() - size);
			}
			case null, default -> context.addNode(node);
		}
	}

	protected abstract void handleLiteral(String value, Context context);

	@Override
	public TagLikeParser asTagLikeParser() {
		return this;
	}

	protected final int handleTag(String value, int pos, Format.Tag tag, Provider provider, Context context) {
		tag = provider.modifyTag(tag, context);
		if (tag == null) {
			context.addNode(new LiteralNode(value.substring(pos)));
			return -1;
		} else {
			if (tag.start() != 0 && tag.start() != pos) {
				context.addNode(new LiteralNode(value.substring(pos, tag.start())));
			}

			pos = tag.end();
			context.currentPos = tag.start;
			provider.handleTag(tag.id(), tag.argument(), context);
			return pos;
		}
	}

	public interface Provider {
		static Provider placeholder(final ParserContext.Key<PlaceholderContext> contextKey, final Placeholders.PlaceholderGetter placeholders) {
			return new Provider() {
				@Override
				public boolean isValidTag(String tag, Context context) {
					return placeholders.exists(tag);
				}

				@Override
				public void handleTag(String id, String argument, Context context) {
					context.addNode(new PlaceholderNode(contextKey, id, placeholders, placeholders.isContextOptional(), argument != null && !argument.isEmpty() ? argument : null));
				}
			};
		}

		static Provider placeholderText(Function<String, @Nullable Component> function) {
			return placeholder((x) -> {
				Component y = function.apply(x);
				return y != null ? new DirectTextNode(y) : null;
			});
		}

		static Provider placeholder(final Function<String, @Nullable TextNode> function) {
			return new Provider() {
				@Override
				public boolean isValidTag(String tag, Context context) {
					return function.apply(tag) != null;
				}

				@Override
				public void handleTag(String id, String argument, Context context) {
					TextNode x = function.apply(id);
					if (x != null) {
						context.addNode(x);
					}
				}
			};
		}

		static Provider placeholder(final Set<String> validTags, final ParserContext.Key<Function<String, Component>> key) {
			return new Provider() {
				@Override
				public boolean isValidTag(String tag, Context context) {
					return validTags.contains(tag);
				}

				@Override
				public void handleTag(String id, String argument, Context context) {
					context.addNode(new DynamicTextNode(id, key));
				}
			};
		}

		static Provider placeholder(final ParserContext.Key<Function<String, Component>> key) {
			return new Provider() {
				@Override
				public boolean isValidTag(String tag, Context context) {
					return true;
				}

				@Override
				public void handleTag(String id, String argument, Context context) {
					context.addNode(new DynamicTextNode(id, key));
				}
			};
		}

		boolean isValidTag(String tag, Context context);

		void handleTag(String id, String argument, Context context);

		default Format.Tag modifyTag(Format.Tag tag, Context context) {
			return tag;
		}
	}

	public interface Format {
		static Format of(char start, char end) {
			return new SingleCharacterFormat(start, end);
		}

		static Format of(char start, char end, char argumentSplitter) {
			return new SingleCharacterFormat(start, end, argumentSplitter);
		}

		static Format of(String start, String end, String argumentSplitter) {
			return new MultiCharacterFormat(start, end, argumentSplitter);
		}

		default @Nullable Tag findFirst(String string, int start, Provider provider, Context context) {
			int maxLength = string.length();
			for (int i = start; i < maxLength; ++i) {
				Tag x = this.findAt(string, i, provider, context);
				if (x != null) {
					return x;
				}
			}
			return null;
		}

		@Nullable Tag findAt(String string, int start, Provider provider, Context context);

		default int index() {
			return 0;
		}

		record Tag(int start, int end, String id, String argument, @Nullable Object extra) {
			public Tag(int start, int end, String id, String argument) {
				this(start, end, id, argument, null);
			}
		}
	}

	public static final class Context {
		private final Stack<Scope> stack = new Stack<>();
		private final TagLikeParser parser;
		private int currentPos;
		private String input;

		Context(TagLikeParser parser, String input) {
			this.parser = parser;
			this.input = input;
			this.stack.push(TagLikeParser.Scope.parent());
		}

		public String input() {
			return this.input;
		}

		public boolean contains(String id) {
			for (int i = 0; i < this.stack.size(); ++i) {
				if (id.equals(this.stack.get(this.stack.size() - i - 1).id)) {
					return true;
				}
			}

			return false;
		}

		public void pop() {
			if (this.stack.size() > 1) {
				Scope x = this.stack.pop();
				this.stack.peek().nodes.add(x.collapse(this.parser));
			}
		}

		public void pop(int count) {
			count = Math.min(count, this.stack.size() - 1);
			for (int i = 0; i < count; ++i) {
				Scope x = this.stack.pop();
				this.stack.peek().nodes.add(x.collapse(this.parser));
			}
		}

		public void pop(String id) {
			if (this.contains(id)) {
				Scope x;
				do {
					if (this.stack.size() <= 1) {
						return;
					}

					x = this.stack.pop();
					this.stack.peek().nodes.add(x.collapse(this.parser));
				} while (!id.equals(x.id));

			}
		}

		public void popOnly(String id) {
			if (this.contains(id)) {
				Stack<Scope> list = new Stack<>();

				while (this.stack.size() > 1) {
					Scope x = this.stack.pop();
					this.stack.peek().nodes.add(x.collapse(this.parser));
					if (id.equals(x.id)) {
						while (!list.isEmpty()) {
							this.stack.push(list.pop());
						}
						return;
					}
					list.add(new Scope(x.id, new ArrayList<>(), x.merger));
				}
			}
		}

		public void pop(Predicate<String> stopPredicate) {
			while (this.stack.size() > 1) {
				if (stopPredicate.test(this.stack.peek().id)) {
					return;
				}
				Scope x = this.stack.pop();
				this.stack.peek().nodes.add(x.collapse(this.parser));
			}
		}

		public void popInclusive(Predicate<String> stopPredicate) {
			while (this.stack.size() > 1) {
				Scope x = this.stack.pop();
				this.stack.peek().nodes.add(x.collapse(this.parser));
				if (stopPredicate.test(x.id)) {
					return;
				}
			}
		}

		@Nullable
		public String peekId() {
			return this.stack.peek().id;
		}

		public void pushParent() {
			this.stack.push(TagLikeParser.Scope.parent());
		}

		public void push(String id, Function<TextNode[], TextNode> merge) {
			this.stack.push(TagLikeParser.Scope.enclosing(id, merge));
		}

		public void pushWithParser(String id, BiFunction<TextNode[], NodeParser, TextNode> merge) {
			this.stack.push(TagLikeParser.Scope.enclosingParsed(id, merge));
		}

		public void addNode(TextNode node) {
			this.stack.peek().nodes.add(node);
		}

		public TextNode[] toTextNode() {
			while (!this.stack.isEmpty()) {
				Scope box = this.stack.pop();

				if (this.stack.isEmpty()) {
					return box.nodes().toArray(TagLikeParser.EMPTY);
				}

				this.stack.peek().nodes.add(box.collapse(this.parser));
			}

			return null;
		}

		public int size() {
			return this.stack.size() - 1;
		}

		public NodeParser parser() {
			return this.parser;
		}

		public int currentTagPos() {
			return this.currentPos;
		}
	}

	private record Scope(@Nullable String id, List<TextNode> nodes,
	                     BiFunction<TextNode[], NodeParser, TextNode> merger) {
		public static Scope parent() {
			return enclosing(ParentNode::new);
		}

		public static Scope enclosing(String id, Function<TextNode[], TextNode> merge) {
			return enclosingParsed(id, (a, b) -> merge.apply(a));
		}

		public static Scope enclosing(Function<TextNode[], TextNode> merge) {
			return enclosingParsed((a, b) -> merge.apply(a));
		}

		public static Scope enclosingParsed(BiFunction<TextNode[], NodeParser, TextNode> merge) {
			return new Scope(null, new ArrayList<>(), merge);
		}

		public static Scope enclosingParsed(String id, BiFunction<TextNode[], NodeParser, TextNode> merge) {
			return new Scope(id, new ArrayList<>(), merge);
		}

		public TextNode collapse(NodeParser parser) {
			return this.merger.apply(this.nodes().toArray(TagLikeParser.EMPTY), parser);
		}
	}
}
