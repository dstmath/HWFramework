package android.emcom;

import android.emcom.IConnectCallback;
import android.emcom.IExternalMpCallback;
import android.emcom.IHandoffSdkCallback;
import android.emcom.IHandoffServiceCallback;
import android.emcom.IListenDataCallback;
import android.emcom.IMultipathCallback;
import android.emcom.IOneHopAppCallback;
import android.emcom.IOneHopAuthReqCallback;
import android.emcom.IOnehopCallback;
import android.emcom.IOnehopCallbackEx;
import android.emcom.IOnehopExCallback;
import android.emcom.IStateCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.softnet.connect.IAdvertiseOption;
import com.huawei.softnet.connect.IConnectionCallback;
import com.huawei.softnet.connect.IDiscoveryCallback;
import com.huawei.softnet.connect.IListenOption;
import java.util.List;
import java.util.Map;

public interface IEmcomManager extends IInterface {
    int activeCongestionConrolAlg(String str, String str2) throws RemoteException;

    int controlMpLink(String str, boolean z) throws RemoteException;

    int deactiveCongestionControlAlg(String str) throws RemoteException;

    int disconnectDevice(String str, List<String> list) throws RemoteException;

    Map getHandoffBindRelationMap(String str, int i) throws RemoteException;

    String getSmartcareData(String str, String str2, String str3) throws RemoteException;

    boolean hasAppInMultiPath() throws RemoteException;

    boolean isAppInMultiPath(String str) throws RemoteException;

    boolean isEnableHandoff() throws RemoteException;

    boolean isHandoffServiceSupported(String str, int i) throws RemoteException;

    boolean isTrustApp(String str) throws RemoteException;

    String linkTurboApiTransact(int i, String str, IMultipathCallback iMultipathCallback) throws RemoteException;

    int linkTurboSyncTransact(int i, int i2) throws RemoteException;

    void listenHiCom(IListenDataCallback iListenDataCallback, String str) throws RemoteException;

    void notifyConnectStateChanged(String str, String str2, int i) throws RemoteException;

    void notifyEmailData(EmailInfo emailInfo) throws RemoteException;

    void notifyExternalMpAppServiceStart(String str) throws RemoteException;

    void notifyExternalMpAppServiceStop(String str) throws RemoteException;

    void notifyExternalMpEnabled(String str) throws RemoteException;

    void notifyExternalMpPopStartGuide(String str) throws RemoteException;

    int notifyHandoffDataEvent(String str, String str2) throws RemoteException;

    int notifyHandoffServiceStart(IHandoffServiceCallback iHandoffServiceCallback) throws RemoteException;

    int notifyHandoffServiceStop() throws RemoteException;

    void notifyHandoffStateChg(int i) throws RemoteException;

    void notifyHwAppData(String str, String str2, String str3) throws RemoteException;

    void notifyMpDnsResult(int i, String str, int i2, String[] strArr, String[] strArr2) throws RemoteException;

    void notifyRunningStatus(int i, String str) throws RemoteException;

    void notifyVideoData(VideoInfo videoInfo) throws RemoteException;

    int onehopAuthReq(String str, IOneHopAuthReqCallback iOneHopAuthReqCallback) throws RemoteException;

    int onehopConnectDevice(String str, String str2, int i, String str3) throws RemoteException;

    int onehopConnectDeviceWithSessionKey(String str, String str2, int i, String str3, byte[] bArr) throws RemoteException;

    int onehopDisconnectDevice(String str, String str2, int i, String str3) throws RemoteException;

    List<OnehopDeviceInfo> onehopGetDeviceList(String str) throws RemoteException;

    String onehopGetVersion() throws RemoteException;

    int onehopRegister(String str, IOnehopCallbackEx iOnehopCallbackEx, String str2) throws RemoteException;

    int onehopRegisterDeviceListChange(String str, boolean z) throws RemoteException;

