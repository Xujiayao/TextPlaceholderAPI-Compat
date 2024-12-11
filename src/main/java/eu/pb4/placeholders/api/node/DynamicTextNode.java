package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
//#if MC <= 11802
//$$ import net.minecraft.text.LiteralText;
//#endif
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record DynamicTextNode(String id, ParserContext.Key<Function<String, Text>> key) implements TextNode {
	public static DynamicTextNode of(String id, ParserContext.Key<Function<String, Text>> key) {
		return new DynamicTextNode(id, key);
	}

	public static ParserContext.Key<Function<String, @Nullable Text>> key(String id) {
		return new ParserContext.Key<>("dynamic:" + id, null);
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
		var x = context.get(key);
		if (x != null) {
			var t = x.apply(id);
			if (t != null) {
				return t;
			}
			//#if MC > 12002
			return createText("[INVALID KEY " + this.key.key() + " | " + this.id + "]").formatted(Formatting.ITALIC).withColor(0xFF0000);
			//#else
			//$$ return createText("[INVALID KEY " + this.key.key() + " | " + this.id + "]").formatted(Formatting.ITALIC, Formatting.RED);
			//#endif
		}
		//#if MC > 12002
		return createText("[MISSING CONTEXT FOR " + this.key.key() + " | " + this.id + "]").formatted(Formatting.ITALIC).withColor(0xFF0000);
		//#else
		//$$ return createText("[MISSING CONTEXT FOR " + this.key.key() + " | " + this.id + "]").formatted(Formatting.ITALIC, Formatting.RED);
		//#endif
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
