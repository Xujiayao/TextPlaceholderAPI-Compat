package com.xujiayao.placeholder_api_compat.api.node.parent;

import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.NodeParser;
import com.xujiayao.placeholder_api_compat.impl.textparser.TextParserImpl;

import java.util.Collection;

public interface ParentTextNode extends TextNode {
	TextNode[] getChildren();

	ParentTextNode copyWith(TextNode[] children);

	default ParentTextNode copyWith(Collection<TextNode> children) {
		return this.copyWith(children.toArray(TextParserImpl.CASTER));
	}

	default boolean isDynamicNoChildren() {
		return false;
	}

	default boolean isDynamic() {
		for (var x : getChildren()) {
			if (x.isDynamic()) {
				return true;
			}
		}
		return this.isDynamicNoChildren();
	}

	default ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
		return copyWith(children);
	}

	default ParentTextNode copyWith(Collection<TextNode> children, NodeParser parser) {
		return this.copyWith(children.toArray(TextParserImpl.CASTER), parser);
	}

	@Deprecated(forRemoval = true)
	@FunctionalInterface
	interface Constructor {
		ParentTextNode createNode(String definition, Collection<ParentTextNode> children);
	}
}
