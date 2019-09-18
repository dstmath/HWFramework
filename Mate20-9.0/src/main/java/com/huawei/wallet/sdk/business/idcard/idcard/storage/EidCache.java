package com.huawei.wallet.sdk.business.idcard.idcard.storage;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.apdu.properties.WalletSystemProperties;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class EidCache {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "IDCard:EidCache";
    private static volatile EidCache mInstance;
    private String aid = "534E4E3A0065004900445E947528";
    private String eidAid = "A000000003454944";
    private Map<String, String> issuerMap = new HashMap();
    private Context mContext;

    private EidCache(Context context) {
        this.mContext = context;
        initIssuerMap();
    }

    public static EidCache getInstance(Context context) {
        LogC.i(TAG, "getInstance executed", false);
        if (mInstance == null) {
            synchronized (SYNC_LOCK) {
                if (mInstance == null) {
                    mInstance = new EidCache(context);
                }
            }
        }
        return mInstance;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
    }

    public String getEidIssuerId() {
        String eidIssuerId = this.issuerMap.get("EID_ISSUER_ID");
        if (TextUtils.isEmpty(eidIssuerId)) {
            return WalletSystemProperties.getInstance().getProperty("EID_ISSUER_ID", "1040034");
        }
        return eidIssuerId;
    }

    public String getCtidIssuerId() {
        String ctidIssuerId = this.issuerMap.get("CTID_ISSUER_ID");
        if (TextUtils.isEmpty(ctidIssuerId)) {
            return WalletSystemProperties.getInstance().getProperty("CTID_ISSUER_ID", "1049999");
        }
        return ctidIssuerId;
    }

    private void initIssuerMap() {
        try {
            String routeStr = WalletTaManager.getInstance(this.mContext).getRouterInfo();
            if (!TextUtils.isEmpty(routeStr)) {
                try {
                    JSONObject jsonObject = new JSONObject(routeStr);
                    this.issuerMap.put("EID_ISSUER_ID", jsonObject.getString("EID_ISSUER_ID"));
                    this.issuerMap.put("CTID_ISSUER_ID", jsonObject.getString("CTID_ISSUER_ID"));
                } catch (JSONException e) {
                    LogC.e("hit JSONException when transfer string to json.", false);
                }
            }
        } catch (Exception e2) {
            LogC.e("hit Exception when getRouterInfo from TA.", false);
        }
    }
}
