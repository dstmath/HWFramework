package com.android.internal.telephony;

import java.util.ArrayList;
import java.util.Iterator;

public class PlmnConstants {
    static final String LOG_TAG = "GSM/CDMA";
    static final String mncAndMccTag = "#";
    static final int plmnNumber = 3;
    private ArrayList<String[]> carrierCfList = new ArrayList();

    public PlmnConstants(String data) {
        loadConfigFile(data);
    }

    public String getPlmnValue(String mncAndMccTemp, String contryCodeTemp) {
        Iterator<String[]> iteratorTemp = this.carrierCfList.iterator();
        while (iteratorTemp.hasNext()) {
            String[] datas = (String[]) iteratorTemp.next();
            if (datas.length == 3) {
                String[] mncAndMcc = datas[0].split(mncAndMccTag);
                String contryCode = datas[1];
                String plmn = datas[2];
                int i = 0;
                while (i < mncAndMcc.length) {
                    if (mncAndMccTemp != null && mncAndMccTemp.equals(mncAndMcc[i]) && contryCodeTemp != null && contryCodeTemp.equalsIgnoreCase(contryCode)) {
                        return plmn;
                    }
                    i++;
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
