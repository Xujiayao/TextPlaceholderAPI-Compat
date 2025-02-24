package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record DynamicTextNode(String id, ParserContext.Key<Function<String, Component>> key) implements TextNode {
	public static DynamicTextNode of(String id, ParserContext.Key<Function<String, Component>> key) {
		return new DynamicTextNode(id, key);
	}

	public static ParserContext.Key<Function<String, @Nullable Component>> key(String id) {
		return new ParserContext.Key<>("dynamic:" + id, null);
	}

	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		Function<String, Component> x = context.get(this.key);
		String var10000;
		if (x != null) {
			Component t = x.apply(this.id);
			if (t != null) {
				return t;
			} else {
				var10000 = this.key.key();
				return Component.literal("[INVALID KEY " + var10000 + " | " + this.id + "]").withStyle(ChatFormatting.ITALIC).withColor(16711680);
			}
		} else {
			var10000 = this.key.key();
			return Component.literal("[MISSING CONTEXT FOR " + var10000 + " | " + this.id + "]").withStyle(ChatFormatting.ITALIC).withColor(16711680);
		}
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
