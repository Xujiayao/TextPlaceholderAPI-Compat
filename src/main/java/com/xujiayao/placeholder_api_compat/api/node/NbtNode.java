package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import net.minecraft.text.NbtDataSource;
import net.minecraft.text.Text;

import java.util.Optional;

public record NbtNode(String rawPath, boolean interpret, Optional<TextNode> separator, NbtDataSource dataSource) implements TextNode {
    @Override
    public Text toText(ParserContext context, boolean removeBackslashes) {
        return Text.nbt(rawPath, interpret, separator.map(x -> x.toText(context, removeBackslashes)), dataSource);
    }

    @Override
    public boolean isDynamic() {
        return separator.isPresent() && separator.get().isDynamic();
    }
}
