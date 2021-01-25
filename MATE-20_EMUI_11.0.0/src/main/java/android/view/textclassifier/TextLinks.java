package android.view.textclassifier;

import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLinksParams;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class TextLinks implements Parcelable {
    public static final int APPLY_STRATEGY_IGNORE = 0;
    public static final int APPLY_STRATEGY_REPLACE = 1;
    public static final Parcelable.Creator<TextLinks> CREATOR = new Parcelable.Creator<TextLinks>() {
        /* class android.view.textclassifier.TextLinks.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TextLinks createFromParcel(Parcel in) {
            return new TextLinks(in);
        }

        @Override // android.os.Parcelable.Creator
        public TextLinks[] newArray(int size) {
            return new TextLinks[size];
        }
    };
    public static final int STATUS_DIFFERENT_TEXT = 3;
    public static final int STATUS_LINKS_APPLIED = 0;
    public static final int STATUS_NO_LINKS_APPLIED = 2;
    public static final int STATUS_NO_LINKS_FOUND = 1;
    public static final int STATUS_UNSUPPORTED_CHARACTER = 4;
    private final Bundle mExtras;
    private final String mFullText;
    private final List<TextLink> mLinks;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ApplyStrategy {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }

    private TextLinks(String fullText, ArrayList<TextLink> links, Bundle extras) {
        this.mFullText = fullText;
        this.mLinks = Collections.unmodifiableList(links);
        this.mExtras = extras;
    }

    public String getText() {
        return this.mFullText;
    }

    public Collection<TextLink> getLinks() {
        return this.mLinks;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public int apply(Spannable text, int applyStrategy, Function<TextLink, TextLinkSpan> spanFactory) {
        Preconditions.checkNotNull(text);
        return new TextLinksParams.Builder().setApplyStrategy(applyStrategy).setSpanFactory(spanFactory).build().apply(text, this);
    }

    public String toString() {
        return String.format(Locale.US, "TextLinks{fullText=%s, links=%s}", this.mFullText, this.mLinks);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFullText);
        dest.writeTypedList(this.mLinks);
        dest.writeBundle(this.mExtras);
    }

    private TextLinks(Parcel in) {
        this.mFullText = in.readString();
        this.mLinks = in.createTypedArrayList(TextLink.CREATOR);
        this.mExtras = in.readBundle();
    }

    public static final class TextLink implements Parcelable {
        public static final Parcelable.Creator<TextLink> CREATOR = new Parcelable.Creator<TextLink>() {
            /* class android.view.textclassifier.TextLinks.TextLink.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public TextLink createFromParcel(Parcel in) {
                return TextLink.readFromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public TextLink[] newArray(int size) {
                return new TextLink[size];
            }
        };
        private final int mEnd;
        private final EntityConfidence mEntityScores;
        private final Bundle mExtras;
        private final int mStart;
        private final URLSpan mUrlSpan;

        private TextLink(int start, int end, EntityConfidence entityConfidence, Bundle extras, URLSpan urlSpan) {
            Preconditions.checkNotNull(entityConfidence);
            boolean z = true;
            Preconditions.checkArgument(!entityConfidence.getEntities().isEmpty());
            Preconditions.checkArgument(start > end ? false : z);
            Preconditions.checkNotNull(extras);
            this.mStart = start;
            this.mEnd = end;
            this.mEntityScores = entityConfidence;
            this.mUrlSpan = urlSpan;
            this.mExtras = extras;
        }

        public int getStart() {
            return this.mStart;
        }

        public int getEnd() {
            return this.mEnd;
        }

        public int getEntityCount() {
            return this.mEntityScores.getEntities().size();
        }

        public String getEntity(int index) {
            return this.mEntityScores.getEntities().get(index);
        }

        public float getConfidenceScore(String entityType) {
            return this.mEntityScores.getConfidenceScore(entityType);
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        public String toString() {
            return String.format(Locale.US, "TextLink{start=%s, end=%s, entityScores=%s, urlSpan=%s}", Integer.valueOf(this.mStart), Integer.valueOf(this.mEnd), this.mEntityScores, this.mUrlSpan);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            this.mEntityScores.writeToParcel(dest, flags);
            dest.writeInt(this.mStart);
            dest.writeInt(this.mEnd);
            dest.writeBundle(this.mExtras);
        }

        /* access modifiers changed from: private */
        public static TextLink readFromParcel(Parcel in) {
            return new TextLink(in.readInt(), in.readInt(), EntityConfidence.CREATOR.createFromParcel(in), in.readBundle(), null);
        }
    }

    public static final class Request implements Parcelable {
        public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
            /* class android.view.textclassifier.TextLinks.Request.AnonymousClass1 */

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
        private final LocaleList mDefaultLocales;
        private final TextClassifier.EntityConfig mEntityConfig;
        private final Bundle mExtras;
        private final boolean mLegacyFallback;
        private final CharSequence mText;
        private int mUserId;

        private Request(CharSequence text, LocaleList defaultLocales, TextClassifier.EntityConfig entityConfig, boolean legacyFallback, Bundle extras) {
            this.mUserId = -10000;
            this.mText = text;
            this.mDefaultLocales = defaultLocales;
            this.mEntityConfig = entityConfig;
            this.mLegacyFallback = legacyFallback;
            this.mExtras = extras;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public LocaleList getDefaultLocales() {
            return this.mDefaultLocales;
        }

        public TextClassifier.EntityConfig getEntityConfig() {
            return this.mEntityConfig;
        }

        public boolean isLegacyFallback() {
            return this.mLegacyFallback;
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
            private LocaleList mDefaultLocales;
            private TextClassifier.EntityConfig mEntityConfig;
            private Bundle mExtras;
            private boolean mLegacyFallback = true;
            private final CharSequence mText;

            public Builder(CharSequence text) {
                this.mText = (CharSequence) Preconditions.checkNotNull(text);
            }

            public Builder setDefaultLocales(LocaleList defaultLocales) {
                this.mDefaultLocales = defaultLocales;
                return this;
            }

            public Builder setEntityConfig(TextClassifier.EntityConfig entityConfig) {
                this.mEntityConfig = entityConfig;
                return this;
            }

            public Builder setLegacyFallback(boolean legacyFallback) {
                this.mLegacyFallback = legacyFallback;
                return this;
            }

            public Builder setExtras(Bundle extras) {
                this.mExtras = extras;
                return this;
            }

            public Request build() {
                CharSequence charSequence = this.mText;
                LocaleList localeList = this.mDefaultLocales;
                TextClassifier.EntityConfig entityConfig = this.mEntityConfig;
                boolean z = this.mLegacyFallback;
                Bundle bundle = this.mExtras;
                if (bundle == null) {
                    bundle = Bundle.EMPTY;
                }
                return new Request(charSequence, localeList, entityConfig, z, bundle);
            }
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mText.toString());
            dest.writeParcelable(this.mDefaultLocales, flags);
            dest.writeParcelable(this.mEntityConfig, flags);
            dest.writeString(this.mCallingPackageName);
            dest.writeInt(this.mUserId);
            dest.writeBundle(this.mExtras);
        }

        /* access modifiers changed from: private */
        public static Request readFromParcel(Parcel in) {
            String callingPackageName = in.readString();
            int userId = in.readInt();
            Request request = new Request(in.readString(), (LocaleList) in.readParcelable(null), (TextClassifier.EntityConfig) in.readParcelable(null), true, in.readBundle());
            request.setCallingPackageName(callingPackageName);
            request.setUserId(userId);
            return request;
        }
    }

    public static class TextLinkSpan extends ClickableSpan {
        public static final int INVOCATION_METHOD_KEYBOARD = 1;
        public static final int INVOCATION_METHOD_TOUCH = 0;
        public static final int INVOCATION_METHOD_UNSPECIFIED = -1;
        private final TextLink mTextLink;

        @Retention(RetentionPolicy.SOURCE)
        public @interface InvocationMethod {
        }

        public TextLinkSpan(TextLink textLink) {
            this.mTextLink = textLink;
        }

        @Override // android.text.style.ClickableSpan
        public void onClick(View widget) {
            onClick(widget, -1);
        }

        public final void onClick(View widget, int invocationMethod) {
            if (widget instanceof TextView) {
                TextView textView = (TextView) widget;
                if (TextClassificationManager.getSettings(textView.getContext()).isSmartLinkifyEnabled()) {
                    if (invocationMethod != 0) {
                        textView.handleClick(this);
                    } else {
                        textView.requestActionMode(this);
                    }
                } else if (this.mTextLink.mUrlSpan != null) {
                    this.mTextLink.mUrlSpan.onClick(textView);
                } else {
                    textView.handleClick(this);
                }
            }
        }

        public final TextLink getTextLink() {
            return this.mTextLink;
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
        public final String getUrl() {
            if (this.mTextLink.mUrlSpan != null) {
                return this.mTextLink.mUrlSpan.getURL();
            }
            return null;
        }
    }

    public static final class Builder {
        private Bundle mExtras;
        private final String mFullText;
        private final ArrayList<TextLink> mLinks = new ArrayList<>();

        public Builder(String fullText) {
            this.mFullText = (String) Preconditions.checkNotNull(fullText);
        }

        public Builder addLink(int start, int end, Map<String, Float> entityScores) {
            return addLink(start, end, entityScores, Bundle.EMPTY, null);
        }

        public Builder addLink(int start, int end, Map<String, Float> entityScores, Bundle extras) {
            return addLink(start, end, entityScores, extras, null);
        }

        /* access modifiers changed from: package-private */
        public Builder addLink(int start, int end, Map<String, Float> entityScores, URLSpan urlSpan) {
            return addLink(start, end, entityScores, Bundle.EMPTY, urlSpan);
        }

        private Builder addLink(int start, int end, Map<String, Float> entityScores, Bundle extras, URLSpan urlSpan) {
            this.mLinks.add(new TextLink(start, end, new EntityConfidence(entityScores), extras, urlSpan));
            return this;
        }

        public Builder clearTextLinks() {
            this.mLinks.clear();
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public TextLinks build() {
            String str = this.mFullText;
            ArrayList<TextLink> arrayList = this.mLinks;
            Bundle bundle = this.mExtras;
            if (bundle == null) {
                bundle = Bundle.EMPTY;
            }
            return new TextLinks(str, arrayList, bundle);
        }
    }
}
