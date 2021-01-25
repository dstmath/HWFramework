package android.support.v4.graphics.drawable;

import android.support.annotation.RestrictTo;
import androidx.versionedparcelable.VersionedParcel;

@RestrictTo({RestrictTo.Scope.LIBRARY})
public final class IconCompatParcelizer extends androidx.core.graphics.drawable.IconCompatParcelizer {
    public static IconCompat read(VersionedParcel parcel) {
        return androidx.core.graphics.drawable.IconCompatParcelizer.read(parcel);
    }

    public static void write(IconCompat obj, VersionedParcel parcel) {
        androidx.core.graphics.drawable.IconCompatParcelizer.write(obj, parcel);
    }
}
