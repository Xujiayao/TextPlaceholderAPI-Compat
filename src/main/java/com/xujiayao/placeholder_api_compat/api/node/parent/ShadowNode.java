package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.text.Style;

import java.util.Arrays;

public final class ShadowNode extends SimpleStylingNode {
	private final int color;

	public ShadowNode(TextNode[] children, int color) {
		super(children);
		this.color = color;
	}

	@Override
	protected Style style(ParserContext context) {
		return Style.EMPTY.withShadowColor(this.color);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new ShadowNode(children, this.color);
	}

	@Override
	public String toString() {
		return "ShadowNode{" +
				"color=" + color +
				", children=" + Arrays.toString(children) +
				'}';
	}
}
