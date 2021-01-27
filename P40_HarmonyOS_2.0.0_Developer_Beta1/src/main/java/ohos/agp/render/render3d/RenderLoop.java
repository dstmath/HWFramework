package ohos.agp.render.render3d;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.Task;
import ohos.agp.vsync.VsyncScheduler;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class RenderLoop {
    private static final float DEFAULT_DISPLAY_REFRESH_RATE = 60.0f;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: RenderLoop");
    private final Object lock = new Object();
    private final float mDisplayFps = 0.0f;
    private Engine mEngine;
    private VsyncScheduler.FrameCallback mFrameCallback;
    private FrameListener mFrameListener;
    private boolean mIsFinished = true;
    private boolean mIsPaused = false;
    private long mLastRenderedFrameTime = 0;
    private Engine.RenderNodeGraph mRenderNodeGraph;
    private Engine.RenderNodeGraphType mRenderNodeGraphType = Engine.RenderNodeGraphType.HIGH_DEFINITION_RENDERING_PIPELINE;
    private final List<Task> mTasks = new ArrayList(1);
    private ViewHolder mViewHolder;
    private int mVsyncCount = 1;

    public interface FrameListener {
        void onFrameBegin(Engine.Time time);

        void onFrameEnd(Engine.Time time);
    }

    private float getDisplayRefreshRate() {
        return DEFAULT_DISPLAY_REFRESH_RATE;
    }

    public RenderLoop(ViewHolder viewHolder, Engine engine) {
        this.mViewHolder = viewHolder;
        this.mEngine = engine;
    }

    public void setRenderNodeGraph(Engine.RenderNodeGraph renderNodeGraph) {
        if (renderNodeGraph != null) {
            this.mRenderNodeGraph = renderNodeGraph;
            this.mRenderNodeGraphType = null;
            this.mEngine.requestRender();
            return;
        }
        throw new NullPointerException();
    }

    public void setRenderNodeGraph(Engine.RenderNodeGraphType renderNodeGraphType) {
        if (renderNodeGraphType != null) {
            this.mRenderNodeGraph = null;
            this.mRenderNodeGraphType = renderNodeGraphType;
            this.mEngine.requestRender();
            return;
        }
        throw new NullPointerException();
    }

    public void startRendering() {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "startRendering", new Object[0]);
        }
        this.mIsFinished = false;
        final long displayRefreshRate = (long) (1.0E9f / getDisplayRefreshRate());
        if (this.mFrameCallback == null) {
            this.mFrameCallback = new VsyncScheduler.FrameCallback() {
                /* class ohos.agp.render.render3d.RenderLoop.AnonymousClass1 */

                @Override // ohos.agp.vsync.VsyncScheduler.FrameCallback
                public void doFrame(long j) {
                    if (!RenderLoop.this.mIsPaused) {
                        RenderLoop.this.runTasks();
                        long j2 = (displayRefreshRate * ((long) (RenderLoop.this.mVsyncCount - 1))) + (displayRefreshRate / 2);
                        long j3 = j - RenderLoop.this.mLastRenderedFrameTime;
                        if (RenderLoop.this.mVsyncCount == 0 || j3 > j2) {
                            RenderLoop.this.renderFrame();
                            RenderLoop.this.mLastRenderedFrameTime = j;
                        }
                    }
                    if (!RenderLoop.this.mIsFinished && !RenderLoop.this.mIsPaused) {
                        VsyncScheduler.getInstance().lambda$postRequestVsync$1$VsyncScheduler(this);
                    }
                }
            };
            VsyncScheduler.getInstance().lambda$postRequestVsync$1$VsyncScheduler(this.mFrameCallback);
        }
    }

    public void submitTask(Task task) {
        synchronized (this.lock) {
            task.initialize();
            this.mTasks.add(task);
        }
    }

    public boolean cancelTask(Task task) {
        boolean remove;
        synchronized (this.lock) {
            if (task.getState() == Task.State.RUNNING) {
                task.onCancel();
            }
            remove = this.mTasks.remove(task);
        }
        return remove;
    }

    public void cancelAllTasks() {
        synchronized (this.lock) {
            while (this.mTasks.size() > 0) {
                cancelTask(this.mTasks.get(0));
            }
        }
    }

    public void requestRender() {
        this.mEngine.requestRender();
    }

    public void stopRendering() {
        this.mIsFinished = true;
    }

    public void pause() {
        this.mIsPaused = true;
    }

    public void resume() {
        this.mIsPaused = false;
    }

    public void setFrameListener(FrameListener frameListener) {
        this.mFrameListener = frameListener;
    }

    public void setVsyncCount(int i) {
        this.mVsyncCount = i;
    }

    public int getVsyncCount() {
        return this.mVsyncCount;
    }

    public void release() {
        cancelAllTasks();
        stopRendering();
        this.mEngine.resourceCleanup();
        this.mRenderNodeGraph = null;
        this.mEngine = null;
        this.mViewHolder = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void renderFrame() {
        boolean z;
        this.mEngine.requireRenderThread();
        if (this.mViewHolder.getTargetBuffer().isBufferAvailable()) {
            FrameListener frameListener = this.mFrameListener;
            if (frameListener != null) {
                frameListener.onFrameBegin(this.mEngine.getEngineTime());
            }
            if (this.mRenderNodeGraph != null) {
                z = this.mEngine.update();
            } else {
                z = this.mRenderNodeGraphType != null;
            }
            if (z) {
                Engine.RenderNodeGraph renderNodeGraph = this.mRenderNodeGraph;
                if (renderNodeGraph != null) {
                    this.mEngine.renderFrame(renderNodeGraph);
                } else {
                    Engine.RenderNodeGraphType renderNodeGraphType = this.mRenderNodeGraphType;
                    if (renderNodeGraphType != null) {
                        this.mEngine.renderFrame(renderNodeGraphType);
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }
            FrameListener frameListener2 = this.mFrameListener;
            if (frameListener2 != null) {
                frameListener2.onFrameEnd(this.mEngine.getEngineTime());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void runTasks() {
        ArrayList<Task> arrayList;
        synchronized (this.lock) {
            arrayList = new ArrayList(this.mTasks);
        }
        ArrayList<Task> arrayList2 = new ArrayList(arrayList.size());
        for (Task task : arrayList) {
            if (task.onExecute()) {
                arrayList2.add(task);
            }
        }
        synchronized (this.lock) {
            this.mTasks.removeAll(arrayList2);
        }
        for (Task task2 : arrayList2) {
            task2.onFinish();
        }
    }
}
