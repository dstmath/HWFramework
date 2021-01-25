package ohos.ace.ability;

import com.huawei.ace.plugin.internal.PluginErrorCode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import ohos.agp.window.service.WindowManager;
import ohos.bluetooth.BluetoothDeviceClass;
import ohos.com.sun.org.apache.xerces.internal.parsers.XMLGrammarCachingConfiguration;
import ohos.devtools.JLogConstants;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.TouchEvent;

public class AceEventProcessor {
    private static final int BYTES_PER_FIELD = 8;
    private static final int MOUSE_DATA_FIELD_COUNT = 13;
    private static final int POINTER_DATA_FIELD_COUNT = 10;
    private static final Map<Integer, Integer> Z2A_KEYMAP = new HashMap();

    private static int actionToActionType(int i) {
        if (i == 1) {
            return 4;
        }
        if (i == 2) {
            return 6;
        }
        if (i == 4) {
            return 4;
        }
        if (i == 5) {
            return 6;
        }
        if (i == 3) {
            return 5;
        }
        return i == 6 ? 0 : -1;
    }

    public static int keyActionToActionType(boolean z) {
        return z ? 0 : 1;
    }

    private static class ActionType {
        static final int ADD = 1;
        static final int CANCEL = 0;
        static final int DOWN = 4;
        static final int HOVER = 3;
        static final int MOVE = 5;
        static final int REMOVE = 2;
        static final int UP = 6;

        private ActionType() {
        }
    }

    private static class KeyActionType {
        static final int DOWN = 0;
        static final int UP = 1;

        private KeyActionType() {
        }
    }

    private static class KeyCodeOfCar {
        static final int KEYCODE_CUSTOM1 = 10001;
        static final int KEYCODE_LAUNCHER_MENU = 10009;
        static final int KEYCODE_LEFT_KNOB = 10004;
        static final int KEYCODE_LEFT_KNOB_ROLL_DOWN = 10003;
        static final int KEYCODE_LEFT_KNOB_ROLL_UP = 10002;
        static final int KEYCODE_RIGHT_KNOB = 10007;
        static final int KEYCODE_RIGHT_KNOB_ROLL_DOWN = 10006;
        static final int KEYCODE_RIGHT_KNOB_ROLL_UP = 10005;
        static final int KEYCODE_VOICE_SOURCE_SWITCH = 10008;

        private KeyCodeOfCar() {
        }
    }

