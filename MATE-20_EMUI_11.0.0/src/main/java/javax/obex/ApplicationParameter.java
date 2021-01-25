package javax.obex;

public final class ApplicationParameter {
    private byte[] mArray = new byte[this.mMaxLength];
    private int mLength = 0;
    private int mMaxLength = 1000;

    public static class TRIPLET_LENGTH {
        public static final byte DATABASEIDENTIFIER_LENGTH = 16;
        public static final byte FORMAT_LENGTH = 1;
        public static final byte LISTSTARTOFFSET_LENGTH = 2;
        public static final byte MAXLISTCOUNT_LENGTH = 2;
        public static final byte NEWMISSEDCALLS_LENGTH = 1;
        public static final byte ORDER_LENGTH = 1;
        public static final byte PHONEBOOKSIZE_LENGTH = 2;
        public static final byte PRIMARYVERSIONCOUNTER_LENGTH = 16;
        public static final byte PROPERTY_SELECTOR_LENGTH = 8;
        public static final byte RESETNEWMISSEDCALLS_LENGTH = 1;
        public static final byte SEARCH_ATTRIBUTE_LENGTH = 1;
        public static final byte SECONDARYVERSIONCOUNTER_LENGTH = 16;
        public static final byte SUPPORTEDFEATURE_LENGTH = 4;
        public static final byte VCARDSELECTOROPERATOR_LENGTH = 1;
        public static final byte VCARDSELECTOR_LENGTH = 8;
    }

    public static class TRIPLET_TAGID {
        public static final byte DATABASEIDENTIFIER_TAGID = 13;
        public static final byte FORMAT_TAGID = 7;
        public static final byte LISTSTARTOFFSET_TAGID = 5;
        public static final byte MAXLISTCOUNT_TAGID = 4;
        public static final byte NEWMISSEDCALLS_TAGID = 9;
        public static final byte ORDER_TAGID = 1;
        public static final byte PHONEBOOKSIZE_TAGID = 8;
        public static final byte PRIMARYVERSIONCOUNTER_TAGID = 10;
        public static final byte PROPERTY_SELECTOR_TAGID = 6;
        public static final byte RESET_NEW_MISSED_CALLS_TAGID = 15;
        public static final byte SEARCH_ATTRIBUTE_TAGID = 3;
        public static final byte SEARCH_VALUE_TAGID = 2;
        public static final byte SECONDARYVERSIONCOUNTER_TAGID = 11;
        public static final byte SUPPORTEDFEATURE_TAGID = 16;
        public static final byte VCARDSELECTOROPERATOR_TAGID = 14;
        public static final byte VCARDSELECTOR_TAGID = 12;
    }

    public static class TRIPLET_VALUE {

        public static class FORMAT {
            public static final byte VCARD_VERSION_21 = 0;
            public static final byte VCARD_VERSION_30 = 1;
        }

        public static class ORDER {
            public static final byte ORDER_BY_ALPHANUMERIC = 1;
            public static final byte ORDER_BY_INDEX = 0;
            public static final byte ORDER_BY_PHONETIC = 2;
        }

        public static class SEARCHATTRIBUTE {
            public static final byte SEARCH_BY_NAME = 0;
            public static final byte SEARCH_BY_NUMBER = 1;
            public static final byte SEARCH_BY_SOUND = 2;
        }
    }

    public void addAPPHeader(byte tag, byte len, byte[] value) {
        int i = this.mLength;
        if (i + len + 2 > this.mMaxLength) {
            byte[] array_tmp = new byte[((len * 4) + i)];
            System.arraycopy(this.mArray, 0, array_tmp, 0, i);
            this.mArray = array_tmp;
            this.mMaxLength = this.mLength + (len * 4);
        }
        byte[] bArr = this.mArray;
        int i2 = this.mLength;
        this.mLength = i2 + 1;
        bArr[i2] = tag;
        int i3 = this.mLength;
        this.mLength = i3 + 1;
        bArr[i3] = len;
        System.arraycopy(value, 0, bArr, this.mLength, len);
        this.mLength += len;
    }

    public byte[] getAPPparam() {
        int i = this.mLength;
        byte[] para = new byte[i];
        System.arraycopy(this.mArray, 0, para, 0, i);
        return para;
    }
}
