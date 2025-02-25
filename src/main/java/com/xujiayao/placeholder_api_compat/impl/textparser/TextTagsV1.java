package com.xujiayao.placeholder_api_compat.impl.textparser;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.xujiayao.placeholder_api_compat.api.node.DirectTextNode;
import com.xujiayao.placeholder_api_compat.api.node.KeybindNode;
import com.xujiayao.placeholder_api_compat.api.node.LiteralNode;
import com.xujiayao.placeholder_api_compat.api.node.NbtNode;
import com.xujiayao.placeholder_api_compat.api.node.ScoreNode;
import com.xujiayao.placeholder_api_compat.api.node.SelectorNode;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.TranslatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.BoldNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ClickActionNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ColorNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.FontNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.FormattingNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.GradientNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.HoverNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.InsertNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ItalicNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ObfuscatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.StrikethroughNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.TransformNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.UnderlinedNode;
import com.xujiayao.placeholder_api_compat.api.parsers.TextParserV1;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Deprecated
@Internal
public final class TextTagsV1 {
	public static void register() {
		{
			Map<String, List<String>> aliases = new HashMap<>();
			aliases.put("gold", List.of("orange"));
			aliases.put("gray", List.of("grey"));
			aliases.put("light_purple", List.of("pink"));
			aliases.put("dark_gray", List.of("dark_grey"));

			for (ChatFormatting formatting : ChatFormatting.values()) {
				if (!formatting.isFormat()) {
					TextParserV1.registerDefault(TextParserV1.TextTag.of(formatting.getName(), aliases.containsKey(formatting.getName()) ? aliases.get(formatting.getName()) : List.of(), "color", true, wrap((nodes, arg) -> new FormattingNode(nodes, formatting))));
				}
			}
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("bold", List.of("b"), "formatting", true, bool(BoldNode::new)));

			TextParserV1.registerDefault(TextParserV1.TextTag.of("underline", List.of("underlined", "u"), "formatting", true, bool(UnderlinedNode::new)));

			TextParserV1.registerDefault(TextParserV1.TextTag.of("strikethrough", List.of("st"), "formatting", true, bool(StrikethroughNode::new)));

			TextParserV1.registerDefault(TextParserV1.TextTag.of("obfuscated", List.of("obf", "matrix"), "formatting", true, bool(ObfuscatedNode::new)));

			TextParserV1.registerDefault(TextParserV1.TextTag.of("italic", List.of("i", "em"), "formatting", true, bool(ItalicNode::new)));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("color", List.of("colour", "c"), "color", true, wrap((nodes, data) -> new ColorNode(nodes, TextColor.parseColor(TextParserImpl.cleanArgument(data)).result().orElse(null)))));
		}
		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("font", "other_formatting", false, wrap((nodes, data) -> new FontNode(nodes, ResourceLocation.tryParse(TextParserImpl.cleanArgument(data))))));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("lang", List.of("translate"), "special", false, (tag, data, input, handlers, endAt) -> {
				String[] lines = data.split(":");
				if (lines.length > 0) {
					List<TextNode> textList = new ArrayList<>();
					boolean skipped = false;
					for (String part : lines) {
						if (!skipped) {
							skipped = true;
						} else {
							textList.add(new ParentNode(TextParserImpl.parse(TextParserImpl.removeEscaping(TextParserImpl.cleanArgument(part)), handlers)));
						}
					}

					TranslatedNode out = TranslatedNode.of(TextParserImpl.removeEscaping(TextParserImpl.cleanArgument(lines[0])), (Object[]) textList.toArray(TextParserImpl.CASTER));
					return new TextParserV1.TagNodeValue(out, 0);
				} else {
					return TextParserV1.TagNodeValue.EMPTY;
				}
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("lang_fallback", List.of("translatef", "langf", "translate_fallback"), "special", false, (tag, data, input, handlers, endAt) -> {
				String[] lines = data.split(":");
				if (lines.length > 1) {
					List<TextNode> textList = new ArrayList<>();
					int skipped = 0;
					for (String part : lines) {
						if (skipped < 2) {
							++skipped;
						} else {
							textList.add(new ParentNode(TextParserImpl.parse(TextParserImpl.removeEscaping(TextParserImpl.cleanArgument(part)), handlers)));
						}
					}

					TranslatedNode out = TranslatedNode.ofFallback(TextParserImpl.removeEscaping(TextParserImpl.cleanArgument(lines[0])), TextParserImpl.removeEscaping(TextParserImpl.cleanArgument(lines[1])), (Object[]) textList.toArray(TextParserImpl.CASTER));
					return new TextParserV1.TagNodeValue(out, 0);
				} else {
					return TextParserV1.TagNodeValue.EMPTY;
				}
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("keybind", List.of("key"), "special", false, (tag, data, input, handlers, endAt) -> !data.isEmpty() ? new TextParserV1.TagNodeValue(new KeybindNode(TextParserImpl.cleanArgument(data)), 0) : TextParserV1.TagNodeValue.EMPTY));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("click", "click_action", false, (tag, data, input, handlers, endAt) -> {
				String[] lines = data.split(":", 2);
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				if (lines.length > 1) {
					for (Action action : Action.values()) {
						if (action.getSerializedName().equals(TextParserImpl.cleanArgument(lines[0]))) {
							return out.value(new ClickActionNode(out.nodes(), action, new LiteralNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[1])))));
						}
					}
				}
				return out.value(new ParentNode(out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("run_command", List.of("run_cmd"), "click_action", false, (tag, data, input, handlers, endAt) -> {
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return !data.isEmpty() ? out.value(new ClickActionNode(out.nodes(), Action.RUN_COMMAND, new LiteralNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data))))) : out.value(new ParentNode(out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("suggest_command", List.of("cmd"), "click_action", false, (tag, data, input, handlers, endAt) -> {
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return !data.isEmpty() ? out.value(new ClickActionNode(out.nodes(), Action.SUGGEST_COMMAND, new LiteralNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data))))) : out.value(new ParentNode(out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("open_url", List.of("url"), "click_action", false, (tag, data, input, handlers, endAt) -> {
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return !data.isEmpty() ? out.value(new ClickActionNode(out.nodes(), Action.OPEN_URL, new LiteralNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data))))) : out.value(new ParentNode(out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("copy_to_clipboard", List.of("copy"), "click_action", false, (tag, data, input, handlers, endAt) -> {
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return !data.isEmpty() ? out.value(new ClickActionNode(out.nodes(), Action.COPY_TO_CLIPBOARD, new LiteralNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data))))) : out.value(new ParentNode(out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("change_page", List.of("page"), "click_action", true, (tag, data, input, handlers, endAt) -> {
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return !data.isEmpty() ? out.value(new ClickActionNode(out.nodes(), Action.CHANGE_PAGE, new LiteralNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data))))) : out.value(new ParentNode(out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("hover", "hover_event", true, (tag, data, input, handlers, endAt) -> {
				String[] lines = data.split(":", 2);
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);

				try {
					if (lines.length <= 1) {
						return out.value(new HoverNode<>(out.nodes(), HoverNode.Action.TEXT, new ParentNode(TextParserImpl.parse(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data)), handlers))));
					}

					HoverEvent.Action<?> action = HoverEvent.Action.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(TextParserImpl.cleanArgument(lines[0].toLowerCase(Locale.ROOT)))).result().orElse(null);
					if (action == net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT) {
						return out.value(new HoverNode<>(out.nodes(), HoverNode.Action.TEXT, new ParentNode(TextParserImpl.parse(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[1])), handlers))));
					}

					if (action == net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY) {
						lines = lines[1].split(":", 3);
						if (lines.length == 3) {
							return out.value(new HoverNode<>(out.nodes(), HoverNode.Action.ENTITY, new HoverNode.EntityNodeContent(EntityType.byString(TextParserImpl.restoreOriginalEscaping(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[0])))).orElse(EntityType.PIG), UUID.fromString(TextParserImpl.cleanArgument(lines[1])), new ParentNode(TextParserImpl.parse(TextParserImpl.restoreOriginalEscaping(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[2]))), handlers)))));
						}
					} else {
						if (action != net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM) {
							return out.value(new HoverNode<>(out.nodes(), HoverNode.Action.TEXT, new ParentNode(TextParserImpl.parse(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data)), handlers))));
						}

						try {
							return out.value(new HoverNode<>(out.nodes(), HoverNode.Action.ITEM_STACK, new HoverEvent.ItemStackInfo(ItemStack.parseOptional(RegistryAccess.EMPTY, TagParser.parseTag(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[1])))))));
						} catch (Throwable var10) {
							lines = lines[1].split(":", 2);
							if (lines.length > 0) {
								ItemStack stack = BuiltInRegistries.ITEM.getValue(ResourceLocation.tryParse(lines[0])).getDefaultInstance();
								if (lines.length > 1) {
									stack.setCount(Integer.parseInt(lines[1]));
								}

								return out.value(new HoverNode<>(out.nodes(), HoverNode.Action.ITEM_STACK, new HoverEvent.ItemStackInfo(stack)));
							}
						}
					}
				} catch (Exception e) {
					// Shut
				}
				return out.value(new ParentNode(out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("insert", List.of("insertion"), "click_action", false, (tag, data, input, handlers, endAt) -> {
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return out.value(new InsertNode(out.nodes(), new LiteralNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data)))));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("clear_color", List.of("uncolor", "colorless"), "special", false, (tag, data, input, handlers, endAt) -> {
				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return out.value(GeneralUtils.removeColors(new ParentNode(out.nodes())));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("rainbow", List.of("rb"), "gradient", true, (tag, data, input, handlers, endAt) -> {
				String[] val = data.split(":");
				float freq = 1.0F;
				float saturation = 1.0F;
				float offset = 0.0F;
				int overriddenLength = -1;

				if (val.length >= 1) {
					try {
						freq = Float.parseFloat(val[0]);
					} catch (Exception e) {
						// No u
					}
				}
				if (val.length >= 2) {
					try {
						saturation = Float.parseFloat(val[1]);
					} catch (Exception e) {
						// Idc
					}
				}
				if (val.length >= 3) {
					try {
						offset = Float.parseFloat(val[2]);
					} catch (Exception e) {
						// Ok float
					}
				}
				if (val.length >= 4) {
					try {
						overriddenLength = Integer.parseInt(val[3]);
					} catch (Exception e) {
						// Ok float
					}
				}

				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);

				return out.value(overriddenLength < 0 ? GradientNode.rainbow(saturation, 1.0F, freq, offset, out.nodes()) : GradientNode.rainbow(saturation, 1.0F, freq, offset, overriddenLength, out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("gradient", List.of("gr"), "gradient", true, (tag, data, input, handlers, endAt) -> {
				String[] val = data.split(":");

				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				List<TextColor> textColors = new ArrayList<>();
				for (String string : val) {
					TextColor.parseColor(string).result().ifPresent(textColors::add);
				}
				return out.value(GradientNode.colors(textColors, out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("hard_gradient", List.of("hgr"), "gradient", true, (tag, data, input, handlers, endAt) -> {
				String[] val = data.split(":");

				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);

				List<TextColor> textColors = new ArrayList<>();

				for (String string : val) {
					TextColor.parseColor(string).result().ifPresent(textColors::add);
				}

				return textColors.isEmpty() ? out.value(new ParentNode(out.nodes())) : out.value(GradientNode.colorsHard(textColors, out.nodes()));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("clear", "special", false, (tag, data, input, handlers, endAt) -> {
				String[] val = data.isEmpty() ? new String[0] : data.split(":");

				TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
				return out.value(new TransformNode(out.nodes(), getTransform(val)));
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("raw_style", "special", false, (tag, data, input, handlers, endAt) -> new TextParserV1.TagNodeValue(new DirectTextNode(Serializer.fromJsonLenient(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(data)), RegistryAccess.EMPTY)), 0)));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("score", "special", false, (tag, data, input, handlers, endAt) -> {
				String[] lines = data.split(":");
				return lines.length == 2 ? new TextParserV1.TagNodeValue(new ScoreNode(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[0])), TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[1]))), 0) : TextParserV1.TagNodeValue.EMPTY;
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("selector", "special", false, (tag, data, input, handlers, endAt) -> {
				String[] lines = data.split(":");
				String pattern = TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[0]));
				Optional<SelectorPattern> optional = SelectorPattern.parse(pattern).result();
				if (optional.isEmpty()) {
					return TextParserV1.TagNodeValue.EMPTY;
				} else if (lines.length == 2) {
					return new TextParserV1.TagNodeValue(new SelectorNode(optional.get(), Optional.of(TextNode.asSingle(TextParserImpl.recursiveParsing(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[1])), handlers, null).nodes()))), 0);
				} else {
					return lines.length == 1 ? new TextParserV1.TagNodeValue(new SelectorNode(optional.get(), Optional.empty()), 0) : TextParserV1.TagNodeValue.EMPTY;
				}
			}));
		}

		{
			TextParserV1.registerDefault(TextParserV1.TextTag.of("nbt", "special", false, (tag, data, input, handlers, endAt) -> {
				String[] lines = data.split(":");

				if (lines.length < 3) {
					return TextParserV1.TagNodeValue.EMPTY;
				} else {
					String cleanLine1 = TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[1]));
					Record type = switch (lines[0]) {
						case "block" -> new BlockDataSource(cleanLine1);
						case "entity" -> new EntityDataSource(cleanLine1);
						case "storage" -> new StorageDataSource(ResourceLocation.tryParse(cleanLine1));
						default -> null;
					};

					if (type == null) {
						return TextParserV1.TagNodeValue.EMPTY;
					} else {
						Optional<TextNode> separator = lines.length > 3 ? Optional.of(TextNode.asSingle(TextParserImpl.recursiveParsing(TextParserImpl.restoreOriginalEscaping(TextParserImpl.cleanArgument(lines[3])), handlers, null).nodes())) : Optional.empty();
						boolean shouldInterpret = lines.length > 4 && Boolean.parseBoolean(lines[4]);

						return new TextParserV1.TagNodeValue(new NbtNode(lines[2], shouldInterpret, separator, (DataSource) type), 0);
					}
				}
			}));
		}
	}

	private static Function<MutableComponent, Component> getTransform(String[] val) {
		if (val.length == 0) {
			return GeneralUtils.MutableTransformer.CLEAR;
		} else {
			Function<Style, Style> func = (x) -> x;

			for (var arg : val) {
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
		return !arg.equals("false");
	}

	private static TextParserV1.TagNodeBuilder wrap(Wrapper wrapper) {
		return (tag, data, input, handlers, endAt) -> {
			TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
			return new TextParserV1.TagNodeValue(wrapper.wrap(out.nodes(), data), out.length());
		};
	}

	private static TextParserV1.TagNodeBuilder bool(BooleanTag wrapper) {
		return (tag, data, input, handlers, endAt) -> {
			TextParserV1.NodeList out = TextParserImpl.recursiveParsing(input, handlers, endAt);
			return new TextParserV1.TagNodeValue(wrapper.wrap(out.nodes(), isntFalse(data)), out.length());
		};
	}

	interface Wrapper {
		TextNode wrap(TextNode[] nodes, String arg);
	}

	interface BooleanTag {
		TextNode wrap(TextNode[] nodes, boolean value);
	}
}