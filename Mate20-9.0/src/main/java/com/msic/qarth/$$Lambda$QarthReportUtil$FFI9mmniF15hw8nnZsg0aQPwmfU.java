package com.msic.qarth;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/* renamed from: com.msic.qarth.-$$Lambda$QarthReportUtil$FFI9mmniF15hw8nnZsg0aQPwmfU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$QarthReportUtil$FFI9mmniF15hw8nnZsg0aQPwmfU implements FilenameFilter {
    public static final /* synthetic */ $$Lambda$QarthReportUtil$FFI9mmniF15hw8nnZsg0aQPwmfU INSTANCE = new $$Lambda$QarthReportUtil$FFI9mmniF15hw8nnZsg0aQPwmfU();

    private /* synthetic */ $$Lambda$QarthReportUtil$FFI9mmniF15hw8nnZsg0aQPwmfU() {
    }

    public final boolean accept(File file, String str) {
        return str.toLowerCase(Locale.ENGLISH).endsWith(".qarth");
    }
}
