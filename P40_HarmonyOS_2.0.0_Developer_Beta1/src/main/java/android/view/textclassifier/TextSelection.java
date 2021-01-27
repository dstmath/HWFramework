package android.view.textclassifier;

import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannedString;
import android.util.ArrayMap;
import android.view.textclassifier.TextClassifier;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.util.Locale;
import java.util.Map;

public final class TextSelection implements Parcelable {
    public static final Parcelable.Creator<TextSelection> CREATOR = new Parcelable.Creator<TextSelection>() {
        /* class android.view.textclassifier.TextSelection.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TextSelection createFromParcel(Parcel in) {
            return new TextSelection(in);
        }

        @Override // android.os.Parcelable.Creator
        public TextSelection[] newArray(int size) {
            return new TextSelection[size];
        }
    };
    private final int mEndIndex;
    private final EntityConfidence mEntityConfidence;
    private final Bundle mExtras;
    private final String mId;
    private final int mStartIndex;

    private TextSelection(int startIndex, int endIndex, Map<String, Float> entityConfidence, String id, Bundle extras) {
        this.mStartIndex = startIndex;
        this.mEndIndex = endIndex;
        this.mEntityConfidence = new EntityConfidence(entityConfidence);
        this.mId = id;
        this.mExtras = extras;
    }

    public int getSelectionStartIndex() {
        return this.mStartIndex;
    }

    public int getSelectionEndIndex() {
        return this.mEndIndex;
    }

    public int getEntityCount() {
        return this.mEntityConfidence.getEntities().size();
    }

    public String getEntity(int index) {
        return this.mEntityConfidence.getEntities().get(index);
    }

    public float getConfidenceScore(String entity) {
        return this.mEntityConfidence.getConfidenceScore(entity);
    }

    public String getId() {
        return this.mId;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public String toString() {
        return String.format(Locale.US, "TextSelection {id=%s, startIndex=%d, endIndex=%d, entities=%s}", this.mId, Integer.valueOf(this.mStartIndex), Integer.valueOf(this.mEndIndex), this.mEntityConfidence);
    }

    public static final class Builder {
        private final int mEndIndex;
        private final Map<String, Float> mEntityConfidence = new ArrayMap();
        private Bundle mExtras;
        private String mId;
        private final int mStartIndex;

        public Builder(int startIndex, int endIndex) {
            boolean z = true;
            Preconditions.checkArgument(startIndex >= 0);
            Preconditions.checkArgument(endIndex <= startIndex ? false : z);
            this.mStartIndex = startIndex;
            this.mEndIndex = endIndex;
        }

        public Builder setEntityType(String type, float confidenceScore) {
            Preconditions.checkNotNull(type);
            this.mEntityConfidence.put(type, Float.valueOf(confidenceScore));
            return this;
        }

        public Builder setId(String id) {
            this.mId = id;
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public TextSelection build() {
            int i = this.mStartIndex;
            int i2 = this.mEndIndex;
            Map<String, Float> map = this.mEntityConfidence;
            String str = this.mId;
            Bundle bundle = this.mExtras;
            if (bundle == null) {
                bundle = Bundle.EMPTY;
            }
            return new TextSelection(i, i2, map, str, bundle);
        }
    }

    public static final class Request implements Parcelable {
        public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
            /* class android.view.textclassifier.TextSelection.Request.AnonymousClass1 */

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
        private final boolean mDarkLaunchAllowed;
        private final LocaleList mDefaultLocales;
        private final int mEndIndex;
        private final Bundle mExtras;
        private final int mStartIndex;
        private final CharSequence mText;
        private int mUserId;

        private Request(CharSequence text, int startIndex, int endIndex, LocaleList defaultLocales, boolean darkLaunchAllowed, Bundle extras) {
            this.mUserId = -10000;
            this.mText = text;
            this.mStartIndex = startIndex;
            this.mEndIndex = endIndex;
            this.mDefaultLocales = defaultLocales;
            this.mDarkLaunchAllowed = darkLaunchAllowed;
            this.mExtras = extras;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public int getStartIndex() {
            return this.mStartIndex;
        }

        public int getEndIndex() {
            return this.mEndIndex;
        }

        public boolean isDarkLaunchAllowed() {
            return this.mDarkLaunchAllowed;
        }

        public LocaleList getDefaultLocales() {
            return this.mDefaultLocales;
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
            return this.mExtras;
        }

        public static final class Builder {
            private boolean mDarkLaunchAllowed;
            private LocaleList mDefaultLocales;
            private final int mEndIndex;
            private Bundle mExtras;
            private final int mStartIndex;
            private final CharSequence mText;

            public Builder(CharSequence text, int startIndex, int endIndex) {
                TextClassifier.Utils.checkArgument(text, startIndex, endIndex);
                this.mText = text;
                this.mStartIndex = startIndex;
                this.mEndIndex = endIndex;
            }

            public Builder setDefaultLocales(LocaleList defaultLocales) {
                this.mDefaultLocales = defaultLocales;
                return this;
            }

            public Builder setDarkLaunchAllowed(boolean allowed) {
                this.mDarkLaunchAllowed = allowed;
                return this;
            }

            public Builder setExtras(Bundle extras) {
                this.mExtras = extras;
                return this;
            }

            public Request build() {
                SpannedString spannedString = new SpannedString(this.mText);
                int i = this.mStartIndex;
                int i2 = this.mEndIndex;
                LocaleList localeList = this.mDefaultLocales;
                boolean z = this.mDarkLaunchAllowed;
                Bundle bundle = this.mExtras;
                if (bundle == null) {
                    bundle = Bundle.EMPTY;
                }
                return new Request(spannedString, i, i2, localeList, z, bundle);
            }
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeCharSequence(this.mText);
            dest.writeInt(this.mStartIndex);
            dest.writeInt(this.mEndIndex);
            dest.writeParcelable(this.mDefaultLocales, flags);
            dest.writeString(this.mCallingPackageName);
            dest.writeInt(this.mUserId);
            dest.writeBundle(this.mExtras);
        }

        /* access modifiers changed from: private */
        public static Request readFromParcel(Parcel in) {
            String callingPackageName = in.readString();
            int userId = in.readInt();
            Request request = new Request(in.readCharSequence(), in.readInt(), in.readInt(), (LocaleList) in.readParcelable(null), false, in.readBundle());
            request.setCallingPackageName(callingPackageName);
            request.setUserId(userId);
            return request;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStartIndex);
        dest.writeInt(this.mEndIndex);
        this.mEntityConfidence.writeToParcel(dest, flags);
        dest.writeString(this.mId);
        dest.writeBundle(this.mExtras);
    }

    private TextSelection(Parcel in) {
        this.mStartIndex = in.readInt();
        this.mEndIndex = in.readInt();
        this.mEntityConfidence = EntityConfidence.CREATOR.createFromParcel(in);
        this.mId = in.readString();
        this.mExtras = in.readBundle();
    }
}
