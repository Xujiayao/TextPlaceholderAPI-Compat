package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.text.Text;

import java.util.List;

public interface TextNode {
	static TextNode convert(Text input) {
		return GeneralUtils.convertToNodes(input);
	}

	static TextNode of(String input) {
		return new LiteralNode(input);
	}

	static TextNode wrap(TextNode... nodes) {
		return new ParentNode(nodes);
	}

	static TextNode wrap(List<TextNode> nodes) {
		return new ParentNode(nodes.toArray(GeneralUtils.CASTER));
	}

	static TextNode asSingle(TextNode... nodes) {
		return switch (nodes.length) {
			case 0 -> EmptyNode.INSTANCE;
			case 1 -> nodes[0];
			default -> wrap(nodes);
		};
	}

	static TextNode asSingle(List<TextNode> nodes) {
		return switch (nodes.size()) {
			case 0 -> EmptyNode.INSTANCE;
			case 1 -> nodes.get(0);
			default -> wrap(nodes);
		};
	}

	static TextNode[] array(TextNode... nodes) {
		return nodes;
	}

	static TextNode empty() {
		return EmptyNode.INSTANCE;
	}

	Text toText(ParserContext context, boolean removeBackslashes);

	default Text toText(ParserContext context) {
		return toText(context, true);
	}

	default Text toText(PlaceholderContext context) {
		return toText(context.asParserContext(), true);
	}

	default Text toText() {
		return toText(ParserContext.of(), true);
	}

	default boolean isDynamic() {
		return false;
	}
}
