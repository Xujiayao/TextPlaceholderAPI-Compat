package eu.pb4.placeholders.impl.textparser;

import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import eu.pb4.placeholders.api.parsers.TagLikeWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public record MergedParser(NodeParser[] parsers) implements NodeParser {
	@SuppressWarnings("unchecked")
	public MergedParser(NodeParser[] parsers) {
		var list = new ArrayList<NodeParser>(parsers.length);
		var combiner = new ArrayList<Pair<TagLikeParser.Format, TagLikeParser.Provider>>(4);
		for (var parser : parsers) {
			if (parser instanceof TagLikeWrapper wrapper) {
				parser = wrapper.asTagLikeParser();
			}

			if (parser instanceof SingleTagLikeParser tagLikeParser) {
				combiner.add(Pair.of(tagLikeParser.format(), tagLikeParser.provider()));
			} else if (parser instanceof MultiTagLikeParser tagLikeParser) {
				combiner.addAll(List.of(tagLikeParser.pairs()));
			} else {
				if (combiner.size() == 1) {
					list.add(new SingleTagLikeParser(combiner.getFirst().getLeft(), combiner.getFirst().getRight()));
					combiner.clear();
				} else if (combiner.size() > 1) {
					list.add(new MultiTagLikeParser(combiner.toArray(new Pair[]{})));
					combiner.clear();
				}
				list.add(parser);
			}
		}

		if (combiner.size() == 1) {
			list.add(new SingleTagLikeParser(combiner.getFirst().getLeft(), combiner.getFirst().getRight()));
		} else if (combiner.size() > 1) {
			list.add(new MultiTagLikeParser(combiner.toArray(new Pair[]{})));
		}
		this.parsers = list.toArray(new NodeParser[0]);
	}

	@Override
	public TextNode[] parseNodes(TextNode input) {
		var out = new TextNode[]{input};
		for (NodeParser parser : this.parsers) {
			out = parser.parseNodes(TextNode.asSingle(out));
		}

		return out;
	}
}
