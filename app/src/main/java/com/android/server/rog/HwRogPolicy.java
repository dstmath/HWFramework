package com.android.server.rog;

import android.content.Context;
import android.graphics.Rect;
import android.rog.AppRogInfo;
import android.util.Slog;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import com.android.server.input.HwCircleAnimation;
import java.util.Map;

public class HwRogPolicy implements IRogPolicy {
    private static final String TAG = "HwRogPolicy";
    private static Map<Integer, Rect> sResolutionLevelToRectMap;
    private float mAppRogScale;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rog.HwRogPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rog.HwRogPolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rog.HwRogPolicy.<clinit>():void");
    }

    public HwRogPolicy(Context context) {
        this.mAppRogScale = HwCircleAnimation.SMALL_ALPHA;
        this.mContext = context;
    }

    public AppRogInfo getAppOwnInfo(HwRogInfosCollector dataCollector, String packageName) {
        if (dataCollector.isInList(packageName)) {
            return dataCollector.getAppRogInfo(packageName);
        }
        return generateDefaultInfo(packageName);
    }

    private AppRogInfo generateDefaultInfo(String packageName) {
        return null;
    }

    private Rect resolutionLevelToPixel(int level) {
        if (!sResolutionLevelToRectMap.containsKey(Integer.valueOf(level))) {
            level = 3;
        }
        return (Rect) sResolutionLevelToRectMap.get(Integer.valueOf(level));
    }

    private int pixelToResolutionLevel(int widthPixel, int heightPixel) {
        if (widthPixel < 720) {
            return -1;
        }
        if (widthPixel < 1080) {
            return 4;
        }
        if (widthPixel < HwEyeProtectionDividedTimeControl.DAY_IN_MINUTE) {
            return 3;
        }
        if (widthPixel < 2160) {
            return 2;
        }
        return 1;
    }

    public void calRogAppScale() {
        int screenWidth = this.mContext.getResources().getDisplayMetrics().noncompatWidthPixels;
        int deviceResolutionLevel = pixelToResolutionLevel(screenWidth, this.mContext.getResources().getDisplayMetrics().noncompatHeightPixels);
        if (deviceResolutionLevel < 0) {
            Slog.i(TAG, "calRogAppScale->invalid resolution level, set to no scale");
            this.mAppRogScale = HwCircleAnimation.SMALL_ALPHA;
            return;
        }
        this.mAppRogScale = (((float) screenWidth) * HwCircleAnimation.SMALL_ALPHA) / ((float) resolutionLevelToPixel(deviceResolutionLevel + 1).width());
        if (Float.compare(this.mAppRogScale, HwCircleAnimation.SMALL_ALPHA) < 0) {
            Slog.i(TAG, "calRogAppScale->invalid scale factor, set to no scale");
            this.mAppRogScale = HwCircleAnimation.SMALL_ALPHA;
        }
        Slog.i(TAG, "calRogAppScale->rog sclae:" + this.mAppRogScale);
    }

    public float getAppRogScale() {
        return this.mAppRogScale;
    }
}
