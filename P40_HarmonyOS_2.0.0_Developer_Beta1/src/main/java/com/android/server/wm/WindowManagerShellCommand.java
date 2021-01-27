package com.android.server.wm;

import android.common.HwFrameworkFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.view.IWindowManager;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.huawei.hwaps.IHwApsImpl;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowManagerShellCommand extends ShellCommand {
    private IHwApsImpl mHwApsImpl = HwFrameworkFactory.getHwApsImpl();
    private final IWindowManager mInterface;
    private final WindowManagerService mInternal;

    public WindowManagerShellCommand(WindowManagerService service) {
        this.mInterface = service;
        this.mInternal = service;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        char c;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -1999459663:
                    if (cmd.equals("set-fix-to-user-rotation")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -1316842219:
                    if (cmd.equals("set-user-rotation")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1067396926:
                    if (cmd.equals("tracing")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -336752166:
                    if (cmd.equals("folded-area")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -229462135:
                    if (cmd.equals("dismiss-keyguard")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 3530753:
                    if (cmd.equals("size")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 530020689:
                    if (cmd.equals("overscan")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1552717032:
                    if (cmd.equals("density")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1910897543:
                    if (cmd.equals("scaling")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    return runDisplaySize(pw);
                case 1:
                    return runDisplayDensity(pw);
                case 2:
                    return runDisplayFoldedArea(pw);
                case 3:
                    return runDisplayOverscan(pw);
                case 4:
                    return runDisplayScaling(pw);
                case 5:
                    return runDismissKeyguard(pw);
                case 6:
                    return this.mInternal.mWindowTracing.onShellCommand(this);
                case 7:
                    return runSetDisplayUserRotation(pw);
                case '\b':
                    return runSetFixToUserRotation(pw);
                default:
                    return handleDefaultCommands(cmd);
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int getDisplayId(String opt) {
        String option = "-d".equals(opt) ? opt : getNextOption();
        if (option == null || !"-d".equals(option)) {
            return 0;
        }
        try {
            return Integer.parseInt(getNextArgRequired());
        } catch (NumberFormatException e) {
            PrintWriter errPrintWriter = getErrPrintWriter();
            errPrintWriter.println("Error: bad number " + e);
            return 0;
        } catch (IllegalArgumentException e2) {
            PrintWriter errPrintWriter2 = getErrPrintWriter();
            errPrintWriter2.println("Error: " + e2);
            return 0;
        }
    }

    private void printInitialDisplaySize(PrintWriter pw, int displayId) {
        Point initialSize = new Point();
        Point baseSize = new Point();
        try {
            this.mInterface.getInitialDisplaySize(displayId, initialSize);
            this.mInterface.getBaseDisplaySize(displayId, baseSize);
            pw.println("Physical size: " + initialSize.x + "x" + initialSize.y);
            if (!initialSize.equals(baseSize)) {
                pw.println("Override size: " + baseSize.x + "x" + baseSize.y);
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
        }
    }

    private int runDisplaySize(PrintWriter pw) throws RemoteException {
        int div;
        String size = getNextArg();
        int displayId = getDisplayId(size);
        if (size == null) {
            printInitialDisplaySize(pw, displayId);
            return 0;
        } else if ("-d".equals(size)) {
            printInitialDisplaySize(pw, displayId);
            return 0;
        } else {
            int h = -1;
            if ("reset".equals(size)) {
                div = -1;
            } else {
                int div2 = size.indexOf(120);
                if (div2 <= 0 || div2 >= size.length() - 1) {
                    getErrPrintWriter().println("Error: bad size " + size);
                    return -1;
                }
                String wstr = size.substring(0, div2);
                String hstr = size.substring(div2 + 1);
                try {
                    int w = parseDimension(wstr, displayId);
                    div = parseDimension(hstr, displayId);
                    h = w;
                } catch (NumberFormatException e) {
                    getErrPrintWriter().println("Error: bad number " + e);
                    return -1;
                }
            }
            if (h < 0 || div < 0) {
                this.mInterface.clearForcedDisplaySize(displayId);
            } else {
                this.mInterface.setForcedDisplaySize(displayId, h, div);
            }
            return 0;
        }
    }

    private void printInitialDisplayDensity(PrintWriter pw, int displayId) {
        try {
            int initialDensity = this.mInterface.getInitialDisplayDensity(displayId);
            int baseDensity = this.mInterface.getBaseDisplayDensity(displayId);
            pw.println("Physical density: " + initialDensity);
            if (initialDensity != baseDensity && this.mHwApsImpl != null && !this.mHwApsImpl.isIn1kResolutionof2kScreen()) {
                pw.println("Override density: " + baseDensity);
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
        }
    }

    private int runDisplayDensity(PrintWriter pw) throws RemoteException {
        int density;
        String densityStr = getNextArg();
        int displayId = getDisplayId(densityStr);
        if (densityStr == null) {
            printInitialDisplayDensity(pw, displayId);
            return 0;
        } else if ("-d".equals(densityStr)) {
            printInitialDisplayDensity(pw, displayId);
            return 0;
        } else {
            if ("reset".equals(densityStr)) {
                density = -1;
            } else {
                try {
                    int density2 = Integer.parseInt(densityStr);
                    if (density2 < 72) {
                        getErrPrintWriter().println("Error: density must be >= 72");
                        return -1;
                    }
                    density = density2;
                } catch (NumberFormatException e) {
                    PrintWriter errPrintWriter = getErrPrintWriter();
                    errPrintWriter.println("Error: bad number " + e);
                    return -1;
                }
            }
            if (density > 0) {
                this.mInterface.setForcedDisplayDensityForUser(displayId, density, -2);
            } else {
                this.mInterface.clearForcedDisplayDensityForUser(displayId, -2);
            }
            return 0;
        }
    }

    private void printFoldedArea(PrintWriter pw) {
        Rect foldedArea = this.mInternal.getFoldedArea();
        if (foldedArea.isEmpty()) {
            pw.println("Folded area: none");
            return;
        }
        pw.println("Folded area: " + foldedArea.left + "," + foldedArea.top + "," + foldedArea.right + "," + foldedArea.bottom);
    }

    private int runDisplayFoldedArea(PrintWriter pw) {
        String areaStr = getNextArg();
        Rect rect = new Rect();
        if (areaStr == null) {
            printFoldedArea(pw);
            return 0;
        }
        if ("reset".equals(areaStr)) {
            rect.setEmpty();
        } else {
            Matcher matcher = Pattern.compile("(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)").matcher(areaStr);
            if (!matcher.matches()) {
                getErrPrintWriter().println("Error: area should be LEFT,TOP,RIGHT,BOTTOM");
                return -1;
            }
            rect.set(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
        }
        this.mInternal.setOverrideFoldedArea(rect);
        return 0;
    }

    private int runDisplayOverscan(PrintWriter pw) throws RemoteException {
        String overscanStr = getNextArgRequired();
        Rect rect = new Rect();
        int displayId = getDisplayId(overscanStr);
        if ("reset".equals(overscanStr)) {
            rect.set(0, 0, 0, 0);
        } else {
            Matcher matcher = Pattern.compile("(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)").matcher(overscanStr);
            if (!matcher.matches()) {
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Error: bad rectangle arg: " + overscanStr);
                return -1;
            }
            rect.left = Integer.parseInt(matcher.group(1));
            rect.top = Integer.parseInt(matcher.group(2));
            rect.right = Integer.parseInt(matcher.group(3));
            rect.bottom = Integer.parseInt(matcher.group(4));
        }
        this.mInterface.setOverscan(displayId, rect.left, rect.top, rect.right, rect.bottom);
        return 0;
    }

    private int runDisplayScaling(PrintWriter pw) throws RemoteException {
        String scalingStr = getNextArgRequired();
        if ("auto".equals(scalingStr)) {
            this.mInterface.setForcedDisplayScalingMode(getDisplayId(scalingStr), 0);
        } else if ("off".equals(scalingStr)) {
            this.mInterface.setForcedDisplayScalingMode(getDisplayId(scalingStr), 1);
        } else {
            getErrPrintWriter().println("Error: scaling must be 'auto' or 'off'");
            return -1;
        }
        return 0;
    }

    private int runDismissKeyguard(PrintWriter pw) throws RemoteException {
        this.mInterface.dismissKeyguard((IKeyguardDismissCallback) null, (CharSequence) null);
        return 0;
    }

    private int parseDimension(String s, int displayId) throws NumberFormatException {
        int density;
        if (s.endsWith("px")) {
            return Integer.parseInt(s.substring(0, s.length() - 2));
        }
        if (!s.endsWith("dp")) {
            return Integer.parseInt(s);
        }
        try {
            density = this.mInterface.getBaseDisplayDensity(displayId);
        } catch (RemoteException e) {
            density = 160;
        }
        return (Integer.parseInt(s.substring(0, s.length() - 2)) * density) / 160;
    }

    private int runSetDisplayUserRotation(PrintWriter pw) {
        int rotation;
        String lockMode = getNextArgRequired();
        int displayId = 0;
        String arg = getNextArg();
        if ("-d".equals(arg)) {
            displayId = Integer.parseInt(getNextArgRequired());
            arg = getNextArg();
        }
        if ("free".equals(lockMode)) {
            this.mInternal.thawDisplayRotation(displayId);
            return 0;
        } else if (!lockMode.equals("lock")) {
            getErrPrintWriter().println("Error: lock mode needs to be either free or lock.");
            return -1;
        } else {
            if (arg != null) {
                try {
                    rotation = Integer.parseInt(arg);
                } catch (IllegalArgumentException e) {
                    getErrPrintWriter().println("Error: " + e.getMessage());
                    return -1;
                }
            } else {
                rotation = 0;
            }
            this.mInternal.freezeDisplayRotation(displayId, rotation);
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0073  */
    private int runSetFixToUserRotation(PrintWriter pw) {
        boolean z;
        int fixedToUserRotation;
        int displayId = 0;
        String arg = getNextArgRequired();
        if ("-d".equals(arg)) {
            displayId = Integer.parseInt(getNextArgRequired());
            arg = getNextArgRequired();
        }
        int hashCode = arg.hashCode();
        if (hashCode != -1609594047) {
            if (hashCode != 270940796) {
                if (hashCode == 1544803905 && arg.equals("default")) {
                    z = true;
                    if (z) {
                        fixedToUserRotation = 2;
                    } else if (z) {
                        fixedToUserRotation = 1;
                    } else if (!z) {
                        getErrPrintWriter().println("Error: expecting enabled, disabled or default, but we get " + arg);
                        return -1;
                    } else {
                        fixedToUserRotation = 1;
                    }
                    this.mInternal.setRotateForApp(displayId, fixedToUserRotation);
                    return 0;
                }
            } else if (arg.equals("disabled")) {
                z = true;
                if (z) {
                }
                this.mInternal.setRotateForApp(displayId, fixedToUserRotation);
                return 0;
            }
        } else if (arg.equals("enabled")) {
            z = false;
            if (z) {
            }
            this.mInternal.setRotateForApp(displayId, fixedToUserRotation);
            return 0;
        }
        z = true;
        if (z) {
        }
        this.mInternal.setRotateForApp(displayId, fixedToUserRotation);
        return 0;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Window manager (window) commands:");
        pw.println("  help");
        pw.println("      Print this help text.");
        pw.println("  size [reset|WxH|WdpxHdp] [-d DISPLAY_ID]");
        pw.println("    Return or override display size.");
        pw.println("    width and height in pixels unless suffixed with 'dp'.");
        pw.println("  density [reset|DENSITY] [-d DISPLAY_ID]");
        pw.println("    Return or override display density.");
        pw.println("  folded-area [reset|LEFT,TOP,RIGHT,BOTTOM]");
        pw.println("    Return or override folded area.");
        pw.println("  overscan [reset|LEFT,TOP,RIGHT,BOTTOM] [-d DISPLAY ID]");
        pw.println("    Set overscan area for display.");
        pw.println("  scaling [off|auto] [-d DISPLAY_ID]");
        pw.println("    Set display scaling mode.");
        pw.println("  dismiss-keyguard");
        pw.println("    Dismiss the keyguard, prompting user for auth if necessary.");
        pw.println("  set-user-rotation [free|lock] [-d DISPLAY_ID] [rotation]");
        pw.println("    Set user rotation mode and user rotation.");
        pw.println("  set-fix-to-user-rotation [-d DISPLAY_ID] [enabled|disabled]");
        pw.println("    Enable or disable rotating display for app requested orientation.");
        if (!Build.IS_USER) {
            pw.println("  tracing (start | stop)");
            pw.println("    Start or stop window tracing.");
        }
    }
}
