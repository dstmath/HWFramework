package vendor.huawei.hardware.fusd.V1_7;

import android.hardware.gnss.V1_0.GnssLocation;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.display.HwUibcReceiver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback;
import vendor.huawei.hardware.fusd.V1_6.FdGeofenceSize;

public interface IFusdGeofenceCallback extends vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback {
    public static final String kInterfaceName = "vendor.huawei.hardware.fusd@1.7::IFusdGeofenceCallback";

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    void onGpsIntervalChangeCb(GpsConfig gpsConfig) throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IFusdGeofenceCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IFusdGeofenceCallback)) {
            return (IFusdGeofenceCallback) iface;
        }
        IFusdGeofenceCallback proxy = new Proxy(binder);
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

    static IFusdGeofenceCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IFusdGeofenceCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IFusdGeofenceCallback getService(boolean retry) throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT, retry);
    }

    static IFusdGeofenceCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IFusdGeofenceCallback getService() throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT);
    }

    public static final class Proxy implements IFusdGeofenceCallback {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.fusd@1.7::IFusdGeofenceCallback]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback
        public void geofenceAddResultCb(ArrayList<IFusdGeofenceCallback.GeofenceResult> results) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
            IFusdGeofenceCallback.GeofenceResult.writeVectorToParcel(_hidl_request, results);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback
        public void geofenceRemoveResultCb(ArrayList<IFusdGeofenceCallback.GeofenceResult> results) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
            IFusdGeofenceCallback.GeofenceResult.writeVectorToParcel(_hidl_request, results);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback
        public void onGetCurrentLocationCb(int locSource, GnssLocation location, long timeUnc, long posUnc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
            _hidl_request.writeInt32(locSource);
            location.writeToParcel(_hidl_request);
            _hidl_request.writeInt64(timeUnc);
            _hidl_request.writeInt64(posUnc);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback
        public void gnssGeofenceTransitionCb(int geofenceId, GnssLocation location, int transition, long timestamp) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            location.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(transition);
            _hidl_request.writeInt64(timestamp);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback
        public void gnssGeofenceStatusCb(int status, GnssLocation lastLocation) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
            _hidl_request.writeInt32(status);
            lastLocation.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback
        public void gnssGeofencePauseCb(int geofenceId, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback
        public void gnssGeofenceResumeCb(int geofenceId, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback
        public void onGetGeofenceSizeCb(FdGeofenceSize size) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback.kInterfaceName);
            size.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback
        public void onGpsIntervalChangeCb(GpsConfig gpsConfig) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IFusdGeofenceCallback.kInterfaceName);
            gpsConfig.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IFusdGeofenceCallback {
        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IFusdGeofenceCallback.kInterfaceName, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback.kInterfaceName, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IFusdGeofenceCallback.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-111, 63, -12, -11, -39, 68, -56, 94, 43, -69, 103, 91, 105, 95, HwUibcReceiver.CurrentPacket.INPUT_MASK, 30, -112, -78, -88, Byte.MIN_VALUE, -33, -76, 97, -100, 116, -113, -85, 96, -114, 8, -25, 5}, new byte[]{6, 35, -2, -69, Byte.MIN_VALUE, -3, 71, -41, -37, 84, 74, 8, -35, 22, -107, 49, -127, 6, 117, 88, 121, 80, 10, -70, 7, -49, 104, 63, -97, -100, -31, -109}, new byte[]{-89, 124, 112, -74, -14, 27, 31, -111, -49, -24, -112, 67, 26, -68, 45, 70, 1, -125, -127, 88, 50, -52, -16, 32, 123, -78, -26, -8, 121, -45, -45, 20}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback, vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IFusdGeofenceCallback.kInterfaceName.equals(descriptor)) {
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
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
                    geofenceAddResultCb(IFusdGeofenceCallback.GeofenceResult.readVectorFromParcel(_hidl_request));
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
                    geofenceRemoveResultCb(IFusdGeofenceCallback.GeofenceResult.readVectorFromParcel(_hidl_request));
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
                    int locSource = _hidl_request.readInt32();
                    GnssLocation location = new GnssLocation();
                    location.readFromParcel(_hidl_request);
                    onGetCurrentLocationCb(locSource, location, _hidl_request.readInt64(), _hidl_request.readInt64());
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
                    int geofenceId = _hidl_request.readInt32();
                    GnssLocation location2 = new GnssLocation();
                    location2.readFromParcel(_hidl_request);
                    gnssGeofenceTransitionCb(geofenceId, location2, _hidl_request.readInt32(), _hidl_request.readInt64());
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
                    int status = _hidl_request.readInt32();
                    GnssLocation lastLocation = new GnssLocation();
                    lastLocation.readFromParcel(_hidl_request);
                    gnssGeofenceStatusCb(status, lastLocation);
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
                    gnssGeofencePauseCb(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceCallback.kInterfaceName);
                    gnssGeofenceResumeCb(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceCallback.kInterfaceName);
                    FdGeofenceSize size = new FdGeofenceSize();
                    size.readFromParcel(_hidl_request);
                    onGetGeofenceSizeCb(size);
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IFusdGeofenceCallback.kInterfaceName);
                    GpsConfig gpsConfig = new GpsConfig();
                    gpsConfig.readFromParcel(_hidl_request);
                    onGpsIntervalChangeCb(gpsConfig);
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
