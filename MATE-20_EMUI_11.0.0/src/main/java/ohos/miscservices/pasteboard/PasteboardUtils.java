package ohos.miscservices.pasteboard;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.abilityshell.utils.IntentConverter;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ShellInfo;
import ohos.miscservices.pasteboard.PasteData;
import ohos.net.UriConverter;
import ohos.utils.adapter.PacMapUtils;

public class PasteboardUtils {
    static final String[] A_MIMETYPES = {PasteData.MIMETYPE_TEXT_PLAIN, PasteData.MIMETYPE_TEXT_HTML, "text/uri-list", "text/vnd.android.intent"};
    static final String CONTENT_PROVIDER_SCHEME = "content";
    static final String DATA_ABILITY_SCHEME = "dataability";
    static final int MAX_TYPE_CONVERT = 4;
    static final String[] Z_MIMETYPES = {PasteData.MIMETYPE_TEXT_PLAIN, PasteData.MIMETYPE_TEXT_HTML, PasteData.MIMETYPE_TEXT_URI, PasteData.MIMETYPE_TEXT_INTENT};

    private PasteboardUtils() {
    }

    public static PasteData convertFromClipData(ClipData clipData) {
        if (clipData == null) {
            return null;
        }
        PasteData pasteData = new PasteData();
        PersistableBundle extras = clipData.getDescription().getExtras();
        if (extras != null) {
            pasteData.getProperty().setAdditions(PacMapUtils.convertFromBundle(new Bundle(extras)));
        }
        pasteData.getProperty().setTag(clipData.getDescription().getLabel());
        pasteData.getProperty().setTimestamp(clipData.getDescription().getTimestamp());
        int itemCount = clipData.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            for (PasteData.Record record : convertFromItem(clipData.getItemAt(i))) {
                pasteData.addRecord(record);
            }
        }
        return pasteData;
    }

    public static ClipData convertFromPasteData(PasteData pasteData) {
        if (pasteData == null) {
            return null;
        }
        PasteData.DataProperty property = pasteData.getProperty();
        CharSequence tag = property.getTag();
        List<String> mimeTypes = property.getMimeTypes();
        for (int length = Z_MIMETYPES.length - 1; length >= 0; length--) {
            if (mimeTypes.contains(Z_MIMETYPES[length])) {
                mimeTypes.remove(Z_MIMETYPES[length]);
                mimeTypes.add(A_MIMETYPES[length]);
            }
        }
        ClipDescription clipDescription = new ClipDescription(tag, (String[]) mimeTypes.toArray(new String[0]));
        clipDescription.setExtras(new PersistableBundle(PacMapUtils.convertIntoBundle(property.getAdditions())));
        clipDescription.setTimestamp(property.getTimestamp());
        int recordCount = pasteData.getRecordCount();
        ArrayList arrayList = new ArrayList(recordCount);
        for (int i = 0; i < recordCount; i++) {
            arrayList.add(convertFromRecord(pasteData.getRecordAt(i)));
        }
        return new ClipData(clipDescription, arrayList);
    }

    public static ClipData.Item convertFromRecord(PasteData.Record record) {
        Uri uri;
        Intent intent = null;
        if (record == null) {
            return null;
        }
        String mimeType = record.getMimeType();
        char c = 65535;
        switch (mimeType.hashCode()) {
            case -1082243251:
                if (mimeType.equals(PasteData.MIMETYPE_TEXT_HTML)) {
                    c = 1;
                    break;
                }
                break;
            case -1004729974:
                if (mimeType.equals(PasteData.MIMETYPE_TEXT_URI)) {
                    c = 3;
                    break;
                }
                break;
            case 37675595:
                if (mimeType.equals(PasteData.MIMETYPE_TEXT_INTENT)) {
                    c = 2;
                    break;
                }
                break;
            case 817335912:
                if (mimeType.equals(PasteData.MIMETYPE_TEXT_PLAIN)) {
                    c = 0;
                    break;
                }
                break;
        }
        if (c == 0) {
            return new ClipData.Item(record.getPlainText());
        }
        if (c == 1) {
            return new ClipData.Item(record.getPlainText(), record.getHtmlText());
        }
        if (c == 2) {
            Optional createAndroidIntent = IntentConverter.createAndroidIntent(record.getIntent(), (ShellInfo) null);
            if (createAndroidIntent.isPresent()) {
                intent = (Intent) createAndroidIntent.get();
            }
            return new ClipData.Item(intent);
        } else if (c != 3) {
            return null;
        } else {
            ohos.utils.net.Uri uri2 = record.getUri();
            if (DATA_ABILITY_SCHEME.equals(uri2.getScheme())) {
                uri = UriConverter.convertToAndroidContentUri(uri2);
            } else {
                uri = UriConverter.convertToAndroidUri(uri2);
            }
            return new ClipData.Item(uri);
        }
    }

    public static List<PasteData.Record> convertFromItem(ClipData.Item item) {
        ohos.utils.net.Uri uri;
        ArrayList arrayList = new ArrayList(4);
        if (item == null) {
            return arrayList;
        }
        if (item.getText() == null || !item.getText().toString().isEmpty() || item.getHtmlText() == null) {
            if (item.getText() != null) {
                arrayList.add(PasteData.Record.createPlainTextRecord(item.getText()));
            }
            if (item.getHtmlText() != null) {
                arrayList.add(PasteData.Record.createHtmlTextRecord(item.getHtmlText()));
            }
        } else {
            arrayList.add(PasteData.Record.createHtmlTextRecord(item.getHtmlText()));
        }
        if (item.getUri() != null) {
            Uri uri2 = item.getUri();
            if (CONTENT_PROVIDER_SCHEME.equals(uri2.getScheme())) {
                uri = UriConverter.convertToZidaneContentUri(uri2, "");
            } else {
                uri = UriConverter.convertToZidaneUri(uri2);
            }
            arrayList.add(PasteData.Record.createUriRecord(uri));
        }
        if (item.getIntent() != null) {
            ohos.aafwk.content.Intent intent = null;
            Optional createZidaneIntent = IntentConverter.createZidaneIntent(item.getIntent(), (AbilityInfo) null);
            if (createZidaneIntent.isPresent()) {
                intent = (ohos.aafwk.content.Intent) createZidaneIntent.get();
            }
            arrayList.add(PasteData.Record.createIntentRecord(intent));
        }
        return arrayList;
    }
}
