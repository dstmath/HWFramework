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

    public Map<String, LegacyPasspointConfig> parseConfig(String fileName) throws IOException {
        Throwable th;
        Map<String, LegacyPasspointConfig> configs = new HashMap();
        BufferedReader in = null;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(fileName));
            try {
                in2.readLine();
                Node root = buildNode(in2);
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "IOException occurs when close in stream");
                    }
                    if (root != null || root.getChildren() == null) {
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
                }
                if (root != null) {
                }
                Log.d(TAG, "Empty configuration data");
                return configs;
            } catch (Throwable th2) {
                th = th2;
                in = in2;
            }
        } catch (Throwable th3) {
            th = th3;
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
            currentLine = in.readLine();
            if (currentLine == null) {
                break;
            }
        } while (currentLine.isEmpty());
        if (currentLine == null) {
            return null;
        }
        currentLine = currentLine.trim();
        if (TextUtils.equals(END_OF_INTERNAL_NODE_INDICATOR, currentLine)) {
            return null;
        }
        Pair<String, String> nameValuePair = parseLine(currentLine.getBytes(StandardCharsets.UTF_8));
        if (nameValuePair.second != null) {
            return new LeafNode((String) nameValuePair.first, (String) nameValuePair.second);
        }
        List<Node> children = new ArrayList();
        while (true) {
            Node child = buildNode(in);
            if (child == null) {
                return new InternalNode((String) nameValuePair.first, children);
            }
            children.add(child);
        }
    }

    private static LegacyPasspointConfig processPpsNode(Node ppsNode) throws IOException {
        if (ppsNode.getChildren() == null || ppsNode.getChildren().size() != 1) {
            throw new IOException("PerProviderSubscription node should contain one instance node");
        } else if (TextUtils.equals(TAG_PER_PROVIDER_SUBSCRIPTION, ppsNode.getName())) {
            Node instanceNode = (Node) ppsNode.getChildren().get(0);
            if (instanceNode.getChildren() == null) {
                throw new IOException("PPS instance node doesn't contained any children");
            }
            LegacyPasspointConfig config = new LegacyPasspointConfig();
            for (Node node : instanceNode.getChildren()) {
                String name = node.getName();
                if (name.equals(TAG_HOMESP)) {
                    processHomeSPNode(node, config);
                } else if (name.equals(TAG_CREDENTIAL)) {
                    processCredentialNode(node, config);
                } else {
                    Log.d(TAG, "Ignore uninterested field under PPS instance: " + node.getName());
                }
            }
            if (config.mFqdn != null) {
                return config;
            }
            throw new IOException("PPS instance missing FQDN");
        } else {
            throw new IOException("Unexpected name for PPS node: " + ppsNode.getName());
        }
    }

    private static void processHomeSPNode(Node homeSpNode, LegacyPasspointConfig config) throws IOException {
        if (homeSpNode.getChildren() == null) {
            throw new IOException("HomeSP node should contain at least one child node");
        }
        for (Node node : homeSpNode.getChildren()) {
            String name = node.getName();
            if (name.equals("FQDN")) {
                config.mFqdn = getValue(node);
            } else if (name.equals(TAG_FRIENDLY_NAME)) {
                config.mFriendlyName = getValue(node);
            } else if (name.equals(TAG_ROAMING_CONSORTIUM_OI)) {
                config.mRoamingConsortiumOis = parseLongArray(getValue(node));
            } else {
                Log.d(TAG, "Ignore uninterested field under HomeSP: " + node.getName());
            }
        }
    }

    private static void processCredentialNode(Node credentialNode, LegacyPasspointConfig config) throws IOException {
        if (credentialNode.getChildren() == null) {
            throw new IOException("Credential node should contain at least one child node");
        }
        for (Node node : credentialNode.getChildren()) {
            String name = node.getName();
            if (name.equals("Realm")) {
                config.mRealm = getValue(node);
            } else if (name.equals(TAG_SIM)) {
                processSimNode(node, config);
            } else {
                Log.d(TAG, "Ignore uninterested field under Credential: " + node.getName());
            }
        }
    }

    private static void processSimNode(Node simNode, LegacyPasspointConfig config) throws IOException {
        if (simNode.getChildren() == null) {
            throw new IOException("SIM node should contain at least one child node");
        }
        for (Node node : simNode.getChildren()) {
            if (node.getName().equals(TAG_IMSI)) {
                config.mImsi = getValue(node);
            } else {
                Log.d(TAG, "Ignore uninterested field under SIM: " + node.getName());
            }
        }
    }

    private static Pair<String, String> parseLine(byte[] lineBytes) throws IOException {
        Pair<String, Integer> nameIndexPair = parseString(lineBytes, 0);
        int currentIndex = ((Integer) nameIndexPair.second).intValue();
        try {
            if (lineBytes[currentIndex] == (byte) 43) {
                return Pair.create((String) nameIndexPair.first, null);
            }
            if (lineBytes[currentIndex] != (byte) 61) {
                throw new IOException("Invalid line - missing both node and value indicator: " + new String(lineBytes, StandardCharsets.UTF_8));
            }
            return Pair.create((String) nameIndexPair.first, (String) parseString(lineBytes, currentIndex + 1).first);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Invalid line - " + e.getMessage() + ": " + new String(lineBytes, StandardCharsets.UTF_8));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x005e A:{Splitter: B:11:0x0033, ExcHandler: java.lang.NumberFormatException (r0_0 'e' java.lang.RuntimeException)} */
    /* JADX WARNING: Missing block: B:17:0x005e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:0x008e, code:
            throw new java.io.IOException("Invalid line - " + r0.getMessage() + ": " + new java.lang.String(r10, java.nio.charset.StandardCharsets.UTF_8));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Pair<String, Integer> parseString(byte[] lineBytes, int startIndex) throws IOException {
        int prefixIndex = -1;
        for (int i = startIndex; i < lineBytes.length; i++) {
            if (lineBytes[i] == (byte) 58) {
                prefixIndex = i;
                break;
            }
        }
        if (prefixIndex == -1) {
            throw new IOException("Invalid line - missing string prefix: " + new String(lineBytes, StandardCharsets.UTF_8));
        }
        try {
            int length = Integer.parseInt(new String(lineBytes, startIndex, prefixIndex - startIndex, StandardCharsets.UTF_8), 16);
            int strStartIndex = prefixIndex + 1;
            if (strStartIndex + length > lineBytes.length) {
                length = lineBytes.length - strStartIndex;
            }
            return Pair.create(new String(lineBytes, strStartIndex, length, StandardCharsets.UTF_8), Integer.valueOf(strStartIndex + length));
        } catch (RuntimeException e) {
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
