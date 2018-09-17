package android.net.wifi.hotspot2.omadm;

import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hotspot2.pps.Credential.CertificateCredential;
import android.net.wifi.hotspot2.pps.Credential.SimCredential;
import android.net.wifi.hotspot2.pps.Credential.UserCredential;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.net.wifi.hotspot2.pps.Policy;
import android.net.wifi.hotspot2.pps.Policy.RoamingPartner;
import android.net.wifi.hotspot2.pps.UpdateParameter;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PpsMoParser {
    private static final String NODE_AAA_SERVER_TRUST_ROOT = "AAAServerTrustRoot";
    private static final String NODE_ABLE_TO_SHARE = "AbleToShare";
    private static final String NODE_CERTIFICATE_TYPE = "CertificateType";
    private static final String NODE_CERT_SHA256_FINGERPRINT = "CertSHA256Fingerprint";
    private static final String NODE_CERT_URL = "CertURL";
    private static final String NODE_CHECK_AAA_SERVER_CERT_STATUS = "CheckAAAServerCertStatus";
    private static final String NODE_COUNTRY = "Country";
    private static final String NODE_CREATION_DATE = "CreationDate";
    private static final String NODE_CREDENTIAL = "Credential";
    private static final String NODE_CREDENTIAL_PRIORITY = "CredentialPriority";
    private static final String NODE_DATA_LIMIT = "DataLimit";
    private static final String NODE_DIGITAL_CERTIFICATE = "DigitalCertificate";
    private static final String NODE_DOWNLINK_BANDWIDTH = "DLBandwidth";
    private static final String NODE_EAP_METHOD = "EAPMethod";
    private static final String NODE_EAP_TYPE = "EAPType";
    private static final String NODE_EXPIRATION_DATE = "ExpirationDate";
    private static final String NODE_EXTENSION = "Extension";
    private static final String NODE_FQDN = "FQDN";
    private static final String NODE_FQDN_MATCH = "FQDN_Match";
    private static final String NODE_FRIENDLY_NAME = "FriendlyName";
    private static final String NODE_HESSID = "HESSID";
    private static final String NODE_HOMESP = "HomeSP";
    private static final String NODE_HOME_OI = "HomeOI";
    private static final String NODE_HOME_OI_LIST = "HomeOIList";
    private static final String NODE_HOME_OI_REQUIRED = "HomeOIRequired";
    private static final String NODE_ICON_URL = "IconURL";
    private static final String NODE_INNER_EAP_TYPE = "InnerEAPType";
    private static final String NODE_INNER_METHOD = "InnerMethod";
    private static final String NODE_INNER_VENDOR_ID = "InnerVendorID";
    private static final String NODE_INNER_VENDOR_TYPE = "InnerVendorType";
    private static final String NODE_IP_PROTOCOL = "IPProtocol";
    private static final String NODE_MACHINE_MANAGED = "MachineManaged";
    private static final String NODE_MAXIMUM_BSS_LOAD_VALUE = "MaximumBSSLoadValue";
    private static final String NODE_MIN_BACKHAUL_THRESHOLD = "MinBackhaulThreshold";
    private static final String NODE_NETWORK_ID = "NetworkID";
    private static final String NODE_NETWORK_TYPE = "NetworkType";
    private static final String NODE_OTHER = "Other";
    private static final String NODE_OTHER_HOME_PARTNERS = "OtherHomePartners";
    private static final String NODE_PASSWORD = "Password";
    private static final String NODE_PER_PROVIDER_SUBSCRIPTION = "PerProviderSubscription";
    private static final String NODE_POLICY = "Policy";
    private static final String NODE_POLICY_UPDATE = "PolicyUpdate";
    private static final String NODE_PORT_NUMBER = "PortNumber";
    private static final String NODE_PREFERRED_ROAMING_PARTNER_LIST = "PreferredRoamingPartnerList";
    private static final String NODE_PRIORITY = "Priority";
    private static final String NODE_REALM = "Realm";
    private static final String NODE_REQUIRED_PROTO_PORT_TUPLE = "RequiredProtoPortTuple";
    private static final String NODE_RESTRICTION = "Restriction";
    private static final String NODE_ROAMING_CONSORTIUM_OI = "RoamingConsortiumOI";
    private static final String NODE_SIM = "SIM";
    private static final String NODE_SIM_IMSI = "IMSI";
    private static final String NODE_SOFT_TOKEN_APP = "SoftTokenApp";
    private static final String NODE_SP_EXCLUSION_LIST = "SPExclusionList";
    private static final String NODE_SSID = "SSID";
    private static final String NODE_START_DATE = "StartDate";
    private static final String NODE_SUBSCRIPTION_PARAMETER = "SubscriptionParameter";
    private static final String NODE_SUBSCRIPTION_UPDATE = "SubscriptionUpdate";
    private static final String NODE_TIME_LIMIT = "TimeLimit";
    private static final String NODE_TRUST_ROOT = "TrustRoot";
    private static final String NODE_TYPE_OF_SUBSCRIPTION = "TypeOfSubscription";
    private static final String NODE_UPDATE_IDENTIFIER = "UpdateIdentifier";
    private static final String NODE_UPDATE_INTERVAL = "UpdateInterval";
    private static final String NODE_UPDATE_METHOD = "UpdateMethod";
    private static final String NODE_UPLINK_BANDWIDTH = "ULBandwidth";
    private static final String NODE_URI = "URI";
    private static final String NODE_USAGE_LIMITS = "UsageLimits";
    private static final String NODE_USAGE_TIME_PERIOD = "UsageTimePeriod";
    private static final String NODE_USERNAME = "Username";
    private static final String NODE_USERNAME_PASSWORD = "UsernamePassword";
    private static final String NODE_VENDOR_ID = "VendorId";
    private static final String NODE_VENDOR_TYPE = "VendorType";
    private static final String PPS_MO_URN = "urn:wfa:mo:hotspot2dot0-perprovidersubscription:1.0";
    private static final String TAG = "PpsMoParser";
    private static final String TAG_DDF_NAME = "DDFName";
    private static final String TAG_MANAGEMENT_TREE = "MgmtTree";
    private static final String TAG_NODE = "Node";
    private static final String TAG_NODE_NAME = "NodeName";
    private static final String TAG_RT_PROPERTIES = "RTProperties";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_VALUE = "Value";
    private static final String TAG_VER_DTD = "VerDTD";

    private static abstract class PPSNode {
        private final String mName;

        public abstract List<PPSNode> getChildren();

        public abstract String getValue();

        public abstract boolean isLeaf();

        public PPSNode(String name) {
            this.mName = name;
        }

        public String getName() {
            return this.mName;
        }
    }

    private static class InternalNode extends PPSNode {
        private final List<PPSNode> mChildren;

        public InternalNode(String nodeName, List<PPSNode> children) {
            super(nodeName);
            this.mChildren = children;
        }

        public String getValue() {
            return null;
        }

        public List<PPSNode> getChildren() {
            return this.mChildren;
        }

        public boolean isLeaf() {
            return false;
        }
    }

    private static class LeafNode extends PPSNode {
        private final String mValue;

        public LeafNode(String nodeName, String value) {
            super(nodeName);
            this.mValue = value;
        }

        public String getValue() {
            return this.mValue;
        }

        public List<PPSNode> getChildren() {
            return null;
        }

        public boolean isLeaf() {
            return true;
        }
    }

    private static class ParsingException extends Exception {
        public ParsingException(String message) {
            super(message);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x000e A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:1:0x0007} */
    /* JADX WARNING: Missing block: B:6:0x000f, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static PasspointConfiguration parseMoText(String xmlString) {
        try {
            XMLNode root = new XMLParser().parse(xmlString);
            if (root == null) {
                return null;
            }
            if (root.getTag() != TAG_MANAGEMENT_TREE) {
                Log.e(TAG, "Root is not a MgmtTree");
                return null;
            }
            String verDtd = null;
            PasspointConfiguration config = null;
            for (XMLNode child : root.getChildren()) {
                String tag = child.getTag();
                if (tag.equals(TAG_VER_DTD)) {
                    if (verDtd != null) {
                        Log.e(TAG, "Duplicate VerDTD element");
                        return null;
                    }
                    verDtd = child.getText();
                } else if (!tag.equals(TAG_NODE)) {
                    Log.e(TAG, "Unknown node: " + child.getTag());
                    return null;
                } else if (config != null) {
                    Log.e(TAG, "Unexpected multiple Node element under MgmtTree");
                    return null;
                } else {
                    try {
                        config = parsePpsNode(child);
                    } catch (ParsingException e) {
                        Log.e(TAG, e.getMessage());
                        return null;
                    }
                }
            }
            return config;
        } catch (IOException e2) {
        }
    }

    private static PasspointConfiguration parsePpsNode(XMLNode node) throws ParsingException {
        PasspointConfiguration config = null;
        String nodeName = null;
        int updateIdentifier = Integer.MIN_VALUE;
        for (XMLNode child : node.getChildren()) {
            String tag = child.getTag();
            if (tag.equals(TAG_NODE_NAME)) {
                if (nodeName != null) {
                    throw new ParsingException("Duplicate NodeName: " + child.getText());
                }
                nodeName = child.getText();
                if (!TextUtils.equals(nodeName, NODE_PER_PROVIDER_SUBSCRIPTION)) {
                    throw new ParsingException("Unexpected NodeName: " + nodeName);
                }
            } else if (tag.equals(TAG_NODE)) {
                PPSNode ppsNodeRoot = buildPpsNode(child);
                if (TextUtils.equals(ppsNodeRoot.getName(), NODE_UPDATE_IDENTIFIER)) {
                    if (updateIdentifier != Integer.MIN_VALUE) {
                        throw new ParsingException("Multiple node for UpdateIdentifier");
                    }
                    updateIdentifier = parseInteger(getPpsNodeValue(ppsNodeRoot));
                } else if (config != null) {
                    throw new ParsingException("Multiple PPS instance");
                } else {
                    config = parsePpsInstance(ppsNodeRoot);
                }
            } else if (tag.equals(TAG_RT_PROPERTIES)) {
                String urn = parseUrn(child);
                if (!TextUtils.equals(urn, PPS_MO_URN)) {
                    throw new ParsingException("Unknown URN: " + urn);
                }
            } else {
                throw new ParsingException("Unknown tag under PPS node: " + child.getTag());
            }
        }
        if (!(config == null || updateIdentifier == Integer.MIN_VALUE)) {
            config.setUpdateIdentifier(updateIdentifier);
        }
        return config;
    }

    private static String parseUrn(XMLNode node) throws ParsingException {
        if (node.getChildren().size() != 1) {
            throw new ParsingException("Expect RTPProperties node to only have one child");
        }
        XMLNode typeNode = (XMLNode) node.getChildren().get(0);
        if (typeNode.getChildren().size() != 1) {
            throw new ParsingException("Expect Type node to only have one child");
        } else if (TextUtils.equals(typeNode.getTag(), TAG_TYPE)) {
            XMLNode ddfNameNode = (XMLNode) typeNode.getChildren().get(0);
            if (!ddfNameNode.getChildren().isEmpty()) {
                throw new ParsingException("Expect DDFName node to have no child");
            } else if (TextUtils.equals(ddfNameNode.getTag(), TAG_DDF_NAME)) {
                return ddfNameNode.getText();
            } else {
                throw new ParsingException("Unexpected tag for DDFName: " + ddfNameNode.getTag());
            }
        } else {
            throw new ParsingException("Unexpected tag for Type: " + typeNode.getTag());
        }
    }

    private static PPSNode buildPpsNode(XMLNode node) throws ParsingException {
        String nodeName = null;
        String nodeValue = null;
        List<PPSNode> childNodes = new ArrayList();
        Set<String> parsedNodes = new HashSet();
        for (XMLNode child : node.getChildren()) {
            String tag = child.getTag();
            if (TextUtils.equals(tag, TAG_NODE_NAME)) {
                if (nodeName != null) {
                    throw new ParsingException("Duplicate NodeName node");
                }
                nodeName = child.getText();
            } else if (TextUtils.equals(tag, TAG_NODE)) {
                PPSNode ppsNode = buildPpsNode(child);
                if (parsedNodes.contains(ppsNode.getName())) {
                    throw new ParsingException("Duplicate node: " + ppsNode.getName());
                }
                parsedNodes.add(ppsNode.getName());
                childNodes.add(ppsNode);
            } else if (!TextUtils.equals(tag, TAG_VALUE)) {
                throw new ParsingException("Unknown tag: " + tag);
            } else if (nodeValue != null) {
                throw new ParsingException("Duplicate Value node");
            } else {
                nodeValue = child.getText();
            }
        }
        if (nodeName == null) {
            throw new ParsingException("Invalid node: missing NodeName");
        } else if (nodeValue == null && childNodes.size() == 0) {
            throw new ParsingException("Invalid node: " + nodeName + " missing both value and children");
        } else if (nodeValue != null && childNodes.size() > 0) {
            throw new ParsingException("Invalid node: " + nodeName + " contained both value and children");
        } else if (nodeValue != null) {
            return new LeafNode(nodeName, nodeValue);
        } else {
            return new InternalNode(nodeName, childNodes);
        }
    }

    private static String getPpsNodeValue(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            return node.getValue();
        }
        throw new ParsingException("Cannot get value from a non-leaf node: " + node.getName());
    }

    private static PasspointConfiguration parsePpsInstance(PPSNode root) throws ParsingException {
        if (root.isLeaf()) {
            throw new ParsingException("Leaf node not expected for PPS instance");
        }
        PasspointConfiguration config = new PasspointConfiguration();
        for (PPSNode child : root.getChildren()) {
            String name = child.getName();
            if (name.equals("HomeSP")) {
                config.setHomeSp(parseHomeSP(child));
            } else if (name.equals(NODE_CREDENTIAL)) {
                config.setCredential(parseCredential(child));
            } else if (name.equals(NODE_POLICY)) {
                config.setPolicy(parsePolicy(child));
            } else if (name.equals(NODE_AAA_SERVER_TRUST_ROOT)) {
                config.setTrustRootCertList(parseAAAServerTrustRootList(child));
            } else if (name.equals(NODE_SUBSCRIPTION_UPDATE)) {
                config.setSubscriptionUpdate(parseUpdateParameter(child));
            } else if (name.equals(NODE_SUBSCRIPTION_PARAMETER)) {
                parseSubscriptionParameter(child, config);
            } else if (name.equals(NODE_CREDENTIAL_PRIORITY)) {
                config.setCredentialPriority(parseInteger(getPpsNodeValue(child)));
            } else if (name.equals(NODE_EXTENSION)) {
                Log.d(TAG, "Ignore Extension node for vendor specific information");
            } else {
                throw new ParsingException("Unknown node: " + child.getName());
            }
        }
        return config;
    }

    private static HomeSp parseHomeSP(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for HomeSP");
        }
        HomeSp homeSp = new HomeSp();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_FQDN)) {
                homeSp.setFqdn(getPpsNodeValue(child));
            } else if (name.equals(NODE_FRIENDLY_NAME)) {
                homeSp.setFriendlyName(getPpsNodeValue(child));
            } else if (name.equals(NODE_ROAMING_CONSORTIUM_OI)) {
                homeSp.setRoamingConsortiumOis(parseRoamingConsortiumOI(getPpsNodeValue(child)));
            } else if (name.equals(NODE_ICON_URL)) {
                homeSp.setIconUrl(getPpsNodeValue(child));
            } else if (name.equals(NODE_NETWORK_ID)) {
                homeSp.setHomeNetworkIds(parseNetworkIds(child));
            } else if (name.equals(NODE_HOME_OI_LIST)) {
                Pair<List<Long>, List<Long>> homeOIs = parseHomeOIList(child);
                homeSp.setMatchAllOis(convertFromLongList((List) homeOIs.first));
                homeSp.setMatchAnyOis(convertFromLongList((List) homeOIs.second));
            } else if (name.equals(NODE_OTHER_HOME_PARTNERS)) {
                homeSp.setOtherHomePartners(parseOtherHomePartners(child));
            } else {
                throw new ParsingException("Unknown node under HomeSP: " + child.getName());
            }
        }
        return homeSp;
    }

    private static long[] parseRoamingConsortiumOI(String oiStr) throws ParsingException {
        String[] oiStrArray = oiStr.split(",");
        long[] oiArray = new long[oiStrArray.length];
        for (int i = 0; i < oiStrArray.length; i++) {
            oiArray[i] = parseLong(oiStrArray[i], 16);
        }
        return oiArray;
    }

    private static Map<String, Long> parseNetworkIds(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for NetworkID");
        }
        Map<String, Long> networkIds = new HashMap();
        for (PPSNode child : node.getChildren()) {
            Pair<String, Long> networkId = parseNetworkIdInstance(child);
            networkIds.put((String) networkId.first, (Long) networkId.second);
        }
        return networkIds;
    }

    private static Pair<String, Long> parseNetworkIdInstance(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for NetworkID instance");
        }
        Object ssid = null;
        Object hessid = null;
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_SSID)) {
                ssid = getPpsNodeValue(child);
            } else if (name.equals(NODE_HESSID)) {
                hessid = Long.valueOf(parseLong(getPpsNodeValue(child), 16));
            } else {
                throw new ParsingException("Unknown node under NetworkID instance: " + child.getName());
            }
        }
        if (ssid != null) {
            return new Pair(ssid, hessid);
        }
        throw new ParsingException("NetworkID instance missing SSID");
    }

    private static Pair<List<Long>, List<Long>> parseHomeOIList(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for HomeOIList");
        }
        List<Long> matchAllOIs = new ArrayList();
        List<Long> matchAnyOIs = new ArrayList();
        for (PPSNode child : node.getChildren()) {
            Pair<Long, Boolean> homeOI = parseHomeOIInstance(child);
            if (((Boolean) homeOI.second).booleanValue()) {
                matchAllOIs.add((Long) homeOI.first);
            } else {
                matchAnyOIs.add((Long) homeOI.first);
            }
        }
        return new Pair(matchAllOIs, matchAnyOIs);
    }

    private static Pair<Long, Boolean> parseHomeOIInstance(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for HomeOI instance");
        }
        Object oi = null;
        Object required = null;
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_HOME_OI)) {
                try {
                    oi = Long.valueOf(getPpsNodeValue(child), 16);
                } catch (NumberFormatException e) {
                    throw new ParsingException("Invalid HomeOI: " + getPpsNodeValue(child));
                }
            } else if (name.equals(NODE_HOME_OI_REQUIRED)) {
                required = Boolean.valueOf(getPpsNodeValue(child));
            } else {
                throw new ParsingException("Unknown node under NetworkID instance: " + child.getName());
            }
        }
        if (oi == null) {
            throw new ParsingException("HomeOI instance missing OI field");
        } else if (required != null) {
            return new Pair(oi, required);
        } else {
            throw new ParsingException("HomeOI instance missing required field");
        }
    }

    private static String[] parseOtherHomePartners(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for OtherHomePartners");
        }
        List<String> otherHomePartners = new ArrayList();
        for (PPSNode child : node.getChildren()) {
            otherHomePartners.add(parseOtherHomePartnerInstance(child));
        }
        return (String[]) otherHomePartners.toArray(new String[otherHomePartners.size()]);
    }

    private static String parseOtherHomePartnerInstance(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for OtherHomePartner instance");
        }
        String fqdn = null;
        for (PPSNode child : node.getChildren()) {
            if (child.getName().equals(NODE_FQDN)) {
                fqdn = getPpsNodeValue(child);
            } else {
                throw new ParsingException("Unknown node under OtherHomePartner instance: " + child.getName());
            }
        }
        if (fqdn != null) {
            return fqdn;
        }
        throw new ParsingException("OtherHomePartner instance missing FQDN field");
    }

    private static Credential parseCredential(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for HomeSP");
        }
        Credential credential = new Credential();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_CREATION_DATE)) {
                credential.setCreationTimeInMillis(parseDate(getPpsNodeValue(child)));
            } else if (name.equals(NODE_EXPIRATION_DATE)) {
                credential.setExpirationTimeInMillis(parseDate(getPpsNodeValue(child)));
            } else if (name.equals(NODE_USERNAME_PASSWORD)) {
                credential.setUserCredential(parseUserCredential(child));
            } else if (name.equals(NODE_DIGITAL_CERTIFICATE)) {
                credential.setCertCredential(parseCertificateCredential(child));
            } else if (name.equals(NODE_REALM)) {
                credential.setRealm(getPpsNodeValue(child));
            } else if (name.equals(NODE_CHECK_AAA_SERVER_CERT_STATUS)) {
                credential.setCheckAaaServerCertStatus(Boolean.parseBoolean(getPpsNodeValue(child)));
            } else if (name.equals(NODE_SIM)) {
                credential.setSimCredential(parseSimCredential(child));
            } else {
                throw new ParsingException("Unknown node under Credential: " + child.getName());
            }
        }
        return credential;
    }

    private static UserCredential parseUserCredential(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for UsernamePassword");
        }
        UserCredential userCred = new UserCredential();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_USERNAME)) {
                userCred.setUsername(getPpsNodeValue(child));
            } else if (name.equals(NODE_PASSWORD)) {
                userCred.setPassword(getPpsNodeValue(child));
            } else if (name.equals(NODE_MACHINE_MANAGED)) {
                userCred.setMachineManaged(Boolean.parseBoolean(getPpsNodeValue(child)));
            } else if (name.equals(NODE_SOFT_TOKEN_APP)) {
                userCred.setSoftTokenApp(getPpsNodeValue(child));
            } else if (name.equals(NODE_ABLE_TO_SHARE)) {
                userCred.setAbleToShare(Boolean.parseBoolean(getPpsNodeValue(child)));
            } else if (name.equals(NODE_EAP_METHOD)) {
                parseEAPMethod(child, userCred);
            } else {
                throw new ParsingException("Unknown node under UsernamPassword: " + child.getName());
            }
        }
        return userCred;
    }

    private static void parseEAPMethod(PPSNode node, UserCredential userCred) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for EAPMethod");
        }
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_EAP_TYPE)) {
                userCred.setEapType(parseInteger(getPpsNodeValue(child)));
            } else if (name.equals(NODE_INNER_METHOD)) {
                userCred.setNonEapInnerMethod(getPpsNodeValue(child));
            } else if (name.equals(NODE_VENDOR_ID) || name.equals(NODE_VENDOR_TYPE) || name.equals(NODE_INNER_EAP_TYPE) || name.equals(NODE_INNER_VENDOR_ID) || name.equals(NODE_INNER_VENDOR_TYPE)) {
                Log.d(TAG, "Ignore unsupported EAP method parameter: " + child.getName());
            } else {
                throw new ParsingException("Unknown node under EAPMethod: " + child.getName());
            }
        }
    }

    private static CertificateCredential parseCertificateCredential(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for DigitalCertificate");
        }
        CertificateCredential certCred = new CertificateCredential();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_CERTIFICATE_TYPE)) {
                certCred.setCertType(getPpsNodeValue(child));
            } else if (name.equals(NODE_CERT_SHA256_FINGERPRINT)) {
                certCred.setCertSha256Fingerprint(parseHexString(getPpsNodeValue(child)));
            } else {
                throw new ParsingException("Unknown node under DigitalCertificate: " + child.getName());
            }
        }
        return certCred;
    }

    private static SimCredential parseSimCredential(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for SIM");
        }
        SimCredential simCred = new SimCredential();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_SIM_IMSI)) {
                simCred.setImsi(getPpsNodeValue(child));
            } else if (name.equals(NODE_EAP_TYPE)) {
                simCred.setEapType(parseInteger(getPpsNodeValue(child)));
            } else {
                throw new ParsingException("Unknown node under SIM: " + child.getName());
            }
        }
        return simCred;
    }

    private static Policy parsePolicy(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for Policy");
        }
        Policy policy = new Policy();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_PREFERRED_ROAMING_PARTNER_LIST)) {
                policy.setPreferredRoamingPartnerList(parsePreferredRoamingPartnerList(child));
            } else if (name.equals(NODE_MIN_BACKHAUL_THRESHOLD)) {
                parseMinBackhaulThreshold(child, policy);
            } else if (name.equals(NODE_POLICY_UPDATE)) {
                policy.setPolicyUpdate(parseUpdateParameter(child));
            } else if (name.equals(NODE_SP_EXCLUSION_LIST)) {
                policy.setExcludedSsidList(parseSpExclusionList(child));
            } else if (name.equals(NODE_REQUIRED_PROTO_PORT_TUPLE)) {
                policy.setRequiredProtoPortMap(parseRequiredProtoPortTuple(child));
            } else if (name.equals(NODE_MAXIMUM_BSS_LOAD_VALUE)) {
                policy.setMaximumBssLoadValue(parseInteger(getPpsNodeValue(child)));
            } else {
                throw new ParsingException("Unknown node under Policy: " + child.getName());
            }
        }
        return policy;
    }

    private static List<RoamingPartner> parsePreferredRoamingPartnerList(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for PreferredRoamingPartnerList");
        }
        List<RoamingPartner> partnerList = new ArrayList();
        for (PPSNode child : node.getChildren()) {
            partnerList.add(parsePreferredRoamingPartner(child));
        }
        return partnerList;
    }

    private static RoamingPartner parsePreferredRoamingPartner(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for PreferredRoamingPartner instance");
        }
        RoamingPartner roamingPartner = new RoamingPartner();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_FQDN_MATCH)) {
                String fqdnMatch = getPpsNodeValue(child);
                String[] fqdnMatchArray = fqdnMatch.split(",");
                if (fqdnMatchArray.length != 2) {
                    throw new ParsingException("Invalid FQDN_Match: " + fqdnMatch);
                }
                roamingPartner.setFqdn(fqdnMatchArray[0]);
                if (TextUtils.equals(fqdnMatchArray[1], "exactMatch")) {
                    roamingPartner.setFqdnExactMatch(true);
                } else if (TextUtils.equals(fqdnMatchArray[1], "includeSubdomains")) {
                    roamingPartner.setFqdnExactMatch(false);
                } else {
                    throw new ParsingException("Invalid FQDN_Match: " + fqdnMatch);
                }
            } else if (name.equals(NODE_PRIORITY)) {
                roamingPartner.setPriority(parseInteger(getPpsNodeValue(child)));
            } else if (name.equals(NODE_COUNTRY)) {
                roamingPartner.setCountries(getPpsNodeValue(child));
            } else {
                throw new ParsingException("Unknown node under PreferredRoamingPartnerList instance " + child.getName());
            }
        }
        return roamingPartner;
    }

    private static void parseMinBackhaulThreshold(PPSNode node, Policy policy) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for MinBackhaulThreshold");
        }
        for (PPSNode child : node.getChildren()) {
            parseMinBackhaulThresholdInstance(child, policy);
        }
    }

    private static void parseMinBackhaulThresholdInstance(PPSNode node, Policy policy) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for MinBackhaulThreshold instance");
        }
        Object networkType = null;
        long downlinkBandwidth = Long.MIN_VALUE;
        long uplinkBandwidth = Long.MIN_VALUE;
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_NETWORK_TYPE)) {
                networkType = getPpsNodeValue(child);
            } else if (name.equals(NODE_DOWNLINK_BANDWIDTH)) {
                downlinkBandwidth = parseLong(getPpsNodeValue(child), 10);
            } else if (name.equals(NODE_UPLINK_BANDWIDTH)) {
                uplinkBandwidth = parseLong(getPpsNodeValue(child), 10);
            } else {
                throw new ParsingException("Unknown node under MinBackhaulThreshold instance " + child.getName());
            }
        }
        if (networkType == null) {
            throw new ParsingException("Missing NetworkType field");
        } else if (TextUtils.equals(networkType, "home")) {
            policy.setMinHomeDownlinkBandwidth(downlinkBandwidth);
            policy.setMinHomeUplinkBandwidth(uplinkBandwidth);
        } else if (TextUtils.equals(networkType, "roaming")) {
            policy.setMinRoamingDownlinkBandwidth(downlinkBandwidth);
            policy.setMinRoamingUplinkBandwidth(uplinkBandwidth);
        } else {
            throw new ParsingException("Invalid network type: " + networkType);
        }
    }

    private static UpdateParameter parseUpdateParameter(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for Update Parameters");
        }
        UpdateParameter updateParam = new UpdateParameter();
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_UPDATE_INTERVAL)) {
                updateParam.setUpdateIntervalInMinutes(parseLong(getPpsNodeValue(child), 10));
            } else if (name.equals(NODE_UPDATE_METHOD)) {
                updateParam.setUpdateMethod(getPpsNodeValue(child));
            } else if (name.equals(NODE_RESTRICTION)) {
                updateParam.setRestriction(getPpsNodeValue(child));
            } else if (name.equals(NODE_URI)) {
                updateParam.setServerUri(getPpsNodeValue(child));
            } else if (name.equals(NODE_USERNAME_PASSWORD)) {
                Pair<String, String> usernamePassword = parseUpdateUserCredential(child);
                updateParam.setUsername((String) usernamePassword.first);
                updateParam.setBase64EncodedPassword((String) usernamePassword.second);
            } else if (name.equals(NODE_TRUST_ROOT)) {
                Pair<String, byte[]> trustRoot = parseTrustRoot(child);
                updateParam.setTrustRootCertUrl((String) trustRoot.first);
                updateParam.setTrustRootCertSha256Fingerprint((byte[]) trustRoot.second);
            } else if (name.equals(NODE_OTHER)) {
                Log.d(TAG, "Ignore unsupported paramter: " + child.getName());
            } else {
                throw new ParsingException("Unknown node under Update Parameters: " + child.getName());
            }
        }
        return updateParam;
    }

    private static Pair<String, String> parseUpdateUserCredential(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for UsernamePassword");
        }
        Object username = null;
        Object password = null;
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_USERNAME)) {
                username = getPpsNodeValue(child);
            } else if (name.equals(NODE_PASSWORD)) {
                password = getPpsNodeValue(child);
            } else {
                throw new ParsingException("Unknown node under UsernamePassword: " + child.getName());
            }
        }
        return Pair.create(username, password);
    }

    private static Pair<String, byte[]> parseTrustRoot(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for TrustRoot");
        }
        Object certUrl = null;
        Object certFingerprint = null;
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_CERT_URL)) {
                certUrl = getPpsNodeValue(child);
            } else if (name.equals(NODE_CERT_SHA256_FINGERPRINT)) {
                certFingerprint = parseHexString(getPpsNodeValue(child));
            } else {
                throw new ParsingException("Unknown node under TrustRoot: " + child.getName());
            }
        }
        return Pair.create(certUrl, certFingerprint);
    }

    private static String[] parseSpExclusionList(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for SPExclusionList");
        }
        List<String> ssidList = new ArrayList();
        for (PPSNode child : node.getChildren()) {
            ssidList.add(parseSpExclusionInstance(child));
        }
        return (String[]) ssidList.toArray(new String[ssidList.size()]);
    }

    private static String parseSpExclusionInstance(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for SPExclusion instance");
        }
        String ssid = null;
        for (PPSNode child : node.getChildren()) {
            if (child.getName().equals(NODE_SSID)) {
                ssid = getPpsNodeValue(child);
            } else {
                throw new ParsingException("Unknown node under SPExclusion instance");
            }
        }
        return ssid;
    }

    private static Map<Integer, String> parseRequiredProtoPortTuple(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for RequiredProtoPortTuple");
        }
        Map<Integer, String> protoPortTupleMap = new HashMap();
        for (PPSNode child : node.getChildren()) {
            Pair<Integer, String> protoPortTuple = parseProtoPortTuple(child);
            protoPortTupleMap.put((Integer) protoPortTuple.first, (String) protoPortTuple.second);
        }
        return protoPortTupleMap;
    }

    private static Pair<Integer, String> parseProtoPortTuple(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for RequiredProtoPortTuple instance");
        }
        int proto = Integer.MIN_VALUE;
        Object ports = null;
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_IP_PROTOCOL)) {
                proto = parseInteger(getPpsNodeValue(child));
            } else if (name.equals(NODE_PORT_NUMBER)) {
                ports = getPpsNodeValue(child);
            } else {
                throw new ParsingException("Unknown node under RequiredProtoPortTuple instance" + child.getName());
            }
        }
        if (proto == Integer.MIN_VALUE) {
            throw new ParsingException("Missing IPProtocol field");
        } else if (ports != null) {
            return Pair.create(Integer.valueOf(proto), ports);
        } else {
            throw new ParsingException("Missing PortNumber field");
        }
    }

    private static Map<String, byte[]> parseAAAServerTrustRootList(PPSNode node) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for AAAServerTrustRoot");
        }
        Map<String, byte[]> certList = new HashMap();
        for (PPSNode child : node.getChildren()) {
            Pair<String, byte[]> certTuple = parseTrustRoot(child);
            certList.put((String) certTuple.first, (byte[]) certTuple.second);
        }
        return certList;
    }

    private static void parseSubscriptionParameter(PPSNode node, PasspointConfiguration config) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for SubscriptionParameter");
        }
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_CREATION_DATE)) {
                config.setSubscriptionCreationTimeInMillis(parseDate(getPpsNodeValue(child)));
            } else if (name.equals(NODE_EXPIRATION_DATE)) {
                config.setSubscriptionExpirationTimeInMillis(parseDate(getPpsNodeValue(child)));
            } else if (name.equals(NODE_TYPE_OF_SUBSCRIPTION)) {
                config.setSubscriptionType(getPpsNodeValue(child));
            } else if (name.equals(NODE_USAGE_LIMITS)) {
                parseUsageLimits(child, config);
            } else {
                throw new ParsingException("Unknown node under SubscriptionParameter" + child.getName());
            }
        }
    }

    private static void parseUsageLimits(PPSNode node, PasspointConfiguration config) throws ParsingException {
        if (node.isLeaf()) {
            throw new ParsingException("Leaf node not expected for UsageLimits");
        }
        for (PPSNode child : node.getChildren()) {
            String name = child.getName();
            if (name.equals(NODE_DATA_LIMIT)) {
                config.setUsageLimitDataLimit(parseLong(getPpsNodeValue(child), 10));
            } else if (name.equals(NODE_START_DATE)) {
                config.setUsageLimitStartTimeInMillis(parseDate(getPpsNodeValue(child)));
            } else if (name.equals(NODE_TIME_LIMIT)) {
                config.setUsageLimitTimeLimitInMinutes(parseLong(getPpsNodeValue(child), 10));
            } else if (name.equals(NODE_USAGE_TIME_PERIOD)) {
                config.setUsageLimitUsageTimePeriodInMinutes(parseLong(getPpsNodeValue(child), 10));
            } else {
                throw new ParsingException("Unknown node under UsageLimits" + child.getName());
            }
        }
    }

    private static byte[] parseHexString(String str) throws ParsingException {
        if ((str.length() & 1) == 1) {
            throw new ParsingException("Odd length hex string: " + str.length());
        }
        byte[] result = new byte[(str.length() / 2)];
        int i = 0;
        while (i < result.length) {
            int index = i * 2;
            try {
                result[i] = (byte) Integer.parseInt(str.substring(index, index + 2), 16);
                i++;
            } catch (NumberFormatException e) {
                throw new ParsingException("Invalid hex string: " + str);
            }
        }
        return result;
    }

    private static long parseDate(String dateStr) throws ParsingException {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateStr).getTime();
        } catch (ParseException e) {
            throw new ParsingException("Badly formatted time: " + dateStr);
        }
    }

    private static int parseInteger(String value) throws ParsingException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ParsingException("Invalid integer value: " + value);
        }
    }

    private static long parseLong(String value, int radix) throws ParsingException {
        try {
            return Long.parseLong(value, radix);
        } catch (NumberFormatException e) {
            throw new ParsingException("Invalid long integer value: " + value);
        }
    }

    private static long[] convertFromLongList(List<Long> list) {
        Long[] objectArray = (Long[]) list.toArray(new Long[list.size()]);
        long[] primitiveArray = new long[objectArray.length];
        for (int i = 0; i < objectArray.length; i++) {
            primitiveArray[i] = objectArray[i].longValue();
        }
        return primitiveArray;
    }
}
