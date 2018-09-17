package android.view.textclassifier;

import android.util.LogException;
import com.android.internal.util.Preconditions;
import java.util.List;

public final class TextSelection {
    private final int mEndIndex;
    private final List<String> mEntities;
    private final EntityConfidence<String> mEntityConfidence;
    private final String mLogSource;
    private final int mStartIndex;

    public static final class Builder {
        private final int mEndIndex;
        private final EntityConfidence<String> mEntityConfidence = new EntityConfidence();
        private String mLogSource = LogException.NO_VALUE;
        private final int mStartIndex;

        public Builder(int startIndex, int endIndex) {
            boolean z;
            boolean z2 = true;
            if (startIndex >= 0) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkArgument(z);
            if (endIndex <= startIndex) {
                z2 = false;
            }
            Preconditions.checkArgument(z2);
            this.mStartIndex = startIndex;
            this.mEndIndex = endIndex;
        }

        public Builder setEntityType(String type, float confidenceScore) {
            this.mEntityConfidence.setEntityType(type, confidenceScore);
            return this;
        }

        Builder setLogSource(String logSource) {
            this.mLogSource = (String) Preconditions.checkNotNull(logSource);
            return this;
        }

        public TextSelection build() {
            return new TextSelection(this.mStartIndex, this.mEndIndex, this.mEntityConfidence, this.mLogSource, null);
        }
    }

    /* synthetic */ TextSelection(int startIndex, int endIndex, EntityConfidence entityConfidence, String logSource, TextSelection -this4) {
        this(startIndex, endIndex, entityConfidence, logSource);
    }

    private TextSelection(int startIndex, int endIndex, EntityConfidence<String> entityConfidence, String logSource) {
        this.mStartIndex = startIndex;
        this.mEndIndex = endIndex;
        this.mEntityConfidence = new EntityConfidence(entityConfidence);
        this.mEntities = this.mEntityConfidence.getEntities();
        this.mLogSource = logSource;
    }

    public int getSelectionStartIndex() {
        return this.mStartIndex;
    }

    public int getSelectionEndIndex() {
        return this.mEndIndex;
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

    public String getSourceClassifier() {
        return this.mLogSource;
    }

    public String toString() {
        return String.format("TextSelection {%d, %d, %s}", new Object[]{Integer.valueOf(this.mStartIndex), Integer.valueOf(this.mEndIndex), this.mEntityConfidence});
    }
}
