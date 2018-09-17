package android.view.textclassifier;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;
import com.android.internal.util.Preconditions;
import java.util.List;

public final class TextClassification {
    static final TextClassification EMPTY = new Builder().build();
    private final List<String> mEntities;
    private final EntityConfidence<String> mEntityConfidence;
    private final Drawable mIcon;
    private final Intent mIntent;
    private final String mLabel;
    private int mLogType;
    private final OnClickListener mOnClickListener;
    private final String mText;

    public static final class Builder {
        private final EntityConfidence<String> mEntityConfidence = new EntityConfidence();
        private Drawable mIcon;
        private Intent mIntent;
        private String mLabel;
        private int mLogType;
        private OnClickListener mOnClickListener;
        private String mText;

        public Builder setText(String text) {
            this.mText = (String) Preconditions.checkNotNull(text);
            return this;
        }

        public Builder setEntityType(String type, float confidenceScore) {
            this.mEntityConfidence.setEntityType(type, confidenceScore);
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setLabel(String label) {
            this.mLabel = label;
            return this;
        }

        public Builder setIntent(Intent intent) {
            this.mIntent = intent;
            return this;
        }

        public Builder setLogType(int type) {
            this.mLogType = type;
            return this;
        }

        public Builder setOnClickListener(OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
            return this;
        }

        public TextClassification build() {
            return new TextClassification(this.mText, this.mIcon, this.mLabel, this.mIntent, this.mOnClickListener, this.mEntityConfidence, this.mLogType, null);
        }
    }

    /* synthetic */ TextClassification(String text, Drawable icon, String label, Intent intent, OnClickListener onClickListener, EntityConfidence entityConfidence, int logType, TextClassification -this7) {
        this(text, icon, label, intent, onClickListener, entityConfidence, logType);
    }

    private TextClassification(String text, Drawable icon, String label, Intent intent, OnClickListener onClickListener, EntityConfidence<String> entityConfidence, int logType) {
        this.mText = text;
        this.mIcon = icon;
        this.mLabel = label;
        this.mIntent = intent;
        this.mOnClickListener = onClickListener;
        this.mEntityConfidence = new EntityConfidence(entityConfidence);
        this.mEntities = this.mEntityConfidence.getEntities();
        this.mLogType = logType;
    }

    public String getText() {
        return this.mText;
    }

    public int getEntityCount() {
        return this.mEntities.size();
    }

    public String getEntity(int index) {
        return (String) this.mEntities.get(index);
    }

    public float getConfidenceScore(String entity) {
        return this.mEntityConfidence.getConfidenceScore(entity);
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public OnClickListener getOnClickListener() {
        return this.mOnClickListener;
    }

    public int getLogType() {
        return this.mLogType;
    }

    public String toString() {
        return String.format("TextClassification {text=%s, entities=%s, label=%s, intent=%s}", new Object[]{this.mText, this.mEntityConfidence, this.mLabel, this.mIntent});
    }

    public static OnClickListener createStartActivityOnClickListener(Context context, Intent intent) {
        boolean z;
        boolean z2 = true;
        if (context != null) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
        if (intent == null) {
            z2 = false;
        }
        Preconditions.checkArgument(z2);
        return new -$Lambda$mxr44OLodDKdoE5ddAZvMdsFssQ(context, intent);
    }
}
