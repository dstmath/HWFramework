package com.huawei.android.bastet;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBastetManager extends IInterface {

    public static abstract class Stub extends Binder implements IBastetManager {
        private static final String DESCRIPTOR = "com.huawei.android.bastet.IBastetManager";
        static final int TRANSACTION_broadcastReceived = 27;
        static final int TRANSACTION_clearProxyById = 7;
        static final int TRANSACTION_configAppUidList = 32;
        static final int TRANSACTION_configBstBlackList = 34;
        static final int TRANSACTION_deleteBstBlackListNum = 35;
        static final int TRANSACTION_hrtAppActivity = 29;
        static final int TRANSACTION_indicateAction = 30;
        static final int TRANSACTION_initEmailProxy = 4;
        static final int TRANSACTION_initHeartbeatProxy = 3;
        static final int TRANSACTION_initHwBastetService = 26;
        static final int TRANSACTION_inquireNetworkQuality = 24;
        static final int TRANSACTION_isProxyProtocolSupport = 2;
        static final int TRANSACTION_isProxyServiceAvailable = 1;
        static final int TRANSACTION_notifyAlarmTimeout = 33;
        static final int TRANSACTION_notifyNrtTimeout = 23;
        static final int TRANSACTION_packageChangedReceived = 28;
        static final int TRANSACTION_prepareHeartbeatProxy = 25;
        static final int TRANSACTION_setBstBarredRule = 36;
        static final int TRANSACTION_setBstBarredSwitch = 37;
        static final int TRANSACTION_setDeviceId = 15;
        static final int TRANSACTION_setExchangeHttpHeader = 14;
        static final int TRANSACTION_setFilterInfo = 31;
        static final int TRANSACTION_setHeartbeatCheckType = 11;
        static final int TRANSACTION_setHeartbeatFixedContent = 8;
        static final int TRANSACTION_setHeartbeatSequence = 10;
        static final int TRANSACTION_setHeartbeatSocketHoldTime = 9;
        static final int TRANSACTION_setHeartbeatTimeout = 12;
        static final int TRANSACTION_setImapIdCmd = 13;
        static final int TRANSACTION_setNrtTime = 22;
        static final int TRANSACTION_setReconnEnable = 21;
        static final int TRANSACTION_startBastetProxy = 5;
        static final int TRANSACTION_stopBastetProxy = 6;
        static final int TRANSACTION_updateEmailBoxInfo = 17;
        static final int TRANSACTION_updateExchangeWebXmlInfo = 18;
        static final int TRANSACTION_updateHeartbeatFileDescriptor = 16;
        static final int TRANSACTION_updateRepeatInterval = 19;
        static final int TRANSACTION_updateServerInfo = 20;

        private static class Proxy implements IBastetManager {
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

            public boolean isProxyServiceAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isProxyServiceAvailable, _data, _reply, 0);
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

            public boolean isProxyProtocolSupport(int protocol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(protocol);
                    this.mRemote.transact(Stub.TRANSACTION_isProxyProtocolSupport, _data, _reply, 0);
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

            public int initHeartbeatProxy(int fd, int intervalType, IBastetListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(intervalType);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_initHeartbeatProxy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int initEmailProxy(int emailType, String addr, int port, int securityMode, int intervalType, String account, String pwd, IBastetListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(emailType);
                    _data.writeString(addr);
                    _data.writeInt(port);
                    _data.writeInt(securityMode);
                    _data.writeInt(intervalType);
                    _data.writeString(account);
                    _data.writeString(pwd);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_initEmailProxy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startBastetProxy(int proxyId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    this.mRemote.transact(Stub.TRANSACTION_startBastetProxy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopBastetProxy(int proxyId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    this.mRemote.transact(Stub.TRANSACTION_stopBastetProxy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int clearProxyById(int proxyId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    this.mRemote.transact(Stub.TRANSACTION_clearProxyById, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setHeartbeatFixedContent(int proxyId, byte[] send, byte[] reply) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeByteArray(send);
                    _data.writeByteArray(reply);
                    this.mRemote.transact(Stub.TRANSACTION_setHeartbeatFixedContent, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setHeartbeatSocketHoldTime(int proxyId, long timeMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeLong(timeMillis);
                    this.mRemote.transact(Stub.TRANSACTION_setHeartbeatSocketHoldTime, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(Stub.TRANSACTION_setHeartbeatSequence, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setHeartbeatCheckType(int proxyId, int checkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(checkType);
                    this.mRemote.transact(Stub.TRANSACTION_setHeartbeatCheckType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setHeartbeatTimeout(int proxyId, long timeoutMillis, int maxSendCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeLong(timeoutMillis);
                    _data.writeInt(maxSendCount);
                    this.mRemote.transact(Stub.TRANSACTION_setHeartbeatTimeout, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setImapIdCmd(int proxyId, String idCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(idCmd);
                    this.mRemote.transact(Stub.TRANSACTION_setImapIdCmd, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setExchangeHttpHeader(int proxyId, String httpProtoVer, String userAgent, String encoding, String policyKey, String hostName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(httpProtoVer);
                    _data.writeString(userAgent);
                    _data.writeString(encoding);
                    _data.writeString(policyKey);
                    _data.writeString(hostName);
                    this.mRemote.transact(Stub.TRANSACTION_setExchangeHttpHeader, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDeviceId(int proxyId, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(deviceId);
                    this.mRemote.transact(Stub.TRANSACTION_setDeviceId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateHeartbeatFileDescriptor(int proxyId, int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(fd);
                    this.mRemote.transact(Stub.TRANSACTION_updateHeartbeatFileDescriptor, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateEmailBoxInfo(int proxyId, String folderName, String boxLatestUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(folderName);
                    _data.writeString(boxLatestUid);
                    this.mRemote.transact(Stub.TRANSACTION_updateEmailBoxInfo, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateExchangeWebXmlInfo(int proxyId, String collectionId, String syncKey, int syncType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(collectionId);
                    _data.writeString(syncKey);
                    _data.writeInt(syncType);
                    this.mRemote.transact(Stub.TRANSACTION_updateExchangeWebXmlInfo, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateRepeatInterval(int proxyId, int intervalType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(intervalType);
                    this.mRemote.transact(Stub.TRANSACTION_updateRepeatInterval, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateServerInfo(int proxyId, String addr, int port, int securityMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeString(addr);
                    _data.writeInt(port);
                    _data.writeInt(securityMode);
                    this.mRemote.transact(Stub.TRANSACTION_updateServerInfo, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setReconnEnable(int proxyId, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    if (enable) {
                        i = Stub.TRANSACTION_isProxyServiceAvailable;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setReconnEnable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setNrtTime(int proxyId, long timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeLong(timeout);
                    this.mRemote.transact(Stub.TRANSACTION_setNrtTime, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyNrtTimeout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_notifyNrtTimeout, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int inquireNetworkQuality() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_inquireNetworkQuality, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int prepareHeartbeatProxy(int fd, IBastetListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_prepareHeartbeatProxy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int initHwBastetService(IBastetListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_initHwBastetService, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int broadcastReceived(int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    this.mRemote.transact(Stub.TRANSACTION_broadcastReceived, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int packageChangedReceived(int action, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_packageChangedReceived, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int hrtAppActivity(int activity, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(activity);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_hrtAppActivity, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int indicateAction(int value, int action, int reserve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    _data.writeInt(action);
                    _data.writeInt(reserve);
                    this.mRemote.transact(Stub.TRANSACTION_indicateAction, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setFilterInfo(int action, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeInt(pid);
                    this.mRemote.transact(Stub.TRANSACTION_setFilterInfo, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configAppUidList(int[] uidList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uidList);
                    this.mRemote.transact(Stub.TRANSACTION_configAppUidList, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyAlarmTimeout(int operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operation);
                    this.mRemote.transact(Stub.TRANSACTION_notifyAlarmTimeout, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configBstBlackList(int action, String[] blacklist, int[] option) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeStringArray(blacklist);
                    _data.writeIntArray(option);
                    this.mRemote.transact(Stub.TRANSACTION_configBstBlackList, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteBstBlackListNum(String[] blacklist) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(blacklist);
                    this.mRemote.transact(Stub.TRANSACTION_deleteBstBlackListNum, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setBstBarredRule(int rule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rule);
                    this.mRemote.transact(Stub.TRANSACTION_setBstBarredRule, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setBstBarredSwitch(int enable_flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable_flag);
                    this.mRemote.transact(Stub.TRANSACTION_setBstBarredSwitch, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            switch (code) {
                case TRANSACTION_isProxyServiceAvailable /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isProxyServiceAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_isProxyServiceAvailable : 0);
                    return true;
                case TRANSACTION_isProxyProtocolSupport /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isProxyProtocolSupport(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_isProxyServiceAvailable : 0);
                    return true;
                case TRANSACTION_initHeartbeatProxy /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = initHeartbeatProxy(data.readInt(), data.readInt(), com.huawei.android.bastet.IBastetListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_initEmailProxy /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = initEmailProxy(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), com.huawei.android.bastet.IBastetListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_startBastetProxy /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = startBastetProxy(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_stopBastetProxy /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = stopBastetProxy(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_clearProxyById /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = clearProxyById(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setHeartbeatFixedContent /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setHeartbeatFixedContent(data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setHeartbeatSocketHoldTime /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setHeartbeatSocketHoldTime(data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setHeartbeatSequence /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setHeartbeatSequence(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setHeartbeatCheckType /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setHeartbeatCheckType(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setHeartbeatTimeout /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setHeartbeatTimeout(data.readInt(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setImapIdCmd /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setImapIdCmd(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setExchangeHttpHeader /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setExchangeHttpHeader(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDeviceId /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setDeviceId(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateHeartbeatFileDescriptor /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateHeartbeatFileDescriptor(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateEmailBoxInfo /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateEmailBoxInfo(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateExchangeWebXmlInfo /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateExchangeWebXmlInfo(data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateRepeatInterval /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateRepeatInterval(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateServerInfo /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateServerInfo(data.readInt(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setReconnEnable /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setReconnEnable(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setNrtTime /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setNrtTime(data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_notifyNrtTimeout /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = notifyNrtTimeout();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_inquireNetworkQuality /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = inquireNetworkQuality();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_prepareHeartbeatProxy /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = prepareHeartbeatProxy(data.readInt(), com.huawei.android.bastet.IBastetListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_initHwBastetService /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = initHwBastetService(com.huawei.android.bastet.IBastetListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_broadcastReceived /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = broadcastReceived(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_packageChangedReceived /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = packageChangedReceived(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_hrtAppActivity /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = hrtAppActivity(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_indicateAction /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = indicateAction(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setFilterInfo /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setFilterInfo(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_configAppUidList /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = configAppUidList(data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_notifyAlarmTimeout /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = notifyAlarmTimeout(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_configBstBlackList /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = configBstBlackList(data.readInt(), data.createStringArray(), data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_deleteBstBlackListNum /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = deleteBstBlackListNum(data.createStringArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setBstBarredRule /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setBstBarredRule(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setBstBarredSwitch /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setBstBarredSwitch(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int broadcastReceived(int i) throws RemoteException;

    int clearProxyById(int i) throws RemoteException;

    int configAppUidList(int[] iArr) throws RemoteException;

    int configBstBlackList(int i, String[] strArr, int[] iArr) throws RemoteException;

    int deleteBstBlackListNum(String[] strArr) throws RemoteException;

    int hrtAppActivity(int i, int i2) throws RemoteException;

    int indicateAction(int i, int i2, int i3) throws RemoteException;

    int initEmailProxy(int i, String str, int i2, int i3, int i4, String str2, String str3, IBastetListener iBastetListener) throws RemoteException;

    int initHeartbeatProxy(int i, int i2, IBastetListener iBastetListener) throws RemoteException;

    int initHwBastetService(IBastetListener iBastetListener) throws RemoteException;

    int inquireNetworkQuality() throws RemoteException;

    boolean isProxyProtocolSupport(int i) throws RemoteException;

    boolean isProxyServiceAvailable() throws RemoteException;

    int notifyAlarmTimeout(int i) throws RemoteException;

    int notifyNrtTimeout() throws RemoteException;

    int packageChangedReceived(int i, String str) throws RemoteException;

    int prepareHeartbeatProxy(int i, IBastetListener iBastetListener) throws RemoteException;

    int setBstBarredRule(int i) throws RemoteException;

    int setBstBarredSwitch(int i) throws RemoteException;

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

    int stopBastetProxy(int i) throws RemoteException;

    int updateEmailBoxInfo(int i, String str, String str2) throws RemoteException;

    int updateExchangeWebXmlInfo(int i, String str, String str2, int i2) throws RemoteException;

    int updateHeartbeatFileDescriptor(int i, int i2) throws RemoteException;

    int updateRepeatInterval(int i, int i2) throws RemoteException;

    int updateServerInfo(int i, String str, int i2, int i3) throws RemoteException;
}
