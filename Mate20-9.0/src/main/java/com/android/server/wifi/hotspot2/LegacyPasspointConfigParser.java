package com.android.server.wifi.hotspot2;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LegacyPasspointConfigParser {
    private static final String END_OF_INTERNAL_NODE_INDICATOR = ".";
    private static final String LONG_ARRAY_SEPARATOR = ",";
    private static final char START_OF_INTERNAL_NODE_INDICATOR = '+';
    private static final char STRING_PREFIX_INDICATOR = ':';
    private static final char STRING_VALUE_INDICATOR = '=';
    private static final String TAG = "LegacyPasspointConfigParser";
    private static final String TAG_CREDENTIAL = "Credential";
    private static final String TAG_FQDN = "FQDN";
    private static final String TAG_FRIENDLY_NAME = "FriendlyName";
    private static final String TAG_HOMESP = "HomeSP";
    private static final String TAG_IMSI = "IMSI";
    private static final String TAG_MANAGEMENT_TREE = "MgmtTree";
    private static final String TAG_PER_PROVIDER_SUBSCRIPTION = "PerProviderSubscription";
    private static final String TAG_REALM = "Realm";
    private static final String TAG_ROAMING_CONSORTIUM_OI = "RoamingConsortiumOI";
    private static final String TAG_SIM = "SIM";

    private static class InternalNode extends Node {
        private final List<Node> mChildren;

        InternalNode(String name, List<Node> children) {
            super(name);
            this.mChildren = children;
        }

        public List<Node> getChildren() {
            return this.mChildren;
        }

        public String getValue() {
            return null;
        }
    }

    private static class LeafNode extends Node {
        private final String mValue;

        LeafNode(String name, String value) {
            super(name);
            this.mValue = value;
        }

        public List<Node> getChildren() {
            return null;
        }

        public String getValue() {
            return this.mValue;
        }
    }

    private static abstract class Node {
        private final String mName;

        public abstract List<Node> getChildren();

        public abstract String getValue();

        Node(String name) {
            this.mName = name;
        }

        public String getName() {
            return this.mName;
        }
    }

    public Map<String, LegacyPasspointConfig> parseConfig(String fileName) throws IOException {
        Map<String, LegacyPasspointConfig> configs = new HashMap<>();
        BufferedReader in = null;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(fileName));
            in2.readLine();
            Node root = buildNode(in2);
            try {
                in2.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException occurs when close in stream");
            }
            if (root == null || root.getChildren() == null) {
                Log.d(TAG, "Empty configuration data");
                return configs;
            } else if (TextUtils.equals(TAG_MANAGEMENT_TREE, root.getName())) {
                for (Node ppsNode : root.getChildren()) {
                    LegacyPasspointConfig config = processPpsNode(ppsNode);
                    configs.put(config.mFqdn, config);
                }
                return configs;
            } else {
                throw new IOException("Unexpected root node: " + root.getName());
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                    Log.e(TAG, "IOException occurs when close in stream");
                }
            }
            throw th;
        }
    }

    private static Node buildNode(BufferedReader in) throws IOException {
        String currentLine;
        do {
            String readLine = in.readLine();
            currentLine = readLine;
            if (readLine == null) {
                break;
            }
        } while (currentLine.isEmpty());
        if (currentLine == null) {
            return null;
        }
        String currentLine2 = currentLine.trim();
        if (TextUtils.equals(END_OF_INTERNAL_NODE_INDICATOR, currentLine2)) {
            return null;
        }
        Pair<String, String> nameValuePair = parseLine(currentLine2.getBytes(StandardCharsets.UTF_8));
        if (nameValuePair.second != null) {
            return new LeafNode((String) nameValuePair.first, (String) nameValuePair.second);
        }
        List<Node> children = new ArrayList<>();
        while (true) {
            Node buildNode = buildNode(in);
            Node child = buildNode;
            if (buildNode == null) {
                return new InternalNode((String) nameValuePair.first, children);
            }
            children.add(child);
        }
    }

    private static LegacyPasspointConfig processPpsNode(Node ppsNode) throws IOException {
        if (ppsNode.getChildren() == null || ppsNode.getChildren().size() != 1) {
            throw new IOException("PerProviderSubscription node should contain one instance node");
        } else if (TextUtils.equals(TAG_PER_PROVIDER_SUBSCRIPTION, ppsNode.getName())) {
            Node instanceNode = ppsNode.getChildren().get(0);
            if (instanceNode.getChildren() != null) {
                LegacyPasspointConfig config = new LegacyPasspointConfig();
                for (Node node : instanceNode.getChildren()) {
                    String name = node.getName();
                    char c = 65535;
                    int hashCode = name.hashCode();
                    if (hashCode != -2127810660) {
                        if (hashCode == 1310049399 && name.equals(TAG_CREDENTIAL)) {
                            c = 1;
                        }
                    } else if (name.equals(TAG_HOMESP)) {
                        c = 0;
                    }
                    switch (c) {
                        case 0:
                            processHomeSPNode(node, config);
                            break;
                        case 1:
                            processCredentialNode(node, config);
                            break;
                        default:
                            Log.d(TAG, "Ignore uninterested field under PPS instance: " + node.getName());
                            break;
                    }
                }
                if (config.mFqdn != null) {
                    return config;
                }
                throw new IOException("PPS instance missing FQDN");
            }
            throw new IOException("PPS instance node doesn't contained any children");
        } else {
            throw new IOException("Unexpected name for PPS node: " + ppsNode.getName());
        }
    }

    private static void processHomeSPNode(Node homeSpNode, LegacyPasspointConfig config) throws IOException {
        if (homeSpNode.getChildren() != null) {
            for (Node node : homeSpNode.getChildren()) {
                String name = node.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != 2165397) {
                    if (hashCode != 542998228) {
                        if (hashCode == 626253302 && name.equals(TAG_FRIENDLY_NAME)) {
                            c = 1;
                        }
                    } else if (name.equals(TAG_ROAMING_CONSORTIUM_OI)) {
                        c = 2;
                    }
                } else if (name.equals("FQDN")) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        config.mFqdn = getValue(node);
                        break;
                    case 1:
                        config.mFriendlyName = getValue(node);
                        break;
                    case 2:
                        config.mRoamingConsortiumOis = parseLongArray(getValue(node));
                        break;
                    default:
                        Log.d(TAG, "Ignore uninterested field under HomeSP: " + node.getName());
                        break;
                }
            }
            return;
        }
        throw new IOException("HomeSP node should contain at least one child node");
    }

    private static void processCredentialNode(Node credentialNode, LegacyPasspointConfig config) throws IOException {
        if (credentialNode.getChildren() != null) {
            for (Node node : credentialNode.getChildren()) {
                String name = node.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != 82103) {
                    if (hashCode == 78834287 && name.equals("Realm")) {
                        c = 0;
                    }
                } else if (name.equals(TAG_SIM)) {
                    c = 1;
                }
                switch (c) {
                    case 0:
                        config.mRealm = getValue(node);
                        break;
                    case 1:
                        processSimNode(node, config);
                        break;
                    default:
                        Log.d(TAG, "Ignore uninterested field under Credential: " + node.getName());
                        break;
                }
            }
            return;
        }
        throw new IOException("Credential node should contain at least one child node");
    }

    private static void processSimNode(Node simNode, LegacyPasspointConfig config) throws IOException {
        if (simNode.getChildren() != null) {
            for (Node node : simNode.getChildren()) {
                String name = node.getName();
                char c = 65535;
                if (name.hashCode() == 2251386 && name.equals(TAG_IMSI)) {
                    c = 0;
                }
                if (c != 0) {
                    Log.d(TAG, "Ignore uninterested field under SIM: " + node.getName());
                } else {
                    config.mImsi = getValue(node);
                }
            }
            return;
        }
        throw new IOException("SIM node should contain at least one child node");
    }

    private static Pair<String, String> parseLine(byte[] lineBytes) throws IOException {
        Pair<String, Integer> nameIndexPair = parseString(lineBytes, 0);
        int currentIndex = ((Integer) nameIndexPair.second).intValue();
        try {
            if (lineBytes[currentIndex] == 43) {
                return Pair.create((String) nameIndexPair.first, null);
            }
            if (lineBytes[currentIndex] == 61) {
                return Pair.create((String) nameIndexPair.first, (String) parseString(lineBytes, currentIndex + 1).first);
            }
            throw new IOException("Invalid line - missing both node and value indicator: " + new String(lineBytes, StandardCharsets.UTF_8));
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Invalid line - " + e.getMessage() + ": " + new String(lineBytes, StandardCharsets.UTF_8));
        }
    }

    private static Pair<String, Integer> parseString(byte[] lineBytes, int startIndex) throws IOException {
        int prefixIndex = -1;
        int i = startIndex;
        while (true) {
            if (i >= lineBytes.length) {
                break;
            } else if (lineBytes[i] == 58) {
                prefixIndex = i;
                break;
            } else {
                i++;
            }
        }
        if (prefixIndex != -1) {
            try {
                int length = Integer.parseInt(new String(lineBytes, startIndex, prefixIndex - startIndex, StandardCharsets.UTF_8), 16);
                int strStartIndex = prefixIndex + 1;
                if (strStartIndex + length > lineBytes.length) {
                    length = lineBytes.length - strStartIndex;
                }
                return Pair.create(new String(lineBytes, strStartIndex, length, StandardCharsets.UTF_8), Integer.valueOf(strStartIndex + length));
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                throw new IOException("Invalid line - " + e.getMessage() + ": " + new String(lineBytes, StandardCharsets.UTF_8));
            }
        } else {
            throw new IOException("Invalid line - missing string prefix: " + new String(lineBytes, StandardCharsets.UTF_8));
        }
    }

    private static long[] parseLongArray(String str) throws IOException {
        String[] strArray = str.split(LONG_ARRAY_SEPARATOR);
        long[] longArray = new long[strArray.length];
        int i = 0;
        while (i < longArray.length) {
            try {
                longArray[i] = Long.parseLong(strArray[i], 16);
                i++;
            } catch (NumberFormatException e) {
                throw new IOException("Invalid long integer value: " + strArray[i]);
            }
        }
        return longArray;
    }

    private static String getValue(Node node) throws IOException {
        if (node.getValue() != null) {
            return node.getValue();
        }
        throw new IOException("Attempt to retreive value from non-leaf node: " + node.getName());
    }
}