    int onehopRegisterModule(String str, IOnehopCallback iOnehopCallback) throws RemoteException;

    int onehopRegisterModuleEx(String str, IOnehopExCallback iOnehopExCallback) throws RemoteException;

    int onehopSendData(OnehopSendDataPara onehopSendDataPara) throws RemoteException;

    int onehopSendDataForZ(String str, String str2, byte[] bArr, String str3, boolean z, int i) throws RemoteException;

    int onehopSetModuleSessionKey(String str, String str2, int i, String str3, byte[] bArr) throws RemoteException;

    int onehopStartDeviceFind(String str, boolean z) throws RemoteException;

    int onehopStartDiscovery(String str, String str2) throws RemoteException;

    int onehopUnregister(String str, String str2) throws RemoteException;

    int onehopUnregisterDeviceListChange(String str) throws RemoteException;

    int onehopUnregisterModule(String str) throws RemoteException;

    int registerDeviceConnectManagerCb(String str, String str2, IConnectCallback iConnectCallback) throws RemoteException;

    int registerDeviceStateCb(String str, IStateCallback iStateCallback) throws RemoteException;

    int registerExternalMp(String str, IExternalMpCallback iExternalMpCallback) throws RemoteException;

    int registerHandoff(String str, int i, IHandoffSdkCallback iHandoffSdkCallback, IBinder iBinder) throws RemoteException;

    int registerOneHop(String str, int i, IOneHopAppCallback iOneHopAppCallback, IBinder iBinder) throws RemoteException;

    void responseForParaUpgrade(int i, int i2, int i3) throws RemoteException;

    int softnetPublish(String str, String str2, IAdvertiseOption iAdvertiseOption, IConnectionCallback iConnectionCallback) throws RemoteException;

    int softnetSubscribe(String str, String str2, IListenOption iListenOption, IDiscoveryCallback iDiscoveryCallback) throws RemoteException;

    int startHandoffService(String str, String str2) throws RemoteException;

    int stopHandoffService(String str, String str2) throws RemoteException;

    int syncHandoffData(String str, String str2) throws RemoteException;

    int unRegisterDeviceConnectManagerCb(String str, String str2) throws RemoteException;

    int unRegisterDeviceStateCb(String str) throws RemoteException;

    int unbindHandoffRelation(String str, int i, String str2) throws RemoteException;

    int unregisterExternalMp(String str) throws RemoteException;

    int unregisterOneHop(String str, int i) throws RemoteException;

    void updateAppExperienceStatus(int i, int i2, int i3) throws RemoteException;

    public static class Default implements IEmcomManager {
        @Override // android.emcom.IEmcomManager
        public void notifyVideoData(VideoInfo info) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void notifyEmailData(EmailInfo info) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void notifyHwAppData(String module, String pkgName, String info) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void responseForParaUpgrade(int paraType, int pathType, int result) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void updateAppExperienceStatus(int uid, int experience, int rrt) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void notifyRunningStatus(int type, String packageName) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public String getSmartcareData(String module, String pkgName, String jsonStr) throws RemoteException {
            return null;
        }

