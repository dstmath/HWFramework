package android.view.textclassifier;

import android.view.textclassifier.-$Lambda$YdZbAd6a5x_pMw8WtGLtYRkzJSM.AnonymousClass1;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class EntityConfidence<T> {
    private final Comparator<T> mEntityComparator;
    private final Map<T, Float> mEntityConfidence;

    /* synthetic */ int lambda$-android_view_textclassifier_EntityConfidence_1225(Object e1, Object e2) {
        float score1 = ((Float) this.mEntityConfidence.get(e1)).floatValue();
        float score2 = ((Float) this.mEntityConfidence.get(e2)).floatValue();
        if (score1 > score2) {
            return -1;
        }
        if (score1 < score2) {
            return 1;
        }
        return 0;
    }

    EntityConfidence() {
        this.mEntityConfidence = new HashMap();
        this.mEntityComparator = new -$Lambda$YdZbAd6a5x_pMw8WtGLtYRkzJSM(this);
    }

    EntityConfidence(EntityConfidence<T> source) {
        this.mEntityConfidence = new HashMap();
        this.mEntityComparator = new AnonymousClass1(this);
        Preconditions.checkNotNull(source);
        this.mEntityConfidence.putAll(source.mEntityConfidence);
    }

    public void setEntityType(T type, float confidenceScore) {
        Preconditions.checkNotNull(type);
        if (confidenceScore > 0.0f) {
            this.mEntityConfidence.put(type, Float.valueOf(Math.min(1.0f, confidenceScore)));
        } else {
            this.mEntityConfidence.remove(type);
        }
    }

    public List<T> getEntities() {
        List<T> entities = new ArrayList(this.mEntityConfidence.size());
        entities.addAll(this.mEntityConfidence.keySet());
        entities.sort(this.mEntityComparator);
        return Collections.unmodifiableList(entities);
    }

    public float getConfidenceScore(T entity) {
        if (this.mEntityConfidence.containsKey(entity)) {
            return ((Float) this.mEntityConfidence.get(entity)).floatValue();
        }
        return 0.0f;
    }

    public String toString() {
        return this.mEntityConfidence.toString();
    }
}
