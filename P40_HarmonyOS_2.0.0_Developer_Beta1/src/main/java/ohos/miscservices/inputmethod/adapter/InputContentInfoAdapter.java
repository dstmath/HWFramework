package ohos.miscservices.inputmethod.adapter;

import android.content.ClipDescription;
import android.os.RemoteException;
import android.view.inputmethod.InputContentInfo;
import com.android.internal.inputmethod.IInputContentUriToken;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.internal.IUriPermission;
import ohos.miscservices.inputmethod.internal.UriPermissionSkeleton;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

public class InputContentInfoAdapter {
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.interfaces.IUriPermission";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputContentInfoAdapter");

    public static RichContent convertToRichContent(final InputContentInfo inputContentInfo) {
        String[] strArr;
        Uri uri = null;
        if (inputContentInfo == null) {
            HiLog.error(TAG, "inputContentInfo is null.", new Object[0]);
            return null;
        }
        if (inputContentInfo.getLinkUri() != null) {
            uri = Uri.parse(inputContentInfo.getLinkUri().toString());
        }
        Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(inputContentInfo.getContentUri(), "");
        int mimeTypeCount = inputContentInfo.getDescription().getMimeTypeCount();
        String charSequence = inputContentInfo.getDescription().getLabel().toString();
        if (mimeTypeCount == 0) {
            strArr = new String[0];
        } else {
            String[] strArr2 = new String[mimeTypeCount];
            for (int i = 0; i < mimeTypeCount; i++) {
                strArr2[i] = inputContentInfo.getDescription().getMimeType(i);
            }
            strArr = strArr2;
        }
        IUriPermission asInterface = UriPermissionSkeleton.asInterface(new UriPermissionSkeleton(DESCRIPTOR) {
            /* class ohos.miscservices.inputmethod.adapter.InputContentInfoAdapter.AnonymousClass1UriPermissionImpl */

            @Override // ohos.miscservices.inputmethod.internal.IUriPermission
            public void take() {
                HiLog.info(InputContentInfoAdapter.TAG, "UriPermission take.", new Object[0]);
                inputContentInfo.requestPermission();
            }

            @Override // ohos.miscservices.inputmethod.internal.IUriPermission
            public void release() {
                HiLog.info(InputContentInfoAdapter.TAG, "UriPermission release.", new Object[0]);
                inputContentInfo.releasePermission();
            }
        }.asObject());
        RichContent richContent = new RichContent(convertToZidaneContentUri, uri, strArr, charSequence);
        richContent.setUriPermission(asInterface);
        return richContent;
    }

    public static InputContentInfo convertToInputContentInfo(final RichContent richContent) {
        android.net.Uri uri = null;
        if (richContent == null) {
            HiLog.error(TAG, "The given richContent can not be null.", new Object[0]);
            return null;
        }
        android.net.Uri convertToAndroidContentUri = UriConverter.convertToAndroidContentUri(richContent.getDataUri());
        ClipDescription clipDescription = new ClipDescription(richContent.getDataDetail(), richContent.getMimeTypes());
        Uri linkUri = richContent.getLinkUri();
        if (linkUri != null) {
            uri = android.net.Uri.parse(linkUri.toString());
        }
        InputContentInfo inputContentInfo = new InputContentInfo(convertToAndroidContentUri, clipDescription, uri);
        inputContentInfo.setUriToken(new IInputContentUriToken.Stub() {
            /* class ohos.miscservices.inputmethod.adapter.InputContentInfoAdapter.AnonymousClass1InputContentInfoUriTokenImpl */

            public void take() throws RemoteException {
                HiLog.info(InputContentInfoAdapter.TAG, "UriToken take.", new Object[0]);
                RichContent.this.takeUriPermission();
            }

            public void release() throws RemoteException {
                HiLog.info(InputContentInfoAdapter.TAG, "UriToken release.", new Object[0]);
                RichContent.this.releaseUriPermission();
            }
        });
        return inputContentInfo;
    }
}
