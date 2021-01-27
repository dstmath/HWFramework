package vendor.huawei.hardware.hwfactoryinterface.V1_1;

import android.internal.hidl.base.V1_0.DebugInfo;
import android.internal.hidl.base.V1_0.IBase;
import android.net.wifi.WifiScanner;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.PhoneConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.huawei.hardware.hwfactoryinterface.V1_0.FmdCmdData;
import vendor.huawei.hardware.hwfactoryinterface.V1_0.FmdMsg;
import vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface;

public interface IHwFactoryInterface extends vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface {
    public static final String kInterfaceName = "vendor.huawei.hardware.hwfactoryinterface@1.1::IHwFactoryInterface";

    @FunctionalInterface
    public interface getControlFlagCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface nv_common_readCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface oeminfo_ReadCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface oeminfo_Read_reusedCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface oeminfo_getinfoCallback {
        void onValues(int i, int i2, int i3);
    }

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase, android.os.IHwInterface
    IHwBinder asBinder();

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    void getControlFlag(int i, int i2, getControlFlagCallback getcontrolflagcallback) throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    int getManufactureMode() throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    void nv_common_read(int i, String str, int i2, nv_common_readCallback nv_common_readcallback) throws RemoteException;

    int nv_common_write(int i, String str, int i2, String str2) throws RemoteException;

    void oeminfo_Read(int i, int i2, oeminfo_ReadCallback oeminfo_readcallback) throws RemoteException;

    void oeminfo_Read_reused(int i, int i2, int i3, oeminfo_Read_reusedCallback oeminfo_read_reusedcallback) throws RemoteException;

    int oeminfo_erase(int i) throws RemoteException;

    int oeminfo_erase_reused(int i, int i2) throws RemoteException;

    void oeminfo_getinfo(int i, oeminfo_getinfoCallback oeminfo_getinfocallback) throws RemoteException;

    int oeminfo_write(int i, int i2, String str) throws RemoteException;

