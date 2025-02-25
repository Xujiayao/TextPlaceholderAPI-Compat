package com.xujiayao.placeholder_api_compat.impl.placeholder;

import com.xujiayao.placeholder_api_compat.api.PlaceholderContext;
import net.minecraft.resources.ResourceLocation;

public record ViewObjectImpl(ResourceLocation identifier) implements PlaceholderContext.ViewObject {
}
