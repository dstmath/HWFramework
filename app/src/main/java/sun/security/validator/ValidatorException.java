package sun.security.validator;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ValidatorException extends CertificateException {
    public static final Object T_ALGORITHM_DISABLED = null;
    public static final Object T_CA_EXTENSIONS = null;
    public static final Object T_CERT_EXPIRED = null;
    public static final Object T_EE_EXTENSIONS = null;
    public static final Object T_NAME_CHAINING = null;
    public static final Object T_NO_TRUST_ANCHOR = null;
    public static final Object T_SIGNATURE_ERROR = null;
    public static final Object T_UNTRUSTED_CERT = null;
    private static final long serialVersionUID = -2836879718282292155L;
    private X509Certificate cert;
    private Object type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.validator.ValidatorException.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.validator.ValidatorException.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.validator.ValidatorException.<clinit>():void");
    }

    public ValidatorException(String msg) {
        super(msg);
    }

    public ValidatorException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }

    public ValidatorException(Object type) {
        this(type, null);
    }

    public ValidatorException(Object type, X509Certificate cert) {
        super((String) type);
        this.type = type;
        this.cert = cert;
    }

    public ValidatorException(Object type, X509Certificate cert, Throwable cause) {
        this(type, cert);
        initCause(cause);
    }

    public ValidatorException(String msg, Object type, X509Certificate cert) {
        super(msg);
        this.type = type;
        this.cert = cert;
    }

    public ValidatorException(String msg, Object type, X509Certificate cert, Throwable cause) {
        this(msg, type, cert);
        initCause(cause);
    }

    public Object getErrorType() {
        return this.type;
    }

    public X509Certificate getErrorCertificate() {
        return this.cert;
    }
}
