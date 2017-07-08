package android.webkit;

public class ConsoleMessage {
    private MessageLevel mLevel;
    private int mLineNumber;
    private String mMessage;
    private String mSourceId;

    public enum MessageLevel {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.webkit.ConsoleMessage.MessageLevel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.webkit.ConsoleMessage.MessageLevel.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.ConsoleMessage.MessageLevel.<clinit>():void");
        }
    }

    public ConsoleMessage(String message, String sourceId, int lineNumber, MessageLevel msgLevel) {
        this.mMessage = message;
        this.mSourceId = sourceId;
        this.mLineNumber = lineNumber;
        this.mLevel = msgLevel;
    }

    public MessageLevel messageLevel() {
        return this.mLevel;
    }

    public String message() {
        return this.mMessage;
    }

    public String sourceId() {
        return this.mSourceId;
    }

    public int lineNumber() {
        return this.mLineNumber;
    }
}
