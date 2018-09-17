package com.android.server.job;

import android.app.IActivityManager;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ContentProvider;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class GrantedUriPermissions {
    private final int mGrantFlags;
    private final IBinder mPermissionOwner;
    private final int mSourceUserId;
    private final String mTag;
    private final ArrayList<Uri> mUris = new ArrayList();

    private GrantedUriPermissions(IActivityManager am, int grantFlags, int uid, String tag) throws RemoteException {
        this.mGrantFlags = grantFlags;
        this.mSourceUserId = UserHandle.getUserId(uid);
        this.mTag = tag;
        this.mPermissionOwner = am.newUriPermissionOwner("job: " + tag);
    }

    public void revoke(IActivityManager am) {
        for (int i = this.mUris.size() - 1; i >= 0; i--) {
            try {
                am.revokeUriPermissionFromOwner(this.mPermissionOwner, (Uri) this.mUris.get(i), this.mGrantFlags, this.mSourceUserId);
            } catch (RemoteException e) {
            }
        }
        this.mUris.clear();
    }

    public static boolean checkGrantFlags(int grantFlags) {
        return (grantFlags & 3) != 0;
    }

    public static GrantedUriPermissions createFromIntent(IActivityManager am, Intent intent, int sourceUid, String targetPackage, int targetUserId, String tag) {
        int grantFlags = intent.getFlags();
        if (!checkGrantFlags(grantFlags)) {
            return null;
        }
        GrantedUriPermissions perms = null;
        Uri data = intent.getData();
        if (data != null) {
            perms = grantUri(am, data, sourceUid, targetPackage, targetUserId, grantFlags, tag, null);
        }
        ClipData clip = intent.getClipData();
        if (clip != null) {
            perms = grantClip(am, clip, sourceUid, targetPackage, targetUserId, grantFlags, tag, perms);
        }
        return perms;
    }

    public static GrantedUriPermissions createFromClip(IActivityManager am, ClipData clip, int sourceUid, String targetPackage, int targetUserId, int grantFlags, String tag) {
        if (!checkGrantFlags(grantFlags)) {
            return null;
        }
        GrantedUriPermissions perms = null;
        if (clip != null) {
            perms = grantClip(am, clip, sourceUid, targetPackage, targetUserId, grantFlags, tag, null);
        }
        return perms;
    }

    private static GrantedUriPermissions grantClip(IActivityManager am, ClipData clip, int sourceUid, String targetPackage, int targetUserId, int grantFlags, String tag, GrantedUriPermissions curPerms) {
        int N = clip.getItemCount();
        for (int i = 0; i < N; i++) {
            curPerms = grantItem(am, clip.getItemAt(i), sourceUid, targetPackage, targetUserId, grantFlags, tag, curPerms);
        }
        return curPerms;
    }

    private static GrantedUriPermissions grantUri(IActivityManager am, Uri uri, int sourceUid, String targetPackage, int targetUserId, int grantFlags, String tag, GrantedUriPermissions curPerms) {
        try {
            int sourceUserId = ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid));
            uri = ContentProvider.getUriWithoutUserId(uri);
            if (curPerms == null) {
                curPerms = new GrantedUriPermissions(am, grantFlags, sourceUid, tag);
            }
            am.grantUriPermissionFromOwner(curPerms.mPermissionOwner, sourceUid, targetPackage, uri, grantFlags, sourceUserId, targetUserId);
            curPerms.mUris.add(uri);
        } catch (RemoteException e) {
            Slog.e("JobScheduler", "AM dead");
        }
        return curPerms;
    }

    private static GrantedUriPermissions grantItem(IActivityManager am, Item item, int sourceUid, String targetPackage, int targetUserId, int grantFlags, String tag, GrantedUriPermissions curPerms) {
        if (item.getUri() != null) {
            curPerms = grantUri(am, item.getUri(), sourceUid, targetPackage, targetUserId, grantFlags, tag, curPerms);
        }
        Intent intent = item.getIntent();
        if (intent == null || intent.getData() == null) {
            return curPerms;
        }
        return grantUri(am, intent.getData(), sourceUid, targetPackage, targetUserId, grantFlags, tag, curPerms);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mGrantFlags=0x");
        pw.print(Integer.toHexString(this.mGrantFlags));
        pw.print(" mSourceUserId=");
        pw.println(this.mSourceUserId);
        pw.print(prefix);
        pw.print("mTag=");
        pw.println(this.mTag);
        pw.print(prefix);
        pw.print("mPermissionOwner=");
        pw.println(this.mPermissionOwner);
        for (int i = 0; i < this.mUris.size(); i++) {
            pw.print(prefix);
            pw.print("#");
            pw.print(i);
            pw.print(": ");
            pw.println(this.mUris.get(i));
        }
    }
}
