package android.view.textclassifier;

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
        public TextLinks createFromParcel(Parcel in) {
            return new TextLinks(in);
        }

        public TextLinks[] newArray(int size) {
            return new TextLinks[size];
        }
    };
    public static final int STATUS_DIFFERENT_TEXT = 3;
    public static final int STATUS_LINKS_APPLIED = 0;
    public static final int STATUS_NO_LINKS_APPLIED = 2;
    public static final int STATUS_NO_LINKS_FOUND = 1;
    private final String mFullText;
    private final List<TextLink> mLinks;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ApplyStrategy {
    }

    public static final class Builder {
        private final String mFullText;
        private final ArrayList<TextLink> mLinks = new ArrayList<>();

        public Builder(String fullText) {
            this.mFullText = (String) Preconditions.checkNotNull(fullText);
        }

        public Builder addLink(int start, int end, Map<String, Float> entityScores) {
            this.mLinks.add(new TextLink(start, end, entityScores, null));
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder addLink(int start, int end, Map<String, Float> entityScores, URLSpan urlSpan) {
            this.mLinks.add(new TextLink(start, end, entityScores, urlSpan));
            return this;
        }

        public Builder clearTextLinks() {
            this.mLinks.clear();
            return this;
        }

        public TextLinks build() {
            return new TextLinks(this.mFullText, this.mLinks);
        }
    }

    public static final class Options {
        private int mApplyStrategy;
        private String mCallingPackageName;
        private LocaleList mDefaultLocales;
        private TextClassifier.EntityConfig mEntityConfig;
        private boolean mLegacyFallback;
        private final Request mRequest;
        private final TextClassificationSessionId mSessionId;
        private Function<TextLink, TextLinkSpan> mSpanFactory;

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
            options.setEntityConfig(request.getEntityConfig());
            return options;
        }

        public static Options fromLinkMask(int mask) {
            List<String> entitiesToFind = new ArrayList<>();
            if ((mask & 1) != 0) {
                entitiesToFind.add("url");
            }
            if ((mask & 2) != 0) {
                entitiesToFind.add("email");
            }
            if ((mask & 4) != 0) {
                entitiesToFind.add("phone");
            }
            if ((mask & 8) != 0) {
                entitiesToFind.add("address");
            }
            return new Options().setEntityConfig(TextClassifier.EntityConfig.createWithEntityList(entitiesToFind));
        }

        public Options setDefaultLocales(LocaleList defaultLocales) {
            this.mDefaultLocales = defaultLocales;
            return this;
        }

        public Options setEntityConfig(TextClassifier.EntityConfig entityConfig) {
            this.mEntityConfig = entityConfig;
            return this;
        }

        public Options setApplyStrategy(int applyStrategy) {
            checkValidApplyStrategy(applyStrategy);
            this.mApplyStrategy = applyStrategy;
            return this;
        }

        public Options setSpanFactory(Function<TextLink, TextLinkSpan> spanFactory) {
            this.mSpanFactory = spanFactory;
            return this;
        }

        public LocaleList getDefaultLocales() {
            return this.mDefaultLocales;
        }

        public TextClassifier.EntityConfig getEntityConfig() {
            return this.mEntityConfig;
        }

        public int getApplyStrategy() {
            return this.mApplyStrategy;
        }

        public Function<TextLink, TextLinkSpan> getSpanFactory() {
            return this.mSpanFactory;
        }

        public Request getRequest() {
            return this.mRequest;
        }

        public TextClassificationSessionId getSessionId() {
            return this.mSessionId;
        }

        private static void checkValidApplyStrategy(int applyStrategy) {
            if (applyStrategy != 0 && applyStrategy != 1) {
                throw new IllegalArgumentException("Invalid apply strategy. See TextLinks.ApplyStrategy for options.");
            }
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
        private String mCallingPackageName;
        private final LocaleList mDefaultLocales;
        private final TextClassifier.EntityConfig mEntityConfig;
        private final boolean mLegacyFallback;
        private final CharSequence mText;

        public static final class Builder {
            private String mCallingPackageName;
            private LocaleList mDefaultLocales;
            private TextClassifier.EntityConfig mEntityConfig;
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

            public Builder setCallingPackageName(String callingPackageName) {
                this.mCallingPackageName = callingPackageName;
                return this;
            }

            public Request build() {
                Request request = new Request(this.mText, this.mDefaultLocales, this.mEntityConfig, this.mLegacyFallback, this.mCallingPackageName);
                return request;
            }
        }

        private Request(CharSequence text, LocaleList defaultLocales, TextClassifier.EntityConfig entityConfig, boolean legacyFallback, String callingPackageName) {
            this.mText = text;
            this.mDefaultLocales = defaultLocales;
            this.mEntityConfig = entityConfig;
            this.mLegacyFallback = legacyFallback;
            this.mCallingPackageName = callingPackageName;
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

        /* access modifiers changed from: package-private */
        public void setCallingPackageName(String callingPackageName) {
            this.mCallingPackageName = callingPackageName;
        }

        public String getCallingPackageName() {
            return this.mCallingPackageName;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mText.toString());
            int i = 0;
            dest.writeInt(this.mDefaultLocales != null ? 1 : 0);
            if (this.mDefaultLocales != null) {
                this.mDefaultLocales.writeToParcel(dest, flags);
            }
            if (this.mEntityConfig != null) {
                i = 1;
            }
            dest.writeInt(i);
            if (this.mEntityConfig != null) {
                this.mEntityConfig.writeToParcel(dest, flags);
            }
            dest.writeString(this.mCallingPackageName);
        }

        private Request(Parcel in) {
            this.mText = in.readString();
            TextClassifier.EntityConfig entityConfig = null;
            this.mDefaultLocales = in.readInt() == 0 ? null : LocaleList.CREATOR.createFromParcel(in);
            this.mEntityConfig = in.readInt() != 0 ? TextClassifier.EntityConfig.CREATOR.createFromParcel(in) : entityConfig;
            this.mLegacyFallback = true;
            this.mCallingPackageName = in.readString();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }

    public static final class TextLink implements Parcelable {
        public static final Parcelable.Creator<TextLink> CREATOR = new Parcelable.Creator<TextLink>() {
            public TextLink createFromParcel(Parcel in) {
                return new TextLink(in);
            }

            public TextLink[] newArray(int size) {
                return new TextLink[size];
            }
        };
        private final int mEnd;
        private final EntityConfidence mEntityScores;
        private final int mStart;
        final URLSpan mUrlSpan;

        TextLink(int start, int end, Map<String, Float> entityScores, URLSpan urlSpan) {
            Preconditions.checkNotNull(entityScores);
            boolean z = true;
            Preconditions.checkArgument(!entityScores.isEmpty());
            Preconditions.checkArgument(start > end ? false : z);
            this.mStart = start;
            this.mEnd = end;
            this.mEntityScores = new EntityConfidence(entityScores);
            this.mUrlSpan = urlSpan;
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

        public String toString() {
            return String.format(Locale.US, "TextLink{start=%s, end=%s, entityScores=%s, urlSpan=%s}", new Object[]{Integer.valueOf(this.mStart), Integer.valueOf(this.mEnd), this.mEntityScores, this.mUrlSpan});
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.mEntityScores.writeToParcel(dest, flags);
            dest.writeInt(this.mStart);
            dest.writeInt(this.mEnd);
        }

        private TextLink(Parcel in) {
            this.mEntityScores = EntityConfidence.CREATOR.createFromParcel(in);
            this.mStart = in.readInt();
            this.mEnd = in.readInt();
            this.mUrlSpan = null;
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

    private TextLinks(String fullText, ArrayList<TextLink> links) {
        this.mFullText = fullText;
        this.mLinks = Collections.unmodifiableList(links);
    }

    public String getText() {
        return this.mFullText;
    }

    public Collection<TextLink> getLinks() {
        return this.mLinks;
    }

    public int apply(Spannable text, int applyStrategy, Function<TextLink, TextLinkSpan> spanFactory) {
        Preconditions.checkNotNull(text);
        return new TextLinksParams.Builder().setApplyStrategy(applyStrategy).setSpanFactory(spanFactory).build().apply(text, this);
    }

    public String toString() {
        return String.format(Locale.US, "TextLinks{fullText=%s, links=%s}", new Object[]{this.mFullText, this.mLinks});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFullText);
        dest.writeTypedList(this.mLinks);
    }

    private TextLinks(Parcel in) {
        this.mFullText = in.readString();
        this.mLinks = in.createTypedArrayList(TextLink.CREATOR);
    }
}
