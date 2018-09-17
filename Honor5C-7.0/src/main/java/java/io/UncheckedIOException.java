package java.io;

import java.util.Objects;

public class UncheckedIOException extends RuntimeException {
    private static final long serialVersionUID = -8134305061645241065L;

    public UncheckedIOException(String message, IOException cause) {
        super(message, (Throwable) Objects.requireNonNull(cause));
    }

    public UncheckedIOException(IOException cause) {
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
