package android.filterfw.core;

import android.annotation.UnsupportedAppUsage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FilterContext {
    private FrameManager mFrameManager;
    private GLEnvironment mGLEnvironment;
    private Set<FilterGraph> mGraphs = new HashSet();
    private HashMap<String, Frame> mStoredFrames = new HashMap<>();

    public interface OnFrameReceivedListener {
        void onFrameReceived(Filter filter, Frame frame, Object obj);
    }

    @UnsupportedAppUsage
    public FrameManager getFrameManager() {
        return this.mFrameManager;
    }

    public void setFrameManager(FrameManager manager) {
        if (manager == null) {
            throw new NullPointerException("Attempting to set null FrameManager!");
        } else if (manager.getContext() == null) {
            this.mFrameManager = manager;
            this.mFrameManager.setContext(this);
        } else {
            throw new IllegalArgumentException("Attempting to set FrameManager which is already bound to another FilterContext!");
        }
    }

    @UnsupportedAppUsage
    public GLEnvironment getGLEnvironment() {
        return this.mGLEnvironment;
    }

    public void initGLEnvironment(GLEnvironment environment) {
        if (this.mGLEnvironment == null) {
            this.mGLEnvironment = environment;
            return;
        }
        throw new RuntimeException("Attempting to re-initialize GL Environment for FilterContext!");
    }

    public synchronized void storeFrame(String key, Frame frame) {
        Frame storedFrame = fetchFrame(key);
        if (storedFrame != null) {
            storedFrame.release();
        }
        frame.onFrameStore();
        this.mStoredFrames.put(key, frame.retain());
    }

    public synchronized Frame fetchFrame(String key) {
        Frame frame;
        frame = this.mStoredFrames.get(key);
        if (frame != null) {
            frame.onFrameFetch();
        }
        return frame;
    }

    public synchronized void removeFrame(String key) {
        Frame frame = this.mStoredFrames.get(key);
        if (frame != null) {
            this.mStoredFrames.remove(key);
            frame.release();
        }
    }

    public synchronized void tearDown() {
        for (Frame frame : this.mStoredFrames.values()) {
            frame.release();
        }
        this.mStoredFrames.clear();
        for (FilterGraph graph : this.mGraphs) {
            graph.tearDown(this);
        }
        this.mGraphs.clear();
        if (this.mFrameManager != null) {
            this.mFrameManager.tearDown();
            this.mFrameManager = null;
        }
        if (this.mGLEnvironment != null) {
            this.mGLEnvironment.tearDown();
            this.mGLEnvironment = null;
        }
    }

    /* access modifiers changed from: package-private */
    public final void addGraph(FilterGraph graph) {
        this.mGraphs.add(graph);
    }
}
