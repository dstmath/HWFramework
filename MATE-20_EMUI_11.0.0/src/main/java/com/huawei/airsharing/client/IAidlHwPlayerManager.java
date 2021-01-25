package com.huawei.airsharing.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.airsharing.api.AuthenticationInfo;
import com.huawei.airsharing.api.ConfigInfo;
import com.huawei.airsharing.api.ConnectInfo;
import com.huawei.airsharing.api.ERepeatMode;
import com.huawei.airsharing.api.IAidlMediaPlayerListener;
import com.huawei.airsharing.api.IRemoteCtrlEventProcessor;
import com.huawei.airsharing.api.PlayInfo;
import com.huawei.airsharing.api.ProjectionDevice;
import com.huawei.airsharing.client.IAidlHwListener;
import com.huawei.airsharing.client.IAidlKitServiceStatusListener;

public interface IAidlHwPlayerManager extends IInterface {
    boolean appendHiSightExInfo(int i, byte[] bArr, int i2, long j) throws RemoteException;

    boolean castConnectDevice(ConnectInfo connectInfo) throws RemoteException;

    boolean castPlay(PlayInfo playInfo) throws RemoteException;

    void clsHwSharingListener(int i, IAidlHwListener iAidlHwListener) throws RemoteException;

    void clsKitServiceStatusListener() throws RemoteException;

    boolean connectDevice(int i, ProjectionDevice projectionDevice) throws RemoteException;

    String convertFilePathToDmsUrl(String str) throws RemoteException;

    boolean disconnectDevice(int i) throws RemoteException;

    int getHiSightServerPort() throws RemoteException;

    int getMsdpServerPort() throws RemoteException;

    int getSdkVersion() throws RemoteException;

    String getTargetDevIndication(int i) throws RemoteException;

    String getTargetDevName(int i) throws RemoteException;

    ProjectionDevice getTargetProjectionDevice(int i) throws RemoteException;

    int getVolume(int i) throws RemoteException;

    boolean isConnected(int i) throws RemoteException;

    boolean isDisplayConnected(int i) throws RemoteException;

    boolean isDisplayConnecting(int i) throws RemoteException;

    boolean next() throws RemoteException;

    boolean pause(int i) throws RemoteException;

    boolean pauseWifiDisplay(int i) throws RemoteException;

    boolean playMediaItem(int i) throws RemoteException;

    boolean previous() throws RemoteException;

    boolean registerMediaPlayerListener(IAidlMediaPlayerListener iAidlMediaPlayerListener) throws RemoteException;

    boolean resume(int i) throws RemoteException;

    boolean resumeWifiDisplay(int i) throws RemoteException;

    boolean seekTo(int i) throws RemoteException;

    int sendRemoteCtrlData(int i, int i2, byte[] bArr) throws RemoteException;

    boolean setAuthenticationInfo(AuthenticationInfo authenticationInfo) throws RemoteException;

    void setHwSharingListener(int i, IAidlHwListener iAidlHwListener) throws RemoteException;

    void setKitServiceStatusListener(IAidlKitServiceStatusListener iAidlKitServiceStatusListener) throws RemoteException;

    void setRemoteCtrlEventProcessor(IRemoteCtrlEventProcessor iRemoteCtrlEventProcessor) throws RemoteException;

    boolean setRepeatMode(ERepeatMode eRepeatMode) throws RemoteException;

    boolean setVolume(int i, int i2) throws RemoteException;

    boolean setVolumeMute(boolean z) throws RemoteException;

    boolean showProjectionActivity(ConfigInfo configInfo) throws RemoteException;

    boolean startScan(int i) throws RemoteException;

    boolean startScanDevice(int i, boolean z) throws RemoteException;

    boolean stop(int i) throws RemoteException;

    boolean stopScanDevice(int i, boolean z) throws RemoteException;

    void subscribServers(int i, String str) throws RemoteException;

    boolean unregisterMediaPlayerListener() throws RemoteException;

    void unsubscribServers(int i, String str) throws RemoteException;

    boolean updatePlayInfo(PlayInfo playInfo) throws RemoteException;

