package ohos.agp.render.render3d.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.render.render3d.BuildConfig;
import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.Scene;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.TargetBuffer;
import ohos.agp.render.render3d.ViewHolder;
import ohos.agp.render.render3d.gltf.GltfExporter;
import ohos.agp.render.render3d.gltf.GltfLoader;
import ohos.agp.render.render3d.impl.CoreDeviceCreateInfo;
import ohos.agp.render.render3d.impl.CoreEcs;
import ohos.agp.render.render3d.impl.CoreGraphicsContext;
import ohos.agp.render.render3d.impl.CoreRenderNodeGraphLoader;
import ohos.agp.render.render3d.impl.CoreRenderNodeGraphManager;
import ohos.agp.render.render3d.impl.CoreSystemGraphLoader;
import ohos.agp.render.render3d.resources.ResourceManager;
import ohos.agp.render.render3d.util.MeshBuilder;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.TouchEvent;

/* access modifiers changed from: package-private */
public final class EngineImpl implements Engine {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: EngineImpl");
    private AgpContextImpl mAgpContext;
    private TargetBuffer[] mDefaultTargetBuffers;
    private EventRunner mEventRunner;
    private final GltfExporter mGltfExporter = new GltfExporterImpl(this);
    private final GltfLoader mGltfLoader = new GltfLoaderImpl(this);
    private EventHandler mHandler;
    private final CoreRenderNodeGraphInput mRenderInput = new CoreRenderNodeGraphInput();
    private final Map<String, CoreRenderHandle> mRenderNodeGraphs = new HashMap(0);
    private ResourceManagerImpl mResourceManager;
    private SceneImpl mScene;

    private static class TimeImpl implements Engine.Time {
        private static final long MILLIS_TO_MICROS = 1000;
        private final long mDeltaTimeMicros;
        private final long mTotalTimeMicros;

        TimeImpl(CoreEngineTime coreEngineTime) {
            this.mDeltaTimeMicros = coreEngineTime.getDeltaTime().longValue() * 1000;
            this.mTotalTimeMicros = coreEngineTime.getTotalTime().longValue() * 1000;
        }

        @Override // ohos.agp.render.render3d.Engine.Time
        public long getDeltaTimeMicros() {
            return this.mDeltaTimeMicros;
        }

        @Override // ohos.agp.render.render3d.Engine.Time
        public long getTotalTimeMicros() {
            return this.mTotalTimeMicros;
        }
    }

    EngineImpl() {
    }

    @Override // ohos.agp.render.render3d.Engine
    public boolean init(Context context, Engine.RenderBackend renderBackend, Map<String, Object> map, String str) {
        HiLog.info(LABEL, "init: ", new Object[0]);
        this.mEventRunner = EventRunner.create();
        this.mHandler = new EventHandler(this.mEventRunner);
        this.mAgpContext = AgpContextImpl.createAgpContext(context.getApplicationContext(), "agp_java_engine", 0, 0, 1);
        if (renderBackend == Engine.RenderBackend.OPEN_GL_ES) {
            this.mAgpContext.initDevice(CoreDeviceCreateInfo.CoreBackend.OPENGLES, map);
        } else if (renderBackend == Engine.RenderBackend.VULKAN) {
            this.mAgpContext.initDevice(CoreDeviceCreateInfo.CoreBackend.VULKAN, map);
        } else {
            HiLog.error(LABEL, "Unsupported backed type: %{public}s.", new Object[]{renderBackend.name()});
            release();
            return false;
        }
        if (this.mAgpContext.getDevice() == null) {
            HiLog.error(LABEL, "Device initialization failed.", new Object[0]);
            release();
            return false;
        }
        this.mAgpContext.init();
        CoreSystemGraphLoader createSystemGraphLoader = Core.createSystemGraphLoader();
        if (createSystemGraphLoader == null) {
            return false;
        }
        CoreSystemGraphLoader.CoreLoadResult load = createSystemGraphLoader.load(this.mAgpContext.getEngine().getFileManager(), str, this.mAgpContext.getGraphicsContext().getEcs(), this.mAgpContext.getEngine().getPluginRegister().getComponentManagerMetadata(), this.mAgpContext.getEngine().getPluginRegister().getSystemMetadata());
        if (!load.getSuccess()) {
            HiLog.error(LABEL, "Loading system graph failed: %{public}s.", new Object[]{load.getError()});
            release();
            return false;
        }
        this.mAgpContext.getGraphicsContext().getEcs().initialize();
        this.mResourceManager = new ResourceManagerImpl(this);
        this.mScene = new SceneImpl(this);
        return true;
    }

