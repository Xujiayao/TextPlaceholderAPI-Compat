package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.network.chat.Style;

import java.util.Arrays;

public final class UnderlinedNode extends SimpleStylingNode {
	private static final Style TRUE;
	private static final Style FALSE;

	static {
		TRUE = Style.EMPTY.withUnderlined(true);
		FALSE = Style.EMPTY.withUnderlined(false);
	}

	private final boolean value;

	public UnderlinedNode(TextNode[] nodes, boolean value) {
		super(nodes);
		this.value = value;
	}

	@Override
	protected Style style(ParserContext context) {
		return this.value ? TRUE : FALSE;
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new UnderlinedNode(children, this.value);
	}

	@Override
	public String toString() {
		String var10000 = Arrays.toString(this.children);
		return "UnderlinedNode{children=" + var10000 + ", value=" + this.value + "}";
	}
}
