package android.content;

import android.annotation.UnsupportedAppUsage;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContentProviderOperation implements Parcelable {
    public static final Parcelable.Creator<ContentProviderOperation> CREATOR = new Parcelable.Creator<ContentProviderOperation>() {
        /* class android.content.ContentProviderOperation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ContentProviderOperation createFromParcel(Parcel source) {
            return new ContentProviderOperation(source);
        }

        @Override // android.os.Parcelable.Creator
        public ContentProviderOperation[] newArray(int size) {
            return new ContentProviderOperation[size];
        }
    };
    private static final String TAG = "ContentProviderOperation";
    public static final int TYPE_ASSERT = 4;
    @UnsupportedAppUsage
    public static final int TYPE_DELETE = 3;
    @UnsupportedAppUsage
    public static final int TYPE_INSERT = 1;
    @UnsupportedAppUsage
    public static final int TYPE_UPDATE = 2;
    private final Integer mExpectedCount;
    private final boolean mFailureAllowed;
    @UnsupportedAppUsage
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final Map<Integer, Integer> mSelectionArgsBackReferences;
    @UnsupportedAppUsage
    private final int mType;
    @UnsupportedAppUsage
    private final Uri mUri;
    private final ContentValues mValues;
    private final ContentValues mValuesBackReferences;
    private final boolean mYieldAllowed;

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
        this.mFailureAllowed = builder.mFailureAllowed;
    }

    private ContentProviderOperation(Parcel source) {
        this.mType = source.readInt();
        this.mUri = Uri.CREATOR.createFromParcel(source);
        HashMap hashMap = null;
        this.mValues = source.readInt() != 0 ? ContentValues.CREATOR.createFromParcel(source) : null;
        this.mSelection = source.readInt() != 0 ? source.readString() : null;
        this.mSelectionArgs = source.readInt() != 0 ? source.readStringArray() : null;
        this.mExpectedCount = source.readInt() != 0 ? Integer.valueOf(source.readInt()) : null;
        this.mValuesBackReferences = source.readInt() != 0 ? ContentValues.CREATOR.createFromParcel(source) : null;
        this.mSelectionArgsBackReferences = source.readInt() != 0 ? new HashMap() : hashMap;
        if (this.mSelectionArgsBackReferences != null) {
            int count = source.readInt();
            for (int i = 0; i < count; i++) {
                this.mSelectionArgsBackReferences.put(Integer.valueOf(source.readInt()), Integer.valueOf(source.readInt()));
            }
        }
        boolean z = false;
        this.mYieldAllowed = source.readInt() != 0;
        this.mFailureAllowed = source.readInt() != 0 ? true : z;
    }

    public ContentProviderOperation(ContentProviderOperation cpo, Uri withUri) {
        this.mType = cpo.mType;
        this.mUri = withUri;
        this.mValues = cpo.mValues;
        this.mSelection = cpo.mSelection;
        this.mSelectionArgs = cpo.mSelectionArgs;
        this.mExpectedCount = cpo.mExpectedCount;
        this.mSelectionArgsBackReferences = cpo.mSelectionArgsBackReferences;
        this.mValuesBackReferences = cpo.mValuesBackReferences;
        this.mYieldAllowed = cpo.mYieldAllowed;
        this.mFailureAllowed = cpo.mFailureAllowed;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        Uri.writeToParcel(dest, this.mUri);
        if (this.mValues != null) {
            dest.writeInt(1);
            this.mValues.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (this.mSelection != null) {
            dest.writeInt(1);
            dest.writeString(this.mSelection);
        } else {
            dest.writeInt(0);
        }
        if (this.mSelectionArgs != null) {
            dest.writeInt(1);
            dest.writeStringArray(this.mSelectionArgs);
        } else {
            dest.writeInt(0);
        }
        if (this.mExpectedCount != null) {
            dest.writeInt(1);
            dest.writeInt(this.mExpectedCount.intValue());
        } else {
            dest.writeInt(0);
        }
        if (this.mValuesBackReferences != null) {
            dest.writeInt(1);
            this.mValuesBackReferences.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (this.mSelectionArgsBackReferences != null) {
            dest.writeInt(1);
            dest.writeInt(this.mSelectionArgsBackReferences.size());
            for (Map.Entry<Integer, Integer> entry : this.mSelectionArgsBackReferences.entrySet()) {
                dest.writeInt(entry.getKey().intValue());
                dest.writeInt(entry.getValue().intValue());
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mYieldAllowed ? 1 : 0);
        dest.writeInt(this.mFailureAllowed ? 1 : 0);
    }

    public static Builder newInsert(Uri uri) {
        return new Builder(1, uri);
    }

    public static Builder newUpdate(Uri uri) {
        return new Builder(2, uri);
    }

    public static Builder newDelete(Uri uri) {
        return new Builder(3, uri);
    }

    public static Builder newAssertQuery(Uri uri) {
        return new Builder(4, uri);
    }

    public Uri getUri() {
        return this.mUri;
    }

    public boolean isYieldAllowed() {
        return this.mYieldAllowed;
    }

    public boolean isFailureAllowed() {
        return this.mFailureAllowed;
    }

    @UnsupportedAppUsage
    public int getType() {
        return this.mType;
    }

    public boolean isInsert() {
        return this.mType == 1;
    }

    public boolean isDelete() {
        return this.mType == 3;
    }

    public boolean isUpdate() {
        return this.mType == 2;
    }

    public boolean isAssertQuery() {
        return this.mType == 4;
    }

    public boolean isWriteOperation() {
        int i = this.mType;
        return i == 3 || i == 1 || i == 2;
    }

    public boolean isReadOperation() {
        return this.mType == 4;
    }

    public ContentProviderResult apply(ContentProvider provider, ContentProviderResult[] backRefs, int numBackRefs) throws OperationApplicationException {
        if (!this.mFailureAllowed) {
            return applyInternal(provider, backRefs, numBackRefs);
        }
        try {
            return applyInternal(provider, backRefs, numBackRefs);
        } catch (Exception e) {
            return new ContentProviderResult(e.getMessage());
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x009d, code lost:
        if (r8 != null) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a3, code lost:
        if (r1.moveToNext() == false) goto L_0x00e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a5, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a7, code lost:
        if (r3 >= r8.length) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a9, code lost:
        r4 = r1.getString(r3);
        r5 = r0.getAsString(r8[r3]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b7, code lost:
        if (android.text.TextUtils.equals(r4, r5) == false) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b9, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e4, code lost:
        throw new android.content.OperationApplicationException("Found value " + r4 + " when expected " + r5 + " for column " + r8[r3]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e6, code lost:
        r1.close();
        r1 = r2;
     */
    private ContentProviderResult applyInternal(ContentProvider provider, ContentProviderResult[] backRefs, int numBackRefs) throws OperationApplicationException {
        int numRows;
        String[] projection;
        ContentValues values = resolveValueBackReferences(backRefs, numBackRefs);
        String[] selectionArgs = resolveSelectionArgsBackReferences(backRefs, numBackRefs);
        int i = this.mType;
        if (i == 1) {
            Uri newUri = provider.insert(this.mUri, values);
            if (newUri != null) {
                return new ContentProviderResult(newUri);
            }
            throw new OperationApplicationException("Insert into " + this.mUri + " returned no result");
        }
        if (i == 3) {
            numRows = provider.delete(this.mUri, this.mSelection, selectionArgs);
        } else if (i == 2) {
            numRows = provider.update(this.mUri, values, this.mSelection, selectionArgs);
        } else if (i == 4) {
            if (values != null) {
                ArrayList<String> projectionList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    projectionList.add(entry.getKey());
                }
                projection = (String[]) projectionList.toArray(new String[projectionList.size()]);
            } else {
                projection = null;
            }
            Cursor cursor = provider.query(this.mUri, projection, this.mSelection, selectionArgs, null);
            try {
                int numRows2 = cursor.getCount();
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
        } else {
            throw new IllegalStateException("bad type, " + this.mType);
        }
        Integer num = this.mExpectedCount;
        if (num == null || num.intValue() == numRows) {
            return new ContentProviderResult(numRows);
        }
        throw new OperationApplicationException("Expected " + this.mExpectedCount + " rows but actual " + numRows);
    }

    public ContentValues resolveValueBackReferences(ContentProviderResult[] backRefs, int numBackRefs) {
        ContentValues values;
        if (this.mValuesBackReferences == null) {
            return this.mValues;
        }
        ContentValues contentValues = this.mValues;
        if (contentValues == null) {
            values = new ContentValues();
        } else {
            values = new ContentValues(contentValues);
        }
        for (Map.Entry<String, Object> entry : this.mValuesBackReferences.valueSet()) {
            String key = entry.getKey();
            Integer backRefIndex = this.mValuesBackReferences.getAsInteger(key);
            if (backRefIndex != null) {
                values.put(key, Long.valueOf(backRefToValue(backRefs, numBackRefs, backRefIndex)));
            } else {
                Log.e(TAG, toString());
                throw new IllegalArgumentException("values backref " + key + " is not an integer");
            }
        }
        return values;
    }

    public String[] resolveSelectionArgsBackReferences(ContentProviderResult[] backRefs, int numBackRefs) {
        if (this.mSelectionArgsBackReferences == null) {
            return this.mSelectionArgs;
        }
        String[] strArr = this.mSelectionArgs;
        String[] newArgs = new String[strArr.length];
        System.arraycopy(strArr, 0, newArgs, 0, strArr.length);
        for (Map.Entry<Integer, Integer> selectionArgBackRef : this.mSelectionArgsBackReferences.entrySet()) {
            newArgs[selectionArgBackRef.getKey().intValue()] = String.valueOf(backRefToValue(backRefs, numBackRefs, Integer.valueOf(selectionArgBackRef.getValue().intValue())));
        }
        return newArgs;
    }

    public String toString() {
        return "mType: " + this.mType + ", mUri: " + this.mUri + ", mSelection: " + this.mSelection + ", mExpectedCount: " + this.mExpectedCount + ", mYieldAllowed: " + this.mYieldAllowed + ", mValues: " + this.mValues + ", mValuesBackReferences: " + this.mValuesBackReferences + ", mSelectionArgsBackReferences: " + this.mSelectionArgsBackReferences;
    }

    private long backRefToValue(ContentProviderResult[] backRefs, int numBackRefs, Integer backRefIndex) {
        if (backRefIndex.intValue() < numBackRefs) {
            ContentProviderResult backRef = backRefs[backRefIndex.intValue()];
            if (backRef.uri != null) {
                return ContentUris.parseId(backRef.uri);
            }
            return (long) backRef.count.intValue();
        }
        Log.e(TAG, toString());
        throw new ArrayIndexOutOfBoundsException("asked for back ref " + backRefIndex + " but there are only " + numBackRefs + " back refs");
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static class Builder {
        private Integer mExpectedCount;
        private boolean mFailureAllowed;
        private String mSelection;
        private String[] mSelectionArgs;
        private Map<Integer, Integer> mSelectionArgsBackReferences;
        private final int mType;
        private final Uri mUri;
        private ContentValues mValues;
        private ContentValues mValuesBackReferences;
        private boolean mYieldAllowed;

        private Builder(int type, Uri uri) {
            if (uri != null) {
                this.mType = type;
                this.mUri = uri;
                return;
            }
            throw new IllegalArgumentException("uri must not be null");
        }

        public ContentProviderOperation build() {
            ContentValues contentValues;
            ContentValues contentValues2;
            ContentValues contentValues3;
            ContentValues contentValues4;
            if (this.mType == 2 && (((contentValues3 = this.mValues) == null || contentValues3.isEmpty()) && ((contentValues4 = this.mValuesBackReferences) == null || contentValues4.isEmpty()))) {
                throw new IllegalArgumentException("Empty values");
            } else if (this.mType != 4 || (((contentValues = this.mValues) != null && !contentValues.isEmpty()) || (((contentValues2 = this.mValuesBackReferences) != null && !contentValues2.isEmpty()) || this.mExpectedCount != null))) {
                return new ContentProviderOperation(this);
            } else {
                throw new IllegalArgumentException("Empty values");
            }
        }

        public Builder withValueBackReferences(ContentValues backReferences) {
            int i = this.mType;
            if (i == 1 || i == 2 || i == 4) {
                this.mValuesBackReferences = backReferences;
                return this;
            }
            throw new IllegalArgumentException("only inserts, updates, and asserts can have value back-references");
        }

        public Builder withValueBackReference(String key, int previousResult) {
            int i = this.mType;
            if (i == 1 || i == 2 || i == 4) {
                if (this.mValuesBackReferences == null) {
                    this.mValuesBackReferences = new ContentValues();
                }
                this.mValuesBackReferences.put(key, Integer.valueOf(previousResult));
                return this;
            }
            throw new IllegalArgumentException("only inserts, updates, and asserts can have value back-references");
        }

        public Builder withSelectionBackReference(int selectionArgIndex, int previousResult) {
            int i = this.mType;
            if (i == 2 || i == 3 || i == 4) {
                if (this.mSelectionArgsBackReferences == null) {
                    this.mSelectionArgsBackReferences = new HashMap();
                }
                this.mSelectionArgsBackReferences.put(Integer.valueOf(selectionArgIndex), Integer.valueOf(previousResult));
                return this;
            }
            throw new IllegalArgumentException("only updates, deletes, and asserts can have selection back-references");
        }

        public Builder withValues(ContentValues values) {
            int i = this.mType;
            if (i == 1 || i == 2 || i == 4) {
                if (this.mValues == null) {
                    this.mValues = new ContentValues();
                }
                this.mValues.putAll(values);
                return this;
            }
            throw new IllegalArgumentException("only inserts, updates, and asserts can have values");
        }

        public Builder withValue(String key, Object value) {
            int i = this.mType;
            if (i == 1 || i == 2 || i == 4) {
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
            int i = this.mType;
            if (i == 2 || i == 3 || i == 4) {
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
            int i = this.mType;
            if (i == 2 || i == 3 || i == 4) {
                this.mExpectedCount = Integer.valueOf(count);
                return this;
            }
            throw new IllegalArgumentException("only updates, deletes, and asserts can have expected counts");
        }

        public Builder withYieldAllowed(boolean yieldAllowed) {
            this.mYieldAllowed = yieldAllowed;
            return this;
        }

        public Builder withFailureAllowed(boolean failureAllowed) {
            this.mFailureAllowed = failureAllowed;
            return this;
        }
    }
}
