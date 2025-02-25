package com.xujiayao.placeholder_api_compat.impl.color;

import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;

public record HSV(float h, float s, float v) {
	public static HSV fromRgb(int rgb) {
		float b = (float) (rgb % 256) / 255.0F;
		rgb >>= 8;
		float g = (float) (rgb % 256) / 255.0F;
		rgb >>= 8;
		float r = (float) (rgb % 256) / 255.0F;

		float cmax = Math.max(r, Math.max(g, b));
		float cmin = Math.min(r, Math.min(g, b));
		float diff = cmax - cmin;
		float h = -1.0F, s;

		if (cmax == cmin) {
			h = 0.0F;
		} else if (cmax == r) {
			h = (0.1666F * ((g - b) / diff) + 1.0F) % 1.0F;
		} else if (cmax == g) {
			h = (0.1666F * ((b - r) / diff) + 0.333F) % 1.0F;
		} else if (cmax == b) {
			h = (0.1666F * ((r - g) / diff) + 0.666F) % 1.0F;
		}
		if (cmax == 0.0F) {
			s = 0.0F;
		} else {
			s = diff / cmax;
		}

		return new HSV(h, s, cmax);
	}

	public static int toRgb(float hue, float saturation, float value) {
		int h = (int) (hue * 6.0F) % 6;
		float f = hue * 6.0F - (float) h;
		float p = value * (1.0F - saturation);
		float q = value * (1.0F - f * saturation);
		float t = value * (1.0F - (1.0F - f) * saturation);

		return switch (h) {
			case 0 -> GeneralUtils.rgbToInt(value, t, p);
			case 1 -> GeneralUtils.rgbToInt(q, value, p);
			case 2 -> GeneralUtils.rgbToInt(p, value, t);
			case 3 -> GeneralUtils.rgbToInt(p, q, value);
			case 4 -> GeneralUtils.rgbToInt(t, p, value);
			case 5 -> GeneralUtils.rgbToInt(value, p, q);
			default -> 0;
		};
	}

	public int toRgb() {
		return toRgb(this.h, this.s, this.v);
	}
}
