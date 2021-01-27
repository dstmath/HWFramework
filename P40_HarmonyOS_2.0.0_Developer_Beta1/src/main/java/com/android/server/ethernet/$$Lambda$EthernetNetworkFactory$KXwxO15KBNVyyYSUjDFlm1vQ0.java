package com.android.server.ethernet;

import com.android.server.ethernet.EthernetNetworkFactory;
import java.util.function.Function;

/* renamed from: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$KXwxO15KBNVyyYS-UjD-Flm1vQ0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EthernetNetworkFactory$KXwxO15KBNVyyYSUjDFlm1vQ0 implements Function {
    public static final /* synthetic */ $$Lambda$EthernetNetworkFactory$KXwxO15KBNVyyYSUjDFlm1vQ0 INSTANCE = new $$Lambda$EthernetNetworkFactory$KXwxO15KBNVyyYSUjDFlm1vQ0();

    private /* synthetic */ $$Lambda$EthernetNetworkFactory$KXwxO15KBNVyyYSUjDFlm1vQ0() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((EthernetNetworkFactory.NetworkInterfaceState) obj).name;
    }
}
