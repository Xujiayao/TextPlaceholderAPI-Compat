package eu.pb4.placeholders.impl;

import eu.pb4.placeholders.api.node.KeybindNode;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.NbtNode;
import eu.pb4.placeholders.api.node.ScoreNode;
import eu.pb4.placeholders.api.node.SelectorNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.TranslatedNode;
import eu.pb4.placeholders.api.node.parent.ColorNode;
import eu.pb4.placeholders.api.node.parent.FormattingNode;
import eu.pb4.placeholders.api.node.parent.GradientNode;
import eu.pb4.placeholders.api.node.parent.ParentNode;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import eu.pb4.placeholders.api.node.parent.StyledNode;
import net.fabricmc.loader.api.FabricLoader;
//#if MC > 12004
import net.minecraft.component.DataComponentTypes;
//#endif
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.KeybindTextContent;
//#if MC <= 12002
//$$ import net.minecraft.text.LiteralTextContent;
//#endif
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Function;

@ApiStatus.Internal
public class GeneralUtils {
	public static final Logger LOGGER = LoggerFactory.getLogger("Text Placeholder API");
	public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
	public static final TextNode[] CASTER = new TextNode[0];

	public static String durationToString(long x) {
		long seconds = x % 60;
		long minutes = (x / 60) % 60;
		long hours = (x / (60 * 60)) % 24;
		long days = x / (60 * 60 * 24);

		if (days > 0) {
			return String.format("%dd%dh%dm%ds", days, hours, minutes, seconds);
		} else if (hours > 0) {
			return String.format("%dh%dm%ds", hours, minutes, seconds);
		} else if (minutes > 0) {
			return String.format("%dm%ds", minutes, seconds);
		} else if (seconds > 0) {
			return String.format("%ds", seconds);
		} else {
			return "---";
		}
	}

	public static boolean isEmpty(Text text) {
		//#if MC > 12002
		return (getContent(text) == PlainTextContent.EMPTY || (getContent(text) instanceof PlainTextContent.Literal l && l.string().isEmpty())) && text.getSiblings().isEmpty();
		//#else
		//$$ return (getContent(text) == TextContent.EMPTY || (getContent(text) instanceof LiteralTextContent l && l.string().isEmpty())) && text.getSiblings().isEmpty();
		//#endif
	}

	public static MutableText toGradient(Text base, GradientNode.GradientProvider posToColor) {
		return recursiveGradient(base, posToColor, 0, getGradientLength(base)).text();
	}

	private static int getGradientLength(Text base) {
		//#if MC > 12002
		int length = getContent(base) instanceof PlainTextContent.Literal l ? l.string().codePointCount(0, l.string().length()) : getContent(base) == PlainTextContent.EMPTY ? 0 : 1;
		//#else
		//$$ int length = getContent(base) instanceof LiteralTextContent l ? l.string().codePointCount(0, l.string().length()) : getContent(base) == TextContent.EMPTY ? 0 : 1;
		//#endif

		for (var text : base.getSiblings()) {
			length += getGradientLength(text);
		}

		return length;
	}

	private static TextLengthPair recursiveGradient(Text base, GradientNode.GradientProvider posToColor, int pos, int totalLength) {
		if (base.getStyle().getColor() == null) {
			//#if MC > 11802
			MutableText out = Text.empty().setStyle(base.getStyle());
			//#else
			//$$ MutableText out = new LiteralText("").setStyle(base.getStyle());
			//#endif
			//#if MC > 12002
			if (getContent(base) instanceof PlainTextContent.Literal literalTextContent) {
			//#else
			//$$ if (getContent(base) instanceof LiteralTextContent literalTextContent) {
			//#endif
				var l = literalTextContent.string().length();
				for (var i = 0; i < l; i++) {
					var character = literalTextContent.string().charAt(i);
					int value;
					if (Character.isHighSurrogate(character) && i + 1 < l) {
						var next = literalTextContent.string().charAt(++i);
						if (Character.isLowSurrogate(next)) {
							value = Character.toCodePoint(character, next);
						} else {
							value = character;
						}
					} else {
						value = character;
					}

					//#if MC > 11802
					out.append(Text.literal(Character.toString(value)).setStyle(Style.EMPTY.withColor(posToColor.getColorAt(pos++, totalLength))));
					//#else
					//$$ out.append(new LiteralText(Character.toString(value)).setStyle(Style.EMPTY.withColor(posToColor.getColorAt(pos++, totalLength))));
					//#endif
				}
			} else {
				out.append(base.copyContentOnly().setStyle(Style.EMPTY.withColor(posToColor.getColorAt(pos++, totalLength))));
			}

			for (Text sibling : base.getSiblings()) {
				var pair = recursiveGradient(sibling, posToColor, pos, totalLength);
				pos = pair.length;
				out.append(pair.text);
			}
			return new TextLengthPair(out, pos);
		}
		return new TextLengthPair(base.copy(), pos + base.getString().length());
	}

