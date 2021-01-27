package com.android.server.wifi.hotspot2.soap;

import android.util.SparseArray;
import com.android.server.wifi.hotspot2.omadm.DevDetailMo;
import com.android.server.wifi.hotspot2.omadm.DevInfoMo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SppConstants {
    public static final String ATTRIBUTE_ERROR_CODE = "errorCode";
    public static final String ATTRIBUTE_MO_URN = "moURN";
    public static final String ATTRIBUTE_REDIRECT_URI = "redirectURI";
    public static final String ATTRIBUTE_REQUEST_REASON = "requestReason";
    public static final String ATTRIBUTE_SESSION_ID = "sessionID";
    public static final String ATTRIBUTE_SPP_STATUS = "sppStatus";
    public static final String ATTRIBUTE_SPP_VERSION = "sppVersion";
    public static final int INVALID_SPP_CONSTANT = -1;
    public static final String METHOD_POST_DEV_DATA = "sppPostDevData";
    public static final String METHOD_UPDATE_RESPONSE = "sppUpdateResponse";
    public static final String PROPERTY_MO_CONTAINER = "moContainer";
    public static final String PROPERTY_SPP_ERROR = "sppError";
    public static final String PROPERTY_SUPPORTED_MO_LIST = "supportedMOList";
    public static final String PROPERTY_SUPPORTED_SPP_VERSIONS = "supportedSPPVersions";
    public static final List<String> SUPPORTED_MO_LIST = Arrays.asList("urn:wfa:mo:hotspot2dot0-perprovidersubscription:1.0", DevDetailMo.HS20_URN, DevInfoMo.URN, DevDetailMo.URN);
    public static final String SUPPORTED_SPP_VERSION = "1.0";
    private static final Map<String, Integer> sErrorEnums = new HashMap();
    private static final SparseArray<String> sErrorStrings = new SparseArray<>();
    private static final Map<String, Integer> sStatusEnums = new HashMap();
    private static final SparseArray<String> sStatusStrings = new SparseArray<>();

    static {
        sStatusStrings.put(0, "OK");
        sStatusStrings.put(1, "Provisioning complete, request sppUpdateResponse");
        sStatusStrings.put(2, "Remediation complete, request sppUpdateResponse");
        sStatusStrings.put(3, "Update complete, request sppUpdateResponse");
        sStatusStrings.put(4, "Exchange complete, release TLS connection");
        sStatusStrings.put(5, "No update available at this time");
        sStatusStrings.put(6, "Error occurred");
        for (int i = 0; i < sStatusStrings.size(); i++) {
            sStatusEnums.put(sStatusStrings.valueAt(i).toLowerCase(), Integer.valueOf(sStatusStrings.keyAt(i)));
        }
        sErrorStrings.put(0, "SPP version not supported");
        sErrorStrings.put(1, "One or more mandatory MOs not supported");
        sErrorStrings.put(2, "Credentials cannot be provisioned at this time");
        sErrorStrings.put(3, "Remediation cannot be completed at this time");
        sErrorStrings.put(4, "Provisioning cannot be completed at this time");
        sErrorStrings.put(5, "Continue to use existing certificate");
        sErrorStrings.put(6, "Cookie invalid");
        sErrorStrings.put(7, "No corresponding web-browser-connection Session ID");
        sErrorStrings.put(8, "Permission denied");
        sErrorStrings.put(9, "Command failed");
        sErrorStrings.put(10, "MO addition or update failed");
        sErrorStrings.put(11, "Device full");
        sErrorStrings.put(12, "Bad management tree URI");
        sErrorStrings.put(13, "Requested entity too large");
        sErrorStrings.put(14, "Command not allowed");
        sErrorStrings.put(15, "Command not executed due to user");
        sErrorStrings.put(16, "Not found");
        sErrorStrings.put(17, "Other");
        for (int i2 = 0; i2 < sErrorStrings.size(); i2++) {
            sErrorEnums.put(sErrorStrings.valueAt(i2).toLowerCase(), Integer.valueOf(sErrorStrings.keyAt(i2)));
        }
    }

    public static String mapStatusIntToString(int status) {
        return sStatusStrings.get(status);
    }

    public static int mapStatusStringToInt(String status) {
        Integer value = sStatusEnums.get(status.toLowerCase(Locale.US));
        if (value == null) {
            return -1;
        }
        return value.intValue();
    }

    public static String mapErrorIntToString(int error) {
        return sErrorStrings.get(error);
    }

    public static int mapErrorStringToInt(String error) {
        Integer value = sErrorEnums.get(error.toLowerCase());
        if (value == null) {
            return -1;
        }
        return value.intValue();
    }

    public class SppReason {
        public static final String CERTIFICATE_ENROLLMENT_COMPLETED = "Certificate enrollment completed";
        public static final String CERTIFICATE_ENROLLMENT_FAILED = "Certificate enrollment failed";
        public static final String MO_UPLOAD = "MO upload";
        public static final String NO_ACCEPTABLE_CLIENT_CERTIFICATE = "No acceptable client certificate";
        public static final String POLICY_UPDATE = "Policy update";
        public static final String RETRIEVE_NEXT_COMMAND = "Retrieve next command";
        public static final String SUBSCRIPTION_METADATA_UPDATE = "Subscription metadata update";
        public static final String SUBSCRIPTION_PROVISIONING = "Subscription provisioning";
        public static final String SUBSCRIPTION_REGISTRATION = "Subscription registration";
        public static final String SUBSCRIPTION_REMEDIATION = "Subscription remediation";
        public static final String UNSPECIFIED = "Unspecified";
        public static final String USER_INPUT_COMPLETED = "User input completed";

        public SppReason() {
        }
    }

    public class SppStatus {
        public static final int ERROR = 6;
        public static final int EXCHANGE_COMPLETE = 4;
        public static final int OK = 0;
        public static final int PROV_COMPLETE = 1;
        public static final int REMEDIATION_COMPLETE = 2;
        public static final int UNKOWN = 5;
        public static final int UPDATE_COMPLETE = 3;

        public SppStatus() {
        }
    }

    public class SppError {
        public static final int BAD_TREE_URI = 12;
        public static final int COMMAND_FAILED = 9;
        public static final int COMMAND_NOT_ALLOWED = 14;
        public static final int COOKIE_INVALID = 6;
        public static final int CREDENTIALS_FAILURE = 2;
        public static final int DEVICE_FULL = 11;
        public static final int EXISITING_CERTIFICATE = 5;
        public static final int MOS_NOT_SUPPORTED = 1;
        public static final int MO_ADD_OR_UPDATE_FAILED = 10;
        public static final int NOT_FOUND = 16;
        public static final int OTHER = 17;
        public static final int PERMISSION_DENITED = 8;
        public static final int PROVISIONING_FAILED = 4;
        public static final int REMEDIATION_FAILURE = 3;
        public static final int TOO_LARGE = 13;
        public static final int USER_ABORTED = 15;
        public static final int VERSION_NOT_SUPPORTED = 0;
        public static final int WEB_SESSION_ID = 7;

        public SppError() {
        }
    }
}
