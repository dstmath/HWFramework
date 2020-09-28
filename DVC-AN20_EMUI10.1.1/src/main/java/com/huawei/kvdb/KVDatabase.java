package com.huawei.kvdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.Hashtable;
import java.util.Set;

public class KVDatabase {
    public static final int IMAGE = 1;
    public static final int VIDEO = 1;
    private HwKVDatabase hwKVDatabase = null;

    public KVDatabase(Context context, String dbPath) {
        this.hwKVDatabase = HwKVDatabase.getInstance(context, dbPath);
    }

    public KVDatabase(Context context) {
        this.hwKVDatabase = HwKVDatabase.getInstance(context);
    }

    public static long generateKey(int id, long timeModified, int mediaType, int type) {
        return HwKVDatabase.generateKey(id, timeModified, mediaType, type);
    }

    @Deprecated
    public static KVDatabase getInstance(Context context) {
        return new KVDatabase(context);
    }

    public byte[] get(String key) {
        return this.hwKVDatabase.get(key);
    }

    public boolean put(long key, byte[] value, int size) throws KVFullException {
        try {
            return this.hwKVDatabase.put(key, value, size);
        } catch (HwKVFullException e) {
            throw new KVFullException();
        }
    }

    public boolean put(String key, byte[] value, int size) throws KVFullException {
        try {
            return this.hwKVDatabase.put(key, value, size);
        } catch (HwKVFullException e) {
            throw new KVFullException();
        }
    }

    public boolean remove(long key) throws KVFullException {
        try {
            return this.hwKVDatabase.remove(key);
        } catch (HwKVFullException e) {
            throw new KVFullException();
        }
    }

    public boolean remove(String key) throws KVFullException {
        try {
            return this.hwKVDatabase.remove(key);
        } catch (HwKVFullException e) {
            throw new KVFullException();
        }
    }

    public Bitmap getBitmap(long key, BitmapFactory.Options options) {
        return this.hwKVDatabase.getBitmap(key, options);
    }

    public boolean hasKey(long key) {
        return this.hwKVDatabase.hasKey(key);
    }

    public boolean hasKey(String key) {
        return this.hwKVDatabase.hasKey(key);
    }

    public KVConnection getKVConnection() {
        KVConnection kvConnection = new KVConnection();
        kvConnection.setHwKVConnection(this.hwKVDatabase);
        return kvConnection;
    }

    public void releaseKVConnection(KVConnection kvConnection, boolean isDatabaseExist) {
        HwKVConnection hwKVConnection = kvConnection.getHwKVConnection();
        if (hwKVConnection != null) {
            this.hwKVDatabase.releaseKVConnection(hwKVConnection, isDatabaseExist);
        }
    }

    public int getKeyNum() {
        return this.hwKVDatabase.getKeyNum();
    }

    public Hashtable<Long, Long> getAllKeys() {
        return this.hwKVDatabase.getAllKeys();
    }

    public Set<String> getKeys() {
        return this.hwKVDatabase.getKeys();
    }

    public void closeAllConnections() {
        this.hwKVDatabase.closeAllConnections();
    }
}
