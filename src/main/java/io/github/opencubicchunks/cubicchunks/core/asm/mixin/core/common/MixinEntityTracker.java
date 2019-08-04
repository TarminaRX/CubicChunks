/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015-2019 OpenCubicChunks
 *  Copyright (c) 2015-2019 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.github.opencubicchunks.cubicchunks.core.asm.mixin.core.common;

import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.core.entity.ICubicEntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketSetPassengers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.InputStream;
import java.util.Set;

@Mixin(EntityTracker.class)
public class MixinEntityTracker implements ICubicEntityTracker {

    @Shadow @Final private Set<EntityTrackerEntry> entries;

    // Previous version of this function contain code which force Minecraft to send all SPacketEntityAttach before any SPacketSetPassengers
    @Override public void sendLeashedEntitiesInCube(EntityPlayerMP player, ICube cubeIn) {
        for (EntityTrackerEntry entitytrackerentry : this.entries) {
            Entity entity = entitytrackerentry.getTrackedEntity();
            if (entity != player &&
                    entity.chunkCoordX == cubeIn.getX() &&
                    entity.chunkCoordZ == cubeIn.getZ() &&
                    entity.chunkCoordY == cubeIn.getY()) {

                entitytrackerentry.updatePlayerEntity(player);
                //noinspection ConstantConditions
                if (entity instanceof EntityLiving && ((EntityLiving) entity).getLeashHolder() != null) {
                    player.connection.sendPacket(new SPacketEntityAttach(entity, ((EntityLiving) entity).getLeashHolder()));
                }

                if (!entity.getPassengers().isEmpty()) {
                    player.connection.sendPacket(new SPacketSetPassengers(entity));
                }
            }
        }
    }
}
