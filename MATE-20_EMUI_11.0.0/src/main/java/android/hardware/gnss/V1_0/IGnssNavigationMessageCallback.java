package android.hardware.gnss.V1_0;

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
import com.android.server.lights.LightsManagerEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IGnssNavigationMessageCallback extends IBase {
    public static final String kInterfaceName = "android.hardware.gnss@1.0::IGnssNavigationMessageCallback";

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void gnssNavigationMessageCb(GnssNavigationMessage gnssNavigationMessage) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IGnssNavigationMessageCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IGnssNavigationMessageCallback)) {
            return (IGnssNavigationMessageCallback) iface;
        }
        IGnssNavigationMessageCallback proxy = new Proxy(binder);
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

    static IGnssNavigationMessageCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IGnssNavigationMessageCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IGnssNavigationMessageCallback getService(boolean retry) throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT, retry);
    }

    static IGnssNavigationMessageCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IGnssNavigationMessageCallback getService() throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT);
    }

    public static final class GnssNavigationMessageType {
        public static final short BDS_D1 = 1281;
        public static final short BDS_D2 = 1282;
        public static final short GAL_F = 1538;
        public static final short GAL_I = 1537;
        public static final short GLO_L1CA = 769;
        public static final short GPS_CNAV2 = 260;
        public static final short GPS_L1CA = 257;
        public static final short GPS_L2CNAV = 258;
        public static final short GPS_L5CNAV = 259;
        public static final short UNKNOWN = 0;

        public static final String toString(short o) {
            if (o == 0) {
                return "UNKNOWN";
            }
            if (o == 257) {
                return "GPS_L1CA";
            }
            if (o == 258) {
                return "GPS_L2CNAV";
            }
            if (o == 259) {
                return "GPS_L5CNAV";
            }
            if (o == 260) {
                return "GPS_CNAV2";
            }
            if (o == 769) {
                return "GLO_L1CA";
            }
            if (o == 1281) {
                return "BDS_D1";
            }
            if (o == 1282) {
                return "BDS_D2";
            }
            if (o == 1537) {
                return "GAL_I";
            }
            if (o == 1538) {
                return "GAL_F";
            }
            return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
        }

        public static final String dumpBitfield(short o) {
            ArrayList<String> list = new ArrayList<>();
            short flipped = 0;
            list.add("UNKNOWN");
            if ((o & GPS_L1CA) == 257) {
                list.add("GPS_L1CA");
                flipped = (short) (0 | LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT);
            }
            if ((o & GPS_L2CNAV) == 258) {
                list.add("GPS_L2CNAV");
                flipped = (short) (flipped | GPS_L2CNAV);
            }
            if ((o & GPS_L5CNAV) == 259) {
                list.add("GPS_L5CNAV");
                flipped = (short) (flipped | GPS_L5CNAV);
            }
            if ((o & GPS_CNAV2) == 260) {
                list.add("GPS_CNAV2");
                flipped = (short) (flipped | GPS_CNAV2);
            }
            if ((o & GLO_L1CA) == 769) {
                list.add("GLO_L1CA");
                flipped = (short) (flipped | GLO_L1CA);
            }
            if ((o & BDS_D1) == 1281) {
                list.add("BDS_D1");
                flipped = (short) (flipped | BDS_D1);
            }
            if ((o & BDS_D2) == 1282) {
                list.add("BDS_D2");
                flipped = (short) (flipped | BDS_D2);
            }
            if ((o & GAL_I) == 1537) {
                list.add("GAL_I");
                flipped = (short) (flipped | GAL_I);
            }
            if ((o & GAL_F) == 1538) {
                list.add("GAL_F");
                flipped = (short) (flipped | GAL_F);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class NavigationMessageStatus {
        public static final short PARITY_PASSED = 1;
        public static final short PARITY_REBUILT = 2;
        public static final short UNKNOWN = 0;

        public static final String toString(short o) {
            if (o == 1) {
                return "PARITY_PASSED";
            }
            if (o == 2) {
                return "PARITY_REBUILT";
            }
            if (o == 0) {
                return "UNKNOWN";
            }
            return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
        }

        public static final String dumpBitfield(short o) {
            ArrayList<String> list = new ArrayList<>();
            short flipped = 0;
            if ((o & 1) == 1) {
                list.add("PARITY_PASSED");
                flipped = (short) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("PARITY_REBUILT");
                flipped = (short) (flipped | 2);
            }
            list.add("UNKNOWN");
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GnssNavigationMessage {
        public ArrayList<Byte> data = new ArrayList<>();
        public short messageId;
        public short status;
        public short submessageId;
        public short svid;
        public short type;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != GnssNavigationMessage.class) {
                return false;
            }
            GnssNavigationMessage other = (GnssNavigationMessage) otherObject;
            if (this.svid == other.svid && this.type == other.type && HidlSupport.deepEquals(Short.valueOf(this.status), Short.valueOf(other.status)) && this.messageId == other.messageId && this.submessageId == other.submessageId && HidlSupport.deepEquals(this.data, other.data)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.svid))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.messageId))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.submessageId))), Integer.valueOf(HidlSupport.deepHashCode(this.data)));
        }

        public final String toString() {
            return "{.svid = " + ((int) this.svid) + ", .type = " + GnssNavigationMessageType.toString(this.type) + ", .status = " + NavigationMessageStatus.dumpBitfield(this.status) + ", .messageId = " + ((int) this.messageId) + ", .submessageId = " + ((int) this.submessageId) + ", .data = " + this.data + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
        }

        public static final ArrayList<GnssNavigationMessage> readVectorFromParcel(HwParcel parcel) {
            ArrayList<GnssNavigationMessage> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                GnssNavigationMessage _hidl_vec_element = new GnssNavigationMessage();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.svid = _hidl_blob.getInt16(_hidl_offset + 0);
            this.type = _hidl_blob.getInt16(_hidl_offset + 2);
            this.status = _hidl_blob.getInt16(_hidl_offset + 4);
            this.messageId = _hidl_blob.getInt16(_hidl_offset + 6);
            this.submessageId = _hidl_blob.getInt16(_hidl_offset + 8);
            int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
            this.data.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.data.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
            }
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(32);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GnssNavigationMessage> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putInt16(_hidl_offset + 0, this.svid);
            _hidl_blob.putInt16(2 + _hidl_offset, this.type);
            _hidl_blob.putInt16(4 + _hidl_offset, this.status);
            _hidl_blob.putInt16(6 + _hidl_offset, this.messageId);
            _hidl_blob.putInt16(_hidl_offset + 8, this.submessageId);
            int _hidl_vec_size = this.data.size();
            _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
            _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                childBlob.putInt8((long) (_hidl_index_0 * 1), this.data.get(_hidl_index_0).byteValue());
            }
            _hidl_blob.putBlob(16 + _hidl_offset + 0, childBlob);
        }
    }

    public static final class Proxy implements IGnssNavigationMessageCallback {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.gnss@1.0::IGnssNavigationMessageCallback]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback
        public void gnssNavigationMessageCb(GnssNavigationMessage message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssNavigationMessageCallback.kInterfaceName);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IGnssNavigationMessageCallback {
        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IGnssNavigationMessageCallback.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IGnssNavigationMessageCallback.kInterfaceName;
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{78, 113, 105, -111, -99, 36, -5, -27, 87, 62, 91, -51, 104, 61, 11, -41, -85, -11, 83, -92, -26, -61, 76, 65, -7, -33, -63, -31, 32, 80, -37, 7}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.gnss.V1_0.IGnssNavigationMessageCallback, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IGnssNavigationMessageCallback.kInterfaceName.equals(descriptor)) {
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
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssNavigationMessageCallback.kInterfaceName);
                    GnssNavigationMessage message = new GnssNavigationMessage();
                    message.readFromParcel(_hidl_request);
                    gnssNavigationMessageCb(message);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
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
