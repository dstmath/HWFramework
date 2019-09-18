package com.android.server.wm;

import android.common.HwFrameworkFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.view.IWindowManager;
import com.android.server.UiModeManagerService;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowManagerShellCommand extends ShellCommand {
    private final IWindowManager mInterface;
    private final WindowManagerService mInternal;

    public WindowManagerShellCommand(WindowManagerService service) {
        this.mInterface = service;
        this.mInternal = service;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public int onCommand(String cmd) {
        char c;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -1067396926:
                    if (cmd.equals("tracing")) {
                        c = 5;
                        break;
                    }
                case -229462135:
                    if (cmd.equals("dismiss-keyguard")) {
                        c = 4;
                        break;
                    }
                case 3530753:
                    if (cmd.equals("size")) {
                        c = 0;
                        break;
                    }
                case 530020689:
                    if (cmd.equals("overscan")) {
                        c = 2;
                        break;
                    }
                case 1552717032:
                    if (cmd.equals("density")) {
                        c = 1;
                        break;
                    }
                case 1910897543:
                    if (cmd.equals("scaling")) {
                        c = 3;
                        break;
                    }
            }
            c = 65535;
            switch (c) {
                case 0:
                    return runDisplaySize(pw);
                case 1:
                    return runDisplayDensity(pw);
                case 2:
                    return runDisplayOverscan(pw);
                case 3:
                    return runDisplayScaling(pw);
                case 4:
                    return runDismissKeyguard(pw);
                case 5:
                    return this.mInternal.mWindowTracing.onShellCommand(this, getNextArgRequired());
                default:
                    return handleDefaultCommands(cmd);
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int runDisplaySize(PrintWriter pw) throws RemoteException {
        int div;
        String size = getNextArg();
        if (size == null) {
            Point initialSize = new Point();
            Point baseSize = new Point();
            try {
                this.mInterface.getInitialDisplaySize(0, initialSize);
                this.mInterface.getBaseDisplaySize(0, baseSize);
                pw.println("Physical size: " + initialSize.x + "x" + initialSize.y);
                if (!initialSize.equals(baseSize)) {
                    pw.println("Override size: " + baseSize.x + "x" + baseSize.y);
                }
            } catch (RemoteException e) {
            }
            return 0;
        }
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
                int w = parseDimension(wstr);
                div = parseDimension(hstr);
                h = w;
            } catch (NumberFormatException e2) {
                getErrPrintWriter().println("Error: bad number " + e2);
                return -1;
            }
        }
        if (h < 0 || div < 0) {
            this.mInterface.clearForcedDisplaySize(0);
        } else {
            this.mInterface.setForcedDisplaySize(0, h, div);
        }
        return 0;
    }

    private int runDisplayDensity(PrintWriter pw) throws RemoteException {
        int density;
        String densityStr = getNextArg();
        if (densityStr == null) {
            try {
                int initialDensity = this.mInterface.getInitialDisplayDensity(0);
                int baseDensity = this.mInterface.getBaseDisplayDensity(0);
                pw.println("Physical density: " + initialDensity);
                if (initialDensity != baseDensity && !HwFrameworkFactory.getHwApsImpl().isIn1kResolutionof2kScreen()) {
                    pw.println("Override density: " + baseDensity);
                }
            } catch (RemoteException e) {
            }
            return 0;
        }
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
            } catch (NumberFormatException e2) {
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Error: bad number " + e2);
                return -1;
            }
        }
        if (density > 0) {
            this.mInterface.setForcedDisplayDensityForUser(0, density, -2);
        } else {
            this.mInterface.clearForcedDisplayDensityForUser(0, -2);
        }
        return 0;
    }

    private int runDisplayOverscan(PrintWriter pw) throws RemoteException {
        String overscanStr = getNextArgRequired();
        Rect rect = new Rect();
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
        this.mInterface.setOverscan(0, rect.left, rect.top, rect.right, rect.bottom);
        return 0;
    }

    private int runDisplayScaling(PrintWriter pw) throws RemoteException {
        String scalingStr = getNextArgRequired();
        if (UiModeManagerService.Shell.NIGHT_MODE_STR_AUTO.equals(scalingStr)) {
            this.mInterface.setForcedDisplayScalingMode(0, 0);
        } else if ("off".equals(scalingStr)) {
            this.mInterface.setForcedDisplayScalingMode(0, 1);
        } else {
            getErrPrintWriter().println("Error: scaling must be 'auto' or 'off'");
            return -1;
        }
        return 0;
    }

    private int runDismissKeyguard(PrintWriter pw) throws RemoteException {
        this.mInterface.dismissKeyguard(null, null);
        return 0;
    }

    private int parseDimension(String s) throws NumberFormatException {
        int density;
        if (s.endsWith("px")) {
            return Integer.parseInt(s.substring(0, s.length() - 2));
        }
        if (!s.endsWith("dp")) {
            return Integer.parseInt(s);
        }
        try {
            density = this.mInterface.getBaseDisplayDensity(0);
        } catch (RemoteException e) {
            density = 160;
        }
        return (Integer.parseInt(s.substring(0, s.length() - 2)) * density) / 160;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Window manager (window) commands:");
        pw.println("  help");
        pw.println("      Print this help text.");
        pw.println("  size [reset|WxH|WdpxHdp]");
        pw.println("    Return or override display size.");
        pw.println("    width and height in pixels unless suffixed with 'dp'.");
        pw.println("  density [reset|DENSITY]");
        pw.println("    Return or override display density.");
        pw.println("  overscan [reset|LEFT,TOP,RIGHT,BOTTOM]");
        pw.println("    Set overscan area for display.");
        pw.println("  scaling [off|auto]");
        pw.println("    Set display scaling mode.");
        pw.println("  dismiss-keyguard");
        pw.println("    Dismiss the keyguard, prompting user for auth if necessary.");
        if (!Build.IS_USER) {
            pw.println("  tracing (start | stop)");
            pw.println("    Start or stop window tracing.");
        }
    }
}
