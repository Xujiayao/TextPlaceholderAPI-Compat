package com.xujiayao.placeholder_api_compat.api.parsers.tag;

import com.xujiayao.placeholder_api_compat.api.node.parent.ColorNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.FormattingNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

import java.util.Collection;

public final class SimpleTags {
	public static TextTag color(String name, Collection<String> aliases, ChatFormatting formatting) {
		return TextTag.enclosing(name, aliases, "color", true, (nodes, arg, parser) -> new FormattingNode(nodes, formatting));
	}

	public static TextTag color(String name, Collection<String> aliases, int rgb) {
		return TextTag.enclosing(name, aliases, "color", true, (nodes, arg, parser) -> new ColorNode(nodes, TextColor.fromRgb(rgb)));
	}
}
