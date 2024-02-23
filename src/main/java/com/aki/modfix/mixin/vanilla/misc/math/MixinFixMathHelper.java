package com.aki.modfix.mixin.vanilla.misc.math;

import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;

@Mixin(MathHelper.class)
public class MixinFixMathHelper {
    @Shadow @Final private static float[] SIN_TABLE;
    @Unique
    private static final int[] SINE_TABLE_INT = new int[16384 + 1];
    @Unique
    private static final float SINE_TABLE_MIDPOINT;

    static {
        // Copy the sine table, covering to raw int bits
        for (int i = 0; i < SINE_TABLE_INT.length; i++) {
            SINE_TABLE_INT[i] = Float.floatToRawIntBits(SIN_TABLE[i]);
        }

        SINE_TABLE_MIDPOINT = SIN_TABLE[SIN_TABLE.length / 2];

        // Test that the lookup table is correct during runtime
        for (int i = 0; i < SIN_TABLE.length; i++) {
            float expected = SIN_TABLE[i];
            float value = lookup(i);

            if (expected != value) {
                throw new IllegalArgumentException(String.format("LUT error at index %d (expected: %s, found: %s)", i, expected, value));
            }
        }
    }

    @Unique
    private static float lookup(int index) {
        // A special case... Is there some way to eliminate this?
        if (index == 32768) {
            return SINE_TABLE_MIDPOINT;
        }

        // Trigonometric identity: sin(-x) = -sin(x)
        // Given a domain of 0 <= x <= 2*pi, just negate the value if x > pi.
        // This allows the sin table size to be halved.
        int neg = (index & 0x8000) << 16;

        // All bits set if (pi/2 <= x), none set otherwise
        // Extracts the 15th bit from 'half'
        int mask = (index << 17) >> 31;

        // Trigonometric identity: sin(x) = sin(pi/2 - x)
        int pos = (0x8001 & mask) + (index ^ mask);

        // Wrap the position in the table. Moving this down to immediately before the array access
        // seems to help the Hotspot compiler optimize the bit math better.
        pos &= 0x7fff;

        // Fetch the corresponding value from the LUT and invert the sign bit as needed
        // This directly manipulate the sign bit on the float bits to simplify logic
        return Float.intBitsToFloat(SINE_TABLE_INT[pos] ^ neg);
    }

    /**
     * @author AKI
     * @reason Replace sin
     */
    @Overwrite
    public static float sin(float value)
    {
        return lookup((int) (value * 10430.378f) & 0xFFFF);
    }

    /**
     * @author AKI
     * @reason Replace cos
     */
    @Overwrite
    public static float cos(float value)
    {
        return lookup((int) (value * 10430.378f + 16384.0f) & 0xFFFF);
    }
}
