package com.huawei.agpengine.impl;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import com.huawei.agpengine.BuildConfig;
import com.huawei.agpengine.Engine;
import com.huawei.agpengine.Entity;
import com.huawei.agpengine.Scene;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.TargetBuffer;
import com.huawei.agpengine.ViewHolder;
import com.huawei.agpengine.gltf.GltfExporter;
import com.huawei.agpengine.gltf.GltfLoader;
import com.huawei.agpengine.impl.CoreDeviceCreateInfo;
import com.huawei.agpengine.impl.CoreEcs;
import com.huawei.agpengine.impl.CoreGraphicsContext;
import com.huawei.agpengine.impl.CoreRenderNodeGraphLoader;
import com.huawei.agpengine.impl.CoreRenderNodeGraphManager;
import com.huawei.agpengine.impl.CoreSystemGraphLoader;
import com.huawei.agpengine.resources.ResourceManager;
import com.huawei.agpengine.util.MeshBuilder;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/* access modifiers changed from: package-private */
public final class EngineImpl implements Engine {
    static final String INTERNAL_ERROR = "Internal graphics engine error";
    private static final long INVALID_HANDLE = 4294967295L;
    private static final String TAG = "core: EngineImpl";
    private AgpContextImpl mAgpContext;
    private Choreographer mChoreographer;
    private TargetBuffer[] mDefaultTargetBuffers;
    private final GltfExporter mGltfExporter = new GltfExporterImpl(this);
    private final GltfLoader mGltfLoader = new GltfLoaderImpl(this);
    private Handler mHandler;
    private final CoreRenderNodeGraphInput mRenderInput = new CoreRenderNodeGraphInput();
    private final Map<String, Long> mRenderNodeGraphs = new HashMap(0);
    private Thread mRenderThread;
    private ResourceManagerImpl mResourceManager;
    private SceneImpl mScene;

    private static class TimeImpl implements Engine.Time {
        private final long mDeltaTimeMicros;
        private final long mTotalTimeMicros;

        TimeImpl(CoreEngineTime time) {
            this.mDeltaTimeMicros = time.getDeltaTimeUs().longValue();
            this.mTotalTimeMicros = time.getTotalTimeUs().longValue();
        }

        @Override // com.huawei.agpengine.Engine.Time
        public long getDeltaTimeMicros() {
            return this.mDeltaTimeMicros;
        }

        @Override // com.huawei.agpengine.Engine.Time
        public long getTotalTimeMicros() {
            return this.mTotalTimeMicros;
        }
    }

    EngineImpl() {
    }

    private boolean registerFilePath(CoreFileManager fileManager, String protocol, File path) {
        if (path == null) {
            return false;
        }
        if (!path.exists()) {
            path.mkdir();
        }
        Core.registerPath(fileManager, protocol, "file://" + path.getPath() + "/", true);
        return true;
    }

    private void registerPaths(Context context) {
        CoreFileManager fileManager = this.mAgpContext.getEngine().getFileManager();
        Core.registerPath(fileManager, "plugins", "file://" + context.getApplicationInfo().nativeLibraryDir, true);
        Core.registerApkFilesystem(fileManager, "apk", context.getAssets());
        registerFilePath(fileManager, "cache", context.getExternalCacheDir());
        registerFilePath(fileManager, "shared", context.getExternalFilesDir(null));
        registerFilePath(fileManager, "app", context.getFilesDir());
        Core.registerPath(fileManager, "assets", "apk://", true);
        Core.registerPath(fileManager, "engine", "apk://app/", true);
        Core.registerPath(fileManager, "shaders", "apk://app/shaders/", true);
    }

