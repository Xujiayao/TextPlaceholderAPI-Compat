package eu.pb4.placeholders.api.parsers;

import com.mojang.brigadier.StringReader;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.TranslatedNode;
import eu.pb4.placeholders.api.node.parent.ColorNode;
import eu.pb4.placeholders.api.node.parent.FormattingNode;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import eu.pb4.placeholders.impl.textparser.TextParserImpl;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Parser that can read legacy (and legacy like) format and convert it into TextNodes
 */
public class LegacyFormattingParser implements NodeParser {
	public static NodeParser COLORS = new LegacyFormattingParser(true, Arrays.stream(Formatting.values()).filter(x -> !x.isColor()).toArray(Formatting[]::new));
	public static NodeParser BASE_COLORS = new LegacyFormattingParser(false, Arrays.stream(Formatting.values()).filter(x -> !x.isColor()).toArray(Formatting[]::new));
	public static NodeParser ALL = new LegacyFormattingParser(true, Formatting.values());
	private final Char2ObjectOpenHashMap<Formatting> map = new Char2ObjectOpenHashMap<>();
	private final boolean allowRgb;

	public LegacyFormattingParser(boolean allowRgb, Formatting... allowedFormatting) {
		this.allowRgb = allowRgb;
		for (var formatting : allowedFormatting) {
			this.map.put(formatting.getCode(), formatting);
		}
	}

	public boolean allowRGB() {
		return allowRgb;
	}

	public Collection<Formatting> formatting() {
		return Collections.unmodifiableCollection(this.map.values());
	}

	@Override
	public TextNode[] parseNodes(TextNode input) {
		return parseNodes(input, new ArrayList<>());
	}

	public TextNode[] parseNodes(TextNode input, List<TextNode> nextNodes) {
		return switch (input) {
			case LiteralNode literalNode -> parseLiteral(literalNode, nextNodes);
			case TranslatedNode translatedNode -> new TextNode[]{translatedNode.transform(this)};
			case ParentTextNode parentTextNode -> parseParents(parentTextNode);
			case null, default -> new TextNode[]{input};
		};
	}

	@SuppressWarnings("deprecation")
	private TextNode[] parseParents(ParentTextNode parentTextNode) {
		var list = new ArrayList<TextNode>();

		if (parentTextNode.getChildren().length > 0) {
			var nodes = new ArrayList<>(List.of(parentTextNode.getChildren()));
			while (!nodes.isEmpty()) {
				list.add(TextNode.asSingle(parseNodes(nodes.removeFirst(), nodes)));
			}
		}

		return new TextNode[]{parentTextNode.copyWith(list.toArray(TextParserImpl.CASTER), this)};
	}

	@SuppressWarnings("deprecation")
	private TextNode[] parseLiteral(LiteralNode literalNode, List<TextNode> nexts) {
		var builder = new StringBuilder();
		var reader = new StringReader(literalNode.value());

		while (reader.canRead(2)) {
			var i = reader.read();

			if (i == '\\') {
				i = reader.read();

				builder.append('\\');
				builder.append(i);

			} else if (i == '&') {
				i = reader.read();

				if (allowRgb && i == '#' && reader.canRead(6)) {
					var start = reader.getCursor();
					try {
						StringBuilder builder1 = new StringBuilder();

						for (int z = 0; z < 6; z++) {
							builder1.append(reader.read());
						}

						var rgb = Integer.parseInt(builder1.toString(), 16);

						var list = new ArrayList<>(nexts);
						nexts.clear();

						var base = TextNode.asSingle(parseLiteral(new LiteralNode(reader.getRemaining()), list));
						list.addFirst(base);

						return new TextNode[]{new LiteralNode(builder.toString()), new ColorNode(list.toArray(TextParserImpl.CASTER), TextColor.fromRgb(rgb))};
					} catch (Throwable e) {
						//noop
					}

					reader.setCursor(start);
				}

				var x = this.map.get(i);

				if (x != null) {
					var list = new ArrayList<>(nexts);
					nexts.clear();

					var base = TextNode.asSingle(parseLiteral(new LiteralNode(reader.getRemaining()), list));

					list.addFirst(base);

					return new TextNode[]{new LiteralNode(builder.toString()), new FormattingNode(list.toArray(TextParserImpl.CASTER), x)};
				} else {
					builder.append('&');
				}
			}
			builder.append(i);
		}

		return new TextNode[]{literalNode};
	}
}
