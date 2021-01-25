package com.android.server.ethernet;

import com.android.server.ethernet.EthernetNetworkFactory;
import java.util.Comparator;

/* renamed from: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$EmftAjIay22czoGb8k_mrRGmnzg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EthernetNetworkFactory$EmftAjIay22czoGb8k_mrRGmnzg implements Comparator {
    public static final /* synthetic */ $$Lambda$EthernetNetworkFactory$EmftAjIay22czoGb8k_mrRGmnzg INSTANCE = new $$Lambda$EthernetNetworkFactory$EmftAjIay22czoGb8k_mrRGmnzg();

    private /* synthetic */ $$Lambda$EthernetNetworkFactory$EmftAjIay22czoGb8k_mrRGmnzg() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return EthernetNetworkFactory.lambda$getAvailableInterfaces$1((EthernetNetworkFactory.NetworkInterfaceState) obj, (EthernetNetworkFactory.NetworkInterfaceState) obj2);
    }
}
