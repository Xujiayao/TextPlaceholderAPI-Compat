package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.function.Function;

public final class TransformNode extends ParentNode {
	private final Function<MutableComponent, Component> transform;

	public TransformNode(TextNode[] nodes, Function<MutableComponent, Component> transform) {
		super(nodes);
		this.transform = transform;
	}

	public static TransformNode deepStyle(Function<Style, Style> styleFunction, TextNode... nodes) {
		return new TransformNode(nodes, new GeneralUtils.MutableTransformer(styleFunction));
	}

	@Override
	protected Component applyFormatting(MutableComponent out, ParserContext context) {
		return this.transform.apply(out);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new TransformNode(children, this.transform);
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.transform);
		return "TransformNode{transform=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}
}
