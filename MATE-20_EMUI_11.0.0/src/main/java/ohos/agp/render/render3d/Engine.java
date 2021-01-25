package ohos.agp.render.render3d;

import java.util.Map;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.render.render3d.gltf.GltfExporter;
import ohos.agp.render.render3d.gltf.GltfLoader;
import ohos.agp.render.render3d.resources.ResourceManager;
import ohos.agp.render.render3d.util.MeshBuilder;
import ohos.app.Context;
import ohos.multimodalinput.event.TouchEvent;

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

    ViewHolder createViewHolder(SurfaceProvider surfaceProvider);

    void deactivateContext();

    Time getEngineTime();

    GltfExporter getGltfExporter();

    GltfLoader getGltfLoader();

    ResourceManager getResourceManager();

    Scene getScene();

    String getVersion();

    boolean handleDevGuiTouchEvent(TouchEvent touchEvent);

    boolean init(Context context, RenderBackend renderBackend, Map<String, Object> map, String str);

    boolean isDebugBuild();

    RenderNodeGraph loadRenderNodeGraph(String str);

    void postInRenderThread(Runnable runnable);

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

    boolean update();

    public interface RenderNodeGraph {
        boolean isValid();

        void release();

        static boolean isValid(RenderNodeGraph renderNodeGraph) {
            if (renderNodeGraph == null) {
                return false;
            }
            return renderNodeGraph.isValid();
        }
    }
}
