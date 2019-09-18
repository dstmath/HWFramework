package java.util.stream;

import java.util.function.BinaryOperator;
import java.util.stream.Node;
import java.util.stream.Nodes;

/* renamed from: java.util.stream.-$$Lambda$O4iFzVwtlyKFZkWcnfXHIHbxaTY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$O4iFzVwtlyKFZkWcnfXHIHbxaTY implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$O4iFzVwtlyKFZkWcnfXHIHbxaTY INSTANCE = new $$Lambda$O4iFzVwtlyKFZkWcnfXHIHbxaTY();

    private /* synthetic */ $$Lambda$O4iFzVwtlyKFZkWcnfXHIHbxaTY() {
    }

    public final Object apply(Object obj, Object obj2) {
        return new Nodes.ConcNode.OfInt((Node.OfInt) obj, (Node.OfInt) obj2);
    }
}
