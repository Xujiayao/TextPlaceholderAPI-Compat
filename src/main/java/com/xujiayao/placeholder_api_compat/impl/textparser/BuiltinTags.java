package com.xujiayao.placeholder_api_compat.impl.textparser;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.xujiayao.placeholder_api_compat.api.arguments.SimpleArguments;
import com.xujiayao.placeholder_api_compat.api.arguments.StringArgs;
import com.xujiayao.placeholder_api_compat.api.node.KeybindNode;
import com.xujiayao.placeholder_api_compat.api.node.NbtNode;
import com.xujiayao.placeholder_api_compat.api.node.ScoreNode;
import com.xujiayao.placeholder_api_compat.api.node.SelectorNode;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.TranslatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.BoldNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ClickActionNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.DynamicColorNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.FontNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.GradientNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.HoverNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.InsertNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ItalicNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ObfuscatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.StrikethroughNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.StyledNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.TransformNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.UnderlinedNode;
import com.xujiayao.placeholder_api_compat.api.parsers.tag.NodeCreator;
import com.xujiayao.placeholder_api_compat.api.parsers.tag.SimpleTags;
import com.xujiayao.placeholder_api_compat.api.parsers.tag.TagRegistry;
import com.xujiayao.placeholder_api_compat.api.parsers.tag.TextTag;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import com.xujiayao.placeholder_api_compat.impl.StringArgOps;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
//#if MC > 12101
import net.minecraft.commands.arguments.selector.SelectorPattern;
//#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Style.Serializer;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Internal
@SuppressWarnings("deprecation")
public final class BuiltinTags {
	public static final TextColor DEFAULT_COLOR;

	static {
		DEFAULT_COLOR = TextColor.fromLegacyFormat(ChatFormatting.WHITE);
	}

