package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.network.chat.Component;

/**
 * It works as long as no parser implements support for it™
 */
public record NonTransformableNode(TextNode node) implements TextNode {
	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		return this.node.toText(context, removeBackslashes);
	}

	@Override
	public boolean isDynamic() {
		return this.node.isDynamic();
	}
}
