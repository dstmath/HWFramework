package tmsdk.common.module.optimus.impl.bean;

import java.util.ArrayList;

/* compiled from: Unknown */
public class BsInfo {
    public BsCloudResult cloudResult;
    public int iCid;
    public int iLac;
    public BsResult localResult;
    public long luLoc;
    public short sBsss;
    public short sDataState;
    public short sMcc;
    public short sMnc;
    public short sNetworkType;
    public short sNumNeighbors;
    public long uTimeInSeconds;
    public ArrayList<BsNeighborCell> vecNeighbors;

    public BsInfo() {
        this.uTimeInSeconds = 0;
        this.sNetworkType = (short) 0;
        this.sDataState = (short) 0;
        this.iCid = 0;
        this.iLac = 0;
        this.luLoc = 0;
        this.sBsss = (short) 0;
        this.sMcc = (short) 0;
        this.sMnc = (short) 0;
        this.sNumNeighbors = (short) 0;
        this.vecNeighbors = null;
        this.localResult = null;
        this.cloudResult = null;
    }
}
