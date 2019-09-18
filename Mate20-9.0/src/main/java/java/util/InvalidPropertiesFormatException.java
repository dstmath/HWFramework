package java.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class InvalidPropertiesFormatException extends IOException {
    private static final long serialVersionUID = 7763056076009360219L;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public InvalidPropertiesFormatException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        initCause(cause);
    }

    public InvalidPropertiesFormatException(String message) {
        super(message);
    }

    private void writeObject(ObjectOutputStream out) throws NotSerializableException {
        throw new NotSerializableException("Not serializable.");
    }

    private void readObject(ObjectInputStream in) throws NotSerializableException {
        throw new NotSerializableException("Not serializable.");
    }
}
