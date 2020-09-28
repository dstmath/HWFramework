package huawei.android.view;

import android.annotation.TargetApi;
import android.util.Pair;

@TargetApi(5)
public class HwPositionPair extends Pair<Integer, Integer> implements Comparable<HwPositionPair> {
    public HwPositionPair(Integer first, Integer second) {
        super(first, second);
    }

    public int compareTo(HwPositionPair o) {
        if (((Integer) this.second).intValue() < ((Integer) o.first).intValue()) {
            return -1;
        }
        if (((Integer) this.first).intValue() > ((Integer) o.second).intValue()) {
            return 1;
        }
        return 0;
    }
}
