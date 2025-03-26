package com.xujiayao.placeholder_api_compat.impl.textparser.providers;

import com.xujiayao.placeholder_api_compat.api.arguments.StringArgs;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ColorNode;
import com.xujiayao.placeholder_api_compat.api.parsers.TagLikeParser;
import com.xujiayao.placeholder_api_compat.api.parsers.tag.TagRegistry;
import net.minecraft.text.TextColor;

public record LegacyProvider(TagRegistry registry) implements TagLikeParser.Provider {
    @Override
    public boolean isValidTag(String tag, TagLikeParser.Context context) {
        var peek = context.peekId();
        return tag.equals("r") || tag.equals("reset")
                || tag.startsWith("#")
                || registry.getTag(tag) != null
                || tag.equals("/") || (peek != null && tag.equals("/" + peek));
    }

    @Override
    public void handleTag(String id, String argument, TagLikeParser.Context context) {
        if (id.equals("/") || id.equals("/" + context.peekId())) {
            context.pop();
            return;
        }

        if (id.equals("r") || id.equals("reset")) {
            context.pop(context.size());
            return;
        }

        if (id.startsWith("#")) {
            var text = TextColor.parse(id);
            if (text.result().isPresent()) {
                context.push("c", x -> new ColorNode(x, text.result().get()));
            }
            return;
        }


        var tag = registry.getTag(id);

        assert tag != null;
        if (tag.selfContained()) {
            context.addNode(tag.nodeCreator().createTextNode(TextNode.array(), StringArgs.ordered(argument, ':'), context.parser()));
        } else {
            context.push(id, (a) -> tag.nodeCreator().createTextNode(a, StringArgs.ordered(argument, ':'), context.parser()));
        }
    }
}
