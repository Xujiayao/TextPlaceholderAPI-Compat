package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.text.Style;

import java.util.Collection;

public abstract class SimpleStylingNode extends ParentNode {

	public SimpleStylingNode(TextNode... children) {
		super(children);
	}

	public SimpleStylingNode(Collection<TextNode> children) {
		super(children);
	}

	@Override
	protected Style applyFormatting(Style style, ParserContext context) {
		return style.withParent(this.style(context));
	}

	protected abstract Style style(ParserContext context);
}
