package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.optimus.impl.bean.BsBlackWhiteItem;
import tmsdk.common.module.optimus.impl.bean.BsCloudResult;
import tmsdk.common.module.optimus.impl.bean.BsInfo;
import tmsdk.common.module.optimus.impl.bean.BsNeighborCell;
import tmsdk.common.module.optimus.impl.bean.BsResult;

public class mw {
    public static BsBlackWhiteItem a(nb nbVar) {
        BsBlackWhiteItem bsBlackWhiteItem = new BsBlackWhiteItem();
        bsBlackWhiteItem.cid = nbVar.iCid;
        bsBlackWhiteItem.lac = nbVar.iLac;
        bsBlackWhiteItem.mnc = (short) nbVar.sMnc;
        return bsBlackWhiteItem;
    }

    public static BsCloudResult a(nd ndVar) {
        BsCloudResult bsCloudResult = new BsCloudResult();
        bsCloudResult.setCloudFakeType(ndVar.BL);
        bsCloudResult.smsType = (short) ((short) ndVar.BN);
        bsCloudResult.cloudScore = ndVar.BM;
        bsCloudResult.lastSmsIsFake = ndVar.BE;
        return bsCloudResult;
    }

    public static my a(BsInfo bsInfo) {
        my myVar = new my();
        myVar.Bz = b(bsInfo.cloudResult);
        myVar.iCid = bsInfo.iCid;
        myVar.iLac = bsInfo.iLac;
        myVar.By = a(bsInfo.localResult);
        myVar.luLoc = bsInfo.luLoc;
        myVar.sBsss = (short) bsInfo.sBsss;
        myVar.sDataState = (short) bsInfo.sDataState;
        myVar.sMcc = (short) bsInfo.sMcc;
        myVar.sMnc = (short) bsInfo.sMnc;
        myVar.sNetworkType = (short) bsInfo.sNetworkType;
        myVar.sNumNeighbors = (short) bsInfo.sNumNeighbors;
        myVar.uTimeInSeconds = bsInfo.uTimeInSeconds;
        myVar.vecNeighbors = n(bsInfo.vecNeighbors);
        return myVar;
    }

    public static mz a(BsResult bsResult) {
        boolean z = true;
        mz mzVar = new mz();
        mzVar.BD = bsResult.fakeType.mValue;
        if (bsResult.lastSmsIsFake != 1) {
            z = false;
        }
        mzVar.BE = z;
        return mzVar;
    }

    public static na a(BsNeighborCell bsNeighborCell) {
        na naVar = new na();
        naVar.iCid = bsNeighborCell.cid;
        naVar.iLac = bsNeighborCell.lac;
        naVar.sBsss = (short) bsNeighborCell.bsss;
        naVar.sNetworkType = (short) bsNeighborCell.networkType;
        return naVar;
    }

    public static nd b(BsCloudResult bsCloudResult) {
        nd ndVar = new nd();
        ndVar.BL = bsCloudResult.cloudFakeType.mValue;
        ndVar.BN = bsCloudResult.smsType;
        ndVar.BM = bsCloudResult.cloudScore;
        ndVar.BE = bsCloudResult.lastSmsIsFake;
        return ndVar;
    }

    public static ArrayList<my> l(List<BsInfo> list) {
        ArrayList<my> arrayList = new ArrayList();
        if (list != null) {
            for (BsInfo a : list) {
                arrayList.add(a(a));
            }
        }
        return arrayList;
    }

    public static ArrayList<BsBlackWhiteItem> m(List<nb> list) {
        ArrayList<BsBlackWhiteItem> arrayList = new ArrayList();
        if (list != null) {
            for (nb a : list) {
                arrayList.add(a(a));
            }
        }
        return arrayList;
    }

    public static ArrayList<na> n(List<BsNeighborCell> list) {
        ArrayList<na> arrayList = new ArrayList();
        if (list != null) {
            for (BsNeighborCell a : list) {
                arrayList.add(a(a));
            }
        }
        return arrayList;
    }
}
