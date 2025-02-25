package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.mojang.serialization.DataResult;
import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.NodeParser;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class DynamicColorNode extends SimpleStylingNode {
	private final TextNode color;

	public DynamicColorNode(TextNode[] children, TextNode color) {
		super(children);
		this.color = color;
	}

	@Override
	public boolean isDynamicNoChildren() {
		return this.color.isDynamic();
	}

	@Override
	protected Style style(ParserContext context) {
		DataResult<TextColor> c = TextColor.parseColor(this.color.toText(context).getString());
		Optional<TextColor> var10000 = c.result();
		Style var10001 = Style.EMPTY;
		Objects.requireNonNull(var10001);
		return var10000.map(var10001::withColor).orElse(Style.EMPTY);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new DynamicColorNode(children, this.color);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
		return new DynamicColorNode(children, parser.parseNode(this.color));
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.color);
		return "ColorNode{color=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}
}
