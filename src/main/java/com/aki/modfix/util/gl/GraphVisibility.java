package com.aki.modfix.util.gl;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import net.minecraft.util.EnumFacing;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.IntStream;

import static net.minecraft.util.EnumFacing.VALUES;

public class GraphVisibility {
    private static final int DX = 1 << 8;
    private static final int DY = 1 << 4;
    private static final int DZ = 1;
    private static final LinkedHashMap<EnumFacing, int[]> INDICES = MapCreateHelper.CreateLinkedHashMap(EnumFacing.values(), index -> {
        switch (EnumFacing.byIndex(index)) {
            case DOWN:
                return IntStream.range(0, 16).flatMap(i -> IntStream.range(0, 16).map(j -> index(i, 0, j))).toArray();
            case UP:
                return IntStream.range(0, 16).flatMap(i -> IntStream.range(0, 16).map(j -> index(i, 15, j))).toArray();
            case NORTH:
                return IntStream.range(0, 16).flatMap(i -> IntStream.range(0, 16).map(j -> index(i, j, 0))).toArray();
            case SOUTH:
                return IntStream.range(0, 16).flatMap(i -> IntStream.range(0, 16).map(j -> index(i, j, 15))).toArray();
            case WEST:
                return IntStream.range(0, 16).flatMap(i -> IntStream.range(0, 16).map(j -> index(0, i, j))).toArray();
            case EAST:
                return IntStream.range(0, 16).flatMap(i -> IntStream.range(0, 16).map(j -> index(15, i, j))).toArray();
            default:
                throw new IllegalArgumentException();
        }
    });

    private final BitSet opacityCache = new BitSet(6 * 16 * 16 * 16);
    private int opaqueFaceCount;

    public void setOpaque(int x, int y, int z, EnumFacing dir) {
        opacityCache.set(indexExt(x & 15, y & 15, z & 15, dir));
        opaqueFaceCount++;
    }

    private static int indexExt(int x, int y, int z, EnumFacing dir) {
        return indexExt(index(x, y, z), dir);
    }

    private static int indexExt(int index, EnumFacing dir) {
        return index | (dir.ordinal() << 12);
    }

    private static int index(int x, int y, int z) {
        return x << 8 | y << 4 | z;
    }

    private static int x(int index) {
        return index >> 8 & 15;
    }

    private static int y(int index) {
        return index >> 4 & 15;
    }

    private static int z(int index) {
        return index & 15;
    }

    public VisibilitySet compute() {
        VisibilitySet visibilitySet = new VisibilitySet();
        if (opaqueFaceCount < 256) {
            visibilitySet.setAllVisible();
        } else if (opaqueFaceCount < 4096 * 6 - 1) {
            BitSet visited = new BitSet(16 * 16 * 16);
            for (EnumFacing dir : VALUES) {
                for (int index : INDICES.get(dir)) {
                    traverse(visibilitySet, visited, index, dir);
                    if (visibilitySet.allVisible())
                        return visibilitySet;
                }
            }
        }
        return visibilitySet;
    }

    private void traverse(VisibilitySet visibilitySet, BitSet visited, int startIndex, EnumFacing origin) {
        if (opacityCache.get(indexExt(startIndex, origin)))
            return;

        IntPriorityQueue queue = new IntArrayFIFOQueue();
        queue.enqueue(startIndex);
        Set<EnumFacing> visibles = EnumSet.noneOf(EnumFacing.class);

        while (!queue.isEmpty()) {
            int index = queue.dequeueInt();

            for (EnumFacing dir : VALUES) {
                if (opacityCache.get(indexExt(index, dir)))
                    continue;
                int neighborIndex = neighbor(index, dir);
                if (neighborIndex < 0) {
                    visibles.add(dir);
                    continue;
                }
                if (opacityCache.get(indexExt(neighborIndex, dir.getOpposite())))
                    continue;
                if (visited.get(neighborIndex))
                    continue;
                visited.set(neighborIndex);
                queue.enqueue(neighborIndex);
            }

            if (visibles.size() == EnumFacing.values().length) {
                visibilitySet.setAllVisible();
                return;
            }
        }

        for (EnumFacing dir1 : visibles) {
            for (EnumFacing dir2 : visibles) {
                visibilitySet.setVisible(dir1, dir2);
            }
        }
    }

    private int neighbor(int index, EnumFacing dir) {
        switch (dir) {
            case DOWN:
                if (y(index) == 0)
                    return -1;
                return index - DY;
            case UP:
                if (y(index) == 15)
                    return -1;
                return index + DY;
            case NORTH:
                if (z(index) == 0)
                    return -1;
                return index - DZ;
            case SOUTH:
                if (z(index) == 15)
                    return -1;
                return index + DZ;
            case WEST:
                if (x(index) == 0)
                    return -1;
                return index - DX;
            case EAST:
                if (x(index) == 15)
                    return -1;
                return index + DX;
            default:
                return -1;
        }
    }
}
