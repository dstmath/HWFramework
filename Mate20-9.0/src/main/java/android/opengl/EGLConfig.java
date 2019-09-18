package android.opengl;

public class EGLConfig extends EGLObjectHandle {
    private EGLConfig(long handle) {
        super(handle);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof EGLConfig)) {
            return false;
        }
        if (getNativeHandle() != ((EGLConfig) o).getNativeHandle()) {
            z = false;
        }
        return z;
    }
}
