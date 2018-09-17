package junit.runner;

import java.util.Vector;

public class Sorter {

    public interface Swapper {
        void swap(Vector vector, int i, int i2);
    }

    public static void sortStrings(Vector values, int left, int right, Swapper swapper) {
        int oleft = left;
        int oright = right;
        String mid = (String) values.elementAt((left + right) / 2);
        while (true) {
            if (((String) values.elementAt(left)).compareTo(mid) < 0) {
                left++;
            } else {
                while (mid.compareTo((String) values.elementAt(right)) < 0) {
                    right--;
                }
                if (left <= right) {
                    swapper.swap(values, left, right);
                    left++;
                    right--;
                }
                if (left > right) {
                    break;
                }
            }
        }
        if (oleft < right) {
            sortStrings(values, oleft, right, swapper);
        }
        if (left < oright) {
            sortStrings(values, left, oright, swapper);
        }
    }
}
