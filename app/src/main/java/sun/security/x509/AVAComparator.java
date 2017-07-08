package sun.security.x509;

import java.util.Comparator;

/* compiled from: RDN */
class AVAComparator implements Comparator<AVA> {
    private static final Comparator<AVA> INSTANCE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.AVAComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.AVAComparator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.AVAComparator.<clinit>():void");
    }

    private AVAComparator() {
    }

    static Comparator<AVA> getInstance() {
        return INSTANCE;
    }

    public int compare(AVA a1, AVA a2) {
        boolean a1Has2253 = a1.hasRFC2253Keyword();
        boolean a2Has2253 = a2.hasRFC2253Keyword();
        if (a1Has2253) {
            if (a2Has2253) {
                return a1.toRFC2253CanonicalString().compareTo(a2.toRFC2253CanonicalString());
            }
            return -1;
        } else if (a2Has2253) {
            return 1;
        } else {
            int len;
            int length;
            int[] a1Oid = a1.getObjectIdentifier().toIntArray();
            int[] a2Oid = a2.getObjectIdentifier().toIntArray();
            int pos = 0;
            if (a1Oid.length > a2Oid.length) {
                len = a2Oid.length;
            } else {
                len = a1Oid.length;
            }
            while (pos < len && a1Oid[pos] == a2Oid[pos]) {
                pos++;
            }
            if (pos == len) {
                length = a1Oid.length - a2Oid.length;
            } else {
                length = a1Oid[pos] - a2Oid[pos];
            }
            return length;
        }
    }
}
