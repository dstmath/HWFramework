package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.optimus.impl.bean.BsBlackWhiteItem;
import tmsdk.common.module.optimus.impl.bean.BsCloudResult;
import tmsdk.common.module.optimus.impl.bean.BsInfo;
import tmsdk.common.module.optimus.impl.bean.BsNeighborCell;
import tmsdk.common.module.optimus.impl.bean.BsResult;

/* compiled from: Unknown */
public class oa {
    public static BsBlackWhiteItem a(of ofVar) {
        BsBlackWhiteItem bsBlackWhiteItem = new BsBlackWhiteItem();
        bsBlackWhiteItem.cid = ofVar.iCid;
        bsBlackWhiteItem.lac = ofVar.iLac;
        bsBlackWhiteItem.mnc = (short) ofVar.sMnc;
        return bsBlackWhiteItem;
    }

    public static BsCloudResult a(oh ohVar) {
        BsCloudResult bsCloudResult = new BsCloudResult();
        bsCloudResult.setCloudFakeType(ohVar.Ej);
        bsCloudResult.smsType = (short) ((short) ohVar.El);
        bsCloudResult.cloudScore = ohVar.Ek;
        bsCloudResult.lastSmsIsFake = ohVar.Ec;
        return bsCloudResult;
    }

    public static oc a(BsInfo bsInfo) {
        oc ocVar = new oc();
        ocVar.DX = b(bsInfo.cloudResult);
        ocVar.iCid = bsInfo.iCid;
        ocVar.iLac = bsInfo.iLac;
        ocVar.DW = a(bsInfo.localResult);
        ocVar.luLoc = bsInfo.luLoc;
        ocVar.sBsss = (short) bsInfo.sBsss;
        ocVar.sDataState = (short) bsInfo.sDataState;
        ocVar.sMcc = (short) bsInfo.sMcc;
        ocVar.sMnc = (short) bsInfo.sMnc;
        ocVar.sNetworkType = (short) bsInfo.sNetworkType;
        ocVar.sNumNeighbors = (short) bsInfo.sNumNeighbors;
        ocVar.uTimeInSeconds = bsInfo.uTimeInSeconds;
        ocVar.vecNeighbors = v(bsInfo.vecNeighbors);
        return ocVar;
    }

    public static od a(BsResult bsResult) {
        boolean z = true;
        od odVar = new od();
        odVar.Eb = bsResult.fakeType.mValue;
        if (bsResult.lastSmsIsFake != 1) {
            z = false;
        }
        odVar.Ec = z;
        return odVar;
    }

    public static oe a(BsNeighborCell bsNeighborCell) {
        oe oeVar = new oe();
        oeVar.iCid = bsNeighborCell.cid;
        oeVar.iLac = bsNeighborCell.lac;
        oeVar.sBsss = (short) bsNeighborCell.bsss;
        oeVar.sNetworkType = (short) bsNeighborCell.networkType;
        return oeVar;
    }

    public static oh b(BsCloudResult bsCloudResult) {
        oh ohVar = new oh();
        ohVar.Ej = bsCloudResult.cloudFakeType.mValue;
        ohVar.El = bsCloudResult.smsType;
        ohVar.Ek = bsCloudResult.cloudScore;
        ohVar.Ec = bsCloudResult.lastSmsIsFake;
        return ohVar;
    }

    public static ArrayList<oc> t(List<BsInfo> list) {
        ArrayList<oc> arrayList = new ArrayList();
        if (list != null) {
            for (BsInfo a : list) {
                arrayList.add(a(a));
            }
        }
        return arrayList;
    }

    public static ArrayList<BsBlackWhiteItem> u(List<of> list) {
        ArrayList<BsBlackWhiteItem> arrayList = new ArrayList();
        if (list != null) {
            for (of a : list) {
                arrayList.add(a(a));
            }
        }
        return arrayList;
    }

    public static ArrayList<oe> v(List<BsNeighborCell> list) {
        ArrayList<oe> arrayList = new ArrayList();
        if (list != null) {
            for (BsNeighborCell a : list) {
                arrayList.add(a(a));
            }
        }
        return arrayList;
    }
}
