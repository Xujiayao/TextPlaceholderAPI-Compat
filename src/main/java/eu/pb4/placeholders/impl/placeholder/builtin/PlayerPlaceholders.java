package eu.pb4.placeholders.impl.placeholder.builtin;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.impl.GeneralUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Locale;

public class PlayerPlaceholders {
	public static void register() {
		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "name"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getName());
			} else {
				return ctx.hasGameProfile() ? PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName())) : PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "name_visual"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(GeneralUtils.removeHoverAndClick(ctx.player().getName()));
			} else {
				return ctx.hasGameProfile() ? PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName())) : PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "name_unformatted"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getName().getString());
			} else {
				return ctx.hasGameProfile() ? PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName())) : PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "ping"), (ctx, arg) -> ctx.hasPlayer() ? PlaceholderResult.value(String.valueOf(ctx.player().connection.latency())) : PlaceholderResult.invalid("No player!"));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "ping_colored"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				int x = ctx.player().connection.latency();
				return PlaceholderResult.value(Component.literal(String.valueOf(x)).withStyle(x < 100 ? ChatFormatting.GREEN : (x < 200 ? ChatFormatting.GOLD : ChatFormatting.RED)));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "displayname"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getDisplayName());
			} else {
				return ctx.hasGameProfile() ? PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName())) : PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "display_name"), Placeholders.getPlaceholders().get(ResourceLocation.fromNamespaceAndPath("player", "displayname")));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "displayname_visual"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(GeneralUtils.removeHoverAndClick(ctx.player().getDisplayName()));
			} else {
				return ctx.hasGameProfile() ? PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName())) : PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "display_name_visual"), Placeholders.getPlaceholders().get(ResourceLocation.fromNamespaceAndPath("player", "displayname_visual")));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "displayname_unformatted"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(Component.literal(ctx.player().getDisplayName().getString()));
			} else {
				return ctx.hasGameProfile() ? PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName())) : PlaceholderResult.invalid("No player!");
			}
		});
		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "display_name_unformatted"), Placeholders.getPlaceholders().get(ResourceLocation.fromNamespaceAndPath("player", "displayname_unformatted")));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "inventory_slot"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					int slot = Integer.parseInt(arg);
					Inventory inventory = ctx.player().getInventory();
					if (slot >= 0 && slot < inventory.getContainerSize()) {
						ItemStack stack = inventory.getItem(slot);
						return PlaceholderResult.value(GeneralUtils.getItemText(stack, true));
					}
				} catch (Exception ignored) {
				}
				return PlaceholderResult.invalid("Invalid argument");
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "inventory_slot_no_rarity"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					int slot = Integer.parseInt(arg);
					Inventory inventory = ctx.player().getInventory();
					if (slot >= 0 && slot < inventory.getContainerSize()) {
						ItemStack stack = inventory.getItem(slot);
						return PlaceholderResult.value(GeneralUtils.getItemText(stack, false));
					}
				} catch (Exception ignored) {
				}
				return PlaceholderResult.invalid("Invalid argument");
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "equipment_slot"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					EquipmentSlot slot = EquipmentSlot.byName(arg);
					ItemStack stack = ctx.player().getItemBySlot(slot);
					return PlaceholderResult.value(GeneralUtils.getItemText(stack, true));
				} catch (Exception var4) {
					return PlaceholderResult.invalid("Invalid argument");
				}
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "equipment_slot_no_rarity"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					EquipmentSlot slot = EquipmentSlot.byName(arg);
					ItemStack stack = ctx.player().getItemBySlot(slot);
					return PlaceholderResult.value(GeneralUtils.getItemText(stack, false));
				} catch (Exception var4) {
					return PlaceholderResult.invalid("Invalid argument");
				}
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "playtime"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				int x = ctx.player().getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
				return PlaceholderResult.value(arg != null ? DurationFormatUtils.formatDuration((long) x * 50L, arg, true) : GeneralUtils.durationToString((long) x / 20L));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "statistic"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					String[] args = arg.split(" ");
					ResourceLocation type;
					if (args.length == 1) {
						type = ResourceLocation.tryParse(args[0]);
						if (type != null) {
							Stat<ResourceLocation> statx = Stats.CUSTOM.get(BuiltInRegistries.CUSTOM_STAT.getValue(type));
							int xx = ctx.player().getStats().getValue(statx);
							return PlaceholderResult.value(statx.format(xx));
						}
					} else if (args.length >= 2) {
						type = ResourceLocation.tryParse(args[0]);
						ResourceLocation id = ResourceLocation.tryParse(args[1]);
						if (type != null) {
							StatType statType = BuiltInRegistries.STAT_TYPE.getValue(type);
							if (statType != null) {
								Object key = statType.getRegistry().getValue(id);
								if (key != null) {
									Stat stat = statType.get(key);
									int x = ctx.player().getStats().getValue(stat);
									return PlaceholderResult.value(stat.format(x));
								}
							}
						}
					}
				} catch (Exception ignored) {
				}
				return PlaceholderResult.invalid("Invalid statistic!");
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "statistic_raw"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					String[] args = arg.split(" ");
					ResourceLocation type;
					if (args.length == 1) {
						type = ResourceLocation.tryParse(args[0]);
						if (type != null) {
							Stat<ResourceLocation> statx = Stats.CUSTOM.get(BuiltInRegistries.CUSTOM_STAT.getValue(type));
							int xx = ctx.player().getStats().getValue(statx);
							return PlaceholderResult.value(String.valueOf(xx));
						}
					} else if (args.length >= 2) {
						type = ResourceLocation.tryParse(args[0]);
						ResourceLocation id = ResourceLocation.tryParse(args[1]);
						if (type != null) {
							StatType statType = BuiltInRegistries.STAT_TYPE.getValue(type);
							if (statType != null) {
								Object key = statType.getRegistry().getValue(id);
								if (key != null) {
									Stat stat = statType.get(key);
									int x = ctx.player().getStats().getValue(stat);
									return PlaceholderResult.value(String.valueOf(x));
								}
							}
						}
					}
				} catch (Exception ignored) {
				}
				return PlaceholderResult.invalid("Invalid statistic!");
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "objective"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					ServerScoreboard scoreboard = ctx.server().getScoreboard();
					Objective scoreboardObjective = scoreboard.getObjective(arg);
					if (scoreboardObjective == null) {
						return PlaceholderResult.invalid("Invalid objective!");
					} else {
						ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(ctx.player(), scoreboardObjective);
						return PlaceholderResult.value(String.valueOf(score.value()));
					}
				} catch (Exception var5) {
					return PlaceholderResult.invalid("Invalid objective!");
				}
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "facing"), (ctx, arg) -> ctx.hasPlayer() ? PlaceholderResult.value(ctx.player().getNearestViewDirection().getSerializedName()) : PlaceholderResult.invalid("No player!"));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "facing_axis"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				Direction facing = ctx.player().getNearestViewDirection();
				return PlaceholderResult.value((facing.getAxisDirection() == AxisDirection.NEGATIVE ? "-" : "+") + facing.getAxis().getSerializedName().toUpperCase(Locale.ROOT));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "horizontal_facing"), (ctx, arg) -> ctx.hasPlayer() ? PlaceholderResult.value(ctx.player().getDirection().getSerializedName()) : PlaceholderResult.invalid("No player!"));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "horizontal_facing_axis"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				Direction facing = ctx.player().getDirection();
				return PlaceholderResult.value((facing.getAxisDirection() == AxisDirection.NEGATIVE ? "-" : "+") + facing.getAxis().getSerializedName().toUpperCase(Locale.ROOT));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "pos_x"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				double value = ctx.player().getX();
				String format = "%.2f";

				if (arg != null) {
					try {
						int x = Integer.parseInt(arg);
						format = "%." + x + "f";
					} catch (Exception var6) {
						format = "%.2f";
					}
				}

				return PlaceholderResult.value(String.format(format, value));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "pos_y"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				double value = ctx.player().getY();
				String format = "%.2f";

				if (arg != null) {
					try {
						int x = Integer.parseInt(arg);
						format = "%." + x + "f";
					} catch (Exception var6) {
						format = "%.2f";
					}
				}

				return PlaceholderResult.value(String.format(format, value));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "pos_z"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				double value = ctx.player().getZ();
				String format = "%.2f";

				if (arg != null) {
					try {
						int x = Integer.parseInt(arg);
						format = "%." + x + "f";
					} catch (Exception var6) {
						format = "%.2f";
					}
				}

				return PlaceholderResult.value(String.format(format, value));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "uuid"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getStringUUID());
			} else {
				return ctx.hasGameProfile() ? PlaceholderResult.value(Component.nullToEmpty(String.valueOf(ctx.gameProfile().getId()))) : PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "health"), (ctx, arg) -> ctx.hasPlayer() ? PlaceholderResult.value(String.format("%.0f", ctx.player().getHealth())) : PlaceholderResult.invalid("No player!"));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "max_health"), (ctx, arg) -> ctx.hasPlayer() ? PlaceholderResult.value(String.format("%.0f", ctx.player().getMaxHealth())) : PlaceholderResult.invalid("No player!"));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "hunger"), (ctx, arg) -> ctx.hasPlayer() ? PlaceholderResult.value(String.valueOf(ctx.player().getFoodData().getFoodLevel())) : PlaceholderResult.invalid("No player!"));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "saturation"), (ctx, arg) -> ctx.hasPlayer() ? PlaceholderResult.value(String.format("%.0f", ctx.player().getFoodData().getSaturationLevel())) : PlaceholderResult.invalid("No player!"));

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "team_name"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				PlayerTeam team = ctx.player().getTeam();
				return PlaceholderResult.value(team == null ? Component.empty() : Component.nullToEmpty(team.getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "team_displayname"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				PlayerTeam team = ctx.player().getTeam();
				return PlaceholderResult.value(team == null ? Component.empty() : team.getDisplayName());
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "team_displayname_formatted"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				PlayerTeam team = ctx.player().getTeam();
				return PlaceholderResult.value(team == null ? Component.empty() : team.getFormattedDisplayName());
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});
	}
}
