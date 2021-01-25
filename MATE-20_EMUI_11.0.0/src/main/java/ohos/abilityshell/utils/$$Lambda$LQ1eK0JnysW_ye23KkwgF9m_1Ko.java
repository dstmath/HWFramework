package ohos.abilityshell.utils;

import java.util.function.Function;
import ohos.utils.Sequenceable;

/* renamed from: ohos.abilityshell.utils.-$$Lambda$LQ1eK0JnysW_ye23KkwgF9m_1Ko  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LQ1eK0JnysW_ye23KkwgF9m_1Ko implements Function {
    public static final /* synthetic */ $$Lambda$LQ1eK0JnysW_ye23KkwgF9m_1Ko INSTANCE = new $$Lambda$LQ1eK0JnysW_ye23KkwgF9m_1Ko();

    private /* synthetic */ $$Lambda$LQ1eK0JnysW_ye23KkwgF9m_1Ko() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return new SequenceableWrapper((Sequenceable) obj);
    }
}
