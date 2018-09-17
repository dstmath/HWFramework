package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.ArrayList;

public class HwSoftApManager extends SoftApManager {
    private static boolean DBG = false;
    protected static final boolean HWFLOW;
    private static final int NT_CHINA_CMCC = 3;
    private static final int NT_CHINA_UT = 2;
    private static final int NT_FOREIGN = 1;
    private static final int NT_UNREG = 0;
    private static final String TAG = "HwSoftApManager";
    private int mDataSub;
    private String mOperatorNumericSub0;
    private String mOperatorNumericSub1;
    private PhoneStateListener[] mPhoneStateListener;
    private int mServiceStateSub0;
    private int mServiceStateSub1;
    private TelephonyManager mTelephonyManager;
    private WifiChannelXmlParse mWifiChannelXmlParse;

    /* renamed from: com.android.server.wifi.HwSoftApManager.2 */
    class AnonymousClass2 extends PhoneStateListener {
        AnonymousClass2(int $anonymous0) {
            super($anonymous0);
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null) {
                if (HwSoftApManager.DBG) {
                    Log.d(HwSoftApManager.TAG, "PhoneStateListener " + this.mSubId);
                }
                if (this.mSubId == 0) {
                    HwSoftApManager.this.mServiceStateSub0 = state.getDataRegState();
                    HwSoftApManager.this.mOperatorNumericSub0 = state.getOperatorNumeric();
                } else if (this.mSubId == HwSoftApManager.NT_FOREIGN) {
                    HwSoftApManager.this.mServiceStateSub1 = state.getDataRegState();
                    HwSoftApManager.this.mOperatorNumericSub1 = state.getOperatorNumeric();
                }
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : HWFLOW : true;
        HWFLOW = isLoggable;
        DBG = HWFLOW;
    }

