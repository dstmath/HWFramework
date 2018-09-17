package tmsdk.common.module.optimus.impl.bean;

import java.util.ArrayList;

public class BsInfo {
    public BsCloudResult cloudResult = null;
    public int iCid = 0;
    public int iLac = 0;
    public BsResult localResult = null;
    public long luLoc = 0;
    public short sBsss = (short) 0;
    public short sDataState = (short) 0;
    public short sMcc = (short) 0;
    public short sMnc = (short) 0;
    public short sNetworkType = (short) 0;
    public short sNumNeighbors = (short) 0;
    public long uTimeInSeconds = 0;
    public ArrayList<BsNeighborCell> vecNeighbors = null;
}
