package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.text.Style;

import java.util.Arrays;

public final class ObfuscatedNode extends SimpleStylingNode {
	private static final Style TRUE = Style.EMPTY.withObfuscated(true);
	private static final Style FALSE = Style.EMPTY.withObfuscated(false);
	private final boolean value;

	public ObfuscatedNode(TextNode[] nodes, boolean value) {
		super(nodes);
		this.value = value;
	}

	@Override
	protected Style style(ParserContext context) {
		return this.value ? TRUE : FALSE;
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new ObfuscatedNode(children, this.value);
	}

	@Override
	public String toString() {
		return "ObfuscatedNode{" +
				"value=" + value +
				", children=" + Arrays.toString(children) +
				'}';
	}
}
