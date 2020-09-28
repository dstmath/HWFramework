package android.net.wifi.hotspot2;

import android.annotation.SystemApi;

@SystemApi
public abstract class ProvisioningCallback {
    public static final int OSU_FAILURE_ADD_PASSPOINT_CONFIGURATION = 22;
    public static final int OSU_FAILURE_AP_CONNECTION = 1;
    public static final int OSU_FAILURE_INVALID_URL_FORMAT_FOR_OSU = 8;
    public static final int OSU_FAILURE_NO_AAA_SERVER_TRUST_ROOT_NODE = 17;
    public static final int OSU_FAILURE_NO_AAA_TRUST_ROOT_CERTIFICATE = 21;
    public static final int OSU_FAILURE_NO_OSU_ACTIVITY_FOUND = 14;
    public static final int OSU_FAILURE_NO_POLICY_SERVER_TRUST_ROOT_NODE = 19;
    public static final int OSU_FAILURE_NO_PPS_MO = 16;
    public static final int OSU_FAILURE_NO_REMEDIATION_SERVER_TRUST_ROOT_NODE = 18;
    public static final int OSU_FAILURE_OSU_PROVIDER_NOT_FOUND = 23;
    public static final int OSU_FAILURE_PROVISIONING_ABORTED = 6;
    public static final int OSU_FAILURE_PROVISIONING_NOT_AVAILABLE = 7;
    public static final int OSU_FAILURE_RETRIEVE_TRUST_ROOT_CERTIFICATES = 20;
    public static final int OSU_FAILURE_SERVER_CONNECTION = 3;
    public static final int OSU_FAILURE_SERVER_URL_INVALID = 2;
    public static final int OSU_FAILURE_SERVER_VALIDATION = 4;
    public static final int OSU_FAILURE_SERVICE_PROVIDER_VERIFICATION = 5;
    public static final int OSU_FAILURE_SOAP_MESSAGE_EXCHANGE = 11;
    public static final int OSU_FAILURE_START_REDIRECT_LISTENER = 12;
    public static final int OSU_FAILURE_TIMED_OUT_REDIRECT_LISTENER = 13;
    public static final int OSU_FAILURE_UNEXPECTED_COMMAND_TYPE = 9;
    public static final int OSU_FAILURE_UNEXPECTED_SOAP_MESSAGE_STATUS = 15;
    public static final int OSU_FAILURE_UNEXPECTED_SOAP_MESSAGE_TYPE = 10;
    public static final int OSU_STATUS_AP_CONNECTED = 2;
    public static final int OSU_STATUS_AP_CONNECTING = 1;
    public static final int OSU_STATUS_INIT_SOAP_EXCHANGE = 6;
    public static final int OSU_STATUS_REDIRECT_RESPONSE_RECEIVED = 8;
    public static final int OSU_STATUS_RETRIEVING_TRUST_ROOT_CERTS = 11;
    public static final int OSU_STATUS_SECOND_SOAP_EXCHANGE = 9;
    public static final int OSU_STATUS_SERVER_CONNECTED = 5;
    public static final int OSU_STATUS_SERVER_CONNECTING = 3;
    public static final int OSU_STATUS_SERVER_VALIDATED = 4;
    public static final int OSU_STATUS_THIRD_SOAP_EXCHANGE = 10;
    public static final int OSU_STATUS_WAITING_FOR_REDIRECT_RESPONSE = 7;

    public abstract void onProvisioningComplete();

    public abstract void onProvisioningFailure(int i);

    public abstract void onProvisioningStatus(int i);
}
