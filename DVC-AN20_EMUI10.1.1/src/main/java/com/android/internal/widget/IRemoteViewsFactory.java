package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.RemoteViews;

public interface IRemoteViewsFactory extends IInterface {
    @UnsupportedAppUsage
    int getCount() throws RemoteException;

    @UnsupportedAppUsage
    long getItemId(int i) throws RemoteException;

    @UnsupportedAppUsage
    RemoteViews getLoadingView() throws RemoteException;

    @UnsupportedAppUsage
    RemoteViews getViewAt(int i) throws RemoteException;

    @UnsupportedAppUsage
    int getViewTypeCount() throws RemoteException;

    @UnsupportedAppUsage
    boolean hasStableIds() throws RemoteException;

    @UnsupportedAppUsage
    boolean isCreated() throws RemoteException;

    @UnsupportedAppUsage
    void onDataSetChanged() throws RemoteException;

    void onDataSetChangedAsync() throws RemoteException;

    void onDestroy(Intent intent) throws RemoteException;

    public static class Default implements IRemoteViewsFactory {
        @Override // com.android.internal.widget.IRemoteViewsFactory
        public void onDataSetChanged() throws RemoteException {
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public void onDataSetChangedAsync() throws RemoteException {
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public void onDestroy(Intent intent) throws RemoteException {
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public int getCount() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public RemoteViews getViewAt(int position) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public RemoteViews getLoadingView() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public int getViewTypeCount() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public long getItemId(int position) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public boolean hasStableIds() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.IRemoteViewsFactory
        public boolean isCreated() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onDataSetChanged";
                case 2:
                    return "onDataSetChangedAsync";
                case 3:
                    return "onDestroy";
                case 4:
                    return "getCount";
                case 5:
                    return "getViewAt";
                case 6:
                    return "getLoadingView";
                case 7:
                    return "getViewTypeCount";
                case 8:
                    return "getItemId";
                case 9:
                    return "hasStableIds";
                case 10:
                    return "isCreated";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onDataSetChanged();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onDataSetChangedAsync();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onDestroy(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getCount();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        RemoteViews _result2 = getViewAt(data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        RemoteViews _result3 = getLoadingView();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getViewTypeCount();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        long _result5 = getItemId(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasStableIds = hasStableIds();
                        reply.writeNoException();
                        reply.writeInt(hasStableIds ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCreated = isCreated();
                        reply.writeNoException();
                        reply.writeInt(isCreated ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRemoteViewsFactory {
            public static IRemoteViewsFactory sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public void onDataSetChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDataSetChanged();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public void onDataSetChangedAsync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDataSetChangedAsync();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public void onDestroy(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDestroy(intent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public int getCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCount();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public RemoteViews getViewAt(int position) throws RemoteException {
                RemoteViews _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(position);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getViewAt(position);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RemoteViews.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public RemoteViews getLoadingView() throws RemoteException {
                RemoteViews _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLoadingView();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RemoteViews.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public int getViewTypeCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getViewTypeCount();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public long getItemId(int position) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(position);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getItemId(position);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public boolean hasStableIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasStableIds();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.IRemoteViewsFactory
            public boolean isCreated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCreated();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRemoteViewsFactory impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRemoteViewsFactory getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
