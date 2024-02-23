package com.aki.modfix.mixin.vanilla.misc.network.pipeline;

import com.aki.modfix.util.fix.network.VarintByteDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NettyVarint21FrameDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

import static com.aki.modfix.util.fix.network.WellKnownExceptions.BAD_LENGTH_CACHED;
import static com.aki.modfix.util.fix.network.WellKnownExceptions.VARINT_BIG_CACHED;

@Mixin(NettyVarint21FrameDecoder.class)
public class MixinNettyVarint21FrameDecoderFix {
    @Unique
    private final VarintByteDecoder reader = new VarintByteDecoder();

    /**
     * @author Aki
     * @reason Replace Method Light Cpu Calculate Cycle.
     */
    @Overwrite
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        reader.reset();

        int varintEnd = in.forEachByte(reader);
        if (varintEnd == -1) {
            // We tried to go beyond the end of the buffer. This is probably a good sign that the
            // buffer was too short to hold a proper varint.
            if (reader.getResult() == VarintByteDecoder.DecodeResult.RUN_OF_ZEROES) {
                // Special case where the entire packet is just a run of zeroes. We ignore them all.
                in.clear();
            }
            return;
        }

        if (reader.getResult() == VarintByteDecoder.DecodeResult.RUN_OF_ZEROES) {
            // this will return to the point where the next varint starts
            in.readerIndex(varintEnd);
        } else if (reader.getResult() == VarintByteDecoder.DecodeResult.SUCCESS) {
            int readVarint = reader.getReadVarint();
            int bytesRead = reader.getBytesRead();
            if (readVarint < 0) {
                in.clear();
                throw BAD_LENGTH_CACHED;
            } else if (readVarint == 0) {
                // skip over the empty packet(s) and ignore it
                in.readerIndex(varintEnd + 1);
            } else {
                int minimumRead = bytesRead + readVarint;
                if (in.isReadable(minimumRead)) {
                    out.add(in.retainedSlice(varintEnd + 1, readVarint));
                    in.skipBytes(minimumRead);
                }
            }
        } else if (reader.getResult() == VarintByteDecoder.DecodeResult.TOO_BIG) {
            in.clear();
            throw VARINT_BIG_CACHED;
        }
    }
        /*p_decode_2_.markReaderIndex();
        byte[] abyte = new byte[3];

        for (int i = 0; i < abyte.length; ++i)
        {
            if (!p_decode_2_.isReadable())
            {
                p_decode_2_.resetReaderIndex();
                return;
            }

            abyte[i] = p_decode_2_.readByte();

            if (abyte[i] >= 0)
            {
                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(abyte));

                try
                {
                    int j = packetbuffer.readVarInt();

                    if (p_decode_2_.readableBytes() >= j)
                    {
                        p_decode_3_.add(p_decode_2_.readBytes(j));
                        return;
                    }

                    p_decode_2_.resetReaderIndex();
                }
                finally
                {
                    packetbuffer.release();
                }

                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");*/
}
