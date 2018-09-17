package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;

class ReasonsMask {
    static final ReasonsMask allReasons = null;
    private int _reasons;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jce.provider.ReasonsMask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jce.provider.ReasonsMask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.ReasonsMask.<clinit>():void");
    }

    ReasonsMask(ReasonFlags reasons) {
        this._reasons = reasons.intValue();
    }

    private ReasonsMask(int reasons) {
        this._reasons = reasons;
    }

    ReasonsMask() {
        this(0);
    }

    void addReasons(ReasonsMask mask) {
        this._reasons |= mask.getReasons();
    }

    boolean isAllReasons() {
        return this._reasons == allReasons._reasons;
    }

    ReasonsMask intersect(ReasonsMask mask) {
        ReasonsMask _mask = new ReasonsMask();
        _mask.addReasons(new ReasonsMask(this._reasons & mask.getReasons()));
        return _mask;
    }

    boolean hasNewReasons(ReasonsMask mask) {
        return (this._reasons | (mask.getReasons() ^ this._reasons)) != 0;
    }

    int getReasons() {
        return this._reasons;
    }
}
