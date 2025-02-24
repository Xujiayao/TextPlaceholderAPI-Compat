package eu.pb4.placeholders.impl.textparser.tagreg;

import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import eu.pb4.placeholders.api.parsers.tag.TextTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record WrappingTagRegistry(TagRegistry source, TagRegistry mutable,
                                  Set<TextTag> removed) implements TagRegistry {
	public static WrappingTagRegistry of(TagRegistry source) {
		return new WrappingTagRegistry(source, TagRegistry.create(), new HashSet<>());
	}

	@Override
	public void register(TextTag tag) {
		this.mutable.register(tag);
	}

	@Override
	public void remove(TextTag tag) {
		this.mutable.remove(tag);
		this.removed.add(tag);
	}

	@Override
	public TagRegistry copy() {
		return new WrappingTagRegistry(this.source, this.mutable.copy(), new HashSet<>(this.removed));
	}

	@Override
	public @Nullable TextTag getTag(String name) {
		TextTag a = this.mutable.getTag(name);
		if (a != null) {
			return a;
		} else {
			TextTag x = this.source.getTag(name);
			return x != null && !this.removed.contains(x) ? x : null;
		}
	}

	@Override
	public List<TextTag> getTags() {
		ArrayList<TextTag> list = new ArrayList<>(this.source.getTags());
		list.removeAll(this.removed);
		list.addAll(this.mutable.getTags());
		return list;
	}

	@Override
	public boolean isGlobal() {
		return false;
	}
}
