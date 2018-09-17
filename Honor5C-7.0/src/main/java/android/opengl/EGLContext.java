package android.opengl;

public class EGLContext extends EGLObjectHandle {
    private EGLContext(long handle) {
        super(handle);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof EGLContext)) {
            return false;
        }
        if (getNativeHandle() != ((EGLContext) o).getNativeHandle()) {
            z = false;
        }
        return z;
    }
}
