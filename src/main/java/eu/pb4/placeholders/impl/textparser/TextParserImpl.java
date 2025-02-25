package eu.pb4.placeholders.impl.textparser;

import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.placeholders.impl.GeneralUtils;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.pb4.placeholders.impl.GeneralUtils.Pair;

@Deprecated
@Internal
public class TextParserImpl {
	// Based on minimessage's regex, modified to fit more parsers needs
	public static final Pattern STARTING_PATTERN = Pattern.compile("<(?<id>[^<>/]+)(?<data>(:('?([^'](\\\\\\\\')?)+'?))*)>");
	@Deprecated
	public static final List<Pair<String, String>> ESCAPED_CHARS = new ArrayList<>();
	public static final TextNode[] CASTER;

	static {
		ESCAPED_CHARS.add(new Pair<>("\\", "&slsh;\002"));
		ESCAPED_CHARS.add(new Pair<>("<", "&lt;\002"));
		ESCAPED_CHARS.add(new Pair<>(">", "&gt;\002"));
		ESCAPED_CHARS.add(new Pair<>("\"", "&quot;\002"));
		ESCAPED_CHARS.add(new Pair<>("'", "&pos;\002"));
		ESCAPED_CHARS.add(new Pair<>(":", "&colon;\002"));
		ESCAPED_CHARS.add(new Pair<>("&", "&amps;\002"));
		ESCAPED_CHARS.add(new Pair<>("{", "&openbrac;\002"));
		ESCAPED_CHARS.add(new Pair<>("}", "&closebrac;\002"));
		ESCAPED_CHARS.add(new Pair<>("$", "&dolar;\002"));
		ESCAPED_CHARS.add(new Pair<>("%", "&perc;\002"));

		CASTER = new TextNode[0];
	}

	public static TextNode[] parse(String string, TextParserV1.TagParserGetter handlers) {
		return recursiveParsing(escapeCharacters(string), handlers, null).nodes();
	}

	public static String escapeCharacters(String string) {
		for (Pair<String, String> entry : ESCAPED_CHARS) {
			string = string.replace("\\" + entry.left(), entry.right());
		}
		return string;
	}

	public static String removeEscaping(String string) {
		for (Pair<String, String> entry : ESCAPED_CHARS) {
			try {
				string = string.replace(entry.right(), entry.left());
			} catch (Exception e) {
				// Silence!
			}
		}
		return string;
	}

	public static String restoreOriginalEscaping(String string) {
		for (Pair<String, String> entry : ESCAPED_CHARS) {
			try {
				string = string.replace(entry.right(), "\\" + entry.left());
			} catch (Exception e) {
				// Silence!
			}
		}
		return string;
	}

	public static String cleanArgument(String string) {
		return string.length() >= 2 && string.startsWith("'") && string.endsWith("'") ? string.substring(1, string.length() - 1) : string;
	}

	public static TextParserV1.NodeList recursiveParsing(String input, TextParserV1.TagParserGetter handlers, String endAt) {
		if (input.isEmpty()) {
			return new TextParserV1.NodeList(new TextNode[0], 0);
		} else {
			ArrayList<TextNode> text = new ArrayList<>();

			Matcher matcher = STARTING_PATTERN.matcher(input);
			Matcher matcherEnd = endAt != null ? Pattern.compile("(" + endAt + ")|(</>)").matcher(input) : null;
			int currentPos = 0;
			boolean hasEndTag = endAt != null && matcherEnd.find();
			int currentEnd = hasEndTag ? matcherEnd.start() : input.length();

			while (matcher.find() && currentEnd > matcher.start()) {
				String[] entireTag = (matcher.group("id") + matcher.group("data")).split(":", 2);
				String tag = entireTag[0].toLowerCase(Locale.ROOT);
				String data = "";
				if (entireTag.length == 2) {
					data = entireTag[1];
				}

				String end;
				if (!tag.equals("reset") && !tag.equals("r")) {
					if (tag.startsWith("#")) {
						data = tag;
						tag = "color";
					}

					end = "</" + tag + ">";
					TextParserV1.TagNodeBuilder handler = handlers.getTagParser(tag);
					if (handler != null) {
						String betweenText = input.substring(currentPos, matcher.start());
						if (!betweenText.isEmpty()) {
							text.add(new LiteralNode(restoreOriginalEscaping(betweenText)));
						}

						currentPos = matcher.end();

						try {
							TextParserV1.TagNodeValue pair = handler.parseString(tag, data, input.substring(currentPos), handlers, end);
							if (pair.node() != null) {
								text.add(pair.node());
							}

							currentPos += pair.length();
							if (currentPos >= input.length()) {
								currentEnd = input.length();
								break;
							}

							matcher.region(currentPos, input.length());
							if (matcherEnd != null) {
								matcherEnd.region(currentPos, input.length());
								if (matcherEnd.find()) {
									hasEndTag = true;
									currentEnd = matcherEnd.start();
								} else {
									hasEndTag = false;
									currentEnd = input.length();
								}
							}
						} catch (Exception e) {
							GeneralUtils.LOGGER.error("Error parsing tag " + tag + "!", e);
						}
					}
				} else {
					if (endAt != null) {
						currentEnd = matcher.start();
						if (currentPos < currentEnd) {
							end = restoreOriginalEscaping(input.substring(currentPos, currentEnd));
							if (!end.isEmpty()) {
								text.add(new LiteralNode(end));
							}
						}

						return new TextParserV1.NodeList(text.toArray(new TextNode[0]), currentEnd);
					}

					end = input.substring(currentPos, matcher.start());
					if (!end.isEmpty()) {
						text.add(new LiteralNode(restoreOriginalEscaping(end)));
					}

					currentPos = matcher.end();
				}
			}

			if (currentPos < currentEnd) {
				String restOfText = restoreOriginalEscaping(input.substring(currentPos, currentEnd));
				if (!restOfText.isEmpty()) {
					text.add(new LiteralNode(restOfText));
				}
			}

			if (hasEndTag) {
				currentEnd += matcherEnd.group().length();
			} else {
				currentEnd = input.length();
			}

			return new TextParserV1.NodeList(text.toArray(new TextNode[0]), currentEnd);
		}
	}
}