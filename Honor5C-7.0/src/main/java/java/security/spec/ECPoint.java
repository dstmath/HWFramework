package java.security.spec;

import java.math.BigInteger;

public class ECPoint {
    public static final ECPoint POINT_INFINITY = null;
    private final BigInteger x;
    private final BigInteger y;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.spec.ECPoint.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.spec.ECPoint.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.security.spec.ECPoint.<clinit>():void");
    }

    private ECPoint() {
        this.x = null;
        this.y = null;
    }

    public ECPoint(BigInteger x, BigInteger y) {
        if (x == null || y == null) {
            throw new NullPointerException("affine coordinate x or y is null");
        }
        this.x = x;
        this.y = y;
    }

    public BigInteger getAffineX() {
        return this.x;
    }

    public BigInteger getAffineY() {
        return this.y;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this == POINT_INFINITY || !(obj instanceof ECPoint)) {
            return false;
        }
        boolean equals;
        if (this.x.equals(((ECPoint) obj).x)) {
            equals = this.y.equals(((ECPoint) obj).y);
        } else {
            equals = false;
        }
        return equals;
    }

    public int hashCode() {
        if (this == POINT_INFINITY) {
            return 0;
        }
        return this.x.hashCode() << (this.y.hashCode() + 5);
    }
}
