package android.view.textclassifier;

import android.content.pm.ResolveInfo;
import android.view.textclassifier.intent.LabeledIntent;

/* renamed from: android.view.textclassifier.-$$Lambda$TextClassifierImpl$naj1VfHYH1Qfut8yLHu8DlsggQE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TextClassifierImpl$naj1VfHYH1Qfut8yLHu8DlsggQE implements LabeledIntent.TitleChooser {
    public static final /* synthetic */ $$Lambda$TextClassifierImpl$naj1VfHYH1Qfut8yLHu8DlsggQE INSTANCE = new $$Lambda$TextClassifierImpl$naj1VfHYH1Qfut8yLHu8DlsggQE();

    private /* synthetic */ $$Lambda$TextClassifierImpl$naj1VfHYH1Qfut8yLHu8DlsggQE() {
    }

    @Override // android.view.textclassifier.intent.LabeledIntent.TitleChooser
    public final CharSequence chooseTitle(LabeledIntent labeledIntent, ResolveInfo resolveInfo) {
        return labeledIntent.titleWithoutEntity;
    }
}
