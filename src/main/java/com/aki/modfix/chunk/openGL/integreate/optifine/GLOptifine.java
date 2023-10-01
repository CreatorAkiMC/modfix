package com.aki.modfix.chunk.openGL.integreate.optifine;

import com.aki.modfix.MixinModLoadConfig;
import com.aki.modfix.chunk.ChunkRenderManager;
import com.aki.modfix.chunk.openGL.ChunkRender;
import com.aki.modfix.chunk.openGL.RenderEngineType;
import com.aki.modfix.chunk.openGL.renderers.ChunkRendererBase;
import com.aki.modfix.util.reflectors.ReflectionField;
import com.aki.modfix.util.reflectors.ReflectionMethod;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class GLOptifine {
    public static final boolean OPTIFINE_INSIDE;
    static {
        boolean flag = false;
        try {
            Class.forName("optifine.OptiFineClassTransformer", false, MixinModLoadConfig.class.getClassLoader());
            flag = true;
        } catch (ClassNotFoundException e) {
            // ignore
        }
        OPTIFINE_INSIDE = flag;
    }

    public static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
    public static final ReflectionMethod<Boolean> IS_FOG_OFF = new ReflectionMethod<>("Config", "isFogOff", "isFogOff");
    public static final ReflectionField<Boolean> FOG_STANDARD = new ReflectionField<>("net.minecraft.client.renderer.EntityRenderer", "fogStandard", "fogStandard");
    public static final ReflectionMethod<Boolean> IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
    public static final ReflectionMethod<Boolean> IS_DYNAMIC_LIGHTS = new ReflectionMethod<>("Config", "isDynamicLights", "isDynamicLights");
    public static final ReflectionMethod<Boolean> IS_DYNAMIC_LIGHTS_FAST = new ReflectionMethod<>("Config", "isDynamicLightsFast", "isDynamicLightsFast");
    public static final ReflectionMethod<Integer> GET_LIGHT_LEVEL = new ReflectionMethod<>("net.optifine.DynamicLights", "getLightLevel", "getLightLevel", Entity.class);
    public static final ReflectionMethod<Integer> GET_COMBINED_LIGHT = new ReflectionMethod<>("net.optifine.DynamicLights", "getCombinedLight", "getCombinedLight", BlockPos.class, int.class);
    public static final ReflectionMethod<Void> DYNAMIC_LIGHTS_UPDATE = new ReflectionMethod<>("net.optifine.DynamicLights", "update", "update", RenderGlobal.class);
    public static final ReflectionMethod<Void> PRE_RENDER_CHUNK_LAYER = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "preRenderChunkLayer", "preRenderChunkLayer", BlockRenderLayer.class);
    public static final ReflectionMethod<Void> SETUP_ARRAY_POINTERS_VBO = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "setupArrayPointersVbo", "setupArrayPointersVbo");
    public static final ReflectionMethod<Void> POST_RENDER_CHUNK_LAYER = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "postRenderChunkLayer", "postRenderChunkLayer", BlockRenderLayer.class);

    public static ChunkRendererBase<ChunkRender> createChunkRenderer(@Nullable ChunkRendererBase<ChunkRender> oldChunkRenderer) {
        RenderEngineType renderEngine = IS_SHADERS.invoke(null) ? RenderEngineType.GL15 : ChunkRenderManager.getBestRenderEngineType();//Configで設定変更可にしてもいいかも
        if (oldChunkRenderer != null && oldChunkRenderer.getRenderEngine() != renderEngine) {
            oldChunkRenderer.deleteDatas();
            oldChunkRenderer = null;
        }
        if (oldChunkRenderer != null) {
            return oldChunkRenderer;
        }

        switch (renderEngine) {
            case GL43:
                return new ChunkRendererGL43Optifine();
            case GL42:
                return new ChunkRendererGL42Optifine();
            case GL20:
                return new ChunkRendererGL20Optifine();
            case GL15:
                return new ChunkRendererGL15Optifine();
            default:
                throw new UnsupportedOperationException();
        }
    }
}