    public HwSoftApManager(Context context, Looper looper, WifiNative wifiNative, INetworkManagementService nmService, ConnectivityManager connectivityManager, String countryCode, ArrayList<Integer> allowed2GChannels, Listener listener) {
        super(context, looper, wifiNative, nmService, connectivityManager, countryCode, allowed2GChannels, listener);
        this.mOperatorNumericSub0 = null;
        this.mOperatorNumericSub1 = null;
        this.mWifiChannelXmlParse = null;
        this.mServiceStateSub0 = NT_FOREIGN;
        this.mServiceStateSub1 = NT_FOREIGN;
        this.mDataSub = -1;
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                HwSoftApManager.this.mDataSub = intent.getIntExtra("subscription", -1);
            }
        }, new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"));
        this.mDataSub = Global.getInt(context.getContentResolver(), "multi_sim_data_call", NT_UNREG);
        registerPhoneStateListener(context);
    }

    private void registerPhoneStateListener(Context context) {
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mPhoneStateListener = new PhoneStateListener[NT_CHINA_UT];
        for (int i = NT_UNREG; i < NT_CHINA_UT; i += NT_FOREIGN) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            this.mTelephonyManager.listen(this.mPhoneStateListener[i], NT_FOREIGN);
        }
    }

    private PhoneStateListener getPhoneStateListener(int subId) {
        return new AnonymousClass2(subId);
    }

    private int getRegistedNetworkType() {
        int serviceState;
        String numeric;
        if (this.mDataSub == 0) {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        } else if (this.mDataSub != NT_FOREIGN) {
            return NT_UNREG;
        } else {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        }
        Log.d(TAG, "isRegistedNetworkType mDataSub " + this.mDataSub + ", serviceState " + serviceState + " , numeric " + numeric);
        if (serviceState != 0 || (numeric != null && numeric.length() >= 5 && numeric.substring(NT_UNREG, 5).equals("99999"))) {
            return NT_UNREG;
        }
        if (numeric == null || numeric.length() < NT_CHINA_CMCC || !numeric.substring(NT_UNREG, NT_CHINA_CMCC).equals("460")) {
            return (numeric == null || numeric.equals("")) ? NT_UNREG : NT_FOREIGN;
        } else {
            if ("46000".equals(this.mOperatorNumericSub0) || "46002".equals(this.mOperatorNumericSub0) || "46007".equals(this.mOperatorNumericSub0)) {
                return NT_CHINA_CMCC;
            }
            return NT_CHINA_UT;
        }
    }

    private String getCurrentBand() {
        String ret = null;
        String[] bandrst = HwTelephonyManagerInner.getDefault().queryServiceCellBand();
        if (bandrst != null) {
            if (bandrst.length < NT_CHINA_UT) {
                if (DBG) {
                    Log.d(TAG, "getCurrentBand bandrst error.");
                }
                return null;
            } else if ("GSM".equals(bandrst[NT_UNREG])) {
                switch (Integer.parseInt(bandrst[NT_FOREIGN])) {
                    case NT_UNREG /*0*/:
                        ret = "GSM850";
                        break;
                    case NT_FOREIGN /*1*/:
                        ret = "GSM900";
                        break;
                    case NT_CHINA_UT /*2*/:
                        ret = "GSM1800";
                        break;
                    case NT_CHINA_CMCC /*3*/:
                        ret = "GSM1900";
                        break;
                    default:
                        Log.e(TAG, "should not be here.");
                        break;
                }
            } else {
                ret = "CDMA".equals(bandrst[NT_UNREG]) ? "BC0" : bandrst[NT_UNREG] + bandrst[NT_FOREIGN];
            }
        }
        if (DBG) {
            Log.d(TAG, "getCurrentBand rst is " + ret);
        }
        return ret;
    }

    private ArrayList<Integer> getAllowed2GChannels(ArrayList<Integer> allowedChannels) {
        int networkType = getRegistedNetworkType();
        ArrayList<Integer> intersectChannels = new ArrayList();
        if (allowedChannels == null) {
            return null;
        }
        if (networkType == NT_CHINA_CMCC) {
            intersectChannels.add(Integer.valueOf(6));
        } else if (networkType == NT_CHINA_UT) {
            intersectChannels.add(Integer.valueOf(NT_FOREIGN));
            intersectChannels.add(Integer.valueOf(6));
        } else if (networkType == NT_FOREIGN) {
            this.mWifiChannelXmlParse = WifiChannelXmlParse.getInstance();
            ArrayList<Integer> vaildChannels = this.mWifiChannelXmlParse.getValidChannels(getCurrentBand(), true);
            intersectChannels = (ArrayList) allowedChannels.clone();
            if (vaildChannels != null) {
                intersectChannels.retainAll(vaildChannels);
            }
            if (intersectChannels.size() == 0) {
                intersectChannels = allowedChannels;
            }
        } else {
            intersectChannels = allowedChannels;
        }
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("channels: ");
            for (Integer channel : intersectChannels) {
                sb.append(channel.toString()).append(",");
            }
            Log.d(TAG, "2G " + sb);
        }
        return intersectChannels;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int[] getAllowed5GChannels(WifiNative wifiNative) {
        Exception e;
        int[] allowedChannels = wifiNative.getChannelsForBand(NT_CHINA_UT);
        if (allowedChannels == null || allowedChannels.length <= NT_FOREIGN) {
            return allowedChannels;
        }
        int i;
        int[] values = new int[allowedChannels.length];
        this.mWifiChannelXmlParse = WifiChannelXmlParse.getInstance();
        ArrayList<Integer> vaildChannels = this.mWifiChannelXmlParse.getValidChannels(getCurrentBand(), HWFLOW);
        int counter = NT_UNREG;
        if (vaildChannels != null) {
            i = NT_UNREG;
            int counter2 = NT_UNREG;
            while (i < allowedChannels.length) {
                try {
                    if (vaildChannels.contains(Integer.valueOf(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i])))) {
                        counter = counter2 + NT_FOREIGN;
                        try {
                            values[counter2] = allowedChannels[i];
                        } catch (Exception e2) {
                            e = e2;
                        }
                    } else {
                        counter = counter2;
                    }
                    i += NT_FOREIGN;
                    counter2 = counter;
                } catch (Exception e3) {
                    e = e3;
                    counter = counter2;
                }
            }
            counter = counter2;
        }
        StringBuilder sb;
        if (counter == 0) {
            Log.d(TAG, "5G counter is 0");
            if (DBG) {
                sb = new StringBuilder();
                sb.append("allowedChannels channels: ");
                for (i = NT_UNREG; i < allowedChannels.length; i += NT_FOREIGN) {
                    sb.append(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i])).append(",");
                }
                Log.d(TAG, "5G " + sb);
            }
            return allowedChannels;
        }
        int[] intersectChannels = new int[counter];
        for (i = NT_UNREG; i < counter; i += NT_FOREIGN) {
            intersectChannels[i] = values[i];
        }
        if (DBG) {
            sb = new StringBuilder();
            sb.append("allowedChannels channels: ");
            for (i = NT_UNREG; i < allowedChannels.length; i += NT_FOREIGN) {
                sb.append(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i])).append(",");
            }
            sb.append("intersectChannels channels: ");
            for (i = NT_UNREG; i < counter; i += NT_FOREIGN) {
                sb.append(ApConfigUtil.convertFrequencyToChannel(intersectChannels[i])).append(",");
            }
            Log.d(TAG, "5G " + sb.toString());
        }
        return intersectChannels;
    }

    public int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        if (!wifiNative.isHalStarted()) {
            config.apBand = NT_UNREG;
            config.apChannel = 6;
            return NT_UNREG;
        } else if (config.apBand == NT_FOREIGN && countryCode == null) {
            Log.e(TAG, "5GHz band is not allowed without country code");
            return NT_CHINA_UT;
        } else {
            if (config.apChannel == 0) {
                config.apChannel = ApConfigUtil.chooseApChannel(config.apBand, getAllowed2GChannels(allowed2GChannels), getAllowed5GChannels(wifiNative));
                if (config.apChannel == -1) {
                    if (wifiNative.isGetChannelsForBandSupported()) {
                        Log.e(TAG, "Failed to get available channel.");
                        return NT_FOREIGN;
                    }
                    config.apBand = NT_UNREG;
                    config.apChannel = 6;
                }
            }
            if (DBG) {
                Log.d(TAG, "updateApChannelConfig apChannel: " + config.apChannel);
            }
            return NT_UNREG;
        }
    }
}
