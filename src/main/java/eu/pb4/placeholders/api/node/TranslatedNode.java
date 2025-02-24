package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record TranslatedNode(String key, @Nullable String fallback, Object[] args) implements TextNode {
	@Deprecated
	public TranslatedNode(String key, Object[] args) {
		this(key, null, new Object[0]);
	}

	public TranslatedNode(String key) {
		this(key, new Object[0]);
	}

	public static TranslatedNode of(String key, Object... args) {
		return new TranslatedNode(key, null, args);
	}

	public static TranslatedNode ofFallback(String key, @Nullable String fallback, Object... args) {
		return new TranslatedNode(key, fallback, args);
	}

	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		Object[] args = new Object[this.args.length];
		for (int i = 0; i < this.args.length; i++) {
			args[i] = this.args[i] instanceof TextNode textNode ? textNode.toText(context, removeBackslashes) : this.args[i];
		}

		return Component.translatableWithFallback(this.key(), this.fallback, args);
	}

	@Override
	public boolean isDynamic() {
		for (Object obj : this.args) {
			if (obj instanceof TextNode t) {
				if (t.isDynamic()) {
					return true;
				}
			}
		}

		return false;
	}

	public TextNode transform(NodeParser parser) {
		if (this.args.length == 0) {
			return this;
		} else {
			ArrayList<Object> list = new ArrayList<>();
			for (Object arg : this.args()) {
				if (arg instanceof TextNode textNode) {
					list.add(parser.parseNode(textNode));
				} else {
					list.add(arg);
				}
			}

			return ofFallback(this.key(), this.fallback(), list.toArray());
		}
	}
}
