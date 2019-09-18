package android.view.textclassifier;

import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.view.textclassifier.TextClassifier;
import com.android.internal.util.Preconditions;
import java.util.Locale;
import java.util.Map;

public final class TextSelection implements Parcelable {
    public static final Parcelable.Creator<TextSelection> CREATOR = new Parcelable.Creator<TextSelection>() {
        public TextSelection createFromParcel(Parcel in) {
            return new TextSelection(in);
        }

        public TextSelection[] newArray(int size) {
            return new TextSelection[size];
        }
    };
    private final int mEndIndex;
    private final EntityConfidence mEntityConfidence;
    private final String mId;
    private final int mStartIndex;

    public static final class Builder {
        private final int mEndIndex;
        private final Map<String, Float> mEntityConfidence = new ArrayMap();
        private String mId;
        private final int mStartIndex;

        public Builder(int startIndex, int endIndex) {
            boolean z = false;
            Preconditions.checkArgument(startIndex >= 0);
            Preconditions.checkArgument(endIndex > startIndex ? true : z);
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

        public TextSelection build() {
            TextSelection textSelection = new TextSelection(this.mStartIndex, this.mEndIndex, this.mEntityConfidence, this.mId);
            return textSelection;
        }
    }

    public static final class Options {
        private boolean mDarkLaunchAllowed;
        private LocaleList mDefaultLocales;
        private final Request mRequest;
        private final TextClassificationSessionId mSessionId;

        public Options() {
            this(null, null);
        }

        private Options(TextClassificationSessionId sessionId, Request request) {
            this.mSessionId = sessionId;
            this.mRequest = request;
        }

        public static Options from(TextClassificationSessionId sessionId, Request request) {
            Options options = new Options(sessionId, request);
            options.setDefaultLocales(request.getDefaultLocales());
            return options;
        }

        public Options setDefaultLocales(LocaleList defaultLocales) {
            this.mDefaultLocales = defaultLocales;
            return this;
        }

        public LocaleList getDefaultLocales() {
            return this.mDefaultLocales;
        }

        public Request getRequest() {
            return this.mRequest;
        }

        public TextClassificationSessionId getSessionId() {
            return this.mSessionId;
        }
    }

    public static final class Request implements Parcelable {
        public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
            public Request createFromParcel(Parcel in) {
                return new Request(in);
            }

            public Request[] newArray(int size) {
                return new Request[size];
            }
        };
        private final boolean mDarkLaunchAllowed;
        private final LocaleList mDefaultLocales;
        private final int mEndIndex;
        private final int mStartIndex;
        private final CharSequence mText;

        public static final class Builder {
            private boolean mDarkLaunchAllowed;
            private LocaleList mDefaultLocales;
            private final int mEndIndex;
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

            public Request build() {
                Request request = new Request(this.mText, this.mStartIndex, this.mEndIndex, this.mDefaultLocales, this.mDarkLaunchAllowed);
                return request;
            }
        }

        private Request(CharSequence text, int startIndex, int endIndex, LocaleList defaultLocales, boolean darkLaunchAllowed) {
            this.mText = text;
            this.mStartIndex = startIndex;
            this.mEndIndex = endIndex;
            this.mDefaultLocales = defaultLocales;
            this.mDarkLaunchAllowed = darkLaunchAllowed;
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

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mText.toString());
            dest.writeInt(this.mStartIndex);
            dest.writeInt(this.mEndIndex);
            dest.writeInt(this.mDefaultLocales != null ? 1 : 0);
            if (this.mDefaultLocales != null) {
                this.mDefaultLocales.writeToParcel(dest, flags);
            }
        }

        private Request(Parcel in) {
            this.mText = in.readString();
            this.mStartIndex = in.readInt();
            this.mEndIndex = in.readInt();
            this.mDefaultLocales = in.readInt() == 0 ? null : LocaleList.CREATOR.createFromParcel(in);
            this.mDarkLaunchAllowed = false;
        }
    }

    private TextSelection(int startIndex, int endIndex, Map<String, Float> entityConfidence, String id) {
        this.mStartIndex = startIndex;
        this.mEndIndex = endIndex;
        this.mEntityConfidence = new EntityConfidence(entityConfidence);
        this.mId = id;
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

    public String toString() {
        return String.format(Locale.US, "TextSelection {id=%s, startIndex=%d, endIndex=%d, entities=%s}", new Object[]{this.mId, Integer.valueOf(this.mStartIndex), Integer.valueOf(this.mEndIndex), this.mEntityConfidence});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStartIndex);
        dest.writeInt(this.mEndIndex);
        this.mEntityConfidence.writeToParcel(dest, flags);
        dest.writeString(this.mId);
    }

    private TextSelection(Parcel in) {
        this.mStartIndex = in.readInt();
        this.mEndIndex = in.readInt();
        this.mEntityConfidence = EntityConfidence.CREATOR.createFromParcel(in);
        this.mId = in.readString();
    }
}
