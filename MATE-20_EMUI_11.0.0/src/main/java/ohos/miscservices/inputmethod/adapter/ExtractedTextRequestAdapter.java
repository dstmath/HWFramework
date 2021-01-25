package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.ExtractedTextRequest;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingTextRequest;

public class ExtractedTextRequestAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ExtractedTextRequestAdapter");

    private ExtractedTextRequestAdapter() {
    }

    public static EditingTextRequest convertToEditingTextRequest(ExtractedTextRequest extractedTextRequest) {
        if (extractedTextRequest == null) {
            HiLog.error(TAG, "extractedTextRequest is null.", new Object[0]);
            return null;
        }
        EditingTextRequest editingTextRequest = new EditingTextRequest();
        editingTextRequest.setToken(extractedTextRequest.token);
        editingTextRequest.setMaxLines(extractedTextRequest.hintMaxLines);
        editingTextRequest.setMaxChars(extractedTextRequest.hintMaxChars);
        return editingTextRequest;
    }

    public static EditingCapability convertToEditingCapability(ExtractedTextRequest extractedTextRequest) {
        if (extractedTextRequest == null) {
            HiLog.error(TAG, "extractedTextRequest is null.", new Object[0]);
            return null;
        }
        EditingCapability editingCapability = new EditingCapability();
        editingCapability.setToken(extractedTextRequest.token);
        editingCapability.setMaxLines(extractedTextRequest.hintMaxLines);
        editingCapability.setMaxChars(extractedTextRequest.hintMaxChars);
        return editingCapability;
    }

    public static ExtractedTextRequest convertToExtractedTextRequest(EditingTextRequest editingTextRequest) {
        if (editingTextRequest == null) {
            HiLog.error(TAG, "editingTextRequest is null.", new Object[0]);
            return null;
        }
        ExtractedTextRequest extractedTextRequest = new ExtractedTextRequest();
        extractedTextRequest.token = editingTextRequest.getToken();
        extractedTextRequest.hintMaxLines = editingTextRequest.getMaxLines();
        extractedTextRequest.hintMaxChars = editingTextRequest.getMaxChars();
        return extractedTextRequest;
    }

    public static ExtractedTextRequest convertToExtractedTextRequest(EditingCapability editingCapability) {
        if (editingCapability == null) {
            HiLog.error(TAG, "editingCapability is null.", new Object[0]);
            return null;
        }
        ExtractedTextRequest extractedTextRequest = new ExtractedTextRequest();
        extractedTextRequest.token = editingCapability.getToken();
        extractedTextRequest.hintMaxLines = editingCapability.getMaxLines();
        extractedTextRequest.hintMaxChars = editingCapability.getMaxChars();
        return extractedTextRequest;
    }
}
