package com.huawei.zxing.multi.qrcode.detector;

import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DetectorResult;
import com.huawei.zxing.qrcode.detector.Detector;
import com.huawei.zxing.qrcode.detector.FinderPatternInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MultiDetector extends Detector {
    private static final DetectorResult[] EMPTY_DETECTOR_RESULTS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.multi.qrcode.detector.MultiDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.multi.qrcode.detector.MultiDetector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.multi.qrcode.detector.MultiDetector.<clinit>():void");
    }

    public MultiDetector(BitMatrix image) {
        super(image);
    }

    public DetectorResult[] detectMulti(Map<DecodeHintType, ?> hints) throws NotFoundException {
        FinderPatternInfo[] infos = new MultiFinderPatternFinder(getImage(), hints == null ? null : (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK)).findMulti(hints);
        if (infos.length == 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        List<DetectorResult> result = new ArrayList();
        for (FinderPatternInfo info : infos) {
            try {
                result.add(processFinderPatternInfo(info));
            } catch (ReaderException e) {
            }
        }
        if (result.isEmpty()) {
            return EMPTY_DETECTOR_RESULTS;
        }
        return (DetectorResult[]) result.toArray(new DetectorResult[result.size()]);
    }
}
