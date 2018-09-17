package com.android.server.display;

import android.opengl.Matrix;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.lang.reflect.Array;
import java.util.Arrays;

public class DisplayTransformManager {
    public static final int LEVEL_COLOR_MATRIX_GRAYSCALE = 200;
    public static final int LEVEL_COLOR_MATRIX_INVERT_COLOR = 300;
    public static final int LEVEL_COLOR_MATRIX_NIGHT_DISPLAY = 100;
    private static final String TAG = "DisplayTransformManager";
    @GuardedBy("mColorMatrix")
    private final SparseArray<float[]> mColorMatrix = new SparseArray(3);
    @GuardedBy("mDaltonizerModeLock")
    private int mDaltonizerMode = -1;
    private final Object mDaltonizerModeLock = new Object();
    @GuardedBy("mColorMatrix")
    private final float[][] mTempColorMatrix = ((float[][]) Array.newInstance(Float.TYPE, new int[]{2, 16}));

    DisplayTransformManager() {
    }

    public float[] getColorMatrix(int key) {
        float[] fArr = null;
        synchronized (this.mColorMatrix) {
            float[] value = (float[]) this.mColorMatrix.get(key);
            if (value != null) {
                fArr = Arrays.copyOf(value, value.length);
            }
        }
        return fArr;
    }

    public void setColorMatrix(int level, float[] value) {
        if (value == null || value.length == 16) {
            synchronized (this.mColorMatrix) {
                float[] oldValue = (float[]) this.mColorMatrix.get(level);
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
            Matrix.multiplyMM(result[(i + 1) % 2], 0, result[i % 2], 0, (float[]) this.mColorMatrix.valueAt(i), 0);
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
            try {
                flinger.transact(1015, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set color transform", ex);
            } finally {
                data.recycle();
            }
        }
    }

    private static void applyDaltonizerMode(int mode) {
        IBinder flinger = ServiceManager.getService("SurfaceFlinger");
        if (flinger != null) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            data.writeInt(mode);
            try {
                flinger.transact(1014, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set Daltonizer mode", ex);
            } finally {
                data.recycle();
            }
        }
    }
}
