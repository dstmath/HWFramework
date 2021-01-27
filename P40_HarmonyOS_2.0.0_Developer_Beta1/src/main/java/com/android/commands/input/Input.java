package com.android.commands.input;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.internal.os.BaseCommand;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Input extends BaseCommand {
    private static final Map<String, InputCmd> COMMANDS = new HashMap();
    private static final float DEFAULT_PRESSURE = 1.0f;
    private static final String INVALID_ARGUMENTS = "Error: Invalid arguments for command: ";
    private static final String INVALID_DISPLAY_ARGUMENTS = "Error: Invalid arguments for display ID.";
    private static final float NO_PRESSURE = 0.0f;
    private static final Map<String, Integer> SOURCES = new HashMap<String, Integer>() {
        /* class com.android.commands.input.Input.AnonymousClass1 */

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

    private interface InputCmd {
        void run(int i, int i2);
    }

    public static void main(String[] args) {
        new Input().run(args);
    }

    Input() {
        COMMANDS.put("text", new InputText());
        COMMANDS.put("keyevent", new InputKeyEvent());
        COMMANDS.put("tap", new InputTap());
        COMMANDS.put("swipe", new InputSwipe());
        COMMANDS.put("draganddrop", new InputDragAndDrop());
        COMMANDS.put("press", new InputPress());
        COMMANDS.put("roll", new InputRoll());
        COMMANDS.put("motionevent", new InputMotionEvent());
    }

    public void onRun() throws Exception {
        String arg = nextArgRequired();
        int inputSource = 0;
        if (SOURCES.containsKey(arg)) {
            inputSource = SOURCES.get(arg).intValue();
            arg = nextArgRequired();
        }
        int displayId = -1;
        if ("-d".equals(arg)) {
            displayId = getDisplayId();
            arg = nextArgRequired();
        }
        InputCmd cmd = COMMANDS.get(arg);
        if (cmd != null) {
            try {
                cmd.run(inputSource, displayId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(INVALID_ARGUMENTS + arg);
            }
        } else {
            throw new IllegalArgumentException("Error: Unknown command: " + arg);
        }
    }

    private int getDisplayId() {
        String displayArg = nextArgRequired();
        if ("INVALID_DISPLAY".equalsIgnoreCase(displayArg)) {
            return -1;
        }
        if ("DEFAULT_DISPLAY".equalsIgnoreCase(displayArg)) {
            return 0;
        }
        try {
            int displayId = Integer.parseInt(displayArg);
            if (displayId == -1) {
                return -1;
            }
            return Math.max(displayId, 0);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(INVALID_DISPLAY_ARGUMENTS);
        }
    }

    class InputText implements InputCmd {
        InputText() {
        }

        @Override // com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            sendText(Input.getSource(inputSource, 257), Input.this.nextArgRequired(), displayId);
        }

        private void sendText(int source, String text, int displayId) {
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
                e.setDisplayId(displayId);
                Input.injectKeyEvent(e);
            }
        }
    }

    class InputKeyEvent implements InputCmd {
        InputKeyEvent() {
        }

        @Override // com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            String nextArg;
            String arg = Input.this.nextArgRequired();
            boolean longpress = "--longpress".equals(arg);
            if (longpress) {
                arg = Input.this.nextArgRequired();
            }
            do {
                sendKeyEvent(inputSource, KeyEvent.keyCodeFromString(arg), longpress, displayId);
                nextArg = Input.this.nextArg();
                arg = nextArg;
            } while (nextArg != null);
        }

        private void sendKeyEvent(int inputSource, int keyCode, boolean longpress, int displayId) {
            long now = SystemClock.uptimeMillis();
            KeyEvent event = new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0, inputSource);
            event.setDisplayId(displayId);
            Input.injectKeyEvent(event);
            if (longpress) {
                Input.injectKeyEvent(KeyEvent.changeTimeRepeat(event, now, 0 + 1, 128));
            }
            Input.injectKeyEvent(KeyEvent.changeAction(event, 1));
        }
    }

    class InputTap implements InputCmd {
        InputTap() {
        }

        @Override // com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            sendTap(Input.getSource(inputSource, 4098), Float.parseFloat(Input.this.nextArgRequired()), Float.parseFloat(Input.this.nextArgRequired()), displayId);
        }

        /* access modifiers changed from: package-private */
        public void sendTap(int inputSource, float x, float y, int displayId) {
            long now = SystemClock.uptimeMillis();
            Input.injectMotionEvent(inputSource, 0, now, now, x, y, Input.DEFAULT_PRESSURE, displayId);
            Input.injectMotionEvent(inputSource, 1, now, now, x, y, Input.NO_PRESSURE, displayId);
        }
    }

    class InputPress extends InputTap {
        InputPress() {
            super();
        }

        @Override // com.android.commands.input.Input.InputTap, com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            sendTap(Input.getSource(inputSource, 65540), Input.NO_PRESSURE, Input.NO_PRESSURE, displayId);
        }
    }

    class InputSwipe implements InputCmd {
        InputSwipe() {
        }

        @Override // com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            sendSwipe(Input.getSource(inputSource, 4098), displayId, false);
        }

        /* access modifiers changed from: package-private */
        public void sendSwipe(int inputSource, int displayId, boolean isDragDrop) {
            int duration;
            float x1 = Float.parseFloat(Input.this.nextArgRequired());
            float y1 = Float.parseFloat(Input.this.nextArgRequired());
            float x2 = Float.parseFloat(Input.this.nextArgRequired());
            float y2 = Float.parseFloat(Input.this.nextArgRequired());
            String durationArg = Input.this.nextArg();
            int duration2 = durationArg != null ? Integer.parseInt(durationArg) : -1;
            if (duration2 < 0) {
                duration = 300;
            } else {
                duration = duration2;
            }
            long down = SystemClock.uptimeMillis();
            Input.injectMotionEvent(inputSource, 0, down, down, x1, y1, Input.DEFAULT_PRESSURE, displayId);
            if (isDragDrop) {
                try {
                    Thread.sleep((long) ViewConfiguration.getLongPressTimeout());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            long endTime = down + ((long) duration);
            long now = SystemClock.uptimeMillis();
            while (now < endTime) {
                float alpha = ((float) (now - down)) / ((float) duration);
                Input.injectMotionEvent(inputSource, 2, down, now, Input.lerp(x1, x2, alpha), Input.lerp(y1, y2, alpha), Input.DEFAULT_PRESSURE, displayId);
                now = SystemClock.uptimeMillis();
            }
            Input.injectMotionEvent(inputSource, 1, down, now, x2, y2, Input.NO_PRESSURE, displayId);
        }
    }

    class InputDragAndDrop extends InputSwipe {
        InputDragAndDrop() {
            super();
        }

        @Override // com.android.commands.input.Input.InputSwipe, com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            sendSwipe(Input.getSource(inputSource, 4098), displayId, true);
        }
    }

    class InputRoll implements InputCmd {
        InputRoll() {
        }

        @Override // com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            sendMove(Input.getSource(inputSource, 65540), Float.parseFloat(Input.this.nextArgRequired()), Float.parseFloat(Input.this.nextArgRequired()), displayId);
        }

        private void sendMove(int inputSource, float dx, float dy, int displayId) {
            long now = SystemClock.uptimeMillis();
            Input.injectMotionEvent(inputSource, 2, now, now, dx, dy, Input.NO_PRESSURE, displayId);
        }
    }

    class InputMotionEvent implements InputCmd {
        InputMotionEvent() {
        }

        @Override // com.android.commands.input.Input.InputCmd
        public void run(int inputSource, int displayId) {
            sendMotionEvent(Input.getSource(inputSource, 4098), Input.this.nextArgRequired(), Float.parseFloat(Input.this.nextArgRequired()), Float.parseFloat(Input.this.nextArgRequired()), displayId);
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x003a  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0062  */
        private void sendMotionEvent(int inputSource, String motionEventType, float x, float y, int displayId) {
            char c;
            float pressure;
            int action;
            String upperCase = motionEventType.toUpperCase();
            int hashCode = upperCase.hashCode();
            if (hashCode != 2715) {
                if (hashCode != 2104482) {
                    if (hashCode == 2372561 && upperCase.equals("MOVE")) {
                        c = 2;
                        if (c != 0) {
                            action = 0;
                            pressure = Input.DEFAULT_PRESSURE;
                        } else if (c == 1) {
                            action = 1;
                            pressure = Input.NO_PRESSURE;
                        } else if (c == 2) {
                            action = 2;
                            pressure = Input.DEFAULT_PRESSURE;
                        } else {
                            throw new IllegalArgumentException("Unknown motionevent " + motionEventType);
                        }
                        long now = SystemClock.uptimeMillis();
                        Input.injectMotionEvent(inputSource, action, now, now, x, y, pressure, displayId);
                    }
                } else if (upperCase.equals("DOWN")) {
                    c = 0;
                    if (c != 0) {
                    }
                    long now2 = SystemClock.uptimeMillis();
                    Input.injectMotionEvent(inputSource, action, now2, now2, x, y, pressure, displayId);
                }
            } else if (upperCase.equals("UP")) {
                c = 1;
                if (c != 0) {
                }
                long now22 = SystemClock.uptimeMillis();
                Input.injectMotionEvent(inputSource, action, now22, now22, x, y, pressure, displayId);
            }
            c = 65535;
            if (c != 0) {
            }
            long now222 = SystemClock.uptimeMillis();
            Input.injectMotionEvent(inputSource, action, now222, now222, x, y, pressure, displayId);
        }
    }

    /* access modifiers changed from: private */
    public static void injectKeyEvent(KeyEvent event) {
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    private static int getInputDeviceId(int inputSource) {
        int[] devIds = InputDevice.getDeviceIds();
        for (int devId : devIds) {
            if (InputDevice.getDevice(devId).supportsSource(inputSource)) {
                return devId;
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public static void injectMotionEvent(int inputSource, int action, long downTime, long when, float x, float y, float pressure, int displayId) {
        int displayId2;
        MotionEvent event = MotionEvent.obtain(downTime, when, action, x, y, pressure, DEFAULT_PRESSURE, 0, DEFAULT_PRESSURE, DEFAULT_PRESSURE, getInputDeviceId(inputSource), 0);
        event.setSource(inputSource);
        if (displayId != -1 || (inputSource & 2) == 0) {
            displayId2 = displayId;
        } else {
            displayId2 = 0;
        }
        event.setDisplayId(displayId2);
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    /* access modifiers changed from: private */
    public static final float lerp(float a, float b, float alpha) {
        return ((b - a) * alpha) + a;
    }

    /* access modifiers changed from: private */
    public static final int getSource(int inputSource, int defaultSource) {
        return inputSource == 0 ? defaultSource : inputSource;
    }

    public void onShowUsage(PrintStream out) {
        out.println("Usage: input [<source>] [-d DISPLAY_ID] <command> [<arg>...]");
        out.println();
        out.println("The sources are: ");
        Iterator<String> it = SOURCES.keySet().iterator();
        while (it.hasNext()) {
            out.println("      " + it.next());
        }
        out.println();
        out.printf("-d: specify the display ID.\n      (Default: %d for key event, %d for motion event if not specified.)", -1, 0);
        out.println();
        out.println("The commands and default sources are:");
        out.println("      text <string> (Default: touchscreen)");
        out.println("      keyevent [--longpress] <key code number or name> ... (Default: keyboard)");
        out.println("      tap <x> <y> (Default: touchscreen)");
        out.println("      swipe <x1> <y1> <x2> <y2> [duration(ms)] (Default: touchscreen)");
        out.println("      draganddrop <x1> <y1> <x2> <y2> [duration(ms)] (Default: touchscreen)");
        out.println("      press (Default: trackball)");
        out.println("      roll <dx> <dy> (Default: trackball)");
        out.println("      event <DOWN|UP|MOVE> <x> <y> (Default: touchscreen)");
    }
}
