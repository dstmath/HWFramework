package android.view.textclassifier;

import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.util.Locale;
import java.util.Map;

public final class TextLanguage implements Parcelable {
    public static final Parcelable.Creator<TextLanguage> CREATOR = new Parcelable.Creator<TextLanguage>() {
        /* class android.view.textclassifier.TextLanguage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TextLanguage createFromParcel(Parcel in) {
            return TextLanguage.readFromParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public TextLanguage[] newArray(int size) {
            return new TextLanguage[size];
        }
    };
    static final TextLanguage EMPTY = new Builder().build();
    private final Bundle mBundle;
    private final EntityConfidence mEntityConfidence;
    private final String mId;

    private TextLanguage(String id, EntityConfidence entityConfidence, Bundle bundle) {
        this.mId = id;
        this.mEntityConfidence = entityConfidence;
        this.mBundle = bundle;
    }

    public String getId() {
        return this.mId;
    }

    public int getLocaleHypothesisCount() {
        return this.mEntityConfidence.getEntities().size();
    }

    public ULocale getLocale(int index) {
        return ULocale.forLanguageTag(this.mEntityConfidence.getEntities().get(index));
    }

    public float getConfidenceScore(ULocale locale) {
        return this.mEntityConfidence.getConfidenceScore(locale.toLanguageTag());
    }

    public Bundle getExtras() {
        return this.mBundle;
    }

    public String toString() {
        return String.format(Locale.US, "TextLanguage {id=%s, locales=%s, bundle=%s}", this.mId, this.mEntityConfidence, this.mBundle);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        this.mEntityConfidence.writeToParcel(dest, flags);
        dest.writeBundle(this.mBundle);
    }

    /* access modifiers changed from: private */
    public static TextLanguage readFromParcel(Parcel in) {
        return new TextLanguage(in.readString(), EntityConfidence.CREATOR.createFromParcel(in), in.readBundle());
    }

    public static final class Builder {
        private Bundle mBundle;
        private final Map<String, Float> mEntityConfidenceMap = new ArrayMap();
        private String mId;

        public Builder putLocale(ULocale locale, float confidenceScore) {
            Preconditions.checkNotNull(locale);
            this.mEntityConfidenceMap.put(locale.toLanguageTag(), Float.valueOf(confidenceScore));
            return this;
        }

        public Builder setId(String id) {
            this.mId = id;
            return this;
        }

        public Builder setExtras(Bundle bundle) {
            this.mBundle = (Bundle) Preconditions.checkNotNull(bundle);
            return this;
        }

        public TextLanguage build() {
            Bundle bundle = this.mBundle;
            if (bundle == null) {
                bundle = Bundle.EMPTY;
            }
            this.mBundle = bundle;
            return new TextLanguage(this.mId, new EntityConfidence(this.mEntityConfidenceMap), this.mBundle);
        }
    }

    public static final class Request implements Parcelable {
        public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
            /* class android.view.textclassifier.TextLanguage.Request.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Request createFromParcel(Parcel in) {
                return Request.readFromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public Request[] newArray(int size) {
                return new Request[size];
            }
        };
        private String mCallingPackageName;
        private final Bundle mExtra;
        private final CharSequence mText;
        private int mUserId;

        private Request(CharSequence text, Bundle bundle) {
            this.mUserId = -10000;
            this.mText = text;
            this.mExtra = bundle;
        }

        public CharSequence getText() {
            return this.mText;
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void setCallingPackageName(String callingPackageName) {
            this.mCallingPackageName = callingPackageName;
        }

        public String getCallingPackageName() {
            return this.mCallingPackageName;
        }

        /* access modifiers changed from: package-private */
        public void setUserId(int userId) {
            this.mUserId = userId;
        }

        public int getUserId() {
            return this.mUserId;
        }

        public Bundle getExtras() {
            return this.mExtra;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeCharSequence(this.mText);
            dest.writeString(this.mCallingPackageName);
            dest.writeInt(this.mUserId);
            dest.writeBundle(this.mExtra);
        }

        /* access modifiers changed from: private */
        public static Request readFromParcel(Parcel in) {
            CharSequence text = in.readCharSequence();
            String callingPackageName = in.readString();
            int userId = in.readInt();
            Request request = new Request(text, in.readBundle());
            request.setCallingPackageName(callingPackageName);
            request.setUserId(userId);
            return request;
        }

        public static final class Builder {
            private Bundle mBundle;
            private final CharSequence mText;

            public Builder(CharSequence text) {
                this.mText = (CharSequence) Preconditions.checkNotNull(text);
            }

            public Builder setExtras(Bundle bundle) {
                this.mBundle = (Bundle) Preconditions.checkNotNull(bundle);
                return this;
            }

            public Request build() {
                String charSequence = this.mText.toString();
                Bundle bundle = this.mBundle;
                if (bundle == null) {
                    bundle = Bundle.EMPTY;
                }
                return new Request(charSequence, bundle);
            }
        }
    }
}
