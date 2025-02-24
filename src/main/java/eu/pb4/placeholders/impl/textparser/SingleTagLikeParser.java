package eu.pb4.placeholders.impl.textparser;

import eu.pb4.placeholders.api.parsers.TagLikeParser;

public class SingleTagLikeParser extends TagLikeParser {
	private final TagLikeParser.Format format;
	private final TagLikeParser.Provider provider;

	public SingleTagLikeParser(TagLikeParser.Format format, TagLikeParser.Provider provider) {
		this.format = format;
		this.provider = provider;
	}

	@Override
	protected void handleLiteral(String value, TagLikeParser.Context context) {
		int pos = 0;

		while (pos != -1) {
			pos = this.handleTag(value, pos, this.format.findFirst(value, pos, provider, context), provider, context);
		}
	}

	public TagLikeParser.Format format() {
		return this.format;
	}

	public TagLikeParser.Provider provider() {
		return this.provider;
	}
}
