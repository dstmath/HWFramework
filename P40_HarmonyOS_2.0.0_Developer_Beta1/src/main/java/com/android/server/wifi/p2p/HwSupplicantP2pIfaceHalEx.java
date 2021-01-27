package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.HidlSupport;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.p2p.SupplicantP2pIfaceHal;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIfaceCallback;

public class HwSupplicantP2pIfaceHalEx implements IHwSupplicantP2pIfaceHalEx {
    private static final int INVALID_LINK_SPEED = -1;
    private static final int NOTIFY_TYPE_P2P_DISCONNECT_ERROR = 5;
    private static final int NOTIFY_TYPE_P2P_RECEIVE_INVITATION = 6;
    private static final int P2P_PERSISTENT_NETWORK_REMOVED_UNEXPECTEDLY_TYPE = 1;
    private static final String TAG = "HwSupplicantP2pIfaceHalEx";
    private IHwSupplicantP2pIfaceHalInner mHwSupplicantP2pIfaceHalInner = null;
    private final WifiP2pMonitor mMonitor;

    private HwSupplicantP2pIfaceHalEx(SupplicantP2pIfaceHal supplicantP2pIfaceHal, WifiP2pMonitor monitor) {
        this.mHwSupplicantP2pIfaceHalInner = supplicantP2pIfaceHal;
        this.mMonitor = monitor;
    }

    public static HwSupplicantP2pIfaceHalEx createHwSupplicantP2pIfaceHalEx(SupplicantP2pIfaceHal supplicantP2pIfaceHal, WifiP2pMonitor monitor) {
        return new HwSupplicantP2pIfaceHalEx(supplicantP2pIfaceHal, monitor);
    }

    private class VendorSupplicantP2pIfaceHalCallbackV3_0 extends ISupplicantP2pIfaceCallback.Stub {
        private static final int IFACE_NAME_TOKEN_INDEX = 2;
        private static final int IFACE_TOKEN_LEN_MIN = 3;
        private static final String TAG = "SupplicantP2pIfaceCallback";
        private SupplicantP2pIfaceCallback mCallback;
        private final String mInterface;
        private final WifiP2pMonitor mMonitor;

        VendorSupplicantP2pIfaceHalCallbackV3_0(String iface, WifiP2pMonitor monitor, SupplicantP2pIfaceCallback callback) {
            this.mInterface = iface;
            this.mMonitor = monitor;
            this.mCallback = callback;
        }

        public void onNetworkAdded(int networkId) {
            this.mCallback.onNetworkAdded(networkId);
        }

        public void onNetworkRemoved(int networkId) {
            this.mCallback.onNetworkRemoved(networkId);
        }

        public void onDeviceFound(byte[] srcAddress, byte[] p2pDeviceAddress, byte[] primaryDeviceType, String deviceName, short configMethods, byte deviceCapabilities, int groupCapabilities, byte[] wfdDeviceInfo) {
            this.mCallback.onDeviceFound(srcAddress, p2pDeviceAddress, primaryDeviceType, deviceName, configMethods, deviceCapabilities, groupCapabilities, wfdDeviceInfo);
        }

        public void onDeviceLost(byte[] p2pDeviceAddress) {
            this.mCallback.onDeviceLost(p2pDeviceAddress);
        }

        public void onFindStopped() {
            this.mCallback.onFindStopped();
        }

        public void onGoNegotiationRequest(byte[] srcAddress, short passwordId) {
            this.mCallback.onGoNegotiationRequest(srcAddress, passwordId);
        }

        public void onGoNegotiationCompleted(int status) {
            this.mCallback.onGoNegotiationCompleted(status);
        }

        public void onGroupFormationSuccess() {
            this.mCallback.onGroupFormationSuccess();
        }

        public void onGroupFormationFailure(String failureReason) {
            this.mCallback.onGroupFormationFailure(failureReason);
        }

        public void onGroupStarted(String groupIfName, boolean isGo, ArrayList<Byte> ssid, int frequency, byte[] psk, String passphrase, byte[] goDeviceAddress, boolean isPersistent) {
            this.mCallback.onGroupStarted(groupIfName, isGo, ssid, frequency, psk, passphrase, goDeviceAddress, isPersistent);
        }

        public void onGroupRemoved(String groupIfName, boolean isGo) {
            this.mCallback.onGroupRemoved(groupIfName, isGo);
        }

