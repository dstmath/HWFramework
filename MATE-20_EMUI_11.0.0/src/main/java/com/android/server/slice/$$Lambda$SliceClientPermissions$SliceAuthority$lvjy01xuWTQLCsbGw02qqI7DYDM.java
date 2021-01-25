package com.android.server.slice;

import android.text.TextUtils;
import com.android.server.slice.SliceClientPermissions;
import java.util.function.Function;

/* renamed from: com.android.server.slice.-$$Lambda$SliceClientPermissions$SliceAuthority$lvjy01xuWTQLCsbGw02qqI7DYDM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SliceClientPermissions$SliceAuthority$lvjy01xuWTQLCsbGw02qqI7DYDM implements Function {
    public static final /* synthetic */ $$Lambda$SliceClientPermissions$SliceAuthority$lvjy01xuWTQLCsbGw02qqI7DYDM INSTANCE = new $$Lambda$SliceClientPermissions$SliceAuthority$lvjy01xuWTQLCsbGw02qqI7DYDM();

    private /* synthetic */ $$Lambda$SliceClientPermissions$SliceAuthority$lvjy01xuWTQLCsbGw02qqI7DYDM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return TextUtils.join(SliceClientPermissions.SliceAuthority.DELIMITER, (String[]) obj);
    }
}
