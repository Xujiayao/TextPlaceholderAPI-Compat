package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.network.chat.Component;

public record EmptyNode() implements TextNode {
	public static final EmptyNode INSTANCE = new EmptyNode();

	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		return Component.empty();
	}
}
