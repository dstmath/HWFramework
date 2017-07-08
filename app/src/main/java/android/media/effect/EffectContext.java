package android.media.effect;

import android.filterfw.core.CachedFrameManager;
import android.filterfw.core.FilterContext;
import android.filterfw.core.GLEnvironment;
import android.opengl.GLES20;
import android.opengl.GLES30;

public class EffectContext {
    private final int GL_STATE_ARRAYBUFFER;
    private final int GL_STATE_COUNT;
    private final int GL_STATE_FBO;
    private final int GL_STATE_PROGRAM;
    private EffectFactory mFactory;
    FilterContext mFilterContext;
    private int[] mOldState;

    public static EffectContext createWithCurrentGlContext() {
        EffectContext result = new EffectContext();
        result.initInCurrentGlContext();
        return result;
    }

    public EffectFactory getFactory() {
        return this.mFactory;
    }

    public void release() {
        this.mFilterContext.tearDown();
        this.mFilterContext = null;
    }

    private EffectContext() {
        this.GL_STATE_FBO = 0;
        this.GL_STATE_PROGRAM = 1;
        this.GL_STATE_ARRAYBUFFER = 2;
        this.GL_STATE_COUNT = 3;
        this.mOldState = new int[3];
        this.mFilterContext = new FilterContext();
        this.mFilterContext.setFrameManager(new CachedFrameManager());
        this.mFactory = new EffectFactory(this);
    }

    private void initInCurrentGlContext() {
        if (GLEnvironment.isAnyContextActive()) {
            GLEnvironment glEnvironment = new GLEnvironment();
            glEnvironment.initWithCurrentContext();
            this.mFilterContext.initGLEnvironment(glEnvironment);
            return;
        }
        throw new RuntimeException("Attempting to initialize EffectContext with no active GL context!");
    }

    final void assertValidGLState() {
        GLEnvironment glEnv = this.mFilterContext.getGLEnvironment();
        if (glEnv != null && glEnv.isContextActive()) {
            return;
        }
        if (GLEnvironment.isAnyContextActive()) {
            throw new RuntimeException("Applying effect in wrong GL context!");
        }
        throw new RuntimeException("Attempting to apply effect without valid GL context!");
    }

    final void saveGLState() {
        GLES20.glGetIntegerv(GLES30.GL_DRAW_FRAMEBUFFER_BINDING, this.mOldState, 0);
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, this.mOldState, 1);
        GLES20.glGetIntegerv(GLES20.GL_ARRAY_BUFFER_BINDING, this.mOldState, 2);
    }

    final void restoreGLState() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.mOldState[0]);
        GLES20.glUseProgram(this.mOldState[1]);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.mOldState[2]);
    }
}
