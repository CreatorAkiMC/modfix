package com.aki.modfix.util.fix.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;

public class InboundHandlerTuplePacketListener
{
    public final Packet<?> packet;
    public final GenericFutureListener<? extends Future<? super Void >>[] futureListeners;

    @SafeVarargs
    public InboundHandlerTuplePacketListener(Packet<?> inPacket, GenericFutureListener <? extends Future <? super Void >> ... inFutureListeners)
    {
        this.packet = inPacket;
        this.futureListeners = inFutureListeners;
    }
}
