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
import vendor.huawei.hardware.fusd.V1_0.IFusdLbsCallback;
import vendor.huawei.hardware.fusd.V1_0.NlpLocation;
import vendor.huawei.hardware.fusd.V1_0.WlanApLocationInfo;
import vendor.huawei.hardware.fusd.V1_1.ICellFenceInterface;
import vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface;
import vendor.huawei.hardware.fusd.V1_1.IGnssBatchingInterface;
import vendor.huawei.hardware.fusd.V1_2.IFusdGeofenceInterface;
import vendor.huawei.hardware.fusd.V1_2.IFusdGnssInterface;
import vendor.huawei.hardware.fusd.V1_2.IMMInterface;
import vendor.huawei.hardware.fusd.V1_2.IOfflineDbInterface;
import vendor.huawei.hardware.fusd.V1_2.IWififenceInterface;
import vendor.huawei.hardware.fusd.V1_3.ICellBatchingInterface;
import vendor.huawei.hardware.fusd.V1_3.IDiagnosticInterface;
import vendor.huawei.hardware.fusd.V1_4.IFlpLocationBatchingInterface;
import vendor.huawei.hardware.fusd.V1_4.IGnssEEData;
import vendor.huawei.hardware.fusd.V1_4.IInterferenceWeakInterface;
import vendor.huawei.hardware.fusd.V1_4.IPDRInterface;
import vendor.huawei.hardware.fusd.V1_4.IRemoteVehicleInterface;
import vendor.huawei.hardware.fusd.V1_4.ISDMInterface;

