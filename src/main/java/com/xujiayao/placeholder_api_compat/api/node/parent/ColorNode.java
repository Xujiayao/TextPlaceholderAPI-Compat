package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Arrays;

public final class ColorNode extends SimpleStylingNode {
	private final TextColor color;

	public ColorNode(TextNode[] children, TextColor color) {
		super(children);
		this.color = color;
	}

	@Override
	protected Style style(ParserContext context) {
		return Style.EMPTY.withColor(this.color);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new ColorNode(children, this.color);
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.color);
		return "ColorNode{color=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}
}
