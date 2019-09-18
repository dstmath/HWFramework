package com.android.commands.input;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Input {
    private static final String INVALID_ARGUMENTS = "Error: Invalid arguments for command: ";
    private static final Map<String, Integer> SOURCES = new HashMap<String, Integer>() {
        {
            put("keyboard", 257);
            put("dpad", 513);
            put("gamepad", 1025);
            put("touchscreen", 4098);
            put("mouse", 8194);
            put("stylus", 16386);
            put("trackball", 65540);
            put("touchpad", 1048584);
            put("touchnavigation", 2097152);
            put("joystick", 16777232);
        }
    };
    private static final String TAG = "Input";

    public static void main(String[] args) {
        new Input().run(args);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00d3, code lost:
        sendSwipe(r8, java.lang.Float.parseFloat(r15[r0 + 1]), java.lang.Float.parseFloat(r15[r0 + 2]), java.lang.Float.parseFloat(r15[r0 + 3]), java.lang.Float.parseFloat(r15[r0 + 4]), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f8, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0117, code lost:
        sendDragAndDrop(r8, java.lang.Float.parseFloat(r15[r0 + 1]), java.lang.Float.parseFloat(r15[r0 + 2]), java.lang.Float.parseFloat(r15[r0 + 3]), java.lang.Float.parseFloat(r15[r0 + 4]), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x013c, code lost:
        return;
     */
    private void run(String[] args) {
        int inputSource;
        if (args.length < 1) {
            showUsage();
            return;
        }
        int index = 0;
        String command = args[0];
        int inputSource2 = 0;
        if (SOURCES.containsKey(command)) {
            inputSource2 = SOURCES.get(command).intValue();
            index = 0 + 1;
            command = args[index];
        }
        int length = args.length - index;
        try {
            if (command.equals("text")) {
                if (length == 2) {
                    sendText(getSource(inputSource2, 257), args[index + 1]);
                    return;
                }
            } else if (command.equals("keyevent")) {
                if (length >= 2) {
                    boolean longpress = "--longpress".equals(args[index + 1]);
                    int start = longpress ? index + 2 : index + 1;
                    int inputSource3 = getSource(inputSource2, 257);
                    if (args.length > start) {
                        for (int i = start; i < args.length; i++) {
                            int keyCode = KeyEvent.keyCodeFromString(args[i]);
                            if (keyCode == 0) {
                                keyCode = KeyEvent.keyCodeFromString("KEYCODE_" + args[i]);
                            }
                            sendKeyEvent(inputSource3, keyCode, longpress);
                        }
                        return;
                    }
                }
            } else if (!command.equals("tap")) {
                if (command.equals("swipe")) {
                    int duration = -1;
                    inputSource = getSource(inputSource2, 4098);
                    switch (length) {
                        case 5:
                            break;
                        case 6:
                            try {
                                duration = Integer.parseInt(args[index + 5]);
                                break;
                            } catch (NumberFormatException e) {
                                int i2 = inputSource;
                                break;
                            }
                    }
                } else if (command.equals("draganddrop")) {
                    int duration2 = -1;
                    inputSource = getSource(inputSource2, 4098);
                    switch (length) {
                        case 5:
                            break;
                        case 6:
                            duration2 = Integer.parseInt(args[index + 5]);
                            break;
                    }
                } else if (command.equals("press")) {
                    int inputSource4 = getSource(inputSource2, 65540);
                    if (length == 1) {
                        sendTap(inputSource4, 0.0f, 0.0f);
                        return;
                    }
                } else if (command.equals("roll")) {
                    int inputSource5 = getSource(inputSource2, 65540);
                    if (length == 3) {
                        sendMove(inputSource5, Float.parseFloat(args[index + 1]), Float.parseFloat(args[index + 2]));
                        return;
                    }
                } else {
                    PrintStream printStream = System.err;
                    printStream.println("Error: Unknown command: " + command);
                    showUsage();
                    return;
                }
            } else if (length == 3) {
                sendTap(getSource(inputSource2, 4098), Float.parseFloat(args[index + 1]), Float.parseFloat(args[index + 2]));
                return;
            }
        } catch (NumberFormatException e2) {
        }
        PrintStream printStream2 = System.err;
        printStream2.println(INVALID_ARGUMENTS + command);
        showUsage();
    }

    private void sendText(int source, String text) {
        StringBuffer buff = new StringBuffer(text);
        boolean escapeFlag = false;
        int i = 0;
        while (i < buff.length()) {
            if (escapeFlag) {
                escapeFlag = false;
                if (buff.charAt(i) == 's') {
                    buff.setCharAt(i, ' ');
                    i--;
                    buff.deleteCharAt(i);
                }
            }
            if (buff.charAt(i) == '%') {
                escapeFlag = true;
            }
            i++;
        }
        KeyEvent[] events = KeyCharacterMap.load(-1).getEvents(buff.toString().toCharArray());
        for (KeyEvent e : events) {
            if (source != e.getSource()) {
                e.setSource(source);
            }
            injectKeyEvent(e);
        }
    }

    private void sendKeyEvent(int inputSource, int keyCode, boolean longpress) {
        long now = SystemClock.uptimeMillis();
        long now2 = now;
        KeyEvent keyEvent = r1;
        KeyEvent keyEvent2 = new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0, inputSource);
        injectKeyEvent(keyEvent);
        if (longpress) {
            KeyEvent keyEvent3 = new KeyEvent(now2, now2, 0, keyCode, 1, 0, -1, 0, 128, inputSource);
            injectKeyEvent(keyEvent3);
        }
        KeyEvent keyEvent4 = new KeyEvent(now2, now2, 1, keyCode, 0, 0, -1, 0, 0, inputSource);
        injectKeyEvent(keyEvent4);
    }

    private void sendTap(int inputSource, float x, float y) {
        int i = inputSource;
        long uptimeMillis = SystemClock.uptimeMillis();
        float f = x;
        float f2 = y;
        injectMotionEvent(i, 0, uptimeMillis, f, f2, 1.0f);
        injectMotionEvent(i, 1, uptimeMillis, f, f2, 0.0f);
    }

    private void sendSwipe(int inputSource, float x1, float y1, float x2, float y2, int duration) {
        int duration2;
        if (duration < 0) {
            duration2 = 300;
        } else {
            duration2 = duration;
        }
        long now = SystemClock.uptimeMillis();
        injectMotionEvent(inputSource, 0, now, x1, y1, 1.0f);
        long startTime = now;
        long endTime = ((long) duration2) + startTime;
        long now2 = now;
        while (now2 < endTime) {
            long elapsedTime = now2 - startTime;
            float alpha = ((float) elapsedTime) / ((float) duration2);
            long j = elapsedTime;
            injectMotionEvent(inputSource, 2, now2, lerp(x1, x2, alpha), lerp(y1, y2, alpha), 1.0f);
            now2 = SystemClock.uptimeMillis();
        }
        injectMotionEvent(inputSource, 1, now2, x2, y2, 0.0f);
    }

    private void sendDragAndDrop(int inputSource, float x1, float y1, float x2, float y2, int dragDuration) {
        int dragDuration2 = dragDuration < 0 ? 300 : dragDuration;
        injectMotionEvent(inputSource, 0, SystemClock.uptimeMillis(), x1, y1, 1.0f);
        try {
            Thread.sleep((long) ViewConfiguration.getLongPressTimeout());
            long now = SystemClock.uptimeMillis();
            long startTime = now;
            long endTime = ((long) dragDuration2) + startTime;
            while (now < endTime) {
                long elapsedTime = now - startTime;
                float alpha = ((float) elapsedTime) / ((float) dragDuration2);
                long j = elapsedTime;
                injectMotionEvent(inputSource, 2, now, lerp(x1, x2, alpha), lerp(y1, y2, alpha), 1.0f);
                now = SystemClock.uptimeMillis();
            }
            injectMotionEvent(inputSource, 1, now, x2, y2, 0.0f);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMove(int inputSource, float dx, float dy) {
        injectMotionEvent(inputSource, 2, SystemClock.uptimeMillis(), dx, dy, 0.0f);
    }

    private void injectKeyEvent(KeyEvent event) {
        Log.i(TAG, "injectKeyEvent: " + event);
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    private int getInputDeviceId(int inputSource) {
        for (int devId : InputDevice.getDeviceIds()) {
            if (InputDevice.getDevice(devId).supportsSource(inputSource)) {
                return devId;
            }
        }
        return 0;
    }

    private void injectMotionEvent(int inputSource, int action, long when, float x, float y, float pressure) {
        MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, getInputDeviceId(inputSource), 0);
        event.setSource(inputSource);
        Log.i(TAG, "injectMotionEvent: " + event);
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    private static final float lerp(float a, float b, float alpha) {
        return ((b - a) * alpha) + a;
    }

    private static final int getSource(int inputSource, int defaultSource) {
        return inputSource == 0 ? defaultSource : inputSource;
    }

    private void showUsage() {
        System.err.println("Usage: input [<source>] <command> [<arg>...]");
        System.err.println();
        System.err.println("The sources are: ");
        Iterator<String> it = SOURCES.keySet().iterator();
        while (it.hasNext()) {
            PrintStream printStream = System.err;
            printStream.println("      " + it.next());
        }
        System.err.println();
        System.err.println("The commands and default sources are:");
        System.err.println("      text <string> (Default: touchscreen)");
        System.err.println("      keyevent [--longpress] <key code number or name> ... (Default: keyboard)");
        System.err.println("      tap <x> <y> (Default: touchscreen)");
        System.err.println("      swipe <x1> <y1> <x2> <y2> [duration(ms)] (Default: touchscreen)");
        System.err.println("      draganddrop <x1> <y1> <x2> <y2> [duration(ms)] (Default: touchscreen)");
        System.err.println("      press (Default: trackball)");
        System.err.println("      roll <dx> <dy> (Default: trackball)");
    }
}
