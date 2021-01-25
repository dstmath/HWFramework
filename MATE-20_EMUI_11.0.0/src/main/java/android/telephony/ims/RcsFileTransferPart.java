package android.telephony.ims;

import android.net.Uri;
import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RcsFileTransferPart {
    public static final int DOWNLOADING = 6;
    public static final int DOWNLOADING_CANCELLED = 9;
    public static final int DOWNLOADING_FAILED = 8;
    public static final int DOWNLOADING_PAUSED = 7;
    public static final int DRAFT = 1;
    public static final int NOT_SET = 0;
    public static final int SENDING = 2;
    public static final int SENDING_CANCELLED = 5;
    public static final int SENDING_FAILED = 4;
    public static final int SENDING_PAUSED = 3;
    public static final int SUCCEEDED = 10;
    private int mId;
    private final RcsControllerCall mRcsControllerCall;

    @Retention(RetentionPolicy.SOURCE)
    public @interface RcsFileTransferStatus {
    }

    RcsFileTransferPart(RcsControllerCall rcsControllerCall, int id) {
        this.mRcsControllerCall = rcsControllerCall;
        this.mId = id;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }

    public void setFileTransferSessionId(String sessionId) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(sessionId) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$eRysznIV0Pr9U0YPttLhvYxp2JE */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setFileTransferSessionId$0$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setFileTransferSessionId$0$RcsFileTransferPart(String sessionId, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferSessionId(this.mId, sessionId, callingPackage);
    }

    public String getFileTransferSessionId() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$KCwtK0SDWMMpZpRsslXFJ_BwLM */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getFileTransferSessionId$1$RcsFileTransferPart(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getFileTransferSessionId$1$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getFileTransferSessionId(this.mId, callingPackage);
    }

    public void setContentUri(Uri contentUri) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(contentUri) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$gHrYiSj4B912GPuzgw6v3qjIwX4 */
            private final /* synthetic */ Uri f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setContentUri$2$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setContentUri$2$RcsFileTransferPart(Uri contentUri, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferContentUri(this.mId, contentUri, callingPackage);
    }

    public Uri getContentUri() throws RcsMessageStoreException {
        return (Uri) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$kvkf6ASdUq8pR3hQ4h9sWdIiOQ */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getContentUri$3$RcsFileTransferPart(iRcs, str);
            }
        });
    }

    public /* synthetic */ Uri lambda$getContentUri$3$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getFileTransferContentUri(this.mId, callingPackage);
    }

    public void setContentMimeType(String contentMimeType) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(contentMimeType) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$_U_JpxTv_8vqlG8zHOxxNMMBqjQ */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setContentMimeType$4$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setContentMimeType$4$RcsFileTransferPart(String contentMimeType, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferContentType(this.mId, contentMimeType, callingPackage);
    }

    public String getContentMimeType() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$X3yfwvMihWzA9VZLnUyeAlq_rVc */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getContentMimeType$5$RcsFileTransferPart(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getContentMimeType$5$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getFileTransferContentType(this.mId, callingPackage);
    }

    public void setFileSize(long contentLength) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(contentLength) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$iFRtCc6m4Iup_st7fFqTiBlhq4o */
            private final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setFileSize$6$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setFileSize$6$RcsFileTransferPart(long contentLength, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferFileSize(this.mId, contentLength, callingPackage);
    }

    public long getFileSize() throws RcsMessageStoreException {
        return ((Long) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$RUTTVEFxx0RPDq0oORm2TF6GoJ8 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getFileSize$7$RcsFileTransferPart(iRcs, str);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getFileSize$7$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return Long.valueOf(iRcs.getFileTransferFileSize(this.mId, callingPackage));
    }

    public void setTransferOffset(long transferOffset) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(transferOffset) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$NeUx42gy02DXOOj3iF2Y92GoU */
            private final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setTransferOffset$8$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setTransferOffset$8$RcsFileTransferPart(long transferOffset, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferTransferOffset(this.mId, transferOffset, callingPackage);
    }

    public long getTransferOffset() throws RcsMessageStoreException {
        return ((Long) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$m0Uztiu9azOAnoxBEWLsT8Br_HE */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getTransferOffset$9$RcsFileTransferPart(iRcs, str);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getTransferOffset$9$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return Long.valueOf(iRcs.getFileTransferTransferOffset(this.mId, callingPackage));
    }

    public void setFileTransferStatus(int status) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(status) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$1I5TANd1JGzUvxVPbWbmYgYHgZg */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setFileTransferStatus$10$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setFileTransferStatus$10$RcsFileTransferPart(int status, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferStatus(this.mId, status, callingPackage);
    }

    public int getFileTransferStatus() throws RcsMessageStoreException {
        return ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$5nq0jbEkQm3ys2NrT291eV7NXn8 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getFileTransferStatus$11$RcsFileTransferPart(iRcs, str);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getFileTransferStatus$11$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.getFileTransferStatus(this.mId, callingPackage));
    }

    public int getWidth() throws RcsMessageStoreException {
        return ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$cbwg3i9EtuBNKXI5md4IWJQ_GDo */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getWidth$12$RcsFileTransferPart(iRcs, str);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getWidth$12$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.getFileTransferWidth(this.mId, callingPackage));
    }

    public void setWidth(int width) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(width) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$dlGXDrIqL9NsNgH4LIS6Yg7j6k */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setWidth$13$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setWidth$13$RcsFileTransferPart(int width, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferWidth(this.mId, width, callingPackage);
    }

    public int getHeight() throws RcsMessageStoreException {
        return ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$A_4O6faLVs6mpaPsKJIA9HefwvU */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getHeight$14$RcsFileTransferPart(iRcs, str);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getHeight$14$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.getFileTransferHeight(this.mId, callingPackage));
    }

    public void setHeight(int height) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(height) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$Ju03J4o5Gnha0Ynbq35sw9HL5nU */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setHeight$15$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setHeight$15$RcsFileTransferPart(int height, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferHeight(this.mId, height, callingPackage);
    }

    public long getLength() throws RcsMessageStoreException {
        return ((Long) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$B5UxN0BhElRxFWpAZgbz41DxuY */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getLength$16$RcsFileTransferPart(iRcs, str);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getLength$16$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return Long.valueOf(iRcs.getFileTransferLength(this.mId, callingPackage));
    }

    public void setLength(long length) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(length) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$kXXTp4pKFNyBztnIElEJdJrz8F8 */
            private final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setLength$17$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setLength$17$RcsFileTransferPart(long length, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferLength(this.mId, length, callingPackage);
    }

    public Uri getPreviewUri() throws RcsMessageStoreException {
        return (Uri) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$pZ6z6R9RPQvoiIFOhauV7YAePw */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getPreviewUri$18$RcsFileTransferPart(iRcs, str);
            }
        });
    }

    public /* synthetic */ Uri lambda$getPreviewUri$18$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getFileTransferPreviewUri(this.mId, callingPackage);
    }

    public void setPreviewUri(Uri previewUri) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(previewUri) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$4bTF8UNuphmPWGI1zJtDN0vEMKQ */
            private final /* synthetic */ Uri f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setPreviewUri$19$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setPreviewUri$19$RcsFileTransferPart(Uri previewUri, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferPreviewUri(this.mId, previewUri, callingPackage);
    }

    public String getPreviewMimeType() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$B5FCShigB8L98Le8jQF4kRDSfhk */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsFileTransferPart.this.lambda$getPreviewMimeType$20$RcsFileTransferPart(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getPreviewMimeType$20$RcsFileTransferPart(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getFileTransferPreviewType(this.mId, callingPackage);
    }

    public void setPreviewMimeType(String previewMimeType) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(previewMimeType) {
            /* class android.telephony.ims.$$Lambda$RcsFileTransferPart$Js49W5j_aEL3sBPRKR3zwBZEwQc */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsFileTransferPart.this.lambda$setPreviewMimeType$21$RcsFileTransferPart(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setPreviewMimeType$21$RcsFileTransferPart(String previewMimeType, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setFileTransferPreviewType(this.mId, previewMimeType, callingPackage);
    }
}
