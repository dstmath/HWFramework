package sun.net.ftp;

import java.net.HttpURLConnection;
import sun.util.logging.PlatformLogger;

public enum FtpReplyCode {
    ;
    
    private final int value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.ftp.FtpReplyCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.ftp.FtpReplyCode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.ftp.FtpReplyCode.<clinit>():void");
    }

    private FtpReplyCode(int val) {
        this.value = val;
    }

    public int getValue() {
        return this.value;
    }

    public boolean isPositivePreliminary() {
        return this.value >= 100 && this.value < HttpURLConnection.HTTP_OK;
    }

    public boolean isPositiveCompletion() {
        return this.value >= HttpURLConnection.HTTP_OK && this.value < PlatformLogger.FINEST;
    }

    public boolean isPositiveIntermediate() {
        return this.value >= PlatformLogger.FINEST && this.value < PlatformLogger.FINER;
    }

    public boolean isTransientNegative() {
        return this.value >= PlatformLogger.FINER && this.value < PlatformLogger.FINE;
    }

    public boolean isPermanentNegative() {
        return this.value >= PlatformLogger.FINE && this.value < 600;
    }

    public boolean isProtectedReply() {
        return this.value >= 600 && this.value < PlatformLogger.CONFIG;
    }

    public boolean isSyntax() {
        return (this.value / 10) - ((this.value / 100) * 10) == 0;
    }

    public boolean isInformation() {
        return (this.value / 10) - ((this.value / 100) * 10) == 1;
    }

    public boolean isConnection() {
        return (this.value / 10) - ((this.value / 100) * 10) == 2;
    }

    public boolean isAuthentication() {
        return (this.value / 10) - ((this.value / 100) * 10) == 3;
    }

    public boolean isUnspecified() {
        return (this.value / 10) - ((this.value / 100) * 10) == 4;
    }

    public boolean isFileSystem() {
        return (this.value / 10) - ((this.value / 100) * 10) == 5;
    }

    public static FtpReplyCode find(int v) {
        for (FtpReplyCode code : values()) {
            if (code.getValue() == v) {
                return code;
            }
        }
        return UNKNOWN_ERROR;
    }
}
