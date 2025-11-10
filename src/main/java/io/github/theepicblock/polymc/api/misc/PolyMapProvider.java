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
package io.github.theepicblock.polymc.api.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.mixins.SCNetworkHandlerAccessor;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PolyMapProvider {
    PolyMapProviderEvent EVENT = new PolyMapProviderEvent();

    /**
     * @return the {@link PolyMap} that is used for this player.
     */
    static PolyMapProvider get(@NotNull ServerPlayer player) {
        return get(player.connection);
    }

    /**
     * @return the {@link PolyMap} that is used for this packet handler.
     */
    static PolyMapProvider get(@NotNull ServerCommonPacketListenerImpl handler) {
        return get(((SCNetworkHandlerAccessor) handler).getConnection());
    }

    /**
     * @return the {@link PolyMap} that is used for this client connection.
     */
    static PolyMapProvider get(@NotNull Connection connection) {
        return ((PolyMapProvider)connection);
    }

    /**
     * @return the {@link PolyMap} that is used for this player.
     */
    static PolyMap getPolyMap(@NotNull ServerPlayer player) {
        return getPolyMap(player.connection);
    }

    /**
     * Might be null only when this player was never initialized
     * @return the {@link PolyMap} that is used for this packet handler.
     */
    @Nullable
    static PolyMap getPolyMap(@NotNull ServerCommonPacketListenerImpl handler) {
        return getPolyMap(((SCNetworkHandlerAccessor) handler).getConnection());
    }

    /**
     * Might be null only when this player was never initialized
     * @return the {@link PolyMap} that is used for this client connection.
     */
    @Nullable
    static PolyMap getPolyMap(@NotNull Connection connection) {
        return get(connection).getPolyMap();
    }

    /**
     * Might be null only when this player was never initialized
     * @return the {@link PolyMap} that is used by this provider.
     */
    @Nullable
    PolyMap getPolyMap();

    /**
     * Directly sets the PolyMap used by this provider.
     * @param map map to use
     * @deprecated this method should <em>not</em> be used directly! Please create an entry in {@link #EVENT} instead.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @ApiStatus.Internal
    void setPolyMap(PolyMap map);

    /**
     * Refreshes the map used by this player. It will call {@link #EVENT} again.
     * <p>
     * Warning: whilst this method allows you to refresh the {@link PolyMap} on the fly it is *not* recommended.
     * This function won't resend any old packets!
     * </p>
     */
    default void refreshUsedPolyMap() {
        this.setPolyMap(EVENT.invoke((Connection) this));
    }

    /**
     * Represents an entry in {@link #EVENT}
     * {@link #getMap(Connection)} should return {@code null} to pass through to the next entry.
     */
    interface PolyMapGetter {
        /**
         * Returns a PolyMap for this entry. Returns `null` when unspecified.
         * @return the map that should be used for this player.
         */
        PolyMap getMap(Connection connection);
    }

    class PolyMapProviderEvent extends Event<PolyMapGetter> {
        public PolyMapProviderEvent() {
            super(new PolyMapGetter[]{});
        }

        public PolyMap invoke(Connection connection) {
            for (int i = handlers.length - 1; i >= 0; i--) {
                PolyMapGetter handler = handlers[i];
                PolyMap map = handler.getMap(connection);
                if (map != null) return map;
            }
            return PolyMc.getGeneratedMap();
        }
    }
}
