package android.net.wifi.hotspot2.omadm;

import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.net.wifi.hotspot2.pps.Policy;
import android.net.wifi.hotspot2.pps.UpdateParameter;
import android.provider.CalendarContract;
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
import org.xml.sax.SAXException;

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

    private static class ParsingException extends Exception {
        public ParsingException(String message) {
            super(message);
        }
    }

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
                char c = 65535;
                int hashCode = tag.hashCode();
                if (hashCode != -1736120495) {
                    if (hashCode == 2433570 && tag.equals(TAG_NODE)) {
                        c = 1;
                    }
                } else if (tag.equals(TAG_VER_DTD)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        if (verDtd == null) {
                            verDtd = child.getText();
                            break;
                        } else {
                            Log.e(TAG, "Duplicate VerDTD element");
                            return null;
                        }
                    case 1:
                        if (config != null) {
                            Log.e(TAG, "Unexpected multiple Node element under MgmtTree");
                            return null;
                        }
                        try {
                            config = parsePpsNode(child);
                            break;
                        } catch (ParsingException e) {
                            Log.e(TAG, e.getMessage());
                            return null;
                        }
                    default:
                        Log.e(TAG, "Unknown node: " + child.getTag());
                        return null;
                }
            }
            return config;
        } catch (IOException | SAXException e2) {
            return null;
        }
    }

    private static PasspointConfiguration parsePpsNode(XMLNode node) throws ParsingException {
        PasspointConfiguration config = null;
        String nodeName = null;
        int updateIdentifier = Integer.MIN_VALUE;
        for (XMLNode child : node.getChildren()) {
            String tag = child.getTag();
            char c = 65535;
            int hashCode = tag.hashCode();
            if (hashCode != -1852765931) {
                if (hashCode != 2433570) {
                    if (hashCode == 1187524557 && tag.equals(TAG_NODE_NAME)) {
                        c = 0;
                    }
                } else if (tag.equals(TAG_NODE)) {
                    c = 1;
                }
            } else if (tag.equals(TAG_RT_PROPERTIES)) {
                c = 2;
            }
            switch (c) {
                case 0:
                    if (nodeName == null) {
                        nodeName = child.getText();
                        if (TextUtils.equals(nodeName, NODE_PER_PROVIDER_SUBSCRIPTION)) {
                            break;
                        } else {
                            throw new ParsingException("Unexpected NodeName: " + nodeName);
                        }
                    } else {
                        throw new ParsingException("Duplicate NodeName: " + child.getText());
                    }
                case 1:
                    PPSNode ppsNodeRoot = buildPpsNode(child);
                    if (TextUtils.equals(ppsNodeRoot.getName(), NODE_UPDATE_IDENTIFIER)) {
                        if (updateIdentifier == Integer.MIN_VALUE) {
                            updateIdentifier = parseInteger(getPpsNodeValue(ppsNodeRoot));
                            break;
                        } else {
                            throw new ParsingException("Multiple node for UpdateIdentifier");
                        }
                    } else if (config == null) {
                        config = parsePpsInstance(ppsNodeRoot);
                        break;
                    } else {
                        throw new ParsingException("Multiple PPS instance");
                    }
                case 2:
                    String urn = parseUrn(child);
                    if (TextUtils.equals(urn, PPS_MO_URN)) {
                        break;
                    } else {
                        throw new ParsingException("Unknown URN: " + urn);
                    }
                default:
                    throw new ParsingException("Unknown tag under PPS node: " + child.getTag());
            }
        }
        if (!(config == null || updateIdentifier == Integer.MIN_VALUE)) {
            config.setUpdateIdentifier(updateIdentifier);
        }
        return config;
    }

    private static String parseUrn(XMLNode node) throws ParsingException {
        if (node.getChildren().size() == 1) {
            XMLNode typeNode = node.getChildren().get(0);
            if (typeNode.getChildren().size() != 1) {
                throw new ParsingException("Expect Type node to only have one child");
            } else if (TextUtils.equals(typeNode.getTag(), TAG_TYPE)) {
                XMLNode ddfNameNode = typeNode.getChildren().get(0);
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
        } else {
            throw new ParsingException("Expect RTPProperties node to only have one child");
        }
    }

    private static PPSNode buildPpsNode(XMLNode node) throws ParsingException {
        String nodeName = null;
        String nodeValue = null;
        List<PPSNode> childNodes = new ArrayList<>();
        Set<String> parsedNodes = new HashSet<>();
        for (XMLNode child : node.getChildren()) {
            String tag = child.getTag();
            if (TextUtils.equals(tag, TAG_NODE_NAME)) {
                if (nodeName == null) {
                    nodeName = child.getText();
                } else {
                    throw new ParsingException("Duplicate NodeName node");
                }
            } else if (TextUtils.equals(tag, TAG_NODE)) {
                PPSNode ppsNode = buildPpsNode(child);
                if (!parsedNodes.contains(ppsNode.getName())) {
                    parsedNodes.add(ppsNode.getName());
                    childNodes.add(ppsNode);
                } else {
                    throw new ParsingException("Duplicate node: " + ppsNode.getName());
                }
            } else if (!TextUtils.equals(tag, TAG_VALUE)) {
                throw new ParsingException("Unknown tag: " + tag);
            } else if (nodeValue == null) {
                nodeValue = child.getText();
            } else {
                throw new ParsingException("Duplicate Value node");
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
        if (!root.isLeaf()) {
            PasspointConfiguration config = new PasspointConfiguration();
            for (PPSNode child : root.getChildren()) {
                String name = child.getName();
                char c = 65535;
                switch (name.hashCode()) {
                    case -2127810660:
                        if (name.equals("HomeSP")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1898802862:
                        if (name.equals(NODE_POLICY)) {
                            c = 2;
                            break;
                        }
                        break;
                    case -102647060:
                        if (name.equals(NODE_SUBSCRIPTION_PARAMETER)) {
                            c = 5;
                            break;
                        }
                        break;
                    case 162345062:
                        if (name.equals(NODE_SUBSCRIPTION_UPDATE)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 314411254:
                        if (name.equals(NODE_AAA_SERVER_TRUST_ROOT)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1310049399:
                        if (name.equals(NODE_CREDENTIAL)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1391410207:
                        if (name.equals(NODE_EXTENSION)) {
                            c = 7;
                            break;
                        }
                        break;
                    case 2017737531:
                        if (name.equals(NODE_CREDENTIAL_PRIORITY)) {
                            c = 6;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        config.setHomeSp(parseHomeSP(child));
                        break;
                    case 1:
                        config.setCredential(parseCredential(child));
                        break;
                    case 2:
                        config.setPolicy(parsePolicy(child));
                        break;
                    case 3:
                        config.setTrustRootCertList(parseAAAServerTrustRootList(child));
                        break;
                    case 4:
                        config.setSubscriptionUpdate(parseUpdateParameter(child));
                        break;
                    case 5:
                        parseSubscriptionParameter(child, config);
                        break;
                    case 6:
                        config.setCredentialPriority(parseInteger(getPpsNodeValue(child)));
                        break;
                    case 7:
                        Log.d(TAG, "Ignore Extension node for vendor specific information");
                        break;
                    default:
                        throw new ParsingException("Unknown node: " + child.getName());
                }
            }
            return config;
        }
        throw new ParsingException("Leaf node not expected for PPS instance");
    }

    private static HomeSp parseHomeSP(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            HomeSp homeSp = new HomeSp();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                switch (name.hashCode()) {
                    case -1560207529:
                        if (name.equals(NODE_HOME_OI_LIST)) {
                            c = 5;
                            break;
                        }
                        break;
                    case -991549930:
                        if (name.equals(NODE_ICON_URL)) {
                            c = 3;
                            break;
                        }
                        break;
                    case -228216919:
                        if (name.equals(NODE_NETWORK_ID)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 2165397:
                        if (name.equals(NODE_FQDN)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 542998228:
                        if (name.equals(NODE_ROAMING_CONSORTIUM_OI)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 626253302:
                        if (name.equals(NODE_FRIENDLY_NAME)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1956561338:
                        if (name.equals(NODE_OTHER_HOME_PARTNERS)) {
                            c = 6;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        homeSp.setFqdn(getPpsNodeValue(child));
                        break;
                    case 1:
                        homeSp.setFriendlyName(getPpsNodeValue(child));
                        break;
                    case 2:
                        homeSp.setRoamingConsortiumOis(parseRoamingConsortiumOI(getPpsNodeValue(child)));
                        break;
                    case 3:
                        homeSp.setIconUrl(getPpsNodeValue(child));
                        break;
                    case 4:
                        homeSp.setHomeNetworkIds(parseNetworkIds(child));
                        break;
                    case 5:
                        Pair<List<Long>, List<Long>> homeOIs = parseHomeOIList(child);
                        homeSp.setMatchAllOis(convertFromLongList((List) homeOIs.first));
                        homeSp.setMatchAnyOis(convertFromLongList((List) homeOIs.second));
                        break;
                    case 6:
                        homeSp.setOtherHomePartners(parseOtherHomePartners(child));
                        break;
                    default:
                        throw new ParsingException("Unknown node under HomeSP: " + child.getName());
                }
            }
            return homeSp;
        }
        throw new ParsingException("Leaf node not expected for HomeSP");
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
        if (!node.isLeaf()) {
            Map<String, Long> networkIds = new HashMap<>();
            for (PPSNode child : node.getChildren()) {
                Pair<String, Long> networkId = parseNetworkIdInstance(child);
                networkIds.put((String) networkId.first, (Long) networkId.second);
            }
            return networkIds;
        }
        throw new ParsingException("Leaf node not expected for NetworkID");
    }

    private static Pair<String, Long> parseNetworkIdInstance(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            String ssid = null;
            Long hessid = null;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != 2554747) {
                    if (hashCode == 2127576568 && name.equals(NODE_HESSID)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_SSID)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        ssid = getPpsNodeValue(child);
                        break;
                    case 1:
                        hessid = Long.valueOf(parseLong(getPpsNodeValue(child), 16));
                        break;
                    default:
                        throw new ParsingException("Unknown node under NetworkID instance: " + child.getName());
                }
            }
            if (ssid != null) {
                return new Pair<>(ssid, hessid);
            }
            throw new ParsingException("NetworkID instance missing SSID");
        }
        throw new ParsingException("Leaf node not expected for NetworkID instance");
    }

    private static Pair<List<Long>, List<Long>> parseHomeOIList(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            List<Long> matchAllOIs = new ArrayList<>();
            List<Long> matchAnyOIs = new ArrayList<>();
            for (PPSNode child : node.getChildren()) {
                Pair<Long, Boolean> homeOI = parseHomeOIInstance(child);
                if (((Boolean) homeOI.second).booleanValue()) {
                    matchAllOIs.add((Long) homeOI.first);
                } else {
                    matchAnyOIs.add((Long) homeOI.first);
                }
            }
            return new Pair<>(matchAllOIs, matchAnyOIs);
        }
        throw new ParsingException("Leaf node not expected for HomeOIList");
    }

    private static Pair<Long, Boolean> parseHomeOIInstance(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Long oi = null;
            Boolean required = null;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -2127810791) {
                    if (hashCode == -1935174184 && name.equals(NODE_HOME_OI_REQUIRED)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_HOME_OI)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        try {
                            oi = Long.valueOf(getPpsNodeValue(child), 16);
                            break;
                        } catch (NumberFormatException e) {
                            throw new ParsingException("Invalid HomeOI: " + getPpsNodeValue(child));
                        }
                    case 1:
                        required = Boolean.valueOf(getPpsNodeValue(child));
                        break;
                    default:
                        throw new ParsingException("Unknown node under NetworkID instance: " + child.getName());
                }
            }
            if (oi == null) {
                throw new ParsingException("HomeOI instance missing OI field");
            } else if (required != null) {
                return new Pair<>(oi, required);
            } else {
                throw new ParsingException("HomeOI instance missing required field");
            }
        } else {
            throw new ParsingException("Leaf node not expected for HomeOI instance");
        }
    }

    private static String[] parseOtherHomePartners(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            List<String> otherHomePartners = new ArrayList<>();
            for (PPSNode child : node.getChildren()) {
                otherHomePartners.add(parseOtherHomePartnerInstance(child));
            }
            return (String[]) otherHomePartners.toArray(new String[otherHomePartners.size()]);
        }
        throw new ParsingException("Leaf node not expected for OtherHomePartners");
    }

    private static String parseOtherHomePartnerInstance(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            String fqdn = null;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                if (name.hashCode() == 2165397 && name.equals(NODE_FQDN)) {
                    c = 0;
                }
                if (c == 0) {
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
        throw new ParsingException("Leaf node not expected for OtherHomePartner instance");
    }

    private static Credential parseCredential(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Credential credential = new Credential();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                switch (name.hashCode()) {
                    case -1670804707:
                        if (name.equals(NODE_EXPIRATION_DATE)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1208321921:
                        if (name.equals(NODE_DIGITAL_CERTIFICATE)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 82103:
                        if (name.equals(NODE_SIM)) {
                            c = 6;
                            break;
                        }
                        break;
                    case 78834287:
                        if (name.equals(NODE_REALM)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 494843313:
                        if (name.equals(NODE_USERNAME_PASSWORD)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 646045490:
                        if (name.equals(NODE_CHECK_AAA_SERVER_CERT_STATUS)) {
                            c = 5;
                            break;
                        }
                        break;
                    case 1749851981:
                        if (name.equals(NODE_CREATION_DATE)) {
                            c = 0;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        credential.setCreationTimeInMillis(parseDate(getPpsNodeValue(child)));
                        break;
                    case 1:
                        credential.setExpirationTimeInMillis(parseDate(getPpsNodeValue(child)));
                        break;
                    case 2:
                        credential.setUserCredential(parseUserCredential(child));
                        break;
                    case 3:
                        credential.setCertCredential(parseCertificateCredential(child));
                        break;
                    case 4:
                        credential.setRealm(getPpsNodeValue(child));
                        break;
                    case 5:
                        credential.setCheckAaaServerCertStatus(Boolean.parseBoolean(getPpsNodeValue(child)));
                        break;
                    case 6:
                        credential.setSimCredential(parseSimCredential(child));
                        break;
                    default:
                        throw new ParsingException("Unknown node under Credential: " + child.getName());
                }
            }
            return credential;
        }
        throw new ParsingException("Leaf node not expected for HomeSP");
    }

    private static Credential.UserCredential parseUserCredential(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Credential.UserCredential userCred = new Credential.UserCredential();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                switch (name.hashCode()) {
                    case -201069322:
                        if (name.equals(NODE_USERNAME)) {
                            c = 0;
                            break;
                        }
                        break;
                    case -123996342:
                        if (name.equals(NODE_ABLE_TO_SHARE)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1045832056:
                        if (name.equals(NODE_MACHINE_MANAGED)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1281629883:
                        if (name.equals(NODE_PASSWORD)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1410776018:
                        if (name.equals(NODE_SOFT_TOKEN_APP)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1740345653:
                        if (name.equals(NODE_EAP_METHOD)) {
                            c = 5;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        userCred.setUsername(getPpsNodeValue(child));
                        break;
                    case 1:
                        userCred.setPassword(getPpsNodeValue(child));
                        break;
                    case 2:
                        userCred.setMachineManaged(Boolean.parseBoolean(getPpsNodeValue(child)));
                        break;
                    case 3:
                        userCred.setSoftTokenApp(getPpsNodeValue(child));
                        break;
                    case 4:
                        userCred.setAbleToShare(Boolean.parseBoolean(getPpsNodeValue(child)));
                        break;
                    case 5:
                        parseEAPMethod(child, userCred);
                        break;
                    default:
                        throw new ParsingException("Unknown node under UsernamPassword: " + child.getName());
                }
            }
            return userCred;
        }
        throw new ParsingException("Leaf node not expected for UsernamePassword");
    }

    private static void parseEAPMethod(PPSNode node, Credential.UserCredential userCred) throws ParsingException {
        if (!node.isLeaf()) {
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                switch (name.hashCode()) {
                    case -2048597853:
                        if (name.equals(NODE_VENDOR_ID)) {
                            c = 2;
                            break;
                        }
                        break;
                    case -1706447464:
                        if (name.equals(NODE_INNER_EAP_TYPE)) {
                            c = 4;
                            break;
                        }
                        break;
                    case -1607163710:
                        if (name.equals(NODE_VENDOR_TYPE)) {
                            c = 3;
                            break;
                        }
                        break;
                    case -1249356658:
                        if (name.equals(NODE_EAP_TYPE)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 541930360:
                        if (name.equals(NODE_INNER_VENDOR_TYPE)) {
                            c = 6;
                            break;
                        }
                        break;
                    case 901061303:
                        if (name.equals(NODE_INNER_METHOD)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 961456313:
                        if (name.equals(NODE_INNER_VENDOR_ID)) {
                            c = 5;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        userCred.setEapType(parseInteger(getPpsNodeValue(child)));
                        break;
                    case 1:
                        userCred.setNonEapInnerMethod(getPpsNodeValue(child));
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        Log.d(TAG, "Ignore unsupported EAP method parameter: " + child.getName());
                        break;
                    default:
                        throw new ParsingException("Unknown node under EAPMethod: " + child.getName());
                }
            }
            return;
        }
        throw new ParsingException("Leaf node not expected for EAPMethod");
    }

    private static Credential.CertificateCredential parseCertificateCredential(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Credential.CertificateCredential certCred = new Credential.CertificateCredential();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1914611375) {
                    if (hashCode == -285451687 && name.equals(NODE_CERT_SHA256_FINGERPRINT)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_CERTIFICATE_TYPE)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        certCred.setCertType(getPpsNodeValue(child));
                        break;
                    case 1:
                        certCred.setCertSha256Fingerprint(parseHexString(getPpsNodeValue(child)));
                        break;
                    default:
                        throw new ParsingException("Unknown node under DigitalCertificate: " + child.getName());
                }
            }
            return certCred;
        }
        throw new ParsingException("Leaf node not expected for DigitalCertificate");
    }

    private static Credential.SimCredential parseSimCredential(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Credential.SimCredential simCred = new Credential.SimCredential();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1249356658) {
                    if (hashCode == 2251386 && name.equals(NODE_SIM_IMSI)) {
                        c = 0;
                    }
                } else if (name.equals(NODE_EAP_TYPE)) {
                    c = 1;
                }
                switch (c) {
                    case 0:
                        simCred.setImsi(getPpsNodeValue(child));
                        break;
                    case 1:
                        simCred.setEapType(parseInteger(getPpsNodeValue(child)));
                        break;
                    default:
                        throw new ParsingException("Unknown node under SIM: " + child.getName());
                }
            }
            return simCred;
        }
        throw new ParsingException("Leaf node not expected for SIM");
    }

    private static Policy parsePolicy(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Policy policy = new Policy();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                switch (name.hashCode()) {
                    case -1710886725:
                        if (name.equals(NODE_POLICY_UPDATE)) {
                            c = 2;
                            break;
                        }
                        break;
                    case -281271454:
                        if (name.equals(NODE_MIN_BACKHAUL_THRESHOLD)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -166875607:
                        if (name.equals(NODE_MAXIMUM_BSS_LOAD_VALUE)) {
                            c = 5;
                            break;
                        }
                        break;
                    case 586018863:
                        if (name.equals(NODE_SP_EXCLUSION_LIST)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 783647838:
                        if (name.equals(NODE_REQUIRED_PROTO_PORT_TUPLE)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1337803246:
                        if (name.equals(NODE_PREFERRED_ROAMING_PARTNER_LIST)) {
                            c = 0;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        policy.setPreferredRoamingPartnerList(parsePreferredRoamingPartnerList(child));
                        break;
                    case 1:
                        parseMinBackhaulThreshold(child, policy);
                        break;
                    case 2:
                        policy.setPolicyUpdate(parseUpdateParameter(child));
                        break;
                    case 3:
                        policy.setExcludedSsidList(parseSpExclusionList(child));
                        break;
                    case 4:
                        policy.setRequiredProtoPortMap(parseRequiredProtoPortTuple(child));
                        break;
                    case 5:
                        policy.setMaximumBssLoadValue(parseInteger(getPpsNodeValue(child)));
                        break;
                    default:
                        throw new ParsingException("Unknown node under Policy: " + child.getName());
                }
            }
            return policy;
        }
        throw new ParsingException("Leaf node not expected for Policy");
    }

    private static List<Policy.RoamingPartner> parsePreferredRoamingPartnerList(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            List<Policy.RoamingPartner> partnerList = new ArrayList<>();
            for (PPSNode child : node.getChildren()) {
                partnerList.add(parsePreferredRoamingPartner(child));
            }
            return partnerList;
        }
        throw new ParsingException("Leaf node not expected for PreferredRoamingPartnerList");
    }

    private static Policy.RoamingPartner parsePreferredRoamingPartner(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Policy.RoamingPartner roamingPartner = new Policy.RoamingPartner();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1672482954) {
                    if (hashCode != -1100816956) {
                        if (hashCode == 305746811 && name.equals(NODE_FQDN_MATCH)) {
                            c = 0;
                        }
                    } else if (name.equals(NODE_PRIORITY)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_COUNTRY)) {
                    c = 2;
                }
                switch (c) {
                    case 0:
                        String[] fqdnMatchArray = getPpsNodeValue(child).split(",");
                        if (fqdnMatchArray.length == 2) {
                            roamingPartner.setFqdn(fqdnMatchArray[0]);
                            if (TextUtils.equals(fqdnMatchArray[1], "exactMatch")) {
                                roamingPartner.setFqdnExactMatch(true);
                                break;
                            } else if (TextUtils.equals(fqdnMatchArray[1], "includeSubdomains")) {
                                roamingPartner.setFqdnExactMatch(false);
                                break;
                            } else {
                                throw new ParsingException("Invalid FQDN_Match: " + fqdnMatch);
                            }
                        } else {
                            throw new ParsingException("Invalid FQDN_Match: " + fqdnMatch);
                        }
                    case 1:
                        roamingPartner.setPriority(parseInteger(getPpsNodeValue(child)));
                        break;
                    case 2:
                        roamingPartner.setCountries(getPpsNodeValue(child));
                        break;
                    default:
                        throw new ParsingException("Unknown node under PreferredRoamingPartnerList instance " + child.getName());
                }
            }
            return roamingPartner;
        }
        throw new ParsingException("Leaf node not expected for PreferredRoamingPartner instance");
    }

    private static void parseMinBackhaulThreshold(PPSNode node, Policy policy) throws ParsingException {
        if (!node.isLeaf()) {
            for (PPSNode child : node.getChildren()) {
                parseMinBackhaulThresholdInstance(child, policy);
            }
            return;
        }
        throw new ParsingException("Leaf node not expected for MinBackhaulThreshold");
    }

    private static void parseMinBackhaulThresholdInstance(PPSNode node, Policy policy) throws ParsingException {
        if (!node.isLeaf()) {
            String networkType = null;
            long downlinkBandwidth = Long.MIN_VALUE;
            long uplinkBandwidth = Long.MIN_VALUE;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -272744856) {
                    if (hashCode != -133967910) {
                        if (hashCode == 349434121 && name.equals(NODE_DOWNLINK_BANDWIDTH)) {
                            c = 1;
                        }
                    } else if (name.equals(NODE_UPLINK_BANDWIDTH)) {
                        c = 2;
                    }
                } else if (name.equals(NODE_NETWORK_TYPE)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        networkType = getPpsNodeValue(child);
                        break;
                    case 1:
                        downlinkBandwidth = parseLong(getPpsNodeValue(child), 10);
                        break;
                    case 2:
                        uplinkBandwidth = parseLong(getPpsNodeValue(child), 10);
                        break;
                    default:
                        throw new ParsingException("Unknown node under MinBackhaulThreshold instance " + child.getName());
                }
            }
            if (networkType == null) {
                throw new ParsingException("Missing NetworkType field");
            } else if (TextUtils.equals(networkType, CalendarContract.CalendarCache.TIMEZONE_TYPE_HOME)) {
                policy.setMinHomeDownlinkBandwidth(downlinkBandwidth);
                policy.setMinHomeUplinkBandwidth(uplinkBandwidth);
            } else if (TextUtils.equals(networkType, "roaming")) {
                policy.setMinRoamingDownlinkBandwidth(downlinkBandwidth);
                policy.setMinRoamingUplinkBandwidth(uplinkBandwidth);
            } else {
                throw new ParsingException("Invalid network type: " + networkType);
            }
        } else {
            throw new ParsingException("Leaf node not expected for MinBackhaulThreshold instance");
        }
    }

    private static UpdateParameter parseUpdateParameter(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            UpdateParameter updateParam = new UpdateParameter();
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                switch (name.hashCode()) {
                    case -961491158:
                        if (name.equals(NODE_UPDATE_METHOD)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -524654790:
                        if (name.equals(NODE_TRUST_ROOT)) {
                            c = 5;
                            break;
                        }
                        break;
                    case 84300:
                        if (name.equals(NODE_URI)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 76517104:
                        if (name.equals(NODE_OTHER)) {
                            c = 6;
                            break;
                        }
                        break;
                    case 106806188:
                        if (name.equals(NODE_RESTRICTION)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 438596814:
                        if (name.equals(NODE_UPDATE_INTERVAL)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 494843313:
                        if (name.equals(NODE_USERNAME_PASSWORD)) {
                            c = 4;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        updateParam.setUpdateIntervalInMinutes(parseLong(getPpsNodeValue(child), 10));
                        break;
                    case 1:
                        updateParam.setUpdateMethod(getPpsNodeValue(child));
                        break;
                    case 2:
                        updateParam.setRestriction(getPpsNodeValue(child));
                        break;
                    case 3:
                        updateParam.setServerUri(getPpsNodeValue(child));
                        break;
                    case 4:
                        Pair<String, String> usernamePassword = parseUpdateUserCredential(child);
                        updateParam.setUsername((String) usernamePassword.first);
                        updateParam.setBase64EncodedPassword((String) usernamePassword.second);
                        break;
                    case 5:
                        Pair<String, byte[]> trustRoot = parseTrustRoot(child);
                        updateParam.setTrustRootCertUrl((String) trustRoot.first);
                        updateParam.setTrustRootCertSha256Fingerprint((byte[]) trustRoot.second);
                        break;
                    case 6:
                        Log.d(TAG, "Ignore unsupported paramter: " + child.getName());
                        break;
                    default:
                        throw new ParsingException("Unknown node under Update Parameters: " + child.getName());
                }
            }
            return updateParam;
        }
        throw new ParsingException("Leaf node not expected for Update Parameters");
    }

    private static Pair<String, String> parseUpdateUserCredential(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            String username = null;
            String password = null;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -201069322) {
                    if (hashCode == 1281629883 && name.equals(NODE_PASSWORD)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_USERNAME)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        username = getPpsNodeValue(child);
                        break;
                    case 1:
                        password = getPpsNodeValue(child);
                        break;
                    default:
                        throw new ParsingException("Unknown node under UsernamePassword: " + child.getName());
                }
            }
            return Pair.create(username, password);
        }
        throw new ParsingException("Leaf node not expected for UsernamePassword");
    }

    private static Pair<String, byte[]> parseTrustRoot(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            String certUrl = null;
            byte[] certFingerprint = null;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1961397109) {
                    if (hashCode == -285451687 && name.equals(NODE_CERT_SHA256_FINGERPRINT)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_CERT_URL)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        certUrl = getPpsNodeValue(child);
                        break;
                    case 1:
                        certFingerprint = parseHexString(getPpsNodeValue(child));
                        break;
                    default:
                        throw new ParsingException("Unknown node under TrustRoot: " + child.getName());
                }
            }
            return Pair.create(certUrl, certFingerprint);
        }
        throw new ParsingException("Leaf node not expected for TrustRoot");
    }

    private static String[] parseSpExclusionList(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            List<String> ssidList = new ArrayList<>();
            for (PPSNode child : node.getChildren()) {
                ssidList.add(parseSpExclusionInstance(child));
            }
            return (String[]) ssidList.toArray(new String[ssidList.size()]);
        }
        throw new ParsingException("Leaf node not expected for SPExclusionList");
    }

    private static String parseSpExclusionInstance(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            String ssid = null;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                if (name.hashCode() == 2554747 && name.equals(NODE_SSID)) {
                    c = 0;
                }
                if (c == 0) {
                    ssid = getPpsNodeValue(child);
                } else {
                    throw new ParsingException("Unknown node under SPExclusion instance");
                }
            }
            return ssid;
        }
        throw new ParsingException("Leaf node not expected for SPExclusion instance");
    }

    private static Map<Integer, String> parseRequiredProtoPortTuple(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Map<Integer, String> protoPortTupleMap = new HashMap<>();
            for (PPSNode child : node.getChildren()) {
                Pair<Integer, String> protoPortTuple = parseProtoPortTuple(child);
                protoPortTupleMap.put((Integer) protoPortTuple.first, (String) protoPortTuple.second);
            }
            return protoPortTupleMap;
        }
        throw new ParsingException("Leaf node not expected for RequiredProtoPortTuple");
    }

    private static Pair<Integer, String> parseProtoPortTuple(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            int proto = Integer.MIN_VALUE;
            String ports = null;
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -952572705) {
                    if (hashCode == 1727403850 && name.equals(NODE_PORT_NUMBER)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_IP_PROTOCOL)) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        proto = parseInteger(getPpsNodeValue(child));
                        break;
                    case 1:
                        ports = getPpsNodeValue(child);
                        break;
                    default:
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
        } else {
            throw new ParsingException("Leaf node not expected for RequiredProtoPortTuple instance");
        }
    }

    private static Map<String, byte[]> parseAAAServerTrustRootList(PPSNode node) throws ParsingException {
        if (!node.isLeaf()) {
            Map<String, byte[]> certList = new HashMap<>();
            for (PPSNode child : node.getChildren()) {
                Pair<String, byte[]> certTuple = parseTrustRoot(child);
                certList.put((String) certTuple.first, (byte[]) certTuple.second);
            }
            return certList;
        }
        throw new ParsingException("Leaf node not expected for AAAServerTrustRoot");
    }

    private static void parseSubscriptionParameter(PPSNode node, PasspointConfiguration config) throws ParsingException {
        if (!node.isLeaf()) {
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1930116871) {
                    if (hashCode != -1670804707) {
                        if (hashCode != -1655596402) {
                            if (hashCode == 1749851981 && name.equals(NODE_CREATION_DATE)) {
                                c = 0;
                            }
                        } else if (name.equals(NODE_TYPE_OF_SUBSCRIPTION)) {
                            c = 2;
                        }
                    } else if (name.equals(NODE_EXPIRATION_DATE)) {
                        c = 1;
                    }
                } else if (name.equals(NODE_USAGE_LIMITS)) {
                    c = 3;
                }
                switch (c) {
                    case 0:
                        config.setSubscriptionCreationTimeInMillis(parseDate(getPpsNodeValue(child)));
                        break;
                    case 1:
                        config.setSubscriptionExpirationTimeInMillis(parseDate(getPpsNodeValue(child)));
                        break;
                    case 2:
                        config.setSubscriptionType(getPpsNodeValue(child));
                        break;
                    case 3:
                        parseUsageLimits(child, config);
                        break;
                    default:
                        throw new ParsingException("Unknown node under SubscriptionParameter" + child.getName());
                }
            }
            return;
        }
        throw new ParsingException("Leaf node not expected for SubscriptionParameter");
    }

    private static void parseUsageLimits(PPSNode node, PasspointConfiguration config) throws ParsingException {
        if (!node.isLeaf()) {
            for (PPSNode child : node.getChildren()) {
                String name = child.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -125810928) {
                    if (hashCode != 587064143) {
                        if (hashCode != 1622722065) {
                            if (hashCode == 2022760654 && name.equals(NODE_TIME_LIMIT)) {
                                c = 2;
                            }
                        } else if (name.equals(NODE_DATA_LIMIT)) {
                            c = 0;
                        }
                    } else if (name.equals(NODE_USAGE_TIME_PERIOD)) {
                        c = 3;
                    }
                } else if (name.equals(NODE_START_DATE)) {
                    c = 1;
                }
                switch (c) {
                    case 0:
                        config.setUsageLimitDataLimit(parseLong(getPpsNodeValue(child), 10));
                        break;
                    case 1:
                        config.setUsageLimitStartTimeInMillis(parseDate(getPpsNodeValue(child)));
                        break;
                    case 2:
                        config.setUsageLimitTimeLimitInMinutes(parseLong(getPpsNodeValue(child), 10));
                        break;
                    case 3:
                        config.setUsageLimitUsageTimePeriodInMinutes(parseLong(getPpsNodeValue(child), 10));
                        break;
                    default:
                        throw new ParsingException("Unknown node under UsageLimits" + child.getName());
                }
            }
            return;
        }
        throw new ParsingException("Leaf node not expected for UsageLimits");
    }

    private static byte[] parseHexString(String str) throws ParsingException {
        if ((str.length() & 1) != 1) {
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
        throw new ParsingException("Odd length hex string: " + str.length());
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
