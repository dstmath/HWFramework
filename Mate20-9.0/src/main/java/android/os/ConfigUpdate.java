package android.os;

import android.annotation.SystemApi;

@SystemApi
public final class ConfigUpdate {
    @SystemApi
    public static final String ACTION_UPDATE_CARRIER_ID_DB = "android.os.action.UPDATE_CARRIER_ID_DB";
    @SystemApi
    public static final String ACTION_UPDATE_CARRIER_PROVISIONING_URLS = "android.intent.action.UPDATE_CARRIER_PROVISIONING_URLS";
    @SystemApi
    public static final String ACTION_UPDATE_CT_LOGS = "android.intent.action.UPDATE_CT_LOGS";
    @SystemApi
    public static final String ACTION_UPDATE_INTENT_FIREWALL = "android.intent.action.UPDATE_INTENT_FIREWALL";
    @SystemApi
    public static final String ACTION_UPDATE_LANG_ID = "android.intent.action.UPDATE_LANG_ID";
    @SystemApi
    public static final String ACTION_UPDATE_NETWORK_WATCHLIST = "android.intent.action.UPDATE_NETWORK_WATCHLIST";
    @SystemApi
    public static final String ACTION_UPDATE_PINS = "android.intent.action.UPDATE_PINS";
    @SystemApi
    public static final String ACTION_UPDATE_SMART_SELECTION = "android.intent.action.UPDATE_SMART_SELECTION";
    @SystemApi
    public static final String ACTION_UPDATE_SMS_SHORT_CODES = "android.intent.action.UPDATE_SMS_SHORT_CODES";

    private ConfigUpdate() {
    }
}
