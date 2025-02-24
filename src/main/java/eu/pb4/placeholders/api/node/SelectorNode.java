package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public record SelectorNode(SelectorPattern selector, Optional<TextNode> separator) implements TextNode {
	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		return Component.selector(this.selector, this.separator.map((x) -> x.toText(context, removeBackslashes)));
	}

	@Override
	public boolean isDynamic() {
		return this.separator.isPresent() && this.separator.get().isDynamic();
	}
}
