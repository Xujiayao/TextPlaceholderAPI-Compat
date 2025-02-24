package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.impl.GeneralUtils;
import eu.pb4.placeholders.impl.color.HSV;
import eu.pb4.placeholders.impl.color.OkLab;
import eu.pb4.placeholders.impl.color.OkLch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GradientNode extends ParentNode {
	private final GradientProvider gradientProvider;

	public GradientNode(TextNode[] children, GradientProvider gradientBuilder) {
		super(children);
		this.gradientProvider = gradientBuilder;
	}

	public static Component apply(Component text, GradientProvider gradientProvider) {
		return GeneralUtils.toGradient(text, gradientProvider);
	}

	public static GradientNode rainbow(float saturation, float value, float frequency, float offset, int gradientLength, TextNode... nodes) {
		return new GradientNode(nodes, GradientNode.GradientProvider.rainbowHvs(saturation, value, frequency, offset, gradientLength));
	}

	public static GradientNode rainbow(float saturation, float value, float frequency, float offset, TextNode... nodes) {
		return new GradientNode(nodes, GradientNode.GradientProvider.rainbowHvs(saturation, value, frequency, offset));
	}

	public static GradientNode rainbow(float saturation, float value, float frequency, TextNode... nodes) {
		return rainbow(saturation, value, frequency, 0.0F, nodes);
	}

	public static GradientNode rainbow(float saturation, float value, TextNode... nodes) {
		return rainbow(saturation, value, 1.0F, 0.0F, nodes);
	}

	public static GradientNode rainbow(float saturation, TextNode... nodes) {
		return rainbow(saturation, 1.0F, 1.0F, 0.0F, nodes);
	}

	public static GradientNode rainbow(TextNode... nodes) {
		return rainbow(1.0F, 1.0F, 1.0F, 0.0F, nodes);
	}

	public static GradientNode colors(TextColor from, TextColor to, TextNode... nodes) {
		return colors(List.of(from, to), nodes);
	}

	public static GradientNode colors(List<TextColor> colors, TextNode... nodes) {
		return new GradientNode(nodes, GradientNode.GradientProvider.colorsOkLab(colors));
	}

	public static GradientNode colorsHard(TextColor from, TextColor to, TextNode... nodes) {
		return colorsHard(List.of(from, to), nodes);
	}

	public static GradientNode colorsHard(List<TextColor> colors, TextNode... nodes) {
		return new GradientNode(nodes, GradientNode.GradientProvider.colorsHard(colors));
	}

	@Override
	protected Component applyFormatting(MutableComponent out, ParserContext context) {
		return GeneralUtils.toGradient(out, this.gradientProvider);
	}

	@Override
	public ParentTextNode copyWith(TextNode[] children) {
		return new GradientNode(children, this.gradientProvider);
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.gradientProvider);
		return "GradientNode{gradientProvider=" + var10000 + ", children=" + Arrays.toString(this.children) + "}";
	}

	@FunctionalInterface
	public interface GradientProvider {
		static GradientProvider colors(List<TextColor> colors) {
			return colorsOkLab(colors);
		}

		static GradientProvider colorsOkLab(List<TextColor> colors) {
			ArrayList<OkLab> hvs = new ArrayList<>(colors.size());

			for (TextColor color : colors) {
				hvs.add(OkLab.fromRgb(color.getValue()));
			}

			if (hvs.isEmpty()) {
				hvs.add(new OkLab(1.0F, 1.0F, 1.0F));
			} else if (hvs.size() == 1) {
				hvs.add(hvs.getFirst());
			}

			int colorSize = hvs.size();
			return (pos, length) -> {
				float sectionSize = (float) length / (float) (colorSize - 1);
				float progress = (float) pos % sectionSize / sectionSize;
				OkLab colorA = hvs.get(Math.min((int) ((float) pos / sectionSize), colorSize - 1));
				OkLab colorB = hvs.get(Math.min((int) ((float) pos / sectionSize) + 1, colorSize - 1));
				float l = Mth.lerp(progress, colorA.l(), colorB.l());
				float a = Mth.lerp(progress, colorA.a(), colorB.a());
				float b = Mth.lerp(progress, colorA.b(), colorB.b());
				return TextColor.fromRgb(OkLab.toRgb(l, a, b));
			};
		}

		static GradientProvider colorsHvs(List<TextColor> colors) {
			ArrayList<HSV> hvs = new ArrayList<>(colors.size());

			for (TextColor color : colors) {
				hvs.add(HSV.fromRgb(color.getValue()));
			}

			if (hvs.isEmpty()) {
				hvs.add(new HSV(1.0F, 1.0F, 1.0F));
			} else if (hvs.size() == 1) {
				hvs.add(hvs.getFirst());
			}

			int colorSize = hvs.size();
			return (pos, length) -> {
				double step = ((double) colorSize - 1.0) / (double) length;
				float sectionSize = (float) length / (float) (colorSize - 1);
				float progress = (float) pos % sectionSize / sectionSize;
				HSV colorA = hvs.get(Math.min((int) ((float) pos / sectionSize), colorSize - 1));
				HSV colorB = hvs.get(Math.min((int) ((float) pos / sectionSize) + 1, colorSize - 1));
				float sat = colorB.h() - colorA.h();
				float value = sat + (float) ((double) Math.abs(sat) > 0.50001 ? (sat < 0.0F ? 1 : -1) : 0);
				float futureHue = (float) ((double) colorA.h() + (double) value * step * (double) ((float) pos % sectionSize));
				if (futureHue < 0.0F) {
					++futureHue;
				} else if (futureHue > 1.0F) {
					--futureHue;
				}

				sat = Mth.clamp(colorB.s() * progress + colorA.s() * (1.0F - progress), 0.0F, 1.0F);
				value = Mth.clamp(colorB.v() * progress + colorA.v() * (1.0F - progress), 0.0F, 1.0F);
				return TextColor.fromRgb(HSV.toRgb(Mth.clamp(futureHue, 0.0F, 1.0F), sat, value));
			};
		}

		static GradientProvider colorsHard(List<TextColor> colors) {
			int colorSize = colors.size();
			return (pos, length) -> {
				if (length == 0) {
					return colors.getFirst();
				} else {
					float sectionSize = (float) length / (float) colorSize;
					return colors.get(Math.min((int) ((float) pos / sectionSize), colorSize - 1));
				}
			};
		}

		static GradientProvider rainbow(float saturation, float value, float frequency, float offset, int gradientLength) {
			return rainbowHvs(saturation, value, frequency, offset, gradientLength);
		}

		static GradientProvider rainbowHvs(float saturation, float value, float frequency, float offset, int gradientLength) {
			float finalFreqLength = frequency < 0.0F ? -frequency : 0.0F;
			return (pos, length) -> TextColor.fromRgb(HSV.toRgb((((float) pos * frequency + finalFreqLength * (float) length) / (float) (gradientLength + 1) + offset) % 1.0F, saturation, value));
		}

		static GradientProvider rainbowOkLch(float saturation, float value, float frequency, float offset, int gradientLength) {
			float finalFreqLength = frequency < 0.0F ? -frequency : 0.0F;
			return (pos, length) -> TextColor.fromRgb(OkLch.toRgb(value, saturation / 2.0F, (((float) pos * frequency * 6.2831855F + finalFreqLength * (float) length) / (float) (gradientLength + 1) + offset) % 1.0F));
		}

		static GradientProvider rainbow(float saturation, float value, float frequency, float offset) {
			return rainbowHvs(saturation, value, frequency, offset);
		}

		static GradientProvider rainbowHvs(float saturation, float value, float frequency, float offset) {
			float finalFreqLength = frequency < 0.0F ? -frequency : 0.0F;
			return (pos, length) -> TextColor.fromRgb(HSV.toRgb(((float) pos * frequency + finalFreqLength * (float) length) / (float) (length + 1) + offset, saturation, value));
		}

		static GradientProvider rainbowOkLch(float saturation, float value, float frequency, float offset) {
			float finalFreqLength = frequency < 0.0F ? -frequency : 0.0F;
			return (pos, length) -> TextColor.fromRgb(OkLch.toRgb(value, saturation / 2.0F, ((float) pos * frequency * 6.2831855F + finalFreqLength * (float) length) / (float) length + offset));
		}

		TextColor getColorAt(int var1, int var2);
	}
}
