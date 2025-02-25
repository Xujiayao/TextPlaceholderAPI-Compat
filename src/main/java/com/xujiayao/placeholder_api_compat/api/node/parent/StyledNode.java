package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.NodeParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public final class StyledNode extends SimpleStylingNode {
	private final Style style;
	private final ParentNode hoverValue;
	private final TextNode clickValue;
	private final TextNode insertion;

	public StyledNode(TextNode[] children, Style style, @Nullable ParentNode hoverValue, @Nullable TextNode clickValue, @Nullable TextNode insertion) {
		super(children);
		this.style = style;
		this.hoverValue = hoverValue;
		this.clickValue = clickValue;
		this.insertion = insertion;
	}

	public Style style(ParserContext context) {
		Style style = this.style;
		if (this.hoverValue != null && style.getHoverEvent() != null && style.getHoverEvent().getAction() == Action.SHOW_TEXT) {
			style = style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, this.hoverValue.toText(context, true)));
		}

		if (this.clickValue != null && style.getClickEvent() != null) {
			style = style.withClickEvent(new ClickEvent(style.getClickEvent().getAction(), this.clickValue.toText(context, true).getString()));
		}

		if (this.insertion != null) {
			style = style.withInsertion(this.insertion.toText(context, true).getString());
		}

		return style;
	}

	public Style rawStyle() {
		return this.style;
	}

	public @Nullable ParentNode hoverValue() {
		return this.hoverValue;
	}

	public @Nullable TextNode clickValue() {
		return this.clickValue;
	}

	public @Nullable TextNode insertion() {
		return this.insertion;
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new StyledNode(children, this.style, this.hoverValue, this.clickValue, this.insertion);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
		return new StyledNode(children, this.style, this.hoverValue != null ? new ParentNode(parser.parseNodes(this.hoverValue)) : null, this.clickValue != null ? TextNode.asSingle(parser.parseNodes(this.clickValue)) : null, this.insertion != null ? TextNode.asSingle(parser.parseNodes(this.insertion)) : null);
	}

	@Override
	public boolean isDynamicNoChildren() {
		return this.clickValue != null && this.clickValue.isDynamic() || this.hoverValue != null && this.hoverValue.isDynamic() || this.insertion != null && this.insertion.isDynamic();
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.style);
		return "StyledNode{style=" + var10000 + ", hoverValue=" + this.hoverValue + ", clickValue=" + this.clickValue + ", insertion=" + this.insertion + "}";
	}
}
