package android.provider;

import android.util.Base64;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.util.Preconditions;
import java.util.Collections;
import java.util.List;

public final class FontRequest {
    private final List<List<byte[]>> mCertificates;
    private final String mIdentifier;
    private final String mProviderAuthority;
    private final String mProviderPackage;
    private final String mQuery;

    public FontRequest(String providerAuthority, String providerPackage, String query) {
        this.mProviderAuthority = (String) Preconditions.checkNotNull(providerAuthority);
        this.mQuery = (String) Preconditions.checkNotNull(query);
        this.mProviderPackage = (String) Preconditions.checkNotNull(providerPackage);
        this.mCertificates = Collections.emptyList();
        this.mIdentifier = this.mProviderAuthority + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + this.mProviderPackage + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + this.mQuery;
    }

    public FontRequest(String providerAuthority, String providerPackage, String query, List<List<byte[]>> certificates) {
        this.mProviderAuthority = (String) Preconditions.checkNotNull(providerAuthority);
        this.mProviderPackage = (String) Preconditions.checkNotNull(providerPackage);
        this.mQuery = (String) Preconditions.checkNotNull(query);
        this.mCertificates = (List) Preconditions.checkNotNull(certificates);
        this.mIdentifier = this.mProviderAuthority + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + this.mProviderPackage + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + this.mQuery;
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

    public List<List<byte[]>> getCertificates() {
        return this.mCertificates;
    }

    public String getIdentifier() {
        return this.mIdentifier;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FontRequest {mProviderAuthority: " + this.mProviderAuthority + ", mProviderPackage: " + this.mProviderPackage + ", mQuery: " + this.mQuery + ", mCertificates:");
        for (int i = 0; i < this.mCertificates.size(); i++) {
            builder.append(" [");
            List<byte[]> set = this.mCertificates.get(i);
            for (int j = 0; j < set.size(); j++) {
                builder.append(" \"");
                builder.append(Base64.encodeToString(set.get(j), 0));
                builder.append("\"");
            }
            builder.append(" ]");
        }
        builder.append("}");
        return builder.toString();
    }
}
