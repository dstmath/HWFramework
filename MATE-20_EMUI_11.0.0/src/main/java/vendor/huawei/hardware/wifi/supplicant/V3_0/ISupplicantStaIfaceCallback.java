package vendor.huawei.hardware.wifi.supplicant.V3_0;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
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
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface ISupplicantStaIfaceCallback extends android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback {
    public static final String kInterfaceName = "vendor.huawei.hardware.wifi.supplicant@3.0::ISupplicantStaIfaceCallback";

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifyStaIfaceEvent(int i, String str) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    void onAbsAntCoreRob() throws RemoteException;

    void onHilinkStartWps(String str) throws RemoteException;

    void onVoWifiIrqStr() throws RemoteException;

    void onWapiAuthFail() throws RemoteException;

    void onWapiCertInitFail() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static ISupplicantStaIfaceCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicantStaIfaceCallback)) {
            return (ISupplicantStaIfaceCallback) iface;
        }
        ISupplicantStaIfaceCallback proxy = new Proxy(binder);
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

    static ISupplicantStaIfaceCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static ISupplicantStaIfaceCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static ISupplicantStaIfaceCallback getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static ISupplicantStaIfaceCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static ISupplicantStaIfaceCallback getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements ISupplicantStaIfaceCallback {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.wifi.supplicant@3.0::ISupplicantStaIfaceCallback]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkAdded(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkRemoved(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(newState);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(id);
            _hidl_request.writeInt8Vector(ssid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            data.writeToParcel(_hidl_request);
            hs20Data.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeString(fileName);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt8(osuMethod);
            _hidl_request.writeString(url);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(reasonCode);
            _hidl_request.writeInt32(reAuthDelayInSec);
            _hidl_request.writeString(url);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeBool(locallyGenerated);
            _hidl_request.writeInt32(reasonCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(statusCode);
            _hidl_request.writeBool(timedOut);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAuthenticationTimeout(byte[] bssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onEapFailure() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onBssidChanged(byte reason, byte[] bssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt8(reason);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventSuccess() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (bssid == null || bssid.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt16(configError);
            _hidl_request.writeInt16(errorInd);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventPbcOverlap() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkStart(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkTimeout(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback
        public void onEapFailure_1_1(int errorCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(errorCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppSuccessConfigReceived(ArrayList<Byte> ssid, String password, byte[] psk, int securityAkm) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt8Vector(ssid);
            _hidl_request.writeString(password);
            HwBlob _hidl_blob = new HwBlob(32);
            if (psk == null || psk.length != 32) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, psk);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(securityAkm);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppSuccessConfigSent() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppProgress(int code) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(code);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppFailure(int code) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(code);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback
        public void onHilinkStartWps(String arg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeString(arg);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback
        public void onWapiCertInitFail() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback
        public void onWapiAuthFail() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback
        public void onAbsAntCoreRob() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback
        public void onVoWifiIrqStr() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback
        public void notifyStaIfaceEvent(int notifyType, String stringData) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(notifyType);
            _hidl_request.writeString(stringData);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISupplicantStaIfaceCallback {
        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISupplicantStaIfaceCallback.kInterfaceName, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback.kInterfaceName, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISupplicantStaIfaceCallback.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{121, -53, 62, -66, 1, -14, -2, -20, 19, -96, -73, 1, 1, 5, 83, -54, -126, -31, 104, 5, 102, 38, -121, 56, -79, 83, 30, -32, -41, 57, 91, 51}, new byte[]{9, -32, -117, 93, 18, -79, 9, 86, 46, -51, -40, -120, 37, 50, -3, 31, 44, 70, 57, 88, -114, 7, 118, -99, 92, 115, -106, -73, -59, -71, -13, 79}, new byte[]{-51, 67, 48, -61, 25, 107, -38, 29, 100, 42, 50, -85, -2, 35, -89, -42, 78, -65, -67, -89, 33, -108, 6, 67, -81, 104, 103, -81, 59, 63, 10, -87}, new byte[]{-41, -127, -56, -41, -25, -77, -2, 92, -54, -116, -10, -31, -40, Byte.MIN_VALUE, 110, 119, 9, -126, -82, 83, 88, -57, -127, 110, -43, 27, 15, 14, -62, 114, -25, 13}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISupplicantStaIfaceCallback.kInterfaceName.equals(descriptor)) {
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
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    onNetworkAdded(_hidl_request.readInt32());
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    onNetworkRemoved(_hidl_request.readInt32());
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    int newState = _hidl_request.readInt32();
                    byte[] bssid = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid, 6);
                    onStateChanged(newState, bssid, _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid2 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid2, 6);
                    ISupplicantStaIfaceCallback.AnqpData data = new ISupplicantStaIfaceCallback.AnqpData();
                    data.readFromParcel(_hidl_request);
                    ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data = new ISupplicantStaIfaceCallback.Hs20AnqpData();
                    hs20Data.readFromParcel(_hidl_request);
                    onAnqpQueryDone(bssid2, data, hs20Data);
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid3 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid3, 6);
                    onHs20IconQueryDone(bssid3, _hidl_request.readString(), _hidl_request.readInt8Vector());
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid4 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid4, 6);
                    onHs20SubscriptionRemediation(bssid4, _hidl_request.readInt8(), _hidl_request.readString());
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid5 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid5, 6);
                    onHs20DeauthImminentNotice(bssid5, _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid6 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid6, 6);
                    onDisconnected(bssid6, _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid7 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid7, 6);
                    onAssociationRejected(bssid7, _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid8 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid8, 6);
                    onAuthenticationTimeout(bssid8);
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    onEapFailure();
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte reason = _hidl_request.readInt8();
                    byte[] bssid9 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid9, 6);
                    onBssidChanged(reason, bssid9);
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    onWpsEventSuccess();
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    byte[] bssid10 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, bssid10, 6);
                    onWpsEventFail(bssid10, _hidl_request.readInt16(), _hidl_request.readInt16());
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    onWpsEventPbcOverlap();
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    onExtRadioWorkStart(_hidl_request.readInt32());
                    return;
                case 17:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.kInterfaceName);
                    onExtRadioWorkTimeout(_hidl_request.readInt32());
                    return;
                case 18:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback.kInterfaceName);
                    onEapFailure_1_1(_hidl_request.readInt32());
                    return;
                case 19:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
                    ArrayList<Byte> ssid = _hidl_request.readInt8Vector();
                    String password = _hidl_request.readString();
                    byte[] psk = new byte[32];
                    _hidl_request.readBuffer(32).copyToInt8Array(0, psk, 32);
                    onDppSuccessConfigReceived(ssid, password, psk, _hidl_request.readInt32());
                    return;
                case 20:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
                    onDppSuccessConfigSent();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /* 21 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
                    onDppProgress(_hidl_request.readInt32());
                    return;
                case 22:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback.kInterfaceName);
                    onDppFailure(_hidl_request.readInt32());
                    return;
                case 23:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onHilinkStartWps(_hidl_request.readString());
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onWapiCertInitFail();
                    return;
                case 25:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onWapiAuthFail();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED /* 26 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onAbsAntCoreRob();
                    return;
                case 27:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    onVoWifiIrqStr();
                    return;
                case 28:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIfaceCallback.kInterfaceName);
                    notifyStaIfaceEvent(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
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
