package com.xujiayao.placeholder_api_compat.api.parsers;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import net.minecraft.network.chat.Component;

public record WrappedText(String input, TextNode textNode, Component text) {
	public static WrappedText from(NodeParser parser, String input) {
		TextNode node = TextNode.asSingle(parser.parseNodes(TextNode.of(input)));

		return new WrappedText(input, node, node.toText(ParserContext.of(), true));
	}
}
