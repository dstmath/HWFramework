package ohos.ace.plugin.clipboard;

import com.huawei.ace.plugin.clipboard.ClipboardPluginBase;
import ohos.app.Context;
import ohos.miscservices.pasteboard.PasteData;
import ohos.miscservices.pasteboard.SystemPasteboard;

public class ClipboardPlugin extends ClipboardPluginBase {
    private static final String LOG_TAG = "Ace_Clipboard";
    private final Context context;
    private final SystemPasteboard systemPasteboard = SystemPasteboard.getSystemPasteboard(this.context);

    public ClipboardPlugin(Context context2) {
        this.context = context2;
        nativeInit();
    }

    @Override // com.huawei.ace.plugin.clipboard.ClipboardPluginBase
    public String getData() {
        PasteData pasteData;
        SystemPasteboard systemPasteboard2 = this.systemPasteboard;
        return (systemPasteboard2 == null || !systemPasteboard2.hasPasteData() || (pasteData = this.systemPasteboard.getPasteData()) == null || pasteData.getRecordCount() <= 0) ? "" : pasteData.getRecordAt(0).getPlainText().toString();
    }

    @Override // com.huawei.ace.plugin.clipboard.ClipboardPluginBase
    public void setData(String str) {
        if (this.systemPasteboard != null) {
            PasteData pasteData = new PasteData();
            pasteData.addTextRecord(str);
            this.systemPasteboard.setPasteData(pasteData);
        }
    }

    @Override // com.huawei.ace.plugin.clipboard.ClipboardPluginBase
    public void clear() {
        SystemPasteboard systemPasteboard2 = this.systemPasteboard;
        if (systemPasteboard2 != null) {
            systemPasteboard2.clear();
        }
    }
}
