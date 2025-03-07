package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.Arrays;

public final class FormattingNode extends SimpleStylingNode {
	private final ChatFormatting[] formatting;

	public FormattingNode(TextNode[] children, ChatFormatting formatting) {
		this(children, new ChatFormatting[]{formatting});
	}

	public FormattingNode(TextNode[] children, ChatFormatting... formatting) {
		super(children);
		this.formatting = formatting;
	}

	@Override
	protected Style style(ParserContext context) {
		return Style.EMPTY.applyFormats(this.formatting);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new FormattingNode(children, this.formatting);
	}

	@Override
	public String toString() {
		String var10000 = Arrays.toString(this.formatting);
		return "FormattingNode{formatting=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}
}
