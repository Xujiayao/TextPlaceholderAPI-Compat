package com.xujiayao.placeholder_api_compat.impl.placeholder;

import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import com.xujiayao.placeholder_api_compat.api.PlaceholderHandler;
import com.xujiayao.placeholder_api_compat.api.Placeholders;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

@Internal
public record PlaceholderNode(ParserContext.Key<PlaceholderContext> contextKey, String placeholder,
                              Placeholders.PlaceholderGetter getter, boolean optionalContext,
                              @Nullable String argument) implements TextNode {
	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		PlaceholderContext ctx = context.get(this.contextKey);
		PlaceholderHandler handler = this.getter.getPlaceholder(this.placeholder, context);
		if ((ctx != null || this.optionalContext) && handler != null) {
			try {
				return handler.onPlaceholderRequest(ctx, this.argument).text();
			} catch (Throwable e) {
				GeneralUtils.LOGGER.error("Error occurred while parsing placeholder " + this.placeholder + " / " + this.contextKey.key() + "!", e);
				return Component.empty();
			}
		} else {
			if (GeneralUtils.IS_DEV) {
				GeneralUtils.LOGGER.error("Missing context for placeholders requiring them (" + this.placeholder + " / " + this.contextKey.key() + ")!", new NullPointerException());
			}
			return Component.empty();
		}
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
