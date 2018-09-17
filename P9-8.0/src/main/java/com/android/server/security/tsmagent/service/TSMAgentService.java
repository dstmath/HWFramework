package com.android.server.security.tsmagent.service;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.tsmagent.openapi.ITSMOperator;
import com.android.server.security.tsmagent.openapi.impl.TSMAgentImpl;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.StringUtil;
import huawei.android.security.ITSMAgent.Stub;
import java.util.Arrays;

public class TSMAgentService extends Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            HwLog.d("createTSMAgentService");
            return new TSMAgentService(context);
        }

        public String getPluginPermission() {
            return TSMAgentService.USE_TSM_AGENT;
        }
    };
    private static final String USE_TSM_AGENT = "com.huawei.permission.USE_TSM_AGENT";
    private Context mContext;
    private ITSMOperator mOperator;

    public TSMAgentService(Context context) {
        this.mContext = context;
        this.mOperator = TSMAgentImpl.getInstance(context);
    }

    public void onStart() {
        HwLog.i("onStart - TSMAgentService");
    }

    public void onStop() {
        HwLog.i("onStop - TSMAgentService");
    }

    public IBinder asBinder() {
        return this;
    }

    public int createSSD(String spID, String ssdAid, String sign, String timeStamp) throws RemoteException {
        HwLog.d(ITSMOperator.OPERATOR_TYPE_CREATE_SSD);
        return this.mOperator.createSSD(getCallingPackage(), spID, ssdAid, sign, timeStamp);
    }

    public int deleteSSD(String spID, String ssdAid, String sign, String timeStamp) throws RemoteException {
        HwLog.d(ITSMOperator.OPERATOR_TYPE_DELETE_SSD);
        return this.mOperator.deleteSSD(getCallingPackage(), spID, ssdAid, sign, timeStamp);
    }

    public String getCplc() throws RemoteException {
        HwLog.d("getCplc");
        return this.mOperator.getCplc(getCallingPackage());
    }

    public int initEse(String spID, String sign, String timeStamp) throws RemoteException {
        HwLog.d("initEse");
        if (!StringUtil.isTrimedEmpty(spID) && !StringUtil.isTrimedEmpty(sign) && !StringUtil.isTrimedEmpty(timeStamp)) {
            return this.mOperator.initEse(getCallingPackage(), spID, sign, timeStamp);
        }
        throw new IllegalArgumentException("empty argument!");
    }

    private String getCallingPackage() throws IllegalArgumentException {
        if (this.mContext != null) {
            String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(getCallingUid());
            if (pkgs != null && pkgs.length > 0) {
                HwLog.d("the caller pkg [ " + Arrays.toString(pkgs) + " ]");
                return pkgs[0];
            }
        }
        throw new IllegalArgumentException();
    }
}
