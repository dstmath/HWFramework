package javax.microedition.khronos.egl;

import com.google.android.gles_jni.EGLImpl;
import javax.microedition.khronos.opengles.GL;

public abstract class EGLContext {
    private static final EGL EGL_INSTANCE = new EGLImpl();

    public abstract GL getGL();

    public static EGL getEGL() {
        return EGL_INSTANCE;
    }
}
