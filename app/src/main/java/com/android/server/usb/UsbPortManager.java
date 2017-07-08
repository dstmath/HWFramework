package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.FgThread;
import com.android.server.wm.WindowManagerService.H;
import java.io.File;
import java.io.IOException;
import libcore.io.IoUtils;

public class UsbPortManager {
    private static final int COMBO_SINK_DEVICE = 0;
    private static final int COMBO_SINK_HOST = 0;
    private static final int COMBO_SOURCE_DEVICE = 0;
    private static final int COMBO_SOURCE_HOST = 0;
    private static final int MSG_UPDATE_PORTS = 1;
    private static final String PORT_DATA_ROLE_DEVICE = "device";
    private static final String PORT_DATA_ROLE_HOST = "host";
    private static final String PORT_MODE_DFP = "dfp";
    private static final String PORT_MODE_UFP = "ufp";
    private static final String PORT_POWER_ROLE_SINK = "sink";
    private static final String PORT_POWER_ROLE_SOURCE = "source";
    private static final String SYSFS_CLASS = "/sys/class/dual_role_usb";
    private static final String SYSFS_PORT_DATA_ROLE = "data_role";
    private static final String SYSFS_PORT_MODE = "mode";
    private static final String SYSFS_PORT_POWER_ROLE = "power_role";
    private static final String SYSFS_PORT_SUPPORTED_MODES = "supported_modes";
    private static final String TAG = "UsbPortManager";
    private static final String UEVENT_FILTER = "SUBSYSTEM=dual_role_usb";
    private static final String USB_TYPEC_PROP_PREFIX = "sys.usb.typec.";
    private static final String USB_TYPEC_STATE = "sys.usb.typec.state";
    private final Context mContext;
    private final Handler mHandler;
    private final boolean mHaveKernelSupport;
    private final Object mLock;
    private final ArrayMap<String, PortInfo> mPorts;
    private final ArrayMap<String, SimulatedPortInfo> mSimulatedPorts;
    private final UEventObserver mUEventObserver;

