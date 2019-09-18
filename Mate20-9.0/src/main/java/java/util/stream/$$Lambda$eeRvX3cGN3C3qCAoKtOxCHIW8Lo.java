package java.util.stream;

import java.util.function.BinaryOperator;
import java.util.stream.Node;
import java.util.stream.Nodes;

/* renamed from: java.util.stream.-$$Lambda$eeRvX3cGN3C3qCAoKtOxCHIW8Lo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$eeRvX3cGN3C3qCAoKtOxCHIW8Lo implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$eeRvX3cGN3C3qCAoKtOxCHIW8Lo INSTANCE = new $$Lambda$eeRvX3cGN3C3qCAoKtOxCHIW8Lo();

    private /* synthetic */ $$Lambda$eeRvX3cGN3C3qCAoKtOxCHIW8Lo() {
    }

    public final Object apply(Object obj, Object obj2) {
        return new Nodes.ConcNode.OfLong((Node.OfLong) obj, (Node.OfLong) obj2);
    }
}
