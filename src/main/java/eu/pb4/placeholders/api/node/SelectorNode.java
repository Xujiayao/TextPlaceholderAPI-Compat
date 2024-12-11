package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
//#if MC > 12101
import net.minecraft.text.ParsedSelector;
//#endif
//#if MC <= 11802
//$$ import net.minecraft.text.SelectorText;
//#endif
import net.minecraft.text.Text;

import java.util.Optional;

//#if MC > 12101
public record SelectorNode(ParsedSelector selector, Optional<TextNode> separator) implements TextNode {
//#else
//$$ public record SelectorNode(String pattern, Optional<TextNode> separator) implements TextNode {
//#endif

	@Override
	public Text toText(ParserContext context, boolean removeBackslashes) {
		//#if MC > 12101
		return Text.selector(selector, separator.map(x -> x.toText(context, removeBackslashes)));
		//#elseif MC > 11802
		//$$ return Text.selector(pattern, separator.map(x -> x.toText(context, removeBackslashes)));
		//#elseif MC > 11605
		//$$ return new SelectorText(pattern, separator.map(x -> x.toText(context, removeBackslashes)));
		//#else
		//$$ return new SelectorText(pattern);
		//#endif
	}

	@Override
	public boolean isDynamic() {
		return separator.isPresent() && separator.get().isDynamic();
	}
}
