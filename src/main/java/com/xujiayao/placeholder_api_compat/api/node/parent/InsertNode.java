package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.NodeParser;
import net.minecraft.network.chat.Style;

import java.util.Arrays;

public final class InsertNode extends SimpleStylingNode {
	private final TextNode value;

	public InsertNode(TextNode[] children, TextNode value) {
		super(children);
		this.value = value;
	}

	public TextNode value() {
		return this.value;
	}

	@Override
	protected Style style(ParserContext context) {
		return Style.EMPTY.withInsertion(this.value.toText(context, true).getString());
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new InsertNode(children, this.value);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
		return new InsertNode(children, TextNode.asSingle(parser.parseNodes(this.value)));
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.value);
		return "InsertNode{value=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}

	@Override
	public boolean isDynamicNoChildren() {
		return this.value.isDynamic();
	}
}
