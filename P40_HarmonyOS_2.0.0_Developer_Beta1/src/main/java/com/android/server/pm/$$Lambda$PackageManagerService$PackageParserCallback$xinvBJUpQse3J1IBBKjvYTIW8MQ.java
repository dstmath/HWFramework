package com.android.server.pm;

import android.content.pm.PackageParser;
import java.util.function.ToIntFunction;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$PackageParserCallback$xinvBJUpQse3J1IBBKjvYTIW8MQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$PackageParserCallback$xinvBJUpQse3J1IBBKjvYTIW8MQ implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$PackageManagerService$PackageParserCallback$xinvBJUpQse3J1IBBKjvYTIW8MQ INSTANCE = new $$Lambda$PackageManagerService$PackageParserCallback$xinvBJUpQse3J1IBBKjvYTIW8MQ();

    private /* synthetic */ $$Lambda$PackageManagerService$PackageParserCallback$xinvBJUpQse3J1IBBKjvYTIW8MQ() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((PackageParser.Package) obj).mOverlayPriority;
    }
}
