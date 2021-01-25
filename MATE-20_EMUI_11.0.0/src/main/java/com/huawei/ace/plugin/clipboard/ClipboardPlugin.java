package com.huawei.ace.plugin.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.View;
import com.huawei.ace.runtime.ALog;

public class ClipboardPlugin extends ClipboardPluginBase {
    private static final String LOG_TAG = "Ace_Clipboard";
    private final ClipboardManager clipManager;
    private final View view;

    public ClipboardPlugin(View view2) {
        this.view = view2;
        if (view2 == null || view2.getContext() == null) {
            ALog.e(LOG_TAG, "View or context is null");
            this.clipManager = null;
        } else {
            Object systemService = view2.getContext().getSystemService("clipboard");
            if (systemService instanceof ClipboardManager) {
                this.clipManager = (ClipboardManager) systemService;
            } else {
                ALog.e(LOG_TAG, "Unable to get CLIPBOARD_SERVICE");
                this.clipManager = null;
            }
        }
        nativeInit();
    }

    @Override // com.huawei.ace.plugin.clipboard.ClipboardPluginBase
    public String getData() {
        ClipData primaryClip;
        ClipboardManager clipboardManager = this.clipManager;
        return (clipboardManager == null || (primaryClip = clipboardManager.getPrimaryClip()) == null || primaryClip.getItemCount() <= 0 || primaryClip.getItemAt(0).getText() == null) ? "" : primaryClip.getItemAt(0).getText().toString();
    }

    @Override // com.huawei.ace.plugin.clipboard.ClipboardPluginBase
    public void setData(String str) {
        if (this.clipManager != null) {
            this.clipManager.setPrimaryClip(ClipData.newPlainText(null, str));
        }
    }

    @Override // com.huawei.ace.plugin.clipboard.ClipboardPluginBase
    public void clear() {
        ClipboardManager clipboardManager = this.clipManager;
        if (clipboardManager != null) {
            clipboardManager.clearPrimaryClip();
        }
    }
}
