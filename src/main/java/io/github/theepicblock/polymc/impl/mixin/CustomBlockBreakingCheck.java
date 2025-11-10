package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CustomBlockBreakingCheck {

    /**
     * @param block The block the player is looking at
     * @return True if the player needs to have custom breaking speeds
     */
    public static boolean needsCustomBreaking(ServerPlayer player, Block block) {
        if (player instanceof FakePlayer || !Util.isPolyMapVanillaLike(player) || player.isCreative())
            return false;

        return needsCustomBreaking(player, block.defaultBlockState());
    }

    /**
     * @param blockState The blockState the player is looking at
     * @return True if the player needs to have custom breaking speeds
     */
    public static boolean needsCustomBreaking(ServerPlayer player, BlockState blockState) {
        if (player instanceof FakePlayer || !Util.isPolyMapVanillaLike(player) || player.isCreative())
            return false;

        var polyMap = PolyMapProvider.getPolyMap(player);

        // A modded block is being broken, this always requires custom breaking
        if (polyMap.getBlockPoly(blockState.getBlock()) != null) {
            return true;
        }

        // If the modded stack has a ToolComponent, the client one will get it too.
        // This means we might not need any trickery for breaking vanilla blocks.
        var handStack = player.getMainHandItem();
        var handItem = handStack.getItem();

        if (polyMap.getItemPoly(handItem) != null) {
            return handStack.get(DataComponents.TOOL) == null;
        }

        return false;
    }
}
