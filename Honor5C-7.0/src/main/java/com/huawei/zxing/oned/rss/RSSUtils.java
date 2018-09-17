package com.huawei.zxing.oned.rss;

public final class RSSUtils {
    public static int getRSSvalue(int[] r1, int r2, boolean r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.rss.RSSUtils.getRSSvalue(int[], int, boolean):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.rss.RSSUtils.getRSSvalue(int[], int, boolean):int");
    }

    private RSSUtils() {
    }

    private static int combins(int n, int r) {
        int minDenom;
        int maxDenom;
        if (n - r > r) {
            minDenom = r;
            maxDenom = n - r;
        } else {
            minDenom = n - r;
            maxDenom = r;
        }
        int val = 1;
        int j = 1;
        for (int i = n; i > maxDenom; i--) {
            val *= i;
            if (j <= minDenom) {
                val /= j;
                j++;
            }
        }
        while (j <= minDenom) {
            val /= j;
            j++;
        }
        return val;
    }
}
