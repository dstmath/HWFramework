package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.base.operation.Operation;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HciConfigDataParser {
    private List<String> hciConfigOriData;
    private Map<String, String> localHciConfigData = new HashMap();
    private Context mContext;

    HciConfigDataParser(Context context, List<String> hciConfigOriData2) {
        this.mContext = context;
        this.hciConfigOriData = hciConfigOriData2;
    }

    /* access modifiers changed from: package-private */
    public List<HciConfigInfo> parseHciConfigData(String productId) throws AppletCardException {
        String jsonData = findHciReservedField();
        if (StringUtil.isEmpty(jsonData, true)) {
            loadLocalHciConfigDatas();
            jsonData = this.localHciConfigData.get(productId);
        }
        if (!StringUtil.isEmpty(jsonData, true)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                if (jsonObject.has(Constants.FIELD_HCI_CONFIG_HCI)) {
                    JSONArray jArray = jsonObject.getJSONArray(Constants.FIELD_HCI_CONFIG_HCI);
                    List<HciConfigInfo> datas = new ArrayList<>();
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jObj = jArray.getJSONObject(i);
                        HciConfigInfo hciConfigInfo = new HciConfigInfo();
                        if (jObj.has(Constants.FIELD_HCI_CONFIG_VERSION)) {
                            hciConfigInfo.setVersion(jObj.getString(Constants.FIELD_HCI_CONFIG_VERSION));
                        }
                        if (jObj.has(Constants.FIELD_HCI_CONFIG_TLVHCI)) {
                            hciConfigInfo.setTlvhcioffset(jObj.getInt(Constants.FIELD_HCI_CONFIG_TLVHCI));
                        }
                        if (jObj.has("info")) {
                            parseOperationJsonArray(jObj.getJSONArray("info"), hciConfigInfo);
                        }
                        datas.add(hciConfigInfo);
                    }
                    return datas;
                }
                throw new AppletCardException(2, "can not find hci tag in config data.");
            } catch (JSONException e) {
                throw new AppletCardException(2, "parseHciConfigData JSONException msg : " + e.getMessage());
            }
        } else {
            throw new AppletCardException(2, "can not find hci config data from card product info and local config data.");
        }
    }

    private void parseOperationJsonArray(JSONArray operationsJson, HciConfigInfo hciConfigInfo) throws AppletCardException {
        if (operationsJson == null || hciConfigInfo == null) {
            throw new AppletCardException(1, "parseOperationJsonArray param is illegal.");
        }
        int i = 0;
        while (i < operationsJson.length()) {
            try {
                JSONObject jObj = operationsJson.getJSONObject(i);
                String type = null;
                if (jObj.has(Constants.FIELD_HCI_CONFIG_DATA_TYPE)) {
                    type = jObj.getString(Constants.FIELD_HCI_CONFIG_DATA_TYPE);
                }
                if (!StringUtil.isEmpty(type, true)) {
                    List<Operation> operations = null;
                    if (jObj.has("op")) {
                        operations = OperationGenerator.parseOperations(jObj.getString("op"));
                    }
                    if (operations != null && !operations.isEmpty()) {
                        hciConfigInfo.addOperations(type, operations);
                    }
                }
                i++;
            } catch (JSONException e) {
                throw new AppletCardException(2, "parseOperationJsonArray failed. msg : " + e.getMessage());
            }
        }
    }

    private String findHciReservedField() {
        if (this.hciConfigOriData == null || this.hciConfigOriData.isEmpty()) {
            return null;
        }
        for (int i = this.hciConfigOriData.size() - 1; i >= 0; i--) {
            String reserved = this.hciConfigOriData.get(i);
            if (!StringUtil.isEmpty(reserved, true) && reserved.contains(Constants.FIELD_HCI_CONFIG_HCI)) {
                return reserved;
            }
        }
        return null;
    }

    private void loadLocalHciConfigDatas() {
        LogX.i("loadLocalHciConfigDatas begin");
        Properties prop = new Properties();
        InputStream in = null;
        try {
            InputStream in2 = this.mContext.getAssets().open("consume_hci_parse_config_data.properties");
            prop.load(in2);
            this.localHciConfigData.clear();
            for (String productId : prop.keySet()) {
                this.localHciConfigData.put(productId, prop.getProperty(productId));
            }
            LogX.i("loadLocalHciConfigDatas load data cnt : " + this.localHciConfigData.size());
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException e) {
                    LogX.i("processTask close stream error1");
                }
            }
        } catch (IOException e2) {
            LogX.i("loadLocalHciConfigDatas failed. IOException happened. msg : " + e2.getMessage(), true);
            if (in != null) {
                in.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    LogX.i("processTask close stream error1");
                }
            }
            throw th;
        }
    }
}
