package android.view.textclassifier;

import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannedString;
import android.util.ArrayMap;
import android.view.View;
import android.view.textclassifier.TextClassifier;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.google.android.textclassifier.AnnotatorModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TextClassification implements Parcelable {
    public static final Parcelable.Creator<TextClassification> CREATOR = new Parcelable.Creator<TextClassification>() {
        /* class android.view.textclassifier.TextClassification.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TextClassification createFromParcel(Parcel in) {
            return new TextClassification(in);
        }

        @Override // android.os.Parcelable.Creator
        public TextClassification[] newArray(int size) {
            return new TextClassification[size];
        }
    };
    public static final TextClassification EMPTY = new Builder().build();
    private static final String LOG_TAG = "TextClassification";
    private static final int MAX_LEGACY_ICON_SIZE = 192;
    private final List<RemoteAction> mActions;
    private final EntityConfidence mEntityConfidence;
    private final Bundle mExtras;
    private final String mId;
    private final Drawable mLegacyIcon;
    private final Intent mLegacyIntent;
    private final String mLegacyLabel;
    private final View.OnClickListener mLegacyOnClickListener;
    private final String mText;

    @Retention(RetentionPolicy.SOURCE)
    private @interface IntentType {
        public static final int ACTIVITY = 0;
        public static final int SERVICE = 1;
        public static final int UNSUPPORTED = -1;
    }

    private TextClassification(String text, Drawable legacyIcon, String legacyLabel, Intent legacyIntent, View.OnClickListener legacyOnClickListener, List<RemoteAction> actions, EntityConfidence entityConfidence, String id, Bundle extras) {
        this.mText = text;
        this.mLegacyIcon = legacyIcon;
        this.mLegacyLabel = legacyLabel;
        this.mLegacyIntent = legacyIntent;
        this.mLegacyOnClickListener = legacyOnClickListener;
        this.mActions = Collections.unmodifiableList(actions);
        this.mEntityConfidence = (EntityConfidence) Preconditions.checkNotNull(entityConfidence);
        this.mId = id;
        this.mExtras = extras;
    }

    public String getText() {
        return this.mText;
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

    public List<RemoteAction> getActions() {
        return this.mActions;
    }

    @Deprecated
    public Drawable getIcon() {
        return this.mLegacyIcon;
    }

    @Deprecated
    public CharSequence getLabel() {
        return this.mLegacyLabel;
    }

    @Deprecated
    public Intent getIntent() {
        return this.mLegacyIntent;
    }

    public View.OnClickListener getOnClickListener() {
        return this.mLegacyOnClickListener;
    }

    public String getId() {
        return this.mId;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public String toString() {
        return String.format(Locale.US, "TextClassification {text=%s, entities=%s, actions=%s, id=%s, extras=%s}", this.mText, this.mEntityConfidence, this.mActions, this.mId, this.mExtras);
    }

    public static View.OnClickListener createIntentOnClickListener(PendingIntent intent) {
        Preconditions.checkNotNull(intent);
        return new View.OnClickListener() {
            /* class android.view.textclassifier.$$Lambda$TextClassification$ysasaE5ZkXkkzjVWIJ06GTV92g */

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                TextClassification.lambda$createIntentOnClickListener$0(PendingIntent.this, view);
            }
        };
    }

    static /* synthetic */ void lambda$createIntentOnClickListener$0(PendingIntent intent, View v) {
        try {
            intent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.e(LOG_TAG, "Error sending PendingIntent", e);
        }
    }

    public static PendingIntent createPendingIntent(Context context, Intent intent, int requestCode) {
        return PendingIntent.getActivity(context, requestCode, intent, 134217728);
    }

    public static final class Builder {
        private final ArrayList<Intent> mActionIntents = new ArrayList<>();
        private List<RemoteAction> mActions = new ArrayList();
        private final Map<String, AnnotatorModel.ClassificationResult> mClassificationResults = new ArrayMap();
        private Bundle mExtras;
        private Bundle mForeignLanguageExtra;
        private String mId;
        private Drawable mLegacyIcon;
        private Intent mLegacyIntent;
        private String mLegacyLabel;
        private View.OnClickListener mLegacyOnClickListener;
        private String mText;
        private final Map<String, Float> mTypeScoreMap = new ArrayMap();

        public Builder setText(String text) {
            this.mText = text;
            return this;
        }

        public Builder setEntityType(String type, float confidenceScore) {
            setEntityType(type, confidenceScore, null);
            return this;
        }

        public Builder setEntityType(AnnotatorModel.ClassificationResult classificationResult) {
            setEntityType(classificationResult.getCollection(), classificationResult.getScore(), classificationResult);
            return this;
        }

        private Builder setEntityType(String type, float confidenceScore, AnnotatorModel.ClassificationResult classificationResult) {
            this.mTypeScoreMap.put(type, Float.valueOf(confidenceScore));
            this.mClassificationResults.put(type, classificationResult);
            return this;
        }

        public Builder addAction(RemoteAction action) {
            return addAction(action, null);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public Builder addAction(RemoteAction action, Intent intent) {
            Preconditions.checkArgument(action != null);
            this.mActions.add(action);
            this.mActionIntents.add(intent);
            return this;
        }

        @Deprecated
        public Builder setIcon(Drawable icon) {
            this.mLegacyIcon = icon;
            return this;
        }

        @Deprecated
        public Builder setLabel(String label) {
            this.mLegacyLabel = label;
            return this;
        }

        @Deprecated
        public Builder setIntent(Intent intent) {
            this.mLegacyIntent = intent;
            return this;
        }

        @Deprecated
        public Builder setOnClickListener(View.OnClickListener onClickListener) {
            this.mLegacyOnClickListener = onClickListener;
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

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public Builder setForeignLanguageExtra(Bundle extra) {
            this.mForeignLanguageExtra = extra;
            return this;
        }

        public TextClassification build() {
            EntityConfidence entityConfidence = new EntityConfidence(this.mTypeScoreMap);
            return new TextClassification(this.mText, this.mLegacyIcon, this.mLegacyLabel, this.mLegacyIntent, this.mLegacyOnClickListener, this.mActions, entityConfidence, this.mId, buildExtras(entityConfidence));
        }

        private Bundle buildExtras(EntityConfidence entityConfidence) {
            Bundle extras = this.mExtras;
            if (extras == null) {
                extras = new Bundle();
            }
            if (this.mActionIntents.stream().anyMatch($$Lambda$L_UQMPjXwBN0ch4zL2dD82nf9RI.INSTANCE)) {
                ExtrasUtils.putActionsIntents(extras, this.mActionIntents);
            }
            Bundle bundle = this.mForeignLanguageExtra;
            if (bundle != null) {
                ExtrasUtils.putForeignLanguageExtra(extras, bundle);
            }
            List<String> sortedTypes = entityConfidence.getEntities();
            ArrayList<AnnotatorModel.ClassificationResult> sortedEntities = new ArrayList<>();
            for (String type : sortedTypes) {
                sortedEntities.add(this.mClassificationResults.get(type));
            }
            ExtrasUtils.putEntities(extras, (AnnotatorModel.ClassificationResult[]) sortedEntities.toArray(new AnnotatorModel.ClassificationResult[0]));
            return extras.isEmpty() ? Bundle.EMPTY : extras;
        }
    }

    public static final class Request implements Parcelable {
        public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
            /* class android.view.textclassifier.TextClassification.Request.AnonymousClass1 */

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
        private final int mEndIndex;
        private final Bundle mExtras;
        private final ZonedDateTime mReferenceTime;
        private final int mStartIndex;
        private final CharSequence mText;
        private int mUserId;

        private Request(CharSequence text, int startIndex, int endIndex, LocaleList defaultLocales, ZonedDateTime referenceTime, Bundle extras) {
            this.mUserId = -10000;
            this.mText = text;
            this.mStartIndex = startIndex;
            this.mEndIndex = endIndex;
            this.mDefaultLocales = defaultLocales;
            this.mReferenceTime = referenceTime;
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

        public LocaleList getDefaultLocales() {
            return this.mDefaultLocales;
        }

        public ZonedDateTime getReferenceTime() {
            return this.mReferenceTime;
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
            private final int mEndIndex;
            private Bundle mExtras;
            private ZonedDateTime mReferenceTime;
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

            public Builder setReferenceTime(ZonedDateTime referenceTime) {
                this.mReferenceTime = referenceTime;
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
                ZonedDateTime zonedDateTime = this.mReferenceTime;
                Bundle bundle = this.mExtras;
                if (bundle == null) {
                    bundle = Bundle.EMPTY;
                }
                return new Request(spannedString, i, i2, localeList, zonedDateTime, bundle);
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
            ZonedDateTime zonedDateTime = this.mReferenceTime;
            dest.writeString(zonedDateTime == null ? null : zonedDateTime.toString());
            dest.writeString(this.mCallingPackageName);
            dest.writeInt(this.mUserId);
            dest.writeBundle(this.mExtras);
        }

        /* access modifiers changed from: private */
        public static Request readFromParcel(Parcel in) {
            CharSequence text = in.readCharSequence();
            int startIndex = in.readInt();
            int endIndex = in.readInt();
            ZonedDateTime referenceTime = null;
            LocaleList defaultLocales = (LocaleList) in.readParcelable(null);
            String referenceTimeString = in.readString();
            if (referenceTimeString != null) {
                referenceTime = ZonedDateTime.parse(referenceTimeString);
            }
            String callingPackageName = in.readString();
            int userId = in.readInt();
            Request request = new Request(text, startIndex, endIndex, defaultLocales, referenceTime, in.readBundle());
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
        dest.writeString(this.mText);
        dest.writeTypedList(this.mActions);
        this.mEntityConfidence.writeToParcel(dest, flags);
        dest.writeString(this.mId);
        dest.writeBundle(this.mExtras);
    }

    private TextClassification(Parcel in) {
        this.mText = in.readString();
        this.mActions = in.createTypedArrayList(RemoteAction.CREATOR);
        if (!this.mActions.isEmpty()) {
            RemoteAction action = this.mActions.get(0);
            this.mLegacyIcon = maybeLoadDrawable(action.getIcon());
            this.mLegacyLabel = action.getTitle().toString();
            this.mLegacyOnClickListener = createIntentOnClickListener(this.mActions.get(0).getActionIntent());
        } else {
            this.mLegacyIcon = null;
            this.mLegacyLabel = null;
            this.mLegacyOnClickListener = null;
        }
        this.mLegacyIntent = null;
        this.mEntityConfidence = EntityConfidence.CREATOR.createFromParcel(in);
        this.mId = in.readString();
        this.mExtras = in.readBundle();
    }

    private static Drawable maybeLoadDrawable(Icon icon) {
        if (icon == null) {
            return null;
        }
        int type = icon.getType();
        if (type == 1) {
            return new BitmapDrawable(Resources.getSystem(), icon.getBitmap());
        }
        if (type == 3) {
            return new BitmapDrawable(Resources.getSystem(), BitmapFactory.decodeByteArray(icon.getDataBytes(), icon.getDataOffset(), icon.getDataLength()));
        }
        if (type != 5) {
            return null;
        }
        return new AdaptiveIconDrawable((Drawable) null, new BitmapDrawable(Resources.getSystem(), icon.getBitmap()));
    }
}
