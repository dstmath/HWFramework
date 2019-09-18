package com.huawei.payments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.util.Log;

public class Paytm implements Wallet {
    private static final int HASHCODE = 911215037;
    private static final String PACKAGE = "net.one97.paytm";
    private static final String SIGNATURE = "3082024f308201b8a00302010202044f97d18e300d06092a864886f70d0101050500306b310b3009060355040613023931311630140603550408130d55747461722050726164657368310e300c060355040713054e6f696461310e300c060355040a1305506179746d31143012060355040b130b456e67696e656572696e67310e300c06035504031305506179746d3020170d3132303432353130323732365a180f32303632303431333130323732365a306b310b3009060355040613023931311630140603550408130d55747461722050726164657368310e300c060355040713054e6f696461310e300c060355040a1305506179746d31143012060355040b130b456e67696e656572696e67310e300c06035504031305506179746d30819f300d06092a864886f70d010101050003818d0030818902818100a08b85239755a1ab37a60db9696f1585a5f4ff24ff826869057d1ca97d7da2f7f91fe6ccc844da88ae98051437de2977ff1efe6bac47d5be7ab918a8e9dce1c59fa98396ededbca863561936d9bb79fb0b68be38f3aba9f71569d66b86ea43a47d06dab6907fbe39c88e80cc0cf6c1bcffd3b1eedd3eccd8c58cec8beaee76950203010001300d06092a864886f70d010105050003818100954804a6b32001c64f90f8d087652bd5e29d09cbfc069d54330b9b7cab90a2ec9a6dae1385579833583c5daf896b0ca58d3a02ccfbdb19f4c4c2dbec1a9a0bcc600ed6f766706af89dfa916610faa356ae53ad2e85278561f1f002fcb30f8c4e9e6736e5be385fb527a94ee889f9472f806ada177a342da412460f89dbdadc26";
    private static final String TAG = "Paytm";
    private static final String URI = "paytmmp://cash_wallet?featuretype=scanner";
    private Context mContext;

    Paytm(Context context) {
        this.mContext = context;
    }

    public String getPackageName() {
        return PACKAGE;
    }

    public Intent getIntent() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(URI));
        intent.setPackage(PACKAGE);
        return intent;
    }

    public String getWalletName() {
        ApplicationInfo appinfo;
        Object obj;
        PackageManager packagemanager = this.mContext.getPackageManager();
        try {
            appinfo = packagemanager.getApplicationInfo(PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            appinfo = null;
        }
        if (appinfo != null) {
            obj = packagemanager.getApplicationLabel(appinfo);
        } else {
            obj = "";
        }
        return (String) obj;
    }

    public boolean isVaildSignature() {
        try {
            for (Signature lsignature : this.mContext.getPackageManager().getPackageInfo(PACKAGE, 64).signatures) {
                if (lsignature != null && lsignature.toCharsString().equals(SIGNATURE) && lsignature.hashCode() == HASHCODE) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Name not found" + e.getMessage());
        }
        return false;
    }
}
