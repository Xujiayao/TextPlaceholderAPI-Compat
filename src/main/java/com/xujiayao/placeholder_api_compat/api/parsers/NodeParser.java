package com.xujiayao.placeholder_api_compat.api.parsers;

import com.mojang.serialization.Codec;
import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.impl.textparser.MergedParser;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface NodeParser {
	NodeParser NOOP = (i) -> new TextNode[]{i};

	static NodeParser merge(NodeParser... parsers) {
		return switch (parsers.length) {
			case 0 -> NOOP;
			case 1 -> parsers[0];
			default -> new MergedParser(parsers);
		};
	}

	static NodeParser merge(List<NodeParser> parsers) {
		return switch (parsers.size()) {
			case 0 -> NOOP;
			case 1 -> parsers.getFirst();
			default -> new MergedParser(parsers.toArray(new NodeParser[0]));
		};
	}

	static ParserBuilder builder() {
		return new ParserBuilder();
	}

	TextNode[] parseNodes(TextNode input);

	default TextNode parseNode(TextNode input) {
		return TextNode.asSingle(this.parseNodes(input));
	}

	default TextNode parseNode(String input) {
		return this.parseNode(TextNode.of(input));
	}

	default Component parseText(TextNode input, ParserContext context) {
		return TextNode.asSingle(this.parseNodes(input)).toText(context, true);
	}

	default Component parseText(String input, ParserContext context) {
		return this.parseText(TextNode.of(input), context);
	}

	default Codec codec() {
		return Codec.STRING.xmap((x) -> WrappedText.from(this, x), WrappedText::input);
	}
}