    @Override // ohos.agp.render.render3d.Engine
    public void reset() {
        requireRenderThread();
        AgpContextImpl agpContextImpl = this.mAgpContext;
        if (agpContextImpl != null) {
            CoreEcs ecs = agpContextImpl.getGraphicsContext().getEcs();
            ecs.uninitialize();
            ecs.initialize();
            return;
        }
        throw new IllegalStateException("Not initialized or already released.");
    }

    @Override // ohos.agp.render.render3d.Engine
    public void release() {
        AgpContextImpl agpContextImpl = this.mAgpContext;
        if (agpContextImpl != null) {
            agpContextImpl.getGraphicsContext().getEcs().uninitialize();
            setDefaultTargetBuffer(null);
        }
        this.mScene = null;
        AgpContextImpl agpContextImpl2 = this.mAgpContext;
        if (agpContextImpl2 != null) {
            agpContextImpl2.release();
            this.mAgpContext = null;
        }
    }

    @Override // ohos.agp.render.render3d.Engine
    public void resourceCleanup() {
        requireRenderThread();
        AgpContextImpl agpContextImpl = this.mAgpContext;
        if (agpContextImpl != null) {
            agpContextImpl.getEngine().getGpuResourceManager().waitForIdleAndDestroyGpuResources();
            return;
        }
        throw new IllegalStateException("Not initialized or already released.");
    }

    @Override // ohos.agp.render.render3d.Engine
    public String getVersion() {
        return CoreEngine.getVersion();
    }

    @Override // ohos.agp.render.render3d.Engine
    public boolean isDebugBuild() {
        return CoreEngine.isDebugBuild();
    }

    @Override // ohos.agp.render.render3d.Engine
    public void setDefaultTargetBuffer(TargetBuffer targetBuffer) {
        setDefaultTargetBuffers(new TargetBuffer[]{targetBuffer});
    }

    @Override // ohos.agp.render.render3d.Engine
    public void setDefaultTargetBuffers(TargetBuffer[] targetBufferArr) {
        if (targetBufferArr == null || targetBufferArr[0] == null) {
            this.mDefaultTargetBuffers = null;
        } else if (targetBufferArr.length <= 1) {
            this.mDefaultTargetBuffers = targetBufferArr;
        } else {
            throw new UnsupportedOperationException("");
        }
        requestRender();
    }

    @Override // ohos.agp.render.render3d.Engine
    public Engine.Time getEngineTime() {
        return new TimeImpl(this.mAgpContext.getEngine().getEngineTime());
    }

    @Override // ohos.agp.render.render3d.Engine
    public Engine.RenderNodeGraph loadRenderNodeGraph(String str) {
        requireRenderThread();
        CoreRenderHandle coreRenderHandle = this.mRenderNodeGraphs.get(str);
        if (coreRenderHandle == null) {
            CoreRenderNodeGraphLoader createRenderNodeGraphLoader = Core.createRenderNodeGraphLoader();
            if (createRenderNodeGraphLoader == null) {
                return new RenderNodeGraphImpl(this, null);
            }
            CoreRenderNodeGraphLoader.CoreLoadResult load = createRenderNodeGraphLoader.load(this.mAgpContext.getEngine().getFileManager(), str);
            if (!load.getSuccess()) {
                HiLog.error(LABEL, "Loading render node graph failed: %{public}s.", new Object[]{load.getError()});
                return new RenderNodeGraphImpl(this, null);
            }
            coreRenderHandle = this.mAgpContext.getEngine().getRenderNodeGraphManager().create(CoreRenderNodeGraphManager.CoreRenderNodeGraphUsageType.RENDER_NODE_GRAPH_STATIC, createRenderNodeGraphLoader.getNodeGraphDescription());
            this.mRenderNodeGraphs.put(str, coreRenderHandle);
        }
        return new RenderNodeGraphImpl(this, coreRenderHandle);
    }

