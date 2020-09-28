package com.mediatek.gba;

import android.content.Context;
import android.net.Network;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.gba.IGbaService;
import com.mediatek.ims.internal.IMtkImsService;

public final class GbaManager {
    public static final boolean DBGLOG;
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID0 = {1, 0, 0, 0, 0};
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID1 = {1, 0, 0, 0, 1};
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID2 = {1, 0, 0, 0, 2};
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID3 = {1, 0, 0, 0, 3};
    private static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID_HTTP = {1, 0, 0, 0, 2};
    private static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID_TLS = {1, 0, 1, 0, 47};
    public static final String IMS_GBA_KS_EXT_NAF = "Ks_ext_NAF";
    public static final String IMS_GBA_KS_NAF = "Ks_NAF";
    public static final int IMS_GBA_ME = 1;
    public static final int IMS_GBA_NONE = 0;
    public static final int IMS_GBA_U = 2;
    public static final String IMS_SERVICE = "ims";
    public static final String MTK_IMS_SERVICE = "mtkIms";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    public static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final String TAG = "GbaManager";
    private static GbaManager mGbaManager = null;
    private static int mNetId;
    private static IGbaService mService;
    private final Context mContext;

    static {
        boolean z = false;
        if (!SENLOG || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        DBGLOG = z;
    }

    public static GbaManager getDefaultGbaManager(Context context) {
        if (context != null) {
            synchronized (GbaManager.class) {
                if (mGbaManager == null) {
                    if (!supportMdAutoSetupIms()) {
                        IBinder b = ServiceManager.getService("GbaService");
                        if (b == null) {
                            Log.i("debug", "The binder is null");
                            return null;
                        }
                        mService = IGbaService.Stub.asInterface(b);
                    }
                    mGbaManager = new GbaManager(context);
                }
                return mGbaManager;
            }
        }
        throw new IllegalArgumentException("context cannot be null");
    }

    GbaManager(Context context) {
        this.mContext = context;
    }

    public int getGbaSupported() {
        try {
            return mService.getGbaSupported();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getGbaSupported(int subId) {
        try {
            return mService.getGbaSupported();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean isGbaKeyExpired(String nafFqdn, byte[] nafSecurProtocolId) {
        try {
            return mService.isGbaKeyExpired(nafFqdn, nafSecurProtocolId);
        } catch (RemoteException e) {
            return true;
        }
    }

    public boolean isGbaKeyExpired(String nafFqdn, byte[] nafSecurProtocolId, int subId) {
        try {
            return mService.isGbaKeyExpiredForSubscriber(nafFqdn, nafSecurProtocolId, subId);
        } catch (RemoteException e) {
            return true;
        }
    }

    public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun) {
        try {
            if (!supportMdAutoSetupIms()) {
                return mService.runGbaAuthentication(nafFqdn, nafSecureProtocolId, forceRun);
            }
            return runNativeGba(nafFqdn, nafSecureProtocolId, forceRun, mNetId, SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
        } catch (RemoteException e) {
            return null;
        }
    }

    public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int subId) {
        try {
            if (!supportMdAutoSetupIms()) {
                return mService.runGbaAuthenticationForSubscriber(nafFqdn, nafSecureProtocolId, forceRun, subId);
            }
            return runNativeGba(nafFqdn, nafSecureProtocolId, forceRun, mNetId, SubscriptionManager.getPhoneId(subId));
        } catch (RemoteException e) {
            return null;
        }
    }

    private NafSessionKey runNativeGba(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int netId, int phoneId) {
        IBinder b = ServiceManager.getService("mtkIms");
        if (b == null) {
            Log.e(TAG, "Service is unavailable binder is null");
            return null;
        }
        IMtkImsService mMtkImsService = IMtkImsService.Stub.asInterface(b);
        if (mMtkImsService == null) {
            Log.e(TAG, "Service is unavailable mImsService is null");
            return null;
        }
        try {
            return mMtkImsService.runGbaAuthentication(nafFqdn, nafSecureProtocolId, forceRun, netId, phoneId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemotaException mImsService.runGbaAuthentication()");
            return null;
        }
    }

    public byte[] getNafSecureProtocolId(boolean isTls, String cipher) {
        GbaCipherSuite cipherSuite;
        if (DBGLOG) {
            Log.d(TAG, "getNafSecureProtocolId isTls = " + isTls + ", cipher = " + cipher);
        }
        byte[] uaId = DEFAULT_UA_SECURITY_PROTOCOL_ID_TLS;
        if (!isTls) {
            if (DBGLOG) {
                Log.e(TAG, "Default cipherSuite");
            }
            return DEFAULT_UA_SECURITY_PROTOCOL_ID_HTTP;
        } else if (cipher == null || cipher.length() <= 0 || (cipherSuite = GbaCipherSuite.getByName(cipher)) == null) {
            return uaId;
        } else {
            if (DBGLOG) {
                Log.d(TAG, "cipherSuite name = " + cipherSuite.getName());
            }
            byte[] cipherSuiteCode = cipherSuite.getCode();
            if (DBGLOG) {
                Log.d(TAG, "uaId = " + String.format("0x%2x", Byte.valueOf(cipherSuiteCode[0])) + ", " + String.format("0x%2x", Byte.valueOf(cipherSuiteCode[1])));
            }
            uaId[3] = cipherSuiteCode[0];
            uaId[4] = cipherSuiteCode[1];
            return uaId;
        }
    }

    public void setNetwork(Network network) {
        try {
            mService.setNetwork(network);
            mNetId = network.netId;
        } catch (RemoteException e) {
            Log.e(TAG, "remote expcetion for setNetwork");
        }
    }

    public NafSessionKey getCachedKey(String nafFqdn, byte[] nafSecureProtocolId, int subId) {
        try {
            return mService.getCachedKey(nafFqdn, nafSecureProtocolId, subId);
        } catch (RemoteException e) {
            Log.e(TAG, "remote expcetion for getCachedKey");
            return null;
        }
    }

    public void updateCachedKey(String nafFqdn, byte[] nafSecureProtocolId, int subId, NafSessionKey nafSessionKey) {
        try {
            mService.updateCachedKey(nafFqdn, nafSecureProtocolId, subId, nafSessionKey);
        } catch (RemoteException e) {
            Log.e(TAG, "remote expcetion for updateCachedKey");
        }
    }

    private static boolean supportMdAutoSetupIms() {
        if (SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }
}
