package com.android.internal.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.net.IOemNetdUnsolicitedEventListener;

public interface IOemNetd extends IInterface {
    void HwNetFilterAddIpRules(String[] strArr, int i) throws RemoteException;

    void HwNetFilterClearIpRules(int i) throws RemoteException;

    void HwNetFilterSetIpRules(String str, int i) throws RemoteException;

    void HwSetArpIgnore(int i, String str) throws RemoteException;

    void SetDnsForwarding(boolean z, String str) throws RemoteException;

    void SetInterfaceProxyArp(boolean z, String str) throws RemoteException;

    void SetMpDnsApp(int i, int i2, String[] strArr) throws RemoteException;

    int getNetdPid() throws RemoteException;

    void hwBandwidthAddNaughtyApps(String str, int i) throws RemoteException;

    void hwBandwidthDisableInterfaceWhitelist(String str) throws RemoteException;

    void hwBandwidthEnableInterfaceWhitelist(String str) throws RemoteException;

    void hwBandwidthRemoveNaughtyApps(String str, int i) throws RemoteException;

    void hwFilterCommand(String[] strArr) throws RemoteException;

    boolean ipTableConfig(int i, int i2, String str) throws RemoteException;

    boolean isAlive() throws RemoteException;

    boolean pgNetFilterSetRule(int i, int[] iArr, int[] iArr2) throws RemoteException;

    void registerOemUnsolicitedEventListener(IOemNetdUnsolicitedEventListener iOemNetdUnsolicitedEventListener) throws RemoteException;

    void setChrAppUid(int i, int i2) throws RemoteException;

    void setNetBoosterAppUid(int i, int i2) throws RemoteException;

    void setNetBoosterKsiEnabled(boolean z) throws RemoteException;

    void setNetBoosterPreDnsAppUid(String[] strArr) throws RemoteException;

    void setNetBoosterPreDnsBrowerUid(int[] iArr) throws RemoteException;

    void setNetBoosterPreDnsDomainName(String[] strArr) throws RemoteException;

    void setNetBoosterRsrpRsrq(int i, int i2) throws RemoteException;

    void setNetBoosterSettingParams(int i, int i2, int i3, int i4) throws RemoteException;

    void setNetBoosterUidForeground(int i, boolean z) throws RemoteException;

    void setNetBoosterVodEnabled(boolean z) throws RemoteException;

    void startPreDnsQuery(int i, String str) throws RemoteException;

    void trafficSwapActiveProcessStatsMap() throws RemoteException;

    public static class Default implements IOemNetd {
        @Override // com.android.internal.net.IOemNetd
        public boolean isAlive() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.net.IOemNetd
        public void startPreDnsQuery(int uid, String host) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void registerOemUnsolicitedEventListener(IOemNetdUnsolicitedEventListener listener) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setChrAppUid(int index, int uid) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterVodEnabled(boolean enable) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterKsiEnabled(boolean enable) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterAppUid(int appUid, int period) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterRsrpRsrq(int rsrp, int rsrq) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterSettingParams(int param1, int param2, int param3, int param4) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterPreDnsAppUid(String[] appInf) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterPreDnsBrowerUid(int[] browserInf) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterPreDnsDomainName(String[] domainNames) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void setNetBoosterUidForeground(int uid, boolean isForeground) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public boolean pgNetFilterSetRule(int cmd, int[] keys, int[] values) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.net.IOemNetd
        public int getNetdPid() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.net.IOemNetd
        public void hwBandwidthAddNaughtyApps(String ifName, int uid) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void hwBandwidthRemoveNaughtyApps(String ifName, int uid) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void hwBandwidthEnableInterfaceWhitelist(String ifName) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void hwBandwidthDisableInterfaceWhitelist(String ifName) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void HwNetFilterSetIpRules(String addr, int whiteOrBlack) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void HwNetFilterAddIpRules(String[] addr, int whiteOrBlack) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void HwNetFilterClearIpRules(int whiteOrBlack) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void hwFilterCommand(String[] parameters) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public boolean ipTableConfig(int enable, int uid, String iface) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.net.IOemNetd
        public void trafficSwapActiveProcessStatsMap() throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void SetMpDnsApp(int action, int uid, String[] hosts) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void SetInterfaceProxyArp(boolean enabled, String ifaceName) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void SetDnsForwarding(boolean enabled, String dnsIp) throws RemoteException {
        }

