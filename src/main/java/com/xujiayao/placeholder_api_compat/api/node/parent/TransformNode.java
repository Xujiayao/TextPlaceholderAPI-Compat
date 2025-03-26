package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.function.Function;

public final class TransformNode extends ParentNode {
    private final Function<MutableText, Text> transform;

    public TransformNode(TextNode[] nodes, Function<MutableText, Text> transform) {
        super(nodes);
        this.transform = transform;
    }

    public static TransformNode deepStyle(Function<Style, Style> styleFunction, TextNode... nodes) {
        return new TransformNode(nodes, new GeneralUtils.MutableTransformer(styleFunction));
    }

    @Override
    protected Text applyFormatting(MutableText out, ParserContext context) {
        return this.transform.apply(out);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new TransformNode(children, this.transform);
    }

    @Override
    public String toString() {
        return "TransformNode{" +
                "transform=" + transform +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}
