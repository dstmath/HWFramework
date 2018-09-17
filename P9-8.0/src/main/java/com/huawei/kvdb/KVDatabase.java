package com.huawei.kvdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import java.util.Hashtable;

public class KVDatabase {
    public static final int IMAGE = 1;
    public static final int VIDEO = 1;
    private static KVDatabase database = null;
    private static HwKVDatabase hwKVDatabase = null;
    private HwKVConnectionPool hwKVConnectionPool = null;

    public static long generateKey(int id, long timeModified, int mediaType, int type) {
        return HwKVDatabase.generateKey(id, timeModified, mediaType, type);
    }

    private KVDatabase(Context context) {
        hwKVDatabase = HwKVDatabase.getInstance(context);
        if (hwKVDatabase != null) {
            this.hwKVConnectionPool = hwKVDatabase.getKVConnectionPool();
        }
    }

    public static synchronized KVDatabase getInstance(Context context) {
        KVDatabase kVDatabase;
        synchronized (KVDatabase.class) {
            if (database == null) {
                database = new KVDatabase(context);
            }
            kVDatabase = database;
        }
        return kVDatabase;
    }

    public boolean put(long key, byte[] value, int size) throws KVFullException {
        try {
            return hwKVDatabase.put(key, value, size);
        } catch (HwKVFullException e) {
            throw new KVFullException();
        }
    }

    public boolean remove(long key) throws KVFullException {
        try {
            return hwKVDatabase.remove(key);
        } catch (HwKVFullException e) {
            throw new KVFullException();
        }
    }

    public Bitmap getBitmap(long key, Options options) {
        return hwKVDatabase.getBitmap(key, options);
    }

    public boolean hasKey(long key) {
        return hwKVDatabase.hasKey(key);
    }

    public KVConnection getKVConnection() {
        KVConnection kvConnection = new KVConnection();
        kvConnection.setHwKVConnection(hwKVDatabase);
        return kvConnection;
    }

    public void releaseKVConnection(KVConnection kvConnection, boolean isDatabaseExist) {
        HwKVConnection hwKVConnection = kvConnection.getHwKVConnection();
        if (hwKVConnection != null) {
            hwKVDatabase.releaseKVConnection(hwKVConnection, isDatabaseExist);
        }
    }

    public int getKeyNum() {
        return hwKVDatabase.getKeyNum();
    }

    public Hashtable<Long, Long> getAllKeys() {
        return hwKVDatabase.getAllKeys();
    }

    public void closeAllConnections() {
        if (this.hwKVConnectionPool != null) {
            this.hwKVConnectionPool.closeConnection();
        }
    }
}
