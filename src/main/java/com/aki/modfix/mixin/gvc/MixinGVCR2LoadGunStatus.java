package com.aki.modfix.mixin.gvc;

import com.aki.mcutils.threads.MCUtilsThreadManager;
import gvclib.LoadGunStatusBase;
import gvclib.RecoilMotionReader;
import gvclib.ReloadMotionReader;
import gvclib.item.ItemGunBase;
import gvcr2.mod_GVCR2;
import gvcr2.registry.GVCR2LoadGunStatus;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import objmodel.AdvancedModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.io.*;

@Pseudo
@Mixin(value = GVCR2LoadGunStatus.class, remap = false)
public class MixinGVCR2LoadGunStatus {

    @Shadow(remap = false) private static int sell_eme;

    @Shadow(remap = false) private static int sell_iron;

    @Shadow(remap = false) private static int sell_red;

    @Shadow(remap = false) private static int sell_tier;

    /**
     * @author Aki
     * @reason Replace to MultiThread
     */
    @Overwrite(remap = false)
    public static void load(ItemGunBase gun, mod_GVCR2 mod, RegistryEvent.Register<Item> event, String text) {
        //reset
        {
            sell_eme = 1;
            sell_iron = 1;
            sell_red = 1;
            sell_tier = 0;
        }


        //System.out.println(String.format("modelroad"));

        MCUtilsThreadManager.FileFJExecutor.execute(() -> {

            try {
                InputStream inputStream = GVCR2LoadGunStatus.class.getResourceAsStream("/assets/gvcr2/textures/gun/status/" + text);
                //FileChannel channel = ((FileInputStream)inputStream).getChannel();
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    //ByteBuffer bb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).order(ByteOrder.LITTLE_ENDIAN);
                    //BufferedReader br = bb.
                    String str;
                    while ((str = br.readLine()) != null) { // 1行ずつ読み込む
                        // System.out.println(str);
                        String[] type = str.split(",");

                        int guntype = 0;

                        if (type.length != 0) {// 1
                            if (mod.proxy.getClient()) {
                                gun.canobj = true;
                                gun.obj_true = true;
                                if (type[0].equals("ObjModel")) {
                                    //System.out.println(String.format("modelroad+") + text);
                                    gun.obj_model = AdvancedModelLoader
                                            .loadModel(new ResourceLocation("gvcr2:textures/gun/model/" + type[1]));
                                    gun.obj_model_name = "gvcr2:textures/gun/model/" + type[1];
                                }
                                if (type[0].equals("ObjTexture")) {
                                    gun.obj_tex = "gvcr2:textures/gun/model/" + type[1];
                                }
                                gun.arm_model = AdvancedModelLoader.loadModel(new ResourceLocation("gvclib:textures/model/arm.obj"));
                                gun.arm_model2 = AdvancedModelLoader.loadModel(new ResourceLocation("gvclib:textures/model/arm2.obj"));
                                gun.flash_model = AdvancedModelLoader.loadModel(new ResourceLocation("gvclib:textures/model/flash.obj"));
                                {
                                    if (type[0].equals("Reload_Motion")) {
                                        gun.newreload = Boolean.parseBoolean(type[1]);
                                    }
                                    if (type[0].equals("ReloadMotionType")) {
                                        gun.reloadmotion = Integer.parseInt(type[1]);
                                    }
                                    String reloadm = null;
                                    if (type[0].equals("ReloadMotion")) {
                                        reloadm = type[1];
                                        gun.reload_motion_name = "gvcr2:textures/gun/reload/" + type[1];
                                    }
                                    if (reloadm != null) {
                                        ResourceLocation reloadmotion = new ResourceLocation("gvcr2:textures/gun/reload/" + reloadm);
                                        ReloadMotionReader.read(gun, reloadmotion);
                                    }
                                    String recoilm = null;
                                    if (type[0].equals("RecoilMotionType")) {
                                        gun.recoilmotion = Boolean.parseBoolean(type[1]);
                                    }
                                    if (type[0].equals("RecoilMotion")) {
                                        recoilm = type[1];
                                        gun.recoil_motion_name = "gvcr2:textures/gun/reload/" + type[1];
                                    }
                                    if (recoilm != null) {
                                        ResourceLocation recoilmotion = new ResourceLocation("gvcr2:textures/gun/reload/" + recoilm);
                                        RecoilMotionReader.read(gun, recoilmotion);
                                    }
                                }
                            }


                            if (type[0].equals("Sell_EMERALD")) {
                                sell_eme = Integer.parseInt(type[1]);
                            }
                            if (type[0].equals("Sell_IRONINGOT")) {
                                sell_iron = Integer.parseInt(type[1]);
                            }
                            if (type[0].equals("Sell_REDSTONE")) {
                                sell_red = Integer.parseInt(type[1]);
                            }
                            if (type[0].equals("Sell_TIER")) {
                                sell_tier = Integer.parseInt(type[1]);
                                if (sell_tier == 1) {
                                    mod_GVCR2.gun_eme_1[mod_GVCR2.gun_id_1] = sell_eme;
                                    mod_GVCR2.gun_iron_1[mod_GVCR2.gun_id_1] = sell_iron;
                                    mod_GVCR2.gun_red_1[mod_GVCR2.gun_id_1] = sell_red;
                                    mod_GVCR2.gun_item_1[mod_GVCR2.gun_id_1] = gun;
                                    ++mod_GVCR2.gun_id_1;
                                }
                                if (sell_tier == 2) {
                                    mod_GVCR2.gun_eme_2[mod_GVCR2.gun_id_2] = sell_eme;
                                    mod_GVCR2.gun_iron_2[mod_GVCR2.gun_id_2] = sell_iron;
                                    mod_GVCR2.gun_red_2[mod_GVCR2.gun_id_2] = sell_red;
                                    mod_GVCR2.gun_item_2[mod_GVCR2.gun_id_2] = gun;
                                    ++mod_GVCR2.gun_id_2;
                                }
                                if (sell_tier == 3) {
                                    mod_GVCR2.gun_eme_3[mod_GVCR2.gun_id_3] = sell_eme;
                                    mod_GVCR2.gun_iron_3[mod_GVCR2.gun_id_3] = sell_iron;
                                    mod_GVCR2.gun_red_3[mod_GVCR2.gun_id_3] = sell_red;
                                    mod_GVCR2.gun_item_3[mod_GVCR2.gun_id_3] = gun;
                                    ++mod_GVCR2.gun_id_3;
                                }
                            }


                            LoadGunStatusBase.load(gun, event, text, type, "gvcr2:textures/misc/", "gvcr2:textures/entity/");

                            if (!mod.cfg_exprotion_player) {
                                gun.ex = false;
                            }
                        }

                    }
                    br.close(); // ファイルを閉じる
                }
                inputStream.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}
