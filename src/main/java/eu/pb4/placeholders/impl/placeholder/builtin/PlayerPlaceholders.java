package eu.pb4.placeholders.impl.placeholder.builtin;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.impl.GeneralUtils;
import net.minecraft.entity.EquipmentSlot;
//#if MC > 11902
import net.minecraft.registry.Registries;
//#else
//$$ import net.minecraft.util.registry.Registry;
//#endif
//#if MC > 12002
import net.minecraft.scoreboard.ReadableScoreboardScore;
//#else
//$$ import net.minecraft.scoreboard.ScoreboardPlayerScore;
//#endif
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
//#if MC <= 12002
//$$ import net.minecraft.scoreboard.Team;
//#endif
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
//#if MC <= 11802
//$$ import net.minecraft.text.LiteralText;
//#endif
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Locale;

public class PlayerPlaceholders {
	private static Identifier createIdentifier(String s1, String s2) {
		//#if MC > 11802
		return Identifier.of(s1, s2);
		//#else
		//$$ return new Identifier(s1, s2);
		//#endif
	}

	private static MutableText createText(String s) {
		//#if MC > 11802
		return Text.literal(s);
		//#else
		//$$ return new LiteralText(s);
		//#endif
	}

	public static void register() {
		Placeholders.register(createIdentifier("player", "name"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getName());
			} else if (ctx.hasGameProfile()) {
				return PlaceholderResult.value(Text.of(ctx.gameProfile().getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "name_visual"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(GeneralUtils.removeHoverAndClick(ctx.player().getName()));
			} else if (ctx.hasGameProfile()) {
				return PlaceholderResult.value(Text.of(ctx.gameProfile().getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "name_unformatted"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getName().getString());
			} else if (ctx.hasGameProfile()) {
				return PlaceholderResult.value(Text.of(ctx.gameProfile().getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "ping"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				//#if MC > 12001
				return PlaceholderResult.value(String.valueOf(ctx.player().networkHandler.getLatency()));
				//#else
				//$$ return PlaceholderResult.value(String.valueOf(ctx.player().pingMilliseconds));
				//#endif
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "ping_colored"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				//#if MC > 12001
				int x = ctx.player().networkHandler.getLatency();
				//#else
				//$$ int x = ctx.player().pingMilliseconds;
				//#endif
				return PlaceholderResult.value(createText(String.valueOf(x)).formatted(x < 100 ? Formatting.GREEN : x < 200 ? Formatting.GOLD : Formatting.RED));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "displayname"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getDisplayName());
			} else if (ctx.hasGameProfile()) {
				return PlaceholderResult.value(Text.of(ctx.gameProfile().getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "display_name"), Placeholders.getPlaceholders().get(createIdentifier("player", "displayname")));

		Placeholders.register(createIdentifier("player", "displayname_visual"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(GeneralUtils.removeHoverAndClick(ctx.player().getDisplayName()));
			} else if (ctx.hasGameProfile()) {
				return PlaceholderResult.value(Text.of(ctx.gameProfile().getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "display_name_visual"), Placeholders.getPlaceholders().get(createIdentifier("player", "displayname_visual")));

		Placeholders.register(createIdentifier("player", "displayname_unformatted"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(createText(ctx.player().getDisplayName().getString()));
			} else if (ctx.hasGameProfile()) {
				return PlaceholderResult.value(Text.of(ctx.gameProfile().getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});
		Placeholders.register(createIdentifier("player", "display_name_unformatted"), Placeholders.getPlaceholders().get(createIdentifier("player", "displayname_unformatted")));

		Placeholders.register(createIdentifier("player", "inventory_slot"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					int slot = Integer.parseInt(arg);

					var inventory = ctx.player().getInventory();

					if (slot >= 0 && slot < inventory.size()) {
						var stack = inventory.getStack(slot);

						return PlaceholderResult.value(GeneralUtils.getItemText(stack, true));
					}

				} catch (Exception e) {
					// noop
				}
				return PlaceholderResult.invalid("Invalid argument");
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(createIdentifier("player", "inventory_slot_no_rarity"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					int slot = Integer.parseInt(arg);

					var inventory = ctx.player().getInventory();

					if (slot >= 0 && slot < inventory.size()) {
						var stack = inventory.getStack(slot);

						return PlaceholderResult.value(GeneralUtils.getItemText(stack, false));
					}

				} catch (Exception e) {
					// noop
				}
				return PlaceholderResult.invalid("Invalid argument");
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(createIdentifier("player", "equipment_slot"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					var slot = EquipmentSlot.byName(arg);

					var stack = ctx.player().getEquippedStack(slot);
					return PlaceholderResult.value(GeneralUtils.getItemText(stack, true));
				} catch (Exception e) {
					// noop
				}
				return PlaceholderResult.invalid("Invalid argument");
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(createIdentifier("player", "equipment_slot_no_rarity"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					var slot = EquipmentSlot.byName(arg);

					var stack = ctx.player().getEquippedStack(slot);
					return PlaceholderResult.value(GeneralUtils.getItemText(stack, false));
				} catch (Exception e) {
					// noop
				}
				return PlaceholderResult.invalid("Invalid argument");
			} else {
				return PlaceholderResult.invalid("No player or invalid argument!");
			}
		});

		Placeholders.register(createIdentifier("player", "playtime"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				int x = ctx.player().getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
				return PlaceholderResult.value(arg != null ? DurationFormatUtils.formatDuration((long) x * 50, arg, true) : GeneralUtils.durationToString((long) x / 20));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "statistic"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					//#if MC > 11902
					var args = arg.split(" ");

					if (args.length == 1) {
						var identifier = Identifier.tryParse(args[0]);
						if (identifier != null) {
							var stat = Stats.CUSTOM.getOrCreateStat(Registries.CUSTOM_STAT.get(identifier));
							int x = ctx.player().getStatHandler().getStat(stat);
							return PlaceholderResult.value(stat.format(x));
						}
					} else if (args.length >= 2) {
						var type = Identifier.tryParse(args[0]);
						var id = Identifier.tryParse(args[1]);
						if (type != null) {
							var statType = (StatType<Object>) Registries.STAT_TYPE.get(type);

							if (statType != null) {
								var key = statType.getRegistry().get(id);
								if (key != null) {
									var stat = statType.getOrCreateStat(key);
									int x = ctx.player().getStatHandler().getStat(stat);
									return PlaceholderResult.value(stat.format(x));
								}
							}
						}
					}
					//#else
					//$$ Identifier identifier = Identifier.tryParse(arg);
					//$$ if (identifier != null) {
					//$$    var stat = Stats.CUSTOM.getOrCreateStat(Registry.CUSTOM_STAT.get(identifier));
					//$$    int x = ctx.player().getStatHandler().getStat(stat);
					//$$    return PlaceholderResult.value(stat.format(x));
					//$$ }
					//#endif
				} catch (Exception e) {
					/* Into the void you go! */
				}
				return PlaceholderResult.invalid("Invalid statistic!");
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "statistic_raw"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					//#if MC > 11902
					var args = arg.split(" ");

					if (args.length == 1) {
						var identifier = Identifier.tryParse(args[0]);
						if (identifier != null) {
							var stat = Stats.CUSTOM.getOrCreateStat(Registries.CUSTOM_STAT.get(identifier));
							int x = ctx.player().getStatHandler().getStat(stat);
							return PlaceholderResult.value(String.valueOf(x));
						}
					} else if (args.length >= 2) {
						var type = Identifier.tryParse(args[0]);
						var id = Identifier.tryParse(args[1]);
						if (type != null) {
							var statType = (StatType<Object>) Registries.STAT_TYPE.get(type);

							if (statType != null) {
								var key = statType.getRegistry().get(id);
								if (key != null) {
									var stat = statType.getOrCreateStat(key);
									int x = ctx.player().getStatHandler().getStat(stat);
									return PlaceholderResult.value(String.valueOf(x));
								}
							}
						}
					}
					//#else
					//$$ Identifier identifier = Identifier.tryParse(arg);
					//$$ if (identifier != null) {
					//$$    var stat = Stats.CUSTOM.getOrCreateStat(Registry.CUSTOM_STAT.get(identifier));
					//$$    int x = ctx.player().getStatHandler().getStat(stat);
					//$$    return PlaceholderResult.value(String.valueOf(x));
					//$$ }
					//#endif
				} catch (Exception e) {
					/* Into the void you go! */
				}
				return PlaceholderResult.invalid("Invalid statistic!");
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "objective"), (ctx, arg) -> {
			if (ctx.hasPlayer() && arg != null) {
				try {
					ServerScoreboard scoreboard = ctx.server().getScoreboard();
					ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(arg);
					if (scoreboardObjective == null) {
						return PlaceholderResult.invalid("Invalid objective!");
					}
					//#if MC > 12002
					ReadableScoreboardScore score = scoreboard.getScore(ctx.player(), scoreboardObjective);
					//#else
					//$$ ScoreboardPlayerScore score = scoreboard.getPlayerScore(ctx.player().getEntityName(), scoreboardObjective);
 					//#endif
					return PlaceholderResult.value(String.valueOf(score.getScore()));
				} catch (Exception e) {
					/* Into the void you go! */
				}
				return PlaceholderResult.invalid("Invalid objective!");
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "facing"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				//#if MC > 12004
				return PlaceholderResult.value(ctx.player().getFacing().asString());
				//#else
				//$$ return PlaceholderResult.invalid("TODO: Not implemented in 1.20.4 and above!");
				//#endif
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "facing_axis"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				//#if MC > 12004
				var facing = ctx.player().getFacing();
				return PlaceholderResult.value((facing.getDirection() == Direction.AxisDirection.NEGATIVE ? "-" : "+") + facing.getAxis().asString().toUpperCase(Locale.ROOT));
				//#else
				//$$ return PlaceholderResult.invalid("TODO: Not implemented in 1.20.4 and above!");
				//#endif
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "horizontal_facing"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getHorizontalFacing().asString());
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "horizontal_facing_axis"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				var facing = ctx.player().getHorizontalFacing();
				return PlaceholderResult.value((facing.getDirection() == Direction.AxisDirection.NEGATIVE ? "-" : "+") + facing.getAxis().asString().toUpperCase(Locale.ROOT));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "pos_x"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				double value = ctx.player().getX();
				String format = "%.2f";

				if (arg != null) {
					try {
						int x = Integer.parseInt(arg);
						format = "%." + x + "f";
					} catch (Exception e) {
						format = "%.2f";
					}
				}

				return PlaceholderResult.value(String.format(format, value));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "pos_y"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				double value = ctx.player().getY();
				String format = "%.2f";

				if (arg != null) {
					try {
						int x = Integer.parseInt(arg);
						format = "%." + x + "f";
					} catch (Exception e) {
						format = "%.2f";
					}
				}

				return PlaceholderResult.value(String.format(format, value));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "pos_z"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				double value = ctx.player().getZ();
				String format = "%.2f";

				if (arg != null) {
					try {
						int x = Integer.parseInt(arg);
						format = "%." + x + "f";
					} catch (Exception e) {
						format = "%.2f";
					}
				}

				return PlaceholderResult.value(String.format(format, value));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "uuid"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(ctx.player().getUuidAsString());
			} else if (ctx.hasGameProfile()) {
				return PlaceholderResult.value(Text.of("" + ctx.gameProfile().getId()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "health"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(String.format("%.0f", ctx.player().getHealth()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "max_health"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(String.format("%.0f", ctx.player().getMaxHealth()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "hunger"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(String.valueOf(ctx.player().getHungerManager().getFoodLevel()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "saturation"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(String.format("%.0f", ctx.player().getHungerManager().getSaturationLevel()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "team_name"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				var team = ctx.player().getScoreboardTeam();
				return PlaceholderResult.value(team == null ? Text.of("") : Text.of(team.getName()));
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "team_displayname"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				//#if MC > 12002
				var team = ctx.player().getScoreboardTeam();
				//#else
				//$$ var team = (Team) ctx.player().getScoreboardTeam();
				//#endif
				return PlaceholderResult.value(team == null ? Text.of("") : team.getDisplayName());
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});

		Placeholders.register(createIdentifier("player", "team_displayname_formatted"), (ctx, arg) -> {
			if (ctx.hasPlayer()) {
				//#if MC > 12002
				var team = ctx.player().getScoreboardTeam();
				//#else
				//$$ var team = (Team) ctx.player().getScoreboardTeam();
				//#endif
				return PlaceholderResult.value(team == null ? Text.of("") : team.getFormattedName());
			} else {
				return PlaceholderResult.invalid("No player!");
			}
		});
	}
}
