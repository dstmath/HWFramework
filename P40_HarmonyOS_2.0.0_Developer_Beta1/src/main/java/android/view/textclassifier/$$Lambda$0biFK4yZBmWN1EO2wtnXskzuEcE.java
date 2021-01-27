package android.view.textclassifier;

import com.google.android.textclassifier.LangIdModel;
import java.util.function.Function;

/* renamed from: android.view.textclassifier.-$$Lambda$0biFK4yZBmWN1EO2wtnXskzuEcE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$0biFK4yZBmWN1EO2wtnXskzuEcE implements Function {
    public static final /* synthetic */ $$Lambda$0biFK4yZBmWN1EO2wtnXskzuEcE INSTANCE = new $$Lambda$0biFK4yZBmWN1EO2wtnXskzuEcE();

    private /* synthetic */ $$Lambda$0biFK4yZBmWN1EO2wtnXskzuEcE() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(LangIdModel.getVersion(((Integer) obj).intValue()));
    }
}
