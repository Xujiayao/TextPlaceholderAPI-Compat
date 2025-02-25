package com.xujiayao.placeholder_api_compat.api;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ParserContext {
	private final Map<Key<?>, Object> map = new HashMap<>();

	private ParserContext() {
	}

	public static ParserContext of() {
		return new ParserContext();
	}

	public static <T> ParserContext of(Key<T> key, T object) {
		return (new ParserContext()).with(key, object);
	}

	public <T> ParserContext with(Key<T> key, T object) {
		this.map.put(key, object);
		return this;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T get(Key<T> key) {
		return (T) this.map.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getOrThrow(Key<T> key) {
		return Objects.requireNonNull((T) this.map.get(key));
	}

	public boolean contains(Key<?> key) {
		return this.map.containsKey(key);
	}

	public record Key<T>(String key, @Nullable Class<T> type) {
		public static final Key<Boolean> COMPACT_TEXT = new Key<>("compact_text", Boolean.class);
		public static final Key<HolderLookup.Provider> WRAPPER_LOOKUP = new Key<>("wrapper_lookup", HolderLookup.Provider.class);

		@SuppressWarnings("unchecked")
		public static <T> Key<T> of(String key, T type) {
			return new Key<>(key, (Class<T>) type.getClass());
		}

		public static <T> Key<T> of(String key) {
			return new Key<>(key, null);
		}
	}
}
