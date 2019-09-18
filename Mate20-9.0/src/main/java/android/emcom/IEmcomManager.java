package android.emcom;

import android.emcom.IHandoffSdkCallback;
import android.emcom.IHandoffServiceCallback;
import android.emcom.IListenDataCallback;
import android.emcom.IMultipathCallback;
import android.emcom.ISliceSdkCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IEmcomManager extends IInterface {

    public static abstract class Stub extends Binder implements IEmcomManager {
        private static final String DESCRIPTOR = "android.emcom.IEmcomManager";
        static final int TRANSACTION_accelerate = 6;
        static final int TRANSACTION_accelerateWithMainCardServiceStatus = 8;
        static final int TRANSACTION_activeSlice = 13;
        static final int TRANSACTION_deactiveSlice = 14;
        static final int TRANSACTION_disableMultipath = 26;
        static final int TRANSACTION_enableMultipathFlow = 25;
        static final int TRANSACTION_enableMultipathSocket = 24;
        static final int TRANSACTION_getAppInfo = 5;
        static final int TRANSACTION_getDisableMpList = 33;
        static final int TRANSACTION_getMultipathEnabled = 28;
        static final int TRANSACTION_getMultipathSupported = 27;
        static final int TRANSACTION_getMultipathTcpInfo = 29;
        static final int TRANSACTION_getRuntimeInfo = 16;
        static final int TRANSACTION_getSmartcareData = 11;
        static final int TRANSACTION_getSupportList = 32;
        static final int TRANSACTION_isSmartMpEnable = 22;
        static final int TRANSACTION_listenHiCom = 31;
        static final int TRANSACTION_notifyAppData = 4;
        static final int TRANSACTION_notifyAppDisableMobileNet = 35;
        static final int TRANSACTION_notifyEmailData = 2;
        static final int TRANSACTION_notifyHandoffDataEvent = 20;
        static final int TRANSACTION_notifyHandoffServiceStart = 18;
        static final int TRANSACTION_notifyHandoffServiceStop = 23;
        static final int TRANSACTION_notifyHandoffStateChg = 19;
        static final int TRANSACTION_notifyHwAppData = 3;
        static final int TRANSACTION_notifyRunningStatus = 10;
        static final int TRANSACTION_notifySmartMp = 21;
        static final int TRANSACTION_notifyUIEvent = 34;
        static final int TRANSACTION_notifyVideoData = 1;
        static final int TRANSACTION_registerAppCallback = 12;
        static final int TRANSACTION_registerHandoff = 17;
        static final int TRANSACTION_responseForParaUpgrade = 7;
        static final int TRANSACTION_updateAppExperienceStatus = 9;
        static final int TRANSACTION_updateAppInfo = 15;
        static final int TRANSACTION_updateMultipathAppInfo = 30;

        private static class Proxy implements IEmcomManager {
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

            public void notifyVideoData(VideoInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyEmailData(EmailInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyHwAppData(String module, String pkgName, String info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(module);
                    _data.writeString(pkgName);
                    _data.writeString(info);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyAppData(String info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(info);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public XEngineAppInfo getAppInfo(String packageName) throws RemoteException {
                XEngineAppInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = XEngineAppInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void accelerate(String packageName, int grade) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(grade);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void responseForParaUpgrade(int paratype, int pathtype, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(paratype);
                    _data.writeInt(pathtype);
                    _data.writeInt(result);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void accelerateWithMainCardServiceStatus(String packageName, int grade, int mainCardPsStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(grade);
                    _data.writeInt(mainCardPsStatus);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAppExperienceStatus(int uid, int experience, int rrt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(experience);
                    _data.writeInt(rrt);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyRunningStatus(int type, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSmartcareData(String module, String pkgName, String jsonStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(module);
                    _data.writeString(pkgName);
                    _data.writeString(jsonStr);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerAppCallback(String packageName, ISliceSdkCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void activeSlice(String packageName, String version, int sessionNumber, String serverList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(version);
                    _data.writeInt(sessionNumber);
                    _data.writeString(serverList);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deactiveSlice(String packageName, String version, int sessionNumber, String saId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(version);
                    _data.writeInt(sessionNumber);
                    _data.writeString(saId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAppInfo(String packageName, String version, int sessionNumber, String saId, String appInfoJson) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(version);
                    _data.writeInt(sessionNumber);
                    _data.writeString(saId);
                    _data.writeString(appInfoJson);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getRuntimeInfo(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerHandoff(String packageName, int dataType, IHandoffSdkCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyHandoffServiceStart(IHandoffServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyHandoffStateChg(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyHandoffDataEvent(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifySmartMp(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSmartMpEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(22, _data, _reply, 0);
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

            public int notifyHandoffServiceStop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enableMultipathSocket(String packageName, SocketInfo socketInfo, String multipathParamJson, IMultipathCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (socketInfo != null) {
                        _data.writeInt(1);
                        socketInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(multipathParamJson);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enableMultipathFlow(String packageName, String flowParamJson, String multipathParamJson, IMultipathCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(flowParamJson);
                    _data.writeString(multipathParamJson);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int disableMultipath(String packageName, String flowParamJson) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(flowParamJson);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMultipathSupported(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getMultipathEnabled(String packageName, SocketInfo socketInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (socketInfo != null) {
                        _data.writeInt(1);
                        socketInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(28, _data, _reply, 0);
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

            public String getMultipathTcpInfo(String packageName, int path, SocketInfo socketInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(path);
                    if (socketInfo != null) {
                        _data.writeInt(1);
                        socketInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateMultipathAppInfo(String packageName, String updateInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(updateInfo);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void listenHiCom(IListenDataCallback callback, String listenInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(listenInfo);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getSupportList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getDisableMpList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyUIEvent(int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyAppDisableMobileNet(int state, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeString(pkgName);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEmcomManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEmcomManager)) {
                return new Proxy(obj);
            }
            return (IEmcomManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.emcom.VideoInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: android.emcom.EmailInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v32, resolved type: android.emcom.SocketInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v39, resolved type: android.emcom.SocketInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v43, resolved type: android.emcom.SocketInfo} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v55 */
        /* JADX WARNING: type inference failed for: r0v56 */
        /* JADX WARNING: type inference failed for: r0v57 */
        /* JADX WARNING: type inference failed for: r0v58 */
        /* JADX WARNING: type inference failed for: r0v59 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg2 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = VideoInfo.CREATOR.createFromParcel(parcel);
                        }
                        notifyVideoData(_arg2);
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = EmailInfo.CREATOR.createFromParcel(parcel);
                        }
                        notifyEmailData(_arg2);
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyHwAppData(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyAppData(data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        XEngineAppInfo _result = getAppInfo(data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            parcel2.writeInt(1);
                            _result.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        accelerate(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        responseForParaUpgrade(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        accelerateWithMainCardServiceStatus(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateAppExperienceStatus(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyRunningStatus(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result2 = getSmartcareData(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result2);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result3 = registerAppCallback(data.readString(), ISliceSdkCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        activeSlice(data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        deactiveSlice(data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateAppInfo(data.readString(), data.readString(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result4 = getRuntimeInfo(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result4);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result5 = registerHandoff(data.readString(), data.readInt(), IHandoffSdkCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result6 = notifyHandoffServiceStart(IHandoffServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyHandoffStateChg(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result7 = notifyHandoffDataEvent(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifySmartMp(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result8 = isSmartMpEnable();
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result9 = notifyHandoffServiceStop();
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = SocketInfo.CREATOR.createFromParcel(parcel);
                        }
                        int _result10 = enableMultipathSocket(_arg0, _arg2, data.readString(), IMultipathCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result11 = enableMultipathFlow(data.readString(), data.readString(), data.readString(), IMultipathCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result12 = disableMultipath(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result13 = getMultipathSupported(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = SocketInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result14 = getMultipathEnabled(_arg02, _arg2);
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = SocketInfo.CREATOR.createFromParcel(parcel);
                        }
                        String _result15 = getMultipathTcpInfo(_arg03, _arg1, _arg2);
                        reply.writeNoException();
                        parcel2.writeString(_result15);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateMultipathAppInfo(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        listenHiCom(IListenDataCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result16 = getSupportList();
                        reply.writeNoException();
                        parcel2.writeStringList(_result16);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result17 = getDisableMpList();
                        reply.writeNoException();
                        parcel2.writeStringList(_result17);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyUIEvent(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyAppDisableMobileNet(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void accelerate(String str, int i) throws RemoteException;

    void accelerateWithMainCardServiceStatus(String str, int i, int i2) throws RemoteException;

    void activeSlice(String str, String str2, int i, String str3) throws RemoteException;

    void deactiveSlice(String str, String str2, int i, String str3) throws RemoteException;

    int disableMultipath(String str, String str2) throws RemoteException;

    int enableMultipathFlow(String str, String str2, String str3, IMultipathCallback iMultipathCallback) throws RemoteException;

    int enableMultipathSocket(String str, SocketInfo socketInfo, String str2, IMultipathCallback iMultipathCallback) throws RemoteException;

    XEngineAppInfo getAppInfo(String str) throws RemoteException;

    List<String> getDisableMpList() throws RemoteException;

    boolean getMultipathEnabled(String str, SocketInfo socketInfo) throws RemoteException;

    int getMultipathSupported(String str) throws RemoteException;

    String getMultipathTcpInfo(String str, int i, SocketInfo socketInfo) throws RemoteException;

    String getRuntimeInfo(String str) throws RemoteException;

    String getSmartcareData(String str, String str2, String str3) throws RemoteException;

    List<String> getSupportList() throws RemoteException;

    boolean isSmartMpEnable() throws RemoteException;

    void listenHiCom(IListenDataCallback iListenDataCallback, String str) throws RemoteException;

    void notifyAppData(String str) throws RemoteException;

    void notifyAppDisableMobileNet(int i, String str) throws RemoteException;

    void notifyEmailData(EmailInfo emailInfo) throws RemoteException;

    int notifyHandoffDataEvent(String str, String str2) throws RemoteException;

    int notifyHandoffServiceStart(IHandoffServiceCallback iHandoffServiceCallback) throws RemoteException;

    int notifyHandoffServiceStop() throws RemoteException;

    void notifyHandoffStateChg(int i) throws RemoteException;

    void notifyHwAppData(String str, String str2, String str3) throws RemoteException;

    void notifyRunningStatus(int i, String str) throws RemoteException;

    void notifySmartMp(int i) throws RemoteException;

    void notifyUIEvent(int i) throws RemoteException;

    void notifyVideoData(VideoInfo videoInfo) throws RemoteException;

    int registerAppCallback(String str, ISliceSdkCallback iSliceSdkCallback) throws RemoteException;

    int registerHandoff(String str, int i, IHandoffSdkCallback iHandoffSdkCallback) throws RemoteException;

    void responseForParaUpgrade(int i, int i2, int i3) throws RemoteException;

    void updateAppExperienceStatus(int i, int i2, int i3) throws RemoteException;

    void updateAppInfo(String str, String str2, int i, String str3, String str4) throws RemoteException;

    void updateMultipathAppInfo(String str, String str2) throws RemoteException;
}
