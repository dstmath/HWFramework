package com.huawei.nb.notification;

import android.os.RemoteException;
import com.huawei.nb.environment.Disposable;

public interface Observable<T> extends Disposable {
    boolean dispatchChange(Variable variable);

    boolean notifyChange(Variable variable);

    boolean registerObserver(T t) throws RemoteException;

    void unregisterAll();

    boolean unregisterObserver(T t) throws RemoteException;
}
