package com.android.internal.appwidget;

import android.appwidget.AppWidgetProviderInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.RemoteViews;

public interface IAppWidgetHost extends IInterface {

    public static abstract class Stub extends Binder implements IAppWidgetHost {
        private static final String DESCRIPTOR = "com.android.internal.appwidget.IAppWidgetHost";
        static final int TRANSACTION_providerChanged = 2;
        static final int TRANSACTION_providersChanged = 3;
        static final int TRANSACTION_updateAppWidget = 1;
        static final int TRANSACTION_viewDataChanged = 4;

        private static class Proxy implements IAppWidgetHost {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void updateAppWidget(int appWidgetId, RemoteViews views) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appWidgetId);
                    if (views != null) {
                        _data.writeInt(Stub.TRANSACTION_updateAppWidget);
                        views.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateAppWidget, _data, null, Stub.TRANSACTION_updateAppWidget);
                } finally {
                    _data.recycle();
                }
            }

            public void providerChanged(int appWidgetId, AppWidgetProviderInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appWidgetId);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_updateAppWidget);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_providerChanged, _data, null, Stub.TRANSACTION_updateAppWidget);
                } finally {
                    _data.recycle();
                }
            }

            public void providersChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_providersChanged, _data, null, Stub.TRANSACTION_updateAppWidget);
                } finally {
                    _data.recycle();
                }
            }

            public void viewDataChanged(int appWidgetId, int viewId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appWidgetId);
                    _data.writeInt(viewId);
                    this.mRemote.transact(Stub.TRANSACTION_viewDataChanged, _data, null, Stub.TRANSACTION_updateAppWidget);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppWidgetHost asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppWidgetHost)) {
                return new Proxy(obj);
            }
            return (IAppWidgetHost) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            switch (code) {
                case TRANSACTION_updateAppWidget /*1*/:
                    RemoteViews remoteViews;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        remoteViews = (RemoteViews) RemoteViews.CREATOR.createFromParcel(data);
                    } else {
                        remoteViews = null;
                    }
                    updateAppWidget(_arg0, remoteViews);
                    return true;
                case TRANSACTION_providerChanged /*2*/:
                    AppWidgetProviderInfo appWidgetProviderInfo;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        appWidgetProviderInfo = (AppWidgetProviderInfo) AppWidgetProviderInfo.CREATOR.createFromParcel(data);
                    } else {
                        appWidgetProviderInfo = null;
                    }
                    providerChanged(_arg0, appWidgetProviderInfo);
                    return true;
                case TRANSACTION_providersChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    providersChanged();
                    return true;
                case TRANSACTION_viewDataChanged /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    viewDataChanged(data.readInt(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void providerChanged(int i, AppWidgetProviderInfo appWidgetProviderInfo) throws RemoteException;

    void providersChanged() throws RemoteException;

    void updateAppWidget(int i, RemoteViews remoteViews) throws RemoteException;

    void viewDataChanged(int i, int i2) throws RemoteException;
}
