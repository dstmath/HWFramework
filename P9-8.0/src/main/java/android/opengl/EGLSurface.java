package android.opengl;

public class EGLSurface extends EGLObjectHandle {
    private EGLSurface(long handle) {
        super(handle);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof EGLSurface)) {
            return false;
        }
        if (getNativeHandle() != ((EGLSurface) o).getNativeHandle()) {
            z = false;
        }
        return z;
    }
}
