package com.xujiayao.placeholder_api_compat.api.parsers.tag;

import com.xujiayao.placeholder_api_compat.api.arguments.StringArgs;
import com.xujiayao.placeholder_api_compat.api.arguments.SimpleArguments;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.NodeParser;

import java.util.function.Function;

public interface NodeCreator {
    TextNode createTextNode(TextNode[] nodes, StringArgs arg, NodeParser parser);

    static NodeCreator self(Function<StringArgs, TextNode> function) {
        return (a, b, c) -> function.apply(b);
    }

    static NodeCreator bool(BoolNodeArg function) {
        return (a, b, c) -> function.apply(a, SimpleArguments.bool(b.get("value", 0), true));
    }

    interface BoolNodeArg {
        TextNode apply(TextNode[] nodes, boolean argument);
    }
}
