package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.text.Text;

public record EmptyNode() implements TextNode {
    public static final EmptyNode INSTANCE = new EmptyNode();
    @Override
    public Text toText(ParserContext context, boolean removeBackslashes) {
        return Text.empty();
    }
}
