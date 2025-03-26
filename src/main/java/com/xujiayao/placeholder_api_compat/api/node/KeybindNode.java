package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.text.Text;

public record KeybindNode(String value) implements TextNode {
    @Override
    public Text toText(ParserContext context, boolean removeBackslashes) {
        return Text.keybind(this.value());
    }
}