        public void onInvitationReceived(byte[] srcAddress, byte[] goDeviceAddress, byte[] bssid, int persistentNetworkId, int operatingFrequency) {
            this.mCallback.onInvitationReceived(srcAddress, goDeviceAddress, bssid, persistentNetworkId, operatingFrequency);
        }

        public void onInvitationResult(byte[] bssid, int status) {
            this.mCallback.onInvitationResult(bssid, status);
        }

        public void onProvisionDiscoveryCompleted(byte[] p2pDeviceAddress, boolean isRequest, byte status, short configMethods, String generatedPin) {
            this.mCallback.onProvisionDiscoveryCompleted(p2pDeviceAddress, isRequest, status, configMethods, generatedPin);
        }

        public void onServiceDiscoveryResponse(byte[] srcAddress, short updateIndicator, ArrayList<Byte> tlvs) {
            this.mCallback.onServiceDiscoveryResponse(srcAddress, updateIndicator, tlvs);
        }

        public void onStaAuthorized(byte[] srcAddress, byte[] p2pDeviceAddress) {
            if (WifiCommonUtils.IS_TV) {
                broadcastP2pInterfaceAddr(srcAddress, p2pDeviceAddress);
            }
            this.mCallback.onStaAuthorized(srcAddress, p2pDeviceAddress);
        }

        public void onStaDeauthorized(byte[] srcAddress, byte[] p2pDeviceAddress) {
            this.mCallback.onStaDeauthorized(srcAddress, p2pDeviceAddress);
        }

        public void onP2pInterfaceCreated(String dataString) {
            HwHiLog.d(TAG, false, "onP2pInterfaceCreated %{public}s", new Object[]{dataString});
            if (dataString != null) {
                String[] tokens = dataString.split(" ");
                if (tokens.length >= 3) {
                    if (tokens[1].startsWith("GO")) {
                        this.mMonitor.broadcastP2pGoInterfaceCreated(this.mInterface, tokens[2]);
                    } else {
                        this.mMonitor.broadcastP2pGcInterfaceCreated(this.mInterface, tokens[2]);
                    }
                }
            }
        }

        public void notifyP2pIfaceEvent(int notifyType, String stringData) {
            HwHiLog.d(TAG, false, "notifyP2pIfaceEvent %{public}d", new Object[]{Integer.valueOf(notifyType)});
            if (stringData == null) {
                HwHiLog.e(TAG, false, "notifyP2pIfaceEvent stringData is null", new Object[0]);
            } else if (notifyType == 1) {
                try {
                    this.mMonitor.broadcastP2pPersistentNetworkRemovedUnexpectedly(this.mInterface, Integer.parseInt(stringData));
                } catch (NumberFormatException e) {
                    HwHiLog.i(TAG, false, "notifyP2pIfaceEvent invalid networkId", new Object[0]);
                }
            } else if (notifyType == 5) {
                this.mMonitor.broadcastP2pDisconnectErrCode(this.mInterface, stringData);
            } else if (notifyType != 6) {
                HwHiLog.i(TAG, false, "notifyP2pIfaceEvent, unknown notifyType", new Object[0]);
            } else {
                this.mMonitor.broadcastP2pRecvInvitation(this.mInterface, stringData);
            }
        }

        public void onHwDeviceFound(byte[] srcAddress, byte[] p2pDeviceAddress, byte[] primaryDeviceType, String deviceName, short configMethods, byte deviceCapabilities, int groupCapabilities, byte[] wfdDeviceInfo) {
            this.mCallback.getHwSupplicantP2pIfaceCallbackExt().onHwDeviceFound(srcAddress, p2pDeviceAddress, primaryDeviceType, deviceName, configMethods, deviceCapabilities, groupCapabilities, wfdDeviceInfo);
            this.mCallback.onDeviceFound(srcAddress, p2pDeviceAddress, primaryDeviceType, deviceName, configMethods, deviceCapabilities, groupCapabilities, wfdDeviceInfo);
            HwHiLog.d(TAG, false, "HwDevice discovered", new Object[0]);
        }

        public void onGroupRemoveAndReform(String iface) {
            this.mMonitor.broadcastP2pGroupRemoveAndReform(this.mInterface);
        }

