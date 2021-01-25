package android.hardware.wifi.V1_2;

import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChipEventCallback;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.IWifiP2pIface;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.WifiDebugHostWakeReasonStats;
import android.hardware.wifi.V1_0.WifiDebugRingBufferStatus;
import android.hardware.wifi.V1_0.WifiStatus;
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

public interface IWifiChip extends android.hardware.wifi.V1_1.IWifiChip {
    public static final String kInterfaceName = "android.hardware.wifi@1.2::IWifiChip";

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    WifiStatus registerEventCallback_1_2(IWifiChipEventCallback iWifiChipEventCallback) throws RemoteException;

    WifiStatus selectTxPowerScenario_1_2(int i) throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IWifiChip asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IWifiChip)) {
            return (IWifiChip) iface;
        }
        IWifiChip proxy = new Proxy(binder);
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

    static IWifiChip castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IWifiChip getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IWifiChip getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static IWifiChip getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiChip getService() throws RemoteException {
        return getService("default");
    }

    public static final class ChipCapabilityMask {
        public static final int D2AP_RTT = 1024;
        public static final int D2D_RTT = 512;
        public static final int DEBUG_ERROR_ALERTS = 128;
        public static final int DEBUG_HOST_WAKE_REASON_STATS = 64;
        public static final int DEBUG_MEMORY_DRIVER_DUMP = 2;
        public static final int DEBUG_MEMORY_FIRMWARE_DUMP = 1;
        public static final int DEBUG_RING_BUFFER_CONNECT_EVENT = 4;
        public static final int DEBUG_RING_BUFFER_POWER_EVENT = 8;
        public static final int DEBUG_RING_BUFFER_VENDOR_DATA = 32;
        public static final int DEBUG_RING_BUFFER_WAKELOCK_EVENT = 16;
        public static final int SET_TX_POWER_LIMIT = 256;
        public static final int USE_BODY_HEAD_SAR = 2048;

        public static final String toString(int o) {
            if (o == 1) {
                return "DEBUG_MEMORY_FIRMWARE_DUMP";
            }
            if (o == 2) {
                return "DEBUG_MEMORY_DRIVER_DUMP";
            }
            if (o == 4) {
                return "DEBUG_RING_BUFFER_CONNECT_EVENT";
            }
            if (o == 8) {
                return "DEBUG_RING_BUFFER_POWER_EVENT";
            }
            if (o == 16) {
                return "DEBUG_RING_BUFFER_WAKELOCK_EVENT";
            }
            if (o == 32) {
                return "DEBUG_RING_BUFFER_VENDOR_DATA";
            }
            if (o == 64) {
                return "DEBUG_HOST_WAKE_REASON_STATS";
            }
            if (o == 128) {
                return "DEBUG_ERROR_ALERTS";
            }
            if (o == 256) {
                return "SET_TX_POWER_LIMIT";
            }
            if (o == 512) {
                return "D2D_RTT";
            }
            if (o == 1024) {
                return "D2AP_RTT";
            }
            if (o == 2048) {
                return "USE_BODY_HEAD_SAR";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("DEBUG_MEMORY_FIRMWARE_DUMP");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("DEBUG_MEMORY_DRIVER_DUMP");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("DEBUG_RING_BUFFER_CONNECT_EVENT");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("DEBUG_RING_BUFFER_POWER_EVENT");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("DEBUG_RING_BUFFER_WAKELOCK_EVENT");
                flipped |= 16;
            }
            if ((o & 32) == 32) {
                list.add("DEBUG_RING_BUFFER_VENDOR_DATA");
                flipped |= 32;
            }
            if ((o & 64) == 64) {
                list.add("DEBUG_HOST_WAKE_REASON_STATS");
                flipped |= 64;
            }
            if ((o & 128) == 128) {
                list.add("DEBUG_ERROR_ALERTS");
                flipped |= 128;
            }
            if ((o & 256) == 256) {
                list.add("SET_TX_POWER_LIMIT");
                flipped |= 256;
            }
            if ((o & 512) == 512) {
                list.add("D2D_RTT");
                flipped |= 512;
            }
            if ((o & 1024) == 1024) {
                list.add("D2AP_RTT");
                flipped |= 1024;
            }
            if ((o & 2048) == 2048) {
                list.add("USE_BODY_HEAD_SAR");
                flipped |= 2048;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class TxPowerScenario {
        public static final int ON_BODY_CELL_OFF = 3;
        public static final int ON_BODY_CELL_ON = 4;
        public static final int ON_HEAD_CELL_OFF = 1;
        public static final int ON_HEAD_CELL_ON = 2;
        public static final int VOICE_CALL = 0;

        public static final String toString(int o) {
            if (o == 0) {
                return "VOICE_CALL";
            }
            if (o == 1) {
                return "ON_HEAD_CELL_OFF";
            }
            if (o == 2) {
                return "ON_HEAD_CELL_ON";
            }
            if (o == 3) {
                return "ON_BODY_CELL_OFF";
            }
            if (o == 4) {
                return "ON_BODY_CELL_ON";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("VOICE_CALL");
            if ((o & 1) == 1) {
                list.add("ON_HEAD_CELL_OFF");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("ON_HEAD_CELL_ON");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("ON_BODY_CELL_OFF");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("ON_BODY_CELL_ON");
                flipped |= 4;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements IWifiChip {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi@1.2::IWifiChip]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getId(IWifiChip.getIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus registerEventCallback(IWifiChipEventCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getCapabilities(IWifiChip.getCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getAvailableModes(IWifiChip.getAvailableModesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiChip.ChipMode.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus configureChip(int modeId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeInt32(modeId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getMode(IWifiChip.getModeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void requestChipDebugInfo(IWifiChip.requestChipDebugInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                IWifiChip.ChipDebugInfo _hidl_out_chipDebugInfo = new IWifiChip.ChipDebugInfo();
                _hidl_out_chipDebugInfo.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_chipDebugInfo);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void requestDriverDebugDump(IWifiChip.requestDriverDebugDumpCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void requestFirmwareDebugDump(IWifiChip.requestFirmwareDebugDumpCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void createApIface(IWifiChip.createApIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiApIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getApIfaceNames(IWifiChip.getApIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getApIface(String ifname, IWifiChip.getApIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiApIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus removeApIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void createNanIface(IWifiChip.createNanIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiNanIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getNanIfaceNames(IWifiChip.getNanIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getNanIface(String ifname, IWifiChip.getNanIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiNanIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus removeNanIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void createP2pIface(IWifiChip.createP2pIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiP2pIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getP2pIfaceNames(IWifiChip.getP2pIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getP2pIface(String ifname, IWifiChip.getP2pIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiP2pIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus removeP2pIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void createStaIface(IWifiChip.createStaIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiStaIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getStaIfaceNames(IWifiChip.getStaIfaceNamesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readStringVector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getStaIface(String ifname, IWifiChip.getStaIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiStaIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus removeStaIface(String ifname) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ifname);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void createRttController(IWifiIface boundIface, IWifiChip.createRttControllerCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeStrongBinder(boundIface == null ? null : boundIface.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiRttController.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getDebugRingBuffersStatus(IWifiChip.getDebugRingBuffersStatusCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, WifiDebugRingBufferStatus.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus startLoggingToDebugRingBuffer(String ringName, int verboseLevel, int maxIntervalInSec, int minDataSizeInBytes) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ringName);
            _hidl_request.writeInt32(verboseLevel);
            _hidl_request.writeInt32(maxIntervalInSec);
            _hidl_request.writeInt32(minDataSizeInBytes);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus forceDumpToDebugRingBuffer(String ringName) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeString(ringName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus stopLoggingToDebugRingBuffer() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public void getDebugHostWakeReasonStats(IWifiChip.getDebugHostWakeReasonStatsCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                WifiDebugHostWakeReasonStats _hidl_out_stats = new WifiDebugHostWakeReasonStats();
                _hidl_out_stats.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_stats);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChip
        public WifiStatus enableDebugErrorAlerts(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_1.IWifiChip
        public WifiStatus selectTxPowerScenario(int scenario) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_1.IWifiChip.kInterfaceName);
            _hidl_request.writeInt32(scenario);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_1.IWifiChip
        public WifiStatus resetTxPowerScenario() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_1.IWifiChip.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip
        public WifiStatus selectTxPowerScenario_1_2(int scenario) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeInt32(scenario);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip
        public WifiStatus registerEventCallback_1_2(IWifiChipEventCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChip.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IWifiChip {
        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IWifiChip.kInterfaceName, android.hardware.wifi.V1_1.IWifiChip.kInterfaceName, android.hardware.wifi.V1_0.IWifiChip.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IWifiChip.kInterfaceName;
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{120, 12, 22, -3, -19, -95, 59, 119, -99, -103, 57, 83, -90, Byte.MAX_VALUE, 124, -91, 120, -55, 56, -95, 114, -87, 66, 76, 28, 113, 90, -24, 27, -60, 15, -41}, new byte[]{-80, 86, -31, -34, -6, -76, 7, 21, -124, 33, 69, -124, 5, 125, 11, -57, 58, 97, 48, -127, -65, 17, 82, 89, 5, 73, 100, -99, 69, -126, -63, 60}, new byte[]{-13, -18, -52, 72, -99, -21, 76, 116, -119, 47, 89, -21, 122, -37, 118, -112, 99, -67, 92, 53, 74, -63, 50, -74, 38, -91, -12, 43, 54, 61, 54, -68}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.wifi.V1_2.IWifiChip, android.hardware.wifi.V1_1.IWifiChip, android.hardware.wifi.V1_0.IWifiChip, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IWifiChip.kInterfaceName.equals(descriptor)) {
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

        public void onTransact(int _hidl_code, HwParcel _hidl_request, final HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getId(new IWifiChip.getIdCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass1 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getIdCallback
                        public void onValues(WifiStatus status, int id) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(id);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status = registerEventCallback(IWifiChipEventCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getCapabilities(new IWifiChip.getCapabilitiesCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass2 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getCapabilitiesCallback
                        public void onValues(WifiStatus status, int capabilities) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(capabilities);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getAvailableModes(new IWifiChip.getAvailableModesCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass3 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getAvailableModesCallback
                        public void onValues(WifiStatus status, ArrayList<IWifiChip.ChipMode> modes) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            IWifiChip.ChipMode.writeVectorToParcel(_hidl_reply, modes);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status2 = configureChip(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getMode(new IWifiChip.getModeCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass4 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getModeCallback
                        public void onValues(WifiStatus status, int modeId) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(modeId);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    requestChipDebugInfo(new IWifiChip.requestChipDebugInfoCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass5 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.requestChipDebugInfoCallback
                        public void onValues(WifiStatus status, IWifiChip.ChipDebugInfo chipDebugInfo) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            chipDebugInfo.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    requestDriverDebugDump(new IWifiChip.requestDriverDebugDumpCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass6 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.requestDriverDebugDumpCallback
                        public void onValues(WifiStatus status, ArrayList<Byte> blob) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    requestFirmwareDebugDump(new IWifiChip.requestFirmwareDebugDumpCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass7 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.requestFirmwareDebugDumpCallback
                        public void onValues(WifiStatus status, ArrayList<Byte> blob) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    createApIface(new IWifiChip.createApIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass8 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.createApIfaceCallback
                        public void onValues(WifiStatus status, IWifiApIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getApIfaceNames(new IWifiChip.getApIfaceNamesCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass9 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getApIfaceNamesCallback
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getApIface(_hidl_request.readString(), new IWifiChip.getApIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass10 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getApIfaceCallback
                        public void onValues(WifiStatus status, IWifiApIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 13:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status3 = removeApIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    createNanIface(new IWifiChip.createNanIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass11 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.createNanIfaceCallback
                        public void onValues(WifiStatus status, IWifiNanIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getNanIfaceNames(new IWifiChip.getNanIfaceNamesCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass12 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getNanIfaceNamesCallback
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getNanIface(_hidl_request.readString(), new IWifiChip.getNanIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass13 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getNanIfaceCallback
                        public void onValues(WifiStatus status, IWifiNanIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 17:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status4 = removeNanIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status4.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 18:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    createP2pIface(new IWifiChip.createP2pIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass14 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.createP2pIfaceCallback
                        public void onValues(WifiStatus status, IWifiP2pIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 19:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getP2pIfaceNames(new IWifiChip.getP2pIfaceNamesCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass15 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceNamesCallback
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 20:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getP2pIface(_hidl_request.readString(), new IWifiChip.getP2pIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass16 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceCallback
                        public void onValues(WifiStatus status, IWifiP2pIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /* 21 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status5 = removeP2pIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status5.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    createStaIface(new IWifiChip.createStaIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass17 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.createStaIfaceCallback
                        public void onValues(WifiStatus status, IWifiStaIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 23:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getStaIfaceNames(new IWifiChip.getStaIfaceNamesCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass18 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getStaIfaceNamesCallback
                        public void onValues(WifiStatus status, ArrayList<String> ifnames) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStringVector(ifnames);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getStaIface(_hidl_request.readString(), new IWifiChip.getStaIfaceCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass19 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getStaIfaceCallback
                        public void onValues(WifiStatus status, IWifiStaIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 25:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status6 = removeStaIface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status6.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED /* 26 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    createRttController(IWifiIface.asInterface(_hidl_request.readStrongBinder()), new IWifiChip.createRttControllerCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass20 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.createRttControllerCallback
                        public void onValues(WifiStatus status, IWifiRttController rtt) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(rtt == null ? null : rtt.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 27:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getDebugRingBuffersStatus(new IWifiChip.getDebugRingBuffersStatusCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass21 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getDebugRingBuffersStatusCallback
                        public void onValues(WifiStatus status, ArrayList<WifiDebugRingBufferStatus> ringBuffers) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            WifiDebugRingBufferStatus.writeVectorToParcel(_hidl_reply, ringBuffers);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 28:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status7 = startLoggingToDebugRingBuffer(_hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status7.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 29:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status8 = forceDumpToDebugRingBuffer(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status8.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 30:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status9 = stopLoggingToDebugRingBuffer();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status9.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 31:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    getDebugHostWakeReasonStats(new IWifiChip.getDebugHostWakeReasonStatsCallback() {
                        /* class android.hardware.wifi.V1_2.IWifiChip.Stub.AnonymousClass22 */

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getDebugHostWakeReasonStatsCallback
                        public void onValues(WifiStatus status, WifiDebugHostWakeReasonStats stats) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            stats.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 32:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status10 = enableDebugErrorAlerts(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status10.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 33:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_1.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status11 = selectTxPowerScenario(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status11.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 34:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_1.IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status12 = resetTxPowerScenario();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status12.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 35:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status13 = selectTxPowerScenario_1_2(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status13.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 36:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiChip.kInterfaceName);
                    WifiStatus _hidl_out_status14 = registerEventCallback_1_2(IWifiChipEventCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status14.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
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
