package com.huawei.zxing.oned;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.common.BitArray;

final class UPCEANExtensionSupport {
    private static final int[] EXTENSION_START_PATTERN = null;
    private final UPCEANExtension5Support fiveSupport;
    private final UPCEANExtension2Support twoSupport;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.UPCEANExtensionSupport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.UPCEANExtensionSupport.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.UPCEANExtensionSupport.<clinit>():void");
    }

    UPCEANExtensionSupport() {
        this.twoSupport = new UPCEANExtension2Support();
        this.fiveSupport = new UPCEANExtension5Support();
    }

    Result decodeRow(int rowNumber, BitArray row, int rowOffset) throws NotFoundException {
        int[] extensionStartRange = UPCEANReader.findGuardPattern(row, rowOffset, false, EXTENSION_START_PATTERN);
        try {
            return this.fiveSupport.decodeRow(rowNumber, row, extensionStartRange);
        } catch (ReaderException e) {
            return this.twoSupport.decodeRow(rowNumber, row, extensionStartRange);
        }
    }
}
