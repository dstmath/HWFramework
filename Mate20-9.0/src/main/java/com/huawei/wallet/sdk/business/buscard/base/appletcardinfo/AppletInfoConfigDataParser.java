package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.buscard.base.model.ApduCommandInfo;
import com.huawei.wallet.sdk.business.buscard.base.model.ApduSet;
import com.huawei.wallet.sdk.business.buscard.base.operation.Operation;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
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

public class AppletInfoConfigDataParser {
    private static final int QUERY_RECORDS_CNT = 10;
    private static String[] cardInfoFields = {Constants.FIELD_APPLET_CONFIG_NUM, "amount", "date", Constants.FIELD_APPLET_CONFIG_STATUS, Constants.FIELD_APPLET_CONFIG_RECORDS, Constants.FIELD_APPLET_CONFIG_C8_FILE_STATUS, Constants.FIELD_APPLET_CONFIG_IN_OUT_STATION_STATUS, Constants.FIELD_APPLET_CONFIG_RIDE_TIMES, Constants.FIELD_APPLET_CONFIG_CONSUME_RECORDS, Constants.FIELD_APPLET_CONFIG_LOGIC_NUM};
    private List<String> appletConfigOriData;
    private Map<String, String> jsonDataForProduct = new HashMap();
    private Context mContext;

    AppletInfoConfigDataParser(Context context, List<String> appletConfigOriData2) {
        this.mContext = context;
        this.appletConfigOriData = appletConfigOriData2;
    }

    /* access modifiers changed from: package-private */
    public ApduSet parseJson2ApduSet(String productId) throws AppletCardException {
        LogX.i("parseJson2ApduSet begin for " + productId);
        Map<String, String> apduJsonData = new HashMap<>();
        findTargetJsonData(apduJsonData);
        if (apduJsonData.isEmpty()) {
            fetchApduDataJsonFromLocalAssetInfo(productId, apduJsonData);
        }
        ApduSet set = new ApduSet();
        try {
            for (Map.Entry<String, String> entry : apduJsonData.entrySet()) {
                String json = entry.getValue();
                if (!StringUtil.isEmpty(json, true)) {
                    String type = entry.getKey();
                    if (Constants.FIELD_APPLET_CONFIG_RECORDS.equals(type) || Constants.FIELD_APPLET_CONFIG_CONSUME_RECORDS.equals(type)) {
                        set.add(type, parseRecordReadCommands(parseApduInfoJson(type, new JSONArray(json))));
                    } else {
                        set.add(type, parseApduInfoJson(type, new JSONArray(json)));
                    }
                }
            }
            set.compareCardNumAndDateApdus();
            return set;
        } catch (JSONException e) {
            throw new AppletCardException(4, "parse apdu json error. json : " + "" + " msg : " + e.getMessage());
        }
    }

    private void findTargetJsonData(Map<String, String> apduJsonData) throws AppletCardException {
        try {
            for (String s : cardInfoFields) {
                String json = findDataByType(s);
                if (!StringUtil.isEmpty(json, true)) {
                    JSONObject jo = new JSONObject(json);
                    if (jo.has(s)) {
                        apduJsonData.put(s, jo.getString(s));
                    }
                }
            }
        } catch (JSONException e) {
            apduJsonData.clear();
            throw new AppletCardException(4, "parse apdu json error. json : " + null + " msg : " + e.getMessage());
        }
    }

    private String findDataByType(String type) {
        if (this.appletConfigOriData == null || this.appletConfigOriData.isEmpty()) {
            return null;
        }
        for (String s : this.appletConfigOriData) {
            if (s != null && s.contains(type)) {
                return s;
            }
        }
        return null;
    }

