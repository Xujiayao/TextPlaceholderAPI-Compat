package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
//#if MC <= 11802
//$$ import net.minecraft.text.KeybindText;
//#endif
import net.minecraft.text.Text;

public record KeybindNode(String value) implements TextNode {
	@Override
	public Text toText(ParserContext context, boolean removeBackslashes) {
		//#if MC > 11802
		return Text.keybind(this.value());
		//#else
		//$$ return new KeybindText(this.value());
		//#endif

	}
}