    @Override // ohos.agp.render.render3d.Engine
    public void setRenderMode(Engine.RenderMode renderMode) {
        CoreEcs ecs = this.mAgpContext.getGraphicsContext().getEcs();
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$Engine$RenderMode[renderMode.ordinal()];
        if (i == 1) {
            ecs.setRenderMode(CoreEcs.CoreRenderMode.RENDER_IF_DIRTY);
        } else if (i == 2) {
            ecs.setRenderMode(CoreEcs.CoreRenderMode.RENDER_ALWAYS);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // ohos.agp.render.render3d.Engine
    public boolean update() {
        requireRenderThread();
        TargetBuffer[] targetBufferArr = this.mDefaultTargetBuffers;
        if (targetBufferArr == null || targetBufferArr.length == 0 || !targetBufferArr[0].isBufferAvailable()) {
            return false;
        }
        return Core.tickFrame(this.mAgpContext.getEngine(), this.mAgpContext.getGraphicsContext().getEcs());
    }

    @Override // ohos.agp.render.render3d.Engine
    public void requestRender() {
        this.mAgpContext.getGraphicsContext().getEcs().requestRender();
    }

    @Override // ohos.agp.render.render3d.Engine
    public void renderFrame(Engine.RenderNodeGraph renderNodeGraph) {
        requireRenderThread();
        if (renderNodeGraph != null && renderNodeGraph.isValid()) {
            CoreEngine engine = this.mAgpContext.getEngine();
            if (renderNodeGraph instanceof RenderNodeGraphImpl) {
                this.mRenderInput.setRenderNodeGraphHandle(((RenderNodeGraphImpl) renderNodeGraph).getNativeHandle());
                engine.getRenderer().renderFrame(this.mRenderInput);
                return;
            }
            throw new IllegalArgumentException();
        }
    }

    @Override // ohos.agp.render.render3d.Engine
    public void renderFrame(Engine.RenderNodeGraphType renderNodeGraphType) {
        CoreGraphicsContext.CoreRenderNodeGraphType coreRenderNodeGraphType;
        requireRenderThread();
        if (renderNodeGraphType != null) {
            int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$Engine$RenderNodeGraphType[renderNodeGraphType.ordinal()];
            if (i == 1) {
                coreRenderNodeGraphType = CoreGraphicsContext.CoreRenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE;
            } else if (i == 2) {
                coreRenderNodeGraphType = CoreGraphicsContext.CoreRenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE_MSAA;
            } else if (i == 3) {
                coreRenderNodeGraphType = CoreGraphicsContext.CoreRenderNodeGraphType.HIGH_DEFINITION_RENDERING_PIPELINE;
            } else {
                throw new IllegalArgumentException();
            }
            this.mAgpContext.getGraphicsContext().renderFrame(coreRenderNodeGraphType);
        }
    }

    /* renamed from: ohos.agp.render.render3d.impl.EngineImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$Engine$RenderMode = new int[Engine.RenderMode.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$Engine$RenderNodeGraphType = new int[Engine.RenderNodeGraphType.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$render$render3d$Engine$RenderNodeGraphType[Engine.RenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$Engine$RenderNodeGraphType[Engine.RenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE_MSAA.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$Engine$RenderNodeGraphType[Engine.RenderNodeGraphType.HIGH_DEFINITION_RENDERING_PIPELINE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$Engine$RenderMode[Engine.RenderMode.RENDER_IF_DIRTY.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$Engine$RenderMode[Engine.RenderMode.ALWAYS.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    @Override // ohos.agp.render.render3d.Engine
    public void activateContext() {
        requireRenderThread();
        getAgpContext().activateGlContext();
    }

    @Override // ohos.agp.render.render3d.Engine
    public void deactivateContext() {
        requireRenderThread();
        getAgpContext().deactivateGlContext();
    }

    @Override // ohos.agp.render.render3d.Engine
    public boolean handleDevGuiTouchEvent(TouchEvent touchEvent) {
        CoreDevGui devGui = this.mAgpContext.getEngine().getDevGui();
        int action = touchEvent.getAction();
        if (action == 1) {
            devGui.setMousePos(touchEvent.getPointerPosition(touchEvent.getIndex()).getX(), touchEvent.getPointerPosition(touchEvent.getIndex()).getY());
            devGui.setMouseButtonState(0, true);
        } else if (action == 2) {
            devGui.setMousePos(touchEvent.getPointerPosition(touchEvent.getIndex()).getX(), touchEvent.getPointerPosition(touchEvent.getIndex()).getY());
            devGui.setMouseButtonState(0, false);
        } else if (action == 3) {
            devGui.setMousePos(touchEvent.getPointerPosition(touchEvent.getIndex()).getX(), touchEvent.getPointerPosition(touchEvent.getIndex()).getY());
        }
        return devGui.wantCaptureMouse();
    }

    @Override // ohos.agp.render.render3d.Engine
    public ResourceManager getResourceManager() {
        return this.mResourceManager;
    }

    @Override // ohos.agp.render.render3d.Engine
    public Scene getScene() {
        return this.mScene;
    }

    @Override // ohos.agp.render.render3d.Engine
    public GltfLoader getGltfLoader() {
        return this.mGltfLoader;
    }

    @Override // ohos.agp.render.render3d.Engine
    public GltfExporter getGltfExporter() {
        return this.mGltfExporter;
    }

    @Override // ohos.agp.render.render3d.Engine
    public MeshBuilder createMeshBuilder(int i) {
        return new MeshBuilderImpl(this, i);
    }

    @Override // ohos.agp.render.render3d.Engine
    public ViewHolder createViewHolder(SurfaceProvider surfaceProvider) {
        return new SurfaceViewHolderImpl(surfaceProvider, this.mAgpContext);
    }

    @Override // ohos.agp.render.render3d.Engine
    public void requireRenderThread() {
        if (BuildConfig.DEBUG && EventRunner.current() == null) {
            HiLog.error(LABEL, "EventRunner.current() is empty.", new Object[0]);
        }
    }

    @Override // ohos.agp.render.render3d.Engine
    public void postInRenderThread(Runnable runnable) {
        this.mHandler.postTask(runnable);
    }

    @Override // ohos.agp.render.render3d.Engine
    public void runInRenderThread(Runnable runnable) {
        if (EventRunner.current() != null) {
            runnable.run();
        } else {
            this.mHandler.postTask(runnable);
        }
    }

    /* access modifiers changed from: package-private */
    public Optional<? extends SceneNode> getNode(CoreSceneNode coreSceneNode) {
        return this.mScene.getNode(coreSceneNode);
    }

    /* access modifiers changed from: package-private */
    public AgpContextImpl getAgpContext() {
        return this.mAgpContext;
    }

    /* access modifiers changed from: package-private */
    public CoreEntity getNativeEntity(Entity entity) {
        return EntityImpl.getNativeEntity(entity);
    }

    /* access modifiers changed from: package-private */
    public Entity getEntity(CoreEntity coreEntity) {
        return EntityImpl.getEntity(this.mScene, coreEntity);
    }

    static class RenderNodeGraphImpl implements Engine.RenderNodeGraph {
        private CoreRenderHandle mRenderNodeGraph;

        RenderNodeGraphImpl(EngineImpl engineImpl, CoreRenderHandle coreRenderHandle) {
            this.mRenderNodeGraph = coreRenderHandle;
        }

        /* access modifiers changed from: package-private */
        public CoreRenderHandle getNativeHandle() {
            return this.mRenderNodeGraph;
        }

        @Override // ohos.agp.render.render3d.Engine.RenderNodeGraph
        public boolean isValid() {
            CoreRenderHandle coreRenderHandle = this.mRenderNodeGraph;
            if (coreRenderHandle == null) {
                return false;
            }
            return CoreRenderHandleUtil.isValid(coreRenderHandle);
        }

        @Override // ohos.agp.render.render3d.Engine.RenderNodeGraph
        public void release() {
            this.mRenderNodeGraph = null;
        }
    }
}
