package com.android.server.usb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.usb.ParcelableUsbPort;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.hardware.usb.V1_0.IUsb;
import android.hardware.usb.V1_0.PortRole;
import android.hardware.usb.V1_0.PortStatus;
import android.hardware.usb.V1_1.PortStatus_1_1;
import android.hardware.usb.V1_2.IUsbCallback;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.notification.SystemNotificationChannels;
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
    private static final int MSG_SYSTEM_READY = 2;
    private static final int MSG_UPDATE_PORTS = 1;
    private static final String PORT_INFO = "port_info";
    private static final String TAG = "UsbPortManager";
    private static final int USB_HAL_DEATH_COOKIE = 1000;
    private final ArrayMap<String, Boolean> mConnected = new ArrayMap<>();
    private final ArrayMap<String, Integer> mContaminantStatus = new ArrayMap<>();
    private final Context mContext;
    private HALCallback mHALCallback = new HALCallback(null, this);
    private final Handler mHandler = new Handler(FgThread.get().getLooper()) {
        /* class com.android.server.usb.UsbPortManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ArrayList<RawPortInfo> PortInfo = msg.getData().getParcelableArrayList(UsbPortManager.PORT_INFO);
                synchronized (UsbPortManager.this.mLock) {
                    UsbPortManager.this.updatePortsLocked(null, PortInfo);
                }
            } else if (i == 2) {
                UsbPortManager usbPortManager = UsbPortManager.this;
                usbPortManager.mNotificationManager = (NotificationManager) usbPortManager.mContext.getSystemService("notification");
            }
        }
    };
    private int mIsPortContaminatedNotificationId;
    private final Object mLock = new Object();
    private NotificationManager mNotificationManager;
    private final ArrayMap<String, PortInfo> mPorts = new ArrayMap<>();
    @GuardedBy({"mLock"})
    private IUsb mProxy = null;
    private final ArrayMap<String, RawPortInfo> mSimulatedPorts = new ArrayMap<>();
    private boolean mSystemReady;

    public UsbPortManager(Context context) {
        this.mContext = context;
        try {
            if (!IServiceManager.getService().registerForNotifications(IUsb.kInterfaceName, "", new ServiceNotification())) {
                logAndPrint(6, null, "Failed to register service start notification");
            }
            connectToProxy(null);
        } catch (RemoteException e) {
            logAndPrintException(null, "Failed to register service start notification", e);
        }
    }

    public void systemReady() {
        this.mSystemReady = true;
        IUsb iUsb = this.mProxy;
        if (iUsb != null) {
            try {
                iUsb.queryPortStatus();
            } catch (RemoteException e) {
                logAndPrintException(null, "ServiceStart: Failed to query port status", e);
            }
        }
        this.mHandler.sendEmptyMessage(2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x002e, code lost:
        r1 = r5;
     */
    private void updateContaminantNotification() {
        int i;
        int i2;
        PortInfo currentPortInfo = null;
        Resources r = this.mContext.getResources();
        int contaminantStatus = 2;
        Iterator<PortInfo> it = this.mPorts.values().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PortInfo portInfo = it.next();
            contaminantStatus = portInfo.mUsbPortStatus.getContaminantDetectionStatus();
            if (contaminantStatus != 3) {
                if (contaminantStatus == 1) {
                    break;
                }
            } else {
                break;
            }
        }
        if (contaminantStatus == 3 && (i2 = this.mIsPortContaminatedNotificationId) != 52) {
            if (i2 == 53) {
                this.mNotificationManager.cancelAsUser(null, i2, UserHandle.ALL);
            }
            this.mIsPortContaminatedNotificationId = 52;
            CharSequence title = r.getText(17041409);
            String channel = SystemNotificationChannels.ALERTS;
            CharSequence message = r.getText(17041408);
            Intent intent = new Intent();
            intent.addFlags(268435456);
            intent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbContaminantActivity");
            intent.putExtra("port", (Parcelable) ParcelableUsbPort.of(currentPortInfo.mUsbPort));
            this.mNotificationManager.notifyAsUser(null, this.mIsPortContaminatedNotificationId, new Notification.Builder(this.mContext, channel).setOngoing(true).setTicker(title).setColor(this.mContext.getColor(17170460)).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT)).setContentTitle(title).setContentText(message).setVisibility(1).setSmallIcon(17301642).setStyle(new Notification.BigTextStyle().bigText(message)).build(), UserHandle.ALL);
        } else if (contaminantStatus != 3 && (i = this.mIsPortContaminatedNotificationId) == 52) {
            this.mNotificationManager.cancelAsUser(null, i, UserHandle.ALL);
            this.mIsPortContaminatedNotificationId = 0;
            if (contaminantStatus == 2) {
                this.mIsPortContaminatedNotificationId = 53;
                CharSequence title2 = r.getText(17041411);
                String channel2 = SystemNotificationChannels.ALERTS;
                CharSequence message2 = r.getText(17041410);
                this.mNotificationManager.notifyAsUser(null, this.mIsPortContaminatedNotificationId, new Notification.Builder(this.mContext, channel2).setSmallIcon(17302844).setTicker(title2).setColor(this.mContext.getColor(17170460)).setContentTitle(title2).setContentText(message2).setVisibility(1).setStyle(new Notification.BigTextStyle().bigText(message2)).build(), UserHandle.ALL);
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

    public void enableContaminantDetection(String portId, boolean enable, IndentingPrintWriter pw) {
        PortInfo portInfo = this.mPorts.get(portId);
        if (portInfo == null) {
            if (pw != null) {
                pw.println("No such USB port: " + portId);
            }
        } else if (portInfo.mUsbPort.supportsEnableContaminantPresenceDetection()) {
            if (enable && portInfo.mUsbPortStatus.getContaminantDetectionStatus() != 1) {
                return;
            }
            if ((enable || portInfo.mUsbPortStatus.getContaminantDetectionStatus() != 1) && portInfo.mUsbPortStatus.getContaminantDetectionStatus() != 0) {
                try {
                    android.hardware.usb.V1_2.IUsb.castFrom((IHwInterface) this.mProxy).enableContaminantPresenceDetection(portId, enable);
                } catch (RemoteException e) {
                    logAndPrintException(pw, "Failed to set contaminant detection", e);
                } catch (ClassCastException e2) {
                    logAndPrintException(pw, "Method only applicable to V1.2 or above implementation", e2);
                }
            }
        }
    }

    public void setPortRoles(String portId, int newPowerRole, int newDataRole, IndentingPrintWriter pw) {
        int newMode;
        synchronized (this.mLock) {
            PortInfo portInfo = this.mPorts.get(portId);
            if (portInfo == null) {
                if (pw != null) {
                    pw.println("No such USB port: " + portId);
                }
            } else if (!portInfo.mUsbPortStatus.isRoleCombinationSupported(newPowerRole, newDataRole)) {
                logAndPrint(6, pw, "Attempted to set USB port into unsupported role combination: portId=" + portId + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
            } else {
                int currentDataRole = portInfo.mUsbPortStatus.getCurrentDataRole();
                int currentPowerRole = portInfo.mUsbPortStatus.getCurrentPowerRole();
                if (currentDataRole == newDataRole && currentPowerRole == newPowerRole) {
                    if (pw != null) {
                        pw.println("No change.");
                    }
                    return;
                }
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
                RawPortInfo sim = this.mSimulatedPorts.get(portId);
                if (sim != null) {
                    sim.currentMode = newMode;
                    sim.currentPowerRole = newPowerRole;
                    sim.currentDataRole = newDataRole;
                    updatePortsLocked(pw, null);
                } else if (this.mProxy != null) {
                    if (currentMode != newMode) {
                        logAndPrint(6, pw, "Trying to set the USB port mode: portId=" + portId + ", newMode=" + UsbPort.modeToString(newMode));
                        PortRole newRole = new PortRole();
                        newRole.type = 2;
                        newRole.role = newMode;
                        try {
                            this.mProxy.switchRole(portId, newRole);
                        } catch (RemoteException e) {
                            logAndPrintException(pw, "Failed to set the USB port mode: portId=" + portId + ", newMode=" + UsbPort.modeToString(newRole.role), e);
                        }
                    } else {
                        if (currentPowerRole != newPowerRole) {
                            PortRole newRole2 = new PortRole();
                            newRole2.type = 1;
                            newRole2.role = newPowerRole;
                            try {
                                this.mProxy.switchRole(portId, newRole2);
                            } catch (RemoteException e2) {
                                logAndPrintException(pw, "Failed to set the USB port power role: portId=" + portId + ", newPowerRole=" + UsbPort.powerRoleToString(newRole2.role), e2);
                                return;
                            }
                        }
                        if (currentDataRole != newDataRole) {
                            PortRole newRole3 = new PortRole();
                            newRole3.type = 0;
                            newRole3.role = newDataRole;
                            try {
                                this.mProxy.switchRole(portId, newRole3);
                            } catch (RemoteException e3) {
                                logAndPrintException(pw, "Failed to set the USB port data role: portId=" + portId + ", newDataRole=" + UsbPort.dataRoleToString(newRole3.role), e3);
                            }
                        }
                    }
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

    public void simulateContaminantStatus(String portId, boolean detected, IndentingPrintWriter pw) {
        int i;
        synchronized (this.mLock) {
            RawPortInfo portInfo = this.mSimulatedPorts.get(portId);
            if (portInfo == null) {
                pw.println("Simulated port not found.");
                return;
            }
            pw.println("Simulating wet port: portId=" + portId + ", wet=" + detected);
            if (detected) {
                i = 3;
            } else {
                i = 2;
            }
            portInfo.contaminantDetectionStatus = i;
            updatePortsLocked(pw, null);
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
    public static class HALCallback extends IUsbCallback.Stub {
        public UsbPortManager portManager;
        public IndentingPrintWriter pw;

        HALCallback(IndentingPrintWriter pw2, UsbPortManager portManager2) {
            this.pw = pw2;
            this.portManager = portManager2;
        }

        @Override // android.hardware.usb.V1_0.IUsbCallback
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
                    newPortInfo.add(new RawPortInfo(current.portName, current.supportedModes, 0, current.currentMode, current.canChangeMode, current.currentPowerRole, current.canChangePowerRole, current.currentDataRole, current.canChangeDataRole, false, 0, false, 0));
                    IndentingPrintWriter indentingPrintWriter = this.pw;
                    UsbPortManager.logAndPrint(4, indentingPrintWriter, "ClientCallback V1_0: " + current.portName);
                }
                Message message = this.portManager.mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(UsbPortManager.PORT_INFO, newPortInfo);
                message.what = 1;
                message.setData(bundle);
                this.portManager.mHandler.sendMessage(message);
            }
        }

        @Override // android.hardware.usb.V1_1.IUsbCallback
        public void notifyPortStatusChange_1_1(ArrayList<PortStatus_1_1> currentPortStatus, int retval) {
            if (this.portManager.mSystemReady) {
                if (retval != 0) {
                    UsbPortManager.logAndPrint(6, this.pw, "port status enquiry failed");
                    return;
                }
                ArrayList<RawPortInfo> newPortInfo = new ArrayList<>();
                int numStatus = currentPortStatus.size();
                for (int i = 0; i < numStatus; i++) {
                    PortStatus_1_1 current = currentPortStatus.get(i);
                    newPortInfo.add(new RawPortInfo(current.status.portName, current.supportedModes, 0, current.currentMode, current.status.canChangeMode, current.status.currentPowerRole, current.status.canChangePowerRole, current.status.currentDataRole, current.status.canChangeDataRole, false, 0, false, 0));
                    IndentingPrintWriter indentingPrintWriter = this.pw;
                    UsbPortManager.logAndPrint(4, indentingPrintWriter, "ClientCallback V1_1: " + current.status.portName);
                }
                Message message = this.portManager.mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(UsbPortManager.PORT_INFO, newPortInfo);
                message.what = 1;
                message.setData(bundle);
                this.portManager.mHandler.sendMessage(message);
            }
        }

        @Override // android.hardware.usb.V1_2.IUsbCallback
        public void notifyPortStatusChange_1_2(ArrayList<android.hardware.usb.V1_2.PortStatus> currentPortStatus, int retval) {
            if (this.portManager.mSystemReady) {
                if (retval != 0) {
                    UsbPortManager.logAndPrint(6, this.pw, "port status enquiry failed");
                    return;
                }
                ArrayList<RawPortInfo> newPortInfo = new ArrayList<>();
                int i = 0;
                for (int numStatus = currentPortStatus.size(); i < numStatus; numStatus = numStatus) {
                    android.hardware.usb.V1_2.PortStatus current = currentPortStatus.get(i);
                    newPortInfo.add(new RawPortInfo(current.status_1_1.status.portName, current.status_1_1.supportedModes, current.supportedContaminantProtectionModes, current.status_1_1.currentMode, current.status_1_1.status.canChangeMode, current.status_1_1.status.currentPowerRole, current.status_1_1.status.canChangePowerRole, current.status_1_1.status.currentDataRole, current.status_1_1.status.canChangeDataRole, current.supportsEnableContaminantPresenceProtection, current.contaminantProtectionStatus, current.supportsEnableContaminantPresenceDetection, current.contaminantDetectionStatus));
                    IndentingPrintWriter indentingPrintWriter = this.pw;
                    UsbPortManager.logAndPrint(4, indentingPrintWriter, "ClientCallback V1_2: " + current.status_1_1.status.portName);
                    i++;
                }
                Message message = this.portManager.mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(UsbPortManager.PORT_INFO, newPortInfo);
                message.what = 1;
                message.setData(bundle);
                this.portManager.mHandler.sendMessage(message);
            }
        }

        @Override // android.hardware.usb.V1_0.IUsbCallback
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

    /* access modifiers changed from: package-private */
    public final class DeathRecipient implements IHwBinder.DeathRecipient {
        public IndentingPrintWriter pw;

        DeathRecipient(IndentingPrintWriter pw2) {
            this.pw = pw2;
        }

        public void serviceDied(long cookie) {
            if (cookie == 1000) {
                IndentingPrintWriter indentingPrintWriter = this.pw;
                UsbPortManager.logAndPrint(6, indentingPrintWriter, "Usb hal service died cookie: " + cookie);
                synchronized (UsbPortManager.this.mLock) {
                    UsbPortManager.this.mProxy = null;
                }
            }
        }
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        @Override // android.hidl.manager.V1_0.IServiceNotification
        public void onRegistration(String fqName, String name, boolean preexisting) {
            UsbPortManager.logAndPrint(4, null, "Usb hal service started " + fqName + " " + name);
            UsbPortManager.this.connectToProxy(null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToProxy(IndentingPrintWriter pw) {
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
    /* access modifiers changed from: public */
    private void updatePortsLocked(IndentingPrintWriter pw, ArrayList<RawPortInfo> newPortInfo) {
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
            int i3 = 0;
            for (int count = this.mSimulatedPorts.size(); i3 < count; count = count) {
                RawPortInfo portInfo = this.mSimulatedPorts.valueAt(i3);
                addOrUpdatePortLocked(portInfo.portId, portInfo.supportedModes, portInfo.supportedContaminantProtectionModes, portInfo.currentMode, portInfo.canChangeMode, portInfo.currentPowerRole, portInfo.canChangePowerRole, portInfo.currentDataRole, portInfo.canChangeDataRole, portInfo.supportsEnableContaminantPresenceProtection, portInfo.contaminantProtectionStatus, portInfo.supportsEnableContaminantPresenceDetection, portInfo.contaminantDetectionStatus, pw);
                i3++;
            }
        } else {
            Iterator<RawPortInfo> it = newPortInfo.iterator();
            while (it.hasNext()) {
                RawPortInfo currentPortInfo = it.next();
                addOrUpdatePortLocked(currentPortInfo.portId, currentPortInfo.supportedModes, currentPortInfo.supportedContaminantProtectionModes, currentPortInfo.currentMode, currentPortInfo.canChangeMode, currentPortInfo.currentPowerRole, currentPortInfo.canChangePowerRole, currentPortInfo.currentDataRole, currentPortInfo.canChangeDataRole, currentPortInfo.supportsEnableContaminantPresenceProtection, currentPortInfo.contaminantProtectionStatus, currentPortInfo.supportsEnableContaminantPresenceDetection, currentPortInfo.contaminantDetectionStatus, pw);
            }
        }
        int i4 = this.mPorts.size();
        while (true) {
            int i5 = i4 - 1;
            if (i4 > 0) {
                PortInfo portInfo2 = this.mPorts.valueAt(i5);
                int i6 = portInfo2.mDisposition;
                if (i6 == 0) {
                    handlePortAddedLocked(portInfo2, pw);
                    portInfo2.mDisposition = 2;
                } else if (i6 == 1) {
                    handlePortChangedLocked(portInfo2, pw);
                    portInfo2.mDisposition = 2;
                } else if (i6 == 3) {
                    this.mPorts.removeAt(i5);
                    portInfo2.mUsbPortStatus = null;
                    handlePortRemovedLocked(portInfo2, pw);
                }
                i4 = i5;
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00e1  */
    private void addOrUpdatePortLocked(String portId, int supportedModes, int supportedContaminantProtectionModes, int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole, boolean supportsEnableContaminantPresenceProtection, int contaminantProtectionStatus, boolean supportsEnableContaminantPresenceDetection, int contaminantDetectionStatus, IndentingPrintWriter pw) {
        boolean canChangeMode2;
        int currentMode2;
        int supportedRoleCombinations;
        PortInfo portInfo;
        if ((supportedModes & 3) == 3) {
            canChangeMode2 = canChangeMode;
            currentMode2 = currentMode;
        } else if (currentMode == 0 || currentMode == supportedModes) {
            currentMode2 = currentMode;
            canChangeMode2 = false;
        } else {
            logAndPrint(5, pw, "Ignoring inconsistent current mode from USB port driver: supportedModes=" + UsbPort.modeToString(supportedModes) + ", currentMode=" + UsbPort.modeToString(currentMode));
            currentMode2 = 0;
            canChangeMode2 = false;
        }
        int supportedRoleCombinations2 = UsbPort.combineRolesAsBit(currentPowerRole, currentDataRole);
        if (!(currentMode2 == 0 || currentPowerRole == 0 || currentDataRole == 0)) {
            if (!canChangePowerRole || !canChangeDataRole) {
                if (canChangePowerRole) {
                    supportedRoleCombinations = supportedRoleCombinations2 | UsbPort.combineRolesAsBit(1, currentDataRole) | UsbPort.combineRolesAsBit(2, currentDataRole);
                } else if (canChangeDataRole) {
                    supportedRoleCombinations = supportedRoleCombinations2 | UsbPort.combineRolesAsBit(currentPowerRole, 1) | UsbPort.combineRolesAsBit(currentPowerRole, 2);
                } else if (canChangeMode2) {
                    supportedRoleCombinations = supportedRoleCombinations2 | COMBO_SOURCE_HOST | COMBO_SINK_DEVICE;
                }
                portInfo = this.mPorts.get(portId);
                if (portInfo == null) {
                    PortInfo portInfo2 = new PortInfo((UsbManager) this.mContext.getSystemService(UsbManager.class), portId, supportedModes, supportedContaminantProtectionModes, supportsEnableContaminantPresenceProtection, supportsEnableContaminantPresenceDetection);
                    portInfo2.setStatus(currentMode2, canChangeMode2, currentPowerRole, canChangePowerRole, currentDataRole, canChangeDataRole, supportedRoleCombinations, contaminantProtectionStatus, contaminantDetectionStatus);
                    this.mPorts.put(portId, portInfo2);
                    return;
                }
                if (supportedModes != portInfo.mUsbPort.getSupportedModes()) {
                    logAndPrint(5, pw, "Ignoring inconsistent list of supported modes from USB port driver (should be immutable): previous=" + UsbPort.modeToString(portInfo.mUsbPort.getSupportedModes()) + ", current=" + UsbPort.modeToString(supportedModes));
                }
                if (supportsEnableContaminantPresenceProtection != portInfo.mUsbPort.supportsEnableContaminantPresenceProtection()) {
                    logAndPrint(5, pw, "Ignoring inconsistent supportsEnableContaminantPresenceProtectionUSB port driver (should be immutable): previous=" + portInfo.mUsbPort.supportsEnableContaminantPresenceProtection() + ", current=" + supportsEnableContaminantPresenceProtection);
                }
                if (supportsEnableContaminantPresenceDetection != portInfo.mUsbPort.supportsEnableContaminantPresenceDetection()) {
                    logAndPrint(5, pw, "Ignoring inconsistent supportsEnableContaminantPresenceDetection USB port driver (should be immutable): previous=" + portInfo.mUsbPort.supportsEnableContaminantPresenceDetection() + ", current=" + supportsEnableContaminantPresenceDetection);
                }
                if (portInfo.setStatus(currentMode2, canChangeMode2, currentPowerRole, canChangePowerRole, currentDataRole, canChangeDataRole, supportedRoleCombinations, contaminantProtectionStatus, contaminantDetectionStatus)) {
                    portInfo.mDisposition = 1;
                    return;
                } else {
                    portInfo.mDisposition = 2;
                    return;
                }
            } else {
                supportedRoleCombinations = supportedRoleCombinations2 | COMBO_SOURCE_HOST | COMBO_SOURCE_DEVICE | COMBO_SINK_HOST | COMBO_SINK_DEVICE;
                portInfo = this.mPorts.get(portId);
                if (portInfo == null) {
                }
            }
        }
        supportedRoleCombinations = supportedRoleCombinations2;
        portInfo = this.mPorts.get(portId);
        if (portInfo == null) {
        }
    }

    private void handlePortLocked(PortInfo portInfo, IndentingPrintWriter pw) {
        sendPortChangedBroadcastLocked(portInfo);
        logToStatsd(portInfo, pw);
        updateContaminantNotification();
    }

    private void handlePortAddedLocked(PortInfo portInfo, IndentingPrintWriter pw) {
        logAndPrint(4, pw, "USB port added: " + portInfo);
        handlePortLocked(portInfo, pw);
    }

    private void handlePortChangedLocked(PortInfo portInfo, IndentingPrintWriter pw) {
        logAndPrint(4, pw, "USB port changed: " + portInfo);
        enableContaminantDetectionIfNeeded(portInfo, pw);
        handlePortLocked(portInfo, pw);
    }

    private void handlePortRemovedLocked(PortInfo portInfo, IndentingPrintWriter pw) {
        logAndPrint(4, pw, "USB port removed: " + portInfo);
        handlePortLocked(portInfo, pw);
    }

    private static int convertContaminantDetectionStatusToProto(int contaminantDetectionStatus) {
        if (contaminantDetectionStatus == 0) {
            return 1;
        }
        if (contaminantDetectionStatus == 1) {
            return 2;
        }
        if (contaminantDetectionStatus == 2) {
            return 3;
        }
        if (contaminantDetectionStatus != 3) {
            return 0;
        }
        return 4;
    }

    private void sendPortChangedBroadcastLocked(PortInfo portInfo) {
        Intent intent = new Intent("android.hardware.usb.action.USB_PORT_CHANGED");
        intent.addFlags(285212672);
        intent.putExtra("port", (Parcelable) ParcelableUsbPort.of(portInfo.mUsbPort));
        intent.putExtra("portStatus", (Parcelable) portInfo.mUsbPortStatus);
        this.mHandler.post(new Runnable(intent) {
            /* class com.android.server.usb.$$Lambda$UsbPortManager$FUqGOOupcl6RrRkZBkBnrRQyPI */
            private final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                UsbPortManager.this.lambda$sendPortChangedBroadcastLocked$0$UsbPortManager(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$sendPortChangedBroadcastLocked$0$UsbPortManager(Intent intent) {
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.MANAGE_USB");
    }

    private void enableContaminantDetectionIfNeeded(PortInfo portInfo, IndentingPrintWriter pw) {
        if (this.mConnected.containsKey(portInfo.mUsbPort.getId()) && this.mConnected.get(portInfo.mUsbPort.getId()).booleanValue() && !portInfo.mUsbPortStatus.isConnected() && portInfo.mUsbPortStatus.getContaminantDetectionStatus() == 1) {
            enableContaminantDetection(portInfo.mUsbPort.getId(), true, pw);
        }
    }

    private void logToStatsd(PortInfo portInfo, IndentingPrintWriter pw) {
        int i = 0;
        if (portInfo.mUsbPortStatus == null) {
            if (this.mConnected.containsKey(portInfo.mUsbPort.getId())) {
                if (this.mConnected.get(portInfo.mUsbPort.getId()).booleanValue()) {
                    StatsLog.write(70, 0, portInfo.mUsbPort.getId(), portInfo.mLastConnectDurationMillis);
                }
                this.mConnected.remove(portInfo.mUsbPort.getId());
            }
            if (this.mContaminantStatus.containsKey(portInfo.mUsbPort.getId())) {
                if (this.mContaminantStatus.get(portInfo.mUsbPort.getId()).intValue() == 3) {
                    StatsLog.write(146, portInfo.mUsbPort.getId(), convertContaminantDetectionStatusToProto(2));
                }
                this.mContaminantStatus.remove(portInfo.mUsbPort.getId());
                return;
            }
            return;
        }
        if (!this.mConnected.containsKey(portInfo.mUsbPort.getId()) || this.mConnected.get(portInfo.mUsbPort.getId()).booleanValue() != portInfo.mUsbPortStatus.isConnected()) {
            this.mConnected.put(portInfo.mUsbPort.getId(), Boolean.valueOf(portInfo.mUsbPortStatus.isConnected()));
            if (portInfo.mUsbPortStatus.isConnected()) {
                i = 1;
            }
            StatsLog.write(70, i, portInfo.mUsbPort.getId(), portInfo.mLastConnectDurationMillis);
        }
        if (!this.mContaminantStatus.containsKey(portInfo.mUsbPort.getId()) || this.mContaminantStatus.get(portInfo.mUsbPort.getId()).intValue() != portInfo.mUsbPortStatus.getContaminantDetectionStatus()) {
            this.mContaminantStatus.put(portInfo.mUsbPort.getId(), Integer.valueOf(portInfo.mUsbPortStatus.getContaminantDetectionStatus()));
            StatsLog.write(146, portInfo.mUsbPort.getId(), convertContaminantDetectionStatusToProto(portInfo.mUsbPortStatus.getContaminantDetectionStatus()));
        }
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

    /* access modifiers changed from: private */
    public static final class PortInfo {
        public static final int DISPOSITION_ADDED = 0;
        public static final int DISPOSITION_CHANGED = 1;
        public static final int DISPOSITION_READY = 2;
        public static final int DISPOSITION_REMOVED = 3;
        public boolean mCanChangeDataRole;
        public boolean mCanChangeMode;
        public boolean mCanChangePowerRole;
        public long mConnectedAtMillis;
        public int mDisposition;
        public long mLastConnectDurationMillis;
        public final UsbPort mUsbPort;
        public UsbPortStatus mUsbPortStatus;

        PortInfo(UsbManager usbManager, String portId, int supportedModes, int supportedContaminantProtectionModes, boolean supportsEnableContaminantPresenceDetection, boolean supportsEnableContaminantPresenceProtection) {
            this.mUsbPort = new UsbPort(usbManager, portId, supportedModes, supportedContaminantProtectionModes, supportsEnableContaminantPresenceDetection, supportsEnableContaminantPresenceProtection);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0037, code lost:
            if (r17.mUsbPortStatus.getSupportedRoleCombinations() != r24) goto L_0x0051;
         */
        public boolean setStatus(int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole, int supportedRoleCombinations) {
            boolean dispositionChanged = false;
            this.mCanChangeMode = canChangeMode;
            this.mCanChangePowerRole = canChangePowerRole;
            this.mCanChangeDataRole = canChangeDataRole;
            UsbPortStatus usbPortStatus = this.mUsbPortStatus;
            if (usbPortStatus != null) {
                if (usbPortStatus.getCurrentMode() == currentMode) {
                    if (this.mUsbPortStatus.getCurrentPowerRole() == currentPowerRole) {
                        if (this.mUsbPortStatus.getCurrentDataRole() == currentDataRole) {
                        }
                    }
                }
            }
            this.mUsbPortStatus = new UsbPortStatus(currentMode, currentPowerRole, currentDataRole, supportedRoleCombinations, 0, 0);
            dispositionChanged = true;
            if (this.mUsbPortStatus.isConnected() && this.mConnectedAtMillis == 0) {
                this.mConnectedAtMillis = SystemClock.elapsedRealtime();
                this.mLastConnectDurationMillis = 0;
            } else if (!this.mUsbPortStatus.isConnected() && this.mConnectedAtMillis != 0) {
                this.mLastConnectDurationMillis = SystemClock.elapsedRealtime() - this.mConnectedAtMillis;
                this.mConnectedAtMillis = 0;
            }
            return dispositionChanged;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x004b, code lost:
            if (r16.mUsbPortStatus.getContaminantDetectionStatus() != r25) goto L_0x007d;
         */
        public boolean setStatus(int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole, int supportedRoleCombinations, int contaminantProtectionStatus, int contaminantDetectionStatus) {
            boolean dispositionChanged = false;
            this.mCanChangeMode = canChangeMode;
            this.mCanChangePowerRole = canChangePowerRole;
            this.mCanChangeDataRole = canChangeDataRole;
            UsbPortStatus usbPortStatus = this.mUsbPortStatus;
            if (usbPortStatus != null) {
                if (usbPortStatus.getCurrentMode() == currentMode) {
                    if (this.mUsbPortStatus.getCurrentPowerRole() == currentPowerRole) {
                        if (this.mUsbPortStatus.getCurrentDataRole() == currentDataRole) {
                            if (this.mUsbPortStatus.getSupportedRoleCombinations() == supportedRoleCombinations) {
                                if (this.mUsbPortStatus.getContaminantProtectionStatus() == contaminantProtectionStatus) {
                                }
                            }
                        }
                    }
                }
            }
            this.mUsbPortStatus = new UsbPortStatus(currentMode, currentPowerRole, currentDataRole, supportedRoleCombinations, contaminantProtectionStatus, contaminantDetectionStatus);
            dispositionChanged = true;
            if (this.mUsbPortStatus.isConnected() && this.mConnectedAtMillis == 0) {
                this.mConnectedAtMillis = SystemClock.elapsedRealtime();
                this.mLastConnectDurationMillis = 0;
            } else if (!this.mUsbPortStatus.isConnected() && this.mConnectedAtMillis != 0) {
                this.mLastConnectDurationMillis = SystemClock.elapsedRealtime() - this.mConnectedAtMillis;
                this.mConnectedAtMillis = 0;
            }
            return dispositionChanged;
        }

        /* access modifiers changed from: package-private */
        public void dump(DualDumpOutputStream dump, String idName, long id) {
            long token = dump.start(idName, id);
            DumpUtils.writePort(dump, "port", 1146756268033L, this.mUsbPort);
            DumpUtils.writePortStatus(dump, "status", 1146756268034L, this.mUsbPortStatus);
            dump.write("can_change_mode", 1133871366147L, this.mCanChangeMode);
            dump.write("can_change_power_role", 1133871366148L, this.mCanChangePowerRole);
            dump.write("can_change_data_role", 1133871366149L, this.mCanChangeDataRole);
            dump.write("connected_at_millis", 1112396529670L, this.mConnectedAtMillis);
            dump.write("last_connect_duration_millis", 1112396529671L, this.mLastConnectDurationMillis);
            dump.end(token);
        }

        public String toString() {
            return "port=" + this.mUsbPort + ", status=" + this.mUsbPortStatus + ", canChangeMode=" + this.mCanChangeMode + ", canChangePowerRole=" + this.mCanChangePowerRole + ", canChangeDataRole=" + this.mCanChangeDataRole + ", connectedAtMillis=" + this.mConnectedAtMillis + ", lastConnectDurationMillis=" + this.mLastConnectDurationMillis;
        }
    }

    /* access modifiers changed from: private */
    public static final class RawPortInfo implements Parcelable {
        public static final Parcelable.Creator<RawPortInfo> CREATOR = new Parcelable.Creator<RawPortInfo>() {
            /* class com.android.server.usb.UsbPortManager.RawPortInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public RawPortInfo createFromParcel(Parcel in) {
                return new RawPortInfo(in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readByte() != 0, in.readInt(), in.readByte() != 0, in.readInt(), in.readByte() != 0, in.readBoolean(), in.readInt(), in.readBoolean(), in.readInt());
            }

            @Override // android.os.Parcelable.Creator
            public RawPortInfo[] newArray(int size) {
                return new RawPortInfo[size];
            }
        };
        public boolean canChangeDataRole;
        public boolean canChangeMode;
        public boolean canChangePowerRole;
        public int contaminantDetectionStatus;
        public int contaminantProtectionStatus;
        public int currentDataRole;
        public int currentMode;
        public int currentPowerRole;
        public final String portId;
        public final int supportedContaminantProtectionModes;
        public final int supportedModes;
        public boolean supportsEnableContaminantPresenceDetection;
        public boolean supportsEnableContaminantPresenceProtection;

        RawPortInfo(String portId2, int supportedModes2) {
            this.portId = portId2;
            this.supportedModes = supportedModes2;
            this.supportedContaminantProtectionModes = 0;
            this.supportsEnableContaminantPresenceProtection = false;
            this.contaminantProtectionStatus = 0;
            this.supportsEnableContaminantPresenceDetection = false;
            this.contaminantDetectionStatus = 0;
        }

        RawPortInfo(String portId2, int supportedModes2, int supportedContaminantProtectionModes2, int currentMode2, boolean canChangeMode2, int currentPowerRole2, boolean canChangePowerRole2, int currentDataRole2, boolean canChangeDataRole2, boolean supportsEnableContaminantPresenceProtection2, int contaminantProtectionStatus2, boolean supportsEnableContaminantPresenceDetection2, int contaminantDetectionStatus2) {
            this.portId = portId2;
            this.supportedModes = supportedModes2;
            this.supportedContaminantProtectionModes = supportedContaminantProtectionModes2;
            this.currentMode = currentMode2;
            this.canChangeMode = canChangeMode2;
            this.currentPowerRole = currentPowerRole2;
            this.canChangePowerRole = canChangePowerRole2;
            this.currentDataRole = currentDataRole2;
            this.canChangeDataRole = canChangeDataRole2;
            this.supportsEnableContaminantPresenceProtection = supportsEnableContaminantPresenceProtection2;
            this.contaminantProtectionStatus = contaminantProtectionStatus2;
            this.supportsEnableContaminantPresenceDetection = supportsEnableContaminantPresenceDetection2;
            this.contaminantDetectionStatus = contaminantDetectionStatus2;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.portId);
            dest.writeInt(this.supportedModes);
            dest.writeInt(this.supportedContaminantProtectionModes);
            dest.writeInt(this.currentMode);
            dest.writeByte(this.canChangeMode ? (byte) 1 : 0);
            dest.writeInt(this.currentPowerRole);
            dest.writeByte(this.canChangePowerRole ? (byte) 1 : 0);
            dest.writeInt(this.currentDataRole);
            dest.writeByte(this.canChangeDataRole ? (byte) 1 : 0);
            dest.writeBoolean(this.supportsEnableContaminantPresenceProtection);
            dest.writeInt(this.contaminantProtectionStatus);
            dest.writeBoolean(this.supportsEnableContaminantPresenceDetection);
            dest.writeInt(this.contaminantDetectionStatus);
        }
    }
}
