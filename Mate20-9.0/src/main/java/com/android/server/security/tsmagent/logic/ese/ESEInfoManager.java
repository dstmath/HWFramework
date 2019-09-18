package com.android.server.security.tsmagent.logic.ese;

import android.content.Context;
import com.android.server.security.tsmagent.utils.HexByteHelper;
import com.android.server.security.tsmagent.utils.HwLog;
import com.leisen.wallet.sdk.tsm.TSMOperator;
import com.leisen.wallet.sdk.tsm.TSMOperatorResponse;

public class ESEInfoManager {
    private static final String AMSD_AID = "A000000151000000";
    private static ESEInfoManager instance;
    private static final Object serviceLock = new Object();
    /* access modifiers changed from: private */
    public String CIN;
    /* access modifiers changed from: private */
    public String IIN;
    /* access modifiers changed from: private */
    public String cplc;
    private Context mContext;

    public static ESEInfoManager getInstance(Context context) {
        synchronized (serviceLock) {
            if (instance == null) {
                instance = new ESEInfoManager(context);
            }
        }
        return instance;
    }

    private ESEInfoManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public String queryCplc(int reader) {
        String str;
        synchronized (serviceLock) {
            this.cplc = null;
            TSMOperator tsmOperator = TSMOperator.getInstance(this.mContext, null, reader);
            tsmOperator.setTsmOperatorResponse(new TSMOperatorResponse() {
                public void onOperSuccess(String response) {
                    String unused = ESEInfoManager.this.cplc = null;
                    try {
                        String unused2 = ESEInfoManager.this.cplc = response.substring(6, (HexByteHelper.hexStringToDecimalInteger(response.substring(4, 6)) * 2) + 6);
                    } catch (Exception e) {
                        HwLog.d("queryCplc err");
                        String unused3 = ESEInfoManager.this.cplc = null;
                    }
                }

                public void onOperFailure(int result, Error e) {
                    String unused = ESEInfoManager.this.cplc = null;
                }
            });
            tsmOperator.getCPLC(AMSD_AID);
            tsmOperator.setTsmOperatorResponse(null);
            str = this.cplc;
        }
        return str;
    }

    public String queryCIN(int reader) {
        String str;
        synchronized (serviceLock) {
            this.CIN = null;
            TSMOperator tsmOperator = TSMOperator.getInstance(this.mContext, null, reader);
            tsmOperator.setTsmOperatorResponse(new TSMOperatorResponse() {
                public void onOperSuccess(String response) {
                    String unused = ESEInfoManager.this.CIN = "";
                    try {
                        String unused2 = ESEInfoManager.this.CIN = response;
                    } catch (Exception e) {
                        HwLog.d("queryCIN err");
                    }
                }

                public void onOperFailure(int result, Error e) {
                    String unused = ESEInfoManager.this.CIN = null;
                }
            });
            tsmOperator.getCIN(AMSD_AID);
            tsmOperator.setTsmOperatorResponse(null);
            str = this.CIN;
        }
        return str;
    }

    public String queryIIN(int reader) {
        String str;
        synchronized (serviceLock) {
            this.cplc = null;
            TSMOperator tsmOperator = TSMOperator.getInstance(this.mContext, null, reader);
            tsmOperator.setTsmOperatorResponse(new TSMOperatorResponse() {
                public void onOperSuccess(String response) {
                    String unused = ESEInfoManager.this.IIN = "";
                    try {
                        String unused2 = ESEInfoManager.this.IIN = response;
                    } catch (Exception e) {
                        HwLog.d("queryIIN err");
                    }
                }

                public void onOperFailure(int result, Error e) {
                    String unused = ESEInfoManager.this.IIN = null;
                }
            });
            tsmOperator.getIIN(AMSD_AID);
            tsmOperator.setTsmOperatorResponse(null);
            str = this.IIN;
        }
        return str;
    }
}
