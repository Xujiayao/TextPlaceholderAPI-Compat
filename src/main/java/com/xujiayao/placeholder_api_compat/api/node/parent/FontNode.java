package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public final class FontNode extends SimpleStylingNode {
	private final ResourceLocation font;

	public FontNode(TextNode[] children, ResourceLocation font) {
		super(children);
		this.font = font;
	}

	@Override
	protected Style style(ParserContext context) {
		return Style.EMPTY.withFont(this.font);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new FontNode(children, this.font);
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.font);
		return "FontNode{font=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}
}
