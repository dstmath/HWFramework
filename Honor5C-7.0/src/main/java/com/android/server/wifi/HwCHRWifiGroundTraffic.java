package com.android.server.wifi;

import java.util.ArrayList;
import java.util.List;

public class HwCHRWifiGroundTraffic {
    public static final int BACK_END = 0;
    public static final int FRONT_END = 1;
    private List<HwCHRWifiTrafficItem> new_traffic;
    private List<HwCHRWifiTrafficItem> old_traffic;

    public class HwCHRWifiTrafficItem {
        private long rx;
        private long tx;
        private int type;

        public HwCHRWifiTrafficItem(int type) {
            this.type = HwCHRWifiGroundTraffic.BACK_END;
            this.tx = 0;
            this.rx = 0;
            this.type = type;
        }

        public HwCHRWifiTrafficItem minus(HwCHRWifiTrafficItem right) {
            HwCHRWifiTrafficItem result = new HwCHRWifiTrafficItem(this.type);
            result.rx = minus(this.rx, right.rx);
            result.tx = minus(this.tx, right.tx);
            return result;
        }

        private long minus(long left, long right) {
            if (left - right >= 0) {
                return left - right;
            }
            return (left - right) + Long.MAX_VALUE;
        }

        public void add(long tx, long rx) {
            this.tx += tx;
            this.rx += rx;
        }

        public long getTx() {
            return this.tx;
        }

        public long getRx() {
            return this.rx;
        }

        public int getType() {
            return this.type;
        }
    }

    public HwCHRWifiGroundTraffic() {
        this.old_traffic = null;
        this.new_traffic = null;
        this.old_traffic = new ArrayList();
        this.old_traffic.add(new HwCHRWifiTrafficItem(FRONT_END));
        this.old_traffic.add(new HwCHRWifiTrafficItem(BACK_END));
        this.new_traffic = new ArrayList();
        this.new_traffic.add(new HwCHRWifiTrafficItem(FRONT_END));
        this.new_traffic.add(new HwCHRWifiTrafficItem(BACK_END));
    }

    public void add(int type, long tx, long rx) {
        for (int i = BACK_END; i < this.new_traffic.size(); i += FRONT_END) {
            if (((HwCHRWifiTrafficItem) this.new_traffic.get(i)).getType() == type) {
                ((HwCHRWifiTrafficItem) this.new_traffic.get(i)).add(tx, rx);
            }
        }
    }

    public List<HwCHRWifiTrafficItem> getDelta() {
        List<HwCHRWifiTrafficItem> delta = new ArrayList();
        delta.add(((HwCHRWifiTrafficItem) this.new_traffic.get(BACK_END)).minus((HwCHRWifiTrafficItem) this.old_traffic.get(BACK_END)));
        delta.add(((HwCHRWifiTrafficItem) this.new_traffic.get(FRONT_END)).minus((HwCHRWifiTrafficItem) this.old_traffic.get(FRONT_END)));
        this.old_traffic.clear();
        this.old_traffic.addAll(this.new_traffic);
        this.new_traffic.clear();
        this.new_traffic.add(new HwCHRWifiTrafficItem(FRONT_END));
        this.new_traffic.add(new HwCHRWifiTrafficItem(BACK_END));
        return delta;
    }
}