    private List<ApduCommand> parseApduInfoJson(String type, JSONArray array) throws AppletCardException {
        List<ApduCommand> apdus = new ArrayList<>();
        int sz = array.length();
        int i = 0;
        while (i < sz) {
            int idx = i + 1;
            try {
                JSONObject obj = array.getJSONObject(i);
                String apduCommand = null;
                try {
                    if (obj.has("apdu")) {
                        try {
                            apduCommand = obj.getString("apdu");
                        } catch (JSONException e) {
                            e = e;
                            throw new AppletCardException(4, "parse apdu json error. " + e.getMessage());
                        }
                    }
                    if (!StringUtil.isEmpty(apduCommand, true)) {
                        String checker = null;
                        if (obj.has(Constants.FIELD_APPLET_CONFIG_CHECKER)) {
                            checker = obj.getString(Constants.FIELD_APPLET_CONFIG_CHECKER);
                        }
                        if (StringUtil.isEmpty(checker, true)) {
                            checker = "9000";
                        }
                        String checker2 = checker.replace(",", "|");
                        String operations = null;
                        List<Operation> ops = null;
                        if (obj.has("op")) {
                            try {
                                operations = obj.getString("op");
                            } catch (JSONException e2) {
                                e = e2;
                                String str = checker2;
                                throw new AppletCardException(4, "parse apdu json error. " + e.getMessage());
                            }
                        }
                        try {
                            if (!StringUtil.isEmpty(operations, true)) {
                                ops = OperationGenerator.parseOperations(operations);
                            }
                            apdu = apdu;
                            JSONObject jSONObject = obj;
                            try {
                                ApduCommandInfo apdu = new ApduCommandInfo(idx, apduCommand, checker2, type, ops);
                                apdus.add(apdu);
                                i++;
                                String str2 = checker2;
                                int i2 = idx;
                            } catch (JSONException e3) {
                                e = e3;
                                String str3 = checker2;
                                throw new AppletCardException(4, "parse apdu json error. " + e.getMessage());
                            }
                        } catch (JSONException e4) {
                            e = e4;
                            JSONObject jSONObject2 = obj;
                            String str4 = checker2;
                            throw new AppletCardException(4, "parse apdu json error. " + e.getMessage());
                        }
                    } else {
                        JSONObject jSONObject3 = obj;
                        try {
                            throw new AppletCardException(2, "apdu json config error");
                        } catch (JSONException e5) {
                            e = e5;
                            throw new AppletCardException(4, "parse apdu json error. " + e.getMessage());
                        }
                    }
                } catch (JSONException e6) {
                    e = e6;
                    JSONObject jSONObject4 = obj;
                    throw new AppletCardException(4, "parse apdu json error. " + e.getMessage());
                }
            } catch (JSONException e7) {
                e = e7;
                throw new AppletCardException(4, "parse apdu json error. " + e.getMessage());
            }
        }
        return apdus;
    }

    private List<ApduCommand> parseRecordReadCommands(List<ApduCommand> commands) {
        List<ApduCommand> commandList = new ArrayList<>();
        int idx = 1;
        for (ApduCommand command : commands) {
            if (command instanceof ApduCommandInfo) {
                ApduCommandInfo commandInfo = (ApduCommandInfo) command;
                commandInfo.setIndex(idx);
                List<Operation> ops = commandInfo.getOperations();
                String apdu = commandInfo.getApdu();
                if (ops == null || !apdu.contains("%")) {
                    commandList.add(commandInfo);
                    idx++;
                } else if (apdu.contains("%")) {
                    String[] dataApdus = apdu.split(",");
                    int length = dataApdus.length;
                    int idx2 = idx;
                    int idx3 = 0;
                    while (idx3 < length) {
                        String apduStr = dataApdus[idx3];
                        int idx4 = idx2;
                        for (int i = 1; i <= 10; i++) {
                            ApduCommandInfo newCommandInfo = new ApduCommandInfo(idx4, String.format(apduStr, new Object[]{Integer.valueOf(i)}), commandInfo.getChecker(), commandInfo.getType(), commandInfo.getOperations());
                            commandList.add(newCommandInfo);
                            idx4++;
                        }
                        idx3++;
                        idx2 = idx4;
                    }
                    idx = idx2;
                } else {
                    commandList.add(commandInfo);
                    idx++;
                }
            }
        }
        return commandList;
    }

    private void fetchApduDataJsonFromLocalAssetInfo(String productId, Map<String, String> apduJsonData) throws AppletCardException {
        LogX.i("parseJson2ApduSet fetch apdu data from local asset info begin");
        if (this.jsonDataForProduct.isEmpty()) {
            loadLocalApduDatas();
        }
        String json = this.jsonDataForProduct.get(productId);
        if (!StringUtil.isEmpty(json, true)) {
            try {
                JSONObject jobj = new JSONObject(json);
                apduJsonData.clear();
                for (String type : cardInfoFields) {
                    if (jobj.has(type)) {
                        apduJsonData.put(type, jobj.getString(type));
                    }
                }
                LogX.i("parseJson2ApduSet fetch apdu data from local asset info end");
            } catch (JSONException e) {
                throw new AppletCardException(4, "fetchApduDataJsonFromLocalAssetInfo failed. parse exception json : " + json);
            }
        } else {
            throw new AppletCardException(5, "parseJson2ApduSet does not have local apdu json data for " + productId);
        }
    }

    private void loadLocalApduDatas() {
        LogX.i("loadLocalApduDatas begin");
        Properties prop = new Properties();
        InputStream in = null;
        try {
            InputStream in2 = this.mContext.getAssets().open("cardinfo_read_apdu_data.properties");
            prop.load(in2);
            this.jsonDataForProduct.clear();
            for (String productId : prop.keySet()) {
                this.jsonDataForProduct.put(productId, prop.getProperty(productId));
            }
            LogX.i("loadLocalApduDatas load data cnt : " + this.jsonDataForProduct.size());
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException e) {
                    LogX.i("processTask close stream error1");
                }
            }
        } catch (IOException e2) {
            LogX.i("loadLocalApduDatas failed. IOException happened. msg : " + e2.getMessage(), true);
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
