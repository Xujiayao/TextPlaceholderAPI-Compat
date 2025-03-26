package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Arrays;

public final class ColorNode extends SimpleStylingNode implements DynamicShadowNode.SimpleColoredTransformer {
    private final TextColor color;

    public ColorNode(TextNode[] children, TextColor color) {
        super(children);
        this.color = color;
    }

    @Override
    protected Style style(ParserContext context) {
        return Style.EMPTY.withColor(this.color);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new ColorNode(children, this.color);
    }

    @Override
    public String toString() {
        return "ColorNode{" +
                "color=" + color +
                ", children=" + Arrays.toString(children) +
                '}';
    }

    @Override
    public int getDefaultShadowColor(Text out, float scale, float alpha, ParserContext context) {
        return DynamicShadowNode.modifiedColor(this.color.getRgb(), scale, alpha);
    }
}
