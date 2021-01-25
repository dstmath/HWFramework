package com.huawei.agpengine;

import android.content.Context;
import android.util.Log;
import android.view.Choreographer;
import android.view.WindowManager;
import com.huawei.agpengine.Engine;
import com.huawei.agpengine.Task;
import java.util.ArrayList;
import java.util.List;

public class RenderLoop {
    private static final float DEFAULT_DISPLAY_REFRESH_RATE = 60.0f;
    private static final String TAG = "core: RenderLoop";
    private float mDisplayFps = 0.0f;
    private Engine mEngine;
    private Choreographer.FrameCallback mFrameCallback;
    private FrameListener mFrameListener;
    private boolean mIsFinished = true;
    private boolean mIsPaused = false;
    private long mLastRenderedFrameTime = 0;
    private Engine.RenderNodeGraph mRenderNodeGraph;
    private Engine.RenderNodeGraphType mRenderNodeGraphType = Engine.RenderNodeGraphType.HIGH_DEFINITION_RENDERING_PIPELINE;
    private List<Task> mTasks = new ArrayList(1);
    private ViewHolder mViewHolder;
    private int mVsyncCount = 1;

    public interface FrameListener {
        void onFrameBegin(Engine.Time time);

        void onFrameEnd(Engine.Time time);
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
        throw new NullPointerException("renderNodeGraph must not be null.");
    }

    public void setRenderNodeGraph(Engine.RenderNodeGraphType renderNodeGraphType) {
        if (renderNodeGraphType != null) {
            this.mRenderNodeGraph = null;
            this.mRenderNodeGraphType = renderNodeGraphType;
            this.mEngine.requestRender();
            return;
        }
        throw new NullPointerException("renderNodeGraphType must not be null.");
    }

    private float getDisplayRefreshRate() {
        ViewHolder viewHolder;
        float displayFps = this.mDisplayFps;
        if (!(displayFps != 0.0f || (viewHolder = this.mViewHolder) == null || viewHolder.getView() == null)) {
            Context context = this.mViewHolder.getView().getContext();
            WindowManager windowManager = (WindowManager) context.getSystemService(WindowManager.class);
            if (windowManager != null) {
                windowManager.getDefaultDisplay().getRefreshRate();
                Object displayManager = context.getSystemService("window");
                if (displayManager instanceof WindowManager) {
                    this.mDisplayFps = ((WindowManager) displayManager).getDefaultDisplay().getRefreshRate();
                    displayFps = this.mDisplayFps;
                }
            }
        }
        if (displayFps <= 0.0f) {
            return DEFAULT_DISPLAY_REFRESH_RATE;
        }
        return displayFps;
    }

    public void startRendering() {
        this.mIsFinished = false;
        final long displayVsyncTimeNanos = (long) (1.0E9f / getDisplayRefreshRate());
        if (this.mFrameCallback == null) {
            this.mFrameCallback = new Choreographer.FrameCallback() {
                /* class com.huawei.agpengine.RenderLoop.AnonymousClass1 */

                @Override // android.view.Choreographer.FrameCallback
                public void doFrame(long frameTimeNanos) {
                    if (!RenderLoop.this.mIsPaused) {
                        RenderLoop.this.runTasks();
                        long minFrameTimeNanos = (displayVsyncTimeNanos / 2) + (displayVsyncTimeNanos * ((long) (RenderLoop.this.mVsyncCount - 1)));
                        long nanosSinceLastFrame = frameTimeNanos - RenderLoop.this.mLastRenderedFrameTime;
                        if (RenderLoop.this.mVsyncCount == 0 || nanosSinceLastFrame > minFrameTimeNanos) {
                            RenderLoop.this.renderFrame();
                            RenderLoop.this.mLastRenderedFrameTime = frameTimeNanos;
                        }
                    }
                    if (!RenderLoop.this.mIsFinished && !RenderLoop.this.mIsPaused) {
                        RenderLoop.this.mEngine.getRenderChoreographer().postFrameCallback(this);
                    }
                }
            };
            this.mEngine.getRenderChoreographer().postFrameCallback(this.mFrameCallback);
        }
    }

    public synchronized void submitTask(Task task) {
        task.initialize();
        this.mTasks.add(task);
    }

    public synchronized boolean cancelTask(Task task) {
        if (task.getState() == Task.State.RUNNING) {
            task.onCancel();
        }
        return this.mTasks.remove(task);
    }

    public synchronized void cancelAllTasks() {
        while (this.mTasks.size() > 0) {
            cancelTask(this.mTasks.get(0));
        }
    }

    public void requestRender() {
        this.mEngine.requestRender();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void renderFrame() {
        this.mEngine.requireRenderThread();
        ViewHolder viewHolder = this.mViewHolder;
        if (viewHolder != null && viewHolder.getTargetBuffer().isBufferAvailable()) {
            FrameListener frameListener = this.mFrameListener;
            if (frameListener != null) {
                frameListener.onFrameBegin(this.mEngine.getEngineTime());
            }
            boolean isRenderRequired = false;
            if (!(this.mRenderNodeGraph == null && this.mRenderNodeGraphType == null)) {
                isRenderRequired = this.mEngine.update();
            }
            if (isRenderRequired) {
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
        List<Task> tasks;
        synchronized (this) {
            tasks = new ArrayList<>(this.mTasks);
        }
        List<Task> completedTasks = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            if (task.onExecute()) {
                completedTasks.add(task);
            }
        }
        synchronized (this) {
            this.mTasks.removeAll(completedTasks);
        }
        for (Task task2 : completedTasks) {
            task2.onFinish();
        }
    }

    public void stopRendering() {
        this.mIsFinished = true;
        if (this.mFrameCallback != null) {
            try {
                this.mEngine.getRenderChoreographer().removeFrameCallback(this.mFrameCallback);
            } catch (IllegalStateException e) {
                Log.w(TAG, "stopRendering: ", null);
            }
            this.mFrameCallback = null;
        }
    }

    public void pause() {
        this.mIsPaused = true;
    }

    public void resume() {
        if (!this.mIsFinished && this.mIsPaused) {
            this.mEngine.getRenderChoreographer().postFrameCallback(this.mFrameCallback);
        }
        this.mIsPaused = false;
    }

    public void setFrameListener(FrameListener frameListener) {
        this.mFrameListener = frameListener;
    }

    public void setVsyncCount(int vsyncCount) {
        this.mVsyncCount = vsyncCount;
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

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mEngine != null) {
            Log.w(TAG, "RenderLoop.release() should be called explicitly when possible.");
            this.mEngine.runInRenderThread(new Runnable() {
                /* class com.huawei.agpengine.RenderLoop.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    RenderLoop.this.release();
                }
            });
        }
        super.finalize();
    }
}
