package android.content;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ContentProviderOperation implements Parcelable {
    public static final Creator<ContentProviderOperation> CREATOR = null;
    private static final String TAG = "ContentProviderOperation";
    public static final int TYPE_ASSERT = 4;
    public static final int TYPE_DELETE = 3;
    public static final int TYPE_INSERT = 1;
    public static final int TYPE_UPDATE = 2;
    private final Integer mExpectedCount;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final Map<Integer, Integer> mSelectionArgsBackReferences;
    private final int mType;
    private final Uri mUri;
    private final ContentValues mValues;
    private final ContentValues mValuesBackReferences;
    private final boolean mYieldAllowed;

    public static class Builder {
        private Integer mExpectedCount;
        private String mSelection;
        private String[] mSelectionArgs;
        private Map<Integer, Integer> mSelectionArgsBackReferences;
        private final int mType;
        private final Uri mUri;
        private ContentValues mValues;
        private ContentValues mValuesBackReferences;
        private boolean mYieldAllowed;

        private Builder(int type, Uri uri) {
            if (uri == null) {
                throw new IllegalArgumentException("uri must not be null");
            }
            this.mType = type;
            this.mUri = uri;
        }

        public ContentProviderOperation build() {
            if (this.mType == ContentProviderOperation.TYPE_UPDATE && ((this.mValues == null || this.mValues.size() == 0) && (this.mValuesBackReferences == null || this.mValuesBackReferences.size() == 0))) {
                throw new IllegalArgumentException("Empty values");
            } else if (this.mType != ContentProviderOperation.TYPE_ASSERT || ((this.mValues != null && this.mValues.size() != 0) || ((this.mValuesBackReferences != null && this.mValuesBackReferences.size() != 0) || this.mExpectedCount != null))) {
                return new ContentProviderOperation();
            } else {
                throw new IllegalArgumentException("Empty values");
            }
        }

        public Builder withValueBackReferences(ContentValues backReferences) {
            if (this.mType == ContentProviderOperation.TYPE_INSERT || this.mType == ContentProviderOperation.TYPE_UPDATE || this.mType == ContentProviderOperation.TYPE_ASSERT) {
                this.mValuesBackReferences = backReferences;
                return this;
            }
            throw new IllegalArgumentException("only inserts, updates, and asserts can have value back-references");
        }

        public Builder withValueBackReference(String key, int previousResult) {
            if (this.mType == ContentProviderOperation.TYPE_INSERT || this.mType == ContentProviderOperation.TYPE_UPDATE || this.mType == ContentProviderOperation.TYPE_ASSERT) {
                if (this.mValuesBackReferences == null) {
                    this.mValuesBackReferences = new ContentValues();
                }
                this.mValuesBackReferences.put(key, Integer.valueOf(previousResult));
                return this;
            }
            throw new IllegalArgumentException("only inserts, updates, and asserts can have value back-references");
        }

        public Builder withSelectionBackReference(int selectionArgIndex, int previousResult) {
            if (this.mType == ContentProviderOperation.TYPE_UPDATE || this.mType == ContentProviderOperation.TYPE_DELETE || this.mType == ContentProviderOperation.TYPE_ASSERT) {
                if (this.mSelectionArgsBackReferences == null) {
                    this.mSelectionArgsBackReferences = new HashMap();
                }
                this.mSelectionArgsBackReferences.put(Integer.valueOf(selectionArgIndex), Integer.valueOf(previousResult));
                return this;
            }
            throw new IllegalArgumentException("only updates, deletes, and asserts can have selection back-references");
        }

        public Builder withValues(ContentValues values) {
            if (this.mType == ContentProviderOperation.TYPE_INSERT || this.mType == ContentProviderOperation.TYPE_UPDATE || this.mType == ContentProviderOperation.TYPE_ASSERT) {
                if (this.mValues == null) {
                    this.mValues = new ContentValues();
                }
                this.mValues.putAll(values);
                return this;
            }
            throw new IllegalArgumentException("only inserts, updates, and asserts can have values");
        }

        public Builder withValue(String key, Object value) {
            if (this.mType == ContentProviderOperation.TYPE_INSERT || this.mType == ContentProviderOperation.TYPE_UPDATE || this.mType == ContentProviderOperation.TYPE_ASSERT) {
                if (this.mValues == null) {
                    this.mValues = new ContentValues();
                }
                if (value == null) {
                    this.mValues.putNull(key);
                } else if (value instanceof String) {
                    this.mValues.put(key, (String) value);
                } else if (value instanceof Byte) {
                    this.mValues.put(key, (Byte) value);
                } else if (value instanceof Short) {
                    this.mValues.put(key, (Short) value);
                } else if (value instanceof Integer) {
                    this.mValues.put(key, (Integer) value);
                } else if (value instanceof Long) {
                    this.mValues.put(key, (Long) value);
                } else if (value instanceof Float) {
                    this.mValues.put(key, (Float) value);
                } else if (value instanceof Double) {
                    this.mValues.put(key, (Double) value);
                } else if (value instanceof Boolean) {
                    this.mValues.put(key, (Boolean) value);
                } else if (value instanceof byte[]) {
                    this.mValues.put(key, (byte[]) value);
                } else {
                    throw new IllegalArgumentException("bad value type: " + value.getClass().getName());
                }
                return this;
            }
            throw new IllegalArgumentException("only inserts and updates can have values");
        }

        public Builder withSelection(String selection, String[] selectionArgs) {
            if (this.mType == ContentProviderOperation.TYPE_UPDATE || this.mType == ContentProviderOperation.TYPE_DELETE || this.mType == ContentProviderOperation.TYPE_ASSERT) {
                this.mSelection = selection;
                if (selectionArgs == null) {
                    this.mSelectionArgs = null;
                } else {
                    this.mSelectionArgs = new String[selectionArgs.length];
                    System.arraycopy(selectionArgs, 0, this.mSelectionArgs, 0, selectionArgs.length);
                }
                return this;
            }
            throw new IllegalArgumentException("only updates, deletes, and asserts can have selections");
        }

        public Builder withExpectedCount(int count) {
            if (this.mType == ContentProviderOperation.TYPE_UPDATE || this.mType == ContentProviderOperation.TYPE_DELETE || this.mType == ContentProviderOperation.TYPE_ASSERT) {
                this.mExpectedCount = Integer.valueOf(count);
                return this;
            }
            throw new IllegalArgumentException("only updates, deletes, and asserts can have expected counts");
        }

        public Builder withYieldAllowed(boolean yieldAllowed) {
            this.mYieldAllowed = yieldAllowed;
            return this;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.ContentProviderOperation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.ContentProviderOperation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ContentProviderOperation.<clinit>():void");
    }

    private ContentProviderOperation(Builder builder) {
        this.mType = builder.mType;
        this.mUri = builder.mUri;
        this.mValues = builder.mValues;
        this.mSelection = builder.mSelection;
        this.mSelectionArgs = builder.mSelectionArgs;
        this.mExpectedCount = builder.mExpectedCount;
        this.mSelectionArgsBackReferences = builder.mSelectionArgsBackReferences;
        this.mValuesBackReferences = builder.mValuesBackReferences;
        this.mYieldAllowed = builder.mYieldAllowed;
    }

    private ContentProviderOperation(Parcel source) {
        ContentValues contentValues;
        String readString;
        String[] readStringArray;
        Integer valueOf;
        boolean z;
        Map map = null;
        this.mType = source.readInt();
        this.mUri = (Uri) Uri.CREATOR.createFromParcel(source);
        if (source.readInt() != 0) {
            contentValues = (ContentValues) ContentValues.CREATOR.createFromParcel(source);
        } else {
            contentValues = null;
        }
        this.mValues = contentValues;
        if (source.readInt() != 0) {
            readString = source.readString();
        } else {
            readString = null;
        }
        this.mSelection = readString;
        if (source.readInt() != 0) {
            readStringArray = source.readStringArray();
        } else {
            readStringArray = null;
        }
        this.mSelectionArgs = readStringArray;
        if (source.readInt() != 0) {
            valueOf = Integer.valueOf(source.readInt());
        } else {
            valueOf = null;
        }
        this.mExpectedCount = valueOf;
        if (source.readInt() != 0) {
            contentValues = (ContentValues) ContentValues.CREATOR.createFromParcel(source);
        } else {
            contentValues = null;
        }
        this.mValuesBackReferences = contentValues;
        if (source.readInt() != 0) {
            map = new HashMap();
        }
        this.mSelectionArgsBackReferences = map;
        if (this.mSelectionArgsBackReferences != null) {
            int count = source.readInt();
            for (int i = 0; i < count; i += TYPE_INSERT) {
                this.mSelectionArgsBackReferences.put(Integer.valueOf(source.readInt()), Integer.valueOf(source.readInt()));
            }
        }
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mYieldAllowed = z;
    }

    public ContentProviderOperation(ContentProviderOperation cpo, boolean removeUserIdFromUri) {
        this.mType = cpo.mType;
        if (removeUserIdFromUri) {
            this.mUri = ContentProvider.getUriWithoutUserId(cpo.mUri);
        } else {
            this.mUri = cpo.mUri;
        }
        this.mValues = cpo.mValues;
        this.mSelection = cpo.mSelection;
        this.mSelectionArgs = cpo.mSelectionArgs;
        this.mExpectedCount = cpo.mExpectedCount;
        this.mSelectionArgsBackReferences = cpo.mSelectionArgsBackReferences;
        this.mValuesBackReferences = cpo.mValuesBackReferences;
        this.mYieldAllowed = cpo.mYieldAllowed;
    }

    public ContentProviderOperation getWithoutUserIdInUri() {
        if (ContentProvider.uriHasUserId(this.mUri)) {
            return new ContentProviderOperation(this, true);
        }
        return this;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeInt(this.mType);
        Uri.writeToParcel(dest, this.mUri);
        if (this.mValues != null) {
            dest.writeInt(TYPE_INSERT);
            this.mValues.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (this.mSelection != null) {
            dest.writeInt(TYPE_INSERT);
            dest.writeString(this.mSelection);
        } else {
            dest.writeInt(0);
        }
        if (this.mSelectionArgs != null) {
            dest.writeInt(TYPE_INSERT);
            dest.writeStringArray(this.mSelectionArgs);
        } else {
            dest.writeInt(0);
        }
        if (this.mExpectedCount != null) {
            dest.writeInt(TYPE_INSERT);
            dest.writeInt(this.mExpectedCount.intValue());
        } else {
            dest.writeInt(0);
        }
        if (this.mValuesBackReferences != null) {
            dest.writeInt(TYPE_INSERT);
            this.mValuesBackReferences.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (this.mSelectionArgsBackReferences != null) {
            dest.writeInt(TYPE_INSERT);
            dest.writeInt(this.mSelectionArgsBackReferences.size());
            for (Entry<Integer, Integer> entry : this.mSelectionArgsBackReferences.entrySet()) {
                dest.writeInt(((Integer) entry.getKey()).intValue());
                dest.writeInt(((Integer) entry.getValue()).intValue());
            }
        } else {
            dest.writeInt(0);
        }
        if (this.mYieldAllowed) {
            i = TYPE_INSERT;
        } else {
            i = 0;
        }
        dest.writeInt(i);
    }

    public static Builder newInsert(Uri uri) {
        return new Builder(uri, null);
    }

    public static Builder newUpdate(Uri uri) {
        return new Builder(uri, null);
    }

    public static Builder newDelete(Uri uri) {
        return new Builder(uri, null);
    }

    public static Builder newAssertQuery(Uri uri) {
        return new Builder(uri, null);
    }

    public Uri getUri() {
        return this.mUri;
    }

    public boolean isYieldAllowed() {
        return this.mYieldAllowed;
    }

    public int getType() {
        return this.mType;
    }

    public boolean isInsert() {
        return this.mType == TYPE_INSERT;
    }

    public boolean isDelete() {
        return this.mType == TYPE_DELETE;
    }

    public boolean isUpdate() {
        return this.mType == TYPE_UPDATE;
    }

    public boolean isAssertQuery() {
        return this.mType == TYPE_ASSERT;
    }

    public boolean isWriteOperation() {
        return this.mType == TYPE_DELETE || this.mType == TYPE_INSERT || this.mType == TYPE_UPDATE;
    }

    public boolean isReadOperation() {
        return this.mType == TYPE_ASSERT;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ContentProviderResult apply(ContentProvider provider, ContentProviderResult[] backRefs, int numBackRefs) throws OperationApplicationException {
        ContentValues values = resolveValueBackReferences(backRefs, numBackRefs);
        String[] selectionArgs = resolveSelectionArgsBackReferences(backRefs, numBackRefs);
        if (this.mType == TYPE_INSERT) {
            Uri newUri = provider.insert(this.mUri, values);
            if (newUri != null) {
                return new ContentProviderResult(newUri);
            }
            throw new OperationApplicationException("insert failed");
        }
        int numRows;
        if (this.mType == TYPE_DELETE) {
            numRows = provider.delete(this.mUri, this.mSelection, selectionArgs);
        } else if (this.mType == TYPE_UPDATE) {
            numRows = provider.update(this.mUri, values, this.mSelection, selectionArgs);
        } else if (this.mType == TYPE_ASSERT) {
            String[] strArr = null;
            if (values != null) {
                ArrayList<String> projectionList = new ArrayList();
                for (Entry<String, Object> entry : values.valueSet()) {
                    projectionList.add((String) entry.getKey());
                }
                strArr = (String[]) projectionList.toArray(new String[projectionList.size()]);
            }
            Cursor cursor = provider.query(this.mUri, strArr, this.mSelection, selectionArgs, null);
            try {
                numRows = cursor.getCount();
            } catch (Throwable th) {
                cursor.close();
            }
        } else {
            Log.e(TAG, toString());
            throw new IllegalStateException("bad type, " + this.mType);
        }
        if (this.mExpectedCount == null || this.mExpectedCount.intValue() == numRows) {
            return new ContentProviderResult(numRows);
        }
        Log.e(TAG, toString());
        throw new OperationApplicationException("wrong number of rows: " + numRows);
    }

    public ContentValues resolveValueBackReferences(ContentProviderResult[] backRefs, int numBackRefs) {
        if (this.mValuesBackReferences == null) {
            return this.mValues;
        }
        ContentValues values;
        if (this.mValues == null) {
            values = new ContentValues();
        } else {
            values = new ContentValues(this.mValues);
        }
        for (Entry<String, Object> entry : this.mValuesBackReferences.valueSet()) {
            String key = (String) entry.getKey();
            Integer backRefIndex = this.mValuesBackReferences.getAsInteger(key);
            if (backRefIndex == null) {
                Log.e(TAG, toString());
                throw new IllegalArgumentException("values backref " + key + " is not an integer");
            }
            values.put(key, Long.valueOf(backRefToValue(backRefs, numBackRefs, backRefIndex)));
        }
        return values;
    }

    public String[] resolveSelectionArgsBackReferences(ContentProviderResult[] backRefs, int numBackRefs) {
        if (this.mSelectionArgsBackReferences == null) {
            return this.mSelectionArgs;
        }
        String[] newArgs = new String[this.mSelectionArgs.length];
        System.arraycopy(this.mSelectionArgs, 0, newArgs, 0, this.mSelectionArgs.length);
        for (Entry<Integer, Integer> selectionArgBackRef : this.mSelectionArgsBackReferences.entrySet()) {
            newArgs[((Integer) selectionArgBackRef.getKey()).intValue()] = String.valueOf(backRefToValue(backRefs, numBackRefs, Integer.valueOf(((Integer) selectionArgBackRef.getValue()).intValue())));
        }
        return newArgs;
    }

    public String toString() {
        return "mType: " + this.mType + ", mUri: " + this.mUri + ", mSelection: " + this.mSelection + ", mExpectedCount: " + this.mExpectedCount + ", mYieldAllowed: " + this.mYieldAllowed + ", mValues: " + this.mValues + ", mValuesBackReferences: " + this.mValuesBackReferences + ", mSelectionArgsBackReferences: " + this.mSelectionArgsBackReferences;
    }

    private long backRefToValue(ContentProviderResult[] backRefs, int numBackRefs, Integer backRefIndex) {
        if (backRefIndex.intValue() >= numBackRefs) {
            Log.e(TAG, toString());
            throw new ArrayIndexOutOfBoundsException("asked for back ref " + backRefIndex + " but there are only " + numBackRefs + " back refs");
        }
        ContentProviderResult backRef = backRefs[backRefIndex.intValue()];
        if (backRef.uri != null) {
            return ContentUris.parseId(backRef.uri);
        }
        return (long) backRef.count.intValue();
    }

    public int describeContents() {
        return 0;
    }
}
