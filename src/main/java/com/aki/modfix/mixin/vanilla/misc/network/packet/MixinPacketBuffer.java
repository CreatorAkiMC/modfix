package com.aki.modfix.mixin.vanilla.misc.network.packet;

import com.aki.modfix.util.fix.network.NetworkUtils;
import com.aki.modfix.util.math.VarIntUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Mixin(PacketBuffer.class)
public abstract class MixinPacketBuffer extends ByteBuf {
    @Shadow
    @Final
    private ByteBuf buf;

    @Shadow
    public abstract int writeCharSequence(CharSequence p_writeCharSequence_1_, Charset p_writeCharSequence_2_);

    /**
     * @author Aki
     * @reason Replace getVarIntSize
     */
    @Overwrite
    public static int getVarIntSize(int input) {
        return VarIntUtil.getVarIntLength(input);
    }

    /**
     * @author Aki
     * @reason Replace WriteString
     */
    @Overwrite
    public PacketBuffer writeString(String string) {
        //byte[] abyte = string.getBytes(StandardCharsets.UTF_8);
        int utf8bytes = NetworkUtils.utf8Bytes(string);

        if (utf8bytes > 32767) {
            throw new EncoderException("String too big (was " + utf8bytes + " bytes encoded, max " + 32767 + ")");
        } else {
            this.writeVarInt(utf8bytes);
            this.writeCharSequence(string, StandardCharsets.UTF_8);
            return new PacketBuffer(this.buf);
        }
    }

    /**
     * @author Aki
     * @reason Replace WriteVarInt
     */
    @Overwrite
    public PacketBuffer writeVarInt(int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            this.buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            this.buf.writeShort(w);
        } else {
            writeVarIntFull(this.buf, value);
        }
        return new PacketBuffer(this.buf);
    }

    private static void writeVarIntFull(ByteBuf buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
            buf.writeMedium(w);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21);
            buf.writeInt(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
        }
    }
}
