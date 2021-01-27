package vendor.huawei.hardware.biometrics.hwvoiceid.V2_1;

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
import android.util.FloatConsts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceIdClientCallback;

public interface IBiometricsVoiceId extends vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId {
    public static final String kInterfaceName = "vendor.huawei.hardware.biometrics.hwvoiceid@2.1::IBiometricsVoiceId";

    @FunctionalInterface
    public interface getVoiceMessageCallback {
        void onValues(int i, int i2, ArrayList<Byte> arrayList);
    }

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    int checkAlgoMatch(int i) throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getVoiceMessage(int i, getVoiceMessageCallback getvoicemessagecallback) throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    int sendVoiceMessage(ArrayList<Byte> arrayList, ArrayList<Byte> arrayList2) throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    int share(ArrayList<Byte> arrayList, ArrayList<Integer> arrayList2) throws RemoteException;

    @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IBiometricsVoiceId asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IBiometricsVoiceId)) {
            return (IBiometricsVoiceId) iface;
        }
        IBiometricsVoiceId proxy = new Proxy(binder);
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

    static IBiometricsVoiceId castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IBiometricsVoiceId getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IBiometricsVoiceId getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static IBiometricsVoiceId getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IBiometricsVoiceId getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IBiometricsVoiceId {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.biometrics.hwvoiceid@2.1::IBiometricsVoiceId]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public long setNotify(IBiometricsVoiceIdClientCallback clientCallback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeStrongBinder(clientCallback == null ? null : clientCallback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt64();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public long preEnroll() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt64();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int enroll(byte[] hat, byte[] headsetMac, String bondData, String micData, byte[] bondIV, byte[] micIV, int gid, int voiceType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(69);
            if (hat == null || hat.length != 69) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, hat);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(6);
            if (headsetMac == null || headsetMac.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob2.putInt8Array(0, headsetMac);
            _hidl_request.writeBuffer(_hidl_blob2);
            _hidl_request.writeString(bondData);
            _hidl_request.writeString(micData);
            HwBlob _hidl_blob3 = new HwBlob(16);
            if (bondIV == null || bondIV.length != 16) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob3.putInt8Array(0, bondIV);
            _hidl_request.writeBuffer(_hidl_blob3);
            HwBlob _hidl_blob4 = new HwBlob(16);
            if (micIV == null || micIV.length != 16) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob4.putInt8Array(0, micIV);
            _hidl_request.writeBuffer(_hidl_blob4);
            _hidl_request.writeInt32(gid);
            _hidl_request.writeInt32(voiceType);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int postEnroll() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int cancelEnroll() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int cancelAuthenticate() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int cancelRemove() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int remove(int gid, int voiceType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt32(gid);
            _hidl_request.writeInt32(voiceType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int removeAll(int gid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt32(gid);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public ArrayList<Integer> setActiveGroup(int gid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt32(gid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32Vector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int authenticate(long operationId, byte[] headsetMac, String bondData, String micData, byte[] bondIV, byte[] micIV, int voiceType, int gid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt64(operationId);
            HwBlob _hidl_blob = new HwBlob(6);
            if (headsetMac == null || headsetMac.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, headsetMac);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeString(bondData);
            _hidl_request.writeString(micData);
            HwBlob _hidl_blob2 = new HwBlob(16);
            if (bondIV == null || bondIV.length != 16) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob2.putInt8Array(0, bondIV);
            _hidl_request.writeBuffer(_hidl_blob2);
            HwBlob _hidl_blob3 = new HwBlob(16);
            if (micIV == null || micIV.length != 16) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob3.putInt8Array(0, micIV);
            _hidl_request.writeBuffer(_hidl_blob3);
            _hidl_request.writeInt32(voiceType);
            _hidl_request.writeInt32(gid);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int init() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int release() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public ArrayList<String> getEnrollWordList() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readStringVector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public byte[] genEncKey(byte[] headsetMac) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (headsetMac == null || headsetMac.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, headsetMac);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte[] _hidl_out_key = new byte[256];
                _hidl_reply.readBuffer(256).copyToInt8Array(0, _hidl_out_key, 256);
                return _hidl_out_key;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public ArrayList<Integer> getEnrolledVoiceTypes() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32Vector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public byte[] getRegStatus(byte[] headsetMac) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (headsetMac == null || headsetMac.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, headsetMac);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte[] _hidl_out_data = new byte[33];
                _hidl_reply.readBuffer(33).copyToInt8Array(0, _hidl_out_data, 33);
                return _hidl_out_data;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public byte[] getAbility(byte[] headsetProVer, byte[] headsetSn, byte[] headsetAbility, byte[] headsetWordList) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(4);
            if (headsetProVer == null || headsetProVer.length != 4) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, headsetProVer);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(16);
            if (headsetSn == null || headsetSn.length != 16) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob2.putInt8Array(0, headsetSn);
            _hidl_request.writeBuffer(_hidl_blob2);
            HwBlob _hidl_blob3 = new HwBlob(4);
            if (headsetAbility == null || headsetAbility.length != 4) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob3.putInt8Array(0, headsetAbility);
            _hidl_request.writeBuffer(_hidl_blob3);
            HwBlob _hidl_blob4 = new HwBlob(4);
            if (headsetWordList == null || headsetWordList.length != 4) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob4.putInt8Array(0, headsetWordList);
            _hidl_request.writeBuffer(_hidl_blob4);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte[] _hidl_out_data = new byte[13];
                _hidl_reply.readBuffer(13).copyToInt8Array(0, _hidl_out_data, 13);
                return _hidl_out_data;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public byte setPubKey(byte[] secKeyVer, byte[] pubKey, byte[] signature) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(4);
            if (secKeyVer == null || secKeyVer.length != 4) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, secKeyVer);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(256);
            if (pubKey == null || pubKey.length != 256) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob2.putInt8Array(0, pubKey);
            _hidl_request.writeBuffer(_hidl_blob2);
            HwBlob _hidl_blob3 = new HwBlob(256);
            if (signature == null || signature.length != 256) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob3.putInt8Array(0, signature);
            _hidl_request.writeBuffer(_hidl_blob3);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt8();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int notifyLostState() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public byte[] getUserStatus() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte[] _hidl_out_data = new byte[2];
                _hidl_reply.readBuffer(2).copyToInt8Array(0, _hidl_out_data, 2);
                return _hidl_out_data;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public int setParameter(String key, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeString(key);
            _hidl_request.writeString(value);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId
        public String getParameter(String key) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeString(key);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readString();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId
        public int checkAlgoMatch(int version) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt32(version);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId
        public int sendVoiceMessage(ArrayList<Byte> message, ArrayList<Byte> extraMessage) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt8Vector(message);
            _hidl_request.writeInt8Vector(extraMessage);
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId
        public void getVoiceMessage(int messageId, getVoiceMessageCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt32(messageId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt32(), _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId
        public int share(ArrayList<Byte> hat, ArrayList<Integer> voiceType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBiometricsVoiceId.kInterfaceName);
            _hidl_request.writeInt8Vector(hat);
            _hidl_request.writeInt32Vector(voiceType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IBiometricsVoiceId {
        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IBiometricsVoiceId.kInterfaceName, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IBiometricsVoiceId.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{114, 38, -105, -27, -6, -114, -51, 86, -97, 107, 125, 11, -4, -69, 39, -75, 23, 49, -109, -4, -98, -3, -56, 83, Byte.MAX_VALUE, -85, -22, -90, 24, 30, -121, 56}, new byte[]{-117, -59, 51, -32, 38, -45, -67, 111, 15, 58, 90, -69, -59, -83, -82, 103, -77, 86, -14, 83, -116, 113, 67, -2, -64, -50, -11, -5, -72, 65, -96, -52}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId, vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IBiometricsVoiceId.kInterfaceName.equals(descriptor)) {
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

        /* JADX INFO: Multiple debug info for r4v11 byte[]: [D('headsetSn' byte[]), D('_hidl_array_offset_0' long)] */
        /* JADX INFO: Multiple debug info for r5v4 byte[]: [D('_hidl_blob' android.os.HwBlob), D('headsetAbility' byte[])] */
        /* JADX INFO: Multiple debug info for r6v7 byte[]: [D('_hidl_blob' android.os.HwBlob), D('headsetWordList' byte[])] */
        /* JADX INFO: Multiple debug info for r1v43 byte[]: [D('_hidl_blob' android.os.HwBlob), D('pubKey' byte[])] */
        /* JADX INFO: Multiple debug info for r2v15 byte[]: [D('_hidl_blob' android.os.HwBlob), D('signature' byte[])] */
        public void onTransact(int _hidl_code, HwParcel _hidl_request, final HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    boolean _hidl_is_oneway3 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway3 = false;
                    }
                    if (_hidl_is_oneway3) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    long _hidl_out_deviceId = setNotify(IBiometricsVoiceIdClientCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt64(_hidl_out_deviceId);
                    _hidl_reply.send();
                    return;
                case 2:
                    boolean _hidl_is_oneway4 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway4 = false;
                    }
                    if (_hidl_is_oneway4) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    long _hidl_out_authChallenge = preEnroll();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt64(_hidl_out_authChallenge);
                    _hidl_reply.send();
                    return;
                case 3:
                    boolean _hidl_is_oneway5 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway5 = false;
                    }
                    if (_hidl_is_oneway5) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    byte[] hat = new byte[69];
                    _hidl_request.readBuffer(69).copyToInt8Array(0, hat, 69);
                    byte[] headsetMac = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, headsetMac, 6);
                    String bondData = _hidl_request.readString();
                    String micData = _hidl_request.readString();
                    byte[] bondIV = new byte[16];
                    _hidl_request.readBuffer(16).copyToInt8Array(0, bondIV, 16);
                    byte[] micIV = new byte[16];
                    _hidl_request.readBuffer(16).copyToInt8Array(0, micIV, 16);
                    int _hidl_out_debugErrno = enroll(hat, headsetMac, bondData, micData, bondIV, micIV, _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_debugErrno);
                    _hidl_reply.send();
                    return;
                case 4:
                    boolean _hidl_is_oneway6 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway6 = false;
                    }
                    if (_hidl_is_oneway6) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_debugErrno2 = postEnroll();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_debugErrno2);
                    _hidl_reply.send();
                    return;
                case 5:
                    boolean _hidl_is_oneway7 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway7 = false;
                    }
                    if (_hidl_is_oneway7) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_debugErrno3 = cancelEnroll();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_debugErrno3);
                    _hidl_reply.send();
                    return;
                case 6:
                    boolean _hidl_is_oneway8 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway8 = false;
                    }
                    if (_hidl_is_oneway8) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_debugErrno4 = cancelAuthenticate();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_debugErrno4);
                    _hidl_reply.send();
                    return;
                case 7:
                    boolean _hidl_is_oneway9 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway9 = false;
                    }
                    if (_hidl_is_oneway9) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_debugErrno5 = cancelRemove();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_debugErrno5);
                    _hidl_reply.send();
                    return;
                case 8:
                    boolean _hidl_is_oneway10 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway10 = false;
                    }
                    if (_hidl_is_oneway10) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_debugErrno6 = remove(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_debugErrno6);
                    _hidl_reply.send();
                    return;
                case 9:
                    boolean _hidl_is_oneway11 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway11 = false;
                    }
                    if (_hidl_is_oneway11) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_debugErrno7 = removeAll(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_debugErrno7);
                    _hidl_reply.send();
                    return;
                case 10:
                    boolean _hidl_is_oneway12 = true;
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway12 = false;
                    }
                    if (_hidl_is_oneway12) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    ArrayList<Integer> _hidl_out_voiceType = setActiveGroup(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32Vector(_hidl_out_voiceType);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    long operationId = _hidl_request.readInt64();
                    byte[] headsetMac2 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, headsetMac2, 6);
                    String bondData2 = _hidl_request.readString();
                    String micData2 = _hidl_request.readString();
                    byte[] bondIV2 = new byte[16];
                    _hidl_request.readBuffer(16).copyToInt8Array(0, bondIV2, 16);
                    byte[] micIV2 = new byte[16];
                    _hidl_request.readBuffer(16).copyToInt8Array(0, micIV2, 16);
                    int _hidl_out_bugErrno = authenticate(operationId, headsetMac2, bondData2, micData2, bondIV2, micIV2, _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_bugErrno);
                    _hidl_reply.send();
                    return;
                case 12:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_result = init();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_result);
                    _hidl_reply.send();
                    return;
                case 13:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_result2 = release();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_result2);
                    _hidl_reply.send();
                    return;
                case 14:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    ArrayList<String> _hidl_out_wordList = getEnrollWordList();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_wordList);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    byte[] headsetMac3 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, headsetMac3, 6);
                    byte[] _hidl_out_key = genEncKey(headsetMac3);
                    _hidl_reply.writeStatus(0);
                    HwBlob _hidl_blob = new HwBlob(256);
                    if (_hidl_out_key == null || _hidl_out_key.length != 256) {
                        throw new IllegalArgumentException("Array element is not of the expected length");
                    }
                    _hidl_blob.putInt8Array(0, _hidl_out_key);
                    _hidl_reply.writeBuffer(_hidl_blob);
                    _hidl_reply.send();
                    return;
                case 16:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    ArrayList<Integer> _hidl_out_voiceType2 = getEnrolledVoiceTypes();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32Vector(_hidl_out_voiceType2);
                    _hidl_reply.send();
                    return;
                case 17:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    byte[] headsetMac4 = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, headsetMac4, 6);
                    byte[] _hidl_out_data = getRegStatus(headsetMac4);
                    _hidl_reply.writeStatus(0);
                    HwBlob _hidl_blob2 = new HwBlob(33);
                    if (_hidl_out_data == null || _hidl_out_data.length != 33) {
                        throw new IllegalArgumentException("Array element is not of the expected length");
                    }
                    _hidl_blob2.putInt8Array(0, _hidl_out_data);
                    _hidl_reply.writeBuffer(_hidl_blob2);
                    _hidl_reply.send();
                    return;
                case 18:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    byte[] headsetProVer = new byte[4];
                    _hidl_request.readBuffer(4).copyToInt8Array(0, headsetProVer, 4);
                    byte[] headsetSn = new byte[16];
                    _hidl_request.readBuffer(16).copyToInt8Array(0, headsetSn, 16);
                    byte[] headsetAbility = new byte[4];
                    _hidl_request.readBuffer(4).copyToInt8Array(0, headsetAbility, 4);
                    byte[] headsetWordList = new byte[4];
                    _hidl_request.readBuffer(4).copyToInt8Array(0, headsetWordList, 4);
                    byte[] _hidl_out_data2 = getAbility(headsetProVer, headsetSn, headsetAbility, headsetWordList);
                    _hidl_reply.writeStatus(0);
                    HwBlob _hidl_blob3 = new HwBlob(13);
                    if (_hidl_out_data2 == null || _hidl_out_data2.length != 13) {
                        throw new IllegalArgumentException("Array element is not of the expected length");
                    }
                    _hidl_blob3.putInt8Array(0, _hidl_out_data2);
                    _hidl_reply.writeBuffer(_hidl_blob3);
                    _hidl_reply.send();
                    return;
                case 19:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    byte[] secKeyVer = new byte[4];
                    _hidl_request.readBuffer(4).copyToInt8Array(0, secKeyVer, 4);
                    byte[] pubKey = new byte[256];
                    _hidl_request.readBuffer(256).copyToInt8Array(0, pubKey, 256);
                    byte[] signature = new byte[256];
                    _hidl_request.readBuffer(256).copyToInt8Array(0, signature, 256);
                    byte _hidl_out_data3 = setPubKey(secKeyVer, pubKey, signature);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8(_hidl_out_data3);
                    _hidl_reply.send();
                    return;
                case 20:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_result3 = notifyLostState();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_result3);
                    _hidl_reply.send();
                    return;
                case 21:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    byte[] _hidl_out_data4 = getUserStatus();
                    _hidl_reply.writeStatus(0);
                    HwBlob _hidl_blob4 = new HwBlob(2);
                    if (_hidl_out_data4 == null || _hidl_out_data4.length != 2) {
                        throw new IllegalArgumentException("Array element is not of the expected length");
                    }
                    _hidl_blob4.putInt8Array(0, _hidl_out_data4);
                    _hidl_reply.writeBuffer(_hidl_blob4);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_result4 = setParameter(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_result4);
                    _hidl_reply.send();
                    return;
                case 23:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId.kInterfaceName);
                    String _hidl_out_value = getParameter(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_value);
                    _hidl_reply.send();
                    return;
                case 24:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_result5 = checkAlgoMatch(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_result5);
                    _hidl_reply.send();
                    return;
                case 25:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_result6 = sendVoiceMessage(_hidl_request.readInt8Vector(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_result6);
                    _hidl_reply.send();
                    return;
                case 26:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IBiometricsVoiceId.kInterfaceName);
                    getVoiceMessage(_hidl_request.readInt32(), new getVoiceMessageCallback() {
                        /* class vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId.Stub.AnonymousClass1 */

                        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_1.IBiometricsVoiceId.getVoiceMessageCallback
                        public void onValues(int result, int version, ArrayList<Byte> mac) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(result);
                            _hidl_reply.writeInt32(version);
                            _hidl_reply.writeInt8Vector(mac);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 27:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IBiometricsVoiceId.kInterfaceName);
                    int _hidl_out_result7 = share(_hidl_request.readInt8Vector(), _hidl_request.readInt32Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_result7);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
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
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
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
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
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
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            _hidl_reply.writeStatus(0);
                            HwBlob _hidl_blob5 = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob5.putInt32(8, _hidl_vec_size);
                            _hidl_blob5.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                                long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                                byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_0);
                                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                    throw new IllegalArgumentException("Array element is not of the expected length");
                                }
                                childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                            }
                            _hidl_blob5.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob5);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
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
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
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
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) FloatConsts.SIGN_BIT_MASK);
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
