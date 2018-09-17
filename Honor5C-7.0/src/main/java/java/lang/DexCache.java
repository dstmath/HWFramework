package java.lang;

import com.android.dex.Dex;

final class DexCache {
    private volatile Dex dex;
    private long dexFile;
    String location;
    private int numResolvedFields;
    private int numResolvedMethods;
    private int numResolvedTypes;
    private int numStrings;
    private long resolvedFields;
    private long resolvedMethods;
    private long resolvedTypes;
    private long strings;

    private native Dex getDexNative();

    native String getResolvedString(int i);

    native Class<?> getResolvedType(int i);

    native void setResolvedString(int i, String str);

    native void setResolvedType(int i, Class<?> cls);

    private DexCache() {
    }

    Dex getDex() {
        Dex result = this.dex;
        if (result == null) {
            synchronized (this) {
                result = this.dex;
                if (result == null) {
                    result = getDexNative();
                    this.dex = result;
                }
            }
        }
        return result;
    }
}
