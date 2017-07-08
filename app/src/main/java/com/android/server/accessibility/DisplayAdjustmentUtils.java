package com.android.server.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.opengl.Matrix;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.util.Slog;

class DisplayAdjustmentUtils {
    private static final int DEFAULT_DISPLAY_DALTONIZER = 12;
    private static final float[] GRAYSCALE_MATRIX = null;
    private static final float[] INVERSION_MATRIX_VALUE_ONLY = null;
    private static final String LOG_TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accessibility.DisplayAdjustmentUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.accessibility.DisplayAdjustmentUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.DisplayAdjustmentUtils.<clinit>():void");
    }

    DisplayAdjustmentUtils() {
    }

    public static boolean hasAdjustments(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        return (Secure.getIntForUser(cr, "accessibility_display_inversion_enabled", 0, userId) == 0 && Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userId) == 0) ? false : true;
    }

    public static void applyAdjustments(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        float[] colorMatrix = null;
        if (Secure.getIntForUser(cr, "accessibility_display_inversion_enabled", 0, userId) != 0) {
            colorMatrix = multiply(null, INVERSION_MATRIX_VALUE_ONLY);
        }
        if (Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userId) != 0) {
            int daltonizerMode = Secure.getIntForUser(cr, "accessibility_display_daltonizer", DEFAULT_DISPLAY_DALTONIZER, userId);
            if (daltonizerMode == 0) {
                colorMatrix = multiply(colorMatrix, GRAYSCALE_MATRIX);
                setDaltonizerMode(-1);
            } else {
                setDaltonizerMode(daltonizerMode);
            }
        } else {
            setDaltonizerMode(-1);
        }
        String matrix = Secure.getStringForUser(cr, "accessibility_display_color_matrix", userId);
        if (matrix != null) {
            float[] userMatrix = get4x4Matrix(matrix);
            if (userMatrix != null) {
                colorMatrix = multiply(colorMatrix, userMatrix);
            }
        }
        setColorTransform(colorMatrix);
    }

    private static float[] get4x4Matrix(String matrix) {
        String[] strValues = matrix.split(",");
        if (strValues.length != 16) {
            return null;
        }
        float[] values = new float[strValues.length];
        int i = 0;
        while (i < values.length) {
            try {
                values[i] = Float.parseFloat(strValues[i]);
                i++;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return values;
    }

    private static float[] multiply(float[] matrix, float[] other) {
        if (matrix == null) {
            return other;
        }
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, matrix, 0, other, 0);
        return result;
    }

    private static void setDaltonizerMode(int mode) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(mode);
                flinger.transact(1014, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            Slog.e(LOG_TAG, "Failed to set Daltonizer mode", ex);
        }
    }

    private static void setColorTransform(float[] m) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                if (m != null) {
                    data.writeInt(1);
                    for (int i = 0; i < 16; i++) {
                        data.writeFloat(m[i]);
                    }
                } else {
                    data.writeInt(0);
                }
                flinger.transact(1015, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            Slog.e(LOG_TAG, "Failed to set color transform", ex);
        }
    }
}
