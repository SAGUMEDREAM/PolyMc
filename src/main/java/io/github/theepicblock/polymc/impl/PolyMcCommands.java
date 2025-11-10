/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.misc.PolyDumper;
import io.github.theepicblock.polymc.impl.misc.logging.CommandSourceLogger;
import io.github.theepicblock.polymc.impl.misc.logging.ErrorTrackerWrapper;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.wizard.PacketCountManager;
import io.github.theepicblock.polymc.impl.poly.wizard.ThreadedWizardUpdater;
import io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator;
import io.github.theepicblock.polymc.mixins.TACSAccessor;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.io.IOException;

import static net.minecraft.commands.Commands.literal;

/**
 * Registers the polymc commands.
 */
public class PolyMcCommands {
    private static boolean isGeneratingResources = false;

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("polymc").requires(source -> source.hasPermission(2))
                    .then(literal("debug")
                            .then(literal("clientItem")
                                    .executes((context) -> {
                                        var player = context.getSource().getPlayerOrException();
                                        var heldItem = player.getInventory().getSelectedItem();
                                        var polydItem = PolyMapProvider.getPolyMap(player).getClientItem(heldItem, player, null);
                                        var heldItemTag = ItemStack.OPTIONAL_CODEC.encodeStart(context.getSource().registryAccess().createSerializationContext(NbtOps.INSTANCE), polydItem).getOrThrow();
                                        var nbtText = NbtUtils.toPrettyComponent(heldItemTag);
                                        context.getSource().sendSuccess(() -> nbtText, false);
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(literal("replaceInventoryWithDebug")
                                    .executes((context) -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        if (!player.isCreative()) {
                                            throw new SimpleCommandExceptionType(new LiteralMessage("You must be in creative mode to execute this command. Keep in mind that this will wipe your inventory.")).create();
                                        }
                                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                            if (i == 0) {
                                                player.getInventory().setItem(i, new ItemStack(Items.GREEN_STAINED_GLASS_PANE));
                                            } else {
                                                player.getInventory().setItem(i, new ItemStack(Items.RED_STAINED_GLASS_PANE, i));
                                            }
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(literal("getWizardRestrictions")
                                    .executes(context -> doGetWizardRestrictions(context, context.getSource().getPlayerOrException()))
                                    .then(Commands.argument("player", EntityArgument.player())
                                            .executes(context -> doGetWizardRestrictions(context, EntityArgument.getPlayer(context, "player"))))))
                    .then(literal("generate")
                            .then(literal("resources")
                                    .executes((context -> {
                                        SimpleLogger commandSource = new CommandSourceLogger(context.getSource(), true);
                                        ErrorTrackerWrapper logger = new ErrorTrackerWrapper(PolyMc.LOGGER);
                                        if (isGeneratingResources) {
                                            commandSource.error("Already generating a resource pack at this moment");
                                            return 0;
                                        }
                                        // Generate pack
                                        isGeneratingResources = true;
                                        new Thread(() -> {
                                            try {
                                                var pack = PolyMc.getMapForResourceGen().generateResourcePack(logger);
                                                if (logger.errors != 0) {
                                                    commandSource.error("There have been errors whilst generating the resource pack. These are usually completely normal. It only means that PolyMc couldn't find some of the textures or models. See the console for more info.");
                                                }

                                                // Write pack to file
                                                try {
                                                    ResourcePackGenerator.cleanAndWrite(pack, "resource", logger);

                                                    commandSource.info("Finished generating resource pack");
                                                    commandSource.warn("Before hosting this resource pack, please make sure you have the legal right to redistribute the assets inside.");
                                                } catch (Exception e) {
                                                    commandSource.error("An error occurred whilst trying to save the resource pack! Please check the console.");
                                                    e.printStackTrace();
                                                }
                                            } catch (Throwable e) {
                                                commandSource.error("An error occurred whilst trying to generate the resource pack! Please check the console.");
                                                e.printStackTrace();
                                            }

                                            isGeneratingResources = false;
                                        }).start();
                                        commandSource.info("Starting resource generation");
                                        return Command.SINGLE_SUCCESS;
                                    })))
                            .then(literal("polyDump")
                                    .executes((context) -> {
                                        SimpleLogger logger = new CommandSourceLogger(context.getSource(), true);
                                        try {
                                            PolyDumper.dumpPolyMap(PolyMc.getMapForResourceGen(), "PolyDump.txt", logger);
                                        } catch (IOException e) {
                                            logger.error(e.getMessage());
                                            return 0;
                                        } catch (Exception e) {
                                            logger.info("An error occurred whilst trying to generate the poly dump! Please check the console.");
                                            e.printStackTrace();
                                            return 0;
                                        }
                                        logger.info("Finished generating poly dump");
                                        return Command.SINGLE_SUCCESS;
                                    }))));
        });
    }

    public static int doGetWizardRestrictions(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        if (ConfigManager.getConfig().enableWizardThreading && !ThreadedWizardUpdater.MAIN.isSameThread()) {
            ThreadedWizardUpdater.MAIN.executeIfPossible(() -> doGetWizardRestrictions(context, player));
        }

        var trackerInfo = PacketCountManager.INSTANCE.getTrackerInfoForPlayer(player);
        var source = context.getSource();

        var headerColour = ConfigManager.getConfig().enableWizardThreading ? ChatFormatting.GOLD : ChatFormatting.AQUA;
        source.sendSuccess(() -> Component.literal("=== Packet restriction info for ").withStyle(headerColour)
                .append(player.getDisplayName())
                .append(Component.literal(" ===").withStyle(headerColour)), false);

        var hoverTxt = Component.literal("Target packet count: ").append(Component.literal(PacketCountManager.MIN_PACKETS+"-"+PacketCountManager.MAX_PACKETS+" packets per tick").withStyle(ChatFormatting.AQUA));
        source.sendSuccess(() -> Component.literal("Average packet count per tick: ")
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(hoverTxt)))
                .append(packetCount2Text(trackerInfo.calculateAveragePacketCount())), false);
        var packetHistory = Component.literal("History: [");
        for (int i = 0; ; i++) {
            packetHistory.append(packetCount2Text(trackerInfo.getPacketHistory()[i]));
            if (i == trackerInfo.getPacketHistory().length-1) {
                source.sendSuccess(() -> packetHistory.append("]"), false);
                break;
            }
            packetHistory.append(", ");
        }

