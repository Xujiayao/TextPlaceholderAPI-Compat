package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.text.Style;

import java.util.Arrays;

public final class ItalicNode extends SimpleStylingNode {
	private static final Style TRUE = Style.EMPTY.withItalic(true);
	private static final Style FALSE = Style.EMPTY.withItalic(false);

	private final boolean value;

	public ItalicNode(TextNode[] nodes, boolean value) {
		super(nodes);
		this.value = value;
	}

	@Override
	protected Style style(ParserContext context) {
		return this.value ? TRUE : FALSE;
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new ItalicNode(children, this.value);
	}


	@Override
	public String toString() {
		return "ItalicNode{" +
				"value=" + value +
				", children=" + Arrays.toString(children) +
				'}';
	}
}
