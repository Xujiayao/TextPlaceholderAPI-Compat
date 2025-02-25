package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.network.chat.Component;

public record DirectTextNode(Component text) implements TextNode {
	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		return this.text;
	}
}
