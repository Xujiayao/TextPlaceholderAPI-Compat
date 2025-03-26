package com.xujiayao.placeholder_api_compat.impl.placeholder;

import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import com.xujiayao.placeholder_api_compat.api.Placeholders;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.ParserContext;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;


@ApiStatus.Internal
public record PlaceholderNode(ParserContext.Key<PlaceholderContext> contextKey, String placeholder, Placeholders.PlaceholderGetter getter, boolean optionalContext, @Nullable String argument) implements TextNode {
    @Override
    public Text toText(ParserContext context, boolean removeBackslashes) {
        var ctx = context.get(contextKey);
        var handler = getter.getPlaceholder(placeholder, context);
        if ((ctx != null || this.optionalContext) && handler != null) {
            try {
                return handler.onPlaceholderRequest(ctx, argument).text();
            } catch (Throwable e) {
                GeneralUtils.LOGGER.error("Error occurred while parsing placeholder " + placeholder + " / " + contextKey.key() + "!", e);
                return Text.empty();
            }
        } else {
            if (GeneralUtils.IS_DEV) {
                GeneralUtils.LOGGER.error("Missing context for placeholders requiring them (" + placeholder + " / " + contextKey.key() + ")!", new NullPointerException());
            }
            return Text.empty();
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
