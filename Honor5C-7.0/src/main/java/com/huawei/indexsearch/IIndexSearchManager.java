package com.huawei.indexsearch;

import android.database.Cursor;
import android.os.IInterface;
import android.os.RemoteException;
import java.util.List;

public interface IIndexSearchManager extends IInterface {
    public static final int BOOT_COMPLETED = 6;
    public static final int BUILD_INDEX_TRANSACTION = 2;
    public static final int FORCE_STOP_BUILD_TRANSACTION = 5;
    public static final int SEARCH_TRANSACTION = 3;
    public static final int SEARCH_WITH_FIELD_TRANSACTION = 4;
    public static final int TRIGGER_IDLE = 7;
    public static final String descriptor = "android.app.IIndexSearchManager";

    void bootCompleted() throws RemoteException;

    void buildIndex(List<CachedItem> list) throws RemoteException;

    void forceStopBuildIndex(String str, long j) throws RemoteException;

    Cursor search(String str, String str2) throws RemoteException;

    Cursor search(String str, String str2, String str3) throws RemoteException;

    void triggerIdle() throws RemoteException;
}
