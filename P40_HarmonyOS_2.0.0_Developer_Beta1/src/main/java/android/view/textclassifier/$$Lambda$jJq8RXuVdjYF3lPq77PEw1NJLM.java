package android.view.textclassifier;

import com.google.android.textclassifier.AnnotatorModel;
import java.util.function.Function;

/* renamed from: android.view.textclassifier.-$$Lambda$jJq8RXuVdjYF3lPq-77PEw1NJLM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$jJq8RXuVdjYF3lPq77PEw1NJLM implements Function {
    public static final /* synthetic */ $$Lambda$jJq8RXuVdjYF3lPq77PEw1NJLM INSTANCE = new $$Lambda$jJq8RXuVdjYF3lPq77PEw1NJLM();

    private /* synthetic */ $$Lambda$jJq8RXuVdjYF3lPq77PEw1NJLM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(AnnotatorModel.getVersion(((Integer) obj).intValue()));
    }
}
