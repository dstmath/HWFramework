package com.android.server.signedconfig;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.signedconfig.SignedConfig;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/* access modifiers changed from: package-private */
public class GlobalSettingsConfigApplicator {
    private static final Set<String> ALLOWED_KEYS = Collections.unmodifiableSet(new ArraySet(Arrays.asList("hidden_api_policy", "hidden_api_blacklist_exemptions")));
    private static final Map<String, String> HIDDEN_API_POLICY_KEY_MAP = makeMap("DEFAULT", String.valueOf(-1), "DISABLED", String.valueOf(0), "JUST_WARN", String.valueOf(1), "ENABLED", String.valueOf(2));
    private static final Map<String, Map<String, String>> KEY_VALUE_MAPPERS = makeMap("hidden_api_policy", HIDDEN_API_POLICY_KEY_MAP);
    private static final String TAG = "SignedConfig";
    private final Context mContext;
    private final SignedConfigEvent mEvent;
    private final String mSourcePackage;
    private final SignatureVerifier mVerifier = new SignatureVerifier(this.mEvent);

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.util.ArrayMap */
    /* JADX WARN: Multi-variable type inference failed */
    private static <K, V> Map<K, V> makeMap(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 == 0) {
            int len = keyValuePairs.length / 2;
            ArrayMap arrayMap = new ArrayMap(len);
            for (int i = 0; i < len; i++) {
                arrayMap.put(keyValuePairs[i * 2], keyValuePairs[(i * 2) + 1]);
            }
            return Collections.unmodifiableMap(arrayMap);
        }
        throw new IllegalArgumentException();
    }

    GlobalSettingsConfigApplicator(Context context, String sourcePackage, SignedConfigEvent event) {
        this.mContext = context;
        this.mSourcePackage = sourcePackage;
        this.mEvent = event;
    }

    private boolean checkSignature(String data, String signature) {
        try {
            return this.mVerifier.verifySignature(data, signature);
        } catch (GeneralSecurityException e) {
            Slog.e(TAG, "Failed to verify signature", e);
            this.mEvent.status = 4;
            return false;
        }
    }

    private int getCurrentConfigVersion() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "signed_config_version", 0);
    }

    private void updateCurrentConfig(int version, Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet()) {
            Settings.Global.putString(this.mContext.getContentResolver(), e.getKey(), e.getValue());
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "signed_config_version", version);
    }

    /* access modifiers changed from: package-private */
    public void applyConfig(String configStr, String signature) {
        if (!checkSignature(configStr, signature)) {
            Slog.e(TAG, "Signature check on global settings in package " + this.mSourcePackage + " failed; ignoring");
            return;
        }
        try {
            SignedConfig config = SignedConfig.parse(configStr, ALLOWED_KEYS, KEY_VALUE_MAPPERS);
            this.mEvent.version = config.version;
            int currentVersion = getCurrentConfigVersion();
            if (currentVersion >= config.version) {
                Slog.i(TAG, "Global settings from package " + this.mSourcePackage + " is older than existing: " + config.version + "<=" + currentVersion);
                this.mEvent.status = 6;
                return;
            }
            Slog.i(TAG, "Got new global settings from package " + this.mSourcePackage + ": version " + config.version + " replacing existing version " + currentVersion);
            SignedConfig.PerSdkConfig matchedConfig = config.getMatchingConfig(Build.VERSION.SDK_INT);
            if (matchedConfig == null) {
                Slog.i(TAG, "Settings is not applicable to current SDK version; ignoring");
                this.mEvent.status = 8;
                return;
            }
            Slog.i(TAG, "Updating global settings to version " + config.version);
            updateCurrentConfig(config.version, matchedConfig.values);
            this.mEvent.status = 1;
        } catch (InvalidConfigException e) {
            Slog.e(TAG, "Failed to parse global settings from package " + this.mSourcePackage, e);
            this.mEvent.status = 5;
        }
    }
}