        private void broadcastP2pInterfaceAddr(byte[] srcAddress, byte[] p2pDeviceAddress) {
            HwHiLog.i(TAG, false, "broadcastP2pInterfaceAddr on " + this.mInterface, new Object[0]);
            WifiP2pDevice device = createP2pInterfaceAddrForDevice(srcAddress, p2pDeviceAddress);
            if (device == null) {
                HwHiLog.i(TAG, false, "device is null, ignore broadcastP2pInterfaceAddr on " + this.mInterface, new Object[0]);
                return;
            }
            this.mMonitor.broadcastP2pInterfaceAddr(this.mInterface, device);
        }

        private WifiP2pDevice createP2pInterfaceAddrForDevice(byte[] srcAddress, byte[] p2pDeviceAddress) {
            WifiP2pDevice device = new WifiP2pDevice();
            if (!Arrays.equals(NativeUtil.ANY_MAC_BYTES, p2pDeviceAddress)) {
                try {
                    device.deviceAddress = NativeUtil.macAddressFromByteArray(p2pDeviceAddress);
                    device.deviceName = NativeUtil.macAddressFromByteArray(srcAddress);
                    return device;
                } catch (IllegalArgumentException e) {
                    HwHiLog.e(TAG, false, "Could not decode MAC address", new Object[0]);
                    return null;
                }
            } else {
                HwHiLog.i(TAG, false, "No p2p device address, ignore", new Object[0]);
                return null;
            }
        }
    }

    public boolean trySetupForVendorV3_0(ISupplicantIface ifaceHwBinder, String ifaceName) {
        WifiP2pMonitor wifiP2pMonitor;
        ISupplicantP2pIface supplicantP2pIface = vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface.castFrom(ifaceHwBinder);
        if (supplicantP2pIface == null) {
            return false;
        }
        HwHiLog.i(TAG, false, "Start to setup vendor ISupplicantP2pIface", new Object[0]);
        this.mHwSupplicantP2pIfaceHalInner.setSupplicantP2pIface(supplicantP2pIface);
        if (!this.mHwSupplicantP2pIfaceHalInner.hwLinkToSupplicantP2pIfaceDeath()) {
            return false;
        }
        if (this.mHwSupplicantP2pIfaceHalInner.getSupplicantP2pIface() == null || (wifiP2pMonitor = this.mMonitor) == null || hwP2pRegisterCallback(new VendorSupplicantP2pIfaceHalCallbackV3_0(ifaceName, wifiP2pMonitor, new SupplicantP2pIfaceCallback(ifaceName, wifiP2pMonitor)))) {
            HwHiLog.i(TAG, false, "Successfully setup vendor ISupplicantP2pIface", new Object[0]);
            return true;
        }
        HwHiLog.e(TAG, false, "Vendor callback registration failed. Initialization incomplete.", new Object[0]);
        return false;
    }

    private vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface checkVendorSupplicantP2pIfaceAndLogFailure(String method) {
        if (this.mHwSupplicantP2pIfaceHalInner.getSupplicantP2pIface() == null) {
            HwHiLog.e(TAG, false, "Can't call %{public}s: ISupplicantP2pIface is null", new Object[]{method});
            return null;
        }
        vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface vendorP2pIface = vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface.castFrom(this.mHwSupplicantP2pIfaceHalInner.getSupplicantP2pIface());
        if (vendorP2pIface != null) {
            return vendorP2pIface;
        }
        HwHiLog.e(TAG, false, "Can't call %{public}s: fail to cast ISupplicantP2pIface to vendor 2.0", new Object[]{method});
        return null;
    }

