package android.app;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class RemoteInput implements Parcelable {
    public static final Creator<RemoteInput> CREATOR = null;
    private static final int DEFAULT_FLAGS = 1;
    public static final String EXTRA_RESULTS_DATA = "android.remoteinput.resultsData";
    private static final int FLAG_ALLOW_FREE_FORM_INPUT = 1;
    public static final String RESULTS_CLIP_LABEL = "android.remoteinput.results";
    private final CharSequence[] mChoices;
    private final Bundle mExtras;
    private final int mFlags;
    private final CharSequence mLabel;
    private final String mResultKey;

    public static final class Builder {
        private CharSequence[] mChoices;
        private Bundle mExtras;
        private int mFlags;
        private CharSequence mLabel;
        private final String mResultKey;

        private void setFlag(int r1, boolean r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.RemoteInput.Builder.setFlag(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 6 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.RemoteInput.Builder.setFlag(int, boolean):void");
        }

        public Builder(String resultKey) {
            this.mFlags = RemoteInput.FLAG_ALLOW_FREE_FORM_INPUT;
            this.mExtras = new Bundle();
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
                for (int i = 0; i < choices.length; i += RemoteInput.FLAG_ALLOW_FREE_FORM_INPUT) {
                    this.mChoices[i] = Notification.safeCharSequence(choices[i]);
                }
            }
            return this;
        }

        public Builder setAllowFreeFormInput(boolean allowFreeFormInput) {
            setFlag(this.mFlags, allowFreeFormInput);
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

        public RemoteInput build() {
            return new RemoteInput(this.mLabel, this.mChoices, this.mFlags, this.mExtras, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.RemoteInput.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.RemoteInput.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.RemoteInput.<clinit>():void");
    }

    private RemoteInput(String resultKey, CharSequence label, CharSequence[] choices, int flags, Bundle extras) {
        this.mResultKey = resultKey;
        this.mLabel = label;
        this.mChoices = choices;
        this.mFlags = flags;
        this.mExtras = extras;
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

    public boolean getAllowFreeFormInput() {
        return (this.mFlags & FLAG_ALLOW_FREE_FORM_INPUT) != 0;
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
    }

    public static Bundle getResultsFromIntent(Intent intent) {
        ClipData clipData = intent.getClipData();
        if (clipData == null) {
            return null;
        }
        ClipDescription clipDescription = clipData.getDescription();
        if (clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT) && clipDescription.getLabel().equals(RESULTS_CLIP_LABEL)) {
            return (Bundle) clipData.getItemAt(0).getIntent().getExtras().getParcelable(EXTRA_RESULTS_DATA);
        }
        return null;
    }

    public static void addResultsToIntent(RemoteInput[] remoteInputs, Intent intent, Bundle results) {
        Bundle resultsBundle = new Bundle();
        int length = remoteInputs.length;
        for (int i = 0; i < length; i += FLAG_ALLOW_FREE_FORM_INPUT) {
            RemoteInput remoteInput = remoteInputs[i];
            Object result = results.get(remoteInput.getResultKey());
            if (result instanceof CharSequence) {
                resultsBundle.putCharSequence(remoteInput.getResultKey(), (CharSequence) result);
            }
        }
        Intent clipIntent = new Intent();
        clipIntent.putExtra(EXTRA_RESULTS_DATA, resultsBundle);
        intent.setClipData(ClipData.newIntent(RESULTS_CLIP_LABEL, clipIntent));
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
    }
}
