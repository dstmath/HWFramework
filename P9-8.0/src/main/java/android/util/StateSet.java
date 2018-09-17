package android.util;

import com.android.internal.R;

public class StateSet {
    public static final int[] NOTHING = new int[]{0};
    public static final int VIEW_STATE_ACCELERATED = 64;
    public static final int VIEW_STATE_ACTIVATED = 32;
    public static final int VIEW_STATE_DRAG_CAN_ACCEPT = 256;
    public static final int VIEW_STATE_DRAG_HOVERED = 512;
    public static final int VIEW_STATE_ENABLED = 8;
    public static final int VIEW_STATE_FOCUSED = 4;
    public static final int VIEW_STATE_HOVERED = 128;
    static final int[] VIEW_STATE_IDS = new int[]{R.attr.state_window_focused, 1, R.attr.state_selected, 2, R.attr.state_focused, 4, R.attr.state_enabled, 8, R.attr.state_pressed, 16, R.attr.state_activated, 32, R.attr.state_accelerated, 64, R.attr.state_hovered, 128, R.attr.state_drag_can_accept, 256, R.attr.state_drag_hovered, 512};
    public static final int VIEW_STATE_PRESSED = 16;
    public static final int VIEW_STATE_SELECTED = 2;
    private static final int[][] VIEW_STATE_SETS = new int[(1 << (VIEW_STATE_IDS.length / 2))][];
    public static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    public static final int[] WILD_CARD = new int[0];

    static {
        if (VIEW_STATE_IDS.length / 2 != R.styleable.ViewDrawableStates.length) {
            throw new IllegalStateException("VIEW_STATE_IDs array length does not match ViewDrawableStates style array");
        }
        int i;
        int j;
        int[] orderedIds = new int[VIEW_STATE_IDS.length];
        for (i = 0; i < R.styleable.ViewDrawableStates.length; i++) {
            int viewState = R.styleable.ViewDrawableStates[i];
            for (j = 0; j < VIEW_STATE_IDS.length; j += 2) {
                if (VIEW_STATE_IDS[j] == viewState) {
                    orderedIds[i * 2] = viewState;
                    orderedIds[(i * 2) + 1] = VIEW_STATE_IDS[j + 1];
                }
            }
        }
        for (i = 0; i < VIEW_STATE_SETS.length; i++) {
            int[] set = new int[Integer.bitCount(i)];
            int pos = 0;
            for (j = 0; j < orderedIds.length; j += 2) {
                if ((orderedIds[j + 1] & i) != 0) {
                    int pos2 = pos + 1;
                    set[pos] = orderedIds[j];
                    pos = pos2;
                }
            }
            VIEW_STATE_SETS[i] = set;
        }
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
        int stateSetSize = stateSet.length;
        for (int stateSpecState : stateSpec) {
            int stateSpecState2;
            if (stateSpecState2 == 0) {
                return true;
            }
            boolean mustMatch;
            if (stateSpecState2 > 0) {
                mustMatch = true;
            } else {
                mustMatch = false;
                stateSpecState2 = -stateSpecState2;
            }
            boolean found = false;
            int j = 0;
            while (j < stateSetSize) {
                int state = stateSet[j];
                if (state == 0) {
                    if (mustMatch) {
                        return false;
                    }
                } else if (state != stateSpecState2) {
                    j++;
                } else if (!mustMatch) {
                    return false;
                } else {
                    found = true;
                }
                if (!mustMatch && (found ^ 1) != 0) {
                    return false;
                }
            }
            if (!mustMatch) {
            }
        }
        return true;
    }

    public static boolean stateSetMatches(int[] stateSpec, int state) {
        for (int stateSpecState : stateSpec) {
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
