package eu.pb4.placeholders.impl.textparser;

import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Comparator;

public class MultiTagLikeParser extends TagLikeParser {
	private final Pair<Format, Provider>[] pairs;

	public MultiTagLikeParser(Pair<Format, Provider>[] formatsAndProviders) {
		Pair<Format, Provider>[] copy = Arrays.copyOf(formatsAndProviders, formatsAndProviders.length);
		Arrays.sort(copy, Comparator.comparingInt((p) -> p.getLeft().index()));
		this.pairs = copy;
	}

	@Override
	protected void handleLiteral(String value, TagLikeParser.Context context) {
		int pos = 0;

		while (pos != -1) {
			int tPos = pos;
			TagLikeParser.Provider provider = null;
			TagLikeParser.Format.Tag tag;

			for (tag = null; tPos < value.length(); ++tPos) {
				for (Pair<Format, Provider> p : this.pairs) {
					Format.Tag tag1 = p.getLeft().findAt(value, tPos, p.getRight(), context);
					if (tag1 != null && (tag == null || tag1.start() < tag.start())) {
						provider = p.getRight();
						tag = tag1;
					}
				}

				if (tag != null) {
					break;
				}
			}
			if (provider != null) {
				pos = this.handleTag(value, pos, tag, provider, context);
			} else {
				context.addNode(new LiteralNode(value.substring(pos)));
				pos = -1;
			}
		}

	}

	public Pair<Format, Provider>[] pairs() {
		return this.pairs;
	}
}
