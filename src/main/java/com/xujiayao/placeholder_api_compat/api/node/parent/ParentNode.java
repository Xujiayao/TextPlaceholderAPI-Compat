package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.Collection;

public class ParentNode implements ParentTextNode {
	public static final ParentNode EMPTY = new ParentNode();
	protected final TextNode[] children;

	public ParentNode(TextNode... children) {
		this.children = children;
	}

	public ParentNode(Collection<TextNode> children) {
		this(children.toArray(GeneralUtils.CASTER));
	}

	@Override
	public final TextNode[] getChildren() {
		return this.children;
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new ParentNode(children);
	}

	@Override
	public final Component toText(ParserContext context, boolean removeBackslashes) {
		boolean compact = context != null && context.get(ParserContext.Key.COMPACT_TEXT) != Boolean.FALSE;
		if (this.children.length == 0) {
			return Component.empty();
		} else if (this.children.length == 1 && this.children[0] != null && compact) {
			Component out = this.children[0].toText(context, true);
			return GeneralUtils.isEmpty(out) ? out : this.applyFormatting(out.copy(), context);
		} else {
			MutableComponent base = compact ? null : Component.empty();

			for (TextNode textNode : this.children) {
				if (textNode != null) {
					Component child = textNode.toText(context, true);
					if (!GeneralUtils.isEmpty(child)) {
						if (base == null) {
							if (child.getStyle().isEmpty()) {
								base = child.copy();
							} else {
								base = Component.empty();
								base.append(child);
							}
						} else {
							base.append(child);
						}
					}
				}
			}

			if (base != null && !GeneralUtils.isEmpty(base)) {
				return this.applyFormatting(base, context);
			} else {
				return Component.empty();
			}
		}
	}

	protected Component applyFormatting(MutableComponent out, ParserContext context) {
		return out.setStyle(this.applyFormatting(out.getStyle(), context));
	}

	protected Style applyFormatting(Style style, ParserContext context) {
		return style;
	}

	@Override
	public String toString() {
		return "ParentNode{children=" + Arrays.toString(this.children) + "}";
	}
}
