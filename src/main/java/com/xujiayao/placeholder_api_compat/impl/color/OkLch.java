package com.xujiayao.placeholder_api_compat.impl.color;

import net.minecraft.util.Mth;

// https://bottosson.github.io/posts/oklab/
public record OkLch(float l, float c, float h) {
	public static OkLch fromRgb(int rgb) {
		OkLab lab = OkLab.fromRgb(rgb);
		float c = Mth.sqrt(lab.a() * lab.a() + lab.b() + lab.b());
		float h = (float) Mth.atan2(lab.b(), lab.a());

		return new OkLch(lab.l(), c, h);
	}

	public static int toRgb(float l, float c, float h) {
		return OkLab.toRgb(l, (float) ((double) c * Math.cos(h)), (float) ((double) c * Math.sin(h)));
	}

	public float a() {
		return this.c * Mth.cos(this.h);
	}

	public float b() {
		return this.c * Mth.sin(this.h);
	}

	public int toRgb() {
		return OkLab.toRgb(this.l, this.a(), this.b());
	}
}
