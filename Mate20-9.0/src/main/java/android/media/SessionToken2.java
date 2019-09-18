package android.media;

import android.content.Context;
import android.media.update.ApiLoader;
import android.media.update.SessionToken2Provider;
import android.os.Bundle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class SessionToken2 {
    public static final int TYPE_LIBRARY_SERVICE = 2;
    public static final int TYPE_SESSION = 0;
    public static final int TYPE_SESSION_SERVICE = 1;
    private static final int UID_UNKNOWN = -1;
    private final SessionToken2Provider mProvider;

    @Retention(RetentionPolicy.SOURCE)
    public @interface TokenType {
    }

    public SessionToken2(Context context, String packageName, String serviceName) {
        this(context, packageName, serviceName, -1);
    }

    public SessionToken2(Context context, String packageName, String serviceName, int uid) {
        this.mProvider = ApiLoader.getProvider().createSessionToken2(context, this, packageName, serviceName, uid);
    }

    public SessionToken2(SessionToken2Provider provider) {
        this.mProvider = provider;
    }

    public int hashCode() {
        return this.mProvider.hashCode_impl();
    }

    public boolean equals(Object obj) {
        return this.mProvider.equals_impl(obj);
    }

    public String toString() {
        return this.mProvider.toString_impl();
    }

    public SessionToken2Provider getProvider() {
        return this.mProvider;
    }

    public int getUid() {
        return this.mProvider.getUid_impl();
    }

    public String getPackageName() {
        return this.mProvider.getPackageName_impl();
    }

    public String getId() {
        return this.mProvider.getId_imp();
    }

    public int getType() {
        return this.mProvider.getType_impl();
    }

    public static SessionToken2 fromBundle(Bundle bundle) {
        return ApiLoader.getProvider().fromBundle_SessionToken2(bundle);
    }

    public Bundle toBundle() {
        return this.mProvider.toBundle_impl();
    }
}
