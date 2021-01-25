package com.huawei.android.bastet;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.bastet.IBastetListener;

public interface IBastetManager extends IInterface {
    void broadcastReceived(int i) throws RemoteException;

    int clearProxyById(int i) throws RemoteException;

    int configAppUidList(int[] iArr) throws RemoteException;

    int configDataAccelerate(int i, int i2) throws RemoteException;

    void hrtAppActivity(int i, int i2) throws RemoteException;

    int indicateAction(int i, int i2, int i3) throws RemoteException;

    int initEmailProxy(int i, String str, int i2, int i3, int i4, String str2, String str3, IBastetListener iBastetListener) throws RemoteException;

    int initHeartbeatProxy(int i, int i2, IBastetListener iBastetListener) throws RemoteException;

    int initHwBastetService(IBastetListener iBastetListener) throws RemoteException;

    int inquireNetworkQuality() throws RemoteException;

    void ipv6AddressUpdateReceived(int i, String str, String str2) throws RemoteException;

    boolean isBastetSupportIpv6() throws RemoteException;

    boolean isProxyProtocolSupport(int i) throws RemoteException;

    boolean isProxyServiceAvailable() throws RemoteException;

    int notifyAlarmTimeout(int i) throws RemoteException;

    int notifyElapsedRealTime(int i, long j) throws RemoteException;

    int notifyNrtTimeout() throws RemoteException;

    void packageChangedReceived(int i, String str) throws RemoteException;

    int prepareHeartbeatProxy(int i, IBastetListener iBastetListener) throws RemoteException;

    int prepareHeartbeatProxyIpv6(int i, IBastetListener iBastetListener) throws RemoteException;

    int setDeviceId(int i, String str) throws RemoteException;