	public static int rgbToInt(float r, float g, float b) {
		return (((int) (r * 0xff)) & 0xFF) << 16 | (((int) (g * 0xff)) & 0xFF) << 8 | (((int) (b * 0xff) & 0xFF));
	}

	public static Text deepTransform(Text input) {
		var output = cloneText(input);
		removeHoverAndClick(output);
		return output;
	}

	public static Text removeHoverAndClick(Text input) {
		var output = cloneText(input);
		removeHoverAndClick(output);
		return output;
	}

	private static void removeHoverAndClick(MutableText input) {
		if (input.getStyle() != null) {
			input.setStyle(input.getStyle().withHoverEvent(null).withClickEvent(null));
		}

		if (getContent(input) instanceof TranslatableTextContent text) {
			for (int i = 0; i < text.getArgs().length; i++) {
				var arg = text.getArgs()[i];
				if (arg instanceof MutableText argText) {
					removeHoverAndClick(argText);
				}
			}
		}

		for (var sibling : input.getSiblings()) {
			removeHoverAndClick((MutableText) sibling);
		}

	}

	public static MutableText cloneText(Text input) {
		MutableText baseText;
		if (getContent(input) instanceof TranslatableTextContent translatable) {
			var obj = new ArrayList<>();

			for (var arg : translatable.getArgs()) {
				if (arg instanceof Text argText) {
					obj.add(cloneText(argText));
				} else {
					obj.add(arg);
				}
			}

			//#if MC > 11802
			baseText = Text.translatable(translatable.getKey(), obj.toArray());
			//#else
			//$$ baseText = new TranslatableText(translatable.getKey(), obj.toArray());
			//#endif
		} else {
			baseText = input.copyContentOnly();
		}

		for (var sibling : input.getSiblings()) {
			baseText.append(cloneText(sibling));
		}

		baseText.setStyle(input.getStyle());
		return baseText;
	}

	public static MutableText cloneTransformText(Text input, Function<MutableText, MutableText> transform) {
		MutableText baseText;
		if (getContent(input) instanceof TranslatableTextContent translatable) {
			var obj = new ArrayList<>();

			for (var arg : translatable.getArgs()) {
				if (arg instanceof Text argText) {
					obj.add(cloneTransformText(argText, transform));
				} else {
					obj.add(arg);
				}
			}

			//#if MC > 11802
			baseText = Text.translatable(translatable.getKey(), obj.toArray());
			//#else
			//$$ baseText = new TranslatableText(translatable.getKey(), obj.toArray());
			//#endif
		} else {
			baseText = input.copyContentOnly();
		}

		for (var sibling : input.getSiblings()) {
			baseText.append(cloneTransformText(sibling, transform));
		}

		baseText.setStyle(input.getStyle());
		return transform.apply(baseText);
	}

