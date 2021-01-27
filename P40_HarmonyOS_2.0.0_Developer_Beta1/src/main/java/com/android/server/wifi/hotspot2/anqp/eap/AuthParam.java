package com.android.server.wifi.hotspot2.anqp.eap;

public abstract class AuthParam {
    public static final int PARAM_TYPE_CREDENTIAL_TYPE = 5;
    public static final int PARAM_TYPE_EXPANDED_EAP_METHOD = 1;
    public static final int PARAM_TYPE_EXPANDED_INNER_EAP_METHOD = 4;
    public static final int PARAM_TYPE_INNER_AUTH_EAP_METHOD_TYPE = 3;
    public static final int PARAM_TYPE_NON_EAP_INNER_AUTH_TYPE = 2;
    public static final int PARAM_TYPE_TUNNELED_EAP_METHOD_CREDENTIAL_TYPE = 6;
    public static final int PARAM_TYPE_VENDOR_SPECIFIC = 221;
    private final int mAuthTypeID;

    protected AuthParam(int authTypeID) {
        this.mAuthTypeID = authTypeID;
    }

    public int getAuthTypeID() {
        return this.mAuthTypeID;
    }
}
