package tmsdk.bg.module.network;

import android.content.Context;
import java.util.ArrayList;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.ErrorCode;
import tmsdkobf.jg;
import tmsdkobf.ly;
import tmsdkobf.ma;

/* compiled from: Unknown */
public final class TrafficCorrectionManager extends BaseManagerB {
    i yG;
    ArrayList<CodeName> yH;

    public TrafficCorrectionManager() {
        this.yH = new ArrayList();
    }

    public int analysisSMS(int i, String str, String str2, String str3) {
        return !jg.cl() ? this.yG.a(i, str, str2, str3, 0) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public ArrayList<CodeName> getAllProvinces() {
        return !jg.cl() ? b.getAllProvinces() : this.yH;
    }

    public ArrayList<CodeName> getBrands(String str) {
        return !jg.cl() ? b.getBrands(str) : this.yH;
    }

    public ArrayList<CodeName> getCarries() {
        return !jg.cl() ? b.getCarries() : this.yH;
    }

    public ArrayList<CodeName> getCities(String str) {
        return !jg.cl() ? b.getCities(str) : this.yH;
    }

    public void onCreate(Context context) {
        this.yG = new i();
        this.yG.onCreate(context);
        a(this.yG);
    }

    public int requestProfile(int i) {
        return !jg.cl() ? this.yG.requestProfile(i) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public int setConfig(int i, String str, String str2, String str3, String str4, int i2) {
        return !jg.cl() ? this.yG.setConfig(i, str, str2, str3, str4, i2) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public int setTrafficCorrectionListener(ITrafficCorrectionListener iTrafficCorrectionListener) {
        return !jg.cl() ? this.yG.setTrafficCorrectionListener(iTrafficCorrectionListener) : ErrorCode.ERR_LICENSE_EXPIRED;
    }

    public int startCorrection(int i) {
        if (jg.cl()) {
            return ErrorCode.ERR_LICENSE_EXPIRED;
        }
        ma.bx(29950);
        ly.ep();
        return this.yG.startCorrection(i);
    }
}
