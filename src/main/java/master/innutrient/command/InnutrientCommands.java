package master.innutrient.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.uapi.command.UApiCommandRegistry;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutritionService;
import master.innutrient.nutrition.MealQualityEngine;
import master.innutrient.nutrition.resolver.NutritionResolver;
import master.innutrient.registry.NutritionRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

public final class InnutrientCommands {
    private InnutrientCommands() {}

    public static void bootstrap() {
        UApiCommandRegistry.registerSection("innutrient", InnutrientCommands::create);
    }

    @SubscribeEvent
    public static void registerStandalone(RegisterCommandsEvent event) {
        event.getDispatcher().register(create());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("innutrient")
            .then(Commands.literal("show")
                .executes(context -> show(context.getSource(), context.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(context -> show(context.getSource(), EntityArgument.getPlayer(context, "player")))))
            .then(Commands.literal("set").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(groupArgument().then(Commands.argument("value", DoubleArgumentType.doubleArg(0, 100))
                        .executes(context -> change(context.getSource(), EntityArgument.getPlayer(context, "player"),
                            IdentifierArgument.getId(context, "group"),
                            DoubleArgumentType.getDouble(context, "value"), false))))))
            .then(Commands.literal("add").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(groupArgument().then(Commands.argument("value", DoubleArgumentType.doubleArg(-100, 100))
                        .executes(context -> change(context.getSource(), EntityArgument.getPlayer(context, "player"),
                            IdentifierArgument.getId(context, "group"),
                            DoubleArgumentType.getDouble(context, "value"), true))))))
            .then(Commands.literal("reset").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                    NutritionService.reset(player);
                    context.getSource().sendSuccess(() -> Component.translatable(
                        "command.innutrient.reset", player.getDisplayName()), true);
                    return 1;
                })))
            .then(Commands.literal("inspect").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> inspect(context.getSource())))
            .then(Commands.literal("reload").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> {
                    var server = context.getSource().getServer();
                    context.getSource().sendSuccess(() -> Component.translatable("commands.reload.success"), true);
                    ReloadCommand.reloadPacks(server.getPackRepository().getSelectedIds(), context.getSource());
                    return 1;
                }));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, net.minecraft.resources.Identifier>
    groupArgument() {
        return Commands.argument("group", IdentifierArgument.id()).suggests((context, builder) ->
            SharedSuggestionProvider.suggest(
                NutritionRegistry.groups().stream().map(value -> value.id().toString()), builder));
    }

    private static int show(CommandSourceStack source, ServerPlayer player) {
        var state = NutritionService.get(player);
        source.sendSuccess(() -> Component.translatable("command.innutrient.show.header", player.getDisplayName()), false);
        for (NutrientGroup group : NutritionRegistry.groups()) {
            String value = String.format(Locale.ROOT, "%.1f%%", state.get(group));
            source.sendSuccess(() -> Component.literal("  ").append(Component.translatable(group.translationKey()))
                .append(Component.literal(": " + value)), false);
        }
        String balance = String.format(Locale.ROOT, "%.1f%%", NutritionService.balanceScore(state));
        source.sendSuccess(() -> Component.translatable("command.innutrient.show.balance", balance), false);
        source.sendSuccess(() -> Component.translatable("command.innutrient.show.quality",
            Component.translatable(state.dietQuality().translationKey())), false);
        return NutritionRegistry.groups().size();
    }

    private static int change(CommandSourceStack source, ServerPlayer player,
                              net.minecraft.resources.Identifier group, double value, boolean add) {
        boolean changed = add ? NutritionService.add(player, group, value) : NutritionService.set(player, group, value);
        if (!changed) {
            source.sendFailure(Component.translatable("command.innutrient.unknown_group", group));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable(add ? "command.innutrient.add" : "command.innutrient.set",
            player.getDisplayName(), group, value), true);
        return 1;
    }

    private static int inspect(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("command.innutrient.inspect.empty"));
            return 0;
        }
        var profile = NutritionResolver.INSTANCE.resolve(stack);
        source.sendSuccess(() -> Component.translatable("command.innutrient.inspect.item", stack.getHoverName()), false);
        source.sendSuccess(() -> Component.translatable("command.innutrient.inspect.source",
            profile.source().name().toLowerCase(Locale.ROOT)), false);
        if (!profile.resolved()) {
            source.sendSuccess(() -> Component.translatable("command.innutrient.inspect.unresolved"), false);
            return 0;
        }
        profile.nutrients().forEach((id, value) -> source.sendSuccess(() -> Component.literal(
            "  " + id + " " + String.format(Locale.ROOT, "%.1f%%", value * 100)), false));
        if (profile.recipeId() != null)
            source.sendSuccess(() -> Component.translatable("command.innutrient.inspect.recipe",
                profile.recipeId(), profile.resolutionDepth()), false);
        var meal = MealQualityEngine.classify(profile);
        source.sendSuccess(() -> Component.translatable("command.innutrient.inspect.meal",
            Component.translatable(meal.translationKey()),
            String.format(Locale.ROOT, "%.0f%%", (MealQualityEngine.multiplier(profile) - 1.0) * 100)), false);
        return profile.nutrients().size();
    }
}