    @Override // com.huawei.agpengine.Engine
    public boolean init(Context context, Engine.RenderBackend backend, Map<String, Object> backendExtra, String systemGraphUri) {
        Log.i(TAG, "init: 1.1.0.100");
        if (context == null) {
            Log.i(TAG, "Init failed: context must not be null.");
            return false;
        } else if (backend == null) {
            Log.i(TAG, "Init failed: backend must not be null.");
            return false;
        } else if (systemGraphUri == null) {
            Log.i(TAG, "Init failed: systemGraphUri must not be null.");
            return false;
        } else {
            initThreading();
            this.mAgpContext = AgpContextImpl.createAgpContext(context.getApplicationContext(), "agp_java_engine", 0, 0, 1);
            if (backend == Engine.RenderBackend.OPEN_GL_ES) {
                this.mAgpContext.initDevice(CoreDeviceCreateInfo.CoreBackend.OPENGLES, backendExtra);
            } else if (backend == Engine.RenderBackend.VULKAN) {
                this.mAgpContext.initDevice(CoreDeviceCreateInfo.CoreBackend.VULKAN, backendExtra);
            } else {
                Log.e(TAG, "Unsupported backed type: " + backend.name());
                release();
                return false;
            }
            if (this.mAgpContext.getDevice() == null) {
                Log.e(TAG, "Device initialization failed.");
                release();
                return false;
            }
            registerPaths(context);
            this.mAgpContext.init();
            if (!initEcs(systemGraphUri)) {
                release();
                return false;
            }
            this.mResourceManager = new ResourceManagerImpl(this);
            this.mScene = new SceneImpl(this);
            return true;
        }
    }

    private void initThreading() {
        Looper looper;
        this.mRenderThread = Thread.currentThread();
        if (Looper.getMainLooper().isCurrentThread()) {
            looper = Looper.getMainLooper();
        } else {
            looper = Looper.myLooper();
        }
        if (looper == null) {
            Log.i(TAG, "No looper defined for this thread. Posting tasks disabled.");
            this.mHandler = null;
            this.mChoreographer = null;
            return;
        }
        this.mHandler = new Handler();
        this.mChoreographer = Choreographer.getInstance();
    }

    private boolean initEcs(String systemGraphUri) {
        CoreSystemGraphLoaderPtr systemGraphLoaderPtr = Core.createSystemGraphLoader(this.mAgpContext.getEngine().getFileManager());
        CoreSystemGraphLoader systemGraphLoader = systemGraphLoaderPtr.get();
        if (systemGraphLoader == null) {
            systemGraphLoaderPtr.delete();
            return false;
        }
        CoreSystemGraphLoader.CoreLoadResult systemGraphResult = systemGraphLoader.load(systemGraphUri, this.mAgpContext.getGraphicsContext().getEcs());
        systemGraphLoaderPtr.delete();
        if (!systemGraphResult.getSuccess()) {
            Log.e(TAG, "Loading system graph failed: " + systemGraphResult.getError());
            return false;
        }
        this.mAgpContext.getGraphicsContext().getEcs().initialize();
        return true;
    }

    private void checkInit() {
        if (this.mAgpContext == null) {
            throw new IllegalStateException("Engine not initialized.");
        }
    }

    @Override // com.huawei.agpengine.Engine
    public void registerApkFilesystem(String protocol, AssetManager assetManager) {
        checkInit();
        requireRenderThread();
        Core.registerApkFilesystem(this.mAgpContext.getEngine().getFileManager(), protocol, assetManager);
    }

    @Override // com.huawei.agpengine.Engine
    public void unregisterFilesystem(String protocol) {
        checkInit();
        requireRenderThread();
        Core.unregisterFilesystem(this.mAgpContext.getEngine().getFileManager(), protocol);
    }

    @Override // com.huawei.agpengine.Engine
    public void reset() {
        checkInit();
        requireRenderThread();
        CoreEcs ecs = this.mAgpContext.getGraphicsContext().getEcs();
        ecs.uninitialize();
        ecs.initialize();
    }

    @Override // com.huawei.agpengine.Engine
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

    @Override // com.huawei.agpengine.Engine
    public void resourceCleanup() {
        checkInit();
        requireRenderThread();
        this.mAgpContext.getEngine().getGpuResourceManager().waitForIdleAndDestroyGpuResources();
    }

    @Override // com.huawei.agpengine.Engine
    public String getVersion() {
        return Core.getVersion();
    }

    @Override // com.huawei.agpengine.Engine
    public boolean isDebugBuild() {
        return Core.isDebugBuild();
    }

