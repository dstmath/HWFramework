package android.opengl;

public class EGLSurface extends EGLObjectHandle {
    private EGLSurface(long handle) {
        super(handle);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EGLSurface)) {
            return false;
        }
        if (getNativeHandle() == ((EGLSurface) o).getNativeHandle()) {
            return true;
        }
        return false;
    }
}
