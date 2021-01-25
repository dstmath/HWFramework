package com.android.server.signedconfig;

import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignedConfig {
    private static final String CONFIG_KEY_MAX_SDK = "max_sdk";
    private static final String CONFIG_KEY_MIN_SDK = "min_sdk";
    private static final String CONFIG_KEY_VALUES = "values";
    private static final String KEY_CONFIG = "config";
    private static final String KEY_VERSION = "version";
    public final List<PerSdkConfig> perSdkConfig;
    public final int version;

    public static class PerSdkConfig {
        public final int maxSdk;
        public final int minSdk;
        public final Map<String, String> values;

        public PerSdkConfig(int minSdk2, int maxSdk2, Map<String, String> values2) {
            this.minSdk = minSdk2;
            this.maxSdk = maxSdk2;
            this.values = Collections.unmodifiableMap(values2);
        }
    }

    public SignedConfig(int version2, List<PerSdkConfig> perSdkConfig2) {
        this.version = version2;
        this.perSdkConfig = Collections.unmodifiableList(perSdkConfig2);
    }

    public PerSdkConfig getMatchingConfig(int sdkVersion) {
        for (PerSdkConfig config : this.perSdkConfig) {
            if (config.minSdk <= sdkVersion && sdkVersion <= config.maxSdk) {
                return config;
            }
        }
        return null;
    }

    public static SignedConfig parse(String config, Set<String> allowedKeys, Map<String, Map<String, String>> keyValueMappers) throws InvalidConfigException {
        try {
            JSONObject json = new JSONObject(config);
            int version2 = json.getInt(KEY_VERSION);
            JSONArray perSdkConfig2 = json.getJSONArray(KEY_CONFIG);
            List<PerSdkConfig> parsedConfigs = new ArrayList<>();
            for (int i = 0; i < perSdkConfig2.length(); i++) {
                parsedConfigs.add(parsePerSdkConfig(perSdkConfig2.getJSONObject(i), allowedKeys, keyValueMappers));
            }
            return new SignedConfig(version2, parsedConfigs);
        } catch (JSONException e) {
            throw new InvalidConfigException("Could not parse JSON", e);
        }
    }

    private static CharSequence quoted(Object s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s + "\"";
    }

    @VisibleForTesting
    static PerSdkConfig parsePerSdkConfig(JSONObject json, Set<String> allowedKeys, Map<String, Map<String, String>> keyValueMappers) throws JSONException, InvalidConfigException {
        String value;
        int minSdk = json.getInt(CONFIG_KEY_MIN_SDK);
        int maxSdk = json.getInt(CONFIG_KEY_MAX_SDK);
        JSONObject valuesJson = json.getJSONObject(CONFIG_KEY_VALUES);
        Map<String, String> values = new HashMap<>();
        for (String key : valuesJson.keySet()) {
            Object valueObject = valuesJson.get(key);
            if (valueObject == JSONObject.NULL || valueObject == null) {
                value = null;
            } else {
                value = valueObject.toString();
            }
            if (allowedKeys.contains(key)) {
                if (keyValueMappers.containsKey(key)) {
                    Map<String, String> mapper = keyValueMappers.get(key);
                    if (mapper.containsKey(value)) {
                        value = mapper.get(value);
                    } else {
                        throw new InvalidConfigException("Config key " + key + " contains unsupported value " + ((Object) quoted(value)));
                    }
                }
                values.put(key, value);
            } else {
                throw new InvalidConfigException("Config key " + key + " is not allowed");
            }
        }
        return new PerSdkConfig(minSdk, maxSdk, values);
    }
}
