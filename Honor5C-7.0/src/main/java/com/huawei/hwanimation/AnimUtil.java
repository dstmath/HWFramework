package com.huawei.hwanimation;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.AttributeSet;
import com.android.internal.R;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnimUtil {
    private static final boolean DEBUG = false;
    private static final String TAG = "AnimUtil";
    public static final int TRANSIT_ACTIVITY_CLOSE = 2;
    public static final int TRANSIT_ACTIVITY_OPEN = 1;
    public static final int TRANSIT_TASK_CLOSE = 4;
    public static final int TRANSIT_TASK_OPEN = 3;
    private static String sHwAnimResPackageName;
    private Context mClientContext;
    private IBinder mClientToken;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hwanimation.AnimUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hwanimation.AnimUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwanimation.AnimUtil.<clinit>():void");
    }

    public AnimUtil() {
        this.mClientContext = null;
        this.mClientToken = null;
    }

    public AnimUtil(Context context) {
        this.mClientContext = null;
        this.mClientToken = null;
        this.mClientContext = context;
        initToken();
    }

    public CubicBezierInterpolator getCubicBezierInterpolator(String name, Context context, AttributeSet attrs) {
        if (name.equals("cubicBezierInterpolator")) {
            return new CubicBezierInterpolator(context, attrs);
        }
        if (name.equals("cubicBezierReverseInterpolator")) {
            return new CubicBezierReverseInterpolator(context, attrs);
        }
        return null;
    }

    public CubicBezierInterpolator getCubicBezierInterpolator(String name, Resources res, Theme theme, AttributeSet attrs) {
        if (name.equals("cubicBezierInterpolator")) {
            return new CubicBezierInterpolator(res, theme, attrs);
        }
        if (name.equals("cubicBezierReverseInterpolator")) {
            return new CubicBezierReverseInterpolator(res, theme, attrs);
        }
        return null;
    }

    public void overrideTransition(int transit) {
        if (this.mClientToken != null) {
            try {
                Context hwAnimationContext = this.mClientContext.createPackageContext(sHwAnimResPackageName, 0);
                if (hwAnimationContext != null) {
                    int enterAnim = 0;
                    int exitAnim = 0;
                    int resId = hwAnimationContext.getResources().getIdentifier("HwAnimation", "style", sHwAnimResPackageName);
                    if (resId != 0) {
                        TypedArray windowAnimationArray = hwAnimationContext.obtainStyledAttributes(resId, R.styleable.WindowAnimation);
                        if (transit == TRANSIT_ACTIVITY_OPEN) {
                            enterAnim = windowAnimationArray.getResourceId(TRANSIT_TASK_CLOSE, 0);
                            exitAnim = windowAnimationArray.getResourceId(5, 0);
                        } else if (transit == TRANSIT_ACTIVITY_CLOSE) {
                            enterAnim = windowAnimationArray.getResourceId(6, 0);
                            exitAnim = windowAnimationArray.getResourceId(7, 0);
                        } else if (transit == TRANSIT_TASK_OPEN) {
                            enterAnim = windowAnimationArray.getResourceId(8, 0);
                            exitAnim = windowAnimationArray.getResourceId(9, 0);
                        } else if (transit == TRANSIT_TASK_CLOSE) {
                            enterAnim = windowAnimationArray.getResourceId(10, 0);
                            exitAnim = windowAnimationArray.getResourceId(11, 0);
                        }
                        windowAnimationArray.recycle();
                        try {
                            ActivityManagerNative.getDefault().overridePendingTransition(this.mClientToken, sHwAnimResPackageName, enterAnim, exitAnim);
                        } catch (RemoteException e) {
                        }
                    }
                }
            } catch (NameNotFoundException e2) {
            }
        }
    }

    private void initToken() {
        try {
            Method method = Activity.class.getDeclaredMethod("getActivityToken", new Class[0]);
            method.setAccessible(true);
            this.mClientToken = (IBinder) method.invoke(this.mClientContext, new Object[0]);
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e2) {
        } catch (IllegalAccessException e3) {
        } catch (InvocationTargetException e4) {
        }
    }
}
