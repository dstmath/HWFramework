package android.content.pm;

import android.content.Intent;
import android.os.Bundle;

public final class InstantAppRequest {
    public final String callingPackage;
    public final Intent origIntent;
    public final String resolvedType;
    public final AuxiliaryResolveInfo responseObj;
    public final int userId;
    public final Bundle verificationBundle;

    public InstantAppRequest(AuxiliaryResolveInfo responseObj, Intent origIntent, String resolvedType, String callingPackage, int userId, Bundle verificationBundle) {
        this.responseObj = responseObj;
        this.origIntent = origIntent;
        this.resolvedType = resolvedType;
        this.callingPackage = callingPackage;
        this.userId = userId;
        this.verificationBundle = verificationBundle;
    }
}
