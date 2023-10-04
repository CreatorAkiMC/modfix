package com.aki.modfix.chunk.GLSytem;

import com.aki.modfix.util.reflectors.ReflectionField;
import com.aki.modfix.util.reflectors.ReflectionMethod;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;

import java.util.function.Consumer;

public interface FreeSectorManager {

    int largestSector();

    SectorizedList.Sector get(int minSectorSize);

    void add(SectorizedList.Sector sector);

    void remove(SectorizedList.Sector sector);

    default void siftUp(SectorizedList.Sector sector, Consumer<SectorizedList.Sector> c) {
        remove(sector);
        c.accept(sector);
        add(sector);
    }

    default void siftDown(SectorizedList.Sector sector, Consumer<SectorizedList.Sector> c) {
        remove(sector);
        c.accept(sector);
        add(sector);
    }

    abstract class Map<T extends Object2ObjectSortedMap<SectorizedList.Sector, SectorizedList.Sector>> implements FreeSectorManager {

        protected final T map;

        public Map(T map) {
            this.map = map;
        }

        @Override
        public void add(SectorizedList.Sector sector) {
            map.put(sector, sector);
        }

        @Override
        public int largestSector() {
            return map.isEmpty() ? 0 : map.lastKey().getSectorCount();
        }

        @Override
        public void remove(SectorizedList.Sector sector) {
            map.remove(sector);
        }

    }

    class AVL extends Map<Object2ObjectAVLTreeMap<SectorizedList.Sector, SectorizedList.Sector>> {

        private static final ReflectionField<?> TREE = new ReflectionField<>(Object2ObjectAVLTreeMap.class, "tree", "tree");
        private static final ReflectionField<SectorizedList.Sector> KEY = new ReflectionField<>(Object2ObjectAVLTreeMap.class.getName() + "$Entry", "key", "key");
        private static final ReflectionMethod<?> LEFT = new ReflectionMethod<>(Object2ObjectAVLTreeMap.class.getName() + "$Entry", "left", "left");
        private static final ReflectionMethod<?> RIGHT = new ReflectionMethod<>(Object2ObjectAVLTreeMap.class.getName() + "$Entry", "right", "right");

        public AVL(Object2ObjectAVLTreeMap<SectorizedList.Sector, SectorizedList.Sector> map) {
            super(map);
        }

        @Override
        public SectorizedList.Sector get(int minSectorSize) {
            if (largestSector() < minSectorSize)
                return null;
            SectorizedList.Sector q = null;
            Object p = TREE.get(map);
            while (p != null) {
                SectorizedList.Sector s = KEY.get(p);
                if (s.getSectorCount() < minSectorSize) {
                    p = RIGHT.invoke(p);
                    continue;
                }
                if (s.getSectorCount() > minSectorSize) {
                    q = s;
                    p = LEFT.invoke(p);
                    continue;
                }
                return s;
            }
            return q;
        }

    }

    class RB extends Map<Object2ObjectRBTreeMap<SectorizedList.Sector, SectorizedList.Sector>> {

        private static final ReflectionField<?> TREE = new ReflectionField<>(Object2ObjectRBTreeMap.class, "tree", "tree");
        private static final ReflectionField<SectorizedList.Sector> KEY = new ReflectionField<>(Object2ObjectRBTreeMap.class.getName() + "$Entry", "key", "key");
        private static final ReflectionMethod<?> LEFT = new ReflectionMethod<>(Object2ObjectRBTreeMap.class.getName() + "$Entry", "left", "left");
        private static final ReflectionMethod<?> RIGHT = new ReflectionMethod<>(Object2ObjectRBTreeMap.class.getName() + "$Entry", "right", "right");

        public RB() {
            super(new Object2ObjectRBTreeMap<>());
        }

        @Override
        public SectorizedList.Sector get(int minSectorSize) {
            if (largestSector() < minSectorSize)
                return null;
            SectorizedList.Sector q = null;
            Object p = TREE.get(map);
            while (p != null) {
                SectorizedList.Sector s = KEY.get(p);
                if (s.getSectorCount() < minSectorSize) {
                    p = RIGHT.invoke(p);
                    continue;
                }
                if (s.getSectorCount() > minSectorSize) {
                    q = s;
                    p = LEFT.invoke(p);
                    continue;
                }
                return s;
            }
            return q;
        }

    }

}
