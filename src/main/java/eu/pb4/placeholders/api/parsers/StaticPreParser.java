package eu.pb4.placeholders.api.parsers;

import eu.pb4.placeholders.api.node.DirectTextNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.parent.ParentNode;

import java.util.ArrayList;

/**
 * Pre-parses TextNode into DirectTextNode with static vanilla Text for Nodes that aren't dynamic.
 * If you want to use this, it should be a last step of parsing into a "template" ((dynamic) placeholders should also be parsed before this).
 */
public record StaticPreParser() implements NodeParser {
	public static final NodeParser INSTANCE = new StaticPreParser();

	public static TextNode parse(TextNode node) {
		if (!node.isDynamic()) {
			return new DirectTextNode(node.toText());
		}

		if (node instanceof ParentNode parentNode) {
			ArrayList<TextNode> c = new ArrayList<>();

			for (TextNode child : parentNode.getChildren()) {
				c.add(parse(child));
			}

			return parentNode.copyWith(c.toArray(new TextNode[0]));
		}

		return node;
	}

	@Override
	public TextNode[] parseNodes(TextNode input) {
		return new TextNode[]{parse(input)};
	}
}