public interface IFusdLbs extends vendor.huawei.hardware.fusd.V1_6.IFusdLbs {
    public static final String kInterfaceName = "vendor.huawei.hardware.fusd@1.7::IFusdLbs";

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    IFusdGeofenceInterface getFusdGeofenceInterfaceV1_7() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IFusdLbs asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IFusdLbs)) {
            return (IFusdLbs) iface;
        }
        IFusdLbs proxy = new Proxy(binder);
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

    static IFusdLbs castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IFusdLbs getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IFusdLbs getService(boolean retry) throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT, retry);
    }

    static IFusdLbs getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IFusdLbs getService() throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT);
    }

    public static final class Proxy implements IFusdLbs {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.fusd@1.7::IFusdLbs]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void registerLbsServiceCallback(IFusdLbsCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public boolean sendMapMatchingResult(long timetag, double heading, double offsetLong, double offsetLat, int rerouted) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt64(timetag);
            _hidl_request.writeDouble(heading);
            _hidl_request.writeDouble(offsetLong);
            _hidl_request.writeDouble(offsetLat);
            _hidl_request.writeInt32(rerouted);
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

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendBatteryState(int batteryState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(batteryState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendScreenState(int screenState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(screenState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendNetworkRoamingState(int networkRoamingState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(networkRoamingState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendMapNavigatingState(int mapNavigatingState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(mapNavigatingState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendHigeoDynamicConfig(int wifiEnable, int mmEnable, int vdrEnable, int pdrEnable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(wifiEnable);
            _hidl_request.writeInt32(mmEnable);
            _hidl_request.writeInt32(vdrEnable);
            _hidl_request.writeInt32(pdrEnable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendHereWifiLocation(NlpLocation location) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            location.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendQuickTtffLocation(int nlpSource, NlpLocation location) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(nlpSource);
            location.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbs
        public void sendWlanApLocationInfo(ArrayList<WlanApLocationInfo> wlanLocInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
            WlanApLocationInfo.writeVectorToParcel(_hidl_request, wlanLocInfo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbs
        public void registerLbsServiceCallbackV1_1(vendor.huawei.hardware.fusd.V1_1.IFusdLbsCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbs
        public ICellFenceInterface getCellFenceInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return ICellFenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbs
        public IGeofenceInterface getGeofenceInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IGeofenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbs
        public IGnssBatchingInterface getGnssBatchingInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IGnssBatchingInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbs
        public void sendHwNLPLocation(ArrayList<GnssLocation> locations) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
            GnssLocation.writeVectorToParcel(_hidl_request, locations);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbs
        public void sendHwNLPStatus(ArrayList<Integer> status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32Vector(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbs
        public void cleanUpHifence(int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public void injectLocation(int source, GnssLocation location) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(source);
            location.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public void sendLocationSourceStatus(int type, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public IOfflineDbInterface getOfflineDbInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IOfflineDbInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public IMMInterface getMMInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IMMInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public IWififenceInterface getWififenceInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IWififenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public IFusdGeofenceInterface getFusdGeofenceInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IFusdGeofenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public IFusdGnssInterface getFusdGnssInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IFusdGnssInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IFusdLbs
        public vendor.huawei.hardware.fusd.V1_2.ICellFenceInterface getCellFenceInterfaceV1_2() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return vendor.huawei.hardware.fusd.V1_2.ICellFenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_3.IFusdLbs
        public ICellBatchingInterface getCellBatchingInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_3.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return ICellBatchingInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_3.IFusdLbs
        public IDiagnosticInterface getDiagnosticInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_3.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IDiagnosticInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IFusdLbs
        public IFlpLocationBatchingInterface getFlpLocationBatchingInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IFlpLocationBatchingInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IFusdLbs
        public vendor.huawei.hardware.fusd.V1_4.IMMInterface getMMInterfaceV1_4() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return vendor.huawei.hardware.fusd.V1_4.IMMInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IFusdLbs
        public ISDMInterface getSDMInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return ISDMInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IFusdLbs
        public IInterferenceWeakInterface getInterferenceWeakInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IInterferenceWeakInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IFusdLbs
        public IRemoteVehicleInterface getRemoteVehicleInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IRemoteVehicleInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IFusdLbs
        public IPDRInterface getPDRInterface() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IPDRInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IFusdLbs
        public IGnssEEData getExtensionEEData() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IGnssEEData.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_5.IFusdLbs
        public void sendHDGnssMode(int hdGnssModeState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(hdGnssModeState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_5.IFusdLbs
        public void sendMccCode(int countryCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(countryCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_5.IFusdLbs
        public void sendCityCode(int cityCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(cityCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_5.IFusdLbs
        public void sendHigeoExtraInfo(int type, ArrayList<Integer> extraData) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(extraData);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs
        public vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceInterface getFusdGeofenceInterfaceV1_6() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_6.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs
        public vendor.huawei.hardware.fusd.V1_6.IWififenceInterface getWififenceInterfaceV1_6() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_6.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return vendor.huawei.hardware.fusd.V1_6.IWififenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_6.IFusdLbs
        public vendor.huawei.hardware.fusd.V1_6.ICellFenceInterface getCellFenceInterfaceV1_6() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.fusd.V1_6.IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return vendor.huawei.hardware.fusd.V1_6.ICellFenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs
        public IFusdGeofenceInterface getFusdGeofenceInterfaceV1_7() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IFusdLbs.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return IFusdGeofenceInterface.asInterface(_hidl_reply.readStrongBinder());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IFusdLbs {
        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IFusdLbs.kInterfaceName, vendor.huawei.hardware.fusd.V1_6.IFusdLbs.kInterfaceName, vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName, vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName, vendor.huawei.hardware.fusd.V1_3.IFusdLbs.kInterfaceName, vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName, vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName, vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IFusdLbs.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-46, 39, 125, -34, -16, -12, 32, -13, 93, -88, 82, Byte.MAX_VALUE, -65, -116, -122, -104, 125, 79, 122, -29, -14, 21, -79, -37, -9, -56, -57, -113, 4, 85, -49, -59}, new byte[]{-65, -76, 54, -105, 27, -2, 62, 109, -85, -94, 111, 43, 99, 86, -114, 121, -57, -79, -10, 58, -126, -124, -53, -115, Byte.MIN_VALUE, 98, -93, -45, 104, -108, 103, 83}, new byte[]{-100, 87, 49, -107, 46, -82, 83, 39, 7, 18, 33, 48, -9, -126, 118, 91, -50, 94, -90, -16, 3, 93, -50, 110, 38, -54, 50, -77, 52, HwUibcReceiver.CurrentPacket.INPUT_MASK, HwUibcReceiver.CurrentPacket.INPUT_MASK, -31}, new byte[]{124, -26, 60, -109, -123, -95, 54, 86, 31, 25, -44, 24, -28, 26, 30, -46, 85, 49, -124, -105, -24, -63, -44, 25, 78, -73, 24, -112, -109, -24, 112, -21}, new byte[]{126, 66, 43, -103, -4, -64, -32, 30, 48, -96, -72, 18, -89, 103, Byte.MAX_VALUE, -115, 43, -77, 20, 52, 125, 38, -24, 96, -46, 48, 30, 50, -91, -127, 124, 123}, new byte[]{-36, 111, -69, 12, -34, -2, 125, -36, 124, -68, 17, -86, 112, 0, 106, Byte.MAX_VALUE, 53, 88, -29, -8, 101, Byte.MIN_VALUE, 13, -115, -86, 108, -84, 28, 43, 77, -69, -121}, new byte[]{-22, -98, 23, 96, -29, 84, 30, 82, -41, 55, 75, -95, -8, -20, -111, 70, -1, 30, 101, -30, 31, -67, -123, 84, -10, -120, 14, 19, -54, -104, -116, -43}, new byte[]{45, 105, 52, -55, -40, 102, -70, -40, 36, 66, -101, 94, -127, 32, -6, -103, 105, -50, -62, -23, 73, -15, 83, -22, 20, 116, 61, -18, -37, 105, 81, -48}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.fusd.V1_7.IFusdLbs, vendor.huawei.hardware.fusd.V1_6.IFusdLbs, vendor.huawei.hardware.fusd.V1_5.IFusdLbs, vendor.huawei.hardware.fusd.V1_4.IFusdLbs, vendor.huawei.hardware.fusd.V1_3.IFusdLbs, vendor.huawei.hardware.fusd.V1_2.IFusdLbs, vendor.huawei.hardware.fusd.V1_1.IFusdLbs, vendor.huawei.hardware.fusd.V1_0.IFusdLbs, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IFusdLbs.kInterfaceName.equals(descriptor)) {
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
            IHwBinder iHwBinder = null;
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    registerLbsServiceCallback(IFusdLbsCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    boolean _hidl_out_result = sendMapMatchingResult(_hidl_request.readInt64(), _hidl_request.readDouble(), _hidl_request.readDouble(), _hidl_request.readDouble(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_result);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    sendBatteryState(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    sendScreenState(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    sendNetworkRoamingState(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    sendMapNavigatingState(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    sendHigeoDynamicConfig(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    NlpLocation location = new NlpLocation();
                    location.readFromParcel(_hidl_request);
                    sendHereWifiLocation(location);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    int nlpSource = _hidl_request.readInt32();
                    NlpLocation location2 = new NlpLocation();
                    location2.readFromParcel(_hidl_request);
                    sendQuickTtffLocation(nlpSource, location2);
                    _hidl_reply.writeStatus(0);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_0.IFusdLbs.kInterfaceName);
                    sendWlanApLocationInfo(WlanApLocationInfo.readVectorFromParcel(_hidl_request));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
                    registerLbsServiceCallbackV1_1(vendor.huawei.hardware.fusd.V1_1.IFusdLbsCallback.asInterface(_hidl_request.readStrongBinder()));
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
                    ICellFenceInterface _hidl_out_cfIface = getCellFenceInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cfIface != null) {
                        iHwBinder = _hidl_out_cfIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
                    IGeofenceInterface _hidl_out_gfIface = getGeofenceInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_gfIface != null) {
                        iHwBinder = _hidl_out_gfIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
                    IGnssBatchingInterface _hidl_out_gbIface = getGnssBatchingInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_gbIface != null) {
                        iHwBinder = _hidl_out_gbIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
                    sendHwNLPLocation(GnssLocation.readVectorFromParcel(_hidl_request));
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
                    sendHwNLPStatus(_hidl_request.readInt32Vector());
                    return;
                case 17:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_1.IFusdLbs.kInterfaceName);
                    cleanUpHifence(_hidl_request.readInt32());
                    return;
                case 18:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    int source = _hidl_request.readInt32();
                    GnssLocation location3 = new GnssLocation();
                    location3.readFromParcel(_hidl_request);
                    injectLocation(source, location3);
                    return;
                case 19:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    sendLocationSourceStatus(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    IOfflineDbInterface _hidl_out_Iface = getOfflineDbInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_Iface != null) {
                        iHwBinder = _hidl_out_Iface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 21:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    IMMInterface _hidl_out_mmIface = getMMInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_mmIface != null) {
                        iHwBinder = _hidl_out_mmIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    IWififenceInterface _hidl_out_wfIface = getWififenceInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_wfIface != null) {
                        iHwBinder = _hidl_out_wfIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 23:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    IFusdGeofenceInterface _hidl_out_gfIface2 = getFusdGeofenceInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_gfIface2 != null) {
                        iHwBinder = _hidl_out_gfIface2.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 24:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    IFusdGnssInterface _hidl_out_fusdGnss = getFusdGnssInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_fusdGnss != null) {
                        iHwBinder = _hidl_out_fusdGnss.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 25:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_2.IFusdLbs.kInterfaceName);
                    vendor.huawei.hardware.fusd.V1_2.ICellFenceInterface _hidl_out_cfIface2 = getCellFenceInterfaceV1_2();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cfIface2 != null) {
                        iHwBinder = _hidl_out_cfIface2.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 26:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_3.IFusdLbs.kInterfaceName);
                    ICellBatchingInterface _hidl_out_cbIface = getCellBatchingInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cbIface != null) {
                        iHwBinder = _hidl_out_cbIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 27:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_3.IFusdLbs.kInterfaceName);
                    IDiagnosticInterface _hidl_out_diagIface = getDiagnosticInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_diagIface != null) {
                        iHwBinder = _hidl_out_diagIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 28:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
                    IFlpLocationBatchingInterface _hidl_out_cbIface2 = getFlpLocationBatchingInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cbIface2 != null) {
                        iHwBinder = _hidl_out_cbIface2.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 29:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
                    vendor.huawei.hardware.fusd.V1_4.IMMInterface _hidl_out_cbIface3 = getMMInterfaceV1_4();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cbIface3 != null) {
                        iHwBinder = _hidl_out_cbIface3.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
                    ISDMInterface _hidl_out_cbIface4 = getSDMInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cbIface4 != null) {
                        iHwBinder = _hidl_out_cbIface4.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
                    IInterferenceWeakInterface _hidl_out_interWeakIface = getInterferenceWeakInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_interWeakIface != null) {
                        iHwBinder = _hidl_out_interWeakIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
                    IRemoteVehicleInterface _hidl_out_rmtIface = getRemoteVehicleInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_rmtIface != null) {
                        iHwBinder = _hidl_out_rmtIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 33:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
                    IPDRInterface _hidl_out_cbIface5 = getPDRInterface();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cbIface5 != null) {
                        iHwBinder = _hidl_out_cbIface5.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 34:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_4.IFusdLbs.kInterfaceName);
                    IGnssEEData _hidl_out_eeDataIface = getExtensionEEData();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_eeDataIface != null) {
                        iHwBinder = _hidl_out_eeDataIface.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 35:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
                    sendHDGnssMode(_hidl_request.readInt32());
                    return;
                case 36:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
                    sendMccCode(_hidl_request.readInt32());
                    return;
                case 37:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
                    sendCityCode(_hidl_request.readInt32());
                    return;
                case 38:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_5.IFusdLbs.kInterfaceName);
                    sendHigeoExtraInfo(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 39:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_6.IFusdLbs.kInterfaceName);
                    vendor.huawei.hardware.fusd.V1_6.IFusdGeofenceInterface _hidl_out_gfIface3 = getFusdGeofenceInterfaceV1_6();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_gfIface3 != null) {
                        iHwBinder = _hidl_out_gfIface3.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 40:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_6.IFusdLbs.kInterfaceName);
                    vendor.huawei.hardware.fusd.V1_6.IWififenceInterface _hidl_out_wfIface2 = getWififenceInterfaceV1_6();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_wfIface2 != null) {
                        iHwBinder = _hidl_out_wfIface2.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 41:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.fusd.V1_6.IFusdLbs.kInterfaceName);
                    vendor.huawei.hardware.fusd.V1_6.ICellFenceInterface _hidl_out_cfIface3 = getCellFenceInterfaceV1_6();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_cfIface3 != null) {
                        iHwBinder = _hidl_out_cfIface3.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
                    _hidl_reply.send();
                    return;
                case 42:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IFusdLbs.kInterfaceName);
                    IFusdGeofenceInterface _hidl_out_gfIface4 = getFusdGeofenceInterfaceV1_7();
                    _hidl_reply.writeStatus(0);
                    if (_hidl_out_gfIface4 != null) {
                        iHwBinder = _hidl_out_gfIface4.asBinder();
                    }
                    _hidl_reply.writeStrongBinder(iHwBinder);
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
