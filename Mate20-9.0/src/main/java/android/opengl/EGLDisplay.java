package android.opengl;

public class EGLDisplay extends EGLObjectHandle {
    private EGLDisplay(long handle) {
        super(handle);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof EGLDisplay)) {
            return false;
        }
        if (getNativeHandle() != ((EGLDisplay) o).getNativeHandle()) {
            z = false;
        }
        return z;
    }
}
