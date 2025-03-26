package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.text.Style;

import java.util.Arrays;

public final class BoldNode extends SimpleStylingNode {
	private static final Style TRUE = Style.EMPTY.withBold(true);
	private static final Style FALSE = Style.EMPTY.withBold(false);
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
		return "BoldNode{" +
				"value=" + value +
				", children=" + Arrays.toString(children) +
				'}';
	}

	@Override
	protected Style style(ParserContext context) {
		return this.value ? TRUE : FALSE;
	}
}
