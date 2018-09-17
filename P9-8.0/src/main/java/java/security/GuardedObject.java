package java.security;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GuardedObject implements Serializable {
    private static final long serialVersionUID = -5240450096227834308L;
    private Guard guard;
    private Object object;

    public GuardedObject(Object object, Guard guard) {
        this.guard = guard;
        this.object = object;
    }

    public Object getObject() throws SecurityException {
        if (this.guard != null) {
            this.guard.checkGuard(this.object);
        }
        return this.object;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        if (this.guard != null) {
            this.guard.checkGuard(this.object);
        }
        oos.defaultWriteObject();
    }
}
