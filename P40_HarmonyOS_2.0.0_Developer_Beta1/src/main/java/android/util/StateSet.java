package android.util;

import com.android.internal.R;

public class StateSet {
    public static final int[] NOTHING = {0};
    public static final int VIEW_STATE_ACCELERATED = 64;
    public static final int VIEW_STATE_ACTIVATED = 32;
    public static final int VIEW_STATE_DRAG_CAN_ACCEPT = 256;
    public static final int VIEW_STATE_DRAG_HOVERED = 512;
    public static final int VIEW_STATE_ENABLED = 8;
    public static final int VIEW_STATE_FOCUSED = 4;
    public static final int VIEW_STATE_HOVERED = 128;
    static final int[] VIEW_STATE_IDS = {16842909, 1, 16842913, 2, 16842908, 4, 16842910, 8, 16842919, 16, 16843518, 32, 16843547, 64, 16843623, 128, 16843624, 256, 16843625, 512};
    public static final int VIEW_STATE_PRESSED = 16;
    public static final int VIEW_STATE_SELECTED = 2;
    private static final int[][] VIEW_STATE_SETS = new int[(1 << (VIEW_STATE_IDS.length / 2))][];
    public static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    public static final int[] WILD_CARD = new int[0];

    static {
        if (VIEW_STATE_IDS.length / 2 == R.styleable.ViewDrawableStates.length) {
            int[] orderedIds = new int[VIEW_STATE_IDS.length];
            for (int i = 0; i < R.styleable.ViewDrawableStates.length; i++) {
                int viewState = R.styleable.ViewDrawableStates[i];
                int j = 0;
                while (true) {
                    int[] iArr = VIEW_STATE_IDS;
                    if (j >= iArr.length) {
                        break;
                    }
                    if (iArr[j] == viewState) {
                        orderedIds[i * 2] = viewState;
                        orderedIds[(i * 2) + 1] = iArr[j + 1];
                    }
                    j += 2;
                }
            }
            for (int i2 = 0; i2 < VIEW_STATE_SETS.length; i2++) {
                int[] set = new int[Integer.bitCount(i2)];
                int pos = 0;
                for (int j2 = 0; j2 < orderedIds.length; j2 += 2) {
                    if ((orderedIds[j2 + 1] & i2) != 0) {
                        set[pos] = orderedIds[j2];
                        pos++;
                    }
                }
                VIEW_STATE_SETS[i2] = set;
            }
            return;
        }
        throw new IllegalStateException("VIEW_STATE_IDs array length does not match ViewDrawableStates style array");
    }

    public static int[] get(int mask) {
        int[][] iArr = VIEW_STATE_SETS;
        if (mask < iArr.length) {
            return iArr[mask];
        }
        throw new IllegalArgumentException("Invalid state set mask");
    }

    public static boolean isWildCard(int[] stateSetOrSpec) {
        return stateSetOrSpec.length == 0 || stateSetOrSpec[0] == 0;
    }

    public static boolean stateSetMatches(int[] stateSpec, int[] stateSet) {
        boolean mustMatch;
        if (stateSet == null) {
            return stateSpec == null || isWildCard(stateSpec);
        }
        int stateSetSize = stateSet.length;
        for (int stateSpecState : stateSpec) {
            if (stateSpecState == 0) {
                return true;
            }
            if (stateSpecState > 0) {
                mustMatch = true;
            } else {
                mustMatch = false;
                stateSpecState = -stateSpecState;
            }
            boolean found = false;
            int j = 0;
            while (true) {
                if (j >= stateSetSize) {
                    break;
                }
                int state = stateSet[j];
                if (state == 0) {
                    if (mustMatch) {
                        return false;
                    }
                } else if (state != stateSpecState) {
                    j++;
                } else if (!mustMatch) {
                    return false;
                } else {
                    found = true;
                }
            }
            if (mustMatch && !found) {
                return false;
            }
        }
        return true;
    }

    public static boolean stateSetMatches(int[] stateSpec, int state) {
        int stateSpecState;
        int stateSpecSize = stateSpec.length;
        int i = 0;
        while (i < stateSpecSize && (stateSpecState = stateSpec[i]) != 0) {
            if (stateSpecState > 0) {
                if (state != stateSpecState) {
                    return false;
                }
            } else if (state == (-stateSpecState)) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static boolean containsAttribute(int[][] stateSpecs, int attr) {
        if (stateSpecs != null) {
            for (int[] spec : stateSpecs) {
                if (spec == null) {
                    break;
                }
                for (int specAttr : spec) {
                    if (specAttr == attr || (-specAttr) == attr) {
                        return true;
                    }
                }
            }
        }
        return false;
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
        for (int i : states) {
            switch (i) {
                case 16842908:
                    sb.append("F ");
                    break;
                case 16842909:
                    sb.append("W ");
                    break;
                case 16842910:
                    sb.append("E ");
                    break;
                case 16842912:
                    sb.append("C ");
                    break;
                case 16842913:
                    sb.append("S ");
                    break;
                case 16842919:
                    sb.append("P ");
                    break;
                case 16843518:
                    sb.append("A ");
                    break;
            }
        }
        return sb.toString();
    }
}
