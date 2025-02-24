package eu.pb4.placeholders.api.node.parent;

import com.mojang.serialization.DynamicOps;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public final class HoverNode<T, H> extends SimpleStylingNode {
	private final Action<T, H> action;
	private final T value;

	public HoverNode(TextNode[] children, Action<T, H> action, T value) {
		super(children);
		this.action = action;
		this.value = value;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Style style(ParserContext context) {
		if (this.action == HoverNode.Action.TEXT) {
			return Style.EMPTY.withHoverEvent(new HoverEvent((HoverEvent.Action<Object>) this.action.vanillaType(), ((TextNode) this.value).toText(context, true)));
		} else if (this.action == HoverNode.Action.ENTITY) {
			return Style.EMPTY.withHoverEvent(new HoverEvent((HoverEvent.Action<Object>) this.action.vanillaType(), ((EntityNodeContent) this.value).toVanilla(context)));
		} else if (this.action == HoverNode.Action.LAZY_ITEM_STACK) {
			HolderLookup.Provider wrapper;
			if (context.contains(ParserContext.Key.WRAPPER_LOOKUP)) {
				wrapper = context.getOrThrow(ParserContext.Key.WRAPPER_LOOKUP);
			} else if (context.contains(PlaceholderContext.KEY)) {
				wrapper = ((PlaceholderContext) context.getOrThrow(PlaceholderContext.KEY)).server().registryAccess();
			} else {
				return Style.EMPTY;
			}

			return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, ((LazyItemStackNodeContent<?>) this.value).toVanilla(wrapper)));
		} else {
			return Style.EMPTY.withHoverEvent(new HoverEvent((HoverEvent.Action<Object>) this.action.vanillaType(), this.value));
		}
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new HoverNode<>(children, this.action, this.value);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
		if (this.action == HoverNode.Action.TEXT) {
			return new HoverNode<>(children, HoverNode.Action.TEXT, parser.parseNode((TextNode) this.value));
		} else if (this.action == HoverNode.Action.ENTITY && ((EntityNodeContent) this.value).name != null) {
			EntityNodeContent val = (EntityNodeContent) this.value;
			return new HoverNode<>(children, HoverNode.Action.ENTITY, new EntityNodeContent(val.entityType, val.uuid, parser.parseNode(val.name)));
		} else {
			return this.copyWith(children);
		}
	}

	public Action<T, H> action() {
		return this.action;
	}

	public T value() {
		return this.value;
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.value);
		return "HoverNode{value=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}

	@Override
	public boolean isDynamicNoChildren() {
		return this.action == HoverNode.Action.TEXT && ((TextNode) this.value).isDynamic() || this.action == HoverNode.Action.ENTITY && ((EntityNodeContent) this.value).name.isDynamic() || this.action == HoverNode.Action.LAZY_ITEM_STACK;
	}

	public record Action<T, H>(HoverEvent.Action<H> vanillaType) {
		public static final Action<EntityNodeContent, HoverEvent.EntityTooltipInfo> ENTITY;
		public static final Action<TextNode, Component> TEXT;
		public static final Action<HoverEvent.ItemStackInfo, HoverEvent.ItemStackInfo> ITEM_STACK;
		public static final Action<LazyItemStackNodeContent<?>, HoverEvent.ItemStackInfo> LAZY_ITEM_STACK;

		static {
			ENTITY = new Action<>(HoverEvent.Action.SHOW_ENTITY);
			TEXT = new Action<>(HoverEvent.Action.SHOW_TEXT);
			ITEM_STACK = new Action<>(HoverEvent.Action.SHOW_ITEM);
			LAZY_ITEM_STACK = new Action<>(HoverEvent.Action.SHOW_ITEM);
		}
	}

	public record EntityNodeContent(EntityType<?> entityType, UUID uuid, @Nullable TextNode name) {
		public HoverEvent.EntityTooltipInfo toVanilla(ParserContext context) {
			return new HoverEvent.EntityTooltipInfo(this.entityType, this.uuid, this.name != null ? this.name.toText(context, true) : null);
		}
	}

	public record LazyItemStackNodeContent<T>(ResourceLocation identifier, int count, DynamicOps<T> ops, T componentMap) {
		public HoverEvent.ItemStackInfo toVanilla(HolderLookup.Provider lookup) {
			ItemStack stack = new ItemStack(lookup.lookupOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(Registries.ITEM, this.identifier)));
			stack.setCount(this.count);
			stack.applyComponentsAndValidate(DataComponentPatch.CODEC.decode(lookup.createSerializationContext(this.ops), this.componentMap).getOrThrow().getFirst());
			return new HoverEvent.ItemStackInfo(stack);
		}
	}
}
