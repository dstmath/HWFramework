package javax.obex;

public final class ApplicationParameter {
    private byte[] mArray = new byte[this.mMaxLength];
    private int mLength = 0;
    private int mMaxLength = 1000;

    public static class TRIPLET_LENGTH {
        public static final byte DATABASEIDENTIFIER_LENGTH = (byte) 16;
        public static final byte FORMAT_LENGTH = (byte) 1;
        public static final byte LISTSTARTOFFSET_LENGTH = (byte) 2;
        public static final byte MAXLISTCOUNT_LENGTH = (byte) 2;
        public static final byte NEWMISSEDCALLS_LENGTH = (byte) 1;
        public static final byte ORDER_LENGTH = (byte) 1;
        public static final byte PHONEBOOKSIZE_LENGTH = (byte) 2;
        public static final byte PRIMARYVERSIONCOUNTER_LENGTH = (byte) 16;
        public static final byte PROPERTY_SELECTOR_LENGTH = (byte) 8;
        public static final byte RESETNEWMISSEDCALLS_LENGTH = (byte) 1;
        public static final byte SEARCH_ATTRIBUTE_LENGTH = (byte) 1;
        public static final byte SECONDARYVERSIONCOUNTER_LENGTH = (byte) 16;
        public static final byte SUPPORTEDFEATURE_LENGTH = (byte) 4;
        public static final byte VCARDSELECTOROPERATOR_LENGTH = (byte) 1;
        public static final byte VCARDSELECTOR_LENGTH = (byte) 8;
    }

    public static class TRIPLET_TAGID {
        public static final byte DATABASEIDENTIFIER_TAGID = (byte) 13;
        public static final byte FORMAT_TAGID = (byte) 7;
        public static final byte LISTSTARTOFFSET_TAGID = (byte) 5;
        public static final byte MAXLISTCOUNT_TAGID = (byte) 4;
        public static final byte NEWMISSEDCALLS_TAGID = (byte) 9;
        public static final byte ORDER_TAGID = (byte) 1;
        public static final byte PHONEBOOKSIZE_TAGID = (byte) 8;
        public static final byte PRIMARYVERSIONCOUNTER_TAGID = (byte) 10;
        public static final byte PROPERTY_SELECTOR_TAGID = (byte) 6;
        public static final byte RESET_NEW_MISSED_CALLS_TAGID = (byte) 15;
        public static final byte SEARCH_ATTRIBUTE_TAGID = (byte) 3;
        public static final byte SEARCH_VALUE_TAGID = (byte) 2;
        public static final byte SECONDARYVERSIONCOUNTER_TAGID = (byte) 11;
        public static final byte SUPPORTEDFEATURE_TAGID = (byte) 16;
        public static final byte VCARDSELECTOROPERATOR_TAGID = (byte) 14;
        public static final byte VCARDSELECTOR_TAGID = (byte) 12;
    }

    public static class TRIPLET_VALUE {

        public static class FORMAT {
            public static final byte VCARD_VERSION_21 = (byte) 0;
            public static final byte VCARD_VERSION_30 = (byte) 1;
        }

        public static class ORDER {
            public static final byte ORDER_BY_ALPHANUMERIC = (byte) 1;
            public static final byte ORDER_BY_INDEX = (byte) 0;
            public static final byte ORDER_BY_PHONETIC = (byte) 2;
        }

        public static class SEARCHATTRIBUTE {
            public static final byte SEARCH_BY_NAME = (byte) 0;
            public static final byte SEARCH_BY_NUMBER = (byte) 1;
            public static final byte SEARCH_BY_SOUND = (byte) 2;
        }
    }

    public void addAPPHeader(byte tag, byte len, byte[] value) {
        if ((this.mLength + len) + 2 > this.mMaxLength) {
            byte[] array_tmp = new byte[(this.mLength + (len * 4))];
            System.arraycopy(this.mArray, 0, array_tmp, 0, this.mLength);
            this.mArray = array_tmp;
            this.mMaxLength = this.mLength + (len * 4);
        }
        byte[] bArr = this.mArray;
        int i = this.mLength;
        this.mLength = i + 1;
        bArr[i] = tag;
        bArr = this.mArray;
        i = this.mLength;
        this.mLength = i + 1;
        bArr[i] = len;
        System.arraycopy(value, 0, this.mArray, this.mLength, len);
        this.mLength += len;
    }

    public byte[] getAPPparam() {
        byte[] para = new byte[this.mLength];
        System.arraycopy(this.mArray, 0, para, 0, this.mLength);
        return para;
    }
}
