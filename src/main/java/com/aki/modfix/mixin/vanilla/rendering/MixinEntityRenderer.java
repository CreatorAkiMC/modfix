package com.aki.modfix.mixin.vanilla.rendering;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityRenderer.class)
public abstract class MixinEntityRenderer {

    /*@Shadow
    protected abstract void updateLightmap(float partialTicks);

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    public abstract void getMouseOver(float partialTicks);

    @Shadow
    public static int anaglyphField;

    @Shadow
    protected abstract boolean isDrawBlockOutline();

    @Shadow
    protected abstract void updateFogColor(float partialTicks);

    @Shadow
    protected abstract void setupCameraTransform(float partialTicks, int pass);

    @Shadow
    private int frameCount;

    @Shadow
    protected abstract void renderCloudsCheck(RenderGlobal renderGlobalIn, float partialTicks, int pass, double x, double y, double z);

    @Shadow
    protected abstract void setupFog(int startCoords, float partialTicks);

    @Shadow
    protected abstract float getFOVModifier(float partialTicks, boolean useFOVSetting);

    @Shadow
    private boolean debugView;

    @Shadow
    public abstract void disableLightmap();

    @Shadow
    private float farPlaneDistance;

    @Shadow
    public abstract void enableLightmap();

    @Shadow
    protected abstract void renderRainSnow(float partialTicks);

    @Shadow
    private boolean renderHand;

    @Shadow
    protected abstract void renderHand(float partialTicks, int pass);


    @Overwrite
    public void renderWorld(float partialTicks, long finishTimeNano)
    {
        this.updateLightmap(partialTicks);

        if (this.mc.getRenderViewEntity() == null)
        {
            this.mc.setRenderViewEntity(this.mc.player);
        }

        this.getMouseOver(partialTicks);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.5F);
        this.mc.profiler.startSection("center");

        long Time = System.currentTimeMillis();

        if (this.mc.gameSettings.anaglyph)
        {
            anaglyphField = 0;
            GlStateManager.colorMask(false, true, true, false);
            this.renderWorldPass(0, partialTicks, finishTimeNano);
            anaglyphField = 1;
            GlStateManager.colorMask(true, false, false, false);
            this.renderWorldPass(1, partialTicks, finishTimeNano);
            GlStateManager.colorMask(true, true, true, false);
        }
        else
        {
            this.renderWorldPass(2, partialTicks, finishTimeNano);
        }

        System.out.println("EntityRendererT: " + (System.currentTimeMillis() - Time));

        this.mc.profiler.endSection();
    }


    @Overwrite
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano)
    {
        RenderGlobal renderglobal = this.mc.renderGlobal;
        ParticleManager particlemanager = this.mc.effectRenderer;
        boolean flag = this.isDrawBlockOutline();
        GlStateManager.enableCull();
        this.mc.profiler.endStartSection("clear");
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        this.updateFogColor(partialTicks);
        GlStateManager.clear(16640);
        this.mc.profiler.endStartSection("camera");
        this.setupCameraTransform(partialTicks, pass);
        ActiveRenderInfo.updateRenderInfo(this.mc.getRenderViewEntity(), this.mc.gameSettings.thirdPersonView == 2); //Forge: MC-46445 Spectator mode particles and sounds computed from where you have been before
        this.mc.profiler.endStartSection("frustum");
        ClippingHelperImpl.getInstance();


        this.mc.profiler.endStartSection("culling");
        ICamera icamera = new Frustum();
        Entity entity = this.mc.getRenderViewEntity();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
        icamera.setPosition(d0, d1, d2);


        if (this.mc.gameSettings.renderDistanceChunks >= 4)
        {
            this.setupFog(-1, partialTicks);
            this.mc.profiler.endStartSection("sky");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
            GlStateManager.matrixMode(5888);
            renderglobal.renderSky(partialTicks, pass);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * MathHelper.SQRT_2);
            GlStateManager.matrixMode(5888);
        }

        this.setupFog(0, partialTicks);
        GlStateManager.shadeModel(7425);

        if (entity.posY + (double)entity.getEyeHeight() < 128.0D)
        {
            this.renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
        }

        this.mc.profiler.endStartSection("prepareterrain");
        this.setupFog(0, partialTicks);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();

        this.mc.profiler.endStartSection("terrain_setup");
        renderglobal.setupTerrain(entity, (double)partialTicks, icamera, this.frameCount++, this.mc.player.isSpectator());

        if (pass == 0 || pass == 2)
        {
            this.mc.profiler.endStartSection("updatechunks");
            this.mc.renderGlobal.updateChunks(finishTimeNano);
        }

        this.mc.profiler.endStartSection("terrain");
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        renderglobal.renderBlockLayer(BlockRenderLayer.SOLID, (double)partialTicks, pass, entity);
        GlStateManager.enableAlpha();
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, this.mc.gameSettings.mipmapLevels > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, (double)partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT, (double)partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);

        if (!this.debugView)
        {
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            this.mc.profiler.endStartSection("entities");
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
            renderglobal.renderEntities(entity, icamera, partialTicks);
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
            RenderHelper.disableStandardItemLighting();
            this.disableLightmap();
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();

        if (flag && this.mc.objectMouseOver != null && !entity.isInsideOfMaterial(Material.WATER))
        {
            EntityPlayer entityplayer = (EntityPlayer)entity;
            GlStateManager.disableAlpha();
            this.mc.profiler.endStartSection("outline");
            if (!net.minecraftforge.client.ForgeHooksClient.onDrawBlockHighlight(renderglobal, entityplayer, mc.objectMouseOver, 0, partialTicks))
                renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, partialTicks);
            GlStateManager.enableAlpha();
        }

        if (this.mc.debugRenderer.shouldRender())
        {
            this.mc.debugRenderer.renderDebug(partialTicks, finishTimeNano);
        }

        this.mc.profiler.endStartSection("destroyProgress");
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), entity, partialTicks);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableBlend();

        if (!this.debugView)
        {
            this.enableLightmap();
            this.mc.profiler.endStartSection("litParticles");
            particlemanager.renderLitParticles(entity, partialTicks);
            RenderHelper.disableStandardItemLighting();
            this.setupFog(0, partialTicks);
            this.mc.profiler.endStartSection("particles");
            particlemanager.renderParticles(entity, partialTicks);
            this.disableLightmap();
        }

        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
        this.mc.profiler.endStartSection("weather");
        this.renderRainSnow(partialTicks);
        GlStateManager.depthMask(true);
        renderglobal.renderWorldBorder(entity, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        this.setupFog(0, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);
        this.mc.profiler.endStartSection("translucent");
        renderglobal.renderBlockLayer(BlockRenderLayer.TRANSLUCENT, (double)partialTicks, pass, entity);
        if (!this.debugView) //Only render if render pass 0 happens as well.
        {
            RenderHelper.enableStandardItemLighting();
            this.mc.profiler.endStartSection("entities");
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(1);
            renderglobal.renderEntities(entity, icamera, partialTicks);
            // restore blending function changed by RenderGlobal.preRenderDamagedBlocks
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(-1);
            RenderHelper.disableStandardItemLighting();
        }
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        if (entity.posY + (double)entity.getEyeHeight() >= 128.0D)
        {
            this.mc.profiler.endStartSection("aboveClouds");
            this.renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
        }

        this.mc.profiler.endStartSection("forge_render_last");
        net.minecraftforge.client.ForgeHooksClient.dispatchRenderLast(renderglobal, partialTicks);

        this.mc.profiler.endStartSection("hand");

        if (this.renderHand)
        {
            GlStateManager.clear(256);
            this.renderHand(partialTicks, pass);
        }
    }*/
}
