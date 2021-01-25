package com.android.server.connectivity.usbp2p;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.net.InetAddresses;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.ip.IpServer;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.connectivity.tethering.TetheringConfiguration;

public class UsbP2pCommands {
    private static final String TAG = "UsbP2pCommands";
    private static final String USB_NEAR_IFACE_ADDR = IpServer.getUsbNearIfaceAddr();
    private static final int USB_PREFIX_LENGTH = 24;
    private final TetheringConfiguration mConfig;
    private final Context mContext;
    private final INetworkManagementService mNMS;

    public UsbP2pCommands(INetworkManagementService nms, Context context, TetheringConfiguration config) {
        this.mNMS = nms;
        this.mContext = context;
        this.mContext.getResources();
        this.mConfig = config;
    }

    public void setUsbFunction(boolean isEnabled) {
        UsbManager usbManager = (UsbManager) this.mContext.getSystemService("usb");
        if (isEnabled) {
            usbManager.setCurrentFunctions(32);
        } else {
            usbManager.setCurrentFunctions(-33 & usbManager.getCurrentFunctions());
        }
    }

    public boolean isUsbRndisEnabled() {
        return (((UsbManager) this.mContext.getSystemService("usb")).getCurrentFunctions() & 32) == 32;
    }

    public boolean startTethering() {
        try {
            this.mNMS.startTethering(this.mConfig.legacyDhcpRanges);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            try {
                loge("Start dnsmasq failed, catch exception first.");
                this.mNMS.stopTethering();
                this.mNMS.startTethering(this.mConfig.legacyDhcpRanges);
                return true;
            } catch (RemoteException | IllegalStateException e2) {
                loge("Start dnsmasq failed, catch exception twice.");
                return false;
            }
        }
    }

    public boolean stopTethering() {
        try {
            this.mNMS.stopTethering();
            return true;
        } catch (RemoteException | IllegalStateException e) {
            try {
                loge("Stop dnsmasq failed, catch exception first.");
                this.mNMS.stopTethering();
                return true;
            } catch (RemoteException | IllegalStateException e2) {
                loge("Stop dnsmasq failed, catch excetion twice.");
                return false;
            }
        }
    }

    public boolean tetherInterface(String ifaceName) {
        try {
            this.mNMS.tetherInterface(ifaceName);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            loge("Error tetherInterface" + ifaceName);
            return false;
        }
    }

    public boolean untetherInterface(String ifaceName) {
        try {
            this.mNMS.untetherInterface(ifaceName);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            loge("Error untetherInterface " + ifaceName);
            return false;
        }
    }

    public boolean isTetheringStarted() {
        try {
            return this.mNMS.isTetheringStarted();
        } catch (RemoteException | IllegalStateException e) {
            loge("isTetheringStarted catch IllegalStateException.");
            return false;
        }
    }

    public boolean configureIpv4(boolean isEnabled, String ifaceName) {
        log("configureIpv4(" + isEnabled + ")");
        try {
            InterfaceConfiguration ifcg = this.mNMS.getInterfaceConfig(ifaceName);
            ifcg.setLinkAddress(new LinkAddress(InetAddresses.parseNumericAddress(USB_NEAR_IFACE_ADDR), 24));
            if (isEnabled) {
                ifcg.setInterfaceUp();
            } else {
                ifcg.setInterfaceDown();
            }
            this.mNMS.setInterfaceConfig(ifaceName, ifcg);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            loge("configureIpv4 catch exception.");
            return false;
        }
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }

    private void loge(String msg) {
        Log.e(TAG, msg);
    }
}
