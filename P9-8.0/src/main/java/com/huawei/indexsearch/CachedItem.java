package com.huawei.indexsearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import android.util.Pools.Pool;
import android.util.Pools.SynchronizedPool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CachedItem implements Parcelable {
    public static final Creator<CachedItem> CREATOR = new Creator<CachedItem>() {
        public CachedItem createFromParcel(Parcel in) {
            return new CachedItem(in, null);
        }

        public CachedItem[] newArray(int size) {
            return new CachedItem[size];
        }
    };
    private static final String TAG = "CachedItem";
    static final Pool<CachedItem> mPool = new SynchronizedPool(50);
    public List<String> idList;
    public int op;
    public String pkgName;
    public int userId;
    public long when;

    /* synthetic */ CachedItem(Parcel in, CachedItem -this1) {
        this(in);
    }

    public CachedItem(String pkgName, int _op, String id, int userId) {
        this.userId = -10000;
        List<String> idList = new ArrayList();
        idList.add(id);
        this.pkgName = pkgName;
        this.op = _op;
        this.idList = idList;
        this.userId = userId;
    }

    public CachedItem(String pkgName, int _op, List<String> idList, int userId) {
        this.userId = -10000;
        this.pkgName = pkgName;
        this.op = _op;
        this.idList = idList;
        this.userId = userId;
    }

    public CachedItem(String pkgName, int _op, List<String> idList, int userId, long when) {
        this(pkgName, _op, (List) idList, userId);
        this.when = when;
    }

    private CachedItem(Parcel in) {
        this.userId = -10000;
        this.pkgName = in.readString();
        this.op = in.readInt();
        this.idList = in.createStringArrayList();
        this.userId = in.readInt();
        this.when = in.readLong();
    }

    public static CachedItem obtain(String pkgName, String id, int op, int userId) {
        CachedItem item = (CachedItem) mPool.acquire();
        if (item == null) {
            return new CachedItem(pkgName, op, id, userId);
        }
        item.pkgName = pkgName;
        item.op = op;
        item.idList = new ArrayList();
        item.idList.add(id);
        return item;
    }

    public static CachedItem obtain(String pkgName, String[] ids, int op, int userId) {
        CachedItem item = (CachedItem) mPool.acquire();
        if (item == null) {
            item = new CachedItem(pkgName, op, Arrays.asList(ids), userId);
        }
        item.pkgName = pkgName;
        item.op = op;
        item.idList = Arrays.asList(ids);
        return item;
    }

    public static CachedItem obtain(String pkgName, String[] ids, int op, int userId, long when) {
        CachedItem item = (CachedItem) mPool.acquire();
        if (item == null) {
            item = new CachedItem(pkgName, op, Arrays.asList(ids), userId);
        }
        item.pkgName = pkgName;
        item.op = op;
        item.when = when;
        item.idList = Arrays.asList(ids);
        return item;
    }

    public static CachedItem obtain(String pkgName, String id, int op, int userId, long when) {
        CachedItem item = obtain(pkgName, id, op, userId);
        item.when = when;
        return item;
    }

    public void recycle() {
        try {
            mPool.release(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "this pool is already recycle");
        }
    }

    public String getPackageName() {
        return this.pkgName;
    }

    public List<String> getIdList() {
        return this.idList;
    }

    public void setPackageName(String _pkgName) {
        this.pkgName = _pkgName;
    }

    void setOp(int _op) {
        this.op = _op;
    }

    int getOp() {
        return this.op;
    }

    void setUserId(int _userId) {
        this.userId = _userId;
    }

    int getUserId() {
        return this.userId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pkgName);
        dest.writeInt(this.op);
        dest.writeStringList(this.idList);
        dest.writeInt(this.userId);
        dest.writeLong(this.when);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.pkgName).append("|").append(this.op).append("|").append(this.userId).append("|").append(this.when).append(" ");
        sb.append("idList:").append(this.idList);
        return sb.toString();
    }
}
