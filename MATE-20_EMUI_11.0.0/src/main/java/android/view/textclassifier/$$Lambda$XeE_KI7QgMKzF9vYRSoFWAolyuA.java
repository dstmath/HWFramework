package android.view.textclassifier;

import com.google.android.textclassifier.ActionsSuggestionsModel;
import java.util.function.Function;

/* renamed from: android.view.textclassifier.-$$Lambda$XeE_KI7QgMKzF9vYRSoFWAolyuA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$XeE_KI7QgMKzF9vYRSoFWAolyuA implements Function {
    public static final /* synthetic */ $$Lambda$XeE_KI7QgMKzF9vYRSoFWAolyuA INSTANCE = new $$Lambda$XeE_KI7QgMKzF9vYRSoFWAolyuA();

    private /* synthetic */ $$Lambda$XeE_KI7QgMKzF9vYRSoFWAolyuA() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ActionsSuggestionsModel.getLocales(((Integer) obj).intValue());
    }
}
