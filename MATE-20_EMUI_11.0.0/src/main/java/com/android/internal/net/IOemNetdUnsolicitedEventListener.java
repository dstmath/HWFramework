package com.android.internal.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOemNetdUnsolicitedEventListener extends IInterface {
    void OnDnsResultChanged(int i, String str, int i2, String[] strArr, String[] strArr2) throws RemoteException;

    void onApkDownloadUrlDetected(String[] strArr, String str) throws RemoteException;

    void onDataSpeedSlowDetected(String[] strArr, String str) throws RemoteException;

    void onDnsStatReportResult(String str, int i, int[] iArr, int[] iArr2, int[] iArr3) throws RemoteException;

    void onNetBoosterKsiReport(int i, int i2, int i3, int i4) throws RemoteException;

    void onNetBoosterVodReport(int[] iArr) throws RemoteException;

    void onRegistered() throws RemoteException;

    void onWebStatInfoReport(String[] strArr, String str) throws RemoteException;

    public static class Default implements IOemNetdUnsolicitedEventListener {
        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onRegistered() throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onNetBoosterVodReport(int[] videoParams) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onNetBoosterKsiReport(int slowType, int avgAmp, int duration, int timeStart) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onDataSpeedSlowDetected(String[] cooked, String raw) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onWebStatInfoReport(String[] cooked, String raw) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onApkDownloadUrlDetected(String[] cooked, String raw) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void OnDnsResultChanged(int uid, String hostname, int netType, String[] v4Addrs, String[] v6Addrs) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onDnsStatReportResult(String serverName, int serverNo, int[] failcount, int[] mQuery_A, int[] mQuery_4A) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOemNetdUnsolicitedEventListener {
        private static final String DESCRIPTOR = "com.android.internal.net.IOemNetdUnsolicitedEventListener";
        static final int TRANSACTION_OnDnsResultChanged = 7;
        static final int TRANSACTION_onApkDownloadUrlDetected = 6;
        static final int TRANSACTION_onDataSpeedSlowDetected = 4;
        static final int TRANSACTION_onDnsStatReportResult = 8;
        static final int TRANSACTION_onNetBoosterKsiReport = 3;
        static final int TRANSACTION_onNetBoosterVodReport = 2;
        static final int TRANSACTION_onRegistered = 1;
        static final int TRANSACTION_onWebStatInfoReport = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOemNetdUnsolicitedEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOemNetdUnsolicitedEventListener)) {
                return new Proxy(obj);
            }
            return (IOemNetdUnsolicitedEventListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onRegistered();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onNetBoosterVodReport(data.createIntArray());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onNetBoosterKsiReport(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onDataSpeedSlowDetected(data.createStringArray(), data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onWebStatInfoReport(data.createStringArray(), data.readString());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onApkDownloadUrlDetected(data.createStringArray(), data.readString());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        OnDnsResultChanged(data.readInt(), data.readString(), data.readInt(), data.createStringArray(), data.createStringArray());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onDnsStatReportResult(data.readString(), data.readInt(), data.createIntArray(), data.createIntArray(), data.createIntArray());
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
        public static class Proxy implements IOemNetdUnsolicitedEventListener {
            public static IOemNetdUnsolicitedEventListener sDefaultImpl;
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

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void onRegistered() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRegistered();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void onNetBoosterVodReport(int[] videoParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(videoParams);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNetBoosterVodReport(videoParams);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void onNetBoosterKsiReport(int slowType, int avgAmp, int duration, int timeStart) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slowType);
                    _data.writeInt(avgAmp);
                    _data.writeInt(duration);
                    _data.writeInt(timeStart);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNetBoosterKsiReport(slowType, avgAmp, duration, timeStart);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void onDataSpeedSlowDetected(String[] cooked, String raw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(cooked);
                    _data.writeString(raw);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDataSpeedSlowDetected(cooked, raw);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void onWebStatInfoReport(String[] cooked, String raw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(cooked);
                    _data.writeString(raw);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onWebStatInfoReport(cooked, raw);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void onApkDownloadUrlDetected(String[] cooked, String raw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(cooked);
                    _data.writeString(raw);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onApkDownloadUrlDetected(cooked, raw);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void OnDnsResultChanged(int uid, String hostname, int netType, String[] v4Addrs, String[] v6Addrs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(hostname);
                    _data.writeInt(netType);
                    _data.writeStringArray(v4Addrs);
                    _data.writeStringArray(v6Addrs);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnDnsResultChanged(uid, hostname, netType, v4Addrs, v6Addrs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
            public void onDnsStatReportResult(String serverName, int serverNo, int[] failcount, int[] mQuery_A, int[] mQuery_4A) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serverName);
                    _data.writeInt(serverNo);
                    _data.writeIntArray(failcount);
                    _data.writeIntArray(mQuery_A);
                    _data.writeIntArray(mQuery_4A);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDnsStatReportResult(serverName, serverNo, failcount, mQuery_A, mQuery_4A);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOemNetdUnsolicitedEventListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOemNetdUnsolicitedEventListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
