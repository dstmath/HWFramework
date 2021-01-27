package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.ExtractedTextRequest;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;

public class ExtractedTextRequestAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ExtractedTextRequestAdapter");

    private ExtractedTextRequestAdapter() {
    }

    public static EditingCapability convertToEditingCapability(ExtractedTextRequest extractedTextRequest) {
        if (extractedTextRequest == null) {
            HiLog.error(TAG, "extractedTextRequest is null.", new Object[0]);
            return null;
        }
        EditingCapability editingCapability = new EditingCapability();
        editingCapability.setMaxLines(extractedTextRequest.hintMaxLines);
        editingCapability.setMaxChars(extractedTextRequest.hintMaxChars);
        return editingCapability;
    }

    public static EditingCapability convertToEditingCapability(ExtractedTextRequest extractedTextRequest, int i) {
        if (extractedTextRequest == null) {
            HiLog.error(TAG, "extractedTextRequest is null.", new Object[0]);
            return null;
        }
        EditingCapability editingCapability = new EditingCapability();
        editingCapability.setMaxLines(extractedTextRequest.hintMaxLines);
        editingCapability.setMaxChars(extractedTextRequest.hintMaxChars);
        return editingCapability;
    }

    public static ExtractedTextRequest convertToExtractedTextRequest(EditingCapability editingCapability) {
        if (editingCapability == null) {
            HiLog.error(TAG, "editingCapability is null.", new Object[0]);
            return null;
        }
        ExtractedTextRequest extractedTextRequest = new ExtractedTextRequest();
        extractedTextRequest.hintMaxLines = editingCapability.getMaxLines();
        extractedTextRequest.hintMaxChars = editingCapability.getMaxChars();
        return extractedTextRequest;
    }

    public static ExtractedTextRequest convertToExtractedTextRequest(EditingCapability editingCapability, int i) {
        if (editingCapability == null) {
            HiLog.error(TAG, "editingCapability is null.", new Object[0]);
            return null;
        }
        ExtractedTextRequest extractedTextRequest = new ExtractedTextRequest();
        extractedTextRequest.token = i;
        extractedTextRequest.hintMaxLines = editingCapability.getMaxLines();
        extractedTextRequest.hintMaxChars = editingCapability.getMaxChars();
        return extractedTextRequest;
    }
}
