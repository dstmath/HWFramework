package android.hardware.camera2.utils;

import java.util.List;

public class ListUtils {
    public static <T> boolean listContains(List<T> list, T needle) {
        if (list == null) {
            return false;
        }
        return list.contains(needle);
    }

    public static <T> boolean listElementsEqualTo(List<T> list, T single) {
        boolean z = false;
        if (list == null) {
            return false;
        }
        if (list.size() == 1) {
            z = list.contains(single);
        }
        return z;
    }

    public static <T> String listToString(List<T> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int size = list.size();
        int i = 0;
        for (T elem : list) {
            sb.append(elem);
            if (i != size - 1) {
                sb.append(',');
            }
            i++;
        }
        sb.append(']');
        return sb.toString();
    }

    public static <T> T listSelectFirstFrom(List<T> list, T[] choices) {
        if (list == null) {
            return null;
        }
        for (T choice : choices) {
            if (list.contains(choice)) {
                return choice;
            }
        }
        return null;
    }

    private ListUtils() {
        throw new AssertionError();
    }
}
