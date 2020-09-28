package android.os.storage;

import android.os.IVold;

public abstract class StorageManagerInternal {

    public interface ExternalStorageMountPolicy {
        int getMountMode(int i, String str);

        boolean hasExternalStorage(int i, String str);
    }

    public interface ResetListener {
        void onReset(IVold iVold);
    }

    public abstract void addExternalStoragePolicy(ExternalStorageMountPolicy externalStorageMountPolicy);

    public abstract void addResetListener(ResetListener resetListener);

    public abstract int getExternalStorageMountMode(int i, String str);

    public abstract void onAppOpsChanged(int i, int i2, String str, int i3);

    public abstract void onExternalStoragePolicyChanged(int i, String str);
}
