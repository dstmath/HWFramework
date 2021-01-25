package android.view.textclassifier.intent;

import android.content.Context;
import android.view.textclassifier.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.google.android.textclassifier.AnnotatorModel;
import com.google.android.textclassifier.RemoteActionTemplate;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class TemplateClassificationIntentFactory implements ClassificationIntentFactory {
    private static final String TAG = "androidtc";
    private final ClassificationIntentFactory mFallback;
    private final TemplateIntentFactory mTemplateIntentFactory;

    public TemplateClassificationIntentFactory(TemplateIntentFactory templateIntentFactory, ClassificationIntentFactory fallback) {
        this.mTemplateIntentFactory = (TemplateIntentFactory) Preconditions.checkNotNull(templateIntentFactory);
        this.mFallback = (ClassificationIntentFactory) Preconditions.checkNotNull(fallback);
    }

    @Override // android.view.textclassifier.intent.ClassificationIntentFactory
    public List<LabeledIntent> create(Context context, String text, boolean foreignText, Instant referenceTime, AnnotatorModel.ClassificationResult classification) {
        if (classification == null) {
            return Collections.emptyList();
        }
        RemoteActionTemplate[] remoteActionTemplates = classification.getRemoteActionTemplates();
        if (remoteActionTemplates == null) {
            Log.w("androidtc", "RemoteActionTemplate is missing, fallback to LegacyClassificationIntentFactory.");
            return this.mFallback.create(context, text, foreignText, referenceTime, classification);
        }
        List<LabeledIntent> labeledIntents = this.mTemplateIntentFactory.create(remoteActionTemplates);
        if (foreignText) {
            ClassificationIntentFactory.insertTranslateAction(labeledIntents, context, text.trim());
        }
        return labeledIntents;
    }
}
