package com.android.commands.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AndroidException;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.internal.os.BaseCommand;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Wm extends BaseCommand {
    private IWindowManager mWm;

    public static void main(String[] args) {
        new Wm().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: wm [subcommand] [options]\n       wm size [reset|WxH|WdpxHdp]\n       wm density [reset|DENSITY]\n       wm overscan [reset|LEFT,TOP,RIGHT,BOTTOM]\n       wm scaling [off|auto]\n       wm screen-capture [userId] [true|false]\n\nwm size: return or override display size.\n         width and height in pixels unless suffixed with 'dp'.\n\nwm density: override display density.\n\nwm overscan: set overscan area for display.\n\nwm scaling: set display scaling mode.\n\nwm screen-capture: enable/disable screen capture.\n\nwm dismiss-keyguard: dismiss the keyguard, prompting the user for auth if necessary.\n\nwm surface-trace: log surface commands to stdout in a binary format.\n");
    }

    public void onRun() throws Exception {
        this.mWm = Stub.asInterface(ServiceManager.checkService("window"));
        if (this.mWm == null) {
            System.err.println("Error type 2");
            throw new AndroidException("Can't connect to window manager; is the system running?");
        }
        String op = nextArgRequired();
        if (op.equals("size")) {
            runDisplaySize();
        } else if (op.equals("density")) {
            runDisplayDensity();
        } else if (op.equals("overscan")) {
            runDisplayOverscan();
        } else if (op.equals("scaling")) {
            runDisplayScaling();
        } else if (op.equals("screen-capture")) {
            runSetScreenCapture();
        } else if (op.equals("dismiss-keyguard")) {
            runDismissKeyguard();
        } else if (op.equals("surface-trace")) {
            runSurfaceTrace();
        } else {
            showError("Error: unknown command '" + op + "'");
        }
    }

    private void runSurfaceTrace() throws Exception {
        this.mWm.enableSurfaceTrace(ParcelFileDescriptor.dup(FileDescriptor.out));
        try {
            synchronized (this) {
                wait();
            }
        } finally {
            this.mWm.disableSurfaceTrace();
        }
    }

    private void runSetScreenCapture() throws Exception {
        String userIdStr = nextArg();
        String enableStr = nextArg();
        try {
            try {
                this.mWm.setScreenCaptureDisabled(Integer.parseInt(userIdStr), Boolean.parseBoolean(enableStr) ^ 1);
            } catch (RemoteException e) {
                System.err.println("Error: Can't set screen capture " + e);
            }
        } catch (NumberFormatException e2) {
            System.err.println("Error: bad number " + e2);
        }
    }

    private void runDisplaySize() throws Exception {
        String size = nextArg();
        if (size == null) {
            Point initialSize = new Point();
            Point baseSize = new Point();
            try {
                this.mWm.getInitialDisplaySize(0, initialSize);
                this.mWm.getBaseDisplaySize(0, baseSize);
                System.out.println("Physical size: " + initialSize.x + "x" + initialSize.y);
                if (!initialSize.equals(baseSize)) {
                    System.out.println("Override size: " + baseSize.x + "x" + baseSize.y);
                }
            } catch (RemoteException e) {
            }
            return;
        }
        int h;
        int w;
        if ("reset".equals(size)) {
            h = -1;
            w = -1;
        } else {
            int div = size.indexOf(120);
            if (div <= 0 || div >= size.length() - 1) {
                System.err.println("Error: bad size " + size);
                return;
            }
            String wstr = size.substring(0, div);
            String hstr = size.substring(div + 1);
            try {
                w = parseDimension(wstr);
                h = parseDimension(hstr);
            } catch (NumberFormatException e2) {
                System.err.println("Error: bad number " + e2);
                return;
            }
        }
        if (w < 0 || h < 0) {
            Slog.d("BaseCommand", "clearForcedDisplaySize by command ");
            this.mWm.clearForcedDisplaySize(0);
        } else {
            try {
                Slog.d("BaseCommand", "setForcedDisplaySize by command, width is: " + w + " height is: " + h);
                this.mWm.setForcedDisplaySize(0, w, h);
            } catch (RemoteException e3) {
            }
        }
    }

    private void runDisplayDensity() throws Exception {
        String densityStr = nextArg();
        if (densityStr == null) {
            try {
                int initialDensity = this.mWm.getInitialDisplayDensity(0);
                int baseDensity = this.mWm.getBaseDisplayDensity(0);
                System.out.println("Physical density: " + initialDensity);
                if (initialDensity != baseDensity) {
                    System.out.println("Override density: " + baseDensity);
                }
            } catch (RemoteException e) {
            }
            return;
        }
        int density;
        if ("reset".equals(densityStr)) {
            density = -1;
        } else {
            try {
                density = Integer.parseInt(densityStr);
                if (density < 72) {
                    System.err.println("Error: density must be >= 72");
                    return;
                }
            } catch (NumberFormatException e2) {
                System.err.println("Error: bad number " + e2);
                return;
            }
        }
        if (density > 0) {
            try {
                Slog.d("BaseCommand", "setForcedDisplayDensityForUser by command, density is: " + density);
                this.mWm.setForcedDisplayDensityForUser(0, density, -2);
            } catch (RemoteException e3) {
            }
        } else {
            Slog.d("BaseCommand", "clearForcedDisplayDensityForUser ");
            this.mWm.clearForcedDisplayDensityForUser(0, -2);
        }
    }

    private void runDisplayOverscan() throws Exception {
        String overscanStr = nextArgRequired();
        Rect rect = new Rect();
        if ("reset".equals(overscanStr)) {
            rect.set(0, 0, 0, 0);
        } else {
            Matcher matcher = Pattern.compile("(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)").matcher(overscanStr);
            if (matcher.matches()) {
                rect.left = Integer.parseInt(matcher.group(1));
                rect.top = Integer.parseInt(matcher.group(2));
                rect.right = Integer.parseInt(matcher.group(3));
                rect.bottom = Integer.parseInt(matcher.group(4));
            } else {
                System.err.println("Error: bad rectangle arg: " + overscanStr);
                return;
            }
        }
        try {
            this.mWm.setOverscan(0, rect.left, rect.top, rect.right, rect.bottom);
        } catch (RemoteException e) {
        }
    }

    private void runDisplayScaling() throws Exception {
        String scalingStr = nextArgRequired();
        if ("auto".equals(scalingStr)) {
            this.mWm.setForcedDisplayScalingMode(0, 0);
        } else if ("off".equals(scalingStr)) {
            this.mWm.setForcedDisplayScalingMode(0, 1);
        } else {
            System.err.println("Error: scaling must be 'auto' or 'off'");
        }
    }

    private void runDismissKeyguard() throws Exception {
        this.mWm.dismissKeyguard(null);
    }

    private int parseDimension(String s) throws NumberFormatException {
        if (s.endsWith("px")) {
            return Integer.parseInt(s.substring(0, s.length() - 2));
        }
        if (!s.endsWith("dp")) {
            return Integer.parseInt(s);
        }
        int density;
        try {
            density = this.mWm.getBaseDisplayDensity(0);
        } catch (RemoteException e) {
            density = 160;
        }
        return (Integer.parseInt(s.substring(0, s.length() - 2)) * density) / 160;
    }
}
