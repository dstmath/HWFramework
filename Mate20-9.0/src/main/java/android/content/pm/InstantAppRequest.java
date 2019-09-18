package android.content.pm;

import android.content.Intent;
import android.content.pm.InstantAppResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;

public final class InstantAppRequest {
    public final String callingPackage;
    public final InstantAppResolveInfo.InstantAppDigest digest;
    public final Intent origIntent;
    public final boolean resolveForStart;
    public final String resolvedType;
    public final AuxiliaryResolveInfo responseObj;
    public final int userId;
    public final Bundle verificationBundle;

    public InstantAppRequest(AuxiliaryResolveInfo responseObj2, Intent origIntent2, String resolvedType2, String callingPackage2, int userId2, Bundle verificationBundle2, boolean resolveForStart2) {
        this.responseObj = responseObj2;
        this.origIntent = origIntent2;
        this.resolvedType = resolvedType2;
        this.callingPackage = callingPackage2;
        this.userId = userId2;
        this.verificationBundle = verificationBundle2;
        this.resolveForStart = resolveForStart2;
        if (origIntent2.getData() == null || TextUtils.isEmpty(origIntent2.getData().getHost())) {
            this.digest = InstantAppResolveInfo.InstantAppDigest.UNDEFINED;
        } else {
            this.digest = new InstantAppResolveInfo.InstantAppDigest(origIntent2.getData().getHost(), 5);
        }
    }
}
