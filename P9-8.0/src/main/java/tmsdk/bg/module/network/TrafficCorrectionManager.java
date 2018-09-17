package tmsdk.bg.module.network;

import android.content.Context;
import java.util.ArrayList;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.ErrorCode;
import tmsdkobf.ic;
import tmsdkobf.kr;
import tmsdkobf.kt;

public final class TrafficCorrectionManager extends BaseManagerB {
    i vM;
    ArrayList<CodeName> vN = new ArrayList();

    public int analysisSMS(int i, String str, String str2, String str3) {
        return !ic.bE() ? this.vM.a(i, str, str2, str3, 0) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public ArrayList<CodeName> getAllBrands() {
        return !ic.bE() ? b.getAllBrands() : this.vN;
    }

    public ArrayList<CodeName> getAllProvinces() {
        return !ic.bE() ? b.getAllProvinces() : this.vN;
    }

    public ArrayList<CodeName> getBrands(String str) {
        return !ic.bE() ? b.getBrands(str) : this.vN;
    }

    public ArrayList<CodeName> getCarries() {
        return !ic.bE() ? b.getCarries() : this.vN;
    }

    public ArrayList<CodeName> getCities(String str) {
        return !ic.bE() ? b.getCities(str) : this.vN;
    }

    public void onCreate(Context context) {
        this.vM = new i();
        this.vM.onCreate(context);
        a(this.vM);
    }

    public void onImsiChanged() {
        this.vM.onImsiChanged();
    }

    public int requestProfile(int i) {
        return !ic.bE() ? this.vM.requestProfile(i) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public int setConfig(int i, String str, String str2, String str3, String str4, int i2) {
        return !ic.bE() ? this.vM.setConfig(i, str, str2, str3, str4, i2) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public int setTrafficCorrectionListener(ITrafficCorrectionListener iTrafficCorrectionListener) {
        return !ic.bE() ? this.vM.setTrafficCorrectionListener(iTrafficCorrectionListener) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public int startCorrection(int i) {
        if (ic.bE()) {
            return ErrorCode.ERR_LICENSE_EXPIRED;
        }
        kt.saveActionData(29950);
        kr.dz();
        return this.vM.startCorrection(i);
    }
}
