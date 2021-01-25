package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.InputContentInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.implement.UriPermissionProxy;
import ohos.miscservices.inputmethod.implement.UriPermissionSkeleton;
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
        Uri parse = Uri.parse(inputContentInfo.getContentUri().toString());
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
        UriPermissionProxy uriPermissionProxy = new UriPermissionProxy(new UriPermissionSkeleton(DESCRIPTOR) {
            /* class ohos.miscservices.inputmethod.adapter.InputContentInfoAdapter.AnonymousClass1InterfacePermission */

            @Override // ohos.miscservices.inputmethod.IUriPermission
            public void take() {
                inputContentInfo.requestPermission();
                HiLog.debug(InputContentInfoAdapter.TAG, "inputContentInfo requestPermission.", new Object[0]);
            }

            @Override // ohos.miscservices.inputmethod.IUriPermission
            public void release() {
                inputContentInfo.releasePermission();
                HiLog.debug(InputContentInfoAdapter.TAG, "inputContentInfo releasePermission.", new Object[0]);
            }
        }.asObject());
        RichContent richContent = new RichContent(parse, uri, strArr, charSequence);
        richContent.setUriPermission(uriPermissionProxy);
        return richContent;
    }
}
