package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.List;

public class DeviceVpnManager {
    private static final String CONFIG_VPN = "config-vpn";
    private static final String DISABLE_VPN = "disable-vpn";
    private static final String SECURE_VPN = "secure-vpn";
    private static final String TAG = "DeviceVpnManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setVpnDisabled(ComponentName admin, boolean allow) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", allow);
        Log.d(TAG, "setVpnDisabled: " + allow);
        return this.mDpm.setPolicy(admin, DISABLE_VPN, bundle);
    }

    public boolean isVpnDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VPN);
        Boolean allow = Boolean.valueOf(false);
        if (bundle != null) {
            allow = Boolean.valueOf(bundle.getBoolean("value"));
            Log.d(TAG, "isVpnDisabled: " + allow);
            return allow.booleanValue();
        }
        Log.d(TAG, "has not set the allow, return default false");
        return allow.booleanValue();
    }

    public boolean setVpnProfile(ComponentName admin, DeviceVpnProfile profile) {
        Bundle bundle = new Bundle();
        bundle.putString("key", profile.getKey());
        bundle.putString("name", profile.getName());
        bundle.putString("type", Integer.toString(profile.getType()));
        bundle.putString("server", profile.getServer());
        bundle.putString("username", profile.getUsername());
        bundle.putString("password", profile.getPassword());
        bundle.putString("mppe", String.valueOf(profile.isMppe()));
        bundle.putString("l2tpSecret", profile.getL2tpSecret());
        bundle.putString("ipsecIdentifier", profile.getIpsecIdentifier());
        bundle.putString("ipsecSecret", profile.getIpsecSecret());
        bundle.putString("ipsecUserCert", profile.getIpsecUserCert());
        bundle.putString("ipsecCaCert", profile.getIpsecCaCert());
        bundle.putString("ipsecServerCert", profile.getIpsecServerCert());
        boolean result = this.mDpm.setCustomPolicy(admin, CONFIG_VPN, bundle);
        Log.d(TAG, "setVpnProfile and the result is: " + result);
        return result;
    }

    public boolean deleteVpnProfile(ComponentName admin, String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        boolean result = this.mDpm.removeCustomPolicy(admin, CONFIG_VPN, bundle);
        Log.d(TAG, "deleteVpnProfile and the result is: " + result);
        return result;
    }

    public List<String> getVpnList(ComponentName admin) {
        List<String> mVpnKeyList = new ArrayList();
        Bundle vpnListBundle = this.mDpm.getCustomPolicy(admin, CONFIG_VPN, null);
        if (vpnListBundle != null) {
            mVpnKeyList = vpnListBundle.getStringArrayList("keylist");
        }
        Log.d(TAG, "getVpnList.");
        return mVpnKeyList;
    }

    public DeviceVpnProfile getVpnProfile(ComponentName admin, String key) {
        Bundle bundle = new Bundle();
        Log.d(TAG, "getVpnProfile.");
        bundle.putString("key", key);
        Bundle vpnProfileBundle = this.mDpm.getCustomPolicy(admin, CONFIG_VPN, bundle);
        if (vpnProfileBundle == null) {
            return null;
        }
        DeviceVpnProfile profile = new DeviceVpnProfile(key);
        profile.setName(vpnProfileBundle.getString("name"));
        profile.setType(Integer.parseInt(vpnProfileBundle.getString("type")));
        profile.setServer(vpnProfileBundle.getString("server"));
        profile.setUsername(vpnProfileBundle.getString("username"));
        profile.setPassword(vpnProfileBundle.getString("password"));
        profile.setMppe(Boolean.parseBoolean(vpnProfileBundle.getString("mppe")));
        profile.setL2tpSecret(vpnProfileBundle.getString("l2tpSecret"));
        profile.setIpsecIdentifier(vpnProfileBundle.getString("ipsecIdentifier"));
        profile.setIpsecSecret(vpnProfileBundle.getString("ipsecSecret"));
        profile.setIpsecUserCert(vpnProfileBundle.getString("ipsecUserCert"));
        profile.setIpsecCaCert(vpnProfileBundle.getString("ipsecCaCert"));
        profile.setIpsecServerCert(vpnProfileBundle.getString("ipsecServerCert"));
        return profile;
    }

    public boolean setInsecureVpnDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        Log.d(TAG, "setInsecureVpnDisabled: " + disabled);
        return this.mDpm.setPolicy(admin, SECURE_VPN, bundle);
    }

    public boolean isInsecureVpnDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, SECURE_VPN);
        Boolean disabled = Boolean.valueOf(false);
        if (bundle != null) {
            disabled = Boolean.valueOf(bundle.getBoolean("value"));
            Log.d(TAG, "isInsecureVpnDisabled: " + disabled);
            return disabled.booleanValue();
        }
        Log.d(TAG, "has not set the disabled, return default false");
        return disabled.booleanValue();
    }
}
