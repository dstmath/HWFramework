package com.android.server.backup.remote;

import android.os.RemoteException;

@FunctionalInterface
public interface RemoteCallable<T> {
    void call(T t) throws RemoteException;
}
