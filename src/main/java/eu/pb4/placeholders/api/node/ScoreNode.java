package eu.pb4.placeholders.api.node;

import com.mojang.datafixers.util.Either;
import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;

public record ScoreNode(Either<SelectorPattern, String> name, String objective) implements TextNode {
	public ScoreNode(String name, String objective) {
		this(SelectorPattern.parse(name).result().map(Either::<SelectorPattern, String>left).orElse(Either.right(name)), objective);
	}

	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		return this.name.map((selector) -> Component.score(selector, this.objective), (name) -> Component.score(name, this.objective));
	}
}
