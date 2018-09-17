package android.support.v4.app;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.RemoteInputCompatBase.RemoteInput;
import android.support.v4.app.RemoteInputCompatBase.RemoteInput.Factory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@RequiresApi(16)
class RemoteInputCompatJellybean {
    private static final String EXTRA_DATA_TYPE_RESULTS_DATA = "android.remoteinput.dataTypeResultsData";
    public static final String EXTRA_RESULTS_DATA = "android.remoteinput.resultsData";
    private static final String KEY_ALLOWED_DATA_TYPES = "allowedDataTypes";
    private static final String KEY_ALLOW_FREE_FORM_INPUT = "allowFreeFormInput";
    private static final String KEY_CHOICES = "choices";
    private static final String KEY_EXTRAS = "extras";
    private static final String KEY_LABEL = "label";
    private static final String KEY_RESULT_KEY = "resultKey";
    public static final String RESULTS_CLIP_LABEL = "android.remoteinput.results";

    RemoteInputCompatJellybean() {
    }

    static RemoteInput fromBundle(Bundle data, Factory factory) {
        ArrayList<String> allowedDataTypesAsList = data.getStringArrayList(KEY_ALLOWED_DATA_TYPES);
        Set<String> allowedDataTypes = new HashSet();
        if (allowedDataTypesAsList != null) {
            for (String type : allowedDataTypesAsList) {
                allowedDataTypes.add(type);
            }
        }
        return factory.build(data.getString(KEY_RESULT_KEY), data.getCharSequence(KEY_LABEL), data.getCharSequenceArray(KEY_CHOICES), data.getBoolean(KEY_ALLOW_FREE_FORM_INPUT), data.getBundle(KEY_EXTRAS), allowedDataTypes);
    }

    static Bundle toBundle(RemoteInput remoteInput) {
        Bundle data = new Bundle();
        data.putString(KEY_RESULT_KEY, remoteInput.getResultKey());
        data.putCharSequence(KEY_LABEL, remoteInput.getLabel());
        data.putCharSequenceArray(KEY_CHOICES, remoteInput.getChoices());
        data.putBoolean(KEY_ALLOW_FREE_FORM_INPUT, remoteInput.getAllowFreeFormInput());
        data.putBundle(KEY_EXTRAS, remoteInput.getExtras());
        Set<String> allowedDataTypes = remoteInput.getAllowedDataTypes();
        if (!(allowedDataTypes == null || (allowedDataTypes.isEmpty() ^ 1) == 0)) {
            ArrayList<String> allowedDataTypesAsList = new ArrayList(allowedDataTypes.size());
            for (String type : allowedDataTypes) {
                allowedDataTypesAsList.add(type);
            }
            data.putStringArrayList(KEY_ALLOWED_DATA_TYPES, allowedDataTypesAsList);
        }
        return data;
    }

    static RemoteInput[] fromBundleArray(Bundle[] bundles, Factory factory) {
        if (bundles == null) {
            return null;
        }
        RemoteInput[] remoteInputs = factory.newArray(bundles.length);
        for (int i = 0; i < bundles.length; i++) {
            remoteInputs[i] = fromBundle(bundles[i], factory);
        }
        return remoteInputs;
    }

    static Bundle[] toBundleArray(RemoteInput[] remoteInputs) {
        if (remoteInputs == null) {
            return null;
        }
        Bundle[] bundles = new Bundle[remoteInputs.length];
        for (int i = 0; i < remoteInputs.length; i++) {
            bundles[i] = toBundle(remoteInputs[i]);
        }
        return bundles;
    }

    static Bundle getResultsFromIntent(Intent intent) {
        Intent clipDataIntent = getClipDataIntentFromIntent(intent);
        if (clipDataIntent == null) {
            return null;
        }
        return (Bundle) clipDataIntent.getExtras().getParcelable("android.remoteinput.resultsData");
    }

    static Map<String, Uri> getDataResultsFromIntent(Intent intent, String remoteInputResultKey) {
        Intent clipDataIntent = getClipDataIntentFromIntent(intent);
        if (clipDataIntent == null) {
            return null;
        }
        Map<String, Uri> results = new HashMap();
        for (String key : clipDataIntent.getExtras().keySet()) {
            if (key.startsWith(EXTRA_DATA_TYPE_RESULTS_DATA)) {
                String mimeType = key.substring(EXTRA_DATA_TYPE_RESULTS_DATA.length());
                if (!(mimeType == null || mimeType.isEmpty())) {
                    String uriStr = clipDataIntent.getBundleExtra(key).getString(remoteInputResultKey);
                    if (!(uriStr == null || uriStr.isEmpty())) {
                        results.put(mimeType, Uri.parse(uriStr));
                    }
                }
            }
        }
        if (results.isEmpty()) {
            results = null;
        }
        return results;
    }

    static void addResultsToIntent(RemoteInput[] remoteInputs, Intent intent, Bundle results) {
        Intent clipDataIntent = getClipDataIntentFromIntent(intent);
        if (clipDataIntent == null) {
            clipDataIntent = new Intent();
        }
        Bundle resultsBundle = clipDataIntent.getBundleExtra("android.remoteinput.resultsData");
        if (resultsBundle == null) {
            resultsBundle = new Bundle();
        }
        for (RemoteInput remoteInput : remoteInputs) {
            Object result = results.get(remoteInput.getResultKey());
            if (result instanceof CharSequence) {
                resultsBundle.putCharSequence(remoteInput.getResultKey(), (CharSequence) result);
            }
        }
        clipDataIntent.putExtra("android.remoteinput.resultsData", resultsBundle);
        intent.setClipData(ClipData.newIntent("android.remoteinput.results", clipDataIntent));
    }

    public static void addDataResultToIntent(RemoteInput remoteInput, Intent intent, Map<String, Uri> results) {
        Intent clipDataIntent = getClipDataIntentFromIntent(intent);
        if (clipDataIntent == null) {
            clipDataIntent = new Intent();
        }
        for (Entry<String, Uri> entry : results.entrySet()) {
            String mimeType = (String) entry.getKey();
            Uri uri = (Uri) entry.getValue();
            if (mimeType != null) {
                Bundle resultsBundle = clipDataIntent.getBundleExtra(getExtraResultsKeyForData(mimeType));
                if (resultsBundle == null) {
                    resultsBundle = new Bundle();
                }
                resultsBundle.putString(remoteInput.getResultKey(), uri.toString());
                clipDataIntent.putExtra(getExtraResultsKeyForData(mimeType), resultsBundle);
            }
        }
        intent.setClipData(ClipData.newIntent("android.remoteinput.results", clipDataIntent));
    }

    private static String getExtraResultsKeyForData(String mimeType) {
        return EXTRA_DATA_TYPE_RESULTS_DATA + mimeType;
    }

    private static Intent getClipDataIntentFromIntent(Intent intent) {
        ClipData clipData = intent.getClipData();
        if (clipData == null) {
            return null;
        }
        ClipDescription clipDescription = clipData.getDescription();
        if (clipDescription.hasMimeType("text/vnd.android.intent") && clipDescription.getLabel().equals("android.remoteinput.results")) {
            return clipData.getItemAt(0).getIntent();
        }
        return null;
    }
}
