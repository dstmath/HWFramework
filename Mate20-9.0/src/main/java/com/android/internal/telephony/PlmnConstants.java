package com.android.internal.telephony;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class PlmnConstants {
    private static final String HANT = "Hant";
    static final String LOG_TAG = "GSM/CDMA";
    private static final String ZH_CN = "zh_CN";
    private static final String ZH_HT = "zh_HT";
    static final String mncAndMccTag = "#";
    static final int plmnNumber = 3;
    private ArrayList<String[]> carrierCfList = new ArrayList<>();

    public PlmnConstants(String data) {
        loadConfigFile(data);
    }

    public String getPlmnValue(String mncAndMccTemp, String contryCodeTemp) {
        if (ZH_CN.equals(contryCodeTemp) && HANT.equals(Locale.getDefault().getScript())) {
            contryCodeTemp = ZH_HT;
        }
        Iterator<String[]> iteratorTemp = this.carrierCfList.iterator();
        while (iteratorTemp.hasNext()) {
            String[] datas = iteratorTemp.next();
            if (datas.length == 3) {
                String[] mncAndMcc = datas[0].split(mncAndMccTag);
                String contryCode = datas[1];
                String plmn = datas[2];
                for (int i = 0; i < mncAndMcc.length; i++) {
                    if (mncAndMccTemp != null && mncAndMccTemp.equals(mncAndMcc[i]) && contryCodeTemp != null && contryCodeTemp.equalsIgnoreCase(contryCode)) {
                        return plmn;
                    }
                }
                continue;
            }
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
