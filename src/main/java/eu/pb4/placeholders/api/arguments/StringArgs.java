package eu.pb4.placeholders.api.arguments;

import net.minecraft.CharPredicate;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class StringArgs {
	private static final StringArgs EMPTY = new StringArgs("");
	private final List<String> ordered = new ArrayList<>();
	private final Map<String, String> keyed = new HashMap<>();
	private final Map<String, StringArgs> keyedMaps = new HashMap<>();
	private final String input;
	private int currentOrdered = 0;

	private StringArgs(String input) {
		this.input = input;
	}

	public static StringArgs ordered(String input, char separator) {
		StringArgs args = new StringArgs(input);
		args.ordered.addAll(SimpleArguments.split(input, separator));
		return args;
	}

	public static StringArgs keyed(String input, char separator, char map) {
		return keyed(input, separator, map, true, SimpleArguments::isWrapCharacter);
	}

	public static StringArgs keyed(String input, char separator, char map, boolean hasMaps, CharPredicate wrapCharacters) {
		StringArgs args = new StringArgs(input);
		BiConsumer<String, String> var10007 = (key, value) -> {
			if (key != null) {
				args.keyed.put(key, value != null ? SimpleArguments.unwrap(value, wrapCharacters) : "");
			}

		};
		Map<String, StringArgs> var10008 = args.keyedMaps;
		Objects.requireNonNull(var10008);
		keyDecomposition(input, 0, separator, map, wrapCharacters, hasMaps, '\u0000', var10007, var10008::put);
		return args;
	}

	public static StringArgs full(String input, char separator, char map) {
		return full(input, separator, map, true, SimpleArguments::isWrapCharacter);
	}

	public static StringArgs full(String input, char separator, char map, boolean hasMaps, CharPredicate wrapCharacters) {
		StringArgs args = new StringArgs(input);
		BiConsumer<String, String> var10007 = (key, value) -> {
			if (key != null) {
				args.keyed.put(key, value != null ? SimpleArguments.unwrap(value, wrapCharacters) : "");
				if (value == null) {
					args.ordered.add(SimpleArguments.unwrap(key, wrapCharacters));
				}
			}

		};
		Map<String, StringArgs> var10008 = args.keyedMaps;
		Objects.requireNonNull(var10008);
		keyDecomposition(input, 0, separator, map, wrapCharacters, hasMaps, '\u0000', var10007, var10008::put);
		return args;
	}

	private static int keyDecomposition(String input, int offset, char separator, char map, CharPredicate isWrap, boolean hasMaps, char stopAt, BiConsumer<String, String> consumer, BiConsumer<String, StringArgs> mapConsumer) {
		String key = null;
		String value = null;
		StringBuilder b = new StringBuilder();
		char wrap = 0;

		int i;
		for (i = offset; i < input.length(); ++i) {
			char chr = input.charAt(i);
			char chrN = i != input.length() - 1 ? input.charAt(i + 1) : 0;
			if (chr == stopAt && wrap == 0) {
				break;
			}

			if (key != null && b.isEmpty() && hasMaps && (chr == '{' || chr == '[') && wrap == 0) {
				ArrayList<String> ordered = new ArrayList<>();
				HashMap<String, String> keyed = new HashMap<>();
				HashMap<String, StringArgs> keyedMaps = new HashMap<>();
				int var10001 = i + 1;
				int var10006 = chr == '{' ? 125 : 93;
				BiConsumer<String, String> var10007 = (keyx, valuex) -> {
					if (keyx != null) {
						keyed.put(keyx, valuex != null ? SimpleArguments.unwrap(valuex, isWrap) : "");
						if (valuex == null) {
							ordered.add(SimpleArguments.unwrap(keyx, isWrap));
						}
					}

				};
				Objects.requireNonNull(keyedMaps);
				int ti = keyDecomposition(input, var10001, separator, map, isWrap, true, (char) var10006, var10007, keyedMaps::put);
				if (ti == input.length()) {
					b.append(chr);
				} else {
					StringArgs arg = new StringArgs(input.substring(i, ti));
					arg.ordered.addAll(ordered);
					arg.keyed.putAll(keyed);
					arg.keyedMaps.putAll(keyedMaps);
					mapConsumer.accept(key, arg);
					key = null;
					i = ti;
				}
			} else if (chr == map && wrap == 0 && key == null) {
				key = b.toString();
				b = new StringBuilder();
			} else if ((chr != '\\' || chrN == 0) && (chrN == 0 || chr != chrN || !isWrap.test(chr))) {
				if (!isWrap.test(chr) || wrap != 0 && wrap != chr) {
					if (chr == separator && wrap == 0) {
						if (b.isEmpty() && key == null) {
							consumer.accept(null, null);
						} else {
							if (key == null) {
								key = b.toString();
							} else {
								value = b.toString();
							}

							consumer.accept(key, value);
							key = null;
							value = null;
							b = new StringBuilder();
						}
					} else {
						b.append(chr);
					}
				} else {
					wrap = wrap == 0 ? chr : 0;
				}
			} else {
				b.append(chrN);
				++i;
			}
		}

		if (key != null) {
			consumer.accept(key, b.isEmpty() ? null : b.toString());
		} else if (!b.isEmpty()) {
			consumer.accept(b.toString(), null);
		}

		return i;
	}

	public static StringArgs empty() {
		return EMPTY;
	}

	public static StringArgs emptyNew() {
		return new StringArgs("");
	}

	public String input() {
		return this.input;
	}

	public @Nullable String get(String name) {
		return this.keyed.get(name);
	}

	public @Nullable StringArgs getNested(String name) {
		return this.keyedMaps.get(name);
	}

	public StringArgs getNestedOrEmpty(String name) {
		return this.keyedMaps.getOrDefault(name, EMPTY);
	}

	public String get(String name, String defaultValue) {
		return this.keyed.getOrDefault(name, defaultValue);
	}

	public @Nullable String get(String name, int id) {
		String x = this.keyed.get(name);
		if (x != null) {
			return x;
		} else {
			return id < this.ordered.size() ? this.ordered.get(id) : null;
		}
	}

	public String get(String name, int id, String defaultValue) {
		String x = this.get(name, id);
		return x != null ? x : defaultValue;
	}

	public @Nullable String getNext(String name) {
		String x = this.keyed.get(name);
		if (x != null) {
			return x;
		} else {
			return this.currentOrdered < this.ordered.size() ? this.ordered.get(this.currentOrdered++) : null;
		}
	}

	public String getNext(String name, String defaultValue) {
		String x = this.getNext(name);
		return x != null ? x : defaultValue;
	}

	public void ifPresent(String key, Consumer<String> valueConsumer) {
		String val = this.get(key);
		if (val != null) {
			valueConsumer.accept(val);
		}
	}

	public boolean contains(String key) {
		return this.keyed.containsKey(key);
	}

	public boolean isEmpty() {
		return this.keyed.isEmpty() && this.ordered.isEmpty();
	}

	public List<String> ordered() {
		return Collections.unmodifiableList(this.ordered);
	}

	public int size() {
		return Math.max(this.keyed.size(), this.ordered.size());
	}

	@Internal
	public List<String> unsafeOrdered() {
		return this.ordered;
	}

	@Internal
	public Map<String, String> unsafeKeyed() {
		return this.keyed;
	}

	@Internal
	public Map<String, StringArgs> unsafeKeyedMap() {
		return this.keyedMaps;
	}

	@Override
	public String toString() {
		String var10000 = String.valueOf(this.ordered);
		return "StringArgs{ordered=" + var10000 + ", keyed=" + this.keyed + ", keyedMaps=" + this.keyedMaps + "}";
	}
}
