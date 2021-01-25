package ohos.javax.xml.xpath;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.PrintStream;
import java.io.PrintWriter;

public class XPathException extends Exception {
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("cause", Throwable.class)};
    private static final long serialVersionUID = -1837080260374986980L;

    public XPathException(String str) {
        super(str);
        if (str == null) {
            throw new NullPointerException("message can't be null");
        }
    }

    public XPathException(Throwable th) {
        super(th);
        if (th == null) {
            throw new NullPointerException("cause can't be null");
        }
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return super.getCause();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.putFields().put("cause", super.getCause());
        objectOutputStream.writeFields();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        Throwable th = (Throwable) objectInputStream.readFields().get("cause", (Object) null);
        if (super.getCause() == null && th != null) {
            try {
                super.initCause(th);
            } catch (IllegalStateException unused) {
                throw new InvalidClassException("Inconsistent state: two causes");
            }
        }
    }

    @Override // java.lang.Throwable
    public void printStackTrace(PrintStream printStream) {
        if (getCause() != null) {
            getCause().printStackTrace(printStream);
            printStream.println("--------------- linked to ------------------");
        }
        super.printStackTrace(printStream);
    }

    @Override // java.lang.Throwable
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override // java.lang.Throwable
    public void printStackTrace(PrintWriter printWriter) {
        if (getCause() != null) {
            getCause().printStackTrace(printWriter);
            printWriter.println("--------------- linked to ------------------");
        }
        super.printStackTrace(printWriter);
    }
}
