package androidx.versionedparcelable;

import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public abstract class CustomVersionedParcelable implements VersionedParcelable {
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void onPreParceling(boolean isStream) {
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void onPostParceling() {
    }
}
