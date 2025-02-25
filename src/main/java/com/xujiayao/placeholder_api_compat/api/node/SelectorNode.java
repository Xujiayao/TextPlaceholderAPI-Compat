package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
//#if MC > 12101
import net.minecraft.commands.arguments.selector.SelectorPattern;
//#endif
import net.minecraft.network.chat.Component;

import java.util.Optional;

//#if MC > 12101
public record SelectorNode(SelectorPattern selector, Optional<TextNode> separator) implements TextNode {
//#else
//$$ public record SelectorNode(String pattern, Optional<TextNode> separator) implements TextNode {
//#endif
	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		//#if MC > 12101
		return Component.selector(this.selector, this.separator.map((x) -> x.toText(context, removeBackslashes)));
		//#else
		//$$ return Component.selector(this.pattern, this.separator.map((x) -> x.toText(context, removeBackslashes)));
		//#endif
	}

	@Override
	public boolean isDynamic() {
		return this.separator.isPresent() && this.separator.get().isDynamic();
	}
}
