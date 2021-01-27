package ohos.miscservices.pasteboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class PasteData implements Sequenceable {
    public static final int MAX_RECORD_NUM = 128;
    public static final String MIMETYPE_TEXT_HTML = "text/html";
    public static final String MIMETYPE_TEXT_INTENT = "text/ohos.intent";
    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    public static final String MIMETYPE_TEXT_URI = "text/uri";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "PasteData");
    private final DataProperty dataProperty = new DataProperty();
    private final ArrayList<Record> records = new ArrayList<>();

    public static PasteData creatPlainTextData(CharSequence charSequence) {
        if (charSequence == null) {
            return null;
        }
        PasteData pasteData = new PasteData();
        pasteData.addTextRecord(charSequence);
        return pasteData;
    }

    public static PasteData creatHtmlData(String str) {
        if (str == null) {
            return null;
        }
        PasteData pasteData = new PasteData();
        pasteData.addHtmlRecord(str);
        return pasteData;
    }

    public static PasteData creatUriData(Uri uri) {
        if (uri == null) {
            return null;
        }
        PasteData pasteData = new PasteData();
        pasteData.addUriRecord(uri);
        return pasteData;
    }

    public static PasteData creatIntentData(Intent intent) {
        if (intent == null) {
            return null;
        }
        PasteData pasteData = new PasteData();
        pasteData.addIntentRecord(intent);
        return pasteData;
    }

    public String getPrimaryMimeType() {
        return getRecordCount() > 0 ? getRecordAt(0).getMimeType() : "";
    }

    public CharSequence getPrimaryText() {
        if (getRecordCount() > 0) {
            return getRecordAt(0).getPlainText();
        }
        return null;
    }

    public String getPrimaryHtml() {
        if (getRecordCount() > 0) {
            return getRecordAt(0).getHtmlText();
        }
        return null;
    }

    public Uri getPrimaryUri() {
        if (getRecordCount() > 0) {
            return getRecordAt(0).getUri();
        }
        return null;
    }

    public Intent getPrimaryIntent() {
        if (getRecordCount() > 0) {
            return getRecordAt(0).getIntent();
        }
        return null;
    }

    public void addTextRecord(CharSequence charSequence) {
        if (charSequence != null && getRecordCount() < 128) {
            this.records.add(Record.createPlainTextRecord(charSequence));
            this.dataProperty.addMimeType(MIMETYPE_TEXT_PLAIN);
        }
    }

    public void addHtmlRecord(String str) {
        if (str != null && getRecordCount() < 128) {
            this.records.add(Record.createHtmlTextRecord(str));
            this.dataProperty.addMimeType(MIMETYPE_TEXT_HTML);
        }
    }

    public void addUriRecord(Uri uri) {
        if (uri != null && getRecordCount() < 128) {
            this.records.add(Record.createUriRecord(uri));
            this.dataProperty.addMimeType(MIMETYPE_TEXT_URI);
        }
    }

    public void addIntentRecord(Intent intent) {
        if (intent != null && getRecordCount() < 128) {
            this.records.add(Record.createIntentRecord(intent));
            this.dataProperty.addMimeType(MIMETYPE_TEXT_INTENT);
        }
    }

    public void addRecord(Record record) {
        if (record != null && getRecordCount() < 128) {
            this.records.add(record);
            this.dataProperty.addMimeType(record.getMimeType());
        }
    }

    public int getRecordCount() {
        return this.records.size();
    }

    public Record getRecordAt(int i) {
        if (i < this.records.size() && i >= 0) {
            return this.records.get(i);
        }
        HiLog.error(TAG, "Get record failed due to out of bound!", new Object[0]);
        return null;
    }

    public boolean removeRecordAt(int i) {
        if (i >= this.records.size() || i < 0) {
            HiLog.error(TAG, "Remove record failed due to out of bound!", new Object[0]);
            return false;
        }
        this.records.remove(i);
        return true;
    }

    public boolean replaceRecordAt(int i, Record record) {
        if (i >= this.records.size() || i < 0) {
            HiLog.error(TAG, "Replace record failed due to out of bound!", new Object[0]);
            return false;
        }
        this.records.set(i, record);
        return true;
    }

    public List<String> getMimeTypes() {
        return this.dataProperty.getMimeTypes();
    }

    public boolean hasMimeType(String str) {
        return this.dataProperty.hasMimeType(str);
    }

    public DataProperty getProperty() {
        return this.dataProperty;
    }

    public CharSequence getTag() {
        return this.dataProperty.getTag();
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(getRecordCount());
        Iterator<Record> it = this.records.iterator();
        while (it.hasNext()) {
            if (!it.next().marshalling(parcel)) {
                HiLog.error(TAG, "marshalling PasteData failed when marshalling record!", new Object[0]);
                return false;
            }
        }
        if (this.dataProperty.marshalling(parcel)) {
            return true;
        }
        HiLog.error(TAG, "marshalling PasteData failed when marshalling dataProperty!", new Object[0]);
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt > 128) {
            HiLog.error(TAG, "unmarshalling PasteData failed when read record num!", new Object[0]);
            return false;
        }
        this.records.clear();
        for (int i = 0; i < readInt; i++) {
            Record record = new Record(null, null, null, null, null);
            if (!record.unmarshalling(parcel)) {
                HiLog.error(TAG, "unmarshalling PasteData failed when unmarshalling record num: %{public}d", Integer.valueOf(i));
                return false;
            }
            addRecord(record);
        }
        if (this.dataProperty.unmarshalling(parcel)) {
            return true;
        }
        HiLog.error(TAG, "unmarshalling PasteData failed when unmarshalling DataProperty", new Object[0]);
        return false;
    }

    public static class Record implements Sequenceable {
        String htmlText;
        Intent intent;
        String mimeType;
        CharSequence text;
        Uri uri;

        private Record(CharSequence charSequence, String str, Uri uri2, Intent intent2, String str2) {
            this.text = charSequence;
            this.htmlText = str;
            this.intent = intent2;
            this.uri = uri2;
            this.mimeType = str2;
        }

        public static Record createPlainTextRecord(CharSequence charSequence) {
            if (charSequence == null) {
                return null;
            }
            return new Record(charSequence, null, null, null, PasteData.MIMETYPE_TEXT_PLAIN);
        }

        public static Record createHtmlTextRecord(String str) {
            if (str == null) {
                return null;
            }
            return new Record("", str, null, null, PasteData.MIMETYPE_TEXT_HTML);
        }

        public static Record createUriRecord(Uri uri2) {
            if (uri2 == null) {
                return null;
            }
            return new Record(null, null, uri2, null, PasteData.MIMETYPE_TEXT_URI);
        }

        public static Record createIntentRecord(Intent intent2) {
            if (intent2 == null) {
                return null;
            }
            return new Record(null, null, null, intent2, PasteData.MIMETYPE_TEXT_INTENT);
        }

        public CharSequence getPlainText() {
            return this.text;
        }

        public String getHtmlText() {
            return this.htmlText;
        }

        public Uri getUri() {
            return this.uri;
        }

        public Intent getIntent() {
            return this.intent;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public CharSequence convertToText(Context context) {
            char c;
            String mimeType2 = getMimeType();
            switch (mimeType2.hashCode()) {
                case -1082243251:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_HTML)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1004729974:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_URI)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 37675595:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_INTENT)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 817335912:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_PLAIN)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                return getPlainText();
            }
            if (c == 1) {
                return PasteboardProxy.htmlToPlainText(getHtmlText());
            }
            if (c == 2) {
                return PasteboardProxy.uriToPlainText(context, getUri());
            }
            if (c == 3) {
                return getIntent().toUri();
            }
            HiLog.error(PasteData.TAG, "Conversion failed", new Object[0]);
            return "";
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // ohos.utils.Sequenceable
        public boolean marshalling(Parcel parcel) {
            char c;
            parcel.writeString(this.mimeType);
            String mimeType2 = getMimeType();
            switch (mimeType2.hashCode()) {
                case -1082243251:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_HTML)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1004729974:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_URI)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 37675595:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_INTENT)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 817335912:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_PLAIN)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                parcel.writeString(this.text.toString());
            } else if (c == 1) {
                parcel.writeString(this.htmlText);
            } else if (!(c == 2 || c == 3)) {
                HiLog.error(PasteData.TAG, "Unknown Mimetype", new Object[0]);
                return false;
            }
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // ohos.utils.Sequenceable
        public boolean unmarshalling(Parcel parcel) {
            char c;
            this.mimeType = parcel.readString();
            String mimeType2 = getMimeType();
            switch (mimeType2.hashCode()) {
                case -1082243251:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_HTML)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1004729974:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_URI)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 37675595:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_INTENT)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 817335912:
                    if (mimeType2.equals(PasteData.MIMETYPE_TEXT_PLAIN)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                this.text = parcel.readString();
            } else if (c == 1) {
                this.htmlText = parcel.readString();
            } else if (!(c == 2 || c == 3)) {
                HiLog.error(PasteData.TAG, "Unknown Mimetype", new Object[0]);
                return false;
            }
            return true;
        }
    }

    public static class DataProperty implements Sequenceable {
        private long expiration;
        private PacMap extraProps = new PacMap();
        private boolean isLocalOnly;
        private List<String> mimeTypes = new ArrayList();
        private CharSequence tag;
        private long timeStamp;

        DataProperty() {
        }

        public List<String> getMimeTypes() {
            return new ArrayList(this.mimeTypes);
        }

        public boolean hasMimeType(String str) {
            return this.mimeTypes.contains(str);
        }

        public long getTimestamp() {
            return this.timeStamp;
        }

        public void setTimestamp(long j) {
            this.timeStamp = j;
        }

        public long getExpiration() {
            return this.expiration;
        }

        public boolean setExpiration(long j) {
            this.expiration = j;
            return true;
        }

        public CharSequence getTag() {
            CharSequence charSequence = this.tag;
            return charSequence != null ? charSequence : "";
        }

        public void setTag(CharSequence charSequence) {
            this.tag = charSequence;
        }

        public PacMap getAdditions() {
            return this.extraProps;
        }

        public void setAdditions(PacMap pacMap) {
            this.extraProps = pacMap;
        }

        public boolean isLocalOnly() {
            return this.isLocalOnly;
        }

        public void setLocalOnly(boolean z) {
            this.isLocalOnly = z;
        }

        public void addMimeType(String str) {
            if (!this.mimeTypes.contains(str)) {
                this.mimeTypes.add(str);
            }
        }

        @Override // ohos.utils.Sequenceable
        public boolean marshalling(Parcel parcel) {
            parcel.writeInt(this.mimeTypes.size());
            for (String str : this.mimeTypes) {
                parcel.writeString(str);
            }
            parcel.writeString(getTag().toString());
            parcel.writeBoolean(this.isLocalOnly);
            parcel.writeLong(this.timeStamp);
            parcel.writeLong(this.expiration);
            return true;
        }

        @Override // ohos.utils.Sequenceable
        public boolean unmarshalling(Parcel parcel) {
            int readInt = parcel.readInt();
            if (readInt > 4) {
                HiLog.error(PasteData.TAG, "unmarshalling DataProperty failed when read mimeTypes num!", new Object[0]);
                return false;
            }
            for (int i = 0; i < readInt; i++) {
                addMimeType(parcel.readString());
            }
            this.tag = parcel.readString();
            this.isLocalOnly = parcel.readBoolean();
            this.timeStamp = parcel.readLong();
            this.expiration = parcel.readLong();
            return true;
        }
    }
}
