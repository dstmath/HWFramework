package com.mediatek.gba;

import android.content.Context;
import android.net.Network;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.mediatek.gba.IGbaService;

public abstract class GbaBaseCredential {
    static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID_HTTP = {1, 0, 0, 0, 2};
    static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID_TLS = {1, 0, 1, 0, 47};
    protected static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    static final String NAFFQDN_PREFER = "original.naf.prefer";
    private static final String TAG = "GbaBaseCredential";
    protected static IGbaService sService;
    protected boolean mIsTlsEnabled;
    protected String mNafAddress;
    protected Network mNetwork;
    protected int mSubId;

    GbaBaseCredential() {
    }

    GbaBaseCredential(Context context, String nafAddress, int subId) {
        this.mSubId = subId;
        nafAddress = nafAddress.charAt(nafAddress.length() - 1) == '/' ? nafAddress.substring(0, nafAddress.length() - 1) : nafAddress;
        this.mIsTlsEnabled = true;
        this.mNafAddress = nafAddress.toLowerCase();
        if (this.mNafAddress.indexOf("http://") != -1) {
            this.mNafAddress = nafAddress.substring(7);
            this.mIsTlsEnabled = false;
        } else if (this.mNafAddress.indexOf("https://") != -1) {
            this.mNafAddress = nafAddress.substring(8);
            this.mIsTlsEnabled = true;
        }
        Log.d(TAG, "nafAddress:" + this.mNafAddress);
    }

    public void setTlsEnabled(boolean tlsEnabled) {
        this.mIsTlsEnabled = tlsEnabled;
    }

    public void setSubId(int subId) {
        this.mSubId = subId;
    }

    public void setNetwork(Network network) {
        if (network != null) {
            Log.i(TAG, "GBA dedicated network netid:" + network);
            this.mNetwork = network;
        }
    }

    /* JADX INFO: Multiple debug info for r4v1 byte[]: [D('e' java.lang.NullPointerException), D('uaId' byte[])] */
    public NafSessionKey getNafSessionKey() {
        GbaCipherSuite cipherSuite;
        NafSessionKey nafSessionKey = null;
        try {
            IBinder b = ServiceManager.getService("GbaService");
            if (b == null) {
                Log.i("debug", "The binder is null");
                return null;
            }
            sService = IGbaService.Stub.asInterface(b);
            try {
                byte[] uaId = DEFAULT_UA_SECURITY_PROTOCOL_ID_TLS;
                if (this.mIsTlsEnabled) {
                    String gbaStr = System.getProperty("gba.ciper.suite", "");
                    if (gbaStr.length() > 0 && (cipherSuite = GbaCipherSuite.getByName(gbaStr)) != null) {
                        byte[] cipherSuiteCode = cipherSuite.getCode();
                        uaId[3] = cipherSuiteCode[0];
                        uaId[4] = cipherSuiteCode[1];
                    }
                } else {
                    uaId = DEFAULT_UA_SECURITY_PROTOCOL_ID_HTTP;
                }
                if (this.mNetwork != null) {
                    sService.setNetwork(this.mNetwork);
                }
                String realm = System.getProperty("digest.realm", "");
                String originalNafPrefer = System.getProperty(NAFFQDN_PREFER, "");
                Log.i(TAG, "realm:" + realm);
                Log.i(TAG, "NAFFQDN_PREFER:" + originalNafPrefer);
                if (realm.length() <= 0) {
                    return null;
                }
                if (originalNafPrefer.length() == 0) {
                    String[] segments = realm.split(";");
                    this.mNafAddress = segments[0].substring(segments[0].indexOf("@") + 1);
                }
                Log.i(TAG, "NAF FQDN:" + this.mNafAddress);
                boolean forceRun = false;
                Log.d(TAG, "gba.auth: " + System.getProperty("gba.auth"));
                if ("401".equals(System.getProperty("gba.auth"))) {
                    forceRun = true;
                    System.setProperty("gba.auth", "");
                }
                Log.d(TAG, "forceRun: " + forceRun);
                if (-1 == this.mSubId) {
                    nafSessionKey = sService.runGbaAuthentication(this.mNafAddress, uaId, forceRun);
                } else {
                    nafSessionKey = sService.runGbaAuthenticationForSubscriber(this.mNafAddress, uaId, forceRun, this.mSubId);
                }
                if (!(nafSessionKey == null || nafSessionKey.getException() == null || !(nafSessionKey.getException() instanceof IllegalStateException))) {
                    String msg = ((IllegalStateException) nafSessionKey.getException()).getMessage();
                    if ("HTTP 403 Forbidden".equals(msg)) {
                        Log.i(TAG, "GBA hit 403");
                        System.setProperty("gba.auth", "403");
                    } else if ("HTTP 400 Bad Request".equals(msg)) {
                        Log.i(TAG, "GBA hit 400");
                    }
                }
                return nafSessionKey;
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
