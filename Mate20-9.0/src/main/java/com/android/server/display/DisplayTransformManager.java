package com.android.server.display;

import android.app.ActivityManager;
import android.opengl.Matrix;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.lang.reflect.Array;
import java.util.Arrays;

public class DisplayTransformManager {
    private static final float COLOR_SATURATION_BOOSTED = 1.1f;
    private static final float COLOR_SATURATION_NATURAL = 1.0f;
    private static final int DISPLAY_COLOR_ENHANCED = 2;
    private static final int DISPLAY_COLOR_MANAGED = 0;
    private static final int DISPLAY_COLOR_UNMANAGED = 1;
    public static final int LEVEL_COLOR_MATRIX_GRAYSCALE = 200;
    public static final int LEVEL_COLOR_MATRIX_INVERT_COLOR = 300;
    public static final int LEVEL_COLOR_MATRIX_NIGHT_DISPLAY = 100;
    public static final int LEVEL_COLOR_MATRIX_SATURATION = 150;
    private static final String PERSISTENT_PROPERTY_DISPLAY_COLOR = "persist.sys.sf.native_mode";
    private static final String PERSISTENT_PROPERTY_SATURATION = "persist.sys.sf.color_saturation";
    private static final String SURFACE_FLINGER = "SurfaceFlinger";
    private static final int SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX = 1015;
    private static final int SURFACE_FLINGER_TRANSACTION_DALTONIZER = 1014;
    private static final int SURFACE_FLINGER_TRANSACTION_DISPLAY_COLOR = 1023;
    private static final int SURFACE_FLINGER_TRANSACTION_SATURATION = 1022;
    private static final String TAG = "DisplayTransformManager";
    @GuardedBy("mColorMatrix")
    private final SparseArray<float[]> mColorMatrix = new SparseArray<>(3);
    @GuardedBy("mDaltonizerModeLock")
    private int mDaltonizerMode = -1;
    private final Object mDaltonizerModeLock = new Object();
    @GuardedBy("mColorMatrix")
    private final float[][] mTempColorMatrix = ((float[][]) Array.newInstance(float.class, new int[]{2, 16}));

    DisplayTransformManager() {
    }

    public float[] getColorMatrix(int key) {
        float[] copyOf;
        synchronized (this.mColorMatrix) {
            float[] value = this.mColorMatrix.get(key);
            copyOf = value == null ? null : Arrays.copyOf(value, value.length);
        }
        return copyOf;
    }

    public void setColorMatrix(int level, float[] value) {
        if (value == null || value.length == 16) {
            synchronized (this.mColorMatrix) {
                float[] oldValue = this.mColorMatrix.get(level);
                if (!Arrays.equals(oldValue, value)) {
                    if (value == null) {
                        this.mColorMatrix.remove(level);
                    } else if (oldValue == null) {
                        this.mColorMatrix.put(level, Arrays.copyOf(value, value.length));
                    } else {
                        System.arraycopy(value, 0, oldValue, 0, value.length);
                    }
                    applyColorMatrix(computeColorMatrixLocked());
                }
            }
            return;
        }
        throw new IllegalArgumentException("Expected length: 16 (4x4 matrix), actual length: " + value.length);
    }

    @GuardedBy("mColorMatrix")
    private float[] computeColorMatrixLocked() {
        int count = this.mColorMatrix.size();
        if (count == 0) {
            return null;
        }
        float[][] result = this.mTempColorMatrix;
        Matrix.setIdentityM(result[0], 0);
        for (int i = 0; i < count; i++) {
            Matrix.multiplyMM(result[(i + 1) % 2], 0, result[i % 2], 0, this.mColorMatrix.valueAt(i), 0);
        }
        return result[count % 2];
    }

    public int getDaltonizerMode() {
        int i;
        synchronized (this.mDaltonizerModeLock) {
            i = this.mDaltonizerMode;
        }
        return i;
    }

    public void setDaltonizerMode(int mode) {
        synchronized (this.mDaltonizerModeLock) {
            if (this.mDaltonizerMode != mode) {
                this.mDaltonizerMode = mode;
                applyDaltonizerMode(mode);
            }
        }
    }

    private static void applyColorMatrix(float[] m) {
        IBinder flinger = ServiceManager.getService(SURFACE_FLINGER);
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
            try {
                flinger.transact(SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set color transform", ex);
            } catch (Throwable th) {
                data.recycle();
                throw th;
            }
            data.recycle();
        }
    }

    private static void applyDaltonizerMode(int mode) {
        IBinder flinger = ServiceManager.getService(SURFACE_FLINGER);
        if (flinger != null) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            data.writeInt(mode);
            try {
                flinger.transact(SURFACE_FLINGER_TRANSACTION_DALTONIZER, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set Daltonizer mode", ex);
            } catch (Throwable th) {
                data.recycle();
                throw th;
            }
            data.recycle();
        }
    }

    public static boolean needsLinearColorMatrix() {
        return SystemProperties.getInt(PERSISTENT_PROPERTY_DISPLAY_COLOR, 1) != 1;
    }

    public static boolean needsLinearColorMatrix(int colorMode) {
        return colorMode != 2;
    }

    public boolean setColorMode(int colorMode, float[] nightDisplayMatrix) {
        if (colorMode == 0) {
            applySaturation(1.0f);
            setDisplayColor(0);
        } else if (colorMode == 1) {
            applySaturation(COLOR_SATURATION_BOOSTED);
            setDisplayColor(0);
        } else if (colorMode == 2) {
            applySaturation(1.0f);
            setDisplayColor(1);
        } else if (colorMode == 3) {
            applySaturation(1.0f);
            setDisplayColor(2);
        }
        setColorMatrix(100, nightDisplayMatrix);
        updateConfiguration();
        return true;
    }

    private void applySaturation(float saturation) {
        SystemProperties.set(PERSISTENT_PROPERTY_SATURATION, Float.toString(saturation));
        IBinder flinger = ServiceManager.getService(SURFACE_FLINGER);
        if (flinger != null) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            data.writeFloat(saturation);
            try {
                flinger.transact(SURFACE_FLINGER_TRANSACTION_SATURATION, data, null, 0);
            } catch (RemoteException ex) {
                Log.e(TAG, "Failed to set saturation", ex);
            } catch (Throwable th) {
                data.recycle();
                throw th;
            }
            data.recycle();
        }
    }

    private void setDisplayColor(int color) {
        SystemProperties.set(PERSISTENT_PROPERTY_DISPLAY_COLOR, Integer.toString(color));
        IBinder flinger = ServiceManager.getService(SURFACE_FLINGER);
        if (flinger != null) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            data.writeInt(color);
            try {
                flinger.transact(SURFACE_FLINGER_TRANSACTION_DISPLAY_COLOR, data, null, 0);
            } catch (RemoteException ex) {
                Log.e(TAG, "Failed to set display color", ex);
            } catch (Throwable th) {
                data.recycle();
                throw th;
            }
            data.recycle();
        }
    }

    private void updateConfiguration() {
        try {
            ActivityManager.getService().updateConfiguration(null);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not update configuration", e);
        }
    }
}
