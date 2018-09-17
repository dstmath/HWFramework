package android.security.net.config;

import android.os.Environment;
import android.os.UserHandle;
import java.io.File;

public final class UserCertificateSource extends DirectoryCertificateSource {

    private static class NoPreloadHolder {
        private static final UserCertificateSource INSTANCE = new UserCertificateSource();

        private NoPreloadHolder() {
        }
    }

    /* synthetic */ UserCertificateSource(UserCertificateSource -this0) {
        this();
    }

    private UserCertificateSource() {
        super(new File(Environment.getUserConfigDirectory(UserHandle.myUserId()), "cacerts-added"));
    }

    public static UserCertificateSource getInstance() {
        return NoPreloadHolder.INSTANCE;
    }

    protected boolean isCertMarkedAsRemoved(String caFile) {
        return false;
    }
}
