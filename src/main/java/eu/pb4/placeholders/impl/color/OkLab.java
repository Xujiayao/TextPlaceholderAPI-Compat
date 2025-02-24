package eu.pb4.placeholders.impl.color;

import eu.pb4.placeholders.impl.GeneralUtils;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

// https://bottosson.github.io/posts/oklab/
public record OkLab(float l, float a, float b) {
	public static OkLab fromRgb(int rgb) {
		return fromLinearSRGB((float) ARGB.red(rgb) / 255.0F, (float) ARGB.green(rgb) / 255.0F, (float) ARGB.blue(rgb) / 255.0F);
	}

	static float f(float x) {
		return (double) x >= 0.0031308 ? (float) (1.055 * Math.pow(x, (1.0 / 2.4)) - 0.055) : (float) (12.92 * (double) x);
	}

	static float f_inv(float x) {
		return (double) x >= 0.04045 ? (float) Math.pow(((double) x + 0.055) / 1.055, 2.4) : x / 12.92F;
	}

	private static OkLab fromLinearSRGB(float r, float g, float b) {
		float l = 0.4122214708F * r + 0.5363325363F * g + 0.0514459929F * b;
		float m = 0.2119034982F * r + 0.6806995451F * g + 0.1073969566F * b;
		float s = 0.0883024619F * r + 0.2817188376F * g + 0.6299787005F * b;

		float l_ = (float) Math.cbrt(l);
		float m_ = (float) Math.cbrt(m);
		float s_ = (float) Math.cbrt(s);

		return new OkLab(0.2104542553F * l_ + 0.7936177850F * m_ - 0.0040720468F * s_, 1.9779984951F * l_ - 2.4285922050F * m_ + 0.4505937099F * s_, 0.0259040371F * l_ + 0.7827717662F * m_ - 0.8086757660F * s_);
	}

	public static int toRgb(float cL, float ca, float cb) {
		float l_ = cL + 0.3963377774F * ca + 0.2158037573F * cb;
		float m_ = cL - 0.1055613458F * ca - 0.0638541728F * cb;
		float s_ = cL - 0.0894841775F * ca - 1.2914855480F * cb;

		float l = l_ * l_ * l_;
		float m = m_ * m_ * m_;
		float s = s_ * s_ * s_;

		var r = 4.0767416621F * l - 3.3077115913F * m + 0.2309699292F * s;
		var g = -1.2684380046F * l + 2.6097574011F * m - 0.3413193965F * s;
		var b = -0.0041960863F * l - 0.7034186147F * m + 1.7076147010F * s;

		float max = Math.max(Math.max(Math.max(r, g), b), 1.0F);
		float min = Math.min(Math.min(Math.min(r, g), b), 0.0F);
		float mult = 1.0F;
		return GeneralUtils.rgbToInt(Mth.clamp(r * mult, 0.0F, 1.0F), Mth.clamp(g * mult, 0.0F, 1.0F), Mth.clamp(b * mult, 0.0F, 1.0F));
	}

	public int toRgb() {
		return toRgb(this.l, this.a, this.b);
	}
}
