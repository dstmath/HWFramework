package com.android.internal.telephony;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class PlmnConstants {
    static final String LOG_TAG = "GSM/CDMA";
    static final String mncAndMccTag = "#";
    static final int plmnNumber = 3;
    private ArrayList<String[]> carrierCfList = new ArrayList<>();

    public PlmnConstants(String data) {
        loadConfigFile(data);
    }

    public String getPlmnValue(String mncAndMccTemp, String contryCodeTemp) {
        if (TextUtils.isEmpty(mncAndMccTemp) || TextUtils.isEmpty(contryCodeTemp)) {
            return null;
        }
        String countryCodeTempScript = null;
        if (!TextUtils.isEmpty(Locale.getDefault().getScript())) {
            countryCodeTempScript = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getScript();
        }
        Iterator<String[]> iteratorTemp = this.carrierCfList.iterator();
        String countryCodeTempPlmn = null;
        String countryCodeTempScriptPlmn = null;
        while (iteratorTemp.hasNext()) {
            String[] datas = iteratorTemp.next();
            if (datas.length == 3) {
                String[] mncAndMcc = datas[0].split(mncAndMccTag);
                String contryCode = datas[1];
                String plmn = datas[2];
                for (String str : mncAndMcc) {
                    if (mncAndMccTemp.equals(str)) {
                        if (!TextUtils.isEmpty(countryCodeTempScript) && countryCodeTempScript.equalsIgnoreCase(contryCode)) {
                            countryCodeTempScriptPlmn = plmn;
                        }
                        if (contryCodeTemp.equalsIgnoreCase(contryCode)) {
                            countryCodeTempPlmn = plmn;
                        }
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(countryCodeTempScriptPlmn)) {
            return countryCodeTempScriptPlmn;
        }
        if (!TextUtils.isEmpty(countryCodeTempPlmn)) {
            return countryCodeTempPlmn;
        }
        return null;
    }

    private void loadConfigFile(String data) {
        if (data != null) {
            String[] dataToArray = data.split(";");
            for (int i = 0; i < dataToArray.length; i++) {
                if (dataToArray[i] != null) {
                    String[] result = dataToArray[i].split(",");
                    if (result.length == 3) {
                        this.carrierCfList.add(result);
                    }
                }
            }
        }
    }
}
