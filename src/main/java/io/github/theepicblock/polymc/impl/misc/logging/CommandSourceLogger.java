package io.github.theepicblock.polymc.impl.misc.logging;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Sends logs to a {@link CommandSourceLogger}
 */
public class CommandSourceLogger implements SimpleLogger {
    protected final CommandSourceStack commandSource;
    protected final boolean sendToOps;

    public CommandSourceLogger(CommandSourceStack commandSource, boolean sendToOps) {
        this.commandSource = commandSource;
        this.sendToOps = sendToOps;
    }

    @Override
    public void error(String string) {
        commandSource.sendSuccess(() -> Component.literal(string).withStyle(ChatFormatting.RED), sendToOps);
    }

    @Override
    public void warn(String string) {
        commandSource.sendSuccess(() -> Component.literal(string).withStyle(ChatFormatting.YELLOW), sendToOps);
    }

    @Override
    public void info(String string) {
        commandSource.sendSuccess(() -> Component.literal(string), sendToOps);
    }
}
