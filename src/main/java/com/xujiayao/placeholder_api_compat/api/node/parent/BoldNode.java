package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.network.chat.Style;

import java.util.Arrays;

public final class BoldNode extends SimpleStylingNode {
	private static final Style TRUE;
	private static final Style FALSE;

	static {
		TRUE = Style.EMPTY.withBold(true);
		FALSE = Style.EMPTY.withBold(false);
	}

	private final boolean value;

	public BoldNode(TextNode[] nodes, boolean value) {
		super(nodes);
		this.value = value;
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new BoldNode(children, this.value);
	}

	@Override
	public String toString() {
		return "BoldNode{value=" + this.value + ", children=" + Arrays.toString(this.children) + "}";
	}

	@Override
	protected Style style(ParserContext context) {
		return this.value ? TRUE : FALSE;
	}
}
