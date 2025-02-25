package com.xujiayao.placeholder_api_compat.api.node;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface TextNode {
	static TextNode convert(Component input) {
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
			case 1 -> nodes.getFirst();
			default -> wrap(nodes);
		};
	}

	static TextNode[] array(TextNode... nodes) {
		return nodes;
	}

	static TextNode empty() {
		return EmptyNode.INSTANCE;
	}

	Component toText(ParserContext context, boolean removeBackslashes);

	default Component toText(ParserContext context) {
		return this.toText(context, true);
	}

	default Component toText(PlaceholderContext context) {
		return this.toText(context.asParserContext(), true);
	}

	default Component toText() {
		return this.toText(ParserContext.of(), true);
	}

	default boolean isDynamic() {
		return false;
	}
}
