package java.security.cert;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;

public class CertPathValidatorException extends GeneralSecurityException {
    private static final long serialVersionUID = -3083180014971893139L;
    private CertPath certPath;
    private int index;
    private Reason reason;

    public interface Reason extends Serializable {
    }

    public enum BasicReason implements Reason {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.cert.CertPathValidatorException.BasicReason.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.cert.CertPathValidatorException.BasicReason.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.security.cert.CertPathValidatorException.BasicReason.<clinit>():void");
        }
    }

    public CertPathValidatorException() {
        this(null, null);
    }

    public CertPathValidatorException(String msg) {
        this(msg, null);
    }

    public CertPathValidatorException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        this(str, cause);
    }

    public CertPathValidatorException(String msg, Throwable cause) {
        this(msg, cause, null, -1);
    }

    public CertPathValidatorException(String msg, Throwable cause, CertPath certPath, int index) {
        this(msg, cause, certPath, index, BasicReason.UNSPECIFIED);
    }

    public CertPathValidatorException(String msg, Throwable cause, CertPath certPath, int index, Reason reason) {
        super(msg, cause);
        this.index = -1;
        this.reason = BasicReason.UNSPECIFIED;
        if (certPath == null && index != -1) {
            throw new IllegalArgumentException();
        } else if (index < -1 || (certPath != null && index >= certPath.getCertificates().size())) {
            throw new IndexOutOfBoundsException();
        } else if (reason == null) {
            throw new NullPointerException("reason can't be null");
        } else {
            this.certPath = certPath;
            this.index = index;
            this.reason = reason;
        }
    }

    public CertPath getCertPath() {
        return this.certPath;
    }

    public int getIndex() {
        return this.index;
    }

    public Reason getReason() {
        return this.reason;
    }

    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
        if (this.reason == null) {
            this.reason = BasicReason.UNSPECIFIED;
        }
        if (this.certPath == null && this.index != -1) {
            throw new InvalidObjectException("certpath is null and index != -1");
        } else if (this.index < -1 || (this.certPath != null && this.index >= this.certPath.getCertificates().size())) {
            throw new InvalidObjectException("index out of range");
        }
    }
}
