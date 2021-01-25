package android.support.v4.app;

import android.app.RemoteInput;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class RemoteInput {
    private static final String EXTRA_DATA_TYPE_RESULTS_DATA = "android.remoteinput.dataTypeResultsData";
    public static final String EXTRA_RESULTS_DATA = "android.remoteinput.resultsData";
    public static final String RESULTS_CLIP_LABEL = "android.remoteinput.results";
    private static final String TAG = "RemoteInput";
    private final boolean mAllowFreeFormTextInput;
    private final Set<String> mAllowedDataTypes;
    private final CharSequence[] mChoices;
    private final Bundle mExtras;
    private final CharSequence mLabel;
    private final String mResultKey;

    RemoteInput(String resultKey, CharSequence label, CharSequence[] choices, boolean allowFreeFormTextInput, Bundle extras, Set<String> allowedDataTypes) {
        this.mResultKey = resultKey;
        this.mLabel = label;
        this.mChoices = choices;
        this.mAllowFreeFormTextInput = allowFreeFormTextInput;
        this.mExtras = extras;
        this.mAllowedDataTypes = allowedDataTypes;
    }

    public String getResultKey() {
        return this.mResultKey;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public CharSequence[] getChoices() {
        return this.mChoices;
    }

    public Set<String> getAllowedDataTypes() {
        return this.mAllowedDataTypes;
    }

    public boolean isDataOnly() {
        return !getAllowFreeFormInput() && (getChoices() == null || getChoices().length == 0) && getAllowedDataTypes() != null && !getAllowedDataTypes().isEmpty();
    }

    public boolean getAllowFreeFormInput() {
        return this.mAllowFreeFormTextInput;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public static final class Builder {
        private boolean mAllowFreeFormTextInput = true;
        private final Set<String> mAllowedDataTypes = new HashSet();
        private CharSequence[] mChoices;
        private final Bundle mExtras = new Bundle();
        private CharSequence mLabel;
        private final String mResultKey;

        public Builder(@NonNull String resultKey) {
            if (resultKey != null) {
                this.mResultKey = resultKey;
                return;
            }
            throw new IllegalArgumentException("Result key can't be null");
        }

        @NonNull
        public Builder setLabel(@Nullable CharSequence label) {
            this.mLabel = label;
            return this;
        }

        @NonNull
        public Builder setChoices(@Nullable CharSequence[] choices) {
            this.mChoices = choices;
            return this;
        }

        @NonNull
        public Builder setAllowDataType(@NonNull String mimeType, boolean doAllow) {
            if (doAllow) {
                this.mAllowedDataTypes.add(mimeType);
            } else {
                this.mAllowedDataTypes.remove(mimeType);
            }
            return this;
        }

        @NonNull
        public Builder setAllowFreeFormInput(boolean allowFreeFormTextInput) {
            this.mAllowFreeFormTextInput = allowFreeFormTextInput;
            return this;
        }

        @NonNull
        public Builder addExtras(@NonNull Bundle extras) {
            if (extras != null) {
                this.mExtras.putAll(extras);
            }
            return this;
        }

        @NonNull
        public Bundle getExtras() {
            return this.mExtras;
        }

        @NonNull
        public RemoteInput build() {
            return new RemoteInput(this.mResultKey, this.mLabel, this.mChoices, this.mAllowFreeFormTextInput, this.mExtras, this.mAllowedDataTypes);
        }
    }

    public static Map<String, Uri> getDataResultsFromIntent(Intent intent, String remoteInputResultKey) {
        String uriStr;
        if (Build.VERSION.SDK_INT >= 26) {
            return android.app.RemoteInput.getDataResultsFromIntent(intent, remoteInputResultKey);
        }
        if (Build.VERSION.SDK_INT >= 16) {
            Intent clipDataIntent = getClipDataIntentFromIntent(intent);
            if (clipDataIntent == null) {
                return null;
            }
            Map<String, Uri> results = new HashMap<>();
            for (String key : clipDataIntent.getExtras().keySet()) {
                if (key.startsWith(EXTRA_DATA_TYPE_RESULTS_DATA)) {
                    String mimeType = key.substring(EXTRA_DATA_TYPE_RESULTS_DATA.length());
                    if (!mimeType.isEmpty() && (uriStr = clipDataIntent.getBundleExtra(key).getString(remoteInputResultKey)) != null && !uriStr.isEmpty()) {
                        results.put(mimeType, Uri.parse(uriStr));
                    }
                }
            }
            if (results.isEmpty()) {
                return null;
            }
            return results;
        }
        Log.w(TAG, "RemoteInput is only supported from API Level 16");
        return null;
    }

    public static Bundle getResultsFromIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= 20) {
            return android.app.RemoteInput.getResultsFromIntent(intent);
        }
        if (Build.VERSION.SDK_INT >= 16) {
            Intent clipDataIntent = getClipDataIntentFromIntent(intent);
            if (clipDataIntent == null) {
                return null;
            }
            return (Bundle) clipDataIntent.getExtras().getParcelable(EXTRA_RESULTS_DATA);
        }
        Log.w(TAG, "RemoteInput is only supported from API Level 16");
        return null;
    }

    public static void addResultsToIntent(RemoteInput[] remoteInputs, Intent intent, Bundle results) {
        if (Build.VERSION.SDK_INT >= 26) {
            android.app.RemoteInput.addResultsToIntent(fromCompat(remoteInputs), intent, results);
            return;
        }
        if (Build.VERSION.SDK_INT >= 20) {
            Bundle existingTextResults = getResultsFromIntent(intent);
            if (existingTextResults == null) {
                existingTextResults = results;
            } else {
                existingTextResults.putAll(results);
            }
            for (RemoteInput input : remoteInputs) {
                Map<String, Uri> existingDataResults = getDataResultsFromIntent(intent, input.getResultKey());
                android.app.RemoteInput.addResultsToIntent(fromCompat(new RemoteInput[]{input}), intent, existingTextResults);
                if (existingDataResults != null) {
                    addDataResultToIntent(input, intent, existingDataResults);
                }
            }
        } else if (Build.VERSION.SDK_INT >= 16) {
            Intent clipDataIntent = getClipDataIntentFromIntent(intent);
            if (clipDataIntent == null) {
                clipDataIntent = new Intent();
            }
            Bundle resultsBundle = clipDataIntent.getBundleExtra(EXTRA_RESULTS_DATA);
            if (resultsBundle == null) {
                resultsBundle = new Bundle();
            }
            for (RemoteInput remoteInput : remoteInputs) {
                Object result = results.get(remoteInput.getResultKey());
                if (result instanceof CharSequence) {
                    resultsBundle.putCharSequence(remoteInput.getResultKey(), (CharSequence) result);
                }
            }
            clipDataIntent.putExtra(EXTRA_RESULTS_DATA, resultsBundle);
            intent.setClipData(ClipData.newIntent(RESULTS_CLIP_LABEL, clipDataIntent));
        } else {
            Log.w(TAG, "RemoteInput is only supported from API Level 16");
        }
    }

    public static void addDataResultToIntent(RemoteInput remoteInput, Intent intent, Map<String, Uri> results) {
        if (Build.VERSION.SDK_INT >= 26) {
            android.app.RemoteInput.addDataResultToIntent(fromCompat(remoteInput), intent, results);
        } else if (Build.VERSION.SDK_INT >= 16) {
            Intent clipDataIntent = getClipDataIntentFromIntent(intent);
            if (clipDataIntent == null) {
                clipDataIntent = new Intent();
            }
            for (Map.Entry<String, Uri> entry : results.entrySet()) {
                String mimeType = entry.getKey();
                Uri uri = entry.getValue();
                if (mimeType != null) {
                    Bundle resultsBundle = clipDataIntent.getBundleExtra(getExtraResultsKeyForData(mimeType));
                    if (resultsBundle == null) {
                        resultsBundle = new Bundle();
                    }
                    resultsBundle.putString(remoteInput.getResultKey(), uri.toString());
                    clipDataIntent.putExtra(getExtraResultsKeyForData(mimeType), resultsBundle);
                }
            }
            intent.setClipData(ClipData.newIntent(RESULTS_CLIP_LABEL, clipDataIntent));
        } else {
            Log.w(TAG, "RemoteInput is only supported from API Level 16");
        }
    }

    private static String getExtraResultsKeyForData(String mimeType) {
        return EXTRA_DATA_TYPE_RESULTS_DATA + mimeType;
    }

    @RequiresApi(20)
    static android.app.RemoteInput[] fromCompat(RemoteInput[] srcArray) {
        if (srcArray == null) {
            return null;
        }
        android.app.RemoteInput[] result = new android.app.RemoteInput[srcArray.length];
        for (int i = 0; i < srcArray.length; i++) {
            result[i] = fromCompat(srcArray[i]);
        }
        return result;
    }

    @RequiresApi(20)
    static android.app.RemoteInput fromCompat(RemoteInput src) {
        return new RemoteInput.Builder(src.getResultKey()).setLabel(src.getLabel()).setChoices(src.getChoices()).setAllowFreeFormInput(src.getAllowFreeFormInput()).addExtras(src.getExtras()).build();
    }

    @RequiresApi(16)
    private static Intent getClipDataIntentFromIntent(Intent intent) {
        ClipData clipData = intent.getClipData();
        if (clipData == null) {
            return null;
        }
        ClipDescription clipDescription = clipData.getDescription();
        if (clipDescription.hasMimeType("text/vnd.android.intent") && clipDescription.getLabel().equals(RESULTS_CLIP_LABEL)) {
            return clipData.getItemAt(0).getIntent();
        }
        return null;
    }
}
