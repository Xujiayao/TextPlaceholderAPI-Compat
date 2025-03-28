package com.xujiayao.placeholder_api_compat.impl;

import com.xujiayao.placeholder_api_compat.api.node.KeybindNode;
import com.xujiayao.placeholder_api_compat.api.node.LiteralNode;
import com.xujiayao.placeholder_api_compat.api.node.NbtNode;
import com.xujiayao.placeholder_api_compat.api.node.ScoreNode;
import com.xujiayao.placeholder_api_compat.api.node.SelectorNode;
import com.xujiayao.placeholder_api_compat.api.node.TextNode;
import com.xujiayao.placeholder_api_compat.api.node.TranslatedNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ColorNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.DynamicShadowNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.FormattingNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.GradientNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.ParentTextNode;
import com.xujiayao.placeholder_api_compat.api.node.parent.StyledNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

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
		return (
				text.getContent() == PlainTextContent.EMPTY
						|| (text.getContent() instanceof PlainTextContent.Literal(String string) && string.isEmpty())
		) && text.getSiblings().isEmpty();
	}

	public static MutableText toGradient(Text base, GradientNode.GradientProvider posToColor) {
		return recursiveGradient(base, posToColor, 0, getGradientLength(base),
				text -> text.getStyle().getColor() == null,
				Style::withColor,
				Text::copy
		).text();
	}

	public static MutableText toGradientShadow(Text base, float scale, float alpha, GradientNode.GradientProvider posToColor) {
		return recursiveGradient(base, posToColor, 0, getGradientLength(base),
				text -> text.getStyle().getShadowColor() == null && text.getStyle().getColor() == null,
				((style, textColor) -> style.withShadowColor(DynamicShadowNode.modifiedColor(textColor.getRgb(), scale, alpha))),
				text2 -> text2.getStyle().getShadowColor() != null ? text2.copy() : GeneralUtils.cloneTransformText(text2, text -> {
					var color = text.getStyle().getColor();
					return text.setStyle(text.getStyle().withShadowColor(DynamicShadowNode.modifiedColor(Objects.requireNonNull(color).getRgb(), scale, alpha)));
				}, text -> text == text2 || text.getStyle().getShadowColor() == null && text.getStyle().getColor() != null)).text();
	}

	private static int getGradientLength(Text base) {
		int length = base.getContent() instanceof PlainTextContent.Literal(String string)
				? string.codePointCount(0, string.length())
				: base.getContent() == PlainTextContent.EMPTY ? 0 : 1;

		for (var text : base.getSiblings()) {
			length += getGradientLength(text);
		}

		return length;
	}

	private static TextLengthPair recursiveGradient(Text base, GradientNode.GradientProvider posToColor, int pos, int totalLength,
	                                                Predicate<Text> canContinue,
	                                                BiFunction<Style, TextColor, Style> apply,
	                                                Function<Text, MutableText> passthroughApply) {
		if (canContinue.test(base)) {
			MutableText out = Text.empty().setStyle(base.getStyle());
			if (base.getContent() instanceof PlainTextContent.Literal(String string)) {
				var l = string.length();
				for (var i = 0; i < l; i++) {
					var character = string.charAt(i);
					int value;
					if (Character.isHighSurrogate(character) && i + 1 < l) {
						var next = string.charAt(++i);
						if (Character.isLowSurrogate(next)) {
							value = Character.toCodePoint(character, next);
						} else {
							value = character;
						}
					} else {
						value = character;
					}

					out.append(Text.literal(Character.toString(value)).setStyle(apply.apply(Style.EMPTY, posToColor.getColorAt(pos++, totalLength))));

				}
			} else if (base.getContent() != PlainTextContent.EMPTY) {
				out.append(base.copyContentOnly().setStyle(apply.apply(Style.EMPTY, posToColor.getColorAt(pos++, totalLength))));
			}

			for (Text sibling : base.getSiblings()) {
				var pair = recursiveGradient(sibling, posToColor, pos, totalLength, canContinue, apply, passthroughApply);
				pos = pair.length;
				out.append(pair.text);
			}
			return new TextLengthPair(out, pos);
		}
		return new TextLengthPair(passthroughApply.apply(base), pos + base.getString().length());
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

		if (input.getContent() instanceof TranslatableTextContent text) {
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
		if (input.getContent() instanceof TranslatableTextContent translatable) {
			var obj = new ArrayList<>();

			for (var arg : translatable.getArgs()) {
				if (arg instanceof Text argText) {
					obj.add(cloneText(argText));
				} else {
					obj.add(arg);
				}
			}

			baseText = Text.translatable(translatable.getKey(), obj.toArray());
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
		return cloneTransformText(input, transform, text -> true);
	}

	public static MutableText cloneTransformText(Text input, Function<MutableText, MutableText> transform, Predicate<Text> canContinue) {
		if (!canContinue.test(input)) {
			return input.copy();
		}

		MutableText baseText;
		if (input.getContent() instanceof TranslatableTextContent translatable) {
			var obj = new ArrayList<>();

			for (var arg : translatable.getArgs()) {
				if (arg instanceof Text argText) {
					obj.add(cloneTransformText(argText, transform));
				} else {
					obj.add(arg);
				}
			}

			baseText = Text.translatable(translatable.getKey(), obj.toArray());
		} else {
			baseText = input.copyContentOnly();
		}

		for (var sibling : input.getSiblings()) {
			baseText.append(cloneTransformText(sibling, transform, canContinue));
		}

		baseText.setStyle(input.getStyle());
		return transform.apply(baseText);
	}

	public static Text getItemText(ItemStack stack, boolean rarity) {
		if (!stack.isEmpty()) {
			MutableText mutableText = Text.empty().append(stack.getName());
			if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
				mutableText.formatted(Formatting.ITALIC);
			}

			if (rarity) {
				mutableText.formatted(stack.getRarity().getFormatting());
			}
			//#if MC >= 12105
			mutableText.styled((style) -> style.withHoverEvent(new HoverEvent.ShowItem(stack)));
			//#else
			//$$ mutableText.styled((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack))));
			//#endif

			return mutableText;
		}

		return Text.empty().append(ItemStack.EMPTY.getName());
	}

	public static ParentNode convertToNodes(Text input) {
		var list = new ArrayList<TextNode>();

		if (input.getContent() instanceof PlainTextContent.Literal(String string)) {
			list.add(new LiteralNode(string));
		} else if (input.getContent() instanceof TranslatableTextContent content) {
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


			list.add(TranslatedNode.ofFallback(content.getKey(), content.getFallback(), args.toArray()));
		} else if (input.getContent() instanceof ScoreTextContent(
				com.mojang.datafixers.util.Either<net.minecraft.text.ParsedSelector, String> name, String objective
		)) {
			list.add(new ScoreNode(name, objective));
		} else if (input.getContent() instanceof KeybindTextContent content) {
			list.add(new KeybindNode(content.getKey()));
		} else if (input.getContent() instanceof SelectorTextContent(
				net.minecraft.text.ParsedSelector selector, java.util.Optional<Text> separator
		)) {
			list.add(new SelectorNode(selector, separator.map(GeneralUtils::convertToNodes)));
		} else if (input.getContent() instanceof NbtTextContent content) {
			list.add(new NbtNode(content.getPath(), content.shouldInterpret(), content.getSeparator().map(GeneralUtils::convertToNodes), content.getDataSource()));
		}


		for (var child : input.getSiblings()) {
			list.add(convertToNodes(child));
		}

		if (input.getStyle() == Style.EMPTY) {
			return new ParentNode(list);
		} else {
			var style = input.getStyle();
			var hoverValue = style.getHoverEvent() != null ? convertToNodes(getHoverValue(style)) : null;
			var clickValue = style.getClickEvent() != null ? (TextNode) getClickValue(style) : null;
			var insertion = style.getInsertion() != null ? new LiteralNode(style.getInsertion()) : null;

			return new StyledNode(list.toArray(new TextNode[0]), style, hoverValue, clickValue, insertion);
		}
	}

	private static Text getHoverValue(Style style) {
		if (style.getHoverEvent() != null) {
			//#if MC >= 12105
			switch (style.getHoverEvent().getAction()) {
				case SHOW_TEXT -> {
					return ((HoverEvent.ShowText) style.getHoverEvent()).value();
				}
                /*
                case SHOW_ITEM -> {
                    return ((HoverEvent.ShowItem) style.getHoverEvent()).item().toHoverableText();
                }
                case SHOW_ENTITY -> {
                    HoverEvent.EntityContent content = ((HoverEvent.ShowEntity) style.getHoverEvent()).entity();
                    Text result = content.name.orElseGet(content.entityType::getName);
                    if (result != null && !result.equals(Text.empty())) {
                        return result;
                    }
                    return Text.literal("id="+ EntityType.getId(content.entityType).toString());
                }
                 */
			}
			//#else
			//$$ if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
			//$$     return style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
			//$$ }
			//#endif
		}

		return Text.literal("Missing Hover Value");
	}

	private static Text getClickValue(Style style) {
		if (style.getClickEvent() != null) {
			//#if MC >= 12105
			switch (style.getClickEvent().getAction()) {
				case CHANGE_PAGE -> {
					return Text.literal(String.valueOf(((ClickEvent.ChangePage) style.getClickEvent()).page()));
				}
				case COPY_TO_CLIPBOARD -> {
					return Text.literal(((ClickEvent.CopyToClipboard) style.getClickEvent()).value());
				}
				case OPEN_FILE -> {
					return Text.literal(((ClickEvent.OpenFile) style.getClickEvent()).file().getAbsolutePath());
				}
				case OPEN_URL -> {
					return Text.literal(((ClickEvent.OpenUrl) style.getClickEvent()).uri().getRawPath());
				}
				case RUN_COMMAND -> {
					return Text.literal(((ClickEvent.RunCommand) style.getClickEvent()).command());
				}
				case SUGGEST_COMMAND -> {
					return Text.literal(((ClickEvent.SuggestCommand) style.getClickEvent()).command());
				}
			}
			//#else
			//$$ return Text.literal(style.getClickEvent().getValue());
			//#endif
		}

		return Text.literal("Missing Click Value");
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
}