        @Override // android.emcom.IEmcomManager
        public int registerHandoff(String packageName, int dataType, IHandoffSdkCallback callback, IBinder binder) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int registerOneHop(String packageName, int dataType, IOneHopAppCallback callback, IBinder binder) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int unregisterOneHop(String packageName, int dataType) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int notifyHandoffServiceStart(IHandoffServiceCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public void notifyHandoffStateChg(int state) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public int notifyHandoffDataEvent(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int notifyHandoffServiceStop() throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public void listenHiCom(IListenDataCallback callback, String listenInfo) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public int activeCongestionConrolAlg(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int deactiveCongestionControlAlg(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopRegisterModule(String moduleName, IOnehopCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopUnregisterModule(String moduleName) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopStartDeviceFind(String moduleName, boolean trust) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public List<OnehopDeviceInfo> onehopGetDeviceList(String moduleName) throws RemoteException {
            return null;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopSendData(OnehopSendDataPara onehopSendDataPara) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopRegisterDeviceListChange(String moduleName, boolean trust) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopUnregisterDeviceListChange(String moduleName) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public String onehopGetVersion() throws RemoteException {
            return null;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopConnectDevice(String deviceId, String moduleName, int type, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopDisconnectDevice(String deviceId, String moduleName, int type, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int registerExternalMp(String appInfo, IExternalMpCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public void notifyExternalMpPopStartGuide(String appInfo) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void notifyExternalMpAppServiceStart(String appInfo) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void notifyExternalMpEnabled(String appInfo) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public void notifyExternalMpAppServiceStop(String appInfo) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public int unregisterExternalMp(String appInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public void notifyMpDnsResult(int uid, String host, int netType, String[] v4Addrs, String[] v6Addrs) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public String linkTurboApiTransact(int uid, String request, IMultipathCallback callback) throws RemoteException {
            return null;
        }

        @Override // android.emcom.IEmcomManager
        public int linkTurboSyncTransact(int uid, int status) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int registerDeviceStateCb(String deviceId, IStateCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int unRegisterDeviceStateCb(String deviceId) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int disconnectDevice(String deviceId, List<String> list) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int registerDeviceConnectManagerCb(String deviceId, String serviceType, IConnectCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int unRegisterDeviceConnectManagerCb(String deviceId, String serviceType) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public void notifyConnectStateChanged(String deviceId, String serviceType, int state) throws RemoteException {
        }

        @Override // android.emcom.IEmcomManager
        public int softnetSubscribe(String pkgName, String moduleId, IListenOption scanOption, IDiscoveryCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int softnetPublish(String pkgName, String moduleId, IAdvertiseOption advOption, IConnectionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopRegisterModuleEx(String moduleName, IOnehopExCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public boolean isTrustApp(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopConnectDeviceWithSessionKey(String deviceId, String moduleName, int type, String extInfo, byte[] sessionKey) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopSetModuleSessionKey(String deviceId, String moduleName, int type, String extInfo, byte[] sessionKey) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopRegister(String moduleName, IOnehopCallbackEx callback, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopUnregister(String moduleName, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopStartDiscovery(String moduleName, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopSendDataForZ(String moduleName, String deviceId, byte[] message, String extStr, boolean isEncrypt, int sendType) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int controlMpLink(String packageName, boolean enable) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public boolean isAppInMultiPath(String pkgName) throws RemoteException {
            return false;
        }

        @Override // android.emcom.IEmcomManager
        public boolean hasAppInMultiPath() throws RemoteException {
            return false;
        }

        @Override // android.emcom.IEmcomManager
        public int startHandoffService(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int stopHandoffService(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int syncHandoffData(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public boolean isEnableHandoff() throws RemoteException {
            return false;
        }

        @Override // android.emcom.IEmcomManager
        public boolean isHandoffServiceSupported(String packageName, int serviceType) throws RemoteException {
            return false;
        }

        @Override // android.emcom.IEmcomManager
        public Map getHandoffBindRelationMap(String packageName, int serviceType) throws RemoteException {
            return null;
        }

        @Override // android.emcom.IEmcomManager
        public int unbindHandoffRelation(String packageName, int serviceType, String nfcSn) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IEmcomManager
        public int onehopAuthReq(String packageName, IOneHopAuthReqCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEmcomManager {
        private static final String DESCRIPTOR = "android.emcom.IEmcomManager";
        static final int TRANSACTION_activeCongestionConrolAlg = 16;
        static final int TRANSACTION_controlMpLink = 53;
        static final int TRANSACTION_deactiveCongestionControlAlg = 17;
        static final int TRANSACTION_disconnectDevice = 39;
        static final int TRANSACTION_getHandoffBindRelationMap = 61;
        static final int TRANSACTION_getSmartcareData = 7;
        static final int TRANSACTION_hasAppInMultiPath = 55;
        static final int TRANSACTION_isAppInMultiPath = 54;
        static final int TRANSACTION_isEnableHandoff = 59;
        static final int TRANSACTION_isHandoffServiceSupported = 60;
        static final int TRANSACTION_isTrustApp = 46;
        static final int TRANSACTION_linkTurboApiTransact = 35;
        static final int TRANSACTION_linkTurboSyncTransact = 36;
        static final int TRANSACTION_listenHiCom = 15;
        static final int TRANSACTION_notifyConnectStateChanged = 42;
        static final int TRANSACTION_notifyEmailData = 2;
        static final int TRANSACTION_notifyExternalMpAppServiceStart = 30;
        static final int TRANSACTION_notifyExternalMpAppServiceStop = 32;
        static final int TRANSACTION_notifyExternalMpEnabled = 31;
        static final int TRANSACTION_notifyExternalMpPopStartGuide = 29;
        static final int TRANSACTION_notifyHandoffDataEvent = 13;
        static final int TRANSACTION_notifyHandoffServiceStart = 11;
        static final int TRANSACTION_notifyHandoffServiceStop = 14;
        static final int TRANSACTION_notifyHandoffStateChg = 12;
        static final int TRANSACTION_notifyHwAppData = 3;
        static final int TRANSACTION_notifyMpDnsResult = 34;
        static final int TRANSACTION_notifyRunningStatus = 6;
        static final int TRANSACTION_notifyVideoData = 1;
        static final int TRANSACTION_onehopAuthReq = 63;
        static final int TRANSACTION_onehopConnectDevice = 26;
        static final int TRANSACTION_onehopConnectDeviceWithSessionKey = 47;
        static final int TRANSACTION_onehopDisconnectDevice = 27;
        static final int TRANSACTION_onehopGetDeviceList = 21;
        static final int TRANSACTION_onehopGetVersion = 25;
        static final int TRANSACTION_onehopRegister = 49;
        static final int TRANSACTION_onehopRegisterDeviceListChange = 23;
        static final int TRANSACTION_onehopRegisterModule = 18;
        static final int TRANSACTION_onehopRegisterModuleEx = 45;
        static final int TRANSACTION_onehopSendData = 22;
        static final int TRANSACTION_onehopSendDataForZ = 52;
        static final int TRANSACTION_onehopSetModuleSessionKey = 48;
        static final int TRANSACTION_onehopStartDeviceFind = 20;
        static final int TRANSACTION_onehopStartDiscovery = 51;
        static final int TRANSACTION_onehopUnregister = 50;
        static final int TRANSACTION_onehopUnregisterDeviceListChange = 24;
        static final int TRANSACTION_onehopUnregisterModule = 19;
        static final int TRANSACTION_registerDeviceConnectManagerCb = 40;
        static final int TRANSACTION_registerDeviceStateCb = 37;
        static final int TRANSACTION_registerExternalMp = 28;
        static final int TRANSACTION_registerHandoff = 8;
        static final int TRANSACTION_registerOneHop = 9;
        static final int TRANSACTION_responseForParaUpgrade = 4;
        static final int TRANSACTION_softnetPublish = 44;
        static final int TRANSACTION_softnetSubscribe = 43;
        static final int TRANSACTION_startHandoffService = 56;
        static final int TRANSACTION_stopHandoffService = 57;
        static final int TRANSACTION_syncHandoffData = 58;
        static final int TRANSACTION_unRegisterDeviceConnectManagerCb = 41;
        static final int TRANSACTION_unRegisterDeviceStateCb = 38;
        static final int TRANSACTION_unbindHandoffRelation = 62;
        static final int TRANSACTION_unregisterExternalMp = 33;
        static final int TRANSACTION_unregisterOneHop = 10;
        static final int TRANSACTION_updateAppExperienceStatus = 5;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            VideoInfo _arg0;
            EmailInfo _arg02;
            OnehopSendDataPara _arg03;
            IListenOption _arg2;
            IAdvertiseOption _arg22;
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = VideoInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        notifyVideoData(_arg0);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = EmailInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        notifyEmailData(_arg02);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        notifyHwAppData(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        responseForParaUpgrade(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        updateAppExperienceStatus(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        notifyRunningStatus(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getSmartcareData(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = registerHandoff(data.readString(), data.readInt(), IHandoffSdkCallback.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = registerOneHop(data.readString(), data.readInt(), IOneHopAppCallback.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = unregisterOneHop(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = notifyHandoffServiceStart(IHandoffServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        notifyHandoffStateChg(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = notifyHandoffDataEvent(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = notifyHandoffServiceStop();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        listenHiCom(IListenDataCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = activeCongestionConrolAlg(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = deactiveCongestionControlAlg(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = onehopRegisterModule(data.readString(), IOnehopCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = onehopUnregisterModule(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        int _result12 = onehopStartDeviceFind(_arg04, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        List<OnehopDeviceInfo> _result13 = onehopGetDeviceList(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result13);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = OnehopSendDataPara.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _result14 = onehopSendData(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        int _result15 = onehopRegisterDeviceListChange(_arg05, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = onehopUnregisterDeviceListChange(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = onehopGetVersion();
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = onehopConnectDevice(data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = onehopDisconnectDevice(data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = registerExternalMp(data.readString(), IExternalMpCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        notifyExternalMpPopStartGuide(data.readString());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        notifyExternalMpAppServiceStart(data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        notifyExternalMpEnabled(data.readString());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        notifyExternalMpAppServiceStop(data.readString());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = unregisterExternalMp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        notifyMpDnsResult(data.readInt(), data.readString(), data.readInt(), data.createStringArray(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        String _result22 = linkTurboApiTransact(data.readInt(), data.readString(), IMultipathCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeString(_result22);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = linkTurboSyncTransact(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = registerDeviceStateCb(data.readString(), IStateCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = unRegisterDeviceStateCb(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = disconnectDevice(data.readString(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = registerDeviceConnectManagerCb(data.readString(), data.readString(), IConnectCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = unRegisterDeviceConnectManagerCb(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        notifyConnectStateChanged(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = IListenOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result29 = softnetSubscribe(_arg06, _arg12, _arg2, IDiscoveryCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = IAdvertiseOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _result30 = softnetPublish(_arg07, _arg13, _arg22, IConnectionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = onehopRegisterModuleEx(data.readString(), IOnehopExCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTrustApp = isTrustApp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isTrustApp ? 1 : 0);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = onehopConnectDeviceWithSessionKey(data.readString(), data.readString(), data.readInt(), data.readString(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        int _result33 = onehopSetModuleSessionKey(data.readString(), data.readString(), data.readInt(), data.readString(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result33);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        int _result34 = onehopRegister(data.readString(), IOnehopCallbackEx.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result34);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = onehopUnregister(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        int _result36 = onehopStartDiscovery(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result36);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        int _result37 = onehopSendDataForZ(data.readString(), data.readString(), data.createByteArray(), data.readString(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        int _result38 = controlMpLink(_arg08, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result38);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAppInMultiPath = isAppInMultiPath(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAppInMultiPath ? 1 : 0);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasAppInMultiPath = hasAppInMultiPath();
                        reply.writeNoException();
                        reply.writeInt(hasAppInMultiPath ? 1 : 0);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        int _result39 = startHandoffService(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result39);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        int _result40 = stopHandoffService(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result40);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        int _result41 = syncHandoffData(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result41);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEnableHandoff = isEnableHandoff();
                        reply.writeNoException();
                        reply.writeInt(isEnableHandoff ? 1 : 0);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHandoffServiceSupported = isHandoffServiceSupported(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isHandoffServiceSupported ? 1 : 0);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result42 = getHandoffBindRelationMap(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result42);
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        int _result43 = unbindHandoffRelation(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result43);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        int _result44 = onehopAuthReq(data.readString(), IOneHopAuthReqCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result44);
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
        public static class Proxy implements IEmcomManager {
            public static IEmcomManager sDefaultImpl;
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

            @Override // android.emcom.IEmcomManager
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
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyVideoData(info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
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
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyEmailData(info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public void notifyHwAppData(String module, String pkgName, String info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(module);
                    _data.writeString(pkgName);
                    _data.writeString(info);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyHwAppData(module, pkgName, info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public void responseForParaUpgrade(int paraType, int pathType, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(paraType);
                    _data.writeInt(pathType);
                    _data.writeInt(result);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().responseForParaUpgrade(paraType, pathType, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public void updateAppExperienceStatus(int uid, int experience, int rrt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(experience);
                    _data.writeInt(rrt);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateAppExperienceStatus(uid, experience, rrt);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public void notifyRunningStatus(int type, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyRunningStatus(type, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public String getSmartcareData(String module, String pkgName, String jsonStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(module);
                    _data.writeString(pkgName);
                    _data.writeString(jsonStr);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSmartcareData(module, pkgName, jsonStr);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int registerHandoff(String packageName, int dataType, IHandoffSdkCallback callback, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeStrongBinder(binder);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerHandoff(packageName, dataType, callback, binder);
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

            @Override // android.emcom.IEmcomManager
            public int registerOneHop(String packageName, int dataType, IOneHopAppCallback callback, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeStrongBinder(binder);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerOneHop(packageName, dataType, callback, binder);
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

            @Override // android.emcom.IEmcomManager
            public int unregisterOneHop(String packageName, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterOneHop(packageName, dataType);
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

            @Override // android.emcom.IEmcomManager
            public int notifyHandoffServiceStart(IHandoffServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyHandoffServiceStart(callback);
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

            @Override // android.emcom.IEmcomManager
            public void notifyHandoffStateChg(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyHandoffStateChg(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int notifyHandoffDataEvent(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyHandoffDataEvent(packageName, para);
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

            @Override // android.emcom.IEmcomManager
            public int notifyHandoffServiceStop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyHandoffServiceStop();
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

            @Override // android.emcom.IEmcomManager
            public void listenHiCom(IListenDataCallback callback, String listenInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(listenInfo);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().listenHiCom(callback, listenInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int activeCongestionConrolAlg(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activeCongestionConrolAlg(packageName, para);
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

            @Override // android.emcom.IEmcomManager
            public int deactiveCongestionControlAlg(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deactiveCongestionControlAlg(packageName);
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

            @Override // android.emcom.IEmcomManager
            public int onehopRegisterModule(String moduleName, IOnehopCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopRegisterModule(moduleName, callback);
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

            @Override // android.emcom.IEmcomManager
            public int onehopUnregisterModule(String moduleName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopUnregisterModule(moduleName);
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

            @Override // android.emcom.IEmcomManager
            public int onehopStartDeviceFind(String moduleName, boolean trust) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeInt(trust ? 1 : 0);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopStartDeviceFind(moduleName, trust);
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

            @Override // android.emcom.IEmcomManager
            public List<OnehopDeviceInfo> onehopGetDeviceList(String moduleName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopGetDeviceList(moduleName);
                    }
                    _reply.readException();
                    List<OnehopDeviceInfo> _result = _reply.createTypedArrayList(OnehopDeviceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int onehopSendData(OnehopSendDataPara onehopSendDataPara) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (onehopSendDataPara != null) {
                        _data.writeInt(1);
                        onehopSendDataPara.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopSendData(onehopSendDataPara);
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

            @Override // android.emcom.IEmcomManager
            public int onehopRegisterDeviceListChange(String moduleName, boolean trust) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeInt(trust ? 1 : 0);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopRegisterDeviceListChange(moduleName, trust);
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

            @Override // android.emcom.IEmcomManager
            public int onehopUnregisterDeviceListChange(String moduleName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopUnregisterDeviceListChange(moduleName);
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

            @Override // android.emcom.IEmcomManager
            public String onehopGetVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopGetVersion();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int onehopConnectDevice(String deviceId, String moduleName, int type, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(moduleName);
                    _data.writeInt(type);
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopConnectDevice(deviceId, moduleName, type, extInfo);
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

            @Override // android.emcom.IEmcomManager
            public int onehopDisconnectDevice(String deviceId, String moduleName, int type, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(moduleName);
                    _data.writeInt(type);
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopDisconnectDevice(deviceId, moduleName, type, extInfo);
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

            @Override // android.emcom.IEmcomManager
            public int registerExternalMp(String appInfo, IExternalMpCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appInfo);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerExternalMp(appInfo, callback);
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

            @Override // android.emcom.IEmcomManager
            public void notifyExternalMpPopStartGuide(String appInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appInfo);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyExternalMpPopStartGuide(appInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public void notifyExternalMpAppServiceStart(String appInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appInfo);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyExternalMpAppServiceStart(appInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public void notifyExternalMpEnabled(String appInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appInfo);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyExternalMpEnabled(appInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public void notifyExternalMpAppServiceStop(String appInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appInfo);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyExternalMpAppServiceStop(appInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int unregisterExternalMp(String appInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appInfo);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterExternalMp(appInfo);
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

            @Override // android.emcom.IEmcomManager
            public void notifyMpDnsResult(int uid, String host, int netType, String[] v4Addrs, String[] v6Addrs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(host);
                    _data.writeInt(netType);
                    _data.writeStringArray(v4Addrs);
                    _data.writeStringArray(v6Addrs);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyMpDnsResult(uid, host, netType, v4Addrs, v6Addrs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public String linkTurboApiTransact(int uid, String request, IMultipathCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(request);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().linkTurboApiTransact(uid, request, callback);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int linkTurboSyncTransact(int uid, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(status);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().linkTurboSyncTransact(uid, status);
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

            @Override // android.emcom.IEmcomManager
            public int registerDeviceStateCb(String deviceId, IStateCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerDeviceStateCb(deviceId, callback);
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

            @Override // android.emcom.IEmcomManager
            public int unRegisterDeviceStateCb(String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterDeviceStateCb(deviceId);
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

            @Override // android.emcom.IEmcomManager
            public int disconnectDevice(String deviceId, List<String> serviceTypeList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeStringList(serviceTypeList);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnectDevice(deviceId, serviceTypeList);
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

            @Override // android.emcom.IEmcomManager
            public int registerDeviceConnectManagerCb(String deviceId, String serviceType, IConnectCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(serviceType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerDeviceConnectManagerCb(deviceId, serviceType, callback);
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

            @Override // android.emcom.IEmcomManager
            public int unRegisterDeviceConnectManagerCb(String deviceId, String serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(serviceType);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterDeviceConnectManagerCb(deviceId, serviceType);
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

            @Override // android.emcom.IEmcomManager
            public void notifyConnectStateChanged(String deviceId, String serviceType, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(serviceType);
                    _data.writeInt(state);
                    if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyConnectStateChanged(deviceId, serviceType, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int softnetSubscribe(String pkgName, String moduleId, IListenOption scanOption, IDiscoveryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(moduleId);
                    if (scanOption != null) {
                        _data.writeInt(1);
                        scanOption.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().softnetSubscribe(pkgName, moduleId, scanOption, callback);
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

            @Override // android.emcom.IEmcomManager
            public int softnetPublish(String pkgName, String moduleId, IAdvertiseOption advOption, IConnectionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(moduleId);
                    if (advOption != null) {
                        _data.writeInt(1);
                        advOption.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().softnetPublish(pkgName, moduleId, advOption, callback);
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

            @Override // android.emcom.IEmcomManager
            public int onehopRegisterModuleEx(String moduleName, IOnehopExCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopRegisterModuleEx(moduleName, callback);
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

            @Override // android.emcom.IEmcomManager
            public boolean isTrustApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTrustApp(packageName);
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

            @Override // android.emcom.IEmcomManager
            public int onehopConnectDeviceWithSessionKey(String deviceId, String moduleName, int type, String extInfo, byte[] sessionKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(moduleName);
                    _data.writeInt(type);
                    _data.writeString(extInfo);
                    _data.writeByteArray(sessionKey);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopConnectDeviceWithSessionKey(deviceId, moduleName, type, extInfo, sessionKey);
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

            @Override // android.emcom.IEmcomManager
            public int onehopSetModuleSessionKey(String deviceId, String moduleName, int type, String extInfo, byte[] sessionKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(moduleName);
                    _data.writeInt(type);
                    _data.writeString(extInfo);
                    _data.writeByteArray(sessionKey);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopSetModuleSessionKey(deviceId, moduleName, type, extInfo, sessionKey);
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

            @Override // android.emcom.IEmcomManager
            public int onehopRegister(String moduleName, IOnehopCallbackEx callback, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopRegister(moduleName, callback, extInfo);
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

            @Override // android.emcom.IEmcomManager
            public int onehopUnregister(String moduleName, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopUnregister(moduleName, extInfo);
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

            @Override // android.emcom.IEmcomManager
            public int onehopStartDiscovery(String moduleName, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopStartDiscovery(moduleName, extInfo);
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

            @Override // android.emcom.IEmcomManager
            public int onehopSendDataForZ(String moduleName, String deviceId, byte[] message, String extStr, boolean isEncrypt, int sendType) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(moduleName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(deviceId);
                        try {
                            _data.writeByteArray(message);
                            try {
                                _data.writeString(extStr);
                                _data.writeInt(isEncrypt ? 1 : 0);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(sendType);
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
                    try {
                        if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int onehopSendDataForZ = Stub.getDefaultImpl().onehopSendDataForZ(moduleName, deviceId, message, extStr, isEncrypt, sendType);
                        _reply.recycle();
                        _data.recycle();
                        return onehopSendDataForZ;
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

            @Override // android.emcom.IEmcomManager
            public int controlMpLink(String packageName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().controlMpLink(packageName, enable);
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

            @Override // android.emcom.IEmcomManager
            public boolean isAppInMultiPath(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAppInMultiPath(pkgName);
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

            @Override // android.emcom.IEmcomManager
            public boolean hasAppInMultiPath() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasAppInMultiPath();
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

            @Override // android.emcom.IEmcomManager
            public int startHandoffService(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startHandoffService(packageName, para);
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

            @Override // android.emcom.IEmcomManager
            public int stopHandoffService(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopHandoffService(packageName, para);
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

            @Override // android.emcom.IEmcomManager
            public int syncHandoffData(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().syncHandoffData(packageName, para);
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

            @Override // android.emcom.IEmcomManager
            public boolean isEnableHandoff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEnableHandoff();
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

            @Override // android.emcom.IEmcomManager
            public boolean isHandoffServiceSupported(String packageName, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    boolean _result = false;
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHandoffServiceSupported(packageName, serviceType);
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

            @Override // android.emcom.IEmcomManager
            public Map getHandoffBindRelationMap(String packageName, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHandoffBindRelationMap(packageName, serviceType);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IEmcomManager
            public int unbindHandoffRelation(String packageName, int serviceType, String nfcSn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    _data.writeString(nfcSn);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unbindHandoffRelation(packageName, serviceType, nfcSn);
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

            @Override // android.emcom.IEmcomManager
            public int onehopAuthReq(String packageName, IOneHopAuthReqCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(63, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onehopAuthReq(packageName, callback);
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
        }

        public static boolean setDefaultImpl(IEmcomManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEmcomManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
