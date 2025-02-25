package com.xujiayao.placeholder_api_compat.api.parsers.format;

import com.xujiayao.placeholder_api_compat.api.parsers.TagLikeParser;
import org.jetbrains.annotations.Nullable;

public interface BaseFormat extends TagLikeParser.Format {
	char[] DEFAULT_ARGUMENT_WRAPPER = new char[]{'"', '\'', '`'};
	char[] LEGACY_ARGUMENT_WRAPPER = new char[]{'\''};

	int matchStart(String string, int index);

	int matchEnd(String string, int index);

	int matchArgument(String string, int index);

	@Override
	@Nullable
	default TagLikeParser.Format.@Nullable Tag findAt(String string, int start, TagLikeParser.Provider provider, TagLikeParser.Context context) {
		if (string.charAt(start) == '\\') {
			return null;
		} else {
			int mStart = this.matchStart(string, start);
			if (mStart == 0) {
				return null;
			} else {
				String id = null;
				String argument = "";
				char wrapper = 0;
				StringBuilder builder = new StringBuilder();
				int maxLengthEnd = string.length();
				int b = start + mStart;

				int end;
				while (true) {
					if (b >= maxLengthEnd) {
						return null;
					}

					char curr = string.charAt(b);
					boolean matched = true;
					int arg = 0;
					if (wrapper != 0) {
						if (curr == wrapper) {
							wrapper = 0;
						}

						builder.append(curr);
					} else if (curr == '\\') {
						if (b + 1 < string.length()) {
							++b;
							builder.append(string.charAt(b));
						}
					} else {
						label82:
						{
							if (id != null) {
								for (char argumentWrapper : this.argumentWrappers()) {
									if (curr == argumentWrapper) {
										builder.append(curr);
										wrapper = curr;
										break label82;
									}
								}
							}

							if (id == null && this.hasArgument()) {
								arg = this.matchArgument(string, b);
								if (arg <= 0) {
									matched = false;
									arg = 0;
								}
							}

							end = 0;
							if (arg == 0) {
								matched = true;
								end = this.matchEnd(string, b);
								if (end <= 0) {
									matched = false;
									end = 0;
								}
							}

							if (matched) {
								String str = builder.toString();
								if (id != null) {
									argument = str;
									break;
								}

								if (!provider.isValidTag(str, context)) {
									return null;
								}

								id = str;
								builder = new StringBuilder();
								if (end != 0) {
									break;
								}
							} else {
								builder.append(curr);
							}
						}
					}

					++b;
				}

				return new TagLikeParser.Format.Tag(start, b + end, id, argument);
			}
		}
	}

	char[] argumentWrappers();

	int endLength();

	boolean hasArgument();
}
