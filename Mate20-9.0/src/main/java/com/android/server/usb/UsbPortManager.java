package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.hardware.usb.V1_0.IUsb;
import android.hardware.usb.V1_0.PortRole;
import android.hardware.usb.V1_0.PortStatus;
import android.hardware.usb.V1_1.IUsbCallback;
import android.hardware.usb.V1_1.PortStatus_1_1;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.usb.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.FgThread;
import java.util.ArrayList;
import java.util.Iterator;
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
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(FgThread.get().getLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ArrayList<RawPortInfo> PortInfo = msg.getData().getParcelableArrayList(UsbPortManager.PORT_INFO);
                synchronized (UsbPortManager.this.mLock) {
                    UsbPortManager.this.updatePortsLocked(null, PortInfo);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final ArrayMap<String, PortInfo> mPorts = new ArrayMap<>();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public IUsb mProxy = null;
    private final ArrayMap<String, RawPortInfo> mSimulatedPorts = new ArrayMap<>();
    /* access modifiers changed from: private */
    public boolean mSystemReady;

    final class DeathRecipient implements IHwBinder.DeathRecipient {
        public IndentingPrintWriter pw;

        DeathRecipient(IndentingPrintWriter pw2) {
            this.pw = pw2;
        }

        public void serviceDied(long cookie) {
            if (cookie == 1000) {
                IndentingPrintWriter indentingPrintWriter = this.pw;
                UsbPortManager.logAndPrint(6, indentingPrintWriter, "Usb hal service died cookie: " + cookie);
                synchronized (UsbPortManager.this.mLock) {
                    IUsb unused = UsbPortManager.this.mProxy = null;
                }
            }
        }
    }

    private static class HALCallback extends IUsbCallback.Stub {
        public UsbPortManager portManager;
        public IndentingPrintWriter pw;

        HALCallback(IndentingPrintWriter pw2, UsbPortManager portManager2) {
            this.pw = pw2;
            this.portManager = portManager2;
        }

        public void notifyPortStatusChange(ArrayList<PortStatus> currentPortStatus, int retval) {
            if (this.portManager.mSystemReady) {
                if (retval != 0) {
                    UsbPortManager.logAndPrint(6, this.pw, "port status enquiry failed");
                    return;
                }
                ArrayList<RawPortInfo> newPortInfo = new ArrayList<>();
                Iterator<PortStatus> it = currentPortStatus.iterator();
                while (it.hasNext()) {
                    PortStatus current = it.next();
                    RawPortInfo temp = new RawPortInfo(current.portName, current.supportedModes, current.currentMode, current.canChangeMode, current.currentPowerRole, current.canChangePowerRole, current.currentDataRole, current.canChangeDataRole);
                    newPortInfo.add(temp);
                    IndentingPrintWriter indentingPrintWriter = this.pw;
                    UsbPortManager.logAndPrint(4, indentingPrintWriter, "ClientCallback: " + current.portName);
                }
                Message message = this.portManager.mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(UsbPortManager.PORT_INFO, newPortInfo);
                message.what = 1;
                message.setData(bundle);
                this.portManager.mHandler.sendMessage(message);
            }
        }

        public void notifyPortStatusChange_1_1(ArrayList<PortStatus_1_1> currentPortStatus, int retval) {
            if (this.portManager.mSystemReady) {
                if (retval != 0) {
                    UsbPortManager.logAndPrint(6, this.pw, "port status enquiry failed");
                    return;
                }
                ArrayList<RawPortInfo> newPortInfo = new ArrayList<>();
                Iterator<PortStatus_1_1> it = currentPortStatus.iterator();
                while (it.hasNext()) {
                    PortStatus_1_1 current = it.next();
                    RawPortInfo temp = new RawPortInfo(current.status.portName, current.supportedModes, current.currentMode, current.status.canChangeMode, current.status.currentPowerRole, current.status.canChangePowerRole, current.status.currentDataRole, current.status.canChangeDataRole);
                    newPortInfo.add(temp);
                    IndentingPrintWriter indentingPrintWriter = this.pw;
                    UsbPortManager.logAndPrint(4, indentingPrintWriter, "ClientCallback: " + current.status.portName);
                }
                Message message = this.portManager.mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(UsbPortManager.PORT_INFO, newPortInfo);
                message.what = 1;
                message.setData(bundle);
                this.portManager.mHandler.sendMessage(message);
            }
        }

        public void notifyRoleSwitchStatus(String portName, PortRole role, int retval) {
            if (retval == 0) {
                IndentingPrintWriter indentingPrintWriter = this.pw;
                UsbPortManager.logAndPrint(4, indentingPrintWriter, portName + " role switch successful");
                return;
            }
            IndentingPrintWriter indentingPrintWriter2 = this.pw;
            UsbPortManager.logAndPrint(6, indentingPrintWriter2, portName + " role switch failed");
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

        /* access modifiers changed from: package-private */
        public void dump(DualDumpOutputStream dump, String idName, long id) {
            long token = dump.start(idName, id);
            DumpUtils.writePort(dump, "port", 1146756268033L, this.mUsbPort);
            DumpUtils.writePortStatus(dump, "status", 1146756268034L, this.mUsbPortStatus);
            dump.write("can_change_mode", 1133871366147L, this.mCanChangeMode);
            dump.write("can_change_power_role", 1133871366148L, this.mCanChangePowerRole);
            dump.write("can_change_data_role", 1133871366149L, this.mCanChangeDataRole);
            dump.end(token);
        }

        public String toString() {
            return "port=" + this.mUsbPort + ", status=" + this.mUsbPortStatus + ", canChangeMode=" + this.mCanChangeMode + ", canChangePowerRole=" + this.mCanChangePowerRole + ", canChangeDataRole=" + this.mCanChangeDataRole;
        }
    }

    private static final class RawPortInfo implements Parcelable {
        public static final Parcelable.Creator<RawPortInfo> CREATOR = new Parcelable.Creator<RawPortInfo>() {
            public RawPortInfo createFromParcel(Parcel in) {
                RawPortInfo rawPortInfo = new RawPortInfo(in.readString(), in.readInt(), in.readInt(), in.readByte() != 0, in.readInt(), in.readByte() != 0, in.readInt(), in.readByte() != 0);
                return rawPortInfo;
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

        RawPortInfo(String portId2, int supportedModes2) {
            this.portId = portId2;
            this.supportedModes = supportedModes2;
        }

        RawPortInfo(String portId2, int supportedModes2, int currentMode2, boolean canChangeMode2, int currentPowerRole2, boolean canChangePowerRole2, int currentDataRole2, boolean canChangeDataRole2) {
            this.portId = portId2;
            this.supportedModes = supportedModes2;
            this.currentMode = currentMode2;
            this.canChangeMode = canChangeMode2;
            this.currentPowerRole = currentPowerRole2;
            this.canChangePowerRole = canChangePowerRole2;
            this.currentDataRole = currentDataRole2;
            this.canChangeDataRole = canChangeDataRole2;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.portId);
            dest.writeInt(this.supportedModes);
            dest.writeInt(this.currentMode);
            dest.writeByte(this.canChangeMode ? (byte) 1 : 0);
            dest.writeInt(this.currentPowerRole);
            dest.writeByte(this.canChangePowerRole ? (byte) 1 : 0);
            dest.writeInt(this.currentDataRole);
            dest.writeByte(this.canChangeDataRole ? (byte) 1 : 0);
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
            if (!IServiceManager.getService().registerForNotifications(IUsb.kInterfaceName, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, new ServiceNotification())) {
                logAndPrint(6, null, "Failed to register service start notification");
            }
            connectToProxy(null);
        } catch (RemoteException e) {
            logAndPrintException(null, "Failed to register service start notification", e);
        }
    }

    public void systemReady() {
        this.mSystemReady = true;
        if (this.mProxy != null) {
            try {
                this.mProxy.queryPortStatus();
            } catch (RemoteException e) {
                logAndPrintException(null, "ServiceStart: Failed to query port status", e);
            }
        }
    }

    public UsbPort[] getPorts() {
        UsbPort[] result;
        synchronized (this.mLock) {
            int count = this.mPorts.size();
            result = new UsbPort[count];
            for (int i = 0; i < count; i++) {
                result[i] = this.mPorts.valueAt(i).mUsbPort;
            }
        }
        return result;
    }

    public UsbPortStatus getPortStatus(String portId) {
        UsbPortStatus usbPortStatus;
        synchronized (this.mLock) {
            PortInfo portInfo = this.mPorts.get(portId);
            usbPortStatus = portInfo != null ? portInfo.mUsbPortStatus : null;
        }
        return usbPortStatus;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0081, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002f, code lost:
        return;
     */
    public void setPortRoles(String portId, int newPowerRole, int newDataRole, IndentingPrintWriter pw) {
        int newMode;
        String str = portId;
        int i = newPowerRole;
        int i2 = newDataRole;
        IndentingPrintWriter indentingPrintWriter = pw;
        synchronized (this.mLock) {
            PortInfo portInfo = this.mPorts.get(str);
            if (portInfo == null) {
                if (indentingPrintWriter != null) {
                    indentingPrintWriter.println("No such USB port: " + str);
                }
            } else if (!portInfo.mUsbPortStatus.isRoleCombinationSupported(i, i2)) {
                logAndPrint(6, indentingPrintWriter, "Attempted to set USB port into unsupported role combination: portId=" + str + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
            } else {
                int currentDataRole = portInfo.mUsbPortStatus.getCurrentDataRole();
                int currentPowerRole = portInfo.mUsbPortStatus.getCurrentPowerRole();
                if (currentDataRole != i2 || currentPowerRole != i) {
                    boolean canChangeMode = portInfo.mCanChangeMode;
                    boolean canChangePowerRole = portInfo.mCanChangePowerRole;
                    boolean canChangeDataRole = portInfo.mCanChangeDataRole;
                    int currentMode = portInfo.mUsbPortStatus.getCurrentMode();
                    if ((canChangePowerRole || currentPowerRole == i) && (canChangeDataRole || currentDataRole == i2)) {
                        newMode = currentMode;
                    } else if (canChangeMode && i == 1 && i2 == 1) {
                        newMode = 2;
                    } else if (canChangeMode && i == 2 && i2 == 2) {
                        newMode = 1;
                    } else {
                        logAndPrint(6, indentingPrintWriter, "Found mismatch in supported USB role combinations while attempting to change role: " + portInfo + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Setting USB port mode and role: portId=");
                    sb.append(str);
                    sb.append(", currentMode=");
                    sb.append(UsbPort.modeToString(currentMode));
                    sb.append(", currentPowerRole=");
                    sb.append(UsbPort.powerRoleToString(currentPowerRole));
                    sb.append(", currentDataRole=");
                    sb.append(UsbPort.dataRoleToString(currentDataRole));
                    sb.append(", newMode=");
                    int newMode2 = newMode;
                    sb.append(UsbPort.modeToString(newMode2));
                    sb.append(", newPowerRole=");
                    sb.append(UsbPort.powerRoleToString(newPowerRole));
                    sb.append(", newDataRole=");
                    sb.append(UsbPort.dataRoleToString(newDataRole));
                    logAndPrint(4, indentingPrintWriter, sb.toString());
                    RawPortInfo sim = this.mSimulatedPorts.get(str);
                    if (sim != null) {
                        sim.currentMode = newMode2;
                        sim.currentPowerRole = i;
                        sim.currentDataRole = i2;
                        updatePortsLocked(indentingPrintWriter, null);
                    } else if (this.mProxy != null) {
                        if (currentMode != newMode2) {
                            StringBuilder sb2 = new StringBuilder();
                            PortInfo portInfo2 = portInfo;
                            sb2.append("Trying to set the USB port mode: portId=");
                            sb2.append(str);
                            sb2.append(", newMode=");
                            sb2.append(UsbPort.modeToString(newMode2));
                            logAndPrint(6, indentingPrintWriter, sb2.toString());
                            PortRole newRole = new PortRole();
                            newRole.type = 2;
                            newRole.role = newMode2;
                            try {
                                this.mProxy.switchRole(str, newRole);
                                int i3 = newMode2;
                                boolean z = canChangeMode;
                            } catch (RemoteException e) {
                                int i4 = newMode2;
                                StringBuilder sb3 = new StringBuilder();
                                boolean z2 = canChangeMode;
                                sb3.append("Failed to set the USB port mode: portId=");
                                sb3.append(str);
                                sb3.append(", newMode=");
                                sb3.append(UsbPort.modeToString(newRole.role));
                                logAndPrintException(indentingPrintWriter, sb3.toString(), e);
                            }
                        } else {
                            int i5 = newMode2;
                            boolean z3 = canChangeMode;
                            if (currentPowerRole != i) {
                                PortRole newRole2 = new PortRole();
                                newRole2.type = 1;
                                newRole2.role = i;
                                try {
                                    this.mProxy.switchRole(str, newRole2);
                                } catch (RemoteException e2) {
                                    logAndPrintException(indentingPrintWriter, "Failed to set the USB port power role: portId=" + str + ", newPowerRole=" + UsbPort.powerRoleToString(newRole2.role), e2);
                                    return;
                                }
                            }
                            if (currentDataRole != i2) {
                                PortRole newRole3 = new PortRole();
                                newRole3.type = 0;
                                newRole3.role = i2;
                                try {
                                    this.mProxy.switchRole(str, newRole3);
                                } catch (RemoteException e3) {
                                    logAndPrintException(indentingPrintWriter, "Failed to set the USB port data role: portId=" + str + ", newDataRole=" + UsbPort.dataRoleToString(newRole3.role), e3);
                                }
                            }
                        }
                    }
                } else if (indentingPrintWriter != null) {
                    indentingPrintWriter.println("No change.");
                }
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
            RawPortInfo portInfo = this.mSimulatedPorts.get(portId);
            if (portInfo == null) {
                pw.println("Cannot connect simulated port which does not exist.");
                return;
            }
            if (!(mode == 0 || powerRole == 0)) {
                if (dataRole != 0) {
                    if ((portInfo.supportedModes & mode) == 0) {
                        pw.println("Simulated port does not support mode: " + UsbPort.modeToString(mode));
                        return;
                    }
                    pw.println("Connecting simulated port: portId=" + portId + ", mode=" + UsbPort.modeToString(mode) + ", canChangeMode=" + canChangeMode + ", powerRole=" + UsbPort.powerRoleToString(powerRole) + ", canChangePowerRole=" + canChangePowerRole + ", dataRole=" + UsbPort.dataRoleToString(dataRole) + ", canChangeDataRole=" + canChangeDataRole);
                    portInfo.currentMode = mode;
                    portInfo.canChangeMode = canChangeMode;
                    portInfo.currentPowerRole = powerRole;
                    portInfo.canChangePowerRole = canChangePowerRole;
                    portInfo.currentDataRole = dataRole;
                    portInfo.canChangeDataRole = canChangeDataRole;
                    updatePortsLocked(pw, null);
                    return;
                }
            }
            pw.println("Cannot connect simulated port in null mode, power role, or data role.");
        }
    }

    public void disconnectSimulatedPort(String portId, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            RawPortInfo portInfo = this.mSimulatedPorts.get(portId);
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

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        synchronized (this.mLock) {
            dump.write("is_simulation_active", 1133871366145L, !this.mSimulatedPorts.isEmpty());
            for (PortInfo portInfo : this.mPorts.values()) {
                portInfo.dump(dump, "usb_ports", 2246267895810L);
            }
        }
        dump.end(token);
    }

    /* access modifiers changed from: private */
    public void connectToProxy(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
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
    }

    /* access modifiers changed from: private */
    public void updatePortsLocked(IndentingPrintWriter pw, ArrayList<RawPortInfo> newPortInfo) {
        IndentingPrintWriter indentingPrintWriter = pw;
        int i = this.mPorts.size();
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                break;
            }
            this.mPorts.valueAt(i2).mDisposition = 3;
            i = i2;
        }
        if (!this.mSimulatedPorts.isEmpty()) {
            int count = this.mSimulatedPorts.size();
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= count) {
                    break;
                }
                RawPortInfo portInfo = this.mSimulatedPorts.valueAt(i4);
                addOrUpdatePortLocked(portInfo.portId, portInfo.supportedModes, portInfo.currentMode, portInfo.canChangeMode, portInfo.currentPowerRole, portInfo.canChangePowerRole, portInfo.currentDataRole, portInfo.canChangeDataRole, indentingPrintWriter);
                i3 = i4 + 1;
            }
        } else {
            Iterator<RawPortInfo> it = newPortInfo.iterator();
            while (it.hasNext()) {
                RawPortInfo currentPortInfo = it.next();
                addOrUpdatePortLocked(currentPortInfo.portId, currentPortInfo.supportedModes, currentPortInfo.currentMode, currentPortInfo.canChangeMode, currentPortInfo.currentPowerRole, currentPortInfo.canChangePowerRole, currentPortInfo.currentDataRole, currentPortInfo.canChangeDataRole, indentingPrintWriter);
            }
        }
        int i5 = this.mPorts.size();
        while (true) {
            int i6 = i5 - 1;
            if (i5 > 0) {
                PortInfo portInfo2 = this.mPorts.valueAt(i6);
                int i7 = portInfo2.mDisposition;
                if (i7 != 3) {
                    switch (i7) {
                        case 0:
                            handlePortAddedLocked(portInfo2, indentingPrintWriter);
                            portInfo2.mDisposition = 2;
                            break;
                        case 1:
                            handlePortChangedLocked(portInfo2, indentingPrintWriter);
                            portInfo2.mDisposition = 2;
                            break;
                    }
                } else {
                    this.mPorts.removeAt(i6);
                    portInfo2.mUsbPortStatus = null;
                    handlePortRemovedLocked(portInfo2, indentingPrintWriter);
                }
                i5 = i6;
            } else {
                return;
            }
        }
    }

    private void addOrUpdatePortLocked(String portId, int supportedModes, int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole, IndentingPrintWriter pw) {
        boolean canChangeMode2;
        int currentMode2;
        String str = portId;
        int i = supportedModes;
        int i2 = currentMode;
        int i3 = currentPowerRole;
        int i4 = currentDataRole;
        IndentingPrintWriter indentingPrintWriter = pw;
        if ((i & 3) != 3) {
            if (!(i2 == 0 || i2 == i)) {
                logAndPrint(5, indentingPrintWriter, "Ignoring inconsistent current mode from USB port driver: supportedModes=" + UsbPort.modeToString(supportedModes) + ", currentMode=" + UsbPort.modeToString(currentMode));
                i2 = 0;
            }
            currentMode2 = i2;
            canChangeMode2 = false;
        } else {
            canChangeMode2 = canChangeMode;
            currentMode2 = i2;
        }
        int supportedRoleCombinations = UsbPort.combineRolesAsBit(i3, i4);
        if (!(currentMode2 == 0 || i3 == 0 || i4 == 0)) {
            if (canChangePowerRole && canChangeDataRole) {
                supportedRoleCombinations |= COMBO_SOURCE_HOST | COMBO_SOURCE_DEVICE | COMBO_SINK_HOST | COMBO_SINK_DEVICE;
            } else if (canChangePowerRole) {
                supportedRoleCombinations = supportedRoleCombinations | UsbPort.combineRolesAsBit(1, i4) | UsbPort.combineRolesAsBit(2, i4);
            } else if (canChangeDataRole) {
                supportedRoleCombinations = supportedRoleCombinations | UsbPort.combineRolesAsBit(i3, 1) | UsbPort.combineRolesAsBit(i3, 2);
            } else if (canChangeMode2) {
                supportedRoleCombinations |= COMBO_SOURCE_HOST | COMBO_SINK_DEVICE;
            }
        }
        int supportedRoleCombinations2 = supportedRoleCombinations;
        PortInfo portInfo = this.mPorts.get(str);
        if (portInfo == null) {
            PortInfo portInfo2 = new PortInfo(str, i);
            portInfo2.setStatus(currentMode2, canChangeMode2, i3, canChangePowerRole, i4, canChangeDataRole, supportedRoleCombinations2);
            this.mPorts.put(str, portInfo2);
            return;
        }
        if (i != portInfo.mUsbPort.getSupportedModes()) {
            logAndPrint(5, indentingPrintWriter, "Ignoring inconsistent list of supported modes from USB port driver (should be immutable): previous=" + UsbPort.modeToString(portInfo.mUsbPort.getSupportedModes()) + ", current=" + UsbPort.modeToString(supportedModes));
        }
        PortInfo portInfo3 = portInfo;
        if (portInfo.setStatus(currentMode2, canChangeMode2, i3, canChangePowerRole, i4, canChangeDataRole, supportedRoleCombinations2)) {
            portInfo3.mDisposition = 1;
        } else {
            portInfo3.mDisposition = 2;
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
        Intent intent = new Intent("android.hardware.usb.action.USB_PORT_CHANGED");
        intent.addFlags(285212672);
        intent.putExtra("port", portInfo.mUsbPort);
        intent.putExtra("portStatus", portInfo.mUsbPortStatus);
        this.mHandler.post(new Runnable(intent) {
            private final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                UsbPortManager.this.mContext.sendBroadcastAsUser(this.f$1, UserHandle.ALL);
            }
        });
    }

    /* access modifiers changed from: private */
    public static void logAndPrint(int priority, IndentingPrintWriter pw, String msg) {
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
