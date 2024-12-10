package eu.pb4.placeholders.api;

import net.minecraft.registry.RegistryWrapper;
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
		return new ParserContext().with(key, object);
	}

	public <T> ParserContext with(Key<T> key, T object) {
		this.map.put(key, object);
		return this;
	}

	@Nullable
	public <T> T get(Key<T> key) {
		//noinspection unchecked
		return (T) this.map.get(key);
	}

	public <T> T getOrThrow(Key<T> key) {
		//noinspection unchecked
		return Objects.requireNonNull((T) this.map.get(key));
	}

	public boolean contains(Key<?> key) {
		return this.map.containsKey(key);
	}

	public record Key<T>(String key, @Nullable Class<T> type) {
		public static final Key<Boolean> COMPACT_TEXT = new Key<>("compact_text", Boolean.class);
		//#if MC > 11902
		public static final Key<RegistryWrapper.WrapperLookup> WRAPPER_LOOKUP = new Key<>("wrapper_lookup", RegistryWrapper.WrapperLookup.class);
		//#else
		//$$ // TODO 1.19.2 RegistryWrapper.WrapperLookup
		//$$ public static final Key<Object> WRAPPER_LOOKUP = new Key<>("wrapper_lookup", Object.class);
		//#endif

		public static <T> Key<T> of(String key, T type) {
			//noinspection unchecked
			return new Key<T>(key, (Class<T>) type.getClass());
		}

		public static <T> Key<T> of(String key) {
			//noinspection unchecked
			return new Key<T>(key, null);
		}
	}
}
