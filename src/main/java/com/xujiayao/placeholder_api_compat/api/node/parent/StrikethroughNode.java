package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.network.chat.Style;

import java.util.Arrays;

public final class StrikethroughNode extends SimpleStylingNode {
	private static final Style TRUE;
	private static final Style FALSE;

	static {
		TRUE = Style.EMPTY.withStrikethrough(true);
		FALSE = Style.EMPTY.withStrikethrough(false);
	}

	private final boolean value;

	public StrikethroughNode(TextNode[] nodes, boolean value) {
		super(nodes);
		this.value = value;
	}

	@Override
	protected Style style(ParserContext context) {
		return this.value ? TRUE : FALSE;
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new StrikethroughNode(children, this.value);
	}

	@Override
	public String toString() {
		String var10000 = Arrays.toString(this.children);
		return "StrikethroughNode{children=" + var10000 + ", value=" + this.value + "}";
	}
}
