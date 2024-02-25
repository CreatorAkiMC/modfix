package com.aki.modfix.mixin.vanilla.misc.network.packet;

import com.aki.modfix.util.fix.network.NetworkUtils;
import com.aki.modfix.util.fix.network.PacketSizeUtil;
import com.aki.modfix.util.math.VarIntUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Mixin(PacketBuffer.class)
public abstract class MixinPacketBuffer extends ByteBuf {
    @Shadow
    @Final
    private ByteBuf buf;

    @Shadow
    public abstract int writeCharSequence(CharSequence p_writeCharSequence_1_, Charset p_writeCharSequence_2_);

    @Shadow public abstract String readString(int maxLength);

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
     * @reason Change Max PacketSize
     */
    @Overwrite
    public ITextComponent readTextComponent() throws IOException
    {
        return ITextComponent.Serializer.jsonToComponent(this.readString(PacketSizeUtil.PacketSize));
    }

    /**
     * @author Aki
     * @reason Change Max PacketSize
     */
    @Overwrite
    public ResourceLocation readResourceLocation()
    {
        return new ResourceLocation(this.readString(PacketSizeUtil.PacketSize));
    }

    /**
     * @author Aki
     * @reason Change Max PacketSize
     */
    @Overwrite
    @Nullable
    public NBTTagCompound readCompoundTag() throws IOException
    {
        int i = this.readerIndex();
        byte b0 = this.readByte();

        if (b0 == 0)
        {
            return null;
        }
        else
        {
            this.readerIndex(i);

            try
            {
                return CompressedStreamTools.read(new ByteBufInputStream(this), new NBTSizeTracker(PacketSizeUtil.ReadNBTSize));
            }
            catch (IOException ioexception)
            {
                throw new EncoderException(ioexception);
            }
        }
    }

    /**
     * @author Aki
     * @reason Replace WriteString
     */
    @Overwrite
    public PacketBuffer writeString(String string) {
        //byte[] abyte = string.getBytes(StandardCharsets.UTF_8);
        int utf8bytes = NetworkUtils.utf8Bytes(string);

        if (utf8bytes > PacketSizeUtil.PacketSize) {
            throw new EncoderException("String too big (was " + utf8bytes + " bytes encoded, max " + PacketSizeUtil.PacketSize + ")");
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
