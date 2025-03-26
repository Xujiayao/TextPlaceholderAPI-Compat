package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;

public class ParentNode implements ParentTextNode {
    public static final ParentNode EMPTY = new ParentNode(new TextNode[0]);
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
    public final Text toText(ParserContext context, boolean removeBackslashes) {
        var compact = context != null && context.get(ParserContext.Key.COMPACT_TEXT) != Boolean.FALSE;
        var oldShadow = context.get(ParserContext.Key.DEFAULT_SHADOW_STYLER);

        if (this instanceof DynamicShadowNode.Transformer transformer && transformer.hasShadowColor(context)) {
            context.with(ParserContext.Key.DEFAULT_SHADOW_STYLER, transformer);
        }

        if (this.children.length == 0) {
            context.with(ParserContext.Key.DEFAULT_SHADOW_STYLER, oldShadow);
            return Text.empty();
        } else if ((this.children.length == 1 && this.children[0] != null) && compact) {
            var out = this.children[0].toText(context, true);
            if (GeneralUtils.isEmpty(out)) {
                return out;
            }
            context.with(ParserContext.Key.DEFAULT_SHADOW_STYLER, oldShadow);
            return this.applyFormatting(out.copy(), context);
        } else {
            MutableText base = compact ? null : Text.empty();

            for (int i = 0; i < this.children.length; i++) {
                if (this.children[i] != null) {
                    var child = this.children[i].toText(context, true);

                    if (!GeneralUtils.isEmpty(child)) {
                        if (base == null) {
                            if (child.getStyle().isEmpty()) {
                                base = child.copy();
                            } else {
                                base = Text.empty();
                                base.append(child);
                            }
                        } else {
                            base.append(child);
                        }
                    }
                }
            }
            context.with(ParserContext.Key.DEFAULT_SHADOW_STYLER, oldShadow);

            if (base == null || GeneralUtils.isEmpty(base)) {
                return Text.empty();
            }

            return this.applyFormatting(base, context);
        }
    }

    protected Text applyFormatting(MutableText out, ParserContext context) {
        return out.setStyle(applyFormatting(out.getStyle(), context));
    }

    protected Style applyFormatting(Style style, ParserContext context) {
        return style;
    }

    @Override
    public String toString() {
        return "ParentNode{" +
                "children=" + Arrays.toString(children) +
                '}';
    }
}
