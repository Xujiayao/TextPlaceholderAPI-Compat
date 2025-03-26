package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;


public final class FormattingNode extends SimpleStylingNode implements DynamicShadowNode.SimpleColoredTransformer {
    private final Formatting[] formatting;

    public FormattingNode(TextNode[] children, Formatting formatting) {
        this(children, new Formatting[]{ formatting });
    }

    public FormattingNode(TextNode[] children, Formatting... formatting) {
        super(children);
        this.formatting = formatting;
    }

    @Override
    protected Style style(ParserContext context) {
        return Style.EMPTY.withFormatting(this.formatting);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new FormattingNode(children, this.formatting);
    }

    @Override
    public String toString() {
        return "FormattingNode{" +
                "formatting=" + formatting +
                ", children=" + Arrays.toString(children) +
                '}';
    }

    @Override
    public int getDefaultShadowColor(Text out, float scale, float alpha, ParserContext context) {
        for (var form : formatting) {
            if (form.isColor()) {
                //noinspection DataFlowIssue
                return DynamicShadowNode.modifiedColor(form.getColorValue(), scale, alpha);
            }
        }
        return -1;
    }

    @Override
    public boolean hasShadowColor(ParserContext context) {
        for (var form : formatting) {
            if (form.isColor()) {
                return true;
            }
        }
        return false;
    }
}
