package com.android.server.uri;

import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.IBinder;
import java.io.PrintWriter;

public interface UriGrantsManagerInternal {
    boolean checkAuthorityGrants(int i, ProviderInfo providerInfo, int i2, boolean z);

    int checkGrantUriPermission(int i, String str, Uri uri, int i2, int i3);

    int checkGrantUriPermission(int i, String str, GrantUri grantUri, int i2, int i3);

    NeededUriGrants checkGrantUriPermissionFromIntent(int i, String str, Intent intent, int i2, NeededUriGrants neededUriGrants, int i3);

    boolean checkUriPermission(GrantUri grantUri, int i, int i2);

    void dump(PrintWriter printWriter, boolean z, String str);

    void grantUriPermission(int i, String str, GrantUri grantUri, int i2, UriPermissionOwner uriPermissionOwner, int i3);

    void grantUriPermissionFromIntent(int i, String str, Intent intent, int i2);

    void grantUriPermissionFromIntent(int i, String str, Intent intent, UriPermissionOwner uriPermissionOwner, int i2);

    void grantUriPermissionUncheckedFromIntent(NeededUriGrants neededUriGrants, UriPermissionOwner uriPermissionOwner);

    IBinder newUriPermissionOwner(String str);

    void onActivityManagerInternalAdded();

    void onSystemReady();

    void removeUriPermissionIfNeeded(UriPermission uriPermission);

    void removeUriPermissionsForPackage(String str, int i, boolean z, boolean z2);

    void revokeUriPermission(String str, int i, GrantUri grantUri, int i2);

    void revokeUriPermissionFromOwner(IBinder iBinder, Uri uri, int i, int i2);
}
