package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.hardware.usb.V1_0.IUsb;
import android.hardware.usb.V1_0.IUsbCallback.Stub;
import android.hardware.usb.V1_0.PortRole;
import android.hardware.usb.V1_0.PortStatus;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.FgThread;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class UsbPortManager {
    private static final int COMBO_SINK_DEVICE = UsbPort.combineRolesAsBit(2, 2);
    private static final int COMBO_SINK_HOST = UsbPort.combineRolesAsBit(2, 1);
    private static final int COMBO_SOURCE_DEVICE = UsbPort.combineRolesAsBit(1, 2);
    private static final int COMBO_SOURCE_HOST = UsbPort.combineRolesAsBit(1, 1);
    private static final int MSG_UPDATE_PORTS = 1;
    private static final String PORT_INFO = "port_info";
    private static final String TAG = "UsbPortManager";
    private static final int USB_HAL_DEATH_COOKIE = 1000;
    private final Context mContext;
    private HALCallback mHALCallback = new HALCallback(null, this);
    private final Handler mHandler = new Handler(FgThread.get().getLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<RawPortInfo> PortInfo = msg.getData().getParcelableArrayList(UsbPortManager.PORT_INFO);
                    synchronized (UsbPortManager.this.mLock) {
                        UsbPortManager.this.updatePortsLocked(null, PortInfo);
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private final Object mLock = new Object();
    private final ArrayMap<String, PortInfo> mPorts = new ArrayMap();
    @GuardedBy("mLock")
    private IUsb mProxy = null;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private final ArrayMap<String, RawPortInfo> mSimulatedPorts = new ArrayMap();
    private boolean mSystemReady;

    final class DeathRecipient implements android.os.IHwBinder.DeathRecipient {
        public IndentingPrintWriter pw;

        DeathRecipient(IndentingPrintWriter pw) {
            this.pw = pw;
        }

        public void serviceDied(long cookie) {
            if (cookie == 1000) {
                UsbPortManager.logAndPrint(6, this.pw, "Usb hal service died cookie: " + cookie);
                synchronized (UsbPortManager.this.mLock) {
                    UsbPortManager.this.mProxy = null;
                }
            }
        }
    }

    private static class HALCallback extends Stub {
        public UsbPortManager portManager;
        public IndentingPrintWriter pw;

        HALCallback() {
        }

        HALCallback(IndentingPrintWriter pw, UsbPortManager portManager) {
            this.pw = pw;
            this.portManager = portManager;
        }

        public void notifyPortStatusChange(ArrayList<PortStatus> currentPortStatus, int retval) {
            if (!this.portManager.mSystemReady) {
                return;
            }
            if (retval != 0) {
                UsbPortManager.logAndPrint(6, this.pw, "port status enquiry failed");
                return;
            }
            ArrayList<RawPortInfo> newPortInfo = new ArrayList();
            for (PortStatus current : currentPortStatus) {
                newPortInfo.add(new RawPortInfo(current.portName, current.supportedModes, current.currentMode, current.canChangeMode, current.currentPowerRole, current.canChangePowerRole, current.currentDataRole, current.canChangeDataRole));
                UsbPortManager.logAndPrint(4, this.pw, "ClientCallback: " + current.portName);
            }
            Message message = this.portManager.mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(UsbPortManager.PORT_INFO, newPortInfo);
            message.what = 1;
            message.setData(bundle);
            this.portManager.mHandler.sendMessage(message);
        }

        public void notifyRoleSwitchStatus(String portName, PortRole role, int retval) {
            if (retval == 0) {
                UsbPortManager.logAndPrint(4, this.pw, portName + " role switch successful");
            } else {
                UsbPortManager.logAndPrint(6, this.pw, portName + " role switch failed");
            }
        }
    }

    private static final class PortInfo {
        public static final int DISPOSITION_ADDED = 0;
        public static final int DISPOSITION_CHANGED = 1;
        public static final int DISPOSITION_READY = 2;
        public static final int DISPOSITION_REMOVED = 3;
        public boolean mCanChangeDataRole;
        public boolean mCanChangeMode;
        public boolean mCanChangePowerRole;
        public int mDisposition;
        public final UsbPort mUsbPort;
        public UsbPortStatus mUsbPortStatus;

        public PortInfo(String portId, int supportedModes) {
            this.mUsbPort = new UsbPort(portId, supportedModes);
        }

        public boolean setStatus(int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole, int supportedRoleCombinations) {
            this.mCanChangeMode = canChangeMode;
            this.mCanChangePowerRole = canChangePowerRole;
            this.mCanChangeDataRole = canChangeDataRole;
            if (this.mUsbPortStatus != null && this.mUsbPortStatus.getCurrentMode() == currentMode && this.mUsbPortStatus.getCurrentPowerRole() == currentPowerRole && this.mUsbPortStatus.getCurrentDataRole() == currentDataRole && this.mUsbPortStatus.getSupportedRoleCombinations() == supportedRoleCombinations) {
                return false;
            }
            this.mUsbPortStatus = new UsbPortStatus(currentMode, currentPowerRole, currentDataRole, supportedRoleCombinations);
            return true;
        }

        public String toString() {
            return "port=" + this.mUsbPort + ", status=" + this.mUsbPortStatus + ", canChangeMode=" + this.mCanChangeMode + ", canChangePowerRole=" + this.mCanChangePowerRole + ", canChangeDataRole=" + this.mCanChangeDataRole;
        }
    }

    private static final class RawPortInfo implements Parcelable {
        public static final Creator<RawPortInfo> CREATOR = new Creator<RawPortInfo>() {
            public RawPortInfo createFromParcel(Parcel in) {
                return new RawPortInfo(in.readString(), in.readInt(), in.readInt(), in.readByte() != (byte) 0, in.readInt(), in.readByte() != (byte) 0, in.readInt(), in.readByte() != (byte) 0);
            }

            public RawPortInfo[] newArray(int size) {
                return new RawPortInfo[size];
            }
        };
        public boolean canChangeDataRole;
        public boolean canChangeMode;
        public boolean canChangePowerRole;
        public int currentDataRole;
        public int currentMode;
        public int currentPowerRole;
        public final String portId;
        public final int supportedModes;

        RawPortInfo(String portId, int supportedModes) {
            this.portId = portId;
            this.supportedModes = supportedModes;
        }

        RawPortInfo(String portId, int supportedModes, int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole) {
            this.portId = portId;
            this.supportedModes = supportedModes;
            this.currentMode = currentMode;
            this.canChangeMode = canChangeMode;
            this.currentPowerRole = currentPowerRole;
            this.canChangePowerRole = canChangePowerRole;
            this.currentDataRole = currentDataRole;
            this.canChangeDataRole = canChangeDataRole;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            dest.writeString(this.portId);
            dest.writeInt(this.supportedModes);
            dest.writeInt(this.currentMode);
            dest.writeByte((byte) (this.canChangeMode ? 1 : 0));
            dest.writeInt(this.currentPowerRole);
            if (this.canChangePowerRole) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            dest.writeInt(this.currentDataRole);
            if (!this.canChangeDataRole) {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
        }
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            UsbPortManager.logAndPrint(4, null, "Usb hal service started " + fqName + " " + name);
            UsbPortManager.this.connectToProxy(null);
        }
    }

    public UsbPortManager(Context context) {
        this.mContext = context;
        try {
            if (!IServiceManager.getService().registerForNotifications(IUsb.kInterfaceName, "", this.mServiceNotification)) {
                logAndPrint(6, null, "Failed to register service start notification");
            }
            connectToProxy(null);
        } catch (RemoteException e) {
            logAndPrintException(null, "Failed to register service start notification", e);
        }
    }

    public void systemReady() {
        if (this.mProxy != null) {
            try {
                this.mProxy.queryPortStatus();
            } catch (RemoteException e) {
                logAndPrintException(null, "ServiceStart: Failed to query port status", e);
            }
        }
        this.mSystemReady = true;
    }

    public UsbPort[] getPorts() {
        UsbPort[] result;
        synchronized (this.mLock) {
            int count = this.mPorts.size();
            result = new UsbPort[count];
            for (int i = 0; i < count; i++) {
                result[i] = ((PortInfo) this.mPorts.valueAt(i)).mUsbPort;
            }
        }
        return result;
    }

    public UsbPortStatus getPortStatus(String portId) {
        UsbPortStatus usbPortStatus = null;
        synchronized (this.mLock) {
            PortInfo portInfo = (PortInfo) this.mPorts.get(portId);
            if (portInfo != null) {
                usbPortStatus = portInfo.mUsbPortStatus;
            }
        }
        return usbPortStatus;
    }

    /* JADX WARNING: Missing block: B:8:0x0031, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x0099, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:0x0150, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPortRoles(String portId, int newPowerRole, int newDataRole, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            PortInfo portInfo = (PortInfo) this.mPorts.get(portId);
            if (portInfo == null) {
                if (pw != null) {
                    pw.println("No such USB port: " + portId);
                }
            } else if (portInfo.mUsbPortStatus.isRoleCombinationSupported(newPowerRole, newDataRole)) {
                int currentDataRole = portInfo.mUsbPortStatus.getCurrentDataRole();
                int currentPowerRole = portInfo.mUsbPortStatus.getCurrentPowerRole();
                if (currentDataRole != newDataRole || currentPowerRole != newPowerRole) {
                    int newMode;
                    boolean canChangeMode = portInfo.mCanChangeMode;
                    boolean canChangePowerRole = portInfo.mCanChangePowerRole;
                    boolean canChangeDataRole = portInfo.mCanChangeDataRole;
                    int currentMode = portInfo.mUsbPortStatus.getCurrentMode();
                    if ((canChangePowerRole || currentPowerRole == newPowerRole) && (canChangeDataRole || currentDataRole == newDataRole)) {
                        newMode = currentMode;
                    } else if (canChangeMode && newPowerRole == 1 && newDataRole == 1) {
                        newMode = 2;
                    } else if (canChangeMode && newPowerRole == 2 && newDataRole == 2) {
                        newMode = 1;
                    } else {
                        logAndPrint(6, pw, "Found mismatch in supported USB role combinations while attempting to change role: " + portInfo + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
                        return;
                    }
                    logAndPrint(4, pw, "Setting USB port mode and role: portId=" + portId + ", currentMode=" + UsbPort.modeToString(currentMode) + ", currentPowerRole=" + UsbPort.powerRoleToString(currentPowerRole) + ", currentDataRole=" + UsbPort.dataRoleToString(currentDataRole) + ", newMode=" + UsbPort.modeToString(newMode) + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
                    RawPortInfo sim = (RawPortInfo) this.mSimulatedPorts.get(portId);
                    if (sim != null) {
                        sim.currentMode = newMode;
                        sim.currentPowerRole = newPowerRole;
                        sim.currentDataRole = newDataRole;
                        updatePortsLocked(pw, null);
                    } else if (this.mProxy != null) {
                        PortRole newRole;
                        if (currentMode != newMode) {
                            logAndPrint(6, pw, "Trying to set the USB port mode: portId=" + portId + ", newMode=" + UsbPort.modeToString(newMode));
                            newRole = new PortRole();
                            newRole.type = 2;
                            newRole.role = newMode;
                            try {
                                this.mProxy.switchRole(portId, newRole);
                            } catch (RemoteException e) {
                                logAndPrintException(pw, "Failed to set the USB port mode: portId=" + portId + ", newMode=" + UsbPort.modeToString(newRole.role), e);
                                return;
                            }
                        }
                        if (currentPowerRole != newPowerRole) {
                            newRole = new PortRole();
                            newRole.type = 1;
                            newRole.role = newPowerRole;
                            try {
                                this.mProxy.switchRole(portId, newRole);
                            } catch (RemoteException e2) {
                                logAndPrintException(pw, "Failed to set the USB port power role: portId=" + portId + ", newPowerRole=" + UsbPort.powerRoleToString(newRole.role), e2);
                                return;
                            }
                        }
                        if (currentDataRole != newDataRole) {
                            newRole = new PortRole();
                            newRole.type = 0;
                            newRole.role = newDataRole;
                            try {
                                this.mProxy.switchRole(portId, newRole);
                            } catch (RemoteException e22) {
                                logAndPrintException(pw, "Failed to set the USB port data role: portId=" + portId + ", newDataRole=" + UsbPort.dataRoleToString(newRole.role), e22);
                            }
                        }
                    }
                } else if (pw != null) {
                    pw.println("No change.");
                }
            } else {
                logAndPrint(6, pw, "Attempted to set USB port into unsupported role combination: portId=" + portId + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
            }
        }
    }

    public void addSimulatedPort(String portId, int supportedModes, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mSimulatedPorts.containsKey(portId)) {
                pw.println("Port with same name already exists.  Please remove it first.");
                return;
            }
            pw.println("Adding simulated port: portId=" + portId + ", supportedModes=" + UsbPort.modeToString(supportedModes));
            this.mSimulatedPorts.put(portId, new RawPortInfo(portId, supportedModes));
            updatePortsLocked(pw, null);
        }
    }

    public void connectSimulatedPort(String portId, int mode, boolean canChangeMode, int powerRole, boolean canChangePowerRole, int dataRole, boolean canChangeDataRole, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            RawPortInfo portInfo = (RawPortInfo) this.mSimulatedPorts.get(portId);
            if (portInfo == null) {
                pw.println("Cannot connect simulated port which does not exist.");
            } else if (mode == 0 || powerRole == 0 || dataRole == 0) {
                pw.println("Cannot connect simulated port in null mode, power role, or data role.");
            } else if ((portInfo.supportedModes & mode) == 0) {
                pw.println("Simulated port does not support mode: " + UsbPort.modeToString(mode));
            } else {
                pw.println("Connecting simulated port: portId=" + portId + ", mode=" + UsbPort.modeToString(mode) + ", canChangeMode=" + canChangeMode + ", powerRole=" + UsbPort.powerRoleToString(powerRole) + ", canChangePowerRole=" + canChangePowerRole + ", dataRole=" + UsbPort.dataRoleToString(dataRole) + ", canChangeDataRole=" + canChangeDataRole);
                portInfo.currentMode = mode;
                portInfo.canChangeMode = canChangeMode;
                portInfo.currentPowerRole = powerRole;
                portInfo.canChangePowerRole = canChangePowerRole;
                portInfo.currentDataRole = dataRole;
                portInfo.canChangeDataRole = canChangeDataRole;
                updatePortsLocked(pw, null);
            }
        }
    }

    public void disconnectSimulatedPort(String portId, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            RawPortInfo portInfo = (RawPortInfo) this.mSimulatedPorts.get(portId);
            if (portInfo == null) {
                pw.println("Cannot disconnect simulated port which does not exist.");
                return;
            }
            pw.println("Disconnecting simulated port: portId=" + portId);
            portInfo.currentMode = 0;
            portInfo.canChangeMode = false;
            portInfo.currentPowerRole = 0;
            portInfo.canChangePowerRole = false;
            portInfo.currentDataRole = 0;
            portInfo.canChangeDataRole = false;
            updatePortsLocked(pw, null);
        }
    }

    public void removeSimulatedPort(String portId, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            int index = this.mSimulatedPorts.indexOfKey(portId);
            if (index < 0) {
                pw.println("Cannot remove simulated port which does not exist.");
                return;
            }
            pw.println("Disconnecting simulated port: portId=" + portId);
            this.mSimulatedPorts.removeAt(index);
            updatePortsLocked(pw, null);
        }
    }

    public void resetSimulation(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("Removing all simulated ports and ending simulation.");
            if (!this.mSimulatedPorts.isEmpty()) {
                this.mSimulatedPorts.clear();
                updatePortsLocked(pw, null);
            }
        }
    }

    public void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            pw.print("USB Port State:");
            if (!this.mSimulatedPorts.isEmpty()) {
                pw.print(" (simulation active; end with 'dumpsys usb reset')");
            }
            pw.println();
            if (this.mPorts.isEmpty()) {
                pw.println("  <no ports>");
            } else {
                for (PortInfo portInfo : this.mPorts.values()) {
                    pw.println("  " + portInfo.mUsbPort.getId() + ": " + portInfo);
                }
            }
        }
    }

    private void connectToProxy(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                return;
            }
            try {
                this.mProxy = IUsb.getService();
                this.mProxy.linkToDeath(new DeathRecipient(pw), 1000);
                this.mProxy.setCallback(this.mHALCallback);
                this.mProxy.queryPortStatus();
            } catch (NoSuchElementException e) {
                logAndPrintException(pw, "connectToProxy: usb hal service not found. Did the service fail to start?", e);
            } catch (RemoteException e2) {
                logAndPrintException(pw, "connectToProxy: usb hal service not responding", e2);
            }
        }
    }

    private void updatePortsLocked(IndentingPrintWriter pw, ArrayList<RawPortInfo> newPortInfo) {
        int i;
        int i2 = this.mPorts.size();
        while (true) {
            i = i2;
            i2 = i - 1;
            if (i <= 0) {
                break;
            }
            ((PortInfo) this.mPorts.valueAt(i2)).mDisposition = 3;
        }
        if (this.mSimulatedPorts.isEmpty()) {
            for (RawPortInfo currentPortInfo : newPortInfo) {
                addOrUpdatePortLocked(currentPortInfo.portId, currentPortInfo.supportedModes, currentPortInfo.currentMode, currentPortInfo.canChangeMode, currentPortInfo.currentPowerRole, currentPortInfo.canChangePowerRole, currentPortInfo.currentDataRole, currentPortInfo.canChangeDataRole, pw);
            }
        } else {
            int count = this.mSimulatedPorts.size();
            for (i2 = 0; i2 < count; i2++) {
                RawPortInfo portInfo = (RawPortInfo) this.mSimulatedPorts.valueAt(i2);
                addOrUpdatePortLocked(portInfo.portId, portInfo.supportedModes, portInfo.currentMode, portInfo.canChangeMode, portInfo.currentPowerRole, portInfo.canChangePowerRole, portInfo.currentDataRole, portInfo.canChangeDataRole, pw);
            }
        }
        i2 = this.mPorts.size();
        while (true) {
            i = i2;
            i2 = i - 1;
            if (i > 0) {
                PortInfo portInfo2 = (PortInfo) this.mPorts.valueAt(i2);
                switch (portInfo2.mDisposition) {
                    case 0:
                        handlePortAddedLocked(portInfo2, pw);
                        portInfo2.mDisposition = 2;
                        break;
                    case 1:
                        handlePortChangedLocked(portInfo2, pw);
                        portInfo2.mDisposition = 2;
                        break;
                    case 3:
                        this.mPorts.removeAt(i2);
                        portInfo2.mUsbPortStatus = null;
                        handlePortRemovedLocked(portInfo2, pw);
                        break;
                    default:
                        break;
                }
            }
            return;
        }
    }

    private void addOrUpdatePortLocked(String portId, int supportedModes, int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole, IndentingPrintWriter pw) {
        if (supportedModes != 3) {
            canChangeMode = false;
            if (!(currentMode == 0 || currentMode == supportedModes)) {
                logAndPrint(5, pw, "Ignoring inconsistent current mode from USB port driver: supportedModes=" + UsbPort.modeToString(supportedModes) + ", currentMode=" + UsbPort.modeToString(currentMode));
                currentMode = 0;
            }
        }
        int supportedRoleCombinations = UsbPort.combineRolesAsBit(currentPowerRole, currentDataRole);
        if (!(currentMode == 0 || currentPowerRole == 0 || currentDataRole == 0)) {
            if (canChangePowerRole && canChangeDataRole) {
                supportedRoleCombinations |= ((COMBO_SOURCE_HOST | COMBO_SOURCE_DEVICE) | COMBO_SINK_HOST) | COMBO_SINK_DEVICE;
            } else if (canChangePowerRole) {
                supportedRoleCombinations = (supportedRoleCombinations | UsbPort.combineRolesAsBit(1, currentDataRole)) | UsbPort.combineRolesAsBit(2, currentDataRole);
            } else if (canChangeDataRole) {
                supportedRoleCombinations = (supportedRoleCombinations | UsbPort.combineRolesAsBit(currentPowerRole, 1)) | UsbPort.combineRolesAsBit(currentPowerRole, 2);
            } else if (canChangeMode) {
                supportedRoleCombinations |= COMBO_SOURCE_HOST | COMBO_SINK_DEVICE;
            }
        }
        PortInfo portInfo = (PortInfo) this.mPorts.get(portId);
        if (portInfo == null) {
            portInfo = new PortInfo(portId, supportedModes);
            portInfo.setStatus(currentMode, canChangeMode, currentPowerRole, canChangePowerRole, currentDataRole, canChangeDataRole, supportedRoleCombinations);
            this.mPorts.put(portId, portInfo);
            return;
        }
        if (supportedModes != portInfo.mUsbPort.getSupportedModes()) {
            logAndPrint(5, pw, "Ignoring inconsistent list of supported modes from USB port driver (should be immutable): previous=" + UsbPort.modeToString(portInfo.mUsbPort.getSupportedModes()) + ", current=" + UsbPort.modeToString(supportedModes));
        }
        if (portInfo.setStatus(currentMode, canChangeMode, currentPowerRole, canChangePowerRole, currentDataRole, canChangeDataRole, supportedRoleCombinations)) {
            portInfo.mDisposition = 1;
        } else {
            portInfo.mDisposition = 2;
        }
    }

    private void handlePortAddedLocked(PortInfo portInfo, IndentingPrintWriter pw) {
        logAndPrint(4, pw, "USB port added: " + portInfo);
        sendPortChangedBroadcastLocked(portInfo);
    }

    private void handlePortChangedLocked(PortInfo portInfo, IndentingPrintWriter pw) {
        logAndPrint(4, pw, "USB port changed: " + portInfo);
        sendPortChangedBroadcastLocked(portInfo);
    }

    private void handlePortRemovedLocked(PortInfo portInfo, IndentingPrintWriter pw) {
        logAndPrint(4, pw, "USB port removed: " + portInfo);
        sendPortChangedBroadcastLocked(portInfo);
    }

    private void sendPortChangedBroadcastLocked(PortInfo portInfo) {
        final Intent intent = new Intent("android.hardware.usb.action.USB_PORT_CHANGED");
        intent.addFlags(285212672);
        intent.putExtra("port", portInfo.mUsbPort);
        intent.putExtra("portStatus", portInfo.mUsbPortStatus);
        this.mHandler.post(new Runnable() {
            public void run() {
                UsbPortManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        });
    }

    private static void logAndPrint(int priority, IndentingPrintWriter pw, String msg) {
        Slog.println(priority, TAG, msg);
        if (pw != null) {
            pw.println(msg);
        }
    }

    private static void logAndPrintException(IndentingPrintWriter pw, String msg, Exception e) {
        Slog.e(TAG, msg, e);
        if (pw != null) {
            pw.println(msg + e);
        }
    }
}
