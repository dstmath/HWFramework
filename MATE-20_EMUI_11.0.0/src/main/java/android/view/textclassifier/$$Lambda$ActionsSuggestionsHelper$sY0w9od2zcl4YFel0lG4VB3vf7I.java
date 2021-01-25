package android.view.textclassifier;

import android.content.pm.ResolveInfo;
import android.view.textclassifier.intent.LabeledIntent;

/* renamed from: android.view.textclassifier.-$$Lambda$ActionsSuggestionsHelper$sY0w9od2zcl4YFel0lG4VB3vf7I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActionsSuggestionsHelper$sY0w9od2zcl4YFel0lG4VB3vf7I implements LabeledIntent.TitleChooser {
    public static final /* synthetic */ $$Lambda$ActionsSuggestionsHelper$sY0w9od2zcl4YFel0lG4VB3vf7I INSTANCE = new $$Lambda$ActionsSuggestionsHelper$sY0w9od2zcl4YFel0lG4VB3vf7I();

    private /* synthetic */ $$Lambda$ActionsSuggestionsHelper$sY0w9od2zcl4YFel0lG4VB3vf7I() {
    }

    @Override // android.view.textclassifier.intent.LabeledIntent.TitleChooser
    public final CharSequence chooseTitle(LabeledIntent labeledIntent, ResolveInfo resolveInfo) {
        return ActionsSuggestionsHelper.lambda$createTitleChooser$1(labeledIntent, resolveInfo);
    }
}
