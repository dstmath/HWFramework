package com.android.server.hidata.channelqoe;

public class HwChannelQoEParmStatistics {
    public int mNetworkType = -1;
    public Rst mRst = new Rst();
    public Rtt mRtt = new Rtt();
    public Svr mSvr = new Svr();
    public Tpt mTpt = new Tpt();
    public DRtt mdRtt = new DRtt();

    public static class DRtt {
        public int[] reserved = new int[10];

        public void reset() {
            for (int i = 0; i < this.reserved.length; i++) {
                this.reserved[i] = 0;
            }
        }
    }

    public static class Rst {
        public int[] mRst = new int[10];

        public Rst() {
            for (int i = 0; i < this.mRst.length; i++) {
                this.mRst[i] = 0;
            }
        }

        public void reset() {
            for (int i = 0; i < this.mRst.length; i++) {
                this.mRst[i] = 0;
            }
        }
    }

    public static class Rtt {
        public int[] mRtt = new int[10];

        public Rtt() {
            for (int i = 0; i < this.mRtt.length; i++) {
                this.mRtt[i] = 0;
            }
        }

        public void reset() {
            for (int i = 0; i < this.mRtt.length; i++) {
                this.mRtt[i] = 0;
            }
        }
    }

    public static class Svr {
        public int[] mSvr = new int[5];

        public Svr() {
            for (int i = 0; i < this.mSvr.length; i++) {
                this.mSvr[i] = 0;
            }
        }

        public void reset() {
            for (int i = 0; i < this.mSvr.length; i++) {
                this.mSvr[i] = 0;
            }
        }
    }

    public static class Tpt {
        public int[] reserved = new int[10];

        public void reset() {
            for (int i = 0; i < this.reserved.length; i++) {
                this.reserved[i] = 0;
            }
        }
    }

    public void reset() {
        this.mRst.reset();
        this.mSvr.reset();
        this.mRtt.reset();
        this.mdRtt.reset();
        this.mTpt.reset();
    }
}