        var restrictionLevelTxt = Component.literal(String.valueOf(trackerInfo.getRestrictionLevel()));
        if (trackerInfo.getRestrictionLevel() > PacketCountManager.MAX_RESTRICTION) {
            restrictionLevelTxt.withStyle(ChatFormatting.DARK_PURPLE);
        } else if (trackerInfo.getRestrictionLevel() > 8) {
            restrictionLevelTxt.withStyle(ChatFormatting.RED);
        } else if (trackerInfo.getRestrictionLevel() > 5) {
            restrictionLevelTxt.withStyle(ChatFormatting.YELLOW);
        } else {
            restrictionLevelTxt.withStyle(ChatFormatting.DARK_GREEN);
        }
        source.sendSuccess(() -> Component.literal("Restriction level: ").append(restrictionLevelTxt), false);
        var watchDistance = ((TACSAccessor)context.getSource().getLevel().getChunkSource().chunkMap).getServerViewDistance();
        source.sendSuccess(() -> Component.literal("Watch distance/radius: ").append(Component.literal(watchDistance+"/"+PacketCountManager.getWatchRadiusFromDistance(watchDistance)).withStyle(ChatFormatting.AQUA)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Component packetCount2Text(int count) {
        var t = Component.literal(String.valueOf(count));
        if (count > PacketCountManager.MAX_PACKETS * 1.6) {
            t.withStyle(ChatFormatting.RED);
        } else if (count > PacketCountManager.MAX_PACKETS) {
            t.withStyle(ChatFormatting.YELLOW);
        } else if (count > PacketCountManager.MIN_PACKETS) {
            t.withStyle(ChatFormatting.DARK_GREEN);
        } else {
            t.withStyle(ChatFormatting.GREEN);
        }
        var hoverText = Component.literal("")
                .append(t.copy().setStyle(t.getStyle()))
                .append(Component.literal(" packets per tick =  ").withStyle(ChatFormatting.RESET))
                .append(Component.literal(String.valueOf(count * 20)).setStyle(t.getStyle()))
                .append(Component.literal(" packets per second").withStyle(ChatFormatting.RESET));
        return t.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(hoverText)));
    }
}
