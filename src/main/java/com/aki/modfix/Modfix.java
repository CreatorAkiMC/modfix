package com.aki.modfix;

import com.aki.mcutils.asm.Optifine;
import com.aki.modfix.compatibility.ModCompatibilityTileRegistry;
import com.aki.modfix.util.fix.GameSettingsExtended;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

@Mod(
        modid = Modfix.MOD_ID,
        name = Modfix.MOD_NAME,
        version = Modfix.VERSION,
        guiFactory = "com.aki.modfix.ConfigGuiFactory",
        dependencies = "required-after:mcutils"
)
public class Modfix {

    public static final String MOD_ID = "modfix";
    public static final String MOD_NAME = "Modsfix";
    public static final String VERSION = "1.0.1-SNAPSHOT";

    public static boolean isBetterFoliageInstalled;
    public static boolean isChunkAnimatorInstalled;
    public static boolean isFluidloggedAPIInstalled;
    public static boolean isCubicChunksInstalled;
    //Optifine natural.properties
    public static List<ResourceLocation> natural_properties = new ArrayList<>();

    public static final int ModPriority = 2339;

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static Modfix INSTANCE;

    public static Logger logger;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        ForgeModContainer.alwaysSetupTerrainOffThread = true;
        logger = event.getModLog();
        ModfixConfig.PreInit(event);
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        if(Optifine.isOptifineDetected())
            this.LoadOptifineProperties();
    }

    public void LoadOptifineProperties() {
        try {
            natural_properties.clear();
            String fileName = "optifine/natural.properties";
            ResourceLocation location = new ResourceLocation(fileName);
            InputStream stream = Optifine.getInputStream(location);
            String data = Optifine.ReadString(stream);
            for(String len : data.split("\n")) {
                natural_properties.add(new ResourceLocation(len.split("=")[0].trim()));
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        isBetterFoliageInstalled = Loader.isModLoaded("betterfoliage");
        isChunkAnimatorInstalled = Loader.isModLoaded("chunkanimator");
        isFluidloggedAPIInstalled = Loader.isModLoaded("fluidlogged_api");
        isCubicChunksInstalled = Loader.isModLoaded("cubicchunks");
        ModCompatibilityTileRegistry.Init();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        // コンフィグが変更された時に呼ばれる。
        if (event.getModID().equals(Modfix.MOD_ID)) {
            ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
            ModfixConfig.SyncConfig();
        }
    }

    @SubscribeEvent
    public void PlayerLogin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            player.sendMessage(new TextComponentString(player.getDisplayName().getUnformattedText() + " chose the key pattern number " + ((GameSettingsExtended) Minecraft.getMinecraft().gameSettings).getPatternID()).setStyle(new Style().setItalic(true).setColor(TextFormatting.AQUA)));
        }
    }

    //経験値を1つにまとめる。
    @SubscribeEvent
    public void WorldTickEvent(TickEvent.WorldTickEvent event) {
        World world = event.world;
        try {
            if (world != null && !world.isRemote) {
                List<EntityXPOrb> xpOrbs = new ArrayList<>();
                for (Entity entity : world.getLoadedEntityList()) {
                    if (entity instanceof EntityXPOrb && !entity.isDead) {
                        if (!xpOrbs.isEmpty()) {
                            xpOrbs.forEach(xp -> {
                                double dist = Math.sqrt(Math.pow((xp.posX - entity.posX) / 8.0d, 2.0) + Math.pow((xp.posY - entity.posY) / 8.0d, 2.0) + Math.pow((xp.posZ - entity.posZ) / 8.0d, 2.0));
                                double d5 = 1.0 - dist;
                                if (d5 > 0.0d) {
                                    d5 = Math.pow(d5, 2.0);
                                    entity.motionX += (xp.posX - entity.posX) / 8 / (dist * d5) * 0.1D;
                                    entity.motionY += (xp.posY - entity.posY) / 8 / (dist * d5) * 0.1D;
                                    entity.motionZ += (xp.posZ - entity.posZ) / 8 / (dist * d5) * 0.1D;
                                    if (dist <= 0.8d) {
                                        ((EntityXPOrb) entity).xpValue += xp.getXpValue();
                                        xp.setDead();
                                    }
                                }
                            });
                            xpOrbs = xpOrbs.stream().filter(orbs -> !orbs.isDead).collect(Collectors.toList());
                        }
                        xpOrbs.add((EntityXPOrb) entity);
                    }
                }
                xpOrbs.clear();
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks {
      /*
          public static final MySpecialBlock mySpecialBlock = null; // placeholder for special block below
      */
    }

    /**
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items {
      /*
          public static final ItemBlock mySpecialBlock = null; // itemblock for the block above
          public static final MySpecialItem mySpecialItem = null; // placeholder for special item below
      */
    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        /**
         * Listen for the register event for creating custom items
         */
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
           /*
             event.getRegistry().register(new ItemBlock(Blocks.myBlock).setRegistryName(MOD_ID, "myBlock"));
             event.getRegistry().register(new MySpecialItem().setRegistryName(MOD_ID, "mySpecialItem"));
            */
        }

        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
           /*
             event.getRegistry().register(new MySpecialBlock().setRegistryName(MOD_ID, "mySpecialBlock"));
            */
        }
    }
    /* EXAMPLE ITEM AND BLOCK - you probably want these in separate files
    public static class MySpecialItem extends Item {

    }

    public static class MySpecialBlock extends Block {

    }
    */
}
