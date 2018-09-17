package com.android.internal.content;

import android.os.ParcelFileDescriptor.OnCloseListener;
import java.io.File;
import java.io.IOException;

final /* synthetic */ class -$Lambda$qCDQZ4U5of2rgsneNEo3bc5KTII implements OnCloseListener {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    private final /* synthetic */ void $m$0(IOException arg0) {
        ((FileSystemProvider) this.-$f0).lambda$-com_android_internal_content_FileSystemProvider_13885((File) this.-$f1, arg0);
    }

    public /* synthetic */ -$Lambda$qCDQZ4U5of2rgsneNEo3bc5KTII(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void onClose(IOException iOException) {
        $m$0(iOException);
    }
}
