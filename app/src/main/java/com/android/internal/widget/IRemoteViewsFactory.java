package com.android.internal.widget;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.RemoteViews;

public interface IRemoteViewsFactory extends IInterface {

    public static abstract class Stub extends Binder implements IRemoteViewsFactory {
        private static final String DESCRIPTOR = "com.android.internal.widget.IRemoteViewsFactory";
        static final int TRANSACTION_getCount = 4;
        static final int TRANSACTION_getItemId = 8;
        static final int TRANSACTION_getLoadingView = 6;
        static final int TRANSACTION_getViewAt = 5;
        static final int TRANSACTION_getViewTypeCount = 7;
        static final int TRANSACTION_hasStableIds = 9;
        static final int TRANSACTION_isCreated = 10;
        static final int TRANSACTION_onDataSetChanged = 1;
        static final int TRANSACTION_onDataSetChangedAsync = 2;
        static final int TRANSACTION_onDestroy = 3;

        private static class Proxy implements IRemoteViewsFactory {
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

            public void onDataSetChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onDataSetChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onDataSetChangedAsync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onDataSetChangedAsync, _data, null, Stub.TRANSACTION_onDataSetChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onDestroy(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_onDataSetChanged);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDestroy, _data, null, Stub.TRANSACTION_onDataSetChanged);
                } finally {
                    _data.recycle();
                }
            }

            public int getCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RemoteViews getViewAt(int position) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    RemoteViews remoteViews;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(position);
                    this.mRemote.transact(Stub.TRANSACTION_getViewAt, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        remoteViews = (RemoteViews) RemoteViews.CREATOR.createFromParcel(_reply);
                    } else {
                        remoteViews = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return remoteViews;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RemoteViews getLoadingView() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    RemoteViews remoteViews;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLoadingView, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        remoteViews = (RemoteViews) RemoteViews.CREATOR.createFromParcel(_reply);
                    } else {
                        remoteViews = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return remoteViews;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getViewTypeCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getViewTypeCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getItemId(int position) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(position);
                    this.mRemote.transact(Stub.TRANSACTION_getItemId, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasStableIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hasStableIds, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCreated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isCreated, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteViewsFactory asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteViewsFactory)) {
                return new Proxy(obj);
            }
            return (IRemoteViewsFactory) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            int _result;
            RemoteViews _result2;
            boolean _result3;
            switch (code) {
                case TRANSACTION_onDataSetChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDataSetChanged();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onDataSetChangedAsync /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDataSetChangedAsync();
                    return true;
                case TRANSACTION_onDestroy /*3*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    onDestroy(intent);
                    return true;
                case TRANSACTION_getCount /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCount();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getViewAt /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getViewAt(data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_onDataSetChanged);
                        _result2.writeToParcel(reply, TRANSACTION_onDataSetChanged);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getLoadingView /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLoadingView();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_onDataSetChanged);
                        _result2.writeToParcel(reply, TRANSACTION_onDataSetChanged);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getViewTypeCount /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getViewTypeCount();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getItemId /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result4 = getItemId(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case TRANSACTION_hasStableIds /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = hasStableIds();
                    reply.writeNoException();
                    if (_result3) {
                        i = TRANSACTION_onDataSetChanged;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_isCreated /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isCreated();
                    reply.writeNoException();
                    if (_result3) {
                        i = TRANSACTION_onDataSetChanged;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int getCount() throws RemoteException;

    long getItemId(int i) throws RemoteException;

    RemoteViews getLoadingView() throws RemoteException;

    RemoteViews getViewAt(int i) throws RemoteException;

    int getViewTypeCount() throws RemoteException;

    boolean hasStableIds() throws RemoteException;

    boolean isCreated() throws RemoteException;

    void onDataSetChanged() throws RemoteException;

    void onDataSetChangedAsync() throws RemoteException;

    void onDestroy(Intent intent) throws RemoteException;
}