    public static abstract class Stub extends Binder implements IAidlHwPlayerManager {
        private static final String DESCRIPTOR = "com.huawei.airsharing.client.IAidlHwPlayerManager";
        static final int TRANSACTION_appendHiSightExInfo = 25;
        static final int TRANSACTION_castConnectDevice = 32;
        static final int TRANSACTION_castPlay = 33;
        static final int TRANSACTION_clsHwSharingListener = 6;
        static final int TRANSACTION_clsKitServiceStatusListener = 4;
        static final int TRANSACTION_connectDevice = 14;
        static final int TRANSACTION_convertFilePathToDmsUrl = 30;
        static final int TRANSACTION_disconnectDevice = 15;
        static final int TRANSACTION_getHiSightServerPort = 24;
        static final int TRANSACTION_getMsdpServerPort = 29;
        static final int TRANSACTION_getSdkVersion = 22;
        static final int TRANSACTION_getTargetDevIndication = 17;
        static final int TRANSACTION_getTargetDevName = 16;
        static final int TRANSACTION_getTargetProjectionDevice = 23;
        static final int TRANSACTION_getVolume = 10;
        static final int TRANSACTION_isConnected = 18;
        static final int TRANSACTION_isDisplayConnected = 28;
        static final int TRANSACTION_isDisplayConnecting = 19;
        static final int TRANSACTION_next = 38;
        static final int TRANSACTION_pause = 7;
        static final int TRANSACTION_pauseWifiDisplay = 20;
        static final int TRANSACTION_playMediaItem = 40;
        static final int TRANSACTION_previous = 37;
        static final int TRANSACTION_registerMediaPlayerListener = 34;
        static final int TRANSACTION_resume = 8;
        static final int TRANSACTION_resumeWifiDisplay = 21;
        static final int TRANSACTION_seekTo = 43;
        static final int TRANSACTION_sendRemoteCtrlData = 27;
        static final int TRANSACTION_setAuthenticationInfo = 31;
        static final int TRANSACTION_setHwSharingListener = 5;
        static final int TRANSACTION_setKitServiceStatusListener = 3;
        static final int TRANSACTION_setRemoteCtrlEventProcessor = 26;
        static final int TRANSACTION_setRepeatMode = 39;
        static final int TRANSACTION_setVolume = 11;
        static final int TRANSACTION_setVolumeMute = 44;
        static final int TRANSACTION_showProjectionActivity = 42;
        static final int TRANSACTION_startScan = 41;
        static final int TRANSACTION_startScanDevice = 12;
        static final int TRANSACTION_stop = 9;
        static final int TRANSACTION_stopScanDevice = 13;
        static final int TRANSACTION_subscribServers = 1;
        static final int TRANSACTION_unregisterMediaPlayerListener = 35;
        static final int TRANSACTION_unsubscribServers = 2;
        static final int TRANSACTION_updatePlayInfo = 36;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAidlHwPlayerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAidlHwPlayerManager)) {
                return new Proxy(obj);
            }
            return (IAidlHwPlayerManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ProjectionDevice _arg1;
            AuthenticationInfo _arg0;
            ConnectInfo _arg02;
            PlayInfo _arg03;
            PlayInfo _arg04;
            ERepeatMode _arg05;
            ConfigInfo _arg06;
            if (code != 1598968902) {
                boolean _arg07 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        subscribServers(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unsubscribServers(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setKitServiceStatusListener(IAidlKitServiceStatusListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        clsKitServiceStatusListener();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setHwSharingListener(data.readInt(), IAidlHwListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        clsHwSharingListener(data.readInt(), IAidlHwListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean pause = pause(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(pause ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean resume = resume(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(resume ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stop = stop(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(stop ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getVolume(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case TRANSACTION_setVolume /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean volume = setVolume(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(volume ? 1 : 0);
                        return true;
                    case TRANSACTION_startScanDevice /* 12 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        boolean startScanDevice = startScanDevice(_arg08, _arg07);
                        reply.writeNoException();
                        reply.writeInt(startScanDevice ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        boolean stopScanDevice = stopScanDevice(_arg09, _arg07);
                        reply.writeNoException();
                        reply.writeInt(stopScanDevice ? 1 : 0);
                        return true;
                    case TRANSACTION_connectDevice /* 14 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ProjectionDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean connectDevice = connectDevice(_arg010, _arg1);
                        reply.writeNoException();
                        reply.writeInt(connectDevice ? 1 : 0);
                        return true;
                    case TRANSACTION_disconnectDevice /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disconnectDevice = disconnectDevice(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disconnectDevice ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getTargetDevName(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case TRANSACTION_getTargetDevIndication /* 17 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getTargetDevIndication(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case TRANSACTION_isConnected /* 18 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isConnected = isConnected(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isConnected ? 1 : 0);
                        return true;
                    case TRANSACTION_isDisplayConnecting /* 19 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDisplayConnecting = isDisplayConnecting(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDisplayConnecting ? 1 : 0);
                        return true;
                    case TRANSACTION_pauseWifiDisplay /* 20 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean pauseWifiDisplay = pauseWifiDisplay(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(pauseWifiDisplay ? 1 : 0);
                        return true;
                    case TRANSACTION_resumeWifiDisplay /* 21 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean resumeWifiDisplay = resumeWifiDisplay(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(resumeWifiDisplay ? 1 : 0);
                        return true;
                    case TRANSACTION_getSdkVersion /* 22 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSdkVersion();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case TRANSACTION_getTargetProjectionDevice /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        ProjectionDevice _result5 = getTargetProjectionDevice(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getHiSightServerPort /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getHiSightServerPort();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case TRANSACTION_appendHiSightExInfo /* 25 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean appendHiSightExInfo = appendHiSightExInfo(data.readInt(), data.createByteArray(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(appendHiSightExInfo ? 1 : 0);
                        return true;
                    case TRANSACTION_setRemoteCtrlEventProcessor /* 26 */:
                        data.enforceInterface(DESCRIPTOR);
                        setRemoteCtrlEventProcessor(IRemoteCtrlEventProcessor.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sendRemoteCtrlData /* 27 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = sendRemoteCtrlData(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case TRANSACTION_isDisplayConnected /* 28 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDisplayConnected = isDisplayConnected(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDisplayConnected ? 1 : 0);
                        return true;
                    case TRANSACTION_getMsdpServerPort /* 29 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getMsdpServerPort();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_convertFilePathToDmsUrl /* 30 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = convertFilePathToDmsUrl(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case TRANSACTION_setAuthenticationInfo /* 31 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AuthenticationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean authenticationInfo = setAuthenticationInfo(_arg0);
                        reply.writeNoException();
                        reply.writeInt(authenticationInfo ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ConnectInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean castConnectDevice = castConnectDevice(_arg02);
                        reply.writeNoException();
                        reply.writeInt(castConnectDevice ? 1 : 0);
                        return true;
                    case TRANSACTION_castPlay /* 33 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = PlayInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean castPlay = castPlay(_arg03);
                        reply.writeNoException();
                        reply.writeInt(castPlay ? 1 : 0);
                        return true;
                    case TRANSACTION_registerMediaPlayerListener /* 34 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerMediaPlayerListener = registerMediaPlayerListener(IAidlMediaPlayerListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerMediaPlayerListener ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterMediaPlayerListener /* 35 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterMediaPlayerListener = unregisterMediaPlayerListener();
                        reply.writeNoException();
                        reply.writeInt(unregisterMediaPlayerListener ? 1 : 0);
                        return true;
                    case TRANSACTION_updatePlayInfo /* 36 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = PlayInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean updatePlayInfo = updatePlayInfo(_arg04);
                        reply.writeNoException();
                        reply.writeInt(updatePlayInfo ? 1 : 0);
                        return true;
                    case TRANSACTION_previous /* 37 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean previous = previous();
                        reply.writeNoException();
                        reply.writeInt(previous ? 1 : 0);
                        return true;
                    case TRANSACTION_next /* 38 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean next = next();
                        reply.writeNoException();
                        reply.writeInt(next ? 1 : 0);
                        return true;
                    case TRANSACTION_setRepeatMode /* 39 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ERepeatMode.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        boolean repeatMode = setRepeatMode(_arg05);
                        reply.writeNoException();
                        reply.writeInt(repeatMode ? 1 : 0);
                        return true;
                    case TRANSACTION_playMediaItem /* 40 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean playMediaItem = playMediaItem(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(playMediaItem ? 1 : 0);
                        return true;
                    case TRANSACTION_startScan /* 41 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startScan = startScan(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(startScan ? 1 : 0);
                        return true;
                    case TRANSACTION_showProjectionActivity /* 42 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = ConfigInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        boolean showProjectionActivity = showProjectionActivity(_arg06);
                        reply.writeNoException();
                        reply.writeInt(showProjectionActivity ? 1 : 0);
                        return true;
                    case TRANSACTION_seekTo /* 43 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean seekTo = seekTo(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(seekTo ? 1 : 0);
                        return true;
                    case TRANSACTION_setVolumeMute /* 44 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        boolean volumeMute = setVolumeMute(_arg07);
                        reply.writeNoException();
                        reply.writeInt(volumeMute ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IAidlHwPlayerManager {
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

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public void subscribServers(int pid, String serverType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeString(serverType);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public void unsubscribServers(int pid, String serverType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeString(serverType);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public void setKitServiceStatusListener(IAidlKitServiceStatusListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public void clsKitServiceStatusListener() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public void setHwSharingListener(int pid, IAidlHwListener mHwSharingListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeStrongBinder(mHwSharingListener != null ? mHwSharingListener.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public void clsHwSharingListener(int pid, IAidlHwListener mHwSharingListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeStrongBinder(mHwSharingListener != null ? mHwSharingListener.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean pause(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean resume(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean stop(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public int getVolume(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean setVolume(int pid, int volume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(volume);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setVolume, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean startScanDevice(int pid, boolean isActiveScan) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = true;
                    _data.writeInt(isActiveScan ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_startScanDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean stopScanDevice(int pid, boolean isActiveScan) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = true;
                    _data.writeInt(isActiveScan ? 1 : 0);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean connectDevice(int pid, ProjectionDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_connectDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean disconnectDevice(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_disconnectDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public String getTargetDevName(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public String getTargetDevIndication(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(Stub.TRANSACTION_getTargetDevIndication, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean isConnected(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isConnected, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean isDisplayConnecting(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isDisplayConnecting, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean pauseWifiDisplay(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_pauseWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean resumeWifiDisplay(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_resumeWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public int getSdkVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSdkVersion, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public ProjectionDevice getTargetProjectionDevice(int pid) throws RemoteException {
                ProjectionDevice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(Stub.TRANSACTION_getTargetProjectionDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProjectionDevice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public int getHiSightServerPort() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getHiSightServerPort, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean appendHiSightExInfo(int type, byte[] data, int len, long ts) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeByteArray(data);
                    _data.writeInt(len);
                    _data.writeLong(ts);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_appendHiSightExInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public void setRemoteCtrlEventProcessor(IRemoteCtrlEventProcessor processor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(processor != null ? processor.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_setRemoteCtrlEventProcessor, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public int sendRemoteCtrlData(int eventType, int len, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventType);
                    _data.writeInt(len);
                    _data.writeByteArray(data);
                    this.mRemote.transact(Stub.TRANSACTION_sendRemoteCtrlData, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean isDisplayConnected(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isDisplayConnected, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public int getMsdpServerPort() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMsdpServerPort, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public String convertFilePathToDmsUrl(String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    this.mRemote.transact(Stub.TRANSACTION_convertFilePathToDmsUrl, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean setAuthenticationInfo(AuthenticationInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setAuthenticationInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean castConnectDevice(ConnectInfo connInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (connInfo != null) {
                        _data.writeInt(1);
                        connInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean castPlay(PlayInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_castPlay, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean registerMediaPlayerListener(IAidlMediaPlayerListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerMediaPlayerListener, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean unregisterMediaPlayerListener() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_unregisterMediaPlayerListener, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean updatePlayInfo(PlayInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updatePlayInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean previous() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_previous, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean next() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_next, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean setRepeatMode(ERepeatMode mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (mode != null) {
                        _data.writeInt(1);
                        mode.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setRepeatMode, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean playMediaItem(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_playMediaItem, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean startScan(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_startScan, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean showProjectionActivity(ConfigInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_showProjectionActivity, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean seekTo(int targetPosition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(targetPosition);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_seekTo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.client.IAidlHwPlayerManager
            public boolean setVolumeMute(boolean isMute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(isMute ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setVolumeMute, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
