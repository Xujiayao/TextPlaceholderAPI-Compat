package com.xujiayao.placeholder_api_compat.api.node;

import com.mojang.datafixers.util.Either;
import com.xujiayao.placeholder_api_compat.api.ParserContext;
//#if MC > 12101
import net.minecraft.commands.arguments.selector.SelectorPattern;
//#endif
import net.minecraft.network.chat.Component;

//#if MC > 12101
public record ScoreNode(Either<SelectorPattern, String> name, String objective) implements TextNode {
//#else
//$$ public record ScoreNode(String name, String objective) implements TextNode {
//#endif
	//#if MC > 12101
	public ScoreNode(String name, String objective) {
		this(SelectorPattern.parse(name).result().map(Either::<SelectorPattern, String>left).orElse(Either.right(name)), objective);
	}
	//#endif

	@Override
	public Component toText(ParserContext context, boolean removeBackslashes) {
		//#if MC > 12101
		return this.name.map((selector) -> Component.score(selector, this.objective), (name) -> Component.score(name, this.objective));
		//#else
		//$$ return Component.score(this.name, this.objective);
		//#endif
	}
}
