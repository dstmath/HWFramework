package android.icu.text;

import java.text.ParseException;

public class StringPrepParseException extends ParseException {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int ACE_PREFIX_ERROR = 6;
    public static final int BUFFER_OVERFLOW_ERROR = 9;
    public static final int CHECK_BIDI_ERROR = 4;
    public static final int DOMAIN_NAME_TOO_LONG_ERROR = 11;
    public static final int ILLEGAL_CHAR_FOUND = 1;
    public static final int INVALID_CHAR_FOUND = 0;
    public static final int LABEL_TOO_LONG_ERROR = 8;
    private static final int PARSE_CONTEXT_LEN = 16;
    public static final int PROHIBITED_ERROR = 2;
    public static final int STD3_ASCII_RULES_ERROR = 5;
    public static final int UNASSIGNED_ERROR = 3;
    public static final int VERIFICATION_ERROR = 7;
    public static final int ZERO_LENGTH_LABEL = 10;
    static final long serialVersionUID = 7160264827701651255L;
    private int error;
    private int line;
    private StringBuffer postContext;
    private StringBuffer preContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.StringPrepParseException.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.StringPrepParseException.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringPrepParseException.<clinit>():void");
    }

    public StringPrepParseException(String message, int error) {
        super(message, -1);
        this.preContext = new StringBuffer();
        this.postContext = new StringBuffer();
        this.error = error;
        this.line = INVALID_CHAR_FOUND;
    }

    public StringPrepParseException(String message, int error, String rules, int pos) {
        super(message, -1);
        this.preContext = new StringBuffer();
        this.postContext = new StringBuffer();
        this.error = error;
        setContext(rules, pos);
        this.line = INVALID_CHAR_FOUND;
    }

    public StringPrepParseException(String message, int error, String rules, int pos, int lineNumber) {
        super(message, -1);
        this.preContext = new StringBuffer();
        this.postContext = new StringBuffer();
        this.error = error;
        setContext(rules, pos);
        this.line = lineNumber;
    }

    public boolean equals(Object other) {
        boolean z = -assertionsDisabled;
        if (!(other instanceof StringPrepParseException)) {
            return -assertionsDisabled;
        }
        if (((StringPrepParseException) other).error == this.error) {
            z = true;
        }
        return z;
    }

    @Deprecated
    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.getMessage());
        buf.append(". line:  ");
        buf.append(this.line);
        buf.append(". preContext:  ");
        buf.append(this.preContext);
        buf.append(". postContext: ");
        buf.append(this.postContext);
        buf.append("\n");
        return buf.toString();
    }

    private void setPreContext(String str, int pos) {
        setPreContext(str.toCharArray(), pos);
    }

    private void setPreContext(char[] str, int pos) {
        int start = pos <= PARSE_CONTEXT_LEN ? INVALID_CHAR_FOUND : pos - 15;
        this.preContext.append(str, start, start <= PARSE_CONTEXT_LEN ? start : PARSE_CONTEXT_LEN);
    }

    private void setPostContext(String str, int pos) {
        setPostContext(str.toCharArray(), pos);
    }

    private void setPostContext(char[] str, int pos) {
        int start = pos;
        this.postContext.append(str, pos, str.length - pos);
    }

    private void setContext(String str, int pos) {
        setPreContext(str, pos);
        setPostContext(str, pos);
    }

    public int getError() {
        return this.error;
    }
}
