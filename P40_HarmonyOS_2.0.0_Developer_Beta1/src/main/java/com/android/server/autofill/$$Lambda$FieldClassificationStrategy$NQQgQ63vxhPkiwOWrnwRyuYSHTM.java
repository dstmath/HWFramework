package com.android.server.autofill;

import android.content.res.Resources;
import com.android.server.autofill.FieldClassificationStrategy;

/* renamed from: com.android.server.autofill.-$$Lambda$FieldClassificationStrategy$NQQgQ63vxhPkiwOWrnwRyuYSHTM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$FieldClassificationStrategy$NQQgQ63vxhPkiwOWrnwRyuYSHTM implements FieldClassificationStrategy.MetadataParser {
    public static final /* synthetic */ $$Lambda$FieldClassificationStrategy$NQQgQ63vxhPkiwOWrnwRyuYSHTM INSTANCE = new $$Lambda$FieldClassificationStrategy$NQQgQ63vxhPkiwOWrnwRyuYSHTM();

    private /* synthetic */ $$Lambda$FieldClassificationStrategy$NQQgQ63vxhPkiwOWrnwRyuYSHTM() {
    }

    @Override // com.android.server.autofill.FieldClassificationStrategy.MetadataParser
    public final Object get(Resources resources, int i) {
        return resources.getStringArray(i);
    }
}
