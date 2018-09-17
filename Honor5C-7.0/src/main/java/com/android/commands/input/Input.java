package com.android.commands.input;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.Map;

public class Input {
    private static final String INVALID_ARGUMENTS = "Error: Invalid arguments for command: ";
    private static final Map<String, Integer> SOURCES = null;
    private static final String TAG = "Input";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.input.Input.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.input.Input.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.input.Input.<clinit>():void");
    }

    public static void main(String[] args) {
        new Input().run(args);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void run(String[] args) {
        if (args.length < 1) {
            showUsage();
            return;
        }
        int index = 0;
        String command = args[0];
        int inputSource = 0;
        if (SOURCES.containsKey(command)) {
            inputSource = ((Integer) SOURCES.get(command)).intValue();
            index = 1;
            command = args[1];
        }
        int length = args.length - index;
        try {
            if (command.equals("text")) {
                if (length == 2) {
                    sendText(getSource(inputSource, 257), args[index + 1]);
                    return;
                }
            } else if (command.equals("keyevent")) {
                if (length >= 2) {
                    boolean longpress = "--longpress".equals(args[index + 1]);
                    int start = longpress ? index + 2 : index + 1;
                    inputSource = getSource(inputSource, 257);
                    if (length > start) {
                        for (int i = start; i < length; i++) {
                            int keyCode = KeyEvent.keyCodeFromString(args[i]);
                            if (keyCode == 0) {
                                keyCode = KeyEvent.keyCodeFromString("KEYCODE_" + args[i]);
                            }
                            sendKeyEvent(inputSource, keyCode, longpress);
                        }
                        return;
                    }
                }
            } else if (command.equals("tap")) {
                if (length == 3) {
                    sendTap(getSource(inputSource, 4098), Float.parseFloat(args[index + 1]), Float.parseFloat(args[index + 2]));
                    return;
                }
            } else if (command.equals("swipe")) {
                int duration = -1;
                inputSource = getSource(inputSource, 4098);
                switch (length) {
                    case 5:
                        break;
                    case 6:
                        duration = Integer.parseInt(args[index + 5]);
                        break;
                }
            } else if (command.equals("press")) {
                inputSource = getSource(inputSource, 65540);
                if (length == 1) {
                    sendTap(inputSource, 0.0f, 0.0f);
                    return;
                }
            } else if (command.equals("roll")) {
                inputSource = getSource(inputSource, 65540);
                if (length == 3) {
                    sendMove(inputSource, Float.parseFloat(args[index + 1]), Float.parseFloat(args[index + 2]));
                    return;
                }
            } else {
                System.err.println("Error: Unknown command: " + command);
                showUsage();
                return;
            }
        } catch (NumberFormatException e) {
        }
        System.err.println(INVALID_ARGUMENTS + command);
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
        injectKeyEvent(new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0, inputSource));
        if (longpress) {
            injectKeyEvent(new KeyEvent(now, now, 0, keyCode, 1, 0, -1, 0, 128, inputSource));
        }
        injectKeyEvent(new KeyEvent(now, now, 1, keyCode, 0, 0, -1, 0, 0, inputSource));
    }

    private void sendTap(int inputSource, float x, float y) {
        long now = SystemClock.uptimeMillis();
        injectMotionEvent(inputSource, 0, now, x, y, 1.0f);
        injectMotionEvent(inputSource, 1, now, x, y, 0.0f);
    }

    private void sendSwipe(int inputSource, float x1, float y1, float x2, float y2, int duration) {
        if (duration < 0) {
            duration = 300;
        }
        long now = SystemClock.uptimeMillis();
        injectMotionEvent(inputSource, 0, now, x1, y1, 1.0f);
        long startTime = now;
        long endTime = now + ((long) duration);
        while (now < endTime) {
            float alpha = ((float) (now - startTime)) / ((float) duration);
            injectMotionEvent(inputSource, 2, now, lerp(x1, x2, alpha), lerp(y1, y2, alpha), 1.0f);
            now = SystemClock.uptimeMillis();
        }
        injectMotionEvent(inputSource, 1, now, x2, y2, 0.0f);
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
        InputEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, getInputDeviceId(inputSource), 0);
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
        for (String src : SOURCES.keySet()) {
            System.err.println("      " + src);
        }
        System.err.println();
        System.err.println("The commands and default sources are:");
        System.err.println("      text <string> (Default: touchscreen)");
        System.err.println("      keyevent [--longpress] <key code number or name> ... (Default: keyboard)");
        System.err.println("      tap <x> <y> (Default: touchscreen)");
        System.err.println("      swipe <x1> <y1> <x2> <y2> [duration(ms)] (Default: touchscreen)");
        System.err.println("      press (Default: trackball)");
        System.err.println("      roll <dx> <dy> (Default: trackball)");
    }
}
