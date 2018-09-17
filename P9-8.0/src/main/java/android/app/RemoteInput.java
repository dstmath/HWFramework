package android.app;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArraySet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class RemoteInput implements Parcelable {
    public static final Creator<RemoteInput> CREATOR = new Creator<RemoteInput>() {
        public RemoteInput createFromParcel(Parcel in) {
            return new RemoteInput(in, null);
        }

        public RemoteInput[] newArray(int size) {
            return new RemoteInput[size];
        }
    };
    private static final int DEFAULT_FLAGS = 1;
    private static final String EXTRA_DATA_TYPE_RESULTS_DATA = "android.remoteinput.dataTypeResultsData";
    public static final String EXTRA_RESULTS_DATA = "android.remoteinput.resultsData";
    private static final int FLAG_ALLOW_FREE_FORM_INPUT = 1;
    public static final String RESULTS_CLIP_LABEL = "android.remoteinput.results";
    private final ArraySet<String> mAllowedDataTypes;
    private final CharSequence[] mChoices;
    private final Bundle mExtras;
    private final int mFlags;
    private final CharSequence mLabel;
    private final String mResultKey;

    public static final class Builder {
        private final ArraySet<String> mAllowedDataTypes = new ArraySet();
        private CharSequence[] mChoices;
        private Bundle mExtras = new Bundle();
        private int mFlags = 1;
        private CharSequence mLabel;
        private final String mResultKey;

        public Builder(String resultKey) {
            if (resultKey == null) {
                throw new IllegalArgumentException("Result key can't be null");
            }
            this.mResultKey = resultKey;
        }

        public Builder setLabel(CharSequence label) {
            this.mLabel = Notification.safeCharSequence(label);
            return this;
        }

        public Builder setChoices(CharSequence[] choices) {
            if (choices == null) {
                this.mChoices = null;
            } else {
                this.mChoices = new CharSequence[choices.length];
                for (int i = 0; i < choices.length; i++) {
                    this.mChoices[i] = Notification.safeCharSequence(choices[i]);
                }
            }
            return this;
        }

        public Builder setAllowDataType(String mimeType, boolean doAllow) {
            if (doAllow) {
                this.mAllowedDataTypes.add(mimeType);
            } else {
                this.mAllowedDataTypes.remove(mimeType);
            }
            return this;
        }

        public Builder setAllowFreeFormInput(boolean allowFreeFormTextInput) {
            setFlag(this.mFlags, allowFreeFormTextInput);
            return this;
        }

        public Builder addExtras(Bundle extras) {
            if (extras != null) {
                this.mExtras.putAll(extras);
            }
            return this;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        private void setFlag(int mask, boolean value) {
            if (value) {
                this.mFlags |= mask;
            } else {
                this.mFlags &= ~mask;
            }
        }

        public RemoteInput build() {
            return new RemoteInput(this.mResultKey, this.mLabel, this.mChoices, this.mFlags, this.mExtras, this.mAllowedDataTypes, null);
        }
    }

    /* synthetic */ RemoteInput(Parcel in, RemoteInput -this1) {
        this(in);
    }

    /* synthetic */ RemoteInput(String resultKey, CharSequence label, CharSequence[] choices, int flags, Bundle extras, ArraySet allowedDataTypes, RemoteInput -this6) {
        this(resultKey, label, choices, flags, extras, allowedDataTypes);
    }

    private RemoteInput(String resultKey, CharSequence label, CharSequence[] choices, int flags, Bundle extras, ArraySet<String> allowedDataTypes) {
        this.mResultKey = resultKey;
        this.mLabel = label;
        this.mChoices = choices;
        this.mFlags = flags;
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
        if (getAllowFreeFormInput()) {
            return false;
        }
        if (getChoices() == null || getChoices().length == 0) {
            return getAllowedDataTypes().isEmpty() ^ 1;
        }
        return false;
    }

    public boolean getAllowFreeFormInput() {
        return (this.mFlags & 1) != 0;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    private RemoteInput(Parcel in) {
        this.mResultKey = in.readString();
        this.mLabel = in.readCharSequence();
        this.mChoices = in.readCharSequenceArray();
        this.mFlags = in.readInt();
        this.mExtras = in.readBundle();
        this.mAllowedDataTypes = in.readArraySet(null);
    }

    public static Map<String, Uri> getDataResultsFromIntent(Intent intent, String remoteInputResultKey) {
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

    public static Bundle getResultsFromIntent(Intent intent) {
        Intent clipDataIntent = getClipDataIntentFromIntent(intent);
        if (clipDataIntent == null) {
            return null;
        }
        return (Bundle) clipDataIntent.getExtras().getParcelable(EXTRA_RESULTS_DATA);
    }

    public static void addResultsToIntent(RemoteInput[] remoteInputs, Intent intent, Bundle results) {
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
        intent.setClipData(ClipData.newIntent(RESULTS_CLIP_LABEL, clipDataIntent));
    }

    private static String getExtraResultsKeyForData(String mimeType) {
        return EXTRA_DATA_TYPE_RESULTS_DATA + mimeType;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mResultKey);
        out.writeCharSequence(this.mLabel);
        out.writeCharSequenceArray(this.mChoices);
        out.writeInt(this.mFlags);
        out.writeBundle(this.mExtras);
        out.writeArraySet(this.mAllowedDataTypes);
    }

    private static Intent getClipDataIntentFromIntent(Intent intent) {
        ClipData clipData = intent.getClipData();
        if (clipData == null) {
            return null;
        }
        ClipDescription clipDescription = clipData.getDescription();
        if (clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT) && clipDescription.getLabel().equals(RESULTS_CLIP_LABEL)) {
            return clipData.getItemAt(0).getIntent();
        }
        return null;
    }
}
