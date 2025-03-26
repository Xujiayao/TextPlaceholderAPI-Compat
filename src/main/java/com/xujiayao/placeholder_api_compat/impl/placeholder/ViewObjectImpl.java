package com.xujiayao.placeholder_api_compat.impl.placeholder;

import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import net.minecraft.util.Identifier;

public record ViewObjectImpl(Identifier identifier) implements PlaceholderContext.ViewObject {
}
