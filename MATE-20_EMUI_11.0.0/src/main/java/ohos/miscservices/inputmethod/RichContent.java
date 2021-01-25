package ohos.miscservices.inputmethod;

import java.util.Arrays;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.implement.UriPermissionSkeleton;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class RichContent implements Sequenceable {
    public static final Sequenceable.Producer<RichContent> CREATOR = new Sequenceable.Producer<RichContent>() {
        /* class ohos.miscservices.inputmethod.RichContent.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public RichContent createFromParcel(Parcel parcel) {
            if (parcel != null) {
                return new RichContent(parcel);
            }
            HiLog.debug(RichContent.TAG, "createFromParcel: source is null", new Object[0]);
            return null;
        }
    };
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "RichContent");
    private final String mContentDescription;
    private final Uri mContentUri;
    private final Uri mLinkUri;
    private String[] mMimeTypes;
    private IUriPermission mUriPermission;

    public RichContent(Uri uri, Uri uri2, String[] strArr, String str) {
        checkParameters(uri, uri2, strArr, str);
        this.mContentUri = uri;
        this.mLinkUri = uri2;
        this.mContentDescription = str;
        saveMimeTypes(strArr);
    }

    private RichContent(Parcel parcel) {
        this.mContentUri = Uri.readFromParcel(parcel);
        this.mMimeTypes = parcel.readStringArray();
        this.mContentDescription = parcel.readString();
        this.mLinkUri = Uri.readFromParcel(parcel);
        if (parcel instanceof MessageParcel) {
            this.mUriPermission = UriPermissionSkeleton.asInterface(((MessageParcel) parcel).readRemoteObject());
        } else {
            this.mUriPermission = null;
        }
    }

    private boolean checkParameters(Uri uri, Uri uri2, String[] strArr, String str) {
        String scheme;
        if (uri == null) {
            throw new NullPointerException("contentUri");
        } else if (strArr != null) {
            String scheme2 = uri.getScheme();
            if (scheme2 == null || !"content".equals(scheme2)) {
                throw new IllegalArgumentException("contentUri must have content scheme");
            } else if (uri2 != null && ((scheme = uri2.getScheme()) == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)))) {
                throw new IllegalArgumentException("linkUri must have either http or https scheme");
            } else if (str != null) {
                return true;
            } else {
                throw new NullPointerException("contentDescription");
            }
        } else {
            throw new NullPointerException("mimeTypes");
        }
    }

    private void saveMimeTypes(String[] strArr) {
        if (strArr.length == 0) {
            this.mMimeTypes = new String[0];
        } else {
            this.mMimeTypes = (String[]) Arrays.copyOf(strArr, strArr.length);
        }
    }

    private String[] exportMimeTypes() {
        String[] strArr = this.mMimeTypes;
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    public Uri getDataUri() {
        return this.mContentUri;
    }

    public void setUriPermission(IUriPermission iUriPermission) {
        if (this.mUriPermission == null) {
            this.mUriPermission = iUriPermission;
            return;
        }
        throw new IllegalStateException("Permission is already get");
    }

    public Uri getLinkUri() {
        return this.mLinkUri;
    }

    public String[] getMimeTypes() {
        return exportMimeTypes();
    }

    public String getDataDetail() {
        return this.mContentDescription;
    }

    public void takeUriPermission() {
        IUriPermission iUriPermission = this.mUriPermission;
        if (iUriPermission != null) {
            try {
                iUriPermission.take();
            } catch (RemoteException e) {
                HiLog.error(TAG, "take failed,Exception = %s", e.toString());
            }
        }
    }

    public void releaseUriPermission() {
        IUriPermission iUriPermission = this.mUriPermission;
        if (iUriPermission != null) {
            try {
                iUriPermission.release();
            } catch (RemoteException e) {
                HiLog.error(TAG, "release failed,Exception = %s", e.toString());
            }
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            HiLog.debug(TAG, "marshalling out is null", new Object[0]);
            return false;
        }
        parcel.writeSequenceable(this.mContentUri);
        if (!parcel.writeStringArray(this.mMimeTypes) || !parcel.writeString(this.mContentDescription)) {
            return false;
        }
        parcel.writeSequenceable(this.mLinkUri);
        if (parcel instanceof MessageParcel) {
            ((MessageParcel) parcel).writeRemoteObject(this.mUriPermission.asObject());
            return true;
        }
        HiLog.error(TAG, "mUriPermission Parcel failed", new Object[0]);
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        HiLog.info(TAG, "RichContent: unmarshalling not implemented", new Object[0]);
        return false;
    }
}
