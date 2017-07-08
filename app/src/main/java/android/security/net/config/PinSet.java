package android.security.net.config;

import android.util.ArraySet;
import java.util.Set;

public final class PinSet {
    public static final PinSet EMPTY_PINSET = null;
    public final long expirationTime;
    public final Set<Pin> pins;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.net.config.PinSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.net.config.PinSet.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.PinSet.<clinit>():void");
    }

    public PinSet(Set<Pin> pins, long expirationTime) {
        if (pins == null) {
            throw new NullPointerException("pins must not be null");
        }
        this.pins = pins;
        this.expirationTime = expirationTime;
    }

    Set<String> getPinAlgorithms() {
        Set<String> algorithms = new ArraySet();
        for (Pin pin : this.pins) {
            algorithms.add(pin.digestAlgorithm);
        }
        return algorithms;
    }
}
