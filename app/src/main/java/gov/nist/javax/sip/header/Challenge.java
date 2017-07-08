package gov.nist.javax.sip.header;

import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;

public class Challenge extends SIPObject {
    private static String ALGORITHM = null;
    private static String DOMAIN = null;
    private static String OPAQUE = null;
    private static String QOP = null;
    private static String REALM = null;
    private static String RESPONSE = null;
    private static String SIGNATURE = null;
    private static String SIGNED_BY = null;
    private static String STALE = null;
    private static String URI = null;
    private static final long serialVersionUID = 5944455875924336L;
    protected NameValueList authParams;
    protected String scheme;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.header.Challenge.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.header.Challenge.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.header.Challenge.<clinit>():void");
    }

    public Challenge() {
        this.authParams = new NameValueList();
        this.authParams.setSeparator(Separators.COMMA);
    }

    public String encode() {
        return new StringBuffer(this.scheme).append(Separators.SP).append(this.authParams.encode()).toString();
    }

    public String getScheme() {
        return this.scheme;
    }

    public NameValueList getAuthParams() {
        return this.authParams;
    }

    public String getDomain() {
        return (String) this.authParams.getValue(DOMAIN);
    }

    public String getURI() {
        return (String) this.authParams.getValue(URI);
    }

    public String getOpaque() {
        return (String) this.authParams.getValue(OPAQUE);
    }

    public String getQOP() {
        return (String) this.authParams.getValue(QOP);
    }

    public String getAlgorithm() {
        return (String) this.authParams.getValue(ALGORITHM);
    }

    public String getStale() {
        return (String) this.authParams.getValue(STALE);
    }

    public String getSignature() {
        return (String) this.authParams.getValue(SIGNATURE);
    }

    public String getSignedBy() {
        return (String) this.authParams.getValue(SIGNED_BY);
    }

    public String getResponse() {
        return (String) this.authParams.getValue(RESPONSE);
    }

    public String getRealm() {
        return (String) this.authParams.getValue(REALM);
    }

    public String getParameter(String name) {
        return (String) this.authParams.getValue(name);
    }

    public boolean hasParameter(String name) {
        return this.authParams.getNameValue(name) != null;
    }

    public boolean hasParameters() {
        return this.authParams.size() != 0;
    }

    public boolean removeParameter(String name) {
        return this.authParams.delete(name);
    }

    public void removeParameters() {
        this.authParams = new NameValueList();
    }

    public void setParameter(NameValue nv) {
        this.authParams.set(nv);
    }

    public void setScheme(String s) {
        this.scheme = s;
    }

    public void setAuthParams(NameValueList a) {
        this.authParams = a;
    }

    public Object clone() {
        Challenge retval = (Challenge) super.clone();
        if (this.authParams != null) {
            retval.authParams = (NameValueList) this.authParams.clone();
        }
        return retval;
    }
}