        @Override // com.android.internal.net.IOemNetd
        public void HwSetArpIgnore(int value, String ifaceName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOemNetd {
        private static final String DESCRIPTOR = "com.android.internal.net.IOemNetd";
        static final int TRANSACTION_HwNetFilterAddIpRules = 21;
        static final int TRANSACTION_HwNetFilterClearIpRules = 22;
        static final int TRANSACTION_HwNetFilterSetIpRules = 20;
        static final int TRANSACTION_HwSetArpIgnore = 29;
        static final int TRANSACTION_SetDnsForwarding = 28;
        static final int TRANSACTION_SetInterfaceProxyArp = 27;
        static final int TRANSACTION_SetMpDnsApp = 26;
        static final int TRANSACTION_getNetdPid = 15;
        static final int TRANSACTION_hwBandwidthAddNaughtyApps = 16;
        static final int TRANSACTION_hwBandwidthDisableInterfaceWhitelist = 19;
        static final int TRANSACTION_hwBandwidthEnableInterfaceWhitelist = 18;
        static final int TRANSACTION_hwBandwidthRemoveNaughtyApps = 17;
        static final int TRANSACTION_hwFilterCommand = 23;
        static final int TRANSACTION_ipTableConfig = 24;
        static final int TRANSACTION_isAlive = 1;
        static final int TRANSACTION_pgNetFilterSetRule = 14;
        static final int TRANSACTION_registerOemUnsolicitedEventListener = 3;
        static final int TRANSACTION_setChrAppUid = 4;
        static final int TRANSACTION_setNetBoosterAppUid = 7;
        static final int TRANSACTION_setNetBoosterKsiEnabled = 6;
        static final int TRANSACTION_setNetBoosterPreDnsAppUid = 10;
        static final int TRANSACTION_setNetBoosterPreDnsBrowerUid = 11;
        static final int TRANSACTION_setNetBoosterPreDnsDomainName = 12;
        static final int TRANSACTION_setNetBoosterRsrpRsrq = 8;
        static final int TRANSACTION_setNetBoosterSettingParams = 9;
        static final int TRANSACTION_setNetBoosterUidForeground = 13;
        static final int TRANSACTION_setNetBoosterVodEnabled = 5;
        static final int TRANSACTION_startPreDnsQuery = 2;
        static final int TRANSACTION_trafficSwapActiveProcessStatsMap = 25;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOemNetd asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOemNetd)) {
                return new Proxy(obj);
            }
            return (IOemNetd) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAlive = isAlive();
                        reply.writeNoException();
                        reply.writeInt(isAlive ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        startPreDnsQuery(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        registerOemUnsolicitedEventListener(IOemNetdUnsolicitedEventListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setChrAppUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setNetBoosterVodEnabled(_arg0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setNetBoosterKsiEnabled(_arg0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setNetBoosterAppUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setNetBoosterRsrpRsrq(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setNetBoosterSettingParams(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        setNetBoosterPreDnsAppUid(data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        setNetBoosterPreDnsBrowerUid(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        setNetBoosterPreDnsDomainName(data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setNetBoosterUidForeground(_arg02, _arg0);
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean pgNetFilterSetRule = pgNetFilterSetRule(data.readInt(), data.createIntArray(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(pgNetFilterSetRule ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getNetdPid();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        hwBandwidthAddNaughtyApps(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        hwBandwidthRemoveNaughtyApps(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        hwBandwidthEnableInterfaceWhitelist(data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        hwBandwidthDisableInterfaceWhitelist(data.readString());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        HwNetFilterSetIpRules(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        HwNetFilterAddIpRules(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        HwNetFilterClearIpRules(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        hwFilterCommand(data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean ipTableConfig = ipTableConfig(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(ipTableConfig ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        trafficSwapActiveProcessStatsMap();
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        SetMpDnsApp(data.readInt(), data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        SetInterfaceProxyArp(_arg0, data.readString());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        SetDnsForwarding(_arg0, data.readString());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        HwSetArpIgnore(data.readInt(), data.readString());
                        reply.writeNoException();
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
        public static class Proxy implements IOemNetd {
            public static IOemNetd sDefaultImpl;
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

            @Override // com.android.internal.net.IOemNetd
            public boolean isAlive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAlive();
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

            @Override // com.android.internal.net.IOemNetd
            public void startPreDnsQuery(int uid, String host) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(host);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startPreDnsQuery(uid, host);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void registerOemUnsolicitedEventListener(IOemNetdUnsolicitedEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerOemUnsolicitedEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setChrAppUid(int index, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setChrAppUid(index, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterVodEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterVodEnabled(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterKsiEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterKsiEnabled(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterAppUid(int appUid, int period) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appUid);
                    _data.writeInt(period);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterAppUid(appUid, period);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterRsrpRsrq(int rsrp, int rsrq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rsrp);
                    _data.writeInt(rsrq);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterRsrpRsrq(rsrp, rsrq);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterSettingParams(int param1, int param2, int param3, int param4) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(param1);
                    _data.writeInt(param2);
                    _data.writeInt(param3);
                    _data.writeInt(param4);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterSettingParams(param1, param2, param3, param4);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterPreDnsAppUid(String[] appInf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(appInf);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterPreDnsAppUid(appInf);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterPreDnsBrowerUid(int[] browserInf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(browserInf);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterPreDnsBrowerUid(browserInf);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterPreDnsDomainName(String[] domainNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(domainNames);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterPreDnsDomainName(domainNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void setNetBoosterUidForeground(int uid, boolean isForeground) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(isForeground ? 1 : 0);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetBoosterUidForeground(uid, isForeground);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public boolean pgNetFilterSetRule(int cmd, int[] keys, int[] values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd);
                    _data.writeIntArray(keys);
                    _data.writeIntArray(values);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pgNetFilterSetRule(cmd, keys, values);
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

            @Override // com.android.internal.net.IOemNetd
            public int getNetdPid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetdPid();
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

            @Override // com.android.internal.net.IOemNetd
            public void hwBandwidthAddNaughtyApps(String ifName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwBandwidthAddNaughtyApps(ifName, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void hwBandwidthRemoveNaughtyApps(String ifName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwBandwidthRemoveNaughtyApps(ifName, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void hwBandwidthEnableInterfaceWhitelist(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwBandwidthEnableInterfaceWhitelist(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void hwBandwidthDisableInterfaceWhitelist(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwBandwidthDisableInterfaceWhitelist(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void HwNetFilterSetIpRules(String addr, int whiteOrBlack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(addr);
                    _data.writeInt(whiteOrBlack);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().HwNetFilterSetIpRules(addr, whiteOrBlack);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void HwNetFilterAddIpRules(String[] addr, int whiteOrBlack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(addr);
                    _data.writeInt(whiteOrBlack);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().HwNetFilterAddIpRules(addr, whiteOrBlack);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void HwNetFilterClearIpRules(int whiteOrBlack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(whiteOrBlack);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().HwNetFilterClearIpRules(whiteOrBlack);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void hwFilterCommand(String[] parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(parameters);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwFilterCommand(parameters);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public boolean ipTableConfig(int enable, int uid, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeInt(uid);
                    _data.writeString(iface);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ipTableConfig(enable, uid, iface);
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

            @Override // com.android.internal.net.IOemNetd
            public void trafficSwapActiveProcessStatsMap() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().trafficSwapActiveProcessStatsMap();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void SetMpDnsApp(int action, int uid, String[] hosts) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeInt(uid);
                    _data.writeStringArray(hosts);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().SetMpDnsApp(action, uid, hosts);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void SetInterfaceProxyArp(boolean enabled, String ifaceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeString(ifaceName);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().SetInterfaceProxyArp(enabled, ifaceName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void SetDnsForwarding(boolean enabled, String dnsIp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeString(dnsIp);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().SetDnsForwarding(enabled, dnsIp);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.net.IOemNetd
            public void HwSetArpIgnore(int value, String ifaceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    _data.writeString(ifaceName);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().HwSetArpIgnore(value, ifaceName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOemNetd impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOemNetd getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
