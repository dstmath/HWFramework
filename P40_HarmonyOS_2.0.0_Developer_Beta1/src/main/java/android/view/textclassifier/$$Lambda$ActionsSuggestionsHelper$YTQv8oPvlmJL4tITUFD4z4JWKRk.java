package android.view.textclassifier;

import android.view.textclassifier.ConversationActions;
import java.util.function.ToIntFunction;

/* renamed from: android.view.textclassifier.-$$Lambda$ActionsSuggestionsHelper$YTQv8oPvlmJL4tITUFD4z4JWKRk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActionsSuggestionsHelper$YTQv8oPvlmJL4tITUFD4z4JWKRk implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$ActionsSuggestionsHelper$YTQv8oPvlmJL4tITUFD4z4JWKRk INSTANCE = new $$Lambda$ActionsSuggestionsHelper$YTQv8oPvlmJL4tITUFD4z4JWKRk();

    private /* synthetic */ $$Lambda$ActionsSuggestionsHelper$YTQv8oPvlmJL4tITUFD4z4JWKRk() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ActionsSuggestionsHelper.hashMessage((ConversationActions.Message) obj);
    }
}
