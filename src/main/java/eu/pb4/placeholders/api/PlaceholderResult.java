package eu.pb4.placeholders.api;

import eu.pb4.placeholders.api.parsers.TextParserV1;
//#if MC <= 11802
//$$ import net.minecraft.text.LiteralText;
//#endif
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class PlaceholderResult {
	private final Text text;
	private final boolean valid;
	private String string;

	private static MutableText createText(String s) {
		//#if MC > 11802
		return Text.literal(s);
		//#else
		//$$ return new LiteralText(s);
		//#endif
	}

	private PlaceholderResult(Text text, String reason) {
		if (text != null) {
			this.text = text;
			this.valid = true;
		} else {
			this.text = createText("[" + (reason != null ? reason : "Invalid placeholder!") + "]").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true));
			this.valid = false;
		}
	}

	/**
	 * Create result for invalid placeholder
	 *
	 * @return PlaceholderResult
	 */
	public static PlaceholderResult invalid(String reason) {
		return new PlaceholderResult(null, reason);
	}

	/**
	 * Create result for invalid placeholder
	 *
	 * @return PlaceholderResult
	 */
	public static PlaceholderResult invalid() {
		return new PlaceholderResult(null, null);
	}

	/**
	 * Create result for placeholder with formatting
	 *
	 * @return PlaceholderResult
	 */
	public static PlaceholderResult value(Text text) {
		return new PlaceholderResult(text, null);
	}

	/**
	 * Create result for placeholder
	 *
	 * @return PlaceholderResult
	 */
	public static PlaceholderResult value(String text) {
		return new PlaceholderResult(TextParserV1.DEFAULT.parseText(text, null), null);
	}

	/**
	 * Returns text component from placeholder
	 *
	 * @return Text
	 */
	public Text text() {
		return this.text;
	}

	/**
	 * Returns text component as String (without formatting) from placeholder
	 * It's not recommended for general usage, as it makes it text static/unable to change depending on player's language or other settings
	 * and removes all styling.
	 *
	 * @return String
	 */
	@Deprecated
	public String string() {
		if (this.string == null) {
			this.string = this.text.getString();
		}
		return this.string;
	}

	/**
	 * Checks if placeholder was valid
	 *
	 * @return boolean
	 */
	public boolean isValid() {
		return this.valid;
	}
}

