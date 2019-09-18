package vendor.huawei.hardware.tp.V1_0;

import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.rms.iaware.AppTypeInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface ITouchscreen extends IBase {
    public static final String kInterfaceName = "vendor.huawei.hardware.tp@1.0::ITouchscreen";

    public static final class Proxy implements ITouchscreen {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.tp@1.0::ITouchscreen]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        public boolean hwTsSetGloveMode(boolean status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeBool(status);
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

        public boolean hwTsSetCoverMode(boolean status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeBool(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsSetCoverWindowSize(boolean status, int x0, int y0, int x1, int y2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeBool(status);
            _hidl_request.writeInt32(x0);
            _hidl_request.writeInt32(y0);
            _hidl_request.writeInt32(x1);
            _hidl_request.writeInt32(y2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsSetRoiEnable(boolean status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeBool(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hwTsGetRoiData(hwTsGetRoiDataCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readInt32Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void hwTsGetChipInfo(hwTsGetChipInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsSetEasyWeakupGesture(int gesture) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(gesture);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsSetEasyWeakupGestureReportEnable(boolean status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeBool(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hwTsGetEasyWeakupGuestureData(hwTsGetEasyWeakupGuestureDataCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readInt32Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsSetDozeMode(int optype, int status, int delaytime) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(optype);
            _hidl_request.writeInt32(status);
            _hidl_request.writeInt32(delaytime);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hwTsCapacitanceMmiTest(hwTsCapacitanceMmiTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void hwTsCapacitanceRunningTest(int runningTestStatus, hwTsCapacitanceRunningTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(runningTestStatus);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void hwTsCalibrationTest(int testMode, hwTsCalibrationTestCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(testMode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsSnrTest() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
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

        public boolean hwTsPressCalSetEndTimeOfAgeing() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public long hwTsPressCalGetLeftTimeOfStartCalibration() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt64();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalIsSupportCalibration() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalSetTypeOfCalibration(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalSet_range_of_spec(int range) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(range);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsPressCalGetStateOfHandle() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
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

        public boolean hwTsPressCalSetCountOfCalibration(int count) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(count);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsPressCalGetCountOfCalibration() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalSetSizeOfVerifyPoint(int size) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(size);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsPressCalGetSizeOfVerifyPoint() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsPressCalGetResultOfVerifyPoint(int point) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(point);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalOpenCalibrationModule() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalCloseCalibrationModule() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalStartCalibration(int number) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(number);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalStopCalibration(int number) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(number);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalStartVerify(int number) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(number);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean hwTsPressCalStopVerify(int number) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(number);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsPressCalGetVersionInformation(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(type);
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

        public int hwTsPressCalZcalPosChecking(int p, int x, int y) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(p);
            _hidl_request.writeInt32(x);
            _hidl_request.writeInt32(y);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsSetAftAlgoState(int enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsSetAftAlgoOrientation(int orientation) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(orientation);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public int hwTsSetAftConfig(String config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeString(config);
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

        public int hwTsSetCallback(ITPCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hwTsRunCommand(String command, String parameter, hwTsRunCommandCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeString(command);
            _hidl_request.writeString(parameter);
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

        public int hwSetFeatureConfig(int feature, String config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ITouchscreen.kInterfaceName);
            _hidl_request.writeInt32(feature);
            _hidl_request.writeString(config);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

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

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                int _hidl_index_0 = 0;
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                while (true) {
                    int _hidl_index_02 = _hidl_index_0;
                    if (_hidl_index_02 >= _hidl_vec_size) {
                        return _hidl_out_hashchain;
                    }
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_02 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                    _hidl_index_0 = _hidl_index_02 + 1;
                }
            } finally {
                _hidl_reply.release();
            }
        }

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

        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

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

        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ITouchscreen {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(new String[]{ITouchscreen.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return ITouchscreen.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[][]{new byte[]{-22, -48, 58, 41, 34, 66, -22, 118, 22, -13, -49, 122, 16, 97, -94, 105, 84, 86, 75, -26, 56, 24, 122, -3, 65, -124, 67, 36, -58, Byte.MIN_VALUE, -40, -82}, new byte[]{-67, -38, -74, 24, 77, 122, 52, 109, -90, -96, 125, -64, -126, -116, -15, -102, 105, 111, 76, -86, 54, 17, -59, 31, 46, 20, 86, 90, 20, -76, 15, -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ITouchscreen.kInterfaceName.equals(descriptor)) {
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

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            HwParcel hwParcel = _hidl_request;
            final HwParcel hwParcel2 = _hidl_reply;
            int _hidl_index_0 = 0;
            boolean _hidl_is_oneway = true;
            switch (_hidl_code) {
                case 1:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret = hwTsSetGloveMode(_hidl_request.readBool());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret);
                    _hidl_reply.send();
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret2 = hwTsSetCoverMode(_hidl_request.readBool());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret2);
                    _hidl_reply.send();
                    return;
                case 3:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret3 = hwTsSetCoverWindowSize(_hidl_request.readBool(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret3);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret4 = hwTsSetRoiEnable(_hidl_request.readBool());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret4);
                    _hidl_reply.send();
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    hwTsGetRoiData(new hwTsGetRoiDataCallback() {
                        public void onValues(boolean ret, ArrayList<Integer> roi_data) {
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeBool(ret);
                            hwParcel2.writeInt32Vector(roi_data);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 6:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    hwTsGetChipInfo(new hwTsGetChipInfoCallback() {
                        public void onValues(boolean ret, String chip_info) {
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeBool(ret);
                            hwParcel2.writeString(chip_info);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 7:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret5 = hwTsSetEasyWeakupGesture(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret5);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret6 = hwTsSetEasyWeakupGestureReportEnable(_hidl_request.readBool());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret6);
                    _hidl_reply.send();
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    hwTsGetEasyWeakupGuestureData(new hwTsGetEasyWeakupGuestureDataCallback() {
                        public void onValues(boolean ret, ArrayList<Integer> gusture_data) {
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeBool(ret);
                            hwParcel2.writeInt32Vector(gusture_data);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret7 = hwTsSetDozeMode(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret7);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    hwTsCapacitanceMmiTest(new hwTsCapacitanceMmiTestCallback() {
                        public void onValues(int ret, String fail_reason) {
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeInt32(ret);
                            hwParcel2.writeString(fail_reason);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    hwTsCapacitanceRunningTest(_hidl_request.readInt32(), new hwTsCapacitanceRunningTestCallback() {
                        public void onValues(int ret, String fail_reason) {
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeInt32(ret);
                            hwParcel2.writeString(fail_reason);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    hwTsCalibrationTest(_hidl_request.readInt32(), new hwTsCalibrationTestCallback() {
                        public void onValues(int ret, String fail_reason) {
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeInt32(ret);
                            hwParcel2.writeString(fail_reason);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 14:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_ret8 = hwTsSnrTest();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_ret8);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret9 = hwTsPressCalSetEndTimeOfAgeing();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret9);
                    _hidl_reply.send();
                    return;
                case 16:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    long _hidl_out_left_time = hwTsPressCalGetLeftTimeOfStartCalibration();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt64(_hidl_out_left_time);
                    _hidl_reply.send();
                    return;
                case 17:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret10 = hwTsPressCalIsSupportCalibration();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret10);
                    _hidl_reply.send();
                    return;
                case 18:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret11 = hwTsPressCalSetTypeOfCalibration(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret11);
                    _hidl_reply.send();
                    return;
                case 19:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret12 = hwTsPressCalSet_range_of_spec(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret12);
                    _hidl_reply.send();
                    return;
                case 20:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_status = hwTsPressCalGetStateOfHandle();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 21:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret13 = hwTsPressCalSetCountOfCalibration(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret13);
                    _hidl_reply.send();
                    return;
                case 22:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_number = hwTsPressCalGetCountOfCalibration();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_number);
                    _hidl_reply.send();
                    return;
                case 23:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret14 = hwTsPressCalSetSizeOfVerifyPoint(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret14);
                    _hidl_reply.send();
                    return;
                case 24:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_count = hwTsPressCalGetSizeOfVerifyPoint();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_count);
                    _hidl_reply.send();
                    return;
                case 25:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_result = hwTsPressCalGetResultOfVerifyPoint(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_result);
                    _hidl_reply.send();
                    return;
                case 26:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret15 = hwTsPressCalOpenCalibrationModule();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret15);
                    _hidl_reply.send();
                    return;
                case 27:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret16 = hwTsPressCalCloseCalibrationModule();
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret16);
                    _hidl_reply.send();
                    return;
                case 28:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret17 = hwTsPressCalStartCalibration(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret17);
                    _hidl_reply.send();
                    return;
                case 29:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret18 = hwTsPressCalStopCalibration(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret18);
                    _hidl_reply.send();
                    return;
                case 30:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret19 = hwTsPressCalStartVerify(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret19);
                    _hidl_reply.send();
                    return;
                case 31:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    boolean _hidl_out_ret20 = hwTsPressCalStopVerify(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeBool(_hidl_out_ret20);
                    _hidl_reply.send();
                    return;
                case 32:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_version = hwTsPressCalGetVersionInformation(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_version);
                    _hidl_reply.send();
                    return;
                case 33:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_code = hwTsPressCalZcalPosChecking(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_code);
                    _hidl_reply.send();
                    return;
                case 34:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_code2 = hwTsSetAftAlgoState(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_code2);
                    _hidl_reply.send();
                    return;
                case 35:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_code3 = hwTsSetAftAlgoOrientation(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_code3);
                    _hidl_reply.send();
                    return;
                case 36:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_code4 = hwTsSetAftConfig(_hidl_request.readString());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_code4);
                    _hidl_reply.send();
                    return;
                case 37:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_code5 = hwTsSetCallback(ITPCallback.asInterface(_hidl_request.readStrongBinder()));
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_code5);
                    _hidl_reply.send();
                    return;
                case 38:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    hwTsRunCommand(_hidl_request.readString(), _hidl_request.readString(), new hwTsRunCommandCallback() {
                        public void onValues(int ret, String status) {
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeInt32(ret);
                            hwParcel2.writeString(status);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 39:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ITouchscreen.kInterfaceName);
                    int _hidl_out_code6 = hwSetFeatureConfig(_hidl_request.readInt32(), _hidl_request.readString());
                    hwParcel2.writeStatus(0);
                    hwParcel2.writeInt32(_hidl_out_code6);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            hwParcel2.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            String _hidl_out_descriptor = interfaceDescriptor();
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            hwParcel2.writeStatus(0);
                            HwBlob _hidl_blob = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob.putInt32(8, _hidl_vec_size);
                            _hidl_blob.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            while (true) {
                                int _hidl_index_02 = _hidl_index_0;
                                if (_hidl_index_02 < _hidl_vec_size) {
                                    childBlob.putInt8Array((long) (_hidl_index_02 * 32), _hidl_out_hashchain.get(_hidl_index_02));
                                    _hidl_index_0 = _hidl_index_02 + 1;
                                } else {
                                    _hidl_blob.putBlob(0, childBlob);
                                    hwParcel2.writeBuffer(_hidl_blob);
                                    _hidl_reply.send();
                                    return;
                                }
                            }
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            ping();
                            hwParcel2.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            DebugInfo _hidl_out_info = getDebugInfo();
                            hwParcel2.writeStatus(0);
                            _hidl_out_info.writeToParcel(hwParcel2);
                            _hidl_reply.send();
                            return;
                        case 257120595:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                hwParcel2.writeStatus(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
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

    @FunctionalInterface
    public interface hwTsCalibrationTestCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface hwTsCapacitanceMmiTestCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface hwTsCapacitanceRunningTestCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface hwTsGetChipInfoCallback {
        void onValues(boolean z, String str);
    }

    @FunctionalInterface
    public interface hwTsGetEasyWeakupGuestureDataCallback {
        void onValues(boolean z, ArrayList<Integer> arrayList);
    }

    @FunctionalInterface
    public interface hwTsGetRoiDataCallback {
        void onValues(boolean z, ArrayList<Integer> arrayList);
    }

    @FunctionalInterface
    public interface hwTsRunCommandCallback {
        void onValues(int i, String str);
    }

    IHwBinder asBinder();

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    int hwSetFeatureConfig(int i, String str) throws RemoteException;

    void hwTsCalibrationTest(int i, hwTsCalibrationTestCallback hwtscalibrationtestcallback) throws RemoteException;

    void hwTsCapacitanceMmiTest(hwTsCapacitanceMmiTestCallback hwtscapacitancemmitestcallback) throws RemoteException;

    void hwTsCapacitanceRunningTest(int i, hwTsCapacitanceRunningTestCallback hwtscapacitancerunningtestcallback) throws RemoteException;

    void hwTsGetChipInfo(hwTsGetChipInfoCallback hwtsgetchipinfocallback) throws RemoteException;

    void hwTsGetEasyWeakupGuestureData(hwTsGetEasyWeakupGuestureDataCallback hwtsgeteasyweakupguesturedatacallback) throws RemoteException;

    void hwTsGetRoiData(hwTsGetRoiDataCallback hwtsgetroidatacallback) throws RemoteException;

    boolean hwTsPressCalCloseCalibrationModule() throws RemoteException;

    int hwTsPressCalGetCountOfCalibration() throws RemoteException;

    long hwTsPressCalGetLeftTimeOfStartCalibration() throws RemoteException;

    int hwTsPressCalGetResultOfVerifyPoint(int i) throws RemoteException;

    int hwTsPressCalGetSizeOfVerifyPoint() throws RemoteException;

    int hwTsPressCalGetStateOfHandle() throws RemoteException;

    int hwTsPressCalGetVersionInformation(int i) throws RemoteException;

    boolean hwTsPressCalIsSupportCalibration() throws RemoteException;

    boolean hwTsPressCalOpenCalibrationModule() throws RemoteException;

    boolean hwTsPressCalSetCountOfCalibration(int i) throws RemoteException;

    boolean hwTsPressCalSetEndTimeOfAgeing() throws RemoteException;

    boolean hwTsPressCalSetSizeOfVerifyPoint(int i) throws RemoteException;

    boolean hwTsPressCalSetTypeOfCalibration(int i) throws RemoteException;

    boolean hwTsPressCalSet_range_of_spec(int i) throws RemoteException;

    boolean hwTsPressCalStartCalibration(int i) throws RemoteException;

    boolean hwTsPressCalStartVerify(int i) throws RemoteException;

    boolean hwTsPressCalStopCalibration(int i) throws RemoteException;

    boolean hwTsPressCalStopVerify(int i) throws RemoteException;

    int hwTsPressCalZcalPosChecking(int i, int i2, int i3) throws RemoteException;

    void hwTsRunCommand(String str, String str2, hwTsRunCommandCallback hwtsruncommandcallback) throws RemoteException;

    int hwTsSetAftAlgoOrientation(int i) throws RemoteException;

    int hwTsSetAftAlgoState(int i) throws RemoteException;

    int hwTsSetAftConfig(String str) throws RemoteException;

    int hwTsSetCallback(ITPCallback iTPCallback) throws RemoteException;

    boolean hwTsSetCoverMode(boolean z) throws RemoteException;

    boolean hwTsSetCoverWindowSize(boolean z, int i, int i2, int i3, int i4) throws RemoteException;

    boolean hwTsSetDozeMode(int i, int i2, int i3) throws RemoteException;

    boolean hwTsSetEasyWeakupGesture(int i) throws RemoteException;

    boolean hwTsSetEasyWeakupGestureReportEnable(boolean z) throws RemoteException;

    boolean hwTsSetGloveMode(boolean z) throws RemoteException;

    boolean hwTsSetRoiEnable(boolean z) throws RemoteException;

    int hwTsSnrTest() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static ITouchscreen asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ITouchscreen)) {
            return (ITouchscreen) iface;
        }
        ITouchscreen proxy = new Proxy(binder);
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

    static ITouchscreen castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static ITouchscreen getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static ITouchscreen getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static ITouchscreen getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static ITouchscreen getService() throws RemoteException {
        return getService("default");
    }
}