    int oeminfo_write_reused(int i, int i2, int i3, String str) throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    int setControlFlag(int i, String str, int i2) throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IHwFactoryInterface asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IHwFactoryInterface)) {
            return (IHwFactoryInterface) iface;
        }
        IHwFactoryInterface proxy = new Proxy(binder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IHwFactoryInterface castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IHwFactoryInterface getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IHwFactoryInterface getService(boolean retry) throws RemoteException {
        return getService(PhoneConstants.APN_TYPE_DEFAULT, retry);
    }

    static IHwFactoryInterface getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IHwFactoryInterface getService() throws RemoteException {
        return getService(PhoneConstants.APN_TYPE_DEFAULT);
    }

    public static final class Proxy implements IHwFactoryInterface {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase, android.os.IHwInterface
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.hwfactoryinterface@1.1::IHwFactoryInterface]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public boolean isFactoryVersion() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getManufactureFileName(IHwFactoryInterface.getManufactureFileNameCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setMmiResult(int type, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getDeviceInfo(int id, IHwFactoryInterface.getDeviceInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setDeviceInfo(int id, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(id);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getTestResult(int testid, IHwFactoryInterface.getTestResultCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(testid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setTestResult(int testid, String result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(testid);
            _hidl_request.writeString(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getVersionAndTime(int id, IHwFactoryInterface.getVersionAndTimeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int getNvBackupResult() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int getBackgroundDebugMode() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setBackgroundDebugMode(int mode, String password) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(mode);
            _hidl_request.writeString(password);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public boolean getLogState(int logId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(logId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setLogState(int logId, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(logId);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int getFuseState() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int verifySecbootKey(String key) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(key);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getSimlockDetail(IHwFactoryInterface.getSimlockDetailCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getBoundPlmnInfo(int id, long resultLen, IHwFactoryInterface.getBoundPlmnInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(id);
            _hidl_request.writeInt64(resultLen);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setCalibrationResult(int type, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void setCommandResult(String target, String cmd, int maxSize, IHwFactoryInterface.setCommandResultCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(target);
            _hidl_request.writeString(cmd);
            _hidl_request.writeInt32(maxSize);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int getMmiDeviceFeaturesCheckResult(int deviceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(deviceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getMmiDeviceSelfCheckResult(IHwFactoryInterface.getMmiDeviceSelfCheckResultCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getI2cCheckInfo(int len, IHwFactoryInterface.getI2cCheckInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(len);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void usbPlugControl(String testScene, IHwFactoryInterface.usbPlugControlCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(testScene);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtFpgaTest(String testScene, int len, IHwFactoryInterface.rtFpgaTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(testScene);
            _hidl_request.writeInt32(len);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtWatchdogCtrl(String status, IHwFactoryInterface.rtWatchdogCtrlCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtJtagCtrl(String status, IHwFactoryInterface.rtJtagCtrlCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtSocTest(String testScene, String parameters, IHwFactoryInterface.rtSocTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(testScene);
            _hidl_request.writeString(parameters);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtSateAgingTest(String parameters, String status, IHwFactoryInterface.rtSateAgingTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(parameters);
            _hidl_request.writeString(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtGetSocInfo(String testScene, IHwFactoryInterface.rtGetSocInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(testScene);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int rtSetSocInfo(String testScene, String parameters) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(testScene);
            _hidl_request.writeString(parameters);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int lockBigCpu(int lockOrRelease) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(lockOrRelease);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setLightSleep(int onOrOff) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(onOrOff);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void getApparatusModel(int apparatusId, int len, IHwFactoryInterface.getApparatusModelCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(apparatusId);
            _hidl_request.writeInt32(len);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void voltageDetect(String testScene, String para, String status, IHwFactoryInterface.voltageDetectCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(testScene);
            _hidl_request.writeString(para);
            _hidl_request.writeString(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void socVoltageOffset(String volTarget, String freq, String offsetValue, IHwFactoryInterface.socVoltageOffsetCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(volTarget);
            _hidl_request.writeString(freq);
            _hidl_request.writeString(offsetValue);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int setHiseePowerctrl(int onoff) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(onoff);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtDDRTest(String test_scene, String parameters, String status, IHwFactoryInterface.rtDDRTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(test_scene);
            _hidl_request.writeString(parameters);
            _hidl_request.writeString(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtMapperTest(String status, IHwFactoryInterface.rtMapperTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtEmmcStressAgingTest(String test_scene, String parameters, String status, IHwFactoryInterface.rtEmmcStressAgingTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(test_scene);
            _hidl_request.writeString(parameters);
            _hidl_request.writeString(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtUfsAgingTest(String test_scene, IHwFactoryInterface.rtUfsAgingTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(test_scene);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtStorageJoblessTest(String test_scene, String parameters, IHwFactoryInterface.rtStorageJoblessTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(test_scene);
            _hidl_request.writeString(parameters);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void rtStorageTestJudge(String test_scene, IHwFactoryInterface.rtStorageTestJudgeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(test_scene);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void setWifiCommand(String testScene, String cmd, int maxSize, IHwFactoryInterface.setWifiCommandCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(testScene);
            _hidl_request.writeString(cmd);
            _hidl_request.writeInt32(maxSize);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void connGetChecktimeOfChipcheck(int type, String reserved, IHwFactoryInterface.connGetChecktimeOfChipcheckCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(reserved);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readInt32Vector(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void connGetChipcheckResult(int type, String reserved, IHwFactoryInterface.connGetChipcheckResultCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(reserved);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void connChipcheck(int type, String reserved, IHwFactoryInterface.connChipcheckCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(reserved);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int sendFmdOperation(FmdCmdData cmd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            cmd.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int sendFmdData(FmdMsg msg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            msg.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void socVoltageOffsetByID(int testID, IHwFactoryInterface.socVoltageOffsetByIDCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(testID);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public int ReleaseFacHidlResource() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void vr_command_test(String in_buff, int in_len, int pad_bytes, int out_len, IHwFactoryInterface.vr_command_testCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeString(in_buff);
            _hidl_request.writeInt32(in_len);
            _hidl_request.writeInt32(pad_bytes);
            _hidl_request.writeInt32(out_len);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface
        public void hwGetDeviceId(int type, int idLen, IHwFactoryInterface.hwGetDeviceIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(idLen);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public void oeminfo_Read_reused(int main_idx, int sub_idx, int size, oeminfo_Read_reusedCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(main_idx);
            _hidl_request.writeInt32(sub_idx);
            _hidl_request.writeInt32(size);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public int oeminfo_write_reused(int main_idx, int sub_idx, int size, String data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(main_idx);
            _hidl_request.writeInt32(sub_idx);
            _hidl_request.writeInt32(size);
            _hidl_request.writeString(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public int oeminfo_erase_reused(int main_idx, int sub_idx) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(main_idx);
            _hidl_request.writeInt32(sub_idx);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public void oeminfo_Read(int idx, int size, oeminfo_ReadCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(idx);
            _hidl_request.writeInt32(size);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public int oeminfo_write(int idx, int size, String data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(idx);
            _hidl_request.writeInt32(size);
            _hidl_request.writeString(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public int oeminfo_erase(int idx) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(idx);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public void oeminfo_getinfo(int idx, oeminfo_getinfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(idx);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt32(), _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public int nv_common_write(int idx, String name, int len, String data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(idx);
            _hidl_request.writeString(name);
            _hidl_request.writeInt32(len);
            _hidl_request.writeString(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(60, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public void nv_common_read(int idx, String name, int len, nv_common_readCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(idx);
            _hidl_request.writeString(name);
            _hidl_request.writeInt32(len);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(61, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public void getControlFlag(int iFlagID, int iFlagBufLen, getControlFlagCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(iFlagID);
            _hidl_request.writeInt32(iFlagBufLen);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(62, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public int setControlFlag(int iFlagID, String pFlag, int iFlagLen) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            _hidl_request.writeInt32(iFlagID);
            _hidl_request.writeString(pFlag);
            _hidl_request.writeInt32(iFlagLen);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(63, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface
        public int getManufactureMode() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHwFactoryInterface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(64, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readStringVector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            _hidl_request.writeNativeHandle(fd);
            _hidl_request.writeStringVector(options);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256131655, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readString();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_0 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IHwFactoryInterface {
        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase, android.os.IHwInterface
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IHwFactoryInterface.kInterfaceName, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IHwFactoryInterface.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-75, 50, -118, 19, 78, 94, 115, -20, -50, 77, 42, 71, -79, 76, -28, -47, 95, 76, 107, 103, 37, 89, -55, MidiConstants.STATUS_CONTROL_CHANGE, 112, 122, MidiConstants.STATUS_SONG_POSITION, 0, -39, 46, 84, 66}, new byte[]{0, -64, -60, -54, -7, WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK, -101, 116, -97, 11, -68, 114, -43, 7, 48, -104, 68, 88, -69, 22, -36, -110, -69, 49, 115, 90, -67, -114, -86, 118, 31, 28}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, MidiConstants.STATUS_CHANNEL_PRESSURE, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, MidiConstants.STATUS_SONG_SELECT, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.os.IHwBinder, android.hardware.cas.V1_0.ICas, android.internal.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface, vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface, android.internal.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.os.IHwBinder, android.hardware.cas.V1_0.ICas, android.internal.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        @Override // android.os.IHwBinder
        public IHwInterface queryLocalInterface(String descriptor) {
            if (IHwFactoryInterface.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        @Override // android.os.HwBinder
        public void onTransact(int _hidl_code, HwParcel _hidl_request, final HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    boolean _hidl_out_isFactoryVersionRet = isFactoryVersion();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_isFactoryVersionRet);
                    _hidl_reply.send();
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getManufactureFileName(new IHwFactoryInterface.getManufactureFileNameCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass1 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getManufactureFileNameCallback
                        public void onValues(int getManufactureFileNameRet, String name) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getManufactureFileNameRet);
                            _hidl_reply.writeString(name);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 3:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setMmiResultRet = setMmiResult(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setMmiResultRet);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getDeviceInfo(_hidl_request.readInt32(), new IHwFactoryInterface.getDeviceInfoCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass2 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getDeviceInfoCallback
                        public void onValues(int getDeviceInfoRet, String value) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getDeviceInfoRet);
                            _hidl_reply.writeString(value);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setDeviceInfoRet = setDeviceInfo(_hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setDeviceInfoRet);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getTestResult(_hidl_request.readInt32(), new IHwFactoryInterface.getTestResultCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass3 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getTestResultCallback
                        public void onValues(int getTestResultRet, String result) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getTestResultRet);
                            _hidl_reply.writeString(result);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 7:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setTestResultRet = setTestResult(_hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setTestResultRet);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getVersionAndTime(_hidl_request.readInt32(), new IHwFactoryInterface.getVersionAndTimeCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass4 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getVersionAndTimeCallback
                        public void onValues(int getVersionAndTimeRet, String result) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getVersionAndTimeRet);
                            _hidl_reply.writeString(result);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 9:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_getNvBackupResultRet = getNvBackupResult();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_getNvBackupResultRet);
                    _hidl_reply.send();
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_getBackgroundDebugModeRet = getBackgroundDebugMode();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_getBackgroundDebugModeRet);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setBackgroundDebugModeRet = setBackgroundDebugMode(_hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setBackgroundDebugModeRet);
                    _hidl_reply.send();
                    return;
                case 12:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    boolean _hidl_out_getLogStateRet = getLogState(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_getLogStateRet);
                    _hidl_reply.send();
                    return;
                case 13:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setLogStateRet = setLogState(_hidl_request.readInt32(), _hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setLogStateRet);
                    _hidl_reply.send();
                    return;
                case 14:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_getFuseStateRet = getFuseState();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_getFuseStateRet);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_verifySecbootKeyRet = verifySecbootKey(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_verifySecbootKeyRet);
                    _hidl_reply.send();
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getSimlockDetail(new IHwFactoryInterface.getSimlockDetailCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass5 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getSimlockDetailCallback
                        public void onValues(int getSimlockDetailRet, String state) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getSimlockDetailRet);
                            _hidl_reply.writeString(state);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 17:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getBoundPlmnInfo(_hidl_request.readInt32(), _hidl_request.readInt64(), new IHwFactoryInterface.getBoundPlmnInfoCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass6 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getBoundPlmnInfoCallback
                        public void onValues(int getBoundPlmnInfoRet, String result) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getBoundPlmnInfoRet);
                            _hidl_reply.writeString(result);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 18:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setCalibrationResultRet = setCalibrationResult(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setCalibrationResultRet);
                    _hidl_reply.send();
                    return;
                case 19:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    setCommandResult(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32(), new IHwFactoryInterface.setCommandResultCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass7 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.setCommandResultCallback
                        public void onValues(int setCommandResultRet, String buf) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(setCommandResultRet);
                            _hidl_reply.writeString(buf);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 20:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_getMmiDeviceFeaturesCheckResultRet = getMmiDeviceFeaturesCheckResult(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_getMmiDeviceFeaturesCheckResultRet);
                    _hidl_reply.send();
                    return;
                case 21:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getMmiDeviceSelfCheckResult(new IHwFactoryInterface.getMmiDeviceSelfCheckResultCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass8 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getMmiDeviceSelfCheckResultCallback
                        public void onValues(int getMmiDeviceSelfCheckResultRet, String result) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getMmiDeviceSelfCheckResultRet);
                            _hidl_reply.writeString(result);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 22:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getI2cCheckInfo(_hidl_request.readInt32(), new IHwFactoryInterface.getI2cCheckInfoCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass9 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getI2cCheckInfoCallback
                        public void onValues(int getI2cCheckInfoRet, String testresult, String apparalist) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getI2cCheckInfoRet);
                            _hidl_reply.writeString(testresult);
                            _hidl_reply.writeString(apparalist);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 23:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    usbPlugControl(_hidl_request.readString(), new IHwFactoryInterface.usbPlugControlCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass10 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.usbPlugControlCallback
                        public void onValues(int usbPlugControlRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(usbPlugControlRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtFpgaTest(_hidl_request.readString(), _hidl_request.readInt32(), new IHwFactoryInterface.rtFpgaTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass11 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtFpgaTestCallback
                        public void onValues(int rtFpgaTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtFpgaTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 25:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtWatchdogCtrl(_hidl_request.readString(), new IHwFactoryInterface.rtWatchdogCtrlCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass12 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtWatchdogCtrlCallback
                        public void onValues(int rtWatchdogCtrlRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtWatchdogCtrlRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 26:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtJtagCtrl(_hidl_request.readString(), new IHwFactoryInterface.rtJtagCtrlCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass13 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtJtagCtrlCallback
                        public void onValues(int rtJtagCtrlRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtJtagCtrlRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 27:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtSocTest(_hidl_request.readString(), _hidl_request.readString(), new IHwFactoryInterface.rtSocTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass14 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtSocTestCallback
                        public void onValues(int rtSocTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtSocTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 28:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtSateAgingTest(_hidl_request.readString(), _hidl_request.readString(), new IHwFactoryInterface.rtSateAgingTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass15 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtSateAgingTestCallback
                        public void onValues(int rtSateAgingTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtSateAgingTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 29:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtGetSocInfo(_hidl_request.readString(), new IHwFactoryInterface.rtGetSocInfoCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass16 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtGetSocInfoCallback
                        public void onValues(int rtGetSocInfoRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtGetSocInfoRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 30:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_rtSetSocInfoRet = rtSetSocInfo(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_rtSetSocInfoRet);
                    _hidl_reply.send();
                    return;
                case 31:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_lockBigCpuRet = lockBigCpu(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_lockBigCpuRet);
                    _hidl_reply.send();
                    return;
                case 32:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setLightSleepRet = setLightSleep(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setLightSleepRet);
                    _hidl_reply.send();
                    return;
                case 33:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    getApparatusModel(_hidl_request.readInt32(), _hidl_request.readInt32(), new IHwFactoryInterface.getApparatusModelCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass17 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.getApparatusModelCallback
                        public void onValues(int getApparatusModelRet, String buf) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getApparatusModelRet);
                            _hidl_reply.writeString(buf);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 34:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    voltageDetect(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), new IHwFactoryInterface.voltageDetectCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass18 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.voltageDetectCallback
                        public void onValues(int voltageDetectRet, String testResult) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(voltageDetectRet);
                            _hidl_reply.writeString(testResult);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 35:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    socVoltageOffset(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), new IHwFactoryInterface.socVoltageOffsetCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass19 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.socVoltageOffsetCallback
                        public void onValues(int socVoltageOffsetRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(socVoltageOffsetRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 36:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setHiseePowerctrlRet = setHiseePowerctrl(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setHiseePowerctrlRet);
                    _hidl_reply.send();
                    return;
                case 37:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtDDRTest(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), new IHwFactoryInterface.rtDDRTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass20 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtDDRTestCallback
                        public void onValues(int rtDDRTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtDDRTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 38:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtMapperTest(_hidl_request.readString(), new IHwFactoryInterface.rtMapperTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass21 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtMapperTestCallback
                        public void onValues(int rtMapperTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtMapperTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 39:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtEmmcStressAgingTest(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), new IHwFactoryInterface.rtEmmcStressAgingTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass22 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtEmmcStressAgingTestCallback
                        public void onValues(int rtEmmcStressAgingTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtEmmcStressAgingTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 40:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtUfsAgingTest(_hidl_request.readString(), new IHwFactoryInterface.rtUfsAgingTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass23 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtUfsAgingTestCallback
                        public void onValues(int rtUfsAgingTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtUfsAgingTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 41:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtStorageJoblessTest(_hidl_request.readString(), _hidl_request.readString(), new IHwFactoryInterface.rtStorageJoblessTestCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass24 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtStorageJoblessTestCallback
                        public void onValues(int rtStorageJoblessTestRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtStorageJoblessTestRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 42:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    rtStorageTestJudge(_hidl_request.readString(), new IHwFactoryInterface.rtStorageTestJudgeCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass25 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.rtStorageTestJudgeCallback
                        public void onValues(int rtStorageTestJudgeRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(rtStorageTestJudgeRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 43:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    setWifiCommand(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32(), new IHwFactoryInterface.setWifiCommandCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass26 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.setWifiCommandCallback
                        public void onValues(boolean succ, String buf) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(succ);
                            _hidl_reply.writeString(buf);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 44:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    connGetChecktimeOfChipcheck(_hidl_request.readInt32(), _hidl_request.readString(), new IHwFactoryInterface.connGetChecktimeOfChipcheckCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass27 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.connGetChecktimeOfChipcheckCallback
                        public void onValues(boolean succ, ArrayList<Integer> timeout, String outReserved) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(succ);
                            _hidl_reply.writeInt32Vector(timeout);
                            _hidl_reply.writeString(outReserved);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 45:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    connGetChipcheckResult(_hidl_request.readInt32(), _hidl_request.readString(), new IHwFactoryInterface.connGetChipcheckResultCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass28 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.connGetChipcheckResultCallback
                        public void onValues(boolean succ, String errmsg, String outReserved) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(succ);
                            _hidl_reply.writeString(errmsg);
                            _hidl_reply.writeString(outReserved);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 46:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    connChipcheck(_hidl_request.readInt32(), _hidl_request.readString(), new IHwFactoryInterface.connChipcheckCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass29 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.connChipcheckCallback
                        public void onValues(boolean succ, String errmsg, String outReserved) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(succ);
                            _hidl_reply.writeString(errmsg);
                            _hidl_reply.writeString(outReserved);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 47:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    FmdCmdData cmd = new FmdCmdData();
                    cmd.readFromParcel(_hidl_request);
                    int _hidl_out_sendFmdOperationRet = sendFmdOperation(cmd);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_sendFmdOperationRet);
                    _hidl_reply.send();
                    return;
                case 48:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    FmdMsg msg = new FmdMsg();
                    msg.readFromParcel(_hidl_request);
                    int _hidl_out_sendFmdDataRet = sendFmdData(msg);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_sendFmdDataRet);
                    _hidl_reply.send();
                    return;
                case 49:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    socVoltageOffsetByID(_hidl_request.readInt32(), new IHwFactoryInterface.socVoltageOffsetByIDCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass30 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.socVoltageOffsetByIDCallback
                        public void onValues(int socVoltageOffsetByIDRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(socVoltageOffsetByIDRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 50:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_ReleaseFacHidlResourceRet = ReleaseFacHidlResource();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ReleaseFacHidlResourceRet);
                    _hidl_reply.send();
                    return;
                case 51:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    vr_command_test(_hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), new IHwFactoryInterface.vr_command_testCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass31 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.vr_command_testCallback
                        public void onValues(int vr_command_testRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(vr_command_testRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 52:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.kInterfaceName);
                    hwGetDeviceId(_hidl_request.readInt32(), _hidl_request.readInt32(), new IHwFactoryInterface.hwGetDeviceIdCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass32 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_0.IHwFactoryInterface.hwGetDeviceIdCallback
                        public void onValues(int hwGetDeviceIdRet, String idBuf) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(hwGetDeviceIdRet);
                            _hidl_reply.writeString(idBuf);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 53:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    oeminfo_Read_reused(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), new oeminfo_Read_reusedCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass33 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.oeminfo_Read_reusedCallback
                        public void onValues(int oeminfo_Read_reusedRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(oeminfo_Read_reusedRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 54:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_oeminfo_write_reusedRet = oeminfo_write_reused(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_oeminfo_write_reusedRet);
                    _hidl_reply.send();
                    return;
                case 55:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_oeminfo_erase_reusedRet = oeminfo_erase_reused(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_oeminfo_erase_reusedRet);
                    _hidl_reply.send();
                    return;
                case 56:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    oeminfo_Read(_hidl_request.readInt32(), _hidl_request.readInt32(), new oeminfo_ReadCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass34 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.oeminfo_ReadCallback
                        public void onValues(int oeminfo_ReadRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(oeminfo_ReadRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 57:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_oeminfo_writeRet = oeminfo_write(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_oeminfo_writeRet);
                    _hidl_reply.send();
                    return;
                case 58:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_oeminfo_erase_reusedRet2 = oeminfo_erase(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_oeminfo_erase_reusedRet2);
                    _hidl_reply.send();
                    return;
                case 59:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    oeminfo_getinfo(_hidl_request.readInt32(), new oeminfo_getinfoCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass35 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.oeminfo_getinfoCallback
                        public void onValues(int oeminfo_getinfoRet, int total_blck, int total_byte) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(oeminfo_getinfoRet);
                            _hidl_reply.writeInt32(total_blck);
                            _hidl_reply.writeInt32(total_byte);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 60:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_nv_common_writeRet = nv_common_write(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_nv_common_writeRet);
                    _hidl_reply.send();
                    return;
                case 61:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    nv_common_read(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), new nv_common_readCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass36 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.nv_common_readCallback
                        public void onValues(int nv_common_readRet, String out) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(nv_common_readRet);
                            _hidl_reply.writeString(out);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 62:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    getControlFlag(_hidl_request.readInt32(), _hidl_request.readInt32(), new getControlFlagCallback() {
                        /* class vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.Stub.AnonymousClass37 */

                        @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.getControlFlagCallback
                        public void onValues(int getControlFlagRet, String pFlagBuf) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(getControlFlagRet);
                            _hidl_reply.writeString(pFlagBuf);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 63:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_setControlFlagRet = setControlFlag(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_setControlFlagRet);
                    _hidl_reply.send();
                    return;
                case 64:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IHwFactoryInterface.kInterfaceName);
                    int _hidl_out_getManufactureModeRet = getManufactureMode();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_getManufactureModeRet);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            String _hidl_out_descriptor = interfaceDescriptor();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            _hidl_reply.writeStatus(0);
                            HwBlob _hidl_blob = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob.putInt32(8, _hidl_vec_size);
                            _hidl_blob.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                                long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                                byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_0);
                                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                    throw new IllegalArgumentException("Array element is not of the expected length");
                                }
                                childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                            }
                            _hidl_blob.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ping();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            DebugInfo _hidl_out_info = getDebugInfo();
                            _hidl_reply.writeStatus(0);
                            _hidl_out_info.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                            return;
                        case 257120595:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
            }
        }
    }
}
