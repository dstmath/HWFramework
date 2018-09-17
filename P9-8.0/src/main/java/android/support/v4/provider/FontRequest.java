package android.support.v4.provider;

import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.util.Preconditions;
import android.util.Base64;
import java.util.List;

public final class FontRequest {
    private final List<List<byte[]>> mCertificates;
    private final int mCertificatesArray;
    private final String mIdentifier;
    private final String mProviderAuthority;
    private final String mProviderPackage;
    private final String mQuery;

    public FontRequest(@NonNull String providerAuthority, @NonNull String providerPackage, @NonNull String query, @NonNull List<List<byte[]>> certificates) {
        this.mProviderAuthority = (String) Preconditions.checkNotNull(providerAuthority);
        this.mProviderPackage = (String) Preconditions.checkNotNull(providerPackage);
        this.mQuery = (String) Preconditions.checkNotNull(query);
        this.mCertificates = (List) Preconditions.checkNotNull(certificates);
        this.mCertificatesArray = 0;
        this.mIdentifier = new StringBuilder(this.mProviderAuthority).append("-").append(this.mProviderPackage).append("-").append(this.mQuery).toString();
    }

    public FontRequest(@NonNull String providerAuthority, @NonNull String providerPackage, @NonNull String query, @ArrayRes int certificates) {
        boolean z;
        this.mProviderAuthority = (String) Preconditions.checkNotNull(providerAuthority);
        this.mProviderPackage = (String) Preconditions.checkNotNull(providerPackage);
        this.mQuery = (String) Preconditions.checkNotNull(query);
        this.mCertificates = null;
        if (certificates != 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
        this.mCertificatesArray = certificates;
        this.mIdentifier = new StringBuilder(this.mProviderAuthority).append("-").append(this.mProviderPackage).append("-").append(this.mQuery).toString();
    }

    public String getProviderAuthority() {
        return this.mProviderAuthority;
    }

    public String getProviderPackage() {
        return this.mProviderPackage;
    }

    public String getQuery() {
        return this.mQuery;
    }

    @Nullable
    public List<List<byte[]>> getCertificates() {
        return this.mCertificates;
    }

    @ArrayRes
    public int getCertificatesArrayResId() {
        return this.mCertificatesArray;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public String getIdentifier() {
        return this.mIdentifier;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FontRequest {mProviderAuthority: ").append(this.mProviderAuthority).append(", mProviderPackage: ").append(this.mProviderPackage).append(", mQuery: ").append(this.mQuery).append(", mCertificates:");
        for (int i = 0; i < this.mCertificates.size(); i++) {
            builder.append(" [");
            List<byte[]> set = (List) this.mCertificates.get(i);
            for (int j = 0; j < set.size(); j++) {
                builder.append(" \"");
                builder.append(Base64.encodeToString((byte[]) set.get(j), 0));
                builder.append("\"");
            }
            builder.append(" ]");
        }
        builder.append("}");
        builder.append("mCertificatesArray: ").append(this.mCertificatesArray);
        return builder.toString();
    }
}