    /* renamed from: com.android.server.usb.UsbPortManager.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbPortManager.MSG_UPDATE_PORTS /*1*/:
                    synchronized (UsbPortManager.this.mLock) {
                        UsbPortManager.this.updatePortsLocked(null);
                        break;
                    }
                default:
            }
        }
    }

    /* renamed from: com.android.server.usb.UsbPortManager.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ Intent val$intent;

        AnonymousClass3(Intent val$intent) {
            this.val$intent = val$intent;
        }

        public void run() {
            UsbPortManager.this.mContext.sendBroadcastAsUser(this.val$intent, UserHandle.ALL);
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

    private static final class SimulatedPortInfo {
        public boolean mCanChangeDataRole;
        public boolean mCanChangeMode;
        public boolean mCanChangePowerRole;
        public int mCurrentDataRole;
        public int mCurrentMode;
        public int mCurrentPowerRole;
        public final String mPortId;
        public final int mSupportedModes;

        public SimulatedPortInfo(String portId, int supportedModes) {
            this.mPortId = portId;
            this.mSupportedModes = supportedModes;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.usb.UsbPortManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.usb.UsbPortManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usb.UsbPortManager.<clinit>():void");
    }

    public UsbPortManager(Context context) {
        this.mLock = new Object();
        this.mPorts = new ArrayMap();
        this.mSimulatedPorts = new ArrayMap();
        this.mHandler = new AnonymousClass1(FgThread.get().getLooper());
        this.mUEventObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                UsbPortManager.this.scheduleUpdatePorts();
            }
        };
        this.mContext = context;
        this.mHaveKernelSupport = new File(SYSFS_CLASS).exists();
    }

    public void systemReady() {
        this.mUEventObserver.startObserving(UEVENT_FILTER);
        scheduleUpdatePorts();
    }

    public UsbPort[] getPorts() {
        UsbPort[] result;
        synchronized (this.mLock) {
            int count = this.mPorts.size();
            result = new UsbPort[count];
            for (int i = COMBO_SOURCE_HOST; i < count; i += MSG_UPDATE_PORTS) {
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
                if (currentDataRole == newDataRole && currentPowerRole == newPowerRole) {
                    if (pw != null) {
                        pw.println("No change.");
                    }
                    return;
                }
                int newMode;
                boolean canChangeMode = portInfo.mCanChangeMode;
                boolean canChangePowerRole = portInfo.mCanChangePowerRole;
                boolean canChangeDataRole = portInfo.mCanChangeDataRole;
                int currentMode = portInfo.mUsbPortStatus.getCurrentMode();
                if ((canChangePowerRole || currentPowerRole == newPowerRole) && (canChangeDataRole || currentDataRole == newDataRole)) {
                    newMode = currentMode;
                } else if (canChangeMode && newPowerRole == MSG_UPDATE_PORTS && newDataRole == MSG_UPDATE_PORTS) {
                    newMode = MSG_UPDATE_PORTS;
                } else if (canChangeMode && newPowerRole == 2 && newDataRole == 2) {
                    newMode = 2;
                } else {
                    logAndPrint(6, pw, "Found mismatch in supported USB role combinations while attempting to change role: " + portInfo + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
                    return;
                }
                logAndPrint(4, pw, "Setting USB port mode and role: portId=" + portId + ", currentMode=" + UsbPort.modeToString(currentMode) + ", currentPowerRole=" + UsbPort.powerRoleToString(currentPowerRole) + ", currentDataRole=" + UsbPort.dataRoleToString(currentDataRole) + ", newMode=" + UsbPort.modeToString(newMode) + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole) + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
                SimulatedPortInfo sim = (SimulatedPortInfo) this.mSimulatedPorts.get(portId);
                if (sim != null) {
                    sim.mCurrentMode = newMode;
                    sim.mCurrentPowerRole = newPowerRole;
                    sim.mCurrentDataRole = newDataRole;
                } else if (this.mHaveKernelSupport) {
                    File portDir = new File(SYSFS_CLASS, portId);
                    if (!portDir.exists()) {
                        logAndPrint(6, pw, "USB port not found: portId=" + portId);
                        return;
                    } else if (currentMode != newMode) {
                        if (!writeFile(portDir, SYSFS_PORT_MODE, newMode == MSG_UPDATE_PORTS ? PORT_MODE_DFP : PORT_MODE_UFP)) {
                            logAndPrint(6, pw, "Failed to set the USB port mode: portId=" + portId + ", newMode=" + UsbPort.modeToString(newMode));
                            return;
                        }
                    } else {
                        if (currentPowerRole != newPowerRole) {
                            if (!writeFile(portDir, SYSFS_PORT_POWER_ROLE, newPowerRole == MSG_UPDATE_PORTS ? PORT_POWER_ROLE_SOURCE : PORT_POWER_ROLE_SINK)) {
                                logAndPrint(6, pw, "Failed to set the USB port power role: portId=" + portId + ", newPowerRole=" + UsbPort.powerRoleToString(newPowerRole));
                                return;
                            }
                        }
                        if (currentDataRole != newDataRole) {
                            if (!writeFile(portDir, SYSFS_PORT_DATA_ROLE, newDataRole == MSG_UPDATE_PORTS ? PORT_DATA_ROLE_HOST : PORT_DATA_ROLE_DEVICE)) {
                                logAndPrint(6, pw, "Failed to set the USB port data role: portId=" + portId + ", newDataRole=" + UsbPort.dataRoleToString(newDataRole));
                                return;
                            }
                        }
                    }
                }
                updatePortsLocked(pw);
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
            this.mSimulatedPorts.put(portId, new SimulatedPortInfo(portId, supportedModes));
            updatePortsLocked(pw);
        }
    }

    public void connectSimulatedPort(String portId, int mode, boolean canChangeMode, int powerRole, boolean canChangePowerRole, int dataRole, boolean canChangeDataRole, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            SimulatedPortInfo portInfo = (SimulatedPortInfo) this.mSimulatedPorts.get(portId);
            if (portInfo == null) {
                pw.println("Cannot connect simulated port which does not exist.");
            } else if (mode == 0 || powerRole == 0 || dataRole == 0) {
                pw.println("Cannot connect simulated port in null mode, power role, or data role.");
            } else if ((portInfo.mSupportedModes & mode) == 0) {
                pw.println("Simulated port does not support mode: " + UsbPort.modeToString(mode));
            } else {
                pw.println("Connecting simulated port: portId=" + portId + ", mode=" + UsbPort.modeToString(mode) + ", canChangeMode=" + canChangeMode + ", powerRole=" + UsbPort.powerRoleToString(powerRole) + ", canChangePowerRole=" + canChangePowerRole + ", dataRole=" + UsbPort.dataRoleToString(dataRole) + ", canChangeDataRole=" + canChangeDataRole);
                portInfo.mCurrentMode = mode;
                portInfo.mCanChangeMode = canChangeMode;
                portInfo.mCurrentPowerRole = powerRole;
                portInfo.mCanChangePowerRole = canChangePowerRole;
                portInfo.mCurrentDataRole = dataRole;
                portInfo.mCanChangeDataRole = canChangeDataRole;
                updatePortsLocked(pw);
            }
        }
    }

    public void disconnectSimulatedPort(String portId, IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            SimulatedPortInfo portInfo = (SimulatedPortInfo) this.mSimulatedPorts.get(portId);
            if (portInfo == null) {
                pw.println("Cannot disconnect simulated port which does not exist.");
                return;
            }
            pw.println("Disconnecting simulated port: portId=" + portId);
            portInfo.mCurrentMode = COMBO_SOURCE_HOST;
            portInfo.mCanChangeMode = false;
            portInfo.mCurrentPowerRole = COMBO_SOURCE_HOST;
            portInfo.mCanChangePowerRole = false;
            portInfo.mCurrentDataRole = COMBO_SOURCE_HOST;
            portInfo.mCanChangeDataRole = false;
            updatePortsLocked(pw);
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
            updatePortsLocked(pw);
        }
    }

    public void resetSimulation(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("Removing all simulated ports and ending simulation.");
            if (!this.mSimulatedPorts.isEmpty()) {
                this.mSimulatedPorts.clear();
                updatePortsLocked(pw);
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

    private void updatePortsLocked(IndentingPrintWriter pw) {
        int i = this.mPorts.size();
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                break;
            }
            ((PortInfo) this.mPorts.valueAt(i2)).mDisposition = 3;
            i = i2;
        }
        if (!this.mSimulatedPorts.isEmpty()) {
            int count = this.mSimulatedPorts.size();
            for (i2 = COMBO_SOURCE_HOST; i2 < count; i2 += MSG_UPDATE_PORTS) {
                SimulatedPortInfo portInfo = (SimulatedPortInfo) this.mSimulatedPorts.valueAt(i2);
                addOrUpdatePortLocked(portInfo.mPortId, portInfo.mSupportedModes, portInfo.mCurrentMode, portInfo.mCanChangeMode, portInfo.mCurrentPowerRole, portInfo.mCanChangePowerRole, portInfo.mCurrentDataRole, portInfo.mCanChangeDataRole, pw);
            }
        } else if (this.mHaveKernelSupport) {
            File[] portDirs = new File(SYSFS_CLASS).listFiles();
            if (portDirs != null) {
                int length = portDirs.length;
                for (int i3 = COMBO_SOURCE_HOST; i3 < length; i3 += MSG_UPDATE_PORTS) {
                    File portDir = portDirs[i3];
                    if (portDir.isDirectory()) {
                        addOrUpdatePortLocked(portDir.getName(), readSupportedModes(portDir), readCurrentMode(portDir), canChangeMode(portDir), readCurrentPowerRole(portDir), canChangePowerRole(portDir), readCurrentDataRole(portDir), canChangeDataRole(portDir), pw);
                    }
                }
            }
        }
        i = this.mPorts.size();
        while (true) {
            i2 = i - 1;
            if (i > 0) {
                PortInfo portInfo2 = (PortInfo) this.mPorts.valueAt(i2);
                switch (portInfo2.mDisposition) {
                    case COMBO_SOURCE_HOST /*0*/:
                        handlePortAddedLocked(portInfo2, pw);
                        portInfo2.mDisposition = 2;
                        break;
                    case MSG_UPDATE_PORTS /*1*/:
                        handlePortChangedLocked(portInfo2, pw);
                        portInfo2.mDisposition = 2;
                        break;
                    case H.REPORT_LOSING_FOCUS /*3*/:
                        this.mPorts.removeAt(i2);
                        portInfo2.mUsbPortStatus = null;
                        handlePortRemovedLocked(portInfo2, pw);
                        break;
                    default:
                        break;
                }
                i = i2;
            } else {
                return;
            }
        }
    }

    private void addOrUpdatePortLocked(String portId, int supportedModes, int currentMode, boolean canChangeMode, int currentPowerRole, boolean canChangePowerRole, int currentDataRole, boolean canChangeDataRole, IndentingPrintWriter pw) {
        if (supportedModes != 3) {
            canChangeMode = false;
            if (!(currentMode == 0 || currentMode == supportedModes)) {
                logAndPrint(5, pw, "Ignoring inconsistent current mode from USB port driver: supportedModes=" + UsbPort.modeToString(supportedModes) + ", currentMode=" + UsbPort.modeToString(currentMode));
                currentMode = COMBO_SOURCE_HOST;
            }
        }
        int supportedRoleCombinations = UsbPort.combineRolesAsBit(currentPowerRole, currentDataRole);
        if (!(currentMode == 0 || currentPowerRole == 0 || currentDataRole == 0)) {
            if (canChangePowerRole && canChangeDataRole) {
                supportedRoleCombinations |= ((COMBO_SOURCE_HOST | COMBO_SOURCE_DEVICE) | COMBO_SINK_HOST) | COMBO_SINK_DEVICE;
            } else if (canChangePowerRole) {
                supportedRoleCombinations = (supportedRoleCombinations | UsbPort.combineRolesAsBit(MSG_UPDATE_PORTS, currentDataRole)) | UsbPort.combineRolesAsBit(2, currentDataRole);
            } else if (canChangeDataRole) {
                supportedRoleCombinations = (supportedRoleCombinations | UsbPort.combineRolesAsBit(currentPowerRole, MSG_UPDATE_PORTS)) | UsbPort.combineRolesAsBit(currentPowerRole, 2);
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
            portInfo.mDisposition = MSG_UPDATE_PORTS;
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
        Intent intent = new Intent("android.hardware.usb.action.USB_PORT_CHANGED");
        intent.addFlags(268435456);
        intent.putExtra("port", portInfo.mUsbPort);
        intent.putExtra("portStatus", portInfo.mUsbPortStatus);
        this.mHandler.post(new AnonymousClass3(intent));
    }

    private void scheduleUpdatePorts() {
        if (!this.mHandler.hasMessages(MSG_UPDATE_PORTS)) {
            this.mHandler.sendEmptyMessage(MSG_UPDATE_PORTS);
        }
    }

    private static int readSupportedModes(File portDir) {
        int modes = COMBO_SOURCE_HOST;
        String contents = readFile(portDir, SYSFS_PORT_SUPPORTED_MODES);
        if (contents == null) {
            return COMBO_SOURCE_HOST;
        }
        if (contents.contains(PORT_MODE_DFP)) {
            modes = MSG_UPDATE_PORTS;
        }
        if (contents.contains(PORT_MODE_UFP)) {
            return modes | 2;
        }
        return modes;
    }

    private static int readCurrentMode(File portDir) {
        String contents = readFile(portDir, SYSFS_PORT_MODE);
        if (contents != null) {
            if (contents.equals(PORT_MODE_DFP)) {
                return MSG_UPDATE_PORTS;
            }
            if (contents.equals(PORT_MODE_UFP)) {
                return 2;
            }
        }
        return COMBO_SOURCE_HOST;
    }

    private static int readCurrentPowerRole(File portDir) {
        String contents = readFile(portDir, SYSFS_PORT_POWER_ROLE);
        if (contents != null) {
            if (contents.equals(PORT_POWER_ROLE_SOURCE)) {
                return MSG_UPDATE_PORTS;
            }
            if (contents.equals(PORT_POWER_ROLE_SINK)) {
                return 2;
            }
        }
        return COMBO_SOURCE_HOST;
    }

    private static int readCurrentDataRole(File portDir) {
        String contents = readFile(portDir, SYSFS_PORT_DATA_ROLE);
        if (contents != null) {
            if (contents.equals(PORT_DATA_ROLE_HOST)) {
                return MSG_UPDATE_PORTS;
            }
            if (contents.equals(PORT_DATA_ROLE_DEVICE)) {
                return 2;
            }
        }
        return COMBO_SOURCE_HOST;
    }

    private static boolean fileIsRootWritable(String path) {
        boolean z = false;
        try {
            if ((Os.stat(path).st_mode & OsConstants.S_IWUSR) != 0) {
                z = true;
            }
            return z;
        } catch (ErrnoException e) {
            return false;
        }
    }

    private static boolean canChangeMode(File portDir) {
        return fileIsRootWritable(new File(portDir, SYSFS_PORT_MODE).getPath());
    }

    private static boolean canChangePowerRole(File portDir) {
        return fileIsRootWritable(new File(portDir, SYSFS_PORT_POWER_ROLE).getPath());
    }

    private static boolean canChangeDataRole(File portDir) {
        return fileIsRootWritable(new File(portDir, SYSFS_PORT_DATA_ROLE).getPath());
    }

    private static String readFile(File dir, String filename) {
        try {
            return IoUtils.readFileAsString(new File(dir, filename).getAbsolutePath()).trim();
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean waitForState(String property, String state) {
        String value = null;
        for (int i = COMBO_SOURCE_HOST; i < 100; i += MSG_UPDATE_PORTS) {
            value = SystemProperties.get(property);
            if (state.equals(value)) {
                return true;
            }
            SystemClock.sleep(50);
        }
        Slog.e(TAG, "waitForState(" + state + ") for " + property + " FAILED: got " + value);
        return false;
    }

    private static String propertyFromFilename(String filename) {
        return USB_TYPEC_PROP_PREFIX + filename;
    }

    private static boolean writeFile(File dir, String filename, String contents) {
        SystemProperties.set(propertyFromFilename(filename), contents);
        boolean changed = waitForState(USB_TYPEC_STATE, contents);
        if (SYSFS_PORT_MODE.equals(filename) && changed) {
            SystemClock.sleep(200);
        }
        return changed;
    }

    private static void logAndPrint(int priority, IndentingPrintWriter pw, String msg) {
        Slog.println(priority, TAG, msg);
        if (pw != null) {
            pw.println(msg);
        }
    }
}
