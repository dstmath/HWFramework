package java.nio.file;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.ConcurrentModificationException;
import java.util.Objects;

public final class DirectoryIteratorException extends ConcurrentModificationException {
    private static final long serialVersionUID = -6012699886086212874L;

    public DirectoryIteratorException(IOException cause) {
        super((Throwable) Objects.requireNonNull(cause));
    }

    public IOException getCause() {
        return (IOException) super.getCause();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (!(super.getCause() instanceof IOException)) {
            throw new InvalidObjectException("Cause must be an IOException");
        }
    }
}
