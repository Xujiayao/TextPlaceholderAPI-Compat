package com.xujiayao.placeholder_api_compat.api.parsers;

import com.mojang.brigadier.StringReader;
import com.xujiayao.placeholder_api_compat.api.node.LiteralNode;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.TranslatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ColorNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.FormattingNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentTextNode;
import com.xujiayao.placeholder_api_compat.impl.textparser.TextParserImpl;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Parser that can read legacy (and legacy like) format and convert it into TextNodes
 */
public class LegacyFormattingParser implements NodeParser {
	public static NodeParser COLORS = new LegacyFormattingParser(true, Arrays.stream(ChatFormatting.values()).filter((x) -> !x.isColor()).toArray(ChatFormatting[]::new));
	public static NodeParser BASE_COLORS = new LegacyFormattingParser(false, Arrays.stream(ChatFormatting.values()).filter((x) -> !x.isColor()).toArray(ChatFormatting[]::new));
	public static NodeParser ALL = new LegacyFormattingParser(true, ChatFormatting.values());
	private final Char2ObjectOpenHashMap<ChatFormatting> map = new Char2ObjectOpenHashMap<>();
	private final boolean allowRgb;

	public LegacyFormattingParser(boolean allowRgb, ChatFormatting... allowedFormatting) {
		this.allowRgb = allowRgb;
		for (var formatting : allowedFormatting) {
			this.map.put(formatting.getChar(), formatting);
		}
	}

	public boolean allowRGB() {
		return this.allowRgb;
	}

	public Collection<ChatFormatting> formatting() {
		return Collections.unmodifiableCollection(this.map.values());
	}

	@Override
	public TextNode[] parseNodes(TextNode input) {
		return this.parseNodes(input, new ArrayList<>());
	}

	public TextNode[] parseNodes(TextNode input, List<TextNode> nextNodes) {
		return switch (input) {
			case LiteralNode literalNode -> this.parseLiteral(literalNode, nextNodes);
			case TranslatedNode translatedNode -> new TextNode[]{translatedNode.transform(this)};
			case ParentTextNode parentTextNode -> this.parseParents(parentTextNode);
			case null, default -> new TextNode[]{input};
		};
	}

	@SuppressWarnings("deprecation")
	private TextNode[] parseParents(ParentTextNode parentTextNode) {
		ArrayList<TextNode> list = new ArrayList<>();
		if (parentTextNode.getChildren().length > 0) {
			ArrayList<TextNode> nodes = new ArrayList<>(List.of(parentTextNode.getChildren()));
			while (!nodes.isEmpty()) {
				list.add(TextNode.asSingle(this.parseNodes(nodes.removeFirst(), nodes)));
			}
		}

		return new TextNode[]{parentTextNode.copyWith(list.toArray(TextParserImpl.CASTER), this)};
	}

	@SuppressWarnings("deprecation")
	private TextNode[] parseLiteral(LiteralNode literalNode, List<TextNode> nexts) {
		StringBuilder builder = new StringBuilder();

		char i;
		for (StringReader reader = new StringReader(literalNode.value()); reader.canRead(2); builder.append(i)) {
			i = reader.read();
			if (i == '\\') {
				i = reader.read();
				builder.append('\\');
				builder.append(i);
			} else if (i == '&') {
				i = reader.read();
				if (this.allowRgb && i == '#' && reader.canRead(6)) {
					int start = reader.getCursor();

					try {
						StringBuilder builder1 = new StringBuilder();

						int rgb;
						for (rgb = 0; rgb < 6; ++rgb) {
							builder1.append(reader.read());
						}

						rgb = Integer.parseInt(builder1.toString(), 16);
						ArrayList<TextNode> list = new ArrayList<>(nexts);
						nexts.clear();
						TextNode base = TextNode.asSingle(this.parseLiteral(new LiteralNode(reader.getRemaining()), list));
						list.addFirst(base);
						return new TextNode[]{new LiteralNode(builder.toString()), new ColorNode(list.toArray(TextParserImpl.CASTER), TextColor.fromRgb(rgb))};
					} catch (Throwable var11) {
						reader.setCursor(start);
					}
				}

				ChatFormatting x = this.map.get(i);
				if (x != null) {
					ArrayList<TextNode> list = new ArrayList<>(nexts);
					nexts.clear();
					TextNode base = TextNode.asSingle(this.parseLiteral(new LiteralNode(reader.getRemaining()), list));
					list.addFirst(base);
					return new TextNode[]{new LiteralNode(builder.toString()), new FormattingNode(list.toArray(TextParserImpl.CASTER), x)};
				}

				builder.append('&');
			}
		}

		return new TextNode[]{literalNode};
	}
}
