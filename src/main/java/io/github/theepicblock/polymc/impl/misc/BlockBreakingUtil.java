package io.github.theepicblock.polymc.impl.misc;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BlockBreakingUtil {

    public static ResourceLocation POLYMC_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("polymc", "block_breaking");

    public static Collection<AttributeInstance> DISABLER_ATTRIBUTES = Collections.singleton(
            new AttributeInstance(
                    Attributes.BLOCK_BREAK_SPEED,
                    (update) -> {}
            )
    );

    public static Collection<AttributeInstance> REMOVE_DISABLER_ATTRIBUTES = Collections.singleton(
            new AttributeInstance(
                    Attributes.BLOCK_BREAK_SPEED,
                    (update) -> {}
            )
    );

    static {
        DISABLER_ATTRIBUTES.iterator().next().addPermanentModifier(new AttributeModifier(POLYMC_MODIFIER_ID, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        REMOVE_DISABLER_ATTRIBUTES.iterator().next().removeModifier(POLYMC_MODIFIER_ID);
    }

    public static void sendBreakDisabler(ServerPlayer player) {
        player.connection.send(new ClientboundUpdateAttributesPacket(
                player.getId(),
                DISABLER_ATTRIBUTES
        ));
    }

    public static void removeBreakDisabler(ServerPlayer player) {
        player.connection.send(new ClientboundUpdateAttributesPacket(
                player.getId(),
                REMOVE_DISABLER_ATTRIBUTES
        ));
    }
}