    @Override // com.huawei.agpengine.Engine
    public void setDefaultTargetBuffer(TargetBuffer targetBuffer) {
        setDefaultTargetBuffers(new TargetBuffer[]{targetBuffer});
    }

    @Override // com.huawei.agpengine.Engine
    public void setDefaultTargetBuffers(TargetBuffer[] targetBuffers) {
        checkInit();
        if (targetBuffers == null || targetBuffers[0] == null) {
            this.mDefaultTargetBuffers = null;
        } else if (targetBuffers.length <= 1) {
            this.mDefaultTargetBuffers = targetBuffers;
        } else {
            throw new UnsupportedOperationException(BuildConfig.FLAVOR);
        }
        requestRender();
    }

    @Override // com.huawei.agpengine.Engine
    public Engine.Time getEngineTime() {
        checkInit();
        return new TimeImpl(this.mAgpContext.getEngine().getEngineTime());
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mAgpContext != null) {
            Log.w(TAG, "Engine.release() should be called explicitly when possible.");
        }
        runInRenderThread(new Runnable() {
            /* class com.huawei.agpengine.impl.EngineImpl.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                EngineImpl.this.release();
            }
        });
        super.finalize();
    }

    @Override // com.huawei.agpengine.Engine
    public Engine.RenderNodeGraph loadRenderNodeGraph(String renderNodeGraphUri) {
        checkInit();
        requireRenderThread();
        Long handle = this.mRenderNodeGraphs.get(renderNodeGraphUri);
        if (handle == null) {
            CoreEngine nativeEngine = this.mAgpContext.getEngine();
            CoreRenderNodeGraphLoader loader = nativeEngine.getRenderNodeGraphManager().getRenderNodeGraphLoader();
            if (loader == null) {
                return new RenderNodeGraphImpl(this, INVALID_HANDLE);
            }
            CoreRenderNodeGraphLoader.CoreLoadResult renderNodeGraphResult = loader.load(renderNodeGraphUri);
            if (!renderNodeGraphResult.getSuccess()) {
                Log.e(TAG, "Loading render node graph failed: " + renderNodeGraphResult.getError());
                return new RenderNodeGraphImpl(this, INVALID_HANDLE);
            }
            handle = Long.valueOf(nativeEngine.getRenderNodeGraphManager().create(CoreRenderNodeGraphManager.CoreRenderNodeGraphUsageType.RENDER_NODE_GRAPH_STATIC, loader.getNodeGraphDescription()));
            this.mRenderNodeGraphs.put(renderNodeGraphUri, handle);
        }
        return new RenderNodeGraphImpl(this, handle.longValue());
    }

    @Override // com.huawei.agpengine.Engine
    public void setRenderMode(Engine.RenderMode renderMode) {
        checkInit();
        CoreEcs ecs = this.mAgpContext.getGraphicsContext().getEcs();
        int i = AnonymousClass2.$SwitchMap$com$huawei$agpengine$Engine$RenderMode[renderMode.ordinal()];
        if (i == 1) {
            ecs.setRenderMode(CoreEcs.CoreRenderMode.RENDER_IF_DIRTY);
        } else if (i == 2) {
            ecs.setRenderMode(CoreEcs.CoreRenderMode.RENDER_ALWAYS);
        } else {
            throw new IllegalArgumentException(INTERNAL_ERROR);
        }
    }

    @Override // com.huawei.agpengine.Engine
    public boolean update() {
        checkInit();
        requireRenderThread();
        TargetBuffer[] targetBufferArr = this.mDefaultTargetBuffers;
        if (targetBufferArr == null || targetBufferArr.length == 0 || !targetBufferArr[0].isBufferAvailable()) {
            return false;
        }
        return Core.tickFrame(this.mAgpContext.getEngine(), this.mAgpContext.getGraphicsContext().getEcs());
    }

    @Override // com.huawei.agpengine.Engine
    public void requestRender() {
        checkInit();
        this.mAgpContext.getGraphicsContext().getEcs().requestRender();
    }

    @Override // com.huawei.agpengine.Engine
    public void renderFrame(Engine.RenderNodeGraph renderNodeGraph) {
        checkInit();
        requireRenderThread();
        if (renderNodeGraph != null && renderNodeGraph.isValid()) {
            CoreEngine engine = this.mAgpContext.getEngine();
            if (renderNodeGraph instanceof RenderNodeGraphImpl) {
                this.mRenderInput.setRenderNodeGraphHandle(((RenderNodeGraphImpl) renderNodeGraph).getNativeHandle());
                engine.getRenderer().renderFrame(this.mRenderInput);
                return;
            }
            throw new IllegalArgumentException("Unsupported implementation.");
        }
    }

    @Override // com.huawei.agpengine.Engine
    public void renderFrame(Engine.RenderNodeGraphType renderNodeGraphType) {
        CoreGraphicsContext.CoreRenderNodeGraphType nativeGraphType;
        checkInit();
        requireRenderThread();
        if (renderNodeGraphType != null) {
            int i = AnonymousClass2.$SwitchMap$com$huawei$agpengine$Engine$RenderNodeGraphType[renderNodeGraphType.ordinal()];
            if (i == 1) {
                nativeGraphType = CoreGraphicsContext.CoreRenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE;
            } else if (i == 2) {
                nativeGraphType = CoreGraphicsContext.CoreRenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE_MSAA;
            } else if (i == 3) {
                nativeGraphType = CoreGraphicsContext.CoreRenderNodeGraphType.HIGH_DEFINITION_RENDERING_PIPELINE;
            } else {
                throw new IllegalArgumentException(INTERNAL_ERROR);
            }
            long graphHandle = this.mAgpContext.getGraphicsContext().getRenderNodeGraph(nativeGraphType);
            CoreEngine engine = this.mAgpContext.getEngine();
            this.mRenderInput.setRenderNodeGraphHandle(graphHandle);
            engine.getRenderer().renderFrame(this.mRenderInput);
        }
    }

    /* renamed from: com.huawei.agpengine.impl.EngineImpl$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$Engine$RenderMode = new int[Engine.RenderMode.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$Engine$RenderNodeGraphType = new int[Engine.RenderNodeGraphType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$agpengine$Engine$RenderNodeGraphType[Engine.RenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$Engine$RenderNodeGraphType[Engine.RenderNodeGraphType.LIGHT_WEIGHT_RENDERING_PIPELINE_MSAA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$Engine$RenderNodeGraphType[Engine.RenderNodeGraphType.HIGH_DEFINITION_RENDERING_PIPELINE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$Engine$RenderMode[Engine.RenderMode.RENDER_IF_DIRTY.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$Engine$RenderMode[Engine.RenderMode.ALWAYS.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    @Override // com.huawei.agpengine.Engine
    public void activateContext() {
        requireRenderThread();
        getAgpContext().activateGlContext();
    }

    @Override // com.huawei.agpengine.Engine
    public void deactivateContext() {
        requireRenderThread();
        getAgpContext().deactivateGlContext();
    }

    @Override // com.huawei.agpengine.Engine
    public boolean handleDevGuiTouchEvent(MotionEvent event) {
        checkInit();
        CoreDevGui devGui = this.mAgpContext.getEngine().getDevGui();
        int action = event.getActionMasked();
        if (action == 0) {
            devGui.setMousePos(event.getX(), event.getY());
            devGui.setMouseButtonState(0, true);
        } else if (action == 1) {
            devGui.setMousePos(event.getX(), event.getY());
            devGui.setMouseButtonState(0, false);
        } else if (action == 2) {
            devGui.setMousePos(event.getX(), event.getY());
        }
        return devGui.wantCaptureMouse();
    }

    @Override // com.huawei.agpengine.Engine
    public ResourceManager getResourceManager() {
        checkInit();
        return this.mResourceManager;
    }

    @Override // com.huawei.agpengine.Engine
    public Scene getScene() {
        checkInit();
        return this.mScene;
    }

    @Override // com.huawei.agpengine.Engine
    public GltfLoader getGltfLoader() {
        checkInit();
        return this.mGltfLoader;
    }

    @Override // com.huawei.agpengine.Engine
    public GltfExporter getGltfExporter() {
        checkInit();
        return this.mGltfExporter;
    }

    @Override // com.huawei.agpengine.Engine
    public MeshBuilder createMeshBuilder(int primitiveCount) {
        checkInit();
        return new MeshBuilderImpl(this, primitiveCount);
    }

    @Override // com.huawei.agpengine.Engine
    public TargetBuffer createSurfaceTargetBuffer(SurfaceTexture surfaceTexture, int width, int height) {
        checkInit();
        SurfaceTargetBufferImpl targetBuffer = new SurfaceTargetBufferImpl();
        targetBuffer.init(this.mAgpContext, surfaceTexture);
        targetBuffer.updateSize(width, height);
        return targetBuffer;
    }

    @Override // com.huawei.agpengine.Engine
    public TargetBuffer createSurfaceTargetBuffer(Surface surface, int width, int height) {
        checkInit();
        SurfaceTargetBufferImpl targetBuffer = new SurfaceTargetBufferImpl();
        targetBuffer.init(this.mAgpContext, surface);
        targetBuffer.updateSize(width, height);
        return targetBuffer;
    }

    @Override // com.huawei.agpengine.Engine
    public void updateSurfaceTargetBuffer(TargetBuffer surfaceTargetBuffer, int width, int height) {
        if (surfaceTargetBuffer instanceof SurfaceTargetBufferImpl) {
            ((SurfaceTargetBufferImpl) surfaceTargetBuffer).updateSize(width, height);
            return;
        }
        throw new IllegalArgumentException("surfaceTargetBuffer must be a valid Surface target buffer.");
    }

    @Override // com.huawei.agpengine.Engine
    public ViewHolder createViewHolder(TextureView textureView) {
        checkInit();
        return new TextureViewHolderImpl(textureView, this.mAgpContext);
    }

    @Override // com.huawei.agpengine.Engine
    public ViewHolder createViewHolder(SurfaceView surfaceView) {
        checkInit();
        return new SurfaceViewHolderImpl(surfaceView, this.mAgpContext);
    }

    @Override // com.huawei.agpengine.Engine
    public Choreographer getRenderChoreographer() {
        return this.mChoreographer;
    }

    @Override // com.huawei.agpengine.Engine
    public void requireRenderThread() {
    }

    @Override // com.huawei.agpengine.Engine
    public void postInRenderThread(Runnable task) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(task);
        }
    }

    @Override // com.huawei.agpengine.Engine
    public void runInRenderThread(Runnable task) {
        if (this.mRenderThread == Thread.currentThread()) {
            task.run();
        } else {
            postInRenderThread(task);
        }
    }

    /* access modifiers changed from: package-private */
    public Optional<? extends SceneNode> getNode(CoreSceneNode nativeSceneNode) {
        checkInit();
        return this.mScene.getNode(nativeSceneNode);
    }

    /* access modifiers changed from: package-private */
    public AgpContextImpl getAgpContext() {
        checkInit();
        return this.mAgpContext;
    }

    /* access modifiers changed from: package-private */
    public Entity getEntity(int nativeEntity) {
        checkInit();
        return new EntityImpl(this.mScene, nativeEntity);
    }

    static class RenderNodeGraphImpl implements Engine.RenderNodeGraph {
        private long mRenderNodeGraph = EngineImpl.INVALID_HANDLE;

        RenderNodeGraphImpl(EngineImpl engine, long nativeHandle) {
            this.mRenderNodeGraph = nativeHandle;
        }

        /* access modifiers changed from: package-private */
        public long getNativeHandle() {
            return this.mRenderNodeGraph;
        }

        @Override // com.huawei.agpengine.Engine.RenderNodeGraph
        public boolean isValid() {
            long j = this.mRenderNodeGraph;
            if (j == EngineImpl.INVALID_HANDLE) {
                return false;
            }
            return Core.isRenderHandleValid(j);
        }

        @Override // com.huawei.agpengine.Engine.RenderNodeGraph
        public void release() {
            this.mRenderNodeGraph = EngineImpl.INVALID_HANDLE;
        }
    }
}
