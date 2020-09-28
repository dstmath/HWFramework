package com.android.internal.telephony.cdnr;

import android.os.PersistableBundle;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CarrierConfigEfData implements EfData {
    private static final String TAG = "CarrierConfigEfData";
    private final PersistableBundle mConfig;

    public CarrierConfigEfData(PersistableBundle config) {
        this.mConfig = config;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public String getServiceProviderName() {
        String spn = this.mConfig.getString("carrier_name_string");
        if (TextUtils.isEmpty(spn)) {
            return null;
        }
        return spn;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public int getServiceProviderNameDisplayCondition() {
        return this.mConfig.getInt("spn_display_condition_override_int", -1);
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<String> getServiceProviderDisplayInformation() {
        String[] spdi = this.mConfig.getStringArray("spdi_override_string_array");
        if (spdi != null) {
            return Arrays.asList(spdi);
        }
        return null;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<String> getEhplmnList() {
        String[] ehplmn = this.mConfig.getStringArray("ehplmn_override_string_array");
        if (ehplmn != null) {
            return Arrays.asList(ehplmn);
        }
        return null;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<IccRecords.PlmnNetworkName> getPlmnNetworkNameList() {
        String[] pnn = this.mConfig.getStringArray("pnn_override_string_array");
        List<IccRecords.PlmnNetworkName> pnnList = null;
        if (pnn != null) {
            pnnList = new ArrayList<>(pnn.length);
            for (String pnnStr : pnn) {
                try {
                    String[] names = pnnStr.split("\\s*,\\s*");
                    pnnList.add(new IccRecords.PlmnNetworkName(names[0], names.length > 1 ? names[1] : PhoneConfigurationManager.SSSS));
                } catch (Exception e) {
                    Rlog.e(TAG, "CarrierConfig wrong pnn format, pnnStr = " + pnnStr);
                }
            }
        }
        return pnnList;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<IccRecords.OperatorPlmnInfo> getOperatorPlmnList() {
        String[] opl = this.mConfig.getStringArray("opl_override_opl_string_array");
        List<IccRecords.OperatorPlmnInfo> oplList = null;
        if (opl != null) {
            oplList = new ArrayList<>(opl.length);
            for (String oplStr : opl) {
                try {
                    String[] info = oplStr.split("\\s*,\\s*");
                    oplList.add(new IccRecords.OperatorPlmnInfo(info[0], Integer.parseInt(info[1]), Integer.parseInt(info[2]), Integer.parseInt(info[3])));
                } catch (Exception e) {
                    Rlog.e(TAG, "CarrierConfig wrong opl format, oplStr = " + oplStr);
                }
            }
        }
        return oplList;
    }
}
