package android.media.effect;

import android.filterfw.core.CachedFrameManager;
import android.filterfw.core.FilterContext;
import android.filterfw.core.GLEnvironment;
import android.opengl.GLES20;

public class EffectContext {
    private final int GL_STATE_ARRAYBUFFER = 2;
    private final int GL_STATE_COUNT = 3;
    private final int GL_STATE_FBO = 0;
    private final int GL_STATE_PROGRAM = 1;
    private EffectFactory mFactory;
    FilterContext mFilterContext = new FilterContext();
    private int[] mOldState = new int[3];

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
        if (glEnv != null && (glEnv.isContextActive() ^ 1) == 0) {
            return;
        }
        if (GLEnvironment.isAnyContextActive()) {
            throw new RuntimeException("Applying effect in wrong GL context!");
        }
        throw new RuntimeException("Attempting to apply effect without valid GL context!");
    }

    final void saveGLState() {
        GLES20.glGetIntegerv(36006, this.mOldState, 0);
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, this.mOldState, 1);
        GLES20.glGetIntegerv(34964, this.mOldState, 2);
    }

    final void restoreGLState() {
        GLES20.glBindFramebuffer(36160, this.mOldState[0]);
        GLES20.glUseProgram(this.mOldState[1]);
        GLES20.glBindBuffer(34962, this.mOldState[2]);
    }
}
