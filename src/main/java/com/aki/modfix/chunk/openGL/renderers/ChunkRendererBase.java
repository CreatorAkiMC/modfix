/**
 * Thank you Meldexun.
 * */
package com.aki.modfix.chunk.openGL.renderers;

import com.aki.mcutils.APICore.Utils.math.MathHelper;
import com.aki.mcutils.APICore.Utils.render.Frustum;
import com.aki.mcutils.APICore.program.shader.ShaderHelper;
import com.aki.mcutils.APICore.program.shader.ShaderObject;
import com.aki.mcutils.APICore.program.shader.ShaderProgram;
import com.aki.modfix.chunk.ChunkRenderManager;
import com.aki.modfix.chunk.GLSytem.GLMutableArrayBuffer;
import com.aki.modfix.chunk.GLSytem.GlCommandBuffer;
import com.aki.modfix.chunk.GLSytem.GlDynamicVBO;
import com.aki.modfix.chunk.GLSytem.GlVertexOffsetBuffer;
import com.aki.modfix.chunk.openGL.ChunkGLDispatcher;
import com.aki.modfix.chunk.openGL.ChunkRender;
import com.aki.modfix.chunk.openGL.ChunkRenderProvider;
import com.aki.modfix.chunk.openGL.RenderEngineType;
import com.aki.modfix.util.gl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL15;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ChunkRendererBase<T extends ChunkRender> {
    /**
     * ChunkRender処理用
     */
    private final Queue<ChunkRender> chunkQueue = new ArrayDeque<>();
    private double lastTransparencyResortX;
    private double lastTransparencyResortY;
    private double lastTransparencyResortZ;
    private int renderedChunks;
    protected final LinkedHashMap<ChunkRenderPass, List<ChunkRender>> RenderChunks = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.values(), i -> new ArrayList<>());

    public RTList<LinkedHashMap<ChunkRenderPass, GLMutableArrayBuffer>> VaoBuffers;
    public LinkedHashMap<ChunkRenderPass, GlDynamicVBO> DynamicBuffers;
    public RTList<LinkedHashMap<ChunkRenderPass, GlCommandBuffer>> CommandBuffers;
    public RTList<LinkedHashMap<ChunkRenderPass, GlVertexOffsetBuffer>> OffsetBuffers;

    public String A_POS = "a_pos";
    public String A_COLOR = "a_color";
    public String A_TEXCOORD = "a_TexCoord";
    public String A_LIGHTCOORD = "a_LightCoord";
    public String A_OFFSET = "a_offset";

    public ShaderProgram program;

    public RTList<Integer> SyncList;

    public ChunkRendererBase() {
        program = getProgram();

        //SyncListのように実装
        this.VaoBuffers = new RTList<>(2, 0, i -> MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i2 -> new GLMutableArrayBuffer()));
        this.DynamicBuffers = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i -> new GlDynamicVBO());
        this.SyncList = new RTList<>(2, 0, i -> -1);
    }

    public ShaderProgram getProgram() {
        ShaderProgram Cprogram = new ShaderProgram();
        try
        {
            Cprogram.attachShader(new ShaderObject(ShaderObject.ShaderType.VERTEX, ShaderHelper.readShader(ShaderHelper.getStream("/assets/modfix/shaders/ChunkGL_v.glsl"))));
            Cprogram.attachShader(new ShaderObject(ShaderObject.ShaderType.FRAGMENT, ShaderHelper.readShader(ShaderHelper.getStream("/assets/modfix/shaders/ChunkGL_f.glsl"))));
        } catch(IOException e)
        {
            e.printStackTrace();
        }

        return Cprogram;
    }

    public abstract RenderEngineType getRenderEngine();

    public final void Render(ChunkRenderPass pass) {
        try {
            RenderHelper.disableStandardItemLighting();
            Minecraft.getMinecraft().entityRenderer.enableLightmap();

            program.useShader(cache -> {
                //マッピング用
                cache.glUniform1I("u_BlockTex", 0);
                cache.glUniform1I("u_LightTex", 1);
            });
            this.RenderChunks(pass);

            program.releaseShader();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            GlStateManager.resetColor();
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
        }
    }

    /**
     * Program Started
     * */
    public abstract void RenderChunks(ChunkRenderPass pass);

    public abstract void Init(int renderDist);

    public void SetUP(ChunkRenderProvider<ChunkRender> provider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int Frame) {
        this.resortTransparency(cameraX, cameraY, cameraZ, frustum);

        int chunkX = MathUtil.floor(cameraX) >> 4;
        int chunkY = MathUtil.floor(cameraY) >> 4;
        int chunkZ = MathUtil.floor(cameraZ) >> 4;

        renderedChunks = 0;
        RenderChunks.values().forEach(List::clear);
        ChunkRender rootRenderChunk = provider.getRenderChunkAt(chunkX, chunkY, chunkZ);
        rootRenderChunk.VisibleDirections = 0x3F;

        int ID = 0;

        chunkQueue.add(rootRenderChunk);

        double fogEndSqr = GLFogUtils.calculateFogEndSqr();
        boolean spectator = isSpectator();

        ChunkRender renderChunk;
        while ((renderChunk = chunkQueue.poll()) != null) {

            renderChunk.lastRecordedTime = Frame;
            renderChunk = renderChunk.setID(ID++);
            renderChunk.ChunkRenderCompileAsync((ChunkRendererBase<ChunkRender>) this, ChunkRenderManager.getRenderDispatcher());
            addToRenderLists(renderChunk);

            for (EnumFacing facing : EnumFacing.VALUES) {
                ChunkRender neighbor = provider.getNeighbor(renderChunk, facing);
                if (neighbor == null)
                    continue;
                if (neighbor.lastRecordedTime == Frame)
                    continue;
                if (EnumFacingCulledHelper.isFaceCulled(facing.getOpposite(), neighbor, cameraX, cameraY, cameraZ))
                    continue;
                if (!spectator && !renderChunk.isVisibleFromAnyOrigin(facing))
                    continue;
                if (neighbor.lastEnqueuedTime != Frame) {
                    neighbor.lastEnqueuedTime = Frame;

                    if (neighbor.isFogCulled(cameraX, cameraY, cameraZ, fogEndSqr) || (frustum != null && neighbor.isFrustumCulled(frustum))) {
                        neighbor.lastRecordedTime = Frame;
                        continue;
                    }

                    neighbor.resetOrigins();
                    chunkQueue.add(neighbor);
                }
                neighbor.setOrigin(facing.getOpposite());
            }
        }
    }

    public int getRenderedChunks(ChunkRenderPass pass) {
        return this.RenderChunks.get(pass).size();
    }

    public int getAllPassRenderChunks() {
        AtomicInteger count = new AtomicInteger();
        count.set(0);
        this.RenderChunks.values().forEach(l -> count.addAndGet(l.size()));
        return count.get();
    }

    public int getRenderedChunks() {
        return renderedChunks;
    }

    private void resortTransparency(double cameraX, double cameraY, double cameraZ, Frustum frustum) {
        double dx = cameraX - lastTransparencyResortX;
        double dy = cameraY - lastTransparencyResortY;
        double dz = cameraZ - lastTransparencyResortZ;
        if (dx * dx + dy * dy + dz * dz > 1.0D) {
            lastTransparencyResortX = cameraX;
            lastTransparencyResortY = cameraY;
            lastTransparencyResortZ = cameraZ;

            ChunkRenderProvider<ChunkRender> provider = ChunkRenderManager.getRenderProvider();
            ChunkGLDispatcher taskDispatcher = ChunkRenderManager.getRenderDispatcher();
            int chunkX = MathHelper.floor(cameraX) >> 4;
            int chunkY = MathHelper.floor(cameraY) >> 4;
            int chunkZ = MathHelper.floor(cameraZ) >> 4;
            int r = 2;
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        ChunkRender renderChunk = provider.getRenderChunkAt(chunkX + x, chunkY + y, chunkZ + z);
                        if (renderChunk == null)
                            continue;
                        if (renderChunk.isFrustumCulled(frustum))
                            continue;
                        renderChunk.ChunkRenderResortTransparency((ChunkRendererBase<ChunkRender>) this, taskDispatcher);
                    }
                }
            }
        }
    }

    private void addToRenderLists(ChunkRender renderChunk) {
        if (renderChunk.IsVBOEmpty()) {
            return;
        }
        renderedChunks++;
        RenderChunks.forEach((pass, list) -> {
            if (renderChunk.getVBO(pass) != null) {
                list.add(renderChunk);
            }
        });
    }

    public void deleteDatas() {
        this.VaoBuffers.forEach((index, map) -> map.values().forEach(GLMutableArrayBuffer::delete));
        this.DynamicBuffers.values().forEach(GlDynamicVBO::Delete);

        if(program != null)
            program.Delete();

        if(this.CommandBuffers != null)
            this.CommandBuffers.forEach((index, map) -> map.values().forEach(GlCommandBuffer::delete));
        if(this.OffsetBuffers != null)
            this.OffsetBuffers.forEach((index, map) -> map.values().forEach(GlVertexOffsetBuffer::delete));

        this.SyncList.getList().stream().filter(i -> i != -1).forEach(GL15::glDeleteQueries);
    }

    public GlDynamicVBO.VBOPart buffer(ChunkRenderPass pass, ChunkRender render, ByteBuffer buffer) {
        return this.DynamicBuffers.get(pass).Buf_Upload(render, buffer);
    }

    protected boolean isSpectator() {
        Minecraft mc = Minecraft.getMinecraft();
        Entity cameraEntity = mc.getRenderViewEntity();
        return cameraEntity instanceof EntityPlayer && ((EntityPlayer) cameraEntity).isSpectator();
    }
}