    int setExchangeHttpHeader(int i, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    int setFilterInfo(int i, int i2) throws RemoteException;

    int setHeartbeatCheckType(int i, int i2) throws RemoteException;

    int setHeartbeatFixedContent(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    int setHeartbeatSequence(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int setHeartbeatSocketHoldTime(int i, long j) throws RemoteException;

    int setHeartbeatTimeout(int i, long j, int i2) throws RemoteException;

    int setImapIdCmd(int i, String str) throws RemoteException;

    int setNrtTime(int i, long j) throws RemoteException;

    int setReconnEnable(int i, boolean z) throws RemoteException;

    int startBastetProxy(int i) throws RemoteException;

    int startBastetProxyIpv6(int i) throws RemoteException;

    int stopBastetProxy(int i) throws RemoteException;

    int updateEmailBoxInfo(int i, String str, String str2) throws RemoteException;

    int updateExchangeWebXmlInfo(int i, String str, String str2, int i2) throws RemoteException;

    int updateHeartbeatFileDescriptor(int i, int i2) throws RemoteException;

    int updateRepeatInterval(int i, int i2) throws RemoteException;

    int updateServerInfo(int i, String str, int i2, int i3) throws RemoteException;

    public static class Default implements IBastetManager {
        @Override // com.huawei.android.bastet.IBastetManager
        public boolean isProxyServiceAvailable() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public boolean isProxyProtocolSupport(int protocol) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int initHeartbeatProxy(int fd, int intervalType, IBastetListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int initEmailProxy(int emailType, String addr, int port, int securityMode, int intervalType, String account, String pwd, IBastetListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int startBastetProxy(int proxyId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int stopBastetProxy(int proxyId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int clearProxyById(int proxyId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setHeartbeatFixedContent(int proxyId, byte[] send, byte[] reply) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setHeartbeatSocketHoldTime(int proxyId, long timeMillis) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setHeartbeatSequence(int proxyId, int seqStart, int seqStep, int seqMin, int seqMax) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setHeartbeatCheckType(int proxyId, int checkType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setHeartbeatTimeout(int proxyId, long timeoutMillis, int maxSendCount) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setImapIdCmd(int proxyId, String idCmd) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setExchangeHttpHeader(int proxyId, String httpProtoVer, String userAgent, String encoding, String policyKey, String hostName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setDeviceId(int proxyId, String deviceId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int updateHeartbeatFileDescriptor(int proxyId, int fd) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int updateEmailBoxInfo(int proxyId, String folderName, String boxLatestUid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int updateExchangeWebXmlInfo(int proxyId, String collectionId, String syncKey, int syncType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int updateRepeatInterval(int proxyId, int intervalType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int updateServerInfo(int proxyId, String addr, int port, int securityMode) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int notifyElapsedRealTime(int proxyId, long elapsedTime) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setReconnEnable(int proxyId, boolean enable) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setNrtTime(int proxyId, long timeout) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int notifyNrtTimeout() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int inquireNetworkQuality() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int prepareHeartbeatProxy(int fd, IBastetListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int initHwBastetService(IBastetListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public void broadcastReceived(int action) throws RemoteException {
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public void packageChangedReceived(int action, String name) throws RemoteException {
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public void hrtAppActivity(int activity, int uid) throws RemoteException {
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int indicateAction(int value, int action, int reserve) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int setFilterInfo(int action, int pid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int configAppUidList(int[] uidList) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int notifyAlarmTimeout(int operation) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int configDataAccelerate(int uid, int usAppState) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public boolean isBastetSupportIpv6() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int prepareHeartbeatProxyIpv6(int fd, IBastetListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public int startBastetProxyIpv6(int proxyId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.bastet.IBastetManager
        public void ipv6AddressUpdateReceived(int action, String interfaceName, String ipv6Address) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBastetManager {
        private static final String DESCRIPTOR = "com.huawei.android.bastet.IBastetManager";
        static final int TRANSACTION_broadcastReceived = 28;
        static final int TRANSACTION_clearProxyById = 7;
        static final int TRANSACTION_configAppUidList = 33;
        static final int TRANSACTION_configDataAccelerate = 35;
        static final int TRANSACTION_hrtAppActivity = 30;
        static final int TRANSACTION_indicateAction = 31;
        static final int TRANSACTION_initEmailProxy = 4;
        static final int TRANSACTION_initHeartbeatProxy = 3;
        static final int TRANSACTION_initHwBastetService = 27;
        static final int TRANSACTION_inquireNetworkQuality = 25;
        static final int TRANSACTION_ipv6AddressUpdateReceived = 39;
        static final int TRANSACTION_isBastetSupportIpv6 = 36;
        static final int TRANSACTION_isProxyProtocolSupport = 2;
        static final int TRANSACTION_isProxyServiceAvailable = 1;
        static final int TRANSACTION_notifyAlarmTimeout = 34;
        static final int TRANSACTION_notifyElapsedRealTime = 21;
        static final int TRANSACTION_notifyNrtTimeout = 24;
        static final int TRANSACTION_packageChangedReceived = 29;
        static final int TRANSACTION_prepareHeartbeatProxy = 26;
        static final int TRANSACTION_prepareHeartbeatProxyIpv6 = 37;
        static final int TRANSACTION_setDeviceId = 15;
        static final int TRANSACTION_setExchangeHttpHeader = 14;
        static final int TRANSACTION_setFilterInfo = 32;
        static final int TRANSACTION_setHeartbeatCheckType = 11;
        static final int TRANSACTION_setHeartbeatFixedContent = 8;
        static final int TRANSACTION_setHeartbeatSequence = 10;
        static final int TRANSACTION_setHeartbeatSocketHoldTime = 9;
        static final int TRANSACTION_setHeartbeatTimeout = 12;
        static final int TRANSACTION_setImapIdCmd = 13;
        static final int TRANSACTION_setNrtTime = 23;
        static final int TRANSACTION_setReconnEnable = 22;
        static final int TRANSACTION_startBastetProxy = 5;
        static final int TRANSACTION_startBastetProxyIpv6 = 38;
        static final int TRANSACTION_stopBastetProxy = 6;
        static final int TRANSACTION_updateEmailBoxInfo = 17;
        static final int TRANSACTION_updateExchangeWebXmlInfo = 18;
        static final int TRANSACTION_updateHeartbeatFileDescriptor = 16;
        static final int TRANSACTION_updateRepeatInterval = 19;
        static final int TRANSACTION_updateServerInfo = 20;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBastetManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBastetManager)) {
                return new Proxy(obj);
            }
            return (IBastetManager) iin;
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
                        boolean isProxyServiceAvailable = isProxyServiceAvailable();
                        reply.writeNoException();
                        reply.writeInt(isProxyServiceAvailable ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isProxyProtocolSupport = isProxyProtocolSupport(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isProxyProtocolSupport ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = initHeartbeatProxy(data.readInt(), data.readInt(), IBastetListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = initEmailProxy(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), IBastetListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = startBastetProxy(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = stopBastetProxy(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = clearProxyById(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = setHeartbeatFixedContent(data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = setHeartbeatSocketHoldTime(data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = setHeartbeatSequence(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = setHeartbeatCheckType(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = setHeartbeatTimeout(data.readInt(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = setImapIdCmd(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = setExchangeHttpHeader(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = setDeviceId(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = updateHeartbeatFileDescriptor(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = updateEmailBoxInfo(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = updateExchangeWebXmlInfo(data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = updateRepeatInterval(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = updateServerInfo(data.readInt(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = notifyElapsedRealTime(data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = setReconnEnable(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = setNrtTime(data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = notifyNrtTimeout();
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = inquireNetworkQuality();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = prepareHeartbeatProxy(data.readInt(), IBastetListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = initHwBastetService(IBastetListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        broadcastReceived(data.readInt());
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        packageChangedReceived(data.readInt(), data.readString());
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        hrtAppActivity(data.readInt(), data.readInt());
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = indicateAction(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = setFilterInfo(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = configAppUidList(data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = notifyAlarmTimeout(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result30 = configDataAccelerate(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBastetSupportIpv6 = isBastetSupportIpv6();
                        reply.writeNoException();
                        reply.writeInt(isBastetSupportIpv6 ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = prepareHeartbeatProxyIpv6(data.readInt(), IBastetListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = startBastetProxyIpv6(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        ipv6AddressUpdateReceived(data.readInt(), data.readString(), data.readString());
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
        public static class Proxy implements IBastetManager {
            public static IBastetManager sDefaultImpl;
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

            @Override // com.huawei.android.bastet.IBastetManager
            public boolean isProxyServiceAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isProxyServiceAvailable();
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

            @Override // com.huawei.android.bastet.IBastetManager
            public boolean isProxyProtocolSupport(int protocol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(protocol);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isProxyProtocolSupport(protocol);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int initHeartbeatProxy(int fd, int intervalType, IBastetListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(intervalType);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().initHeartbeatProxy(fd, intervalType, listener);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int initEmailProxy(int emailType, String addr, int port, int securityMode, int intervalType, String account, String pwd, IBastetListener listener) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(emailType);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(addr);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(port);
                        try {
                            _data.writeInt(securityMode);
                            _data.writeInt(intervalType);
                            _data.writeString(account);
                            _data.writeString(pwd);
                            _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                            if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int initEmailProxy = Stub.getDefaultImpl().initEmailProxy(emailType, addr, port, securityMode, intervalType, account, pwd, listener);
                            _reply.recycle();
                            _data.recycle();
                            return initEmailProxy;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.bastet.IBastetManager
            public int startBastetProxy(int proxyId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startBastetProxy(proxyId);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int stopBastetProxy(int proxyId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopBastetProxy(proxyId);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int clearProxyById(int proxyId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearProxyById(proxyId);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setHeartbeatFixedContent(int proxyId, byte[] send, byte[] reply) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeByteArray(send);
                    _data.writeByteArray(reply);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHeartbeatFixedContent(proxyId, send, reply);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setHeartbeatSocketHoldTime(int proxyId, long timeMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeLong(timeMillis);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHeartbeatSocketHoldTime(proxyId, timeMillis);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setHeartbeatSequence(int proxyId, int seqStart, int seqStep, int seqMin, int seqMax) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(seqStart);
                    _data.writeInt(seqStep);
                    _data.writeInt(seqMin);
                    _data.writeInt(seqMax);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHeartbeatSequence(proxyId, seqStart, seqStep, seqMin, seqMax);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setHeartbeatCheckType(int proxyId, int checkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(checkType);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHeartbeatCheckType(proxyId, checkType);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setHeartbeatTimeout(int proxyId, long timeoutMillis, int maxSendCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeLong(timeoutMillis);
                    _data.writeInt(maxSendCount);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHeartbeatTimeout(proxyId, timeoutMillis, maxSendCount);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setImapIdCmd(int proxyId, String idCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(idCmd);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setImapIdCmd(proxyId, idCmd);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setExchangeHttpHeader(int proxyId, String httpProtoVer, String userAgent, String encoding, String policyKey, String hostName) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(proxyId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(httpProtoVer);
                        try {
                            _data.writeString(userAgent);
                            try {
                                _data.writeString(encoding);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(policyKey);
                        try {
                            _data.writeString(hostName);
                            if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int exchangeHttpHeader = Stub.getDefaultImpl().setExchangeHttpHeader(proxyId, httpProtoVer, userAgent, encoding, policyKey, hostName);
                            _reply.recycle();
                            _data.recycle();
                            return exchangeHttpHeader;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.bastet.IBastetManager
            public int setDeviceId(int proxyId, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(deviceId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceId(proxyId, deviceId);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int updateHeartbeatFileDescriptor(int proxyId, int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(fd);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateHeartbeatFileDescriptor(proxyId, fd);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int updateEmailBoxInfo(int proxyId, String folderName, String boxLatestUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(folderName);
                    _data.writeString(boxLatestUid);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateEmailBoxInfo(proxyId, folderName, boxLatestUid);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int updateExchangeWebXmlInfo(int proxyId, String collectionId, String syncKey, int syncType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(collectionId);
                    _data.writeString(syncKey);
                    _data.writeInt(syncType);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateExchangeWebXmlInfo(proxyId, collectionId, syncKey, syncType);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int updateRepeatInterval(int proxyId, int intervalType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(intervalType);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateRepeatInterval(proxyId, intervalType);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int updateServerInfo(int proxyId, String addr, int port, int securityMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(addr);
                    _data.writeInt(port);
                    _data.writeInt(securityMode);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateServerInfo(proxyId, addr, port, securityMode);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int notifyElapsedRealTime(int proxyId, long elapsedTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeLong(elapsedTime);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyElapsedRealTime(proxyId, elapsedTime);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setReconnEnable(int proxyId, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setReconnEnable(proxyId, enable);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setNrtTime(int proxyId, long timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeLong(timeout);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNrtTime(proxyId, timeout);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int notifyNrtTimeout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyNrtTimeout();
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int inquireNetworkQuality() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().inquireNetworkQuality();
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int prepareHeartbeatProxy(int fd, IBastetListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().prepareHeartbeatProxy(fd, listener);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int initHwBastetService(IBastetListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().initHwBastetService(listener);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public void broadcastReceived(int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    if (this.mRemote.transact(28, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().broadcastReceived(action);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.bastet.IBastetManager
            public void packageChangedReceived(int action, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeString(name);
                    if (this.mRemote.transact(29, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().packageChangedReceived(action, name);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.bastet.IBastetManager
            public void hrtAppActivity(int activity, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(activity);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(30, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hrtAppActivity(activity, uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.bastet.IBastetManager
            public int indicateAction(int value, int action, int reserve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    _data.writeInt(action);
                    _data.writeInt(reserve);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().indicateAction(value, action, reserve);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int setFilterInfo(int action, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeInt(pid);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setFilterInfo(action, pid);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int configAppUidList(int[] uidList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uidList);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configAppUidList(uidList);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int notifyAlarmTimeout(int operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operation);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyAlarmTimeout(operation);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int configDataAccelerate(int uid, int usAppState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(usAppState);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configDataAccelerate(uid, usAppState);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public boolean isBastetSupportIpv6() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBastetSupportIpv6();
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int prepareHeartbeatProxyIpv6(int fd, IBastetListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().prepareHeartbeatProxyIpv6(fd, listener);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public int startBastetProxyIpv6(int proxyId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startBastetProxyIpv6(proxyId);
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

            @Override // com.huawei.android.bastet.IBastetManager
            public void ipv6AddressUpdateReceived(int action, String interfaceName, String ipv6Address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeString(interfaceName);
                    _data.writeString(ipv6Address);
                    if (this.mRemote.transact(39, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().ipv6AddressUpdateReceived(action, interfaceName, ipv6Address);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBastetManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBastetManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
