package com.aki.modfix.util.fix.extensions;

import io.netty.buffer.ByteBuf;

public interface PacketBufExtends {
    ByteBuf getParent();
}
