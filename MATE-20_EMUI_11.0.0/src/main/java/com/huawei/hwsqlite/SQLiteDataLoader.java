package com.huawei.hwsqlite;

import android.util.Log;

public abstract class SQLiteDataLoader {
    private static final int LOADER_MAX = 1;
    public static final int POSITION_LOADER = 0;
    private static final String TAG = "SQLiteDataLoader";
    private static SQLiteDataLoader[] loaders = new SQLiteDataLoader[1];

    public abstract int onLoad(byte[] bArr);

    static int defaultCallback(int id, byte[] data) {
        if (id != 0) {
            Log.e(TAG, "Unknown loader id!");
            return -1;
        }
        SQLiteDataLoader[] sQLiteDataLoaderArr = loaders;
        if (sQLiteDataLoaderArr[0] != null) {
            return sQLiteDataLoaderArr[0].onLoad(data);
        }
        Log.e(TAG, "Position loader doesn't exist!");
        return -1;
    }

    public static void registerLoader(int id, SQLiteDataLoader loader) {
        if (id >= 1 || loader == null) {
            throw new IllegalArgumentException("Register loader failed due to invalid arguments!");
        }
        SQLiteDataLoader[] sQLiteDataLoaderArr = loaders;
        if (sQLiteDataLoaderArr[id] == null) {
            sQLiteDataLoaderArr[id] = loader;
            return;
        }
        throw new IllegalStateException("Register loader failed because the load exists!");
    }
}