    private boolean hwP2pRegisterCallback(ISupplicantP2pIfaceCallback receiver) {
        synchronized (this.mHwSupplicantP2pIfaceHalInner.getLock()) {
            vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            SupplicantP2pIfaceHal.SupplicantResult<Void> result = new SupplicantP2pIfaceHal.SupplicantResult<>("hwP2pRegisterCallback()");
            try {
                result.setResult(vendorP2pIface.hwP2pRegisterCallback(receiver));
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "hwP2pRegisterCallback exception happens", new Object[0]);
                this.mHwSupplicantP2pIfaceHalInner.hwSupplicantServiceDiedHandler();
            }
            return result.isSuccess();
        }
    }

    public boolean groupAddWithFreq(int networkId, boolean isPersistent, String freq) {
        synchronized (this.mHwSupplicantP2pIfaceHalInner.getLock()) {
            vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            if (freq == null) {
                HwHiLog.e(TAG, false, "freq try to create is null", new Object[0]);
                return false;
            }
            SupplicantP2pIfaceHal.SupplicantResult<Void> result = new SupplicantP2pIfaceHal.SupplicantResult<>("groupAddWithFreq(" + networkId + ", " + isPersistent + ", " + freq + ")");
            try {
                result.setResult(vendorP2pIface.addGroupWithFreq(isPersistent, networkId, freq));
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "groupAddWithFreq exception happens", new Object[0]);
                this.mHwSupplicantP2pIfaceHalInner.hwSupplicantServiceDiedHandler();
            }
            return result.isSuccess();
        }
    }

    public boolean magiclinkConnect(String cmd) {
        synchronized (this.mHwSupplicantP2pIfaceHalInner.getLock()) {
            vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            if (cmd == null) {
                HwHiLog.e(TAG, false, "cmd try to connect is null", new Object[0]);
                return false;
            }
            SupplicantP2pIfaceHal.SupplicantResult<Void> result = new SupplicantP2pIfaceHal.SupplicantResult<>("magiclinkConnect([secrecy parameters])");
            try {
                result.setResult(vendorP2pIface.magiclinkConnect(cmd));
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "magiclinkConnect exception happens", new Object[0]);
                this.mHwSupplicantP2pIfaceHalInner.hwSupplicantServiceDiedHandler();
            }
            return result.isSuccess();
        }
    }

    public boolean addP2pRptGroup(String config) {
        synchronized (this.mHwSupplicantP2pIfaceHalInner.getLock()) {
            vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            SupplicantP2pIfaceHal.SupplicantResult<Void> result = new SupplicantP2pIfaceHal.SupplicantResult<>("rptP2pAddGroup()");
            try {
                result.setResult(vendorP2pIface.rptP2pAddGroup(config));
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "addP2pRptGroup exception happens", new Object[0]);
                this.mHwSupplicantP2pIfaceHalInner.hwSupplicantServiceDiedHandler();
            }
            return result.isSuccess();
        }
    }

    public int getP2pLinkspeed(String ifaceName) {
        synchronized (this.mHwSupplicantP2pIfaceHalInner.getLock()) {
            if (TextUtils.isEmpty(ifaceName)) {
                return -1;
            }
            vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return this.mHwSupplicantP2pIfaceHalInner.getValueOfResultNotValid();
            }
            SupplicantP2pIfaceHal.SupplicantResult<Integer> result = new SupplicantP2pIfaceHal.SupplicantResult<>("getP2pLinkspeed()");
            try {
                vendorP2pIface.getP2pLinkspeed(ifaceName, new ISupplicantP2pIface.getP2pLinkspeedCallback(result) {
                    /* class com.android.server.wifi.p2p.$$Lambda$HwSupplicantP2pIfaceHalEx$uw8Wdapjyq7mxgl_6TJvDJSi6Q */
                    private final /* synthetic */ SupplicantP2pIfaceHal.SupplicantResult f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        this.f$0.setResult(supplicantStatus, Integer.valueOf(i));
                    }
                });
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "getP2pLinkspeed exception happens", new Object[0]);
                this.mHwSupplicantP2pIfaceHalInner.hwSupplicantServiceDiedHandler();
            }
            if (!result.isSuccess()) {
                return this.mHwSupplicantP2pIfaceHalInner.getValueOfResultNotValid();
            }
            return ((Integer) result.getResult()).intValue();
        }
    }

    public String deliverP2pData(int cmdType, int dataType, String carryData) {
        HwHiLog.d(TAG, false, "deliverP2pData: cmdType=%{public}d, dataType=%{public}d, carryData=%{private}s", new Object[]{Integer.valueOf(cmdType), Integer.valueOf(dataType), carryData});
        synchronized (this.mHwSupplicantP2pIfaceHalInner.getLock()) {
            vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return "";
            }
            HidlSupport.Mutable<String> deliverP2pData = new HidlSupport.Mutable<>();
            try {
                vendorP2pIface.deliverP2pData(cmdType, dataType, carryData, new ISupplicantP2pIface.deliverP2pDataCallback(deliverP2pData) {
                    /* class com.android.server.wifi.p2p.$$Lambda$HwSupplicantP2pIfaceHalEx$EsUmDv8Tlmli0MOimFeyoGoXcZg */
                    private final /* synthetic */ HidlSupport.Mutable f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        this.f$0.value = str;
                    }
                });
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "deliverP2pData exception happens", new Object[0]);
                this.mHwSupplicantP2pIfaceHalInner.hwSupplicantServiceDiedHandler();
            }
            return (String) deliverP2pData.value;
        }
    }
}
