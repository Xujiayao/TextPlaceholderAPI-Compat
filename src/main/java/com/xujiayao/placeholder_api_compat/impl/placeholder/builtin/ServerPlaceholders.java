package com.xujiayao.placeholder_api_compat.impl.placeholder.builtin;

import com.xujiayao.placeholder_api_compat.api.PlaceholderResult;
import com.xujiayao.placeholder_api_compat.api.Placeholders;
import com.xujiayao.placeholder_api_compat.api.arguments.StringArgs;
import com.xujiayao.placeholder_api_compat.impl.GeneralUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ServerPlaceholders {
	public static void register() {
		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "tps"), (ctx, arg) -> {
			double tps = (float) TimeUnit.SECONDS.toMillis(1L) / Math.max(ctx.server().getCurrentSmoothedTickTime(), ctx.server().tickRateManager().millisecondsPerTick());
			String format = "%.1f";

			if (arg != null) {
				try {
					int x = Integer.parseInt(arg);
					format = "%." + x + "f";
				} catch (Exception var6) {
					format = "%.1f";
				}
			}

			return PlaceholderResult.value(String.format(format, tps));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "tps_colored"), (ctx, arg) -> {
			double tps = (float) TimeUnit.SECONDS.toMillis(1L) / Math.max(ctx.server().getCurrentSmoothedTickTime(), ctx.server().tickRateManager().millisecondsPerTick());
			String format = "%.1f";

			if (arg != null) {
				try {
					int x = Integer.parseInt(arg);
					format = "%." + x + "f";
				} catch (Exception var6) {
					format = "%.1f";
				}
			}
			return PlaceholderResult.value(Component.literal(String.format(format, tps)).withStyle(tps > 19.0 ? ChatFormatting.GREEN : (tps > 16.0 ? ChatFormatting.GOLD : ChatFormatting.RED)));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "mspt"), (ctx, arg) -> PlaceholderResult.value(String.format("%.0f", ctx.server().getCurrentSmoothedTickTime())));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "mspt_colored"), (ctx, arg) -> {
			float x = ctx.server().getCurrentSmoothedTickTime();
			return PlaceholderResult.value(Component.literal(String.format("%.0f", x)).withStyle(x < 45.0F ? ChatFormatting.GREEN : (x < 51.0F ? ChatFormatting.GOLD : ChatFormatting.RED)));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "time"), (ctx, arg) -> {
			SimpleDateFormat format = new SimpleDateFormat(arg != null ? arg : "HH:mm:ss");
			return PlaceholderResult.value(format.format(new Date(System.currentTimeMillis())));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "time_new"), (ctx, arg) -> {
			StringArgs args = arg == null ? StringArgs.empty() : StringArgs.full(arg, ' ', ':');
			DateTimeFormatter format = DateTimeFormatter.ofPattern(args.get("format", "HH:mm:ss"));
			LocalDateTime date = args.get("zone") != null ? LocalDateTime.now(ZoneId.of(args.get("zone", ""))) : LocalDateTime.now();
			return PlaceholderResult.value(format.format(date));
		});

		{
			var ref = new Object() {
				WeakReference<MinecraftServer> server;
				long ms;
			};

			Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "uptime"), (ctx, arg) -> {
				if (ref.server == null || !ref.server.refersTo(ctx.server())) {
					ref.server = new WeakReference<>(ctx.server());
					ref.ms = System.currentTimeMillis() - (long) ctx.server().getTickCount() * 50L;
				}

				return PlaceholderResult.value(arg != null ? DurationFormatUtils.formatDuration(System.currentTimeMillis() - ref.ms, arg, true) : GeneralUtils.durationToString((System.currentTimeMillis() - ref.ms) / 1000L));
			});
		}

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "version"), (ctx, arg) -> PlaceholderResult.value(ctx.server().getServerVersion()));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "motd"), (ctx, arg) -> {
			ServerStatus metadata = ctx.server().getStatus();
			return metadata == null ? PlaceholderResult.invalid("Server metadata missing!") : PlaceholderResult.value(metadata.description());
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "mod_version"), (ctx, arg) -> {
			if (arg != null) {
				Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(arg);

				if (container.isPresent()) {
					return PlaceholderResult.value(Component.literal(container.get().getMetadata().getVersion().getFriendlyString()));
				}
			}
			return PlaceholderResult.invalid("Invalid argument");
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "mod_name"), (ctx, arg) -> {
			if (arg != null) {
				Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(arg);

				if (container.isPresent()) {
					return PlaceholderResult.value(Component.literal(container.get().getMetadata().getName()));
				}
			}
			return PlaceholderResult.invalid("Invalid argument");
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "brand"), (ctx, arg) -> PlaceholderResult.value(Component.literal(ctx.server().getServerModName())));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "mod_count"), (ctx, arg) -> PlaceholderResult.value(Component.literal("" + FabricLoader.getInstance().getAllMods().size())));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "mod_description"), (ctx, arg) -> {
			if (arg != null) {
				Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(arg);

				if (container.isPresent()) {
					return PlaceholderResult.value(Component.literal(container.get().getMetadata().getDescription()));
				}
			}
			return PlaceholderResult.invalid("Invalid argument");
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "name"), (ctx, arg) -> PlaceholderResult.value(ctx.server().name()));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "used_ram"), (ctx, arg) -> {
			MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
			MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

			return PlaceholderResult.value(Objects.equals(arg, "gb") ? String.format("%.1f", (float) heapUsage.getUsed() / 1.0737418E9F) : String.format("%d", heapUsage.getUsed() / 1048576L));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "max_ram"), (ctx, arg) -> {
			MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
			MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

			return PlaceholderResult.value(Objects.equals(arg, "gb") ? String.format("%.1f", (float) heapUsage.getMax() / 1.0737418E9F) : String.format("%d", heapUsage.getMax() / 1048576L));
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "online"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(ctx.server().getPlayerList().getPlayerCount())));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "max_players"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(ctx.server().getPlayerList().getMaxPlayers())));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "objective_name_top"), (ctx, arg) -> {
			String[] args = arg.split(" ");
			if (args.length >= 2) {
				ServerScoreboard scoreboard = ctx.server().getScoreboard();
				Objective scoreboardObjective = scoreboard.getObjective(args[0]);
				if (scoreboardObjective == null) {
					return PlaceholderResult.invalid("Invalid objective!");
				} else {
					try {
						int position = Integer.parseInt(args[1]);
						Collection<PlayerScoreEntry> scoreboardEntries = scoreboard.listPlayerScores(scoreboardObjective);
						PlayerScoreEntry scoreboardEntry = ((PlayerScoreEntry[]) scoreboardEntries.toArray(PlayerScoreEntry[]::new))[scoreboardEntries.size() - position];
						return PlaceholderResult.value(scoreboardEntry.ownerName());
					} catch (Exception var8) {
						return PlaceholderResult.invalid("Invalid position!");
					}
				}
			} else {
				return PlaceholderResult.invalid("Not enough arguments!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("server", "objective_score_top"), (ctx, arg) -> {
			String[] args = arg.split(" ");
			if (args.length >= 2) {
				ServerScoreboard scoreboard = ctx.server().getScoreboard();
				Objective scoreboardObjective = scoreboard.getObjective(args[0]);
				if (scoreboardObjective == null) {
					return PlaceholderResult.invalid("Invalid objective!");
				} else {
					try {
						int position = Integer.parseInt(args[1]);
						Collection<PlayerScoreEntry> scoreboardEntries = scoreboard.listPlayerScores(scoreboardObjective);
						PlayerScoreEntry scoreboardEntry = ((PlayerScoreEntry[]) scoreboardEntries.toArray(PlayerScoreEntry[]::new))[scoreboardEntries.size() - position];
						return PlaceholderResult.value(String.valueOf(scoreboardEntry.value()));
					} catch (Exception var8) {
						return PlaceholderResult.invalid("Invalid position!");
					}
				}
			} else {
				return PlaceholderResult.invalid("Not enough arguments!");
			}
		});
	}
}