    static {
        Z2A_KEYMAP.put(2067, 82);
        Z2A_KEYMAP.put(1, 3);
        Z2A_KEYMAP.put(2, 4);
        Z2A_KEYMAP.put(Integer.valueOf((int) PluginErrorCode.FA_START_ABILITY_RET_FAILED), 19);
        Z2A_KEYMAP.put(Integer.valueOf((int) PluginErrorCode.FA_CONTINUE_ABILITY_RET_FAILED), 20);
        Z2A_KEYMAP.put(Integer.valueOf((int) PluginErrorCode.FA_FINISH_ABILITY_RET_FAILED), 21);
        Z2A_KEYMAP.put(2015, 22);
        Z2A_KEYMAP.put(2016, 23);
        Z2A_KEYMAP.put(16, 24);
        Z2A_KEYMAP.put(17, 25);
        Z2A_KEYMAP.put(18, 26);
        Z2A_KEYMAP.put(2054, 66);
        Z2A_KEYMAP.put(3, 5);
        Z2A_KEYMAP.put(4, 6);
        Z2A_KEYMAP.put(5, 28);
        Z2A_KEYMAP.put(6, 79);
        Z2A_KEYMAP.put(7, 80);
        Z2A_KEYMAP.put(8, 83);
        Z2A_KEYMAP.put(9, 84);
        Z2A_KEYMAP.put(10, 85);
        Z2A_KEYMAP.put(11, 86);
        Z2A_KEYMAP.put(12, 87);
        Z2A_KEYMAP.put(13, 88);
        Z2A_KEYMAP.put(14, 89);
        Z2A_KEYMAP.put(15, 90);
        Z2A_KEYMAP.put(19, 27);
        Z2A_KEYMAP.put(20, 231);
        Z2A_KEYMAP.put(40, 221);
        Z2A_KEYMAP.put(41, 220);
        Z2A_KEYMAP.put(2000, 7);
        Z2A_KEYMAP.put(2001, 8);
        Z2A_KEYMAP.put(2002, 9);
        Z2A_KEYMAP.put(2003, 10);
        Z2A_KEYMAP.put(2004, 11);
        Z2A_KEYMAP.put(2005, 12);
        Z2A_KEYMAP.put(2006, 13);
        Z2A_KEYMAP.put(2007, 14);
        Z2A_KEYMAP.put(Integer.valueOf((int) PluginErrorCode.FA_UNSUBSCRIBE_ABILITY_RET_FAILED), 15);
        Z2A_KEYMAP.put(2009, 16);
        Z2A_KEYMAP.put(2010, 17);
        Z2A_KEYMAP.put(2011, 18);
        Z2A_KEYMAP.put(2017, 29);
        Z2A_KEYMAP.put(2018, 30);
        Z2A_KEYMAP.put(2019, 31);
        Z2A_KEYMAP.put(2020, 32);
        Z2A_KEYMAP.put(2021, 33);
        Z2A_KEYMAP.put(2022, 34);
        Z2A_KEYMAP.put(2023, 35);
        Z2A_KEYMAP.put(2024, 36);
        Z2A_KEYMAP.put(2025, 37);
        Z2A_KEYMAP.put(2026, 38);
        Z2A_KEYMAP.put(2027, 39);
        Z2A_KEYMAP.put(2028, 40);
        Z2A_KEYMAP.put(2029, 41);
        Z2A_KEYMAP.put(2030, 42);
        Z2A_KEYMAP.put(2031, 43);
        Z2A_KEYMAP.put(2032, 44);
        Z2A_KEYMAP.put(2033, 45);
        Z2A_KEYMAP.put(2034, 46);
        Z2A_KEYMAP.put(2035, 47);
        Z2A_KEYMAP.put(2036, 48);
        Z2A_KEYMAP.put(Integer.valueOf((int) WindowManager.LayoutConfig.MOD_PRESENTATION), 49);
        Z2A_KEYMAP.put(2038, 50);
        Z2A_KEYMAP.put(Integer.valueOf((int) XMLGrammarCachingConfiguration.BIG_PRIME), 51);
        Z2A_KEYMAP.put(2040, 52);
        Z2A_KEYMAP.put(2041, 53);
        Z2A_KEYMAP.put(2042, 54);
        Z2A_KEYMAP.put(2043, 55);
        Z2A_KEYMAP.put(2044, 56);
        Z2A_KEYMAP.put(2045, 57);
        Z2A_KEYMAP.put(2046, 58);
        Z2A_KEYMAP.put(2047, 59);
        Z2A_KEYMAP.put(2048, 60);
        Z2A_KEYMAP.put(2049, 61);
        Z2A_KEYMAP.put(2050, 62);
        Z2A_KEYMAP.put(2051, 63);
        Z2A_KEYMAP.put(Integer.valueOf((int) BluetoothDeviceClass.MajorMinorClass.TOY_ROBOT), 64);
        Z2A_KEYMAP.put(2053, 65);
        Z2A_KEYMAP.put(2055, 67);
        Z2A_KEYMAP.put(Integer.valueOf((int) BluetoothDeviceClass.MajorMinorClass.TOY_VEHICLE), 68);
        Z2A_KEYMAP.put(2057, 69);
        Z2A_KEYMAP.put(2058, 70);
        Z2A_KEYMAP.put(2059, 71);
        Z2A_KEYMAP.put(Integer.valueOf((int) BluetoothDeviceClass.MajorMinorClass.TOY_DOLL_ACTION_FIGURE), 72);
        Z2A_KEYMAP.put(2061, 73);
        Z2A_KEYMAP.put(2062, 74);
        Z2A_KEYMAP.put(2063, 75);
        Z2A_KEYMAP.put(Integer.valueOf((int) BluetoothDeviceClass.MajorMinorClass.TOY_CONTROLLER), 76);
        Z2A_KEYMAP.put(2065, 77);
        Z2A_KEYMAP.put(2066, 81);
        Z2A_KEYMAP.put(2067, 82);
        Z2A_KEYMAP.put(Integer.valueOf((int) BluetoothDeviceClass.MajorMinorClass.TOY_GAME), 92);
        Z2A_KEYMAP.put(2069, 93);
        Z2A_KEYMAP.put(2070, 111);
        Z2A_KEYMAP.put(2071, 112);
        Z2A_KEYMAP.put(2072, 113);
        Z2A_KEYMAP.put(2073, 114);
        Z2A_KEYMAP.put(2074, 115);
        Z2A_KEYMAP.put(2075, 116);
        Z2A_KEYMAP.put(2076, 117);
        Z2A_KEYMAP.put(2077, 118);
        Z2A_KEYMAP.put(2078, 119);
        Z2A_KEYMAP.put(2079, 120);
        Z2A_KEYMAP.put(2080, 121);
        Z2A_KEYMAP.put(2081, 122);
        Z2A_KEYMAP.put(2082, 123);
        Z2A_KEYMAP.put(2083, 124);
        Z2A_KEYMAP.put(2084, 125);
        Z2A_KEYMAP.put(2085, 126);
        Z2A_KEYMAP.put(2086, 127);
        Z2A_KEYMAP.put(2087, 128);
        Z2A_KEYMAP.put(2088, 129);
        Z2A_KEYMAP.put(2089, 130);
        Z2A_KEYMAP.put(2090, 131);
        Z2A_KEYMAP.put(2091, 132);
        Z2A_KEYMAP.put(2092, 133);
        Z2A_KEYMAP.put(2093, 134);
        Z2A_KEYMAP.put(2094, 135);
        Z2A_KEYMAP.put(2095, 136);
        Z2A_KEYMAP.put(2096, 137);
        Z2A_KEYMAP.put(2097, 138);
        Z2A_KEYMAP.put(2098, 139);
        Z2A_KEYMAP.put(2099, 140);
        Z2A_KEYMAP.put(2100, 141);
        Z2A_KEYMAP.put(2101, 142);
        Z2A_KEYMAP.put(2102, 143);
        Z2A_KEYMAP.put(2103, 144);
        Z2A_KEYMAP.put(2104, 145);
        Z2A_KEYMAP.put(2105, 146);
        Z2A_KEYMAP.put(2106, 147);
        Z2A_KEYMAP.put(2107, 148);
        Z2A_KEYMAP.put(2108, 149);
        Z2A_KEYMAP.put(2109, 150);
        Z2A_KEYMAP.put(2110, 151);
        Z2A_KEYMAP.put(2111, 152);
        Z2A_KEYMAP.put(2112, 153);
        Z2A_KEYMAP.put(2113, 154);
        Z2A_KEYMAP.put(2114, 155);
        Z2A_KEYMAP.put(2115, 156);
        Z2A_KEYMAP.put(2116, 157);
        Z2A_KEYMAP.put(2117, 158);
        Z2A_KEYMAP.put(2118, 159);
        Z2A_KEYMAP.put(2119, 160);
        Z2A_KEYMAP.put(2120, 161);
        Z2A_KEYMAP.put(2121, 162);
        Z2A_KEYMAP.put(2122, 163);
        Z2A_KEYMAP.put(21, Integer.valueOf((int) JLogConstants.JLID_DISTRIBUTE_FILE_WRITE));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_DISTRIBUTE_FILE_WRITE), Integer.valueOf((int) JLogConstants.JLID_COAUTH_BEGIN));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_COAUTH_BEGIN), Integer.valueOf((int) JLogConstants.JLID_COAUTH_SCHEDULER_COMMAND_SEND));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_COAUTH_SCHEDULER_COMMAND_SEND), Integer.valueOf((int) JLogConstants.JLID_COAUTH_SCHEDULER_COMMAND_RECV));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_COAUTH_SCHEDULER_COMMAND_RECV), Integer.valueOf((int) JLogConstants.JLID_COAUTH_EXECUTOR_COMMAND_SEND));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_COAUTH_EXECUTOR_COMMAND_SEND), Integer.valueOf((int) JLogConstants.JLID_COAUTH_EXECUTOR_COMMAND_RECV));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_COAUTH_EXECUTOR_COMMAND_RECV), Integer.valueOf((int) JLogConstants.JLID_COAUTH_DATA_FIRST_FRAME_SEND));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_COAUTH_DATA_FIRST_FRAME_SEND), Integer.valueOf((int) JLogConstants.JLID_COAUTH_ALGORITHM_INIT_COMPLETE));
        Z2A_KEYMAP.put(Integer.valueOf((int) JLogConstants.JLID_COAUTH_ALGORITHM_INIT_COMPLETE), Integer.valueOf((int) JLogConstants.JLID_COAUTH_DATA_FIRST_FRAME_RECV));
    }

    public static ByteBuffer processTouchEvent(TouchEvent touchEvent) {
        int pointerCount = touchEvent.getPointerCount();
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(pointerCount * 10 * 8);
        allocateDirect.order(ByteOrder.LITTLE_ENDIAN);
        int actionToActionType = actionToActionType(touchEvent.getAction());
        if (actionToActionType == 4 || actionToActionType == 6) {
            addTouchEventToBuffer(touchEvent, touchEvent.getIndex(), actionToActionType, allocateDirect);
        } else {
            for (int i = 0; i < pointerCount; i++) {
                addTouchEventToBuffer(touchEvent, touchEvent.getIndex(), actionToActionType, allocateDirect);
            }
        }
        if (allocateDirect.position() % 80 == 0) {
            return allocateDirect;
        }
        throw new AssertionError("Packet position is not on field boundary");
    }

    public static ByteBuffer processMouseEvent(MouseEvent mouseEvent) {
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(104);
        allocateDirect.order(ByteOrder.LITTLE_ENDIAN);
        addMouseEventToBuffer(mouseEvent, allocateDirect);
        if (allocateDirect.position() % 104 == 0) {
            return allocateDirect;
        }
        throw new AssertionError("Packet position is not on field boundary");
    }

    private static void addTouchEventToBuffer(TouchEvent touchEvent, int i, int i2, ByteBuffer byteBuffer) {
        if (i2 != -1) {
            byteBuffer.putLong(touchEvent.getOccurredTime() * 1000);
            byteBuffer.putLong((long) i2);
            byteBuffer.putLong((long) touchEvent.getPointerId(i));
            byteBuffer.putDouble((double) touchEvent.getPointerPosition(i).getX());
            byteBuffer.putDouble((double) touchEvent.getPointerPosition(i).getY());
            byteBuffer.putDouble((double) touchEvent.getForcePrecision());
            byteBuffer.putDouble((double) touchEvent.getMaxForce());
            byteBuffer.putDouble((double) touchEvent.getRadius(i));
            byteBuffer.putLong((long) touchEvent.getSourceDevice());
            byteBuffer.putLong((long) touchEvent.getInputDeviceId());
        }
    }

    private static void addMouseEventToBuffer(MouseEvent mouseEvent, ByteBuffer byteBuffer) {
        int action = mouseEvent.getAction();
        int actionButton = mouseEvent.getActionButton();
        int pressedButtons = mouseEvent.getPressedButtons();
        MmiPoint cursor = mouseEvent.getCursor();
        byteBuffer.putDouble((double) cursor.getX());
        byteBuffer.putDouble((double) cursor.getY());
        byteBuffer.putDouble((double) cursor.getZ());
        byteBuffer.putDouble((double) mouseEvent.getCursorDelta(0));
        byteBuffer.putDouble((double) mouseEvent.getCursorDelta(1));
        byteBuffer.putDouble((double) mouseEvent.getCursorDelta(2));
        byteBuffer.putDouble((double) mouseEvent.getScrollingDelta(0));
        byteBuffer.putDouble((double) mouseEvent.getScrollingDelta(1));
        byteBuffer.putDouble((double) mouseEvent.getScrollingDelta(2));
        byteBuffer.putLong((long) action);
        byteBuffer.putLong((long) actionButton);
        byteBuffer.putLong((long) pressedButtons);
        byteBuffer.putLong(mouseEvent.getOccurredTime() * 1000);
    }

    public static int keyToKeyCode(int i) {
        return Z2A_KEYMAP.containsKey(Integer.valueOf(i)) ? Z2A_KEYMAP.get(Integer.valueOf(i)).intValue() : i;
    }
}
