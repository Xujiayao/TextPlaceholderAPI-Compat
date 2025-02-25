package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.DataSource;

import java.util.Optional;

public record NbtNode(String rawPath, boolean interpret, Optional<TextNode> separator,
                      DataSource dataSource) implements TextNode {
	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		return Component.nbt(this.rawPath, this.interpret, this.separator.map((x) -> x.toText(context, removeBackslashes)), this.dataSource);
	}

	@Override
	public boolean isDynamic() {
		return this.separator.isPresent() && this.separator.get().isDynamic();
	}
}
