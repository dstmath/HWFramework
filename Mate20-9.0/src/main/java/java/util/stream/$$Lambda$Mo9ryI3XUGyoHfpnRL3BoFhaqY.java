package java.util.stream;

import java.util.function.BinaryOperator;
import java.util.stream.Nodes;

/* renamed from: java.util.stream.-$$Lambda$Mo9-ryI3XUGyoHfpnRL3BoFhaqY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Mo9ryI3XUGyoHfpnRL3BoFhaqY implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$Mo9ryI3XUGyoHfpnRL3BoFhaqY INSTANCE = new $$Lambda$Mo9ryI3XUGyoHfpnRL3BoFhaqY();

    private /* synthetic */ $$Lambda$Mo9ryI3XUGyoHfpnRL3BoFhaqY() {
    }

    public final Object apply(Object obj, Object obj2) {
        return new Nodes.ConcNode((Node) obj, (Node) obj2);
    }
}
