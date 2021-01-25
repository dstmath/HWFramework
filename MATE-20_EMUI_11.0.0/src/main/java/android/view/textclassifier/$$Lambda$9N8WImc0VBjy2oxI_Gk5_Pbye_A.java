package android.view.textclassifier;

import com.google.android.textclassifier.ActionsSuggestionsModel;
import java.util.function.Function;

/* renamed from: android.view.textclassifier.-$$Lambda$9N8WImc0VBjy2oxI_Gk5_Pbye_A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$9N8WImc0VBjy2oxI_Gk5_Pbye_A implements Function {
    public static final /* synthetic */ $$Lambda$9N8WImc0VBjy2oxI_Gk5_Pbye_A INSTANCE = new $$Lambda$9N8WImc0VBjy2oxI_Gk5_Pbye_A();

    private /* synthetic */ $$Lambda$9N8WImc0VBjy2oxI_Gk5_Pbye_A() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(ActionsSuggestionsModel.getVersion(((Integer) obj).intValue()));
    }
}
