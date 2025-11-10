package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.ItemFrameEntityAccessor;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class VItemFrame extends AbstractVirtualEntity {
    public void spawn(PacketConsumer player, Vec3 pos, Direction facing) {
        player.sendPacket(new ClientboundAddEntityPacket(
                id,
                Mth.createInsecureUUID(),
                pos.x(),
                pos.y(),
                pos.z(),
                0,
                0,
                this.getEntityType(),
                facing.ordinal(),
                Vec3.ZERO,
                0
        ));
    }

    public void sendItemStack(PacketConsumer player, ItemStack stack) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ItemFrameEntityAccessor.getItemStackTracker(),
                stack.copy()
        ));
    }

    public void makeInvisible(PacketConsumer playerEntity) {
        this.sendFlags(playerEntity,
                false,
                false,
                false,
                false,
                true,
                false,
                false);
    }

    @Override
    public EntityType<?> getEntityType() {
        return EntityType.ITEM_FRAME;
    }
}
