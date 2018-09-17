package com.huawei.zxing.qrcode.decoder;

import com.huawei.lcagent.client.MetricConstant;
import huawei.android.widget.DialogContentHelper.Dex;
import huawei.android.widget.ViewDragHelper;

public enum Mode {
    ;
    
    private final int bits;
    private final int[] characterCountBitsForVersions;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.qrcode.decoder.Mode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.qrcode.decoder.Mode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.qrcode.decoder.Mode.<clinit>():void");
    }

    private Mode(int[] characterCountBitsForVersions, int bits) {
        this.characterCountBitsForVersions = characterCountBitsForVersions;
        this.bits = bits;
    }

    public static Mode forBits(int bits) {
        switch (bits) {
            case ViewDragHelper.STATE_IDLE /*0*/:
                return TERMINATOR;
            case ViewDragHelper.STATE_DRAGGING /*1*/:
                return NUMERIC;
            case ViewDragHelper.STATE_SETTLING /*2*/:
                return ALPHANUMERIC;
            case ViewDragHelper.DIRECTION_ALL /*3*/:
                return STRUCTURED_APPEND;
            case ViewDragHelper.EDGE_TOP /*4*/:
                return BYTE;
            case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
                return FNC1_FIRST_POSITION;
            case MetricConstant.CALL_METRIC_ID /*7*/:
                return ECI;
            case ViewDragHelper.EDGE_BOTTOM /*8*/:
                return KANJI;
            case MetricConstant.APR_STATISTICS_METRIC_ID /*9*/:
                return FNC1_SECOND_POSITION;
            case MetricConstant.AUDIO_METRIC_ID /*13*/:
                return HANZI;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getCharacterCountBits(Version version) {
        int offset;
        int number = version.getVersionNumber();
        if (number <= 9) {
            offset = 0;
        } else if (number <= 26) {
            offset = 1;
        } else {
            offset = 2;
        }
        return this.characterCountBitsForVersions[offset];
    }

    public int getBits() {
        return this.bits;
    }
}
