package com.xujiayao.placeholder_api_compat.api;

import com.xujiayao.placeholder_api_compat.api.node.LiteralNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentTextNode;
import com.xujiayao.placeholder_api_compat.api.parsers.TextParserV1;
import net.minecraft.network.chat.Component;

/**
 * You should use {@link com.xujiayao.placeholder_api_compat.api.parsers.ParserBuilder} for stacked parsing
 * or {@link com.xujiayao.placeholder_api_compat.api.parsers.TagParser} for only tags to text.
 */
@Deprecated
public final class TextParserUtils {
	private TextParserUtils() {
	}

	public static Component formatText(String text) {
		return formatNodes(text).toText(null, true);
	}

	public static Component formatTextSafe(String text) {
		return formatNodesSafe(text).toText(null, true);
	}

	public static Component formatText(String text, TextParserV1.TagParserGetter getter) {
		return formatNodes(text, getter).toText(null, true);
	}

	public static ParentTextNode formatNodes(String text) {
		return new ParentNode(TextParserV1.DEFAULT.parseNodes(new LiteralNode(text)));
	}

	public static ParentTextNode formatNodesSafe(String text) {
		return new ParentNode(TextParserV1.DEFAULT.parseNodes(new LiteralNode(text)));
	}

	public static ParentTextNode formatNodes(String text, TextParserV1.TagParserGetter getter) {
		return new ParentNode(TextParserV1.parseNodesWith(new LiteralNode(text), getter));
	}
}
