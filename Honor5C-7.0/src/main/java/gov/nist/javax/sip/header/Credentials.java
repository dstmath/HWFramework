package gov.nist.javax.sip.header;

import gov.nist.core.NameValueList;
import gov.nist.core.Separators;

public class Credentials extends SIPObject {
    private static String CNONCE = null;
    private static String DOMAIN = null;
    private static String NONCE = null;
    private static String OPAQUE = null;
    private static String REALM = null;
    private static String RESPONSE = null;
    private static String URI = null;
    private static String USERNAME = null;
    private static final long serialVersionUID = -6335592791505451524L;
    protected NameValueList parameters;
    protected String scheme;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.header.Credentials.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.header.Credentials.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.header.Credentials.<clinit>():void");
    }

    public Credentials() {
        this.parameters = new NameValueList();
        this.parameters.setSeparator(Separators.COMMA);
    }

    public NameValueList getCredentials() {
        return this.parameters;
    }

    public String getScheme() {
        return this.scheme;
    }

    public void setScheme(String s) {
        this.scheme = s;
    }

    public void setCredentials(NameValueList c) {
        this.parameters = c;
    }

    public String encode() {
        String retval = this.scheme;
        if (this.parameters.isEmpty()) {
            return retval;
        }
        return retval + Separators.SP + this.parameters.encode();
    }

    public Object clone() {
        Credentials retval = (Credentials) super.clone();
        if (this.parameters != null) {
            retval.parameters = (NameValueList) this.parameters.clone();
        }
        return retval;
    }
}