	public static Text getItemText(ItemStack stack, boolean rarity) {
		if (!stack.isEmpty()) {
			//#if MC > 11802
			MutableText mutableText = Text.empty().append(stack.getName());
			//#else
			//$$ MutableText mutableText = new LiteralText("").append(stack.getName());
			//#endif
			//#if MC > 12004
			if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
			//#else
			//$$ if (stack.hasCustomName()) {
			//#endif
				mutableText.formatted(Formatting.ITALIC);
			}

			if (rarity) {
				//#if MC > 12004
				mutableText.formatted(stack.getRarity().getFormatting());
				//#else
				//$$ mutableText.formatted(stack.getRarity().formatting);
				//#endif
			}
			mutableText.styled((style) -> {
				return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack)));
			});

			return mutableText;
		}

		//#if MC > 11802
		return Text.empty().append(ItemStack.EMPTY.getName());
		//#else
		//$$ return new LiteralText("").append(ItemStack.EMPTY.getName());
		//#endif
	}

	public static ParentNode convertToNodes(Text input) {
		var list = new ArrayList<TextNode>();

		//#if MC > 12002
		if (getContent(input) instanceof PlainTextContent.Literal content) {
		//#else
		//$$ if (getContent(input) instanceof LiteralTextContent content) {
		//#endif
			list.add(new LiteralNode(content.string()));
		} else if (getContent(input) instanceof TranslatableTextContent content) {
			var args = new ArrayList<>();
			for (var arg : content.getArgs()) {
				if (arg instanceof Text text) {
					args.add(convertToNodes(text));
				} else if (arg instanceof String s) {
					args.add(new LiteralNode(s));
				} else {
					args.add(arg);
				}
			}

			//#if MC > 11903
			list.add(TranslatedNode.ofFallback(content.getKey(), content.getFallback(), args.toArray()));
			//#else
			//$$ list.add(TranslatedNode.of(content.getKey(), args.toArray()));
			//#endif
		} else if (getContent(input) instanceof ScoreTextContent content) {
			list.add(new ScoreNode(content.name(), content.objective()));
		} else if (getContent(input) instanceof KeybindTextContent content) {
			list.add(new KeybindNode(content.getKey()));
		} else if (getContent(input) instanceof SelectorTextContent content) {
			list.add(new SelectorNode(content.selector(), content.separator().map(GeneralUtils::convertToNodes)));
		} else if (getContent(input) instanceof NbtTextContent content) {
			//#if MC > 11802
			list.add(new NbtNode(content.getPath(), content.shouldInterpret(), content.getSeparator().map(GeneralUtils::convertToNodes), content.getDataSource()));
			//#endif
		}

		for (var child : input.getSiblings()) {
			list.add(convertToNodes(child));
		}

		if (input.getStyle() == Style.EMPTY) {
			return new ParentNode(list);
		} else {
			var style = input.getStyle();
			var hoverValue = style.getHoverEvent() != null && style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT ? convertToNodes(style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT)) : null;

			var clickValue = style.getClickEvent() != null ? new LiteralNode(style.getClickEvent().getValue()) : null;
			var insertion = style.getInsertion() != null ? new LiteralNode(style.getInsertion()) : null;

			return new StyledNode(list.toArray(new TextNode[0]), style, hoverValue, clickValue, insertion);
		}
	}

	public static TextNode removeColors(TextNode node) {
		if (node instanceof ParentTextNode parentNode) {
			var list = new ArrayList<TextNode>();

			for (var child : parentNode.getChildren()) {
				list.add(removeColors(child));
			}

			if (node instanceof ColorNode || node instanceof FormattingNode) {
				return new ParentNode(list.toArray(new TextNode[0]));
			} else if (node instanceof StyledNode styledNode) {
				return new StyledNode(list.toArray(new TextNode[0]), styledNode.rawStyle().withColor((TextColor) null), styledNode.hoverValue(), styledNode.clickValue(), styledNode.insertion());
			}

			return parentNode.copyWith(list.toArray(new TextNode[0]));
		} else {
			return node;
		}
	}

	public record TextLengthPair(MutableText text, int length) {
		public static final TextLengthPair EMPTY = new TextLengthPair(null, 0);
	}

	public record Pair<L, R>(L left, R right) {
	}

	public record MutableTransformer(
			Function<Style, Style> textMutableTextFunction) implements Function<MutableText, Text> {
		public static final MutableTransformer CLEAR = new MutableTransformer(x -> Style.EMPTY);

		@Override
		public Text apply(MutableText text) {
			return GeneralUtils.cloneTransformText(text, this::transformStyle);
		}

		private MutableText transformStyle(MutableText mutableText) {
			return mutableText.setStyle(textMutableTextFunction.apply(mutableText.getStyle()));
		}
	}

	//#if MC > 11802
	private static TextContent getContent(Text text) {
		return text.getContent();
	}
	//#else
	//$$ private static Text getContent(Text text) {
	//$$ 	return text;
	//$$ }
	//#endif
}
