package eu.pb4.placeholders.impl.placeholder.builtin;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldPlaceholders {
	static final int CHUNK_AREA = (int) Math.pow(17.0, 2.0);

	public static void register() {
		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "time"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			long dayTime = (long) ((double) world.getDayTime() * 3.6 / 60.0);

			return PlaceholderResult.value(String.format("%02d:%02d", (dayTime / 60L + 6L) % 24L, dayTime % 60L));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "time_alt"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			long dayTime = (long) ((double) world.getDayTime() * 3.6 / 60.0);
			long x = (dayTime / 60L + 6L) % 24L;
			long y = x % 12L;
			if (y == 0L) {
				y = 12L;
			}
			return PlaceholderResult.value(String.format("%02d:%02d %s", y, dayTime % 60L, x > 11L ? "PM" : "AM"));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "day"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			return PlaceholderResult.value("" + world.getDayTime() / 24000L);
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "id"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			return PlaceholderResult.value(world.dimension().location().toString());
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "name"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}
			List<String> parts = new ArrayList<>();
			{
				String[] words = world.dimension().location().getPath().split("_");
				for (String word : words) {
					String[] s = word.split("", 2);
					s[0] = s[0].toUpperCase(Locale.ROOT);
					parts.add(String.join("", s));
				}
			}
			return PlaceholderResult.value(String.join(" ", parts));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "player_count"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			return PlaceholderResult.value("" + world.players().size());
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "mob_count_colored"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			NaturalSpawner.SpawnState info = world.getChunkSource().getLastSpawnState();

			MobCategory spawnGroup = null;
			if (arg != null) {
				spawnGroup = MobCategory.valueOf(arg.toUpperCase(Locale.ROOT));
			}

			int cap;
			int count;
			if (spawnGroup != null) {
				cap = info.getMobCategoryCounts().getInt(spawnGroup);
				count = spawnGroup.getMaxInstancesPerChunk() * info.getSpawnableChunkCount() / CHUNK_AREA;

				return PlaceholderResult.value(cap > 0 ? Component.literal("" + cap).withStyle(cap > count ? ChatFormatting.LIGHT_PURPLE : ((double) cap > 0.8 * (double) count ? ChatFormatting.RED : ((double) cap > 0.5 * (double) count ? ChatFormatting.GOLD : ChatFormatting.GREEN))) : Component.literal("-").withStyle(ChatFormatting.GRAY));
			} else {
				cap = 0;

				for (MobCategory group : MobCategory.values()) {
					cap += group.getMaxInstancesPerChunk();
				}
				cap = cap * info.getSpawnableChunkCount() / CHUNK_AREA;

				count = 0;

				for (int value : info.getMobCategoryCounts().values()) {
					count += value;
				}
				return PlaceholderResult.value(count > 0 ? Component.literal("" + count).withStyle(count > cap ? ChatFormatting.LIGHT_PURPLE : ((double) count > 0.8 * (double) cap ? ChatFormatting.RED : ((double) count > 0.5 * (double) cap ? ChatFormatting.GOLD : ChatFormatting.GREEN))) : Component.literal("-").withStyle(ChatFormatting.GRAY));
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "mob_count"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			NaturalSpawner.SpawnState info = world.getChunkSource().getLastSpawnState();

			MobCategory spawnGroup = null;
			if (arg != null) {
				spawnGroup = MobCategory.valueOf(arg.toUpperCase(Locale.ROOT));
			}

			if (spawnGroup != null) {
				return PlaceholderResult.value("" + info.getMobCategoryCounts().getInt(spawnGroup));
			} else {
				int x = 0;

				for (int value : info.getMobCategoryCounts().values()) {
					x += value;
				}
				return PlaceholderResult.value("" + x);
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "mob_cap"), (ctx, arg) -> {
			ServerLevel world;
			if (ctx.player() != null) {
				world = ctx.player().serverLevel();
			} else {
				world = ctx.server().overworld();
			}

			NaturalSpawner.SpawnState info = world.getChunkSource().getLastSpawnState();

			MobCategory spawnGroup = null;
			if (arg != null) {
				spawnGroup = MobCategory.valueOf(arg.toUpperCase(Locale.ROOT));
			}

			if (spawnGroup != null) {
				return PlaceholderResult.value("" + spawnGroup.getMaxInstancesPerChunk() * info.getSpawnableChunkCount() / CHUNK_AREA);
			} else {
				int x = 0;

				for (MobCategory group : MobCategory.values()) {
					x += group.getMaxInstancesPerChunk();
				}
				return PlaceholderResult.value("" + x * info.getSpawnableChunkCount() / CHUNK_AREA);
			}
		});
	}
}
