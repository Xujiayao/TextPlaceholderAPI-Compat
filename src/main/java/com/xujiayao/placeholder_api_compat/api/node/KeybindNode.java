package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.network.chat.Component;

public record KeybindNode(String value) implements TextNode {
	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		return Component.keybind(this.value());
	}
}