	public static void register() {
		{
			Map<ChatFormatting, List<String>> aliases = new HashMap<>();
			aliases.put(ChatFormatting.GOLD, List.of("orange"));
			aliases.put(ChatFormatting.GRAY, List.of("grey", "light_gray", "light_grey"));
			aliases.put(ChatFormatting.LIGHT_PURPLE, List.of("pink"));
			aliases.put(ChatFormatting.DARK_PURPLE, List.of("purple"));
			aliases.put(ChatFormatting.DARK_GRAY, List.of("dark_grey"));

			for (ChatFormatting formatting : ChatFormatting.values()) {
				if (formatting.isFormat()) {
					continue;
				}

				TagRegistry.registerDefault(SimpleTags.color(formatting.getName(), aliases.containsKey(formatting) ? aliases.get(formatting) : List.of(), formatting));
			}
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("bold", List.of("b"), "formatting", true, NodeCreator.bool(BoldNode::new)));

			TagRegistry.registerDefault(TextTag.enclosing("underline", List.of("underlined", "u"), "formatting", true, NodeCreator.bool(UnderlinedNode::new)));

			TagRegistry.registerDefault(TextTag.enclosing("strikethrough", List.of("st"), "formatting", true, NodeCreator.bool(StrikethroughNode::new)));

			TagRegistry.registerDefault(TextTag.enclosing("obfuscated", List.of("obf", "matrix"), "formatting", true, NodeCreator.bool(ObfuscatedNode::new)));

			TagRegistry.registerDefault(TextTag.enclosing("italic", List.of("i", "em"), "formatting", true, NodeCreator.bool(ItalicNode::new)));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("color", List.of("colour", "c"), "color", true, (nodes, data, parser) -> new DynamicColorNode(nodes, parser.parseNode(data.get("value", 0, "white")))));
		}
		{
			TagRegistry.registerDefault(TextTag.enclosing("font", "other_formatting", false, (nodes, data, parser) -> new FontNode(nodes, ResourceLocation.tryParse(data.get("value", 0, "")))));
		}
		{
			TagRegistry.registerDefault(TextTag.self("lang", List.of("translate"), "special", false, (nodes, data, parser) -> {
				if (data.isEmpty()) {
					return TextNode.empty();
				} else {
					String key = data.getNext("key");
					String fallback = data.get("fallback");

					List<TextNode> textList = new ArrayList<>();
					int i = 0;
					while (true) {
						String part = data.getNext("" + (i++));
						if (part == null) {
							return TranslatedNode.ofFallback(key, fallback, (Object[]) textList.toArray(TextParserImpl.CASTER));
						}
						textList.add(parser.parseNode(part));
					}
				}
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.self("lang_fallback", List.of("translatef", "langf", "translate_fallback"), "special", false, (nodes, data, parser) -> {
				if (data.isEmpty()) {
					return TextNode.empty();
				} else {
					String key = data.getNext("key");
					String fallback = data.getNext("fallback");

					List<TextNode> textList = new ArrayList<>();
					int i = 0;
					while (true) {
						String part = data.getNext("" + (i++));
						if (part == null) {
							return TranslatedNode.ofFallback(key, fallback, (Object[]) textList.toArray(TextParserImpl.CASTER));
						}
						textList.add(parser.parseNode(part));
					}
				}
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.self("keybind", List.of("key"), "special", false, (data) -> new KeybindNode(data.getNext("value", ""))));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("click", "click_action", false, (nodes, data, parser) -> {
				if (!data.isEmpty()) {
					String type = data.getNext("type");
					String value = data.getNext("value", "");
					for (Action action : Action.values()) {
						if (action.getSerializedName().equals(type)) {
							return new ClickActionNode(nodes, action, parser.parseNode(value));
						}
					}
				}
				return new ParentNode(nodes);
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("run_command", List.of("run_cmd"), "click_action", false, (nodes, data, parser) -> !data.isEmpty() ? new ClickActionNode(nodes, Action.RUN_COMMAND, parser.parseNode(data.get("value", 0))) : new ParentNode(nodes)));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("suggest_command", List.of("cmd"), "click_action", false, (nodes, data, parser) -> !data.isEmpty() ? new ClickActionNode(nodes, Action.SUGGEST_COMMAND, parser.parseNode(data.getNext("value", ""))) : new ParentNode(nodes)));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("open_url", List.of("url"), "click_action", false, (nodes, data, parser) -> !data.isEmpty() ? new ClickActionNode(nodes, Action.OPEN_URL, parser.parseNode(data.get("value", 0))) : new ParentNode(nodes)));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("copy_to_clipboard", List.of("copy"), "click_action", false, (nodes, data, parser) -> !data.isEmpty() ? new ClickActionNode(nodes, Action.COPY_TO_CLIPBOARD, parser.parseNode(data.get("value", 0))) : new ParentNode(nodes)));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("change_page", List.of("page"), "click_action", true, (nodes, data, parser) -> !data.isEmpty() ? new ClickActionNode(nodes, Action.CHANGE_PAGE, parser.parseNode(data.get("value", 0))) : new ParentNode(nodes)));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("hover", "hover_event", true, (nodes, data, parser) -> {
				try {
					String type = data.get("type");

					if (type != null || data.size() > 1) {
						if (type == null) {
							type = data.getNext("type", "");
						}
						type = type.toLowerCase(Locale.ROOT);
						switch (type) {
							case "show_text", "text" -> {
								return new HoverNode<>(nodes, HoverNode.Action.TEXT, parser.parseNode(data.getNext("value", "")));
							}
							case "show_entity", "entity" -> {
								return new HoverNode<>(nodes, HoverNode.Action.ENTITY, new HoverNode.EntityNodeContent(EntityType.byString(data.getNext("entity", "")).orElse(EntityType.PIG), UUID.fromString(data.getNext("uuid", Util.NIL_UUID.toString())), new ParentNode(parser.parseNode(data.get("name", 3, "")))));
							}
							case "show_item", "item" -> {
								String value = data.getNext("value", "");
								try {
									CompoundTag nbt = TagParser.parseTag(value);
									ResourceLocation id = ResourceLocation.parse(nbt.getString("id"));
									int count = nbt.contains("count") ? nbt.getInt("count") : 1;

									CompoundTag comps = nbt.getCompound("components");
									return new HoverNode<>(nodes, HoverNode.Action.LAZY_ITEM_STACK, new HoverNode.LazyItemStackNodeContent<>(id, count, NbtOps.INSTANCE, comps));
								} catch (Throwable ignored) {
								}
								try {
									ResourceLocation item = ResourceLocation.parse(data.get("item", value));
									int count = 1;
									String countTxt = data.getNext("count");
									if (countTxt != null) {
										count = Integer.parseInt(countTxt);
									}

									return new HoverNode<>(nodes, HoverNode.Action.LAZY_ITEM_STACK, new HoverNode.LazyItemStackNodeContent<>(item, count, StringArgOps.INSTANCE, Either.right(data.getNestedOrEmpty("components"))));
								} catch (Throwable ignored) {
								}
							}
							default -> {
								return new HoverNode<>(nodes, HoverNode.Action.TEXT, parser.parseNode(data.get("value", type)));
							}
						}
					} else {
						return new HoverNode<>(nodes, HoverNode.Action.TEXT, parser.parseNode(data.getNext("value")));
					}
				} catch (Exception e) {
					// Shut
				}
				return new ParentNode(nodes);
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("insert", List.of("insertion"), "click_action", false, (nodes, data, parser) -> new InsertNode(nodes, parser.parseNode(data.get("value", 0)))));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("clear_color", List.of("uncolor", "colorless"), "special", false, (nodes, data, parser) -> GeneralUtils.removeColors(TextNode.asSingle(nodes))));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("rainbow", List.of("rb"), "gradient", true, (nodes, data, parser) -> {
				String type = data.get("type", "");

				float freq = SimpleArguments.floatNumber(data.getNext("frequency", data.get("freq", data.get("f"))), 1.0F);
				float saturation = SimpleArguments.floatNumber(data.getNext("saturation", data.get("sat", data.get("s"))), 1.0F);
				float offset = SimpleArguments.floatNumber(data.getNext("offset", data.get("off", data.get("o"))), 0.0F);
				int overriddenLength = SimpleArguments.intNumber(data.getNext("length", data.get("len", data.get("l"))), -1);
				int value = SimpleArguments.intNumber(data.get("value", data.get("val", data.get("v"))), 1);

				return new GradientNode(nodes, switch (type) {
					case "oklab", "okhcl" ->
							overriddenLength < 0 ? GradientNode.GradientProvider.rainbowOkLch(saturation, (float) value, freq, offset) : GradientNode.GradientProvider.rainbowOkLch(saturation, (float) value, freq, offset, overriddenLength);
					case "hvs" ->
							overriddenLength < 0 ? GradientNode.GradientProvider.rainbowHvs(saturation, (float) value, freq, offset) : GradientNode.GradientProvider.rainbowHvs(saturation, (float) value, freq, offset, overriddenLength);
					default ->
							overriddenLength < 0 ? GradientNode.GradientProvider.rainbow(saturation, (float) value, freq, offset) : GradientNode.GradientProvider.rainbow(saturation, (float) value, freq, offset, overriddenLength);
				});
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("gradient", List.of("gr"), "gradient", true, (nodes, data, parser) -> {
				ArrayList<TextColor> textColors = new ArrayList<>();
				int i = 0;
				String type = data.get("type", "");

				while (true) {
					String part = data.getNext("" + i);
					if (part == null) {
						return new GradientNode(nodes, switch (type) {
							case "oklab" -> GradientNode.GradientProvider.colorsOkLab(textColors);
							case "hvs" -> GradientNode.GradientProvider.colorsHvs(textColors);
							case "hard" -> GradientNode.GradientProvider.colorsHard(textColors);
							default -> GradientNode.GradientProvider.colors(textColors);
						});
					}

					TextColor.parseColor(part).result().ifPresent(textColors::add);
				}
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("hard_gradient", List.of("hgr"), "gradient", true, (nodes, data, parser) -> {
				ArrayList<TextColor> textColors = new ArrayList<>();

				int i = 0;
				while (true) {
					String part = data.getNext("" + i);
					if (part == null) {
						return textColors.isEmpty() ? new ParentNode(nodes) : GradientNode.colorsHard(textColors, nodes);
					}

					TextColor.parseColor(part).result().ifPresent(textColors::add);
				}
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("clear", "special", false, (nodes, data, parser) -> new TransformNode(nodes, getTransform(data))));
		}

		{
			TagRegistry.registerDefault(TextTag.enclosing("rawstyle", "special", false, (nodes, data, parser) -> {
				DataResult<Pair<Style, Either<String, StringArgs>>> x = Serializer.CODEC.decode(StringArgOps.INSTANCE, Either.right(data));
				if (x.error().isPresent()) {
					System.out.println(x.error().get().message());
					return TextNode.asSingle(nodes);
				} else {
					return new StyledNode(nodes, x.result().get().getFirst(), null, null, null);
				}
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.self("score", "special", false, (nodes, data, parser) -> new ScoreNode(data.getNext("name", ""), data.getNext("objective", ""))));
		}

		{
			TagRegistry.registerDefault(TextTag.self("selector", "special", false, (nodes, data, parser) -> {
				String sel = data.getNext("pattern", "@p");
				String arg = data.getNext("separator");

				//#if MC > 12101
				Optional<SelectorPattern> selector = SelectorPattern.parse(sel).result();
				return selector.isEmpty() ? TextNode.empty() : new SelectorNode(selector.get(), arg != null ? Optional.of(TextNode.of(arg)) : Optional.empty());
				//#else
				//$$ return new SelectorNode(sel, arg != null ? Optional.of(TextNode.of(arg)) : Optional.empty());
				//#endif
			}));
		}

		{
			TagRegistry.registerDefault(TextTag.self("nbt", "special", false, (nodes, data, parser) -> {
				String source = data.getNext("source", "");
				String cleanLine1 = data.getNext("path", "");

				Record type = switch (source) {
					case "block" -> new BlockDataSource(cleanLine1);
					case "entity" -> new EntityDataSource(cleanLine1);
					case "storage" -> new StorageDataSource(ResourceLocation.tryParse(cleanLine1));
					default -> null;
				};

				if (type == null) {
					return TextNode.empty();
				} else {
					String separ = data.getNext("separator");

					Optional<TextNode> separator = separ != null ? Optional.of(TextNode.asSingle(parser.parseNode(separ))) : Optional.empty();
					boolean shouldInterpret = SimpleArguments.bool(data.getNext("interpret"), false);

					return new NbtNode(cleanLine1, shouldInterpret, separator, (DataSource) type);
				}
			}));
		}
	}

	private static Function<MutableComponent, Component> getTransform(StringArgs val) {
		if (val.isEmpty()) {
			return GeneralUtils.MutableTransformer.CLEAR;
		} else {
			Function<Style, Style> func = (x) -> x;

			for (String arg : val.ordered()) {
				func = func.andThen(switch (arg) {
					case "hover" -> x -> x.withHoverEvent(null);
					case "click" -> x -> x.withClickEvent(null);
					case "color" -> x -> x.withColor((TextColor) null);
					case "insertion" -> x -> x.withInsertion(null);
					case "font" -> x -> x.withFont(null);
					case "bold" -> x -> x.withBold(null);
					case "italic" -> x -> x.withItalic(null);
					case "underline" -> x -> x.withUnderlined(null);
					case "strikethrough" -> x -> x.withStrikethrough(null);
					case "all" -> x -> Style.EMPTY;
					default -> x -> x;
				});
			}

			return new GeneralUtils.MutableTransformer(func);
		}
	}

	private static boolean isntFalse(String arg) {
		return SimpleArguments.bool(arg, arg.isEmpty());
	}
}