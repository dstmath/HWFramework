package com.huawei.hwwifiproservice;

import android.content.Context;
import android.util.Log;
import com.huawei.hwwifiproservice.HwNetworkPropertyChecker;
import java.lang.reflect.InvocationTargetException;

public class HwWifiProPartManager {
    private static final String TAG = "HwWifiProPartManager";
    private static HwWifiProPartManager sHwWifiProPartManager;
    private Context mContext = null;
    private IHwWifiProPart mHwWifiProPartImpl = null;

    public HwWifiProPartManager(Context context) {
        if (this.mContext == null) {
            this.mContext = context;
        }
        if (this.mHwWifiProPartImpl == null) {
            this.mHwWifiProPartImpl = getHwWifiProPartImplObject();
        }
    }

    public static HwWifiProPartManager getHwWifiProPartManager(Context context) {
        if (sHwWifiProPartManager == null) {
            sHwWifiProPartManager = new HwWifiProPartManager(context);
        }
        return sHwWifiProPartManager;
    }

    public static HwWifiProPartManager getInstance() {
        return sHwWifiProPartManager;
    }

    private IHwWifiProPart getHwWifiProPartImplObject() {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            return iHwWifiProPart;
        }
        Object result = null;
        try {
            result = Class.forName("com.huawei.hwwifiproservice.HwWifiProPartImpl").getConstructor(Context.class).newInstance(this.mContext);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            Log.e(TAG, "class or method not found.");
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e2) {
            Log.e(TAG, "newInstance expression is illegal.");
        }
        IHwWifiProPart iHwWifiProPart2 = null;
        if (result instanceof IHwWifiProPart) {
            iHwWifiProPart2 = (IHwWifiProPart) result;
        }
        if (iHwWifiProPart2 == null) {
            Log.i(TAG, "fail to get WifiProPartImpl object.");
        }
        return iHwWifiProPart2;
    }

    public int getAutoOpenCnt() {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            return iHwWifiProPart.getAutoOpenCnt();
        }
        return 0;
    }

    public void setAutoOpenCnt(int count) {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            iHwWifiProPart.setAutoOpenCnt(count);
        }
    }

    public String getCurrentPackageName() {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            return iHwWifiProPart.getCurrentPackageName();
        }
        return "";
    }

    public void updateStandardPortalTable(HwNetworkPropertyChecker.StarndardPortalInfo portalInfo) {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            iHwWifiProPart.updateStandardPortalTable(portalInfo);
        }
    }

    public void updateDhcpResultsByBssid(String currBssid, String dhcpResults) {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            iHwWifiProPart.updateDhcpResultsByBssid(currBssid, dhcpResults);
        }
    }

    public String syncQueryDhcpResultsByBssid(String currentBssid) {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            return iHwWifiProPart.syncQueryDhcpResultsByBssid(currentBssid);
        }
        return null;
    }

    public void notifyHttpReachableForWifiPro(boolean httpReachable) {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            iHwWifiProPart.notifyHttpReachableForWifiPro(httpReachable);
        }
    }

    public void notifyHttpRedirectedForWifiPro() {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            iHwWifiProPart.notifyHttpRedirectedForWifiPro();
        }
    }

    public void notifyRoamingCompletedForWifiPro(String newBssid) {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            iHwWifiProPart.notifyRoamingCompletedForWifiPro(newBssid);
        }
    }

    public void notifyRenewDhcpTimeoutForWifiPro() {
        IHwWifiProPart iHwWifiProPart = this.mHwWifiProPartImpl;
        if (iHwWifiProPart != null) {
            iHwWifiProPart.notifyRenewDhcpTimeoutForWifiPro();
        }
    }
}
