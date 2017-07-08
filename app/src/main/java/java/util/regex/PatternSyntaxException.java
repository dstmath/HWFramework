package java.util.regex;

public class PatternSyntaxException extends IllegalArgumentException {
    private static final String nl = null;
    private static final long serialVersionUID = -3864639126226059218L;
    private final String desc;
    private final int index;
    private final String pattern;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.regex.PatternSyntaxException.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.regex.PatternSyntaxException.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.regex.PatternSyntaxException.<clinit>():void");
    }

    public PatternSyntaxException(String desc, String regex, int index) {
        this.desc = desc;
        this.pattern = regex;
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public String getDescription() {
        return this.desc;
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.desc);
        if (this.index >= 0) {
            sb.append(" near index ");
            sb.append(this.index);
        }
        sb.append(nl);
        sb.append(this.pattern);
        if (this.index >= 0) {
            sb.append(nl);
            for (int i = 0; i < this.index; i++) {
                sb.append(' ');
            }
            sb.append('^');
        }
        return sb.toString();
    }
}
