package android.app;

import android.util.SparseIntArray;
import com.android.internal.util.function.QuadFunction;
import com.android.internal.util.function.TriFunction;

public abstract class AppOpsManagerInternal {

    public interface CheckOpsDelegate {
        int checkAudioOperation(int i, int i2, int i3, String str, QuadFunction<Integer, Integer, Integer, String, Integer> quadFunction);

        int checkOperation(int i, int i2, String str, boolean z, QuadFunction<Integer, Integer, String, Boolean, Integer> quadFunction);

        int noteOperation(int i, int i2, String str, TriFunction<Integer, Integer, String, Integer> triFunction);
    }

    public abstract int checkOperationUnchecked(int i, int i2, String str);

    public abstract void setAllPkgModesToDefault(int i, int i2);

    public abstract void setDeviceAndProfileOwners(SparseIntArray sparseIntArray);

    public abstract void setUidMode(int i, int i2, int i3);
}
