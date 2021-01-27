package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.ExtractedText;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;

public class ExtractedTextAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ExtractedTextAdapter");

    private ExtractedTextAdapter() {
    }

    public static EditingText convertToEditingText(ExtractedText extractedText) {
        String str;
        String str2 = null;
        if (extractedText == null) {
            HiLog.error(TAG, "extractedText is null.", new Object[0]);
            return null;
        }
        if (extractedText.text == null) {
            str = null;
        } else {
            str = extractedText.text.toString();
        }
        if (extractedText.hint != null) {
            str2 = extractedText.hint.toString();
        }
        EditingText editingText = new EditingText();
        editingText.setTextContent(str);
        editingText.setOffset(extractedText.startOffset);
        editingText.setChangedStart(extractedText.partialStartOffset);
        editingText.setChangedEnd(extractedText.partialEndOffset);
        editingText.setSelectionStart(extractedText.selectionStart);
        editingText.setSelectionEnd(extractedText.selectionEnd);
        editingText.setFlags(extractedText.flags);
        editingText.setPrompt(str2);
        return editingText;
    }

    public static ExtractedText convertToExtractedText(EditingText editingText) {
        if (editingText == null) {
            HiLog.error(TAG, "extractedText is null.", new Object[0]);
            return null;
        }
        ExtractedText extractedText = new ExtractedText();
        extractedText.text = editingText.getTextContent();
        extractedText.startOffset = editingText.getOffset();
        extractedText.partialStartOffset = editingText.getChangedStart();
        extractedText.partialEndOffset = editingText.getChangedEnd();
        extractedText.selectionStart = editingText.getSelectionStart();
        extractedText.selectionEnd = editingText.getSelectionEnd();
        extractedText.flags = editingText.getFlags();
        extractedText.hint = editingText.getPrompt();
        return extractedText;
    }
}
