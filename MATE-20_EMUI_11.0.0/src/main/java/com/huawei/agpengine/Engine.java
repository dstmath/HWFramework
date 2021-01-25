package com.huawei.agpengine;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import com.huawei.agpengine.gltf.GltfExporter;
import com.huawei.agpengine.gltf.GltfLoader;
import com.huawei.agpengine.resources.ResourceManager;
import com.huawei.agpengine.util.MeshBuilder;
import java.util.Map;

public interface Engine {

    public enum RenderBackend {
        OPEN_GL_ES,
        VULKAN
    }

    public enum RenderMode {
        RENDER_IF_DIRTY,
        ALWAYS
    }

    public enum RenderNodeGraphType {
        LIGHT_WEIGHT_RENDERING_PIPELINE,
        LIGHT_WEIGHT_RENDERING_PIPELINE_MSAA,
        HIGH_DEFINITION_RENDERING_PIPELINE
    }

    public interface Time {
        long getDeltaTimeMicros();

        long getTotalTimeMicros();
    }

    void activateContext();

    MeshBuilder createMeshBuilder(int i);

    TargetBuffer createSurfaceTargetBuffer(SurfaceTexture surfaceTexture, int i, int i2);

    TargetBuffer createSurfaceTargetBuffer(Surface surface, int i, int i2);

    ViewHolder createViewHolder(SurfaceView surfaceView);

    ViewHolder createViewHolder(TextureView textureView);

    void deactivateContext();

    Time getEngineTime();

    GltfExporter getGltfExporter();

    GltfLoader getGltfLoader();

    Choreographer getRenderChoreographer();

    ResourceManager getResourceManager();

    Scene getScene();

    String getVersion();

    boolean handleDevGuiTouchEvent(MotionEvent motionEvent);

    boolean init(Context context, RenderBackend renderBackend, Map<String, Object> map, String str);

    boolean isDebugBuild();

    RenderNodeGraph loadRenderNodeGraph(String str);

    void postInRenderThread(Runnable runnable);

    void registerApkFilesystem(String str, AssetManager assetManager);

    void release();

    void renderFrame(RenderNodeGraph renderNodeGraph);

    void renderFrame(RenderNodeGraphType renderNodeGraphType);

    void requestRender();

    void requireRenderThread();

    void reset();

    void resourceCleanup();

    void runInRenderThread(Runnable runnable);

    void setDefaultTargetBuffer(TargetBuffer targetBuffer);

    void setDefaultTargetBuffers(TargetBuffer[] targetBufferArr);

    void setRenderMode(RenderMode renderMode);

    void unregisterFilesystem(String str);

    boolean update();

    void updateSurfaceTargetBuffer(TargetBuffer targetBuffer, int i, int i2);

    public interface RenderNodeGraph {
        boolean isValid();

        void release();

        static boolean isValid(RenderNodeGraph handle) {
            if (handle == null) {
                return false;
            }
            return handle.isValid();
        }
    }
}
