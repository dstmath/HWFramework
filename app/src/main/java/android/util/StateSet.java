package android.util;

import com.android.internal.R;

public class StateSet {
    public static final int[] NOTHING = null;
    public static final int VIEW_STATE_ACCELERATED = 64;
    public static final int VIEW_STATE_ACTIVATED = 32;
    public static final int VIEW_STATE_DRAG_CAN_ACCEPT = 256;
    public static final int VIEW_STATE_DRAG_HOVERED = 512;
    public static final int VIEW_STATE_ENABLED = 8;
    public static final int VIEW_STATE_FOCUSED = 4;
    public static final int VIEW_STATE_HOVERED = 128;
    static final int[] VIEW_STATE_IDS = null;
    public static final int VIEW_STATE_PRESSED = 16;
    public static final int VIEW_STATE_SELECTED = 2;
    private static final int[][] VIEW_STATE_SETS = null;
    public static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    public static final int[] WILD_CARD = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.StateSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.StateSet.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.StateSet.<clinit>():void");
    }

    public static int[] get(int mask) {
        if (mask < VIEW_STATE_SETS.length) {
            return VIEW_STATE_SETS[mask];
        }
        throw new IllegalArgumentException("Invalid state set mask");
    }

    public static boolean isWildCard(int[] stateSetOrSpec) {
        return stateSetOrSpec.length == 0 || stateSetOrSpec[0] == 0;
    }

    public static boolean stateSetMatches(int[] stateSpec, int[] stateSet) {
        boolean z = true;
        if (stateSet == null) {
            if (stateSpec != null) {
                z = isWildCard(stateSpec);
            }
            return z;
        }
        int stateSpecSize = stateSpec.length;
        int stateSetSize = stateSet.length;
        for (int i = 0; i < stateSpecSize; i += VIEW_STATE_WINDOW_FOCUSED) {
            int stateSpecState = stateSpec[i];
            if (stateSpecState == 0) {
                return true;
            }
            boolean mustMatch;
            if (stateSpecState > 0) {
                mustMatch = true;
            } else {
                mustMatch = false;
                stateSpecState = -stateSpecState;
            }
            boolean found = false;
            int j = 0;
            while (j < stateSetSize) {
                int state = stateSet[j];
                if (state == 0) {
                    if (mustMatch) {
                        return false;
                    }
                } else if (state != stateSpecState) {
                    j += VIEW_STATE_WINDOW_FOCUSED;
                } else if (!mustMatch) {
                    return false;
                } else {
                    found = true;
                }
                if (!mustMatch && !found) {
                    return false;
                }
            }
            if (!mustMatch) {
            }
        }
        return true;
    }

    public static boolean stateSetMatches(int[] stateSpec, int state) {
        int stateSpecSize = stateSpec.length;
        for (int i = 0; i < stateSpecSize; i += VIEW_STATE_WINDOW_FOCUSED) {
            int stateSpecState = stateSpec[i];
            if (stateSpecState == 0) {
                return true;
            }
            if (stateSpecState > 0) {
                if (state != stateSpecState) {
                    return false;
                }
            } else if (state == (-stateSpecState)) {
                return false;
            }
        }
        return true;
    }

    public static int[] trimStateSet(int[] states, int newSize) {
        if (states.length == newSize) {
            return states;
        }
        int[] trimmedStates = new int[newSize];
        System.arraycopy(states, 0, trimmedStates, 0, newSize);
        return trimmedStates;
    }

    public static String dump(int[] states) {
        StringBuilder sb = new StringBuilder();
        int count = states.length;
        for (int i = 0; i < count; i += VIEW_STATE_WINDOW_FOCUSED) {
            switch (states[i]) {
                case R.attr.state_focused /*16842908*/:
                    sb.append("F ");
                    break;
                case R.attr.state_window_focused /*16842909*/:
                    sb.append("W ");
                    break;
                case R.attr.state_enabled /*16842910*/:
                    sb.append("E ");
                    break;
                case R.attr.state_checked /*16842912*/:
                    sb.append("C ");
                    break;
                case R.attr.state_selected /*16842913*/:
                    sb.append("S ");
                    break;
                case R.attr.state_pressed /*16842919*/:
                    sb.append("P ");
                    break;
                case R.attr.state_activated /*16843518*/:
                    sb.append("A ");
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }
}
