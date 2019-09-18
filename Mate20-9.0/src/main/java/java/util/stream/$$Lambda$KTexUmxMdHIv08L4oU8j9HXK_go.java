package java.util.stream;

import java.util.function.BinaryOperator;
import java.util.stream.Node;
import java.util.stream.Nodes;

/* renamed from: java.util.stream.-$$Lambda$KTexUmxMdHIv08L4oU8j9HXK_go  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KTexUmxMdHIv08L4oU8j9HXK_go implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$KTexUmxMdHIv08L4oU8j9HXK_go INSTANCE = new $$Lambda$KTexUmxMdHIv08L4oU8j9HXK_go();

    private /* synthetic */ $$Lambda$KTexUmxMdHIv08L4oU8j9HXK_go() {
    }

    public final Object apply(Object obj, Object obj2) {
        return new Nodes.ConcNode.OfDouble((Node.OfDouble) obj, (Node.OfDouble) obj2);
    }
}
