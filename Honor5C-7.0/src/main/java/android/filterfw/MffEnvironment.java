package android.filterfw;

import android.filterfw.core.CachedFrameManager;
import android.filterfw.core.FilterContext;
import android.filterfw.core.FrameManager;
import android.filterfw.core.GLEnvironment;

public class MffEnvironment {
    private FilterContext mContext;

    protected MffEnvironment(FrameManager frameManager) {
        if (frameManager == null) {
            frameManager = new CachedFrameManager();
        }
        this.mContext = new FilterContext();
        this.mContext.setFrameManager(frameManager);
    }

    public FilterContext getContext() {
        return this.mContext;
    }

    public void setGLEnvironment(GLEnvironment glEnvironment) {
        this.mContext.initGLEnvironment(glEnvironment);
    }

    public void createGLEnvironment() {
        GLEnvironment glEnvironment = new GLEnvironment();
        glEnvironment.initWithNewContext();
        setGLEnvironment(glEnvironment);
    }

    public void activateGLEnvironment() {
        if (this.mContext.getGLEnvironment() != null) {
            this.mContext.getGLEnvironment().activate();
            return;
        }
        throw new NullPointerException("No GLEnvironment in place to activate!");
    }

    public void deactivateGLEnvironment() {
        if (this.mContext.getGLEnvironment() != null) {
            this.mContext.getGLEnvironment().deactivate();
            return;
        }
        throw new NullPointerException("No GLEnvironment in place to deactivate!");
    }
}
