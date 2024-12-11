package eu.pb4.placeholders.api.node;

import com.mojang.datafixers.util.Either;
import eu.pb4.placeholders.api.ParserContext;
//#if MC > 12101
import net.minecraft.text.ParsedSelector;
//#endif
//#if MC <= 11802
//$$ import net.minecraft.text.ScoreText;
//#endif
import net.minecraft.text.Text;

//#if MC > 12101
public record ScoreNode(Either<ParsedSelector, String> name, String objective) implements TextNode {
//#else
//$$ public record ScoreNode(String name, String objective) implements TextNode {
//#endif

	//#if MC > 12101
	public ScoreNode(String name, String objective) {
		this(ParsedSelector.parse(name).result().map(Either::<ParsedSelector, String>left).orElse(Either.right(name)), objective);
	}
	//#endif

	@Override
	public Text toText(ParserContext context, boolean removeBackslashes) {
		//#if MC > 12101
		return name.map(selector -> Text.score(selector, objective), name -> Text.score(name, objective));
		//#elseif MC > 11802
		//$$ return Text.score(name, objective);
		//#else
		//$$ return new ScoreText(name, objective);
		//#endif
	}
}
