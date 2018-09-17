package tmsdk.common.module.optimus.impl.bean;

import java.util.List;

public class BsInput {
    public static final int MAX_NUM_NEIGHBORS = 10;
    public short bsss;
    public int cid;
    public short dataState;
    public int lac;
    public long loc;
    public short mcc;
    public short mnc;
    public List<BsNeighborCell> neighbors;
    public short networkType;
    public String sender;
    public String sms;
    public int timeInSeconds;

    public String toString() {
        return "BsInput [timeInSeconds=" + this.timeInSeconds + ", networkType=" + this.networkType + ", dataState=" + this.dataState + ", cid=" + this.cid + ", lac=" + this.lac + ", loc=" + this.loc + ", bsss=" + this.bsss + ", mcc=" + this.mcc + ", mnc=" + this.mnc + ", neighbors=" + this.neighbors + ", sender=" + this.sender + ", sms=" + this.sms + "]";
    }
}
