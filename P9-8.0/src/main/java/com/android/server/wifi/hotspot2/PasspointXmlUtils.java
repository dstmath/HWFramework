package com.android.server.wifi.hotspot2;

import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hotspot2.pps.Credential.CertificateCredential;
import android.net.wifi.hotspot2.pps.Credential.SimCredential;
import android.net.wifi.hotspot2.pps.Credential.UserCredential;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.net.wifi.hotspot2.pps.Policy;
import android.net.wifi.hotspot2.pps.Policy.RoamingPartner;
import android.net.wifi.hotspot2.pps.UpdateParameter;
import com.android.internal.util.XmlUtils;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PasspointXmlUtils {
    private static final String XML_TAG_ABLE_TO_SHARE = "AbleToShare";
    private static final String XML_TAG_CERT_SHA256_FINGERPRINT = "CertSHA256Fingerprint";
    private static final String XML_TAG_CERT_TYPE = "CertType";
    private static final String XML_TAG_CHECK_AAA_SERVER_CERT_STATUS = "CheckAAAServerCertStatus";
    private static final String XML_TAG_COUNTRIES = "Countries";
    private static final String XML_TAG_CREATION_TIME = "CreationTime";
    private static final String XML_TAG_CREDENTIAL_PRIORITY = "CredentialPriority";
    private static final String XML_TAG_EAP_TYPE = "EAPType";
    private static final String XML_TAG_EXCLUDED_SSID_LIST = "ExcludedSSIDList";
    private static final String XML_TAG_EXPIRATION_TIME = "ExpirationTime";
    private static final String XML_TAG_FQDN = "FQDN";
    private static final String XML_TAG_FQDN_EXACT_MATCH = "FQDNExactMatch";
    private static final String XML_TAG_FRIENDLY_NAME = "FriendlyName";
    private static final String XML_TAG_HOME_NETWORK_IDS = "HomeNetworkIDs";
    private static final String XML_TAG_ICON_URL = "IconURL";
    private static final String XML_TAG_IMSI = "IMSI";
    private static final String XML_TAG_MACHINE_MANAGED = "MachineManaged";
    private static final String XML_TAG_MATCH_ALL_OIS = "MatchAllOIs";
    private static final String XML_TAG_MATCH_ANY_OIS = "MatchAnyOIs";
    private static final String XML_TAG_MAXIMUM_BSS_LOAD_VALUE = "MaximumBSSLoadValue";
    private static final String XML_TAG_MIN_HOME_DOWNLINK_BANDWIDTH = "MinHomeDownlinkBandwidth";
    private static final String XML_TAG_MIN_HOME_UPLINK_BANDWIDTH = "MinHomeUplinkBandwidth";
    private static final String XML_TAG_MIN_ROAMING_DOWNLINK_BANDWIDTH = "MinRoamingDownlinkBandwidth";
    private static final String XML_TAG_MIN_ROAMING_UPLINK_BANDWIDTH = "MinRoamingUplinkBandwidth";
    private static final String XML_TAG_NON_EAP_INNER_METHOD = "NonEAPInnerMethod";
    private static final String XML_TAG_OTHER_HOME_PARTNERS = "OtherHomePartners";
    private static final String XML_TAG_PASSWORD = "Password";
    private static final String XML_TAG_PORTS = "Ports";
    private static final String XML_TAG_PRIORITY = "Priority";
    private static final String XML_TAG_PROTO = "Proto";
    private static final String XML_TAG_REALM = "Realm";
    private static final String XML_TAG_RESTRICTION = "Restriction";
    private static final String XML_TAG_ROAMING_CONSORTIUM_OIS = "RoamingConsortiumOIs";
    private static final String XML_TAG_SECTION_HEADER_CERT_CREDENTIAL = "CertCredential";
    private static final String XML_TAG_SECTION_HEADER_CREDENTIAL = "Credential";
    private static final String XML_TAG_SECTION_HEADER_HOMESP = "HomeSP";
    private static final String XML_TAG_SECTION_HEADER_POLICY = "Policy";
    private static final String XML_TAG_SECTION_HEADER_POLICY_UPDATE = "PolicyUpdate";
    private static final String XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST = "RoamingPartnerList";
    private static final String XML_TAG_SECTION_HEADER_PROTO_PORT = "ProtoPort";
    private static final String XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP = "RequiredProtoPortMap";
    private static final String XML_TAG_SECTION_HEADER_ROAMING_PARTNER = "RoamingPartner";
    private static final String XML_TAG_SECTION_HEADER_SIM_CREDENTIAL = "SimCredential";
    private static final String XML_TAG_SECTION_HEADER_SUBSCRIPTION_UPDATE = "SubscriptionUpdate";
    private static final String XML_TAG_SECTION_HEADER_USER_CREDENTIAL = "UserCredential";
    private static final String XML_TAG_SERVER_URI = "ServerURI";
    private static final String XML_TAG_SOFT_TOKEN_APP = "SoftTokenApp";
    private static final String XML_TAG_SUBSCRIPTION_CREATION_TIME = "SubscriptionCreationTime";
    private static final String XML_TAG_SUBSCRIPTION_EXPIRATION_TIME = "SubscriptionExpirationTime";
    private static final String XML_TAG_SUBSCRIPTION_TYPE = "SubscriptionType";
    private static final String XML_TAG_TRUST_ROOT_CERT_LIST = "TrustRootCertList";
    private static final String XML_TAG_TRUST_ROOT_CERT_SHA256_FINGERPRINT = "TrustRootCertSHA256Fingerprint";
    private static final String XML_TAG_TRUST_ROOT_CERT_URL = "TrustRootCertURL";
    private static final String XML_TAG_UPDATE_IDENTIFIER = "UpdateIdentifier";
    private static final String XML_TAG_UPDATE_INTERVAL = "UpdateInterval";
    private static final String XML_TAG_UPDATE_METHOD = "UpdateMethod";
    private static final String XML_TAG_USAGE_LIMIT_DATA_LIMIT = "UsageLimitDataLimit";
    private static final String XML_TAG_USAGE_LIMIT_START_TIME = "UsageLimitStartTime";
    private static final String XML_TAG_USAGE_LIMIT_TIME_LIMIT = "UsageLimitTimeLimit";
    private static final String XML_TAG_USAGE_LIMIT_TIME_PERIOD = "UsageLimitTimePeriod";
    private static final String XML_TAG_USERNAME = "Username";

    public static void serializePasspointConfiguration(XmlSerializer out, PasspointConfiguration config) throws XmlPullParserException, IOException {
        XmlUtil.writeNextValue(out, XML_TAG_UPDATE_IDENTIFIER, Integer.valueOf(config.getUpdateIdentifier()));
        XmlUtil.writeNextValue(out, XML_TAG_CREDENTIAL_PRIORITY, Integer.valueOf(config.getCredentialPriority()));
        XmlUtil.writeNextValue(out, XML_TAG_TRUST_ROOT_CERT_LIST, config.getTrustRootCertList());
        XmlUtil.writeNextValue(out, XML_TAG_SUBSCRIPTION_CREATION_TIME, Long.valueOf(config.getSubscriptionCreationTimeInMillis()));
        XmlUtil.writeNextValue(out, XML_TAG_SUBSCRIPTION_EXPIRATION_TIME, Long.valueOf(config.getSubscriptionExpirationTimeInMillis()));
        XmlUtil.writeNextValue(out, XML_TAG_SUBSCRIPTION_TYPE, config.getSubscriptionType());
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_TIME_PERIOD, Long.valueOf(config.getUsageLimitUsageTimePeriodInMinutes()));
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_START_TIME, Long.valueOf(config.getUsageLimitStartTimeInMillis()));
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_DATA_LIMIT, Long.valueOf(config.getUsageLimitDataLimit()));
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_TIME_LIMIT, Long.valueOf(config.getUsageLimitTimeLimitInMinutes()));
        serializeHomeSp(out, config.getHomeSp());
        serializeCredential(out, config.getCredential());
        serializePolicy(out, config.getPolicy());
        serializeUpdateParameter(out, XML_TAG_SECTION_HEADER_SUBSCRIPTION_UPDATE, config.getSubscriptionUpdate());
    }

    public static PasspointConfiguration deserializePasspointConfiguration(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        PasspointConfiguration config = new PasspointConfiguration();
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            String str;
            if (isValueElement(in)) {
                String[] name = new String[1];
                Object value = XmlUtil.readCurrentValue(in, name);
                str = name[0];
                if (str.equals(XML_TAG_UPDATE_IDENTIFIER)) {
                    config.setUpdateIdentifier(((Integer) value).intValue());
                } else if (str.equals(XML_TAG_CREDENTIAL_PRIORITY)) {
                    config.setCredentialPriority(((Integer) value).intValue());
                } else if (str.equals(XML_TAG_TRUST_ROOT_CERT_LIST)) {
                    config.setTrustRootCertList((Map) value);
                } else if (str.equals(XML_TAG_SUBSCRIPTION_CREATION_TIME)) {
                    config.setSubscriptionCreationTimeInMillis(((Long) value).longValue());
                } else if (str.equals(XML_TAG_SUBSCRIPTION_EXPIRATION_TIME)) {
                    config.setSubscriptionExpirationTimeInMillis(((Long) value).longValue());
                } else if (str.equals(XML_TAG_SUBSCRIPTION_TYPE)) {
                    config.setSubscriptionType((String) value);
                } else if (str.equals(XML_TAG_USAGE_LIMIT_TIME_PERIOD)) {
                    config.setUsageLimitUsageTimePeriodInMinutes(((Long) value).longValue());
                } else if (str.equals(XML_TAG_USAGE_LIMIT_START_TIME)) {
                    config.setUsageLimitStartTimeInMillis(((Long) value).longValue());
                } else if (str.equals(XML_TAG_USAGE_LIMIT_DATA_LIMIT)) {
                    config.setUsageLimitDataLimit(((Long) value).longValue());
                } else if (str.equals(XML_TAG_USAGE_LIMIT_TIME_LIMIT)) {
                    config.setUsageLimitTimeLimitInMinutes(((Long) value).longValue());
                } else {
                    throw new XmlPullParserException("Unknown value under PasspointConfiguration: " + in.getName());
                }
            }
            str = in.getName();
            if (str.equals(XML_TAG_SECTION_HEADER_HOMESP)) {
                config.setHomeSp(deserializeHomeSP(in, outerTagDepth + 1));
            } else if (str.equals(XML_TAG_SECTION_HEADER_CREDENTIAL)) {
                config.setCredential(deserializeCredential(in, outerTagDepth + 1));
            } else if (str.equals(XML_TAG_SECTION_HEADER_POLICY)) {
                config.setPolicy(deserializePolicy(in, outerTagDepth + 1));
            } else if (str.equals(XML_TAG_SECTION_HEADER_SUBSCRIPTION_UPDATE)) {
                config.setSubscriptionUpdate(deserializeUpdateParameter(in, outerTagDepth + 1));
            } else {
                throw new XmlPullParserException("Unknown section under PasspointConfiguration: " + in.getName());
            }
        }
        return config;
    }

    private static void serializeHomeSp(XmlSerializer out, HomeSp homeSp) throws XmlPullParserException, IOException {
        if (homeSp != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_HOMESP);
            XmlUtil.writeNextValue(out, "FQDN", homeSp.getFqdn());
            XmlUtil.writeNextValue(out, XML_TAG_FRIENDLY_NAME, homeSp.getFriendlyName());
            XmlUtil.writeNextValue(out, XML_TAG_ICON_URL, homeSp.getIconUrl());
            XmlUtil.writeNextValue(out, XML_TAG_HOME_NETWORK_IDS, homeSp.getHomeNetworkIds());
            XmlUtil.writeNextValue(out, XML_TAG_MATCH_ALL_OIS, homeSp.getMatchAllOis());
            XmlUtil.writeNextValue(out, XML_TAG_MATCH_ANY_OIS, homeSp.getMatchAnyOis());
            XmlUtil.writeNextValue(out, XML_TAG_OTHER_HOME_PARTNERS, homeSp.getOtherHomePartners());
            XmlUtil.writeNextValue(out, "RoamingConsortiumOIs", homeSp.getRoamingConsortiumOis());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_HOMESP);
        }
    }

    private static void serializeCredential(XmlSerializer out, Credential credential) throws XmlPullParserException, IOException {
        if (credential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_CREDENTIAL);
            XmlUtil.writeNextValue(out, "CreationTime", Long.valueOf(credential.getCreationTimeInMillis()));
            XmlUtil.writeNextValue(out, XML_TAG_EXPIRATION_TIME, Long.valueOf(credential.getExpirationTimeInMillis()));
            XmlUtil.writeNextValue(out, "Realm", credential.getRealm());
            XmlUtil.writeNextValue(out, XML_TAG_CHECK_AAA_SERVER_CERT_STATUS, Boolean.valueOf(credential.getCheckAaaServerCertStatus()));
            serializeUserCredential(out, credential.getUserCredential());
            serializeCertCredential(out, credential.getCertCredential());
            serializeSimCredential(out, credential.getSimCredential());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_CREDENTIAL);
        }
    }

    private static void serializePolicy(XmlSerializer out, Policy policy) throws XmlPullParserException, IOException {
        if (policy != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_POLICY);
            XmlUtil.writeNextValue(out, XML_TAG_MIN_HOME_DOWNLINK_BANDWIDTH, Long.valueOf(policy.getMinHomeDownlinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_MIN_HOME_UPLINK_BANDWIDTH, Long.valueOf(policy.getMinHomeUplinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_MIN_ROAMING_DOWNLINK_BANDWIDTH, Long.valueOf(policy.getMinRoamingDownlinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_MIN_ROAMING_UPLINK_BANDWIDTH, Long.valueOf(policy.getMinRoamingUplinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_EXCLUDED_SSID_LIST, policy.getExcludedSsidList());
            XmlUtil.writeNextValue(out, XML_TAG_MAXIMUM_BSS_LOAD_VALUE, Integer.valueOf(policy.getMaximumBssLoadValue()));
            serializeProtoPortMap(out, policy.getRequiredProtoPortMap());
            serializeUpdateParameter(out, XML_TAG_SECTION_HEADER_POLICY_UPDATE, policy.getPolicyUpdate());
            serializePreferredRoamingPartnerList(out, policy.getPreferredRoamingPartnerList());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_POLICY);
        }
    }

    private static void serializeUserCredential(XmlSerializer out, UserCredential userCredential) throws XmlPullParserException, IOException {
        if (userCredential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_USER_CREDENTIAL);
            XmlUtil.writeNextValue(out, XML_TAG_USERNAME, userCredential.getUsername());
            XmlUtil.writeNextValue(out, "Password", userCredential.getPassword());
            XmlUtil.writeNextValue(out, XML_TAG_MACHINE_MANAGED, Boolean.valueOf(userCredential.getMachineManaged()));
            XmlUtil.writeNextValue(out, XML_TAG_SOFT_TOKEN_APP, userCredential.getSoftTokenApp());
            XmlUtil.writeNextValue(out, XML_TAG_ABLE_TO_SHARE, Boolean.valueOf(userCredential.getAbleToShare()));
            XmlUtil.writeNextValue(out, XML_TAG_EAP_TYPE, Integer.valueOf(userCredential.getEapType()));
            XmlUtil.writeNextValue(out, XML_TAG_NON_EAP_INNER_METHOD, userCredential.getNonEapInnerMethod());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_USER_CREDENTIAL);
        }
    }

    private static void serializeCertCredential(XmlSerializer out, CertificateCredential certCredential) throws XmlPullParserException, IOException {
        if (certCredential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_CERT_CREDENTIAL);
            XmlUtil.writeNextValue(out, XML_TAG_CERT_TYPE, certCredential.getCertType());
            XmlUtil.writeNextValue(out, XML_TAG_CERT_SHA256_FINGERPRINT, certCredential.getCertSha256Fingerprint());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_CERT_CREDENTIAL);
        }
    }

    private static void serializeSimCredential(XmlSerializer out, SimCredential simCredential) throws XmlPullParserException, IOException {
        if (simCredential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_SIM_CREDENTIAL);
            XmlUtil.writeNextValue(out, XML_TAG_IMSI, simCredential.getImsi());
            XmlUtil.writeNextValue(out, XML_TAG_EAP_TYPE, Integer.valueOf(simCredential.getEapType()));
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_SIM_CREDENTIAL);
        }
    }

    private static void serializePreferredRoamingPartnerList(XmlSerializer out, List<RoamingPartner> preferredRoamingPartnerList) throws XmlPullParserException, IOException {
        if (preferredRoamingPartnerList != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST);
            for (RoamingPartner partner : preferredRoamingPartnerList) {
                XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_ROAMING_PARTNER);
                XmlUtil.writeNextValue(out, "FQDN", partner.getFqdn());
                XmlUtil.writeNextValue(out, XML_TAG_FQDN_EXACT_MATCH, Boolean.valueOf(partner.getFqdnExactMatch()));
                XmlUtil.writeNextValue(out, "Priority", Integer.valueOf(partner.getPriority()));
                XmlUtil.writeNextValue(out, XML_TAG_COUNTRIES, partner.getCountries());
                XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_ROAMING_PARTNER);
            }
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST);
        }
    }

    private static void serializeUpdateParameter(XmlSerializer out, String type, UpdateParameter param) throws XmlPullParserException, IOException {
        if (param != null) {
            XmlUtil.writeNextSectionStart(out, type);
            XmlUtil.writeNextValue(out, XML_TAG_UPDATE_INTERVAL, Long.valueOf(param.getUpdateIntervalInMinutes()));
            XmlUtil.writeNextValue(out, XML_TAG_UPDATE_METHOD, param.getUpdateMethod());
            XmlUtil.writeNextValue(out, XML_TAG_RESTRICTION, param.getRestriction());
            XmlUtil.writeNextValue(out, XML_TAG_SERVER_URI, param.getServerUri());
            XmlUtil.writeNextValue(out, XML_TAG_USERNAME, param.getUsername());
            XmlUtil.writeNextValue(out, "Password", param.getBase64EncodedPassword());
            XmlUtil.writeNextValue(out, XML_TAG_TRUST_ROOT_CERT_URL, param.getTrustRootCertUrl());
            XmlUtil.writeNextValue(out, XML_TAG_TRUST_ROOT_CERT_SHA256_FINGERPRINT, param.getTrustRootCertSha256Fingerprint());
            XmlUtil.writeNextSectionEnd(out, type);
        }
    }

    private static void serializeProtoPortMap(XmlSerializer out, Map<Integer, String> protoPortMap) throws XmlPullParserException, IOException {
        if (protoPortMap != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP);
            for (Entry<Integer, String> entry : protoPortMap.entrySet()) {
                XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PROTO_PORT);
                XmlUtil.writeNextValue(out, XML_TAG_PROTO, entry.getKey());
                XmlUtil.writeNextValue(out, XML_TAG_PORTS, entry.getValue());
                XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PROTO_PORT);
            }
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP);
        }
    }

    private static HomeSp deserializeHomeSP(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        HomeSp homeSp = new HomeSp();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] == null) {
                throw new XmlPullParserException("Missing value name");
            }
            String str = valueName[0];
            if (str.equals("FQDN")) {
                homeSp.setFqdn((String) value);
            } else if (str.equals(XML_TAG_FRIENDLY_NAME)) {
                homeSp.setFriendlyName((String) value);
            } else if (str.equals(XML_TAG_ICON_URL)) {
                homeSp.setIconUrl((String) value);
            } else if (str.equals(XML_TAG_HOME_NETWORK_IDS)) {
                homeSp.setHomeNetworkIds((Map) value);
            } else if (str.equals(XML_TAG_MATCH_ALL_OIS)) {
                homeSp.setMatchAllOis((long[]) value);
            } else if (str.equals(XML_TAG_MATCH_ANY_OIS)) {
                homeSp.setMatchAnyOis((long[]) value);
            } else if (str.equals("RoamingConsortiumOIs")) {
                homeSp.setRoamingConsortiumOis((long[]) value);
            } else if (str.equals(XML_TAG_OTHER_HOME_PARTNERS)) {
                homeSp.setOtherHomePartners((String[]) value);
            } else {
                throw new XmlPullParserException("Unknown data under HomeSP: " + valueName[0]);
            }
        }
        return homeSp;
    }

    private static Credential deserializeCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Credential credential = new Credential();
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            String str;
            if (isValueElement(in)) {
                String[] name = new String[1];
                Object value = XmlUtil.readCurrentValue(in, name);
                str = name[0];
                if (str.equals("CreationTime")) {
                    credential.setCreationTimeInMillis(((Long) value).longValue());
                } else if (str.equals(XML_TAG_EXPIRATION_TIME)) {
                    credential.setExpirationTimeInMillis(((Long) value).longValue());
                } else if (str.equals("Realm")) {
                    credential.setRealm((String) value);
                } else if (str.equals(XML_TAG_CHECK_AAA_SERVER_CERT_STATUS)) {
                    credential.setCheckAaaServerCertStatus(((Boolean) value).booleanValue());
                } else {
                    throw new XmlPullParserException("Unknown value under Credential: " + name[0]);
                }
            }
            str = in.getName();
            if (str.equals(XML_TAG_SECTION_HEADER_USER_CREDENTIAL)) {
                credential.setUserCredential(deserializeUserCredential(in, outerTagDepth + 1));
            } else if (str.equals(XML_TAG_SECTION_HEADER_CERT_CREDENTIAL)) {
                credential.setCertCredential(deserializeCertCredential(in, outerTagDepth + 1));
            } else if (str.equals(XML_TAG_SECTION_HEADER_SIM_CREDENTIAL)) {
                credential.setSimCredential(deserializeSimCredential(in, outerTagDepth + 1));
            } else {
                throw new XmlPullParserException("Unknown section under Credential: " + in.getName());
            }
        }
        return credential;
    }

    private static Policy deserializePolicy(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Policy policy = new Policy();
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            String str;
            if (isValueElement(in)) {
                String[] name = new String[1];
                Object value = XmlUtil.readCurrentValue(in, name);
                str = name[0];
                if (str.equals(XML_TAG_MIN_HOME_DOWNLINK_BANDWIDTH)) {
                    policy.setMinHomeDownlinkBandwidth(((Long) value).longValue());
                } else if (str.equals(XML_TAG_MIN_HOME_UPLINK_BANDWIDTH)) {
                    policy.setMinHomeUplinkBandwidth(((Long) value).longValue());
                } else if (str.equals(XML_TAG_MIN_ROAMING_DOWNLINK_BANDWIDTH)) {
                    policy.setMinRoamingDownlinkBandwidth(((Long) value).longValue());
                } else if (str.equals(XML_TAG_MIN_ROAMING_UPLINK_BANDWIDTH)) {
                    policy.setMinRoamingUplinkBandwidth(((Long) value).longValue());
                } else if (str.equals(XML_TAG_EXCLUDED_SSID_LIST)) {
                    policy.setExcludedSsidList((String[]) value);
                } else if (str.equals(XML_TAG_MAXIMUM_BSS_LOAD_VALUE)) {
                    policy.setMaximumBssLoadValue(((Integer) value).intValue());
                }
            } else {
                str = in.getName();
                if (str.equals(XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP)) {
                    policy.setRequiredProtoPortMap(deserializeProtoPortMap(in, outerTagDepth + 1));
                } else if (str.equals(XML_TAG_SECTION_HEADER_POLICY_UPDATE)) {
                    policy.setPolicyUpdate(deserializeUpdateParameter(in, outerTagDepth + 1));
                } else if (str.equals(XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST)) {
                    policy.setPreferredRoamingPartnerList(deserializePreferredRoamingPartnerList(in, outerTagDepth + 1));
                } else {
                    throw new XmlPullParserException("Unknown section under Policy: " + in.getName());
                }
            }
        }
        return policy;
    }

    private static UserCredential deserializeUserCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        UserCredential userCredential = new UserCredential();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] == null) {
                throw new XmlPullParserException("Missing value name");
            }
            String str = valueName[0];
            if (str.equals(XML_TAG_USERNAME)) {
                userCredential.setUsername((String) value);
            } else if (str.equals("Password")) {
                userCredential.setPassword((String) value);
            } else if (str.equals(XML_TAG_MACHINE_MANAGED)) {
                userCredential.setMachineManaged(((Boolean) value).booleanValue());
            } else if (str.equals(XML_TAG_SOFT_TOKEN_APP)) {
                userCredential.setSoftTokenApp((String) value);
            } else if (str.equals(XML_TAG_ABLE_TO_SHARE)) {
                userCredential.setAbleToShare(((Boolean) value).booleanValue());
            } else if (str.equals(XML_TAG_EAP_TYPE)) {
                userCredential.setEapType(((Integer) value).intValue());
            } else if (str.equals(XML_TAG_NON_EAP_INNER_METHOD)) {
                userCredential.setNonEapInnerMethod((String) value);
            } else {
                throw new XmlPullParserException("Unknown value under UserCredential: " + valueName[0]);
            }
        }
        return userCredential;
    }

    private static CertificateCredential deserializeCertCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        CertificateCredential certCredential = new CertificateCredential();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] == null) {
                throw new XmlPullParserException("Missing value name");
            }
            String str = valueName[0];
            if (str.equals(XML_TAG_CERT_TYPE)) {
                certCredential.setCertType((String) value);
            } else if (str.equals(XML_TAG_CERT_SHA256_FINGERPRINT)) {
                certCredential.setCertSha256Fingerprint((byte[]) value);
            } else {
                throw new XmlPullParserException("Unknown value under CertCredential: " + valueName[0]);
            }
        }
        return certCredential;
    }

    private static SimCredential deserializeSimCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        SimCredential simCredential = new SimCredential();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] == null) {
                throw new XmlPullParserException("Missing value name");
            }
            String str = valueName[0];
            if (str.equals(XML_TAG_IMSI)) {
                simCredential.setImsi((String) value);
            } else if (str.equals(XML_TAG_EAP_TYPE)) {
                simCredential.setEapType(((Integer) value).intValue());
            } else {
                throw new XmlPullParserException("Unknown value under CertCredential: " + valueName[0]);
            }
        }
        return simCredential;
    }

    private static List<RoamingPartner> deserializePreferredRoamingPartnerList(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        List<RoamingPartner> roamingPartnerList = new ArrayList();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_ROAMING_PARTNER, outerTagDepth)) {
            roamingPartnerList.add(deserializeRoamingPartner(in, outerTagDepth + 1));
        }
        return roamingPartnerList;
    }

    private static RoamingPartner deserializeRoamingPartner(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        RoamingPartner partner = new RoamingPartner();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] == null) {
                throw new XmlPullParserException("Missing value name");
            }
            String str = valueName[0];
            if (str.equals("FQDN")) {
                partner.setFqdn((String) value);
            } else if (str.equals(XML_TAG_FQDN_EXACT_MATCH)) {
                partner.setFqdnExactMatch(((Boolean) value).booleanValue());
            } else if (str.equals("Priority")) {
                partner.setPriority(((Integer) value).intValue());
            } else if (str.equals(XML_TAG_COUNTRIES)) {
                partner.setCountries((String) value);
            } else {
                throw new XmlPullParserException("Unknown value under RoamingPartner: " + valueName[0]);
            }
        }
        return partner;
    }

    private static UpdateParameter deserializeUpdateParameter(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        UpdateParameter param = new UpdateParameter();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] == null) {
                throw new XmlPullParserException("Missing value name");
            }
            String str = valueName[0];
            if (str.equals(XML_TAG_UPDATE_INTERVAL)) {
                param.setUpdateIntervalInMinutes(((Long) value).longValue());
            } else if (str.equals(XML_TAG_UPDATE_METHOD)) {
                param.setUpdateMethod((String) value);
            } else if (str.equals(XML_TAG_RESTRICTION)) {
                param.setRestriction((String) value);
            } else if (str.equals(XML_TAG_SERVER_URI)) {
                param.setServerUri((String) value);
            } else if (str.equals(XML_TAG_USERNAME)) {
                param.setUsername((String) value);
            } else if (str.equals("Password")) {
                param.setBase64EncodedPassword((String) value);
            } else if (str.equals(XML_TAG_TRUST_ROOT_CERT_URL)) {
                param.setTrustRootCertUrl((String) value);
            } else if (str.equals(XML_TAG_TRUST_ROOT_CERT_SHA256_FINGERPRINT)) {
                param.setTrustRootCertSha256Fingerprint((byte[]) value);
            } else {
                throw new XmlPullParserException("Unknown value under UpdateParameter: " + valueName[0]);
            }
        }
        return param;
    }

    private static Map<Integer, String> deserializeProtoPortMap(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Map<Integer, String> protoPortMap = new HashMap();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_PROTO_PORT, outerTagDepth)) {
            String ports = (String) XmlUtil.readNextValueWithName(in, XML_TAG_PORTS);
            protoPortMap.put(Integer.valueOf(((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_PROTO)).intValue()), ports);
        }
        return protoPortMap;
    }

    private static boolean isValueElement(XmlPullParser in) {
        return in.getAttributeValue(null, "name") != null;
    }
}
