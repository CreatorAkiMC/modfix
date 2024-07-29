package com.aki.modfix.compatibility;

import cofh.thermalexpansion.block.storage.TileTank;
import com.aki.modfix.mixin.extrautils2.MixinTileMachine;
import com.rwtema.extrautils2.machine.TileMachine;
import com.rwtema.extrautils2.machine.TileMachineProvider;
import com.rwtema.extrautils2.machine.TileMachineReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import java.util.HashMap;
import java.util.function.Function;

public class ModCompatibilityTileRegistry {
    public static HashMap<Class<? extends TileEntity>, Function<TileEntity, Integer>> tileChangeRegistry = new HashMap<>();

    public static void Init() {
        if(Loader.isModLoaded("thermalexpansion")) {
            AddTile(TileTank.class, (tile) -> {
                TileTank tileTank = ((TileTank)tile);
                FluidStack fluidStack = tileTank.getTankFluid();
                return (fluidStack != null ? fluidStack.hashCode() + fluidStack.amount * 31 : 31) + tileTank.getType() * 31;
            });
        }

        if(Loader.isModLoaded("extrautils2")) {
            AddTile(TileMachineProvider.class, (tile) -> {
                TileMachine tileMachine = (TileMachine)tile;
                return ((MixinTileMachine)tileMachine).getType().hashCode() + (((TileMachine) tile).fluidInputMap != null ? tileMachine.fluidInputMap.hashCode() : 31) + tileMachine.upgrades.hashCode();
            });
            AddTile(TileMachineReceiver.class, (tile) -> {
                TileMachine tileMachine = (TileMachine)tile;
                return ((MixinTileMachine)tileMachine).getType().hashCode() + (((TileMachine) tile).fluidInputMap != null ? tileMachine.fluidInputMap.hashCode() : 31) + tileMachine.upgrades.hashCode();
            });
        }
    }

    public static void AddTile(Class<? extends TileEntity> tileClass, Function<TileEntity, Integer> function) {
        tileChangeRegistry.put(tileClass, function);
    }
}
