package javax.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/* compiled from: SealedObject */
final class extObjectInputStream extends ObjectInputStream {
    private static ClassLoader systemClassLoader = null;

    extObjectInputStream(InputStream in) throws IOException, StreamCorruptedException {
        super(in);
    }

    protected Class<?> resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(v);
        } catch (ClassNotFoundException e) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                if (systemClassLoader == null) {
                    systemClassLoader = ClassLoader.getSystemClassLoader();
                }
                loader = systemClassLoader;
                if (loader == null) {
                    throw new ClassNotFoundException(v.getName());
                }
            }
            return Class.forName(v.getName(), false, loader);
        }
    }
}
