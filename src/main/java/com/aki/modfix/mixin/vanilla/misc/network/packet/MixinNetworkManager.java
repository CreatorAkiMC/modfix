package com.aki.modfix.mixin.vanilla.misc.network.packet;

import com.aki.modfix.util.fix.extensions.PacketBufExtends;
import com.aki.modfix.util.fix.network.InboundHandlerTuplePacketListener;
import com.google.common.collect.Queues;
import io.netty.buffer.AbstractReferenceCountedByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {

    @Shadow private Channel channel;

    @Shadow @Final private ReentrantReadWriteLock readWriteLock;

    @Shadow protected abstract void dispatchPacket(Packet<?> inPacket, @Nullable GenericFutureListener<? extends Future<? super Void>>[] futureListeners);

    @Shadow public abstract boolean isChannelOpen();

    private final Queue<InboundHandlerTuplePacketListener> outboundPacketsQueue1 = Queues.newConcurrentLinkedQueue();

    /**
    * @author Aki
    * @reason Replace
    */
    @Overwrite
    public void sendPacket(Packet<?> packetIn)
    {
        if (this.isChannelOpen())
        {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, (GenericFutureListener[])null);
        }
        else
        {
            this.readWriteLock.writeLock().lock();

            try
            {
                this.outboundPacketsQueue1.add(new InboundHandlerTuplePacketListener(packetIn, new GenericFutureListener[0]));
            }
            finally
            {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void sendPacket(Packet<?> packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>[] listeners, CallbackInfo ci)
    {
        if (this.isChannelOpen())
        {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, (GenericFutureListener[]) ArrayUtils.add(listeners, 0, listener));
        }
        else
        {
            this.readWriteLock.writeLock().lock();

            try
            {
                this.outboundPacketsQueue1.add(new InboundHandlerTuplePacketListener(packetIn, (GenericFutureListener[])ArrayUtils.add(listeners, 0, listener)));
            }
            finally
            {
                this.readWriteLock.writeLock().unlock();
            }
        }
        ci.cancel();
    }

    /**
     * @author Aki
     * @reason Replace Method and Clean Cache
     */
    @Overwrite
    private void flushOutboundQueue()
    {
        if (this.channel != null && this.channel.isOpen())
        {
            this.readWriteLock.readLock().lock();

            try
            {
                while (!this.outboundPacketsQueue1.isEmpty())
                {
                    InboundHandlerTuplePacketListener networkmanager$inboundhandlertuplepacketlistener = this.outboundPacketsQueue1.poll();
                    this.dispatchPacket(networkmanager$inboundhandlertuplepacketlistener.packet, networkmanager$inboundhandlertuplepacketlistener.futureListeners);
                }
            }
            finally
            {
                //RenderCacheManager.BUFFERS_CLEAR.removeIf(this::IsRelease);//.clear();
                this.readWriteLock.readLock().unlock();
            }
        }
    }

    @Unique
    public boolean IsRelease(PacketBuffer buf) {
        if(buf instanceof PacketBufExtends && !(((PacketBufExtends)buf).getParent() instanceof AbstractReferenceCountedByteBuf))
            return buf.refCnt() == 0 && buf.release();
        return true;
    }
}
