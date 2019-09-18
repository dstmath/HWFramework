package com.android.server.hidata.wavemapping.statehandler;

public class HwWmpFastBackLte {
    public int mCellId = -1;
    public int mEarfcn = -1;
    public int mLai = -1;
    public int mPlmnId = -1;
    public int mRat = -1;
    public int mSubId = -1;

    public void setRat(String sRat) {
        if (sRat != null) {
            if (sRat.equals("4G")) {
                this.mRat = 7;
            } else if (sRat.equals("3G")) {
                this.mRat = 2;
            } else if (sRat.equals("2G")) {
                this.mRat = 0;
            } else if (sRat.equals("CDMA")) {
                this.mRat = 0;
            }
        }
    }

    public void copyObjectValue(HwWmpFastBackLte tempWmBack2Lte) {
        if (tempWmBack2Lte != null) {
            this.mSubId = tempWmBack2Lte.mSubId;
            this.mPlmnId = tempWmBack2Lte.mPlmnId;
            this.mRat = tempWmBack2Lte.mRat;
            this.mEarfcn = tempWmBack2Lte.mEarfcn;
            this.mLai = tempWmBack2Lte.mLai;
            this.mCellId = tempWmBack2Lte.mCellId;
        }
    }
}
