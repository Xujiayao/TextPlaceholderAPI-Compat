package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.text.Text;

public record DirectTextNode(Text text) implements TextNode {
	@Override
	public Text toText(ParserContext context, boolean removeBackslashes) {
		return this.text;
	}
}
