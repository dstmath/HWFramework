package android.view.textclassifier;

import com.google.android.textclassifier.AnnotatorModel;
import java.util.function.Function;

/* renamed from: android.view.textclassifier.-$$Lambda$NxwbyZSxofZ4Z5SQhfXmtLQ1nxk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NxwbyZSxofZ4Z5SQhfXmtLQ1nxk implements Function {
    public static final /* synthetic */ $$Lambda$NxwbyZSxofZ4Z5SQhfXmtLQ1nxk INSTANCE = new $$Lambda$NxwbyZSxofZ4Z5SQhfXmtLQ1nxk();

    private /* synthetic */ $$Lambda$NxwbyZSxofZ4Z5SQhfXmtLQ1nxk() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return AnnotatorModel.getLocales(((Integer) obj).intValue());
    }
}
