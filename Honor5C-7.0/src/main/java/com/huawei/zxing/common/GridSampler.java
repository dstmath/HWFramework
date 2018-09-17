package com.huawei.zxing.common;

import com.huawei.zxing.NotFoundException;

public abstract class GridSampler {
    private static GridSampler gridSampler;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.common.GridSampler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.common.GridSampler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.common.GridSampler.<clinit>():void");
    }

    public abstract BitMatrix sampleGrid(BitMatrix bitMatrix, int i, int i2, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16) throws NotFoundException;

    public abstract BitMatrix sampleGrid(BitMatrix bitMatrix, int i, int i2, PerspectiveTransform perspectiveTransform) throws NotFoundException;

    public static void setGridSampler(GridSampler newGridSampler) {
        gridSampler = newGridSampler;
    }

    public static GridSampler getInstance() {
        return gridSampler;
    }

    protected static void checkAndNudgePoints(BitMatrix image, float[] points) throws NotFoundException {
        int offset;
        int width = image.getWidth();
        int height = image.getHeight();
        boolean nudged = true;
        for (offset = 0; offset < points.length && nudged; offset += 2) {
            int x = (int) points[offset];
            int y = (int) points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw NotFoundException.getNotFoundInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = (float) (width - 1);
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
            } else if (y == height) {
                points[offset + 1] = (float) (height - 1);
                nudged = true;
            }
        }
        nudged = true;
        for (offset = points.length - 2; offset >= 0 && nudged; offset -= 2) {
            x = (int) points[offset];
            y = (int) points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw NotFoundException.getNotFoundInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = (float) (width - 1);
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
            } else if (y == height) {
                points[offset + 1] = (float) (height - 1);
                nudged = true;
            }
        }
    }
}
