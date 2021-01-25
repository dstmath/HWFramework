package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.Comparator;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutPackage$DImOsVxMicPEAJPTxf_RRXuc70I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutPackage$DImOsVxMicPEAJPTxf_RRXuc70I implements Comparator {
    public static final /* synthetic */ $$Lambda$ShortcutPackage$DImOsVxMicPEAJPTxf_RRXuc70I INSTANCE = new $$Lambda$ShortcutPackage$DImOsVxMicPEAJPTxf_RRXuc70I();

    private /* synthetic */ $$Lambda$ShortcutPackage$DImOsVxMicPEAJPTxf_RRXuc70I() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return Integer.compare(((ShortcutInfo) obj).getRank(), ((ShortcutInfo) obj2).getRank());
    }
}
