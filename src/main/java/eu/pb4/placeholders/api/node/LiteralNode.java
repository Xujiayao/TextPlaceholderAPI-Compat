package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
//#if MC <= 11802
//$$ import net.minecraft.text.LiteralText;
//#endif
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public record LiteralNode(String value) implements TextNode {

	public LiteralNode(StringBuilder builder) {
		this(builder.toString());
	}

	private static MutableText createText(String s) {
		//#if MC > 11802
		return Text.literal(s);
		//#else
		//$$ return new LiteralText(s);
		//#endif
	}

	@Override
	public Text toText(ParserContext context, boolean removeBackslashes) {
		if (this.value.isEmpty()) {
			return Text.of("");
		}

		if (removeBackslashes) {
			var builder = new StringBuilder();

			var length = this.value.length();
			for (var i = 0; i < length; i++) {
				var c = this.value.charAt(i);

				if (c == '\\' && i + 1 < length) {
					var n = this.value.charAt(i + 1);
					if (Character.isWhitespace(n) || Character.isLetterOrDigit(n)) {
						builder.append(c);
					} else {
						builder.append(n);
						i++;
					}
				} else {
					builder.append(c);
				}
			}

			return createText(builder.toString());
		} else {
			return createText(this.value());
		}
	}
}
