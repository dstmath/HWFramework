package android.renderscript;

public class Element extends BaseObj {
    int[] mArraySizes;
    String[] mElementNames;
    Element[] mElements;
    DataKind mKind;
    boolean mNormalized;
    int[] mOffsetInBytes;
    int mSize;
    DataType mType;
    int mVectorSize;
    int[] mVisibleElementMap;

    public static class Builder {
        int[] mArraySizes = new int[8];
        int mCount = 0;
        String[] mElementNames = new String[8];
        Element[] mElements = new Element[8];
        RenderScript mRS;
        int mSkipPadding;

        public Builder(RenderScript rs) {
            this.mRS = rs;
        }

        public Builder add(Element element, String name, int arraySize) {
            if (arraySize < 1) {
                throw new RSIllegalArgumentException("Array size cannot be less than 1.");
            } else if (this.mSkipPadding == 0 || !name.startsWith("#padding_")) {
                if (element.mVectorSize == 3) {
                    this.mSkipPadding = 1;
                } else {
                    this.mSkipPadding = 0;
                }
                if (this.mCount == this.mElements.length) {
                    Element[] e = new Element[(this.mCount + 8)];
                    String[] s = new String[(this.mCount + 8)];
                    int[] as = new int[(this.mCount + 8)];
                    System.arraycopy(this.mElements, 0, e, 0, this.mCount);
                    System.arraycopy(this.mElementNames, 0, s, 0, this.mCount);
                    System.arraycopy(this.mArraySizes, 0, as, 0, this.mCount);
                    this.mElements = e;
                    this.mElementNames = s;
                    this.mArraySizes = as;
                }
                this.mElements[this.mCount] = element;
                this.mElementNames[this.mCount] = name;
                this.mArraySizes[this.mCount] = arraySize;
                this.mCount++;
                return this;
            } else {
                this.mSkipPadding = 0;
                return this;
            }
        }

        public Builder add(Element element, String name) {
            return add(element, name, 1);
        }

        public Element create() {
            this.mRS.validate();
            Element[] ein = new Element[this.mCount];
            String[] sin = new String[this.mCount];
            int[] asin = new int[this.mCount];
            int ct = 0;
            System.arraycopy(this.mElements, 0, ein, 0, this.mCount);
            System.arraycopy(this.mElementNames, 0, sin, 0, this.mCount);
            System.arraycopy(this.mArraySizes, 0, asin, 0, this.mCount);
            long[] ids = new long[ein.length];
            while (true) {
                int ct2 = ct;
                if (ct2 < ein.length) {
                    ids[ct2] = ein[ct2].getID(this.mRS);
                    ct = ct2 + 1;
                } else {
                    Element element = new Element(this.mRS.nElementCreate2(ids, sin, asin), this.mRS, ein, sin, asin);
                    return element;
                }
            }
        }
    }

    public enum DataKind {
        USER(0),
        PIXEL_L(7),
        PIXEL_A(8),
        PIXEL_LA(9),
        PIXEL_RGB(10),
        PIXEL_RGBA(11),
        PIXEL_DEPTH(12),
        PIXEL_YUV(13);
        
        int mID;

        private DataKind(int id) {
            this.mID = id;
        }
    }

    public enum DataType {
        NONE(0, 0),
        FLOAT_16(1, 2),
        FLOAT_32(2, 4),
        FLOAT_64(3, 8),
        SIGNED_8(4, 1),
        SIGNED_16(5, 2),
        SIGNED_32(6, 4),
        SIGNED_64(7, 8),
        UNSIGNED_8(8, 1),
        UNSIGNED_16(9, 2),
        UNSIGNED_32(10, 4),
        UNSIGNED_64(11, 8),
        BOOLEAN(12, 1),
        UNSIGNED_5_6_5(13, 2),
        UNSIGNED_5_5_5_1(14, 2),
        UNSIGNED_4_4_4_4(15, 2),
        MATRIX_4X4(16, 64),
        MATRIX_3X3(17, 36),
        MATRIX_2X2(18, 16),
        RS_ELEMENT(1000),
        RS_TYPE(1001),
        RS_ALLOCATION(1002),
        RS_SAMPLER(1003),
        RS_SCRIPT(1004),
        RS_MESH(1005),
        RS_PROGRAM_FRAGMENT(1006),
        RS_PROGRAM_VERTEX(1007),
        RS_PROGRAM_RASTER(1008),
        RS_PROGRAM_STORE(1009),
        RS_FONT(1010);
        
        int mID;
        int mSize;

        private DataType(int id, int size) {
            this.mID = id;
            this.mSize = size;
        }

        private DataType(int id) {
            this.mID = id;
            this.mSize = 4;
            if (RenderScript.sPointerSize == 8) {
                this.mSize = 32;
            }
        }
    }

    private void updateVisibleSubElements() {
        if (this.mElements != null) {
            int noPaddingFieldCount = 0;
            for (String charAt : this.mElementNames) {
                if (charAt.charAt(0) != '#') {
                    noPaddingFieldCount++;
                }
            }
            this.mVisibleElementMap = new int[noPaddingFieldCount];
            int ctNoPadding = 0;
            for (int ct = 0; ct < fieldCount; ct++) {
                if (this.mElementNames[ct].charAt(0) != '#') {
                    this.mVisibleElementMap[ctNoPadding] = ct;
                    ctNoPadding++;
                }
            }
        }
    }

    public int getBytesSize() {
        return this.mSize;
    }

    public int getVectorSize() {
        return this.mVectorSize;
    }

    public boolean isComplex() {
        if (this.mElements == null) {
            return false;
        }
        for (Element element : this.mElements) {
            if (element.mElements != null) {
                return true;
            }
        }
        return false;
    }

    public int getSubElementCount() {
        if (this.mVisibleElementMap == null) {
            return 0;
        }
        return this.mVisibleElementMap.length;
    }

    public Element getSubElement(int index) {
        if (this.mVisibleElementMap == null) {
            throw new RSIllegalArgumentException("Element contains no sub-elements");
        } else if (index >= 0 && index < this.mVisibleElementMap.length) {
            return this.mElements[this.mVisibleElementMap[index]];
        } else {
            throw new RSIllegalArgumentException("Illegal sub-element index");
        }
    }

    public String getSubElementName(int index) {
        if (this.mVisibleElementMap == null) {
            throw new RSIllegalArgumentException("Element contains no sub-elements");
        } else if (index >= 0 && index < this.mVisibleElementMap.length) {
            return this.mElementNames[this.mVisibleElementMap[index]];
        } else {
            throw new RSIllegalArgumentException("Illegal sub-element index");
        }
    }

    public int getSubElementArraySize(int index) {
        if (this.mVisibleElementMap == null) {
            throw new RSIllegalArgumentException("Element contains no sub-elements");
        } else if (index >= 0 && index < this.mVisibleElementMap.length) {
            return this.mArraySizes[this.mVisibleElementMap[index]];
        } else {
            throw new RSIllegalArgumentException("Illegal sub-element index");
        }
    }

    public int getSubElementOffsetBytes(int index) {
        if (this.mVisibleElementMap == null) {
            throw new RSIllegalArgumentException("Element contains no sub-elements");
        } else if (index >= 0 && index < this.mVisibleElementMap.length) {
            return this.mOffsetInBytes[this.mVisibleElementMap[index]];
        } else {
            throw new RSIllegalArgumentException("Illegal sub-element index");
        }
    }

    public DataType getDataType() {
        return this.mType;
    }

    public DataKind getDataKind() {
        return this.mKind;
    }

    public static Element BOOLEAN(RenderScript rs) {
        if (rs.mElement_BOOLEAN == null) {
            synchronized (rs) {
                if (rs.mElement_BOOLEAN == null) {
                    rs.mElement_BOOLEAN = createUser(rs, DataType.BOOLEAN);
                }
            }
        }
        return rs.mElement_BOOLEAN;
    }

    public static Element U8(RenderScript rs) {
        if (rs.mElement_U8 == null) {
            synchronized (rs) {
                if (rs.mElement_U8 == null) {
                    rs.mElement_U8 = createUser(rs, DataType.UNSIGNED_8);
                }
            }
        }
        return rs.mElement_U8;
    }

    public static Element I8(RenderScript rs) {
        if (rs.mElement_I8 == null) {
            synchronized (rs) {
                if (rs.mElement_I8 == null) {
                    rs.mElement_I8 = createUser(rs, DataType.SIGNED_8);
                }
            }
        }
        return rs.mElement_I8;
    }

    public static Element U16(RenderScript rs) {
        if (rs.mElement_U16 == null) {
            synchronized (rs) {
                if (rs.mElement_U16 == null) {
                    rs.mElement_U16 = createUser(rs, DataType.UNSIGNED_16);
                }
            }
        }
        return rs.mElement_U16;
    }

    public static Element I16(RenderScript rs) {
        if (rs.mElement_I16 == null) {
            synchronized (rs) {
                if (rs.mElement_I16 == null) {
                    rs.mElement_I16 = createUser(rs, DataType.SIGNED_16);
                }
            }
        }
        return rs.mElement_I16;
    }

    public static Element U32(RenderScript rs) {
        if (rs.mElement_U32 == null) {
            synchronized (rs) {
                if (rs.mElement_U32 == null) {
                    rs.mElement_U32 = createUser(rs, DataType.UNSIGNED_32);
                }
            }
        }
        return rs.mElement_U32;
    }

    public static Element I32(RenderScript rs) {
        if (rs.mElement_I32 == null) {
            synchronized (rs) {
                if (rs.mElement_I32 == null) {
                    rs.mElement_I32 = createUser(rs, DataType.SIGNED_32);
                }
            }
        }
        return rs.mElement_I32;
    }

    public static Element U64(RenderScript rs) {
        if (rs.mElement_U64 == null) {
            synchronized (rs) {
                if (rs.mElement_U64 == null) {
                    rs.mElement_U64 = createUser(rs, DataType.UNSIGNED_64);
                }
            }
        }
        return rs.mElement_U64;
    }

    public static Element I64(RenderScript rs) {
        if (rs.mElement_I64 == null) {
            synchronized (rs) {
                if (rs.mElement_I64 == null) {
                    rs.mElement_I64 = createUser(rs, DataType.SIGNED_64);
                }
            }
        }
        return rs.mElement_I64;
    }

    public static Element F16(RenderScript rs) {
        if (rs.mElement_F16 == null) {
            synchronized (rs) {
                if (rs.mElement_F16 == null) {
                    rs.mElement_F16 = createUser(rs, DataType.FLOAT_16);
                }
            }
        }
        return rs.mElement_F16;
    }

    public static Element F32(RenderScript rs) {
        if (rs.mElement_F32 == null) {
            synchronized (rs) {
                if (rs.mElement_F32 == null) {
                    rs.mElement_F32 = createUser(rs, DataType.FLOAT_32);
                }
            }
        }
        return rs.mElement_F32;
    }

    public static Element F64(RenderScript rs) {
        if (rs.mElement_F64 == null) {
            synchronized (rs) {
                if (rs.mElement_F64 == null) {
                    rs.mElement_F64 = createUser(rs, DataType.FLOAT_64);
                }
            }
        }
        return rs.mElement_F64;
    }

    public static Element ELEMENT(RenderScript rs) {
        if (rs.mElement_ELEMENT == null) {
            synchronized (rs) {
                if (rs.mElement_ELEMENT == null) {
                    rs.mElement_ELEMENT = createUser(rs, DataType.RS_ELEMENT);
                }
            }
        }
        return rs.mElement_ELEMENT;
    }

    public static Element TYPE(RenderScript rs) {
        if (rs.mElement_TYPE == null) {
            synchronized (rs) {
                if (rs.mElement_TYPE == null) {
                    rs.mElement_TYPE = createUser(rs, DataType.RS_TYPE);
                }
            }
        }
        return rs.mElement_TYPE;
    }

    public static Element ALLOCATION(RenderScript rs) {
        if (rs.mElement_ALLOCATION == null) {
            synchronized (rs) {
                if (rs.mElement_ALLOCATION == null) {
                    rs.mElement_ALLOCATION = createUser(rs, DataType.RS_ALLOCATION);
                }
            }
        }
        return rs.mElement_ALLOCATION;
    }

    public static Element SAMPLER(RenderScript rs) {
        if (rs.mElement_SAMPLER == null) {
            synchronized (rs) {
                if (rs.mElement_SAMPLER == null) {
                    rs.mElement_SAMPLER = createUser(rs, DataType.RS_SAMPLER);
                }
            }
        }
        return rs.mElement_SAMPLER;
    }

    public static Element SCRIPT(RenderScript rs) {
        if (rs.mElement_SCRIPT == null) {
            synchronized (rs) {
                if (rs.mElement_SCRIPT == null) {
                    rs.mElement_SCRIPT = createUser(rs, DataType.RS_SCRIPT);
                }
            }
        }
        return rs.mElement_SCRIPT;
    }

    public static Element MESH(RenderScript rs) {
        if (rs.mElement_MESH == null) {
            synchronized (rs) {
                if (rs.mElement_MESH == null) {
                    rs.mElement_MESH = createUser(rs, DataType.RS_MESH);
                }
            }
        }
        return rs.mElement_MESH;
    }

    public static Element PROGRAM_FRAGMENT(RenderScript rs) {
        if (rs.mElement_PROGRAM_FRAGMENT == null) {
            synchronized (rs) {
                if (rs.mElement_PROGRAM_FRAGMENT == null) {
                    rs.mElement_PROGRAM_FRAGMENT = createUser(rs, DataType.RS_PROGRAM_FRAGMENT);
                }
            }
        }
        return rs.mElement_PROGRAM_FRAGMENT;
    }

    public static Element PROGRAM_VERTEX(RenderScript rs) {
        if (rs.mElement_PROGRAM_VERTEX == null) {
            synchronized (rs) {
                if (rs.mElement_PROGRAM_VERTEX == null) {
                    rs.mElement_PROGRAM_VERTEX = createUser(rs, DataType.RS_PROGRAM_VERTEX);
                }
            }
        }
        return rs.mElement_PROGRAM_VERTEX;
    }

    public static Element PROGRAM_RASTER(RenderScript rs) {
        if (rs.mElement_PROGRAM_RASTER == null) {
            synchronized (rs) {
                if (rs.mElement_PROGRAM_RASTER == null) {
                    rs.mElement_PROGRAM_RASTER = createUser(rs, DataType.RS_PROGRAM_RASTER);
                }
            }
        }
        return rs.mElement_PROGRAM_RASTER;
    }

    public static Element PROGRAM_STORE(RenderScript rs) {
        if (rs.mElement_PROGRAM_STORE == null) {
            synchronized (rs) {
                if (rs.mElement_PROGRAM_STORE == null) {
                    rs.mElement_PROGRAM_STORE = createUser(rs, DataType.RS_PROGRAM_STORE);
                }
            }
        }
        return rs.mElement_PROGRAM_STORE;
    }

    public static Element FONT(RenderScript rs) {
        if (rs.mElement_FONT == null) {
            synchronized (rs) {
                if (rs.mElement_FONT == null) {
                    rs.mElement_FONT = createUser(rs, DataType.RS_FONT);
                }
            }
        }
        return rs.mElement_FONT;
    }

    public static Element A_8(RenderScript rs) {
        if (rs.mElement_A_8 == null) {
            synchronized (rs) {
                if (rs.mElement_A_8 == null) {
                    rs.mElement_A_8 = createPixel(rs, DataType.UNSIGNED_8, DataKind.PIXEL_A);
                }
            }
        }
        return rs.mElement_A_8;
    }

    public static Element RGB_565(RenderScript rs) {
        if (rs.mElement_RGB_565 == null) {
            synchronized (rs) {
                if (rs.mElement_RGB_565 == null) {
                    rs.mElement_RGB_565 = createPixel(rs, DataType.UNSIGNED_5_6_5, DataKind.PIXEL_RGB);
                }
            }
        }
        return rs.mElement_RGB_565;
    }

    public static Element RGB_888(RenderScript rs) {
        if (rs.mElement_RGB_888 == null) {
            synchronized (rs) {
                if (rs.mElement_RGB_888 == null) {
                    rs.mElement_RGB_888 = createPixel(rs, DataType.UNSIGNED_8, DataKind.PIXEL_RGB);
                }
            }
        }
        return rs.mElement_RGB_888;
    }

    public static Element RGBA_5551(RenderScript rs) {
        if (rs.mElement_RGBA_5551 == null) {
            synchronized (rs) {
                if (rs.mElement_RGBA_5551 == null) {
                    rs.mElement_RGBA_5551 = createPixel(rs, DataType.UNSIGNED_5_5_5_1, DataKind.PIXEL_RGBA);
                }
            }
        }
        return rs.mElement_RGBA_5551;
    }

    public static Element RGBA_4444(RenderScript rs) {
        if (rs.mElement_RGBA_4444 == null) {
            synchronized (rs) {
                if (rs.mElement_RGBA_4444 == null) {
                    rs.mElement_RGBA_4444 = createPixel(rs, DataType.UNSIGNED_4_4_4_4, DataKind.PIXEL_RGBA);
                }
            }
        }
        return rs.mElement_RGBA_4444;
    }

    public static Element RGBA_8888(RenderScript rs) {
        if (rs.mElement_RGBA_8888 == null) {
            synchronized (rs) {
                if (rs.mElement_RGBA_8888 == null) {
                    rs.mElement_RGBA_8888 = createPixel(rs, DataType.UNSIGNED_8, DataKind.PIXEL_RGBA);
                }
            }
        }
        return rs.mElement_RGBA_8888;
    }

    public static Element F16_2(RenderScript rs) {
        if (rs.mElement_HALF_2 == null) {
            synchronized (rs) {
                if (rs.mElement_HALF_2 == null) {
                    rs.mElement_HALF_2 = createVector(rs, DataType.FLOAT_16, 2);
                }
            }
        }
        return rs.mElement_HALF_2;
    }

    public static Element F16_3(RenderScript rs) {
        if (rs.mElement_HALF_3 == null) {
            synchronized (rs) {
                if (rs.mElement_HALF_3 == null) {
                    rs.mElement_HALF_3 = createVector(rs, DataType.FLOAT_16, 3);
                }
            }
        }
        return rs.mElement_HALF_3;
    }

    public static Element F16_4(RenderScript rs) {
        if (rs.mElement_HALF_4 == null) {
            synchronized (rs) {
                if (rs.mElement_HALF_4 == null) {
                    rs.mElement_HALF_4 = createVector(rs, DataType.FLOAT_16, 4);
                }
            }
        }
        return rs.mElement_HALF_4;
    }

    public static Element F32_2(RenderScript rs) {
        if (rs.mElement_FLOAT_2 == null) {
            synchronized (rs) {
                if (rs.mElement_FLOAT_2 == null) {
                    rs.mElement_FLOAT_2 = createVector(rs, DataType.FLOAT_32, 2);
                }
            }
        }
        return rs.mElement_FLOAT_2;
    }

    public static Element F32_3(RenderScript rs) {
        if (rs.mElement_FLOAT_3 == null) {
            synchronized (rs) {
                if (rs.mElement_FLOAT_3 == null) {
                    rs.mElement_FLOAT_3 = createVector(rs, DataType.FLOAT_32, 3);
                }
            }
        }
        return rs.mElement_FLOAT_3;
    }

    public static Element F32_4(RenderScript rs) {
        if (rs.mElement_FLOAT_4 == null) {
            synchronized (rs) {
                if (rs.mElement_FLOAT_4 == null) {
                    rs.mElement_FLOAT_4 = createVector(rs, DataType.FLOAT_32, 4);
                }
            }
        }
        return rs.mElement_FLOAT_4;
    }

    public static Element F64_2(RenderScript rs) {
        if (rs.mElement_DOUBLE_2 == null) {
            synchronized (rs) {
                if (rs.mElement_DOUBLE_2 == null) {
                    rs.mElement_DOUBLE_2 = createVector(rs, DataType.FLOAT_64, 2);
                }
            }
        }
        return rs.mElement_DOUBLE_2;
    }

    public static Element F64_3(RenderScript rs) {
        if (rs.mElement_DOUBLE_3 == null) {
            synchronized (rs) {
                if (rs.mElement_DOUBLE_3 == null) {
                    rs.mElement_DOUBLE_3 = createVector(rs, DataType.FLOAT_64, 3);
                }
            }
        }
        return rs.mElement_DOUBLE_3;
    }

    public static Element F64_4(RenderScript rs) {
        if (rs.mElement_DOUBLE_4 == null) {
            synchronized (rs) {
                if (rs.mElement_DOUBLE_4 == null) {
                    rs.mElement_DOUBLE_4 = createVector(rs, DataType.FLOAT_64, 4);
                }
            }
        }
        return rs.mElement_DOUBLE_4;
    }

    public static Element U8_2(RenderScript rs) {
        if (rs.mElement_UCHAR_2 == null) {
            synchronized (rs) {
                if (rs.mElement_UCHAR_2 == null) {
                    rs.mElement_UCHAR_2 = createVector(rs, DataType.UNSIGNED_8, 2);
                }
            }
        }
        return rs.mElement_UCHAR_2;
    }

    public static Element U8_3(RenderScript rs) {
        if (rs.mElement_UCHAR_3 == null) {
            synchronized (rs) {
                if (rs.mElement_UCHAR_3 == null) {
                    rs.mElement_UCHAR_3 = createVector(rs, DataType.UNSIGNED_8, 3);
                }
            }
        }
        return rs.mElement_UCHAR_3;
    }

    public static Element U8_4(RenderScript rs) {
        if (rs.mElement_UCHAR_4 == null) {
            synchronized (rs) {
                if (rs.mElement_UCHAR_4 == null) {
                    rs.mElement_UCHAR_4 = createVector(rs, DataType.UNSIGNED_8, 4);
                }
            }
        }
        return rs.mElement_UCHAR_4;
    }

    public static Element I8_2(RenderScript rs) {
        if (rs.mElement_CHAR_2 == null) {
            synchronized (rs) {
                if (rs.mElement_CHAR_2 == null) {
                    rs.mElement_CHAR_2 = createVector(rs, DataType.SIGNED_8, 2);
                }
            }
        }
        return rs.mElement_CHAR_2;
    }

    public static Element I8_3(RenderScript rs) {
        if (rs.mElement_CHAR_3 == null) {
            synchronized (rs) {
                if (rs.mElement_CHAR_3 == null) {
                    rs.mElement_CHAR_3 = createVector(rs, DataType.SIGNED_8, 3);
                }
            }
        }
        return rs.mElement_CHAR_3;
    }

    public static Element I8_4(RenderScript rs) {
        if (rs.mElement_CHAR_4 == null) {
            synchronized (rs) {
                if (rs.mElement_CHAR_4 == null) {
                    rs.mElement_CHAR_4 = createVector(rs, DataType.SIGNED_8, 4);
                }
            }
        }
        return rs.mElement_CHAR_4;
    }

    public static Element U16_2(RenderScript rs) {
        if (rs.mElement_USHORT_2 == null) {
            synchronized (rs) {
                if (rs.mElement_USHORT_2 == null) {
                    rs.mElement_USHORT_2 = createVector(rs, DataType.UNSIGNED_16, 2);
                }
            }
        }
        return rs.mElement_USHORT_2;
    }

    public static Element U16_3(RenderScript rs) {
        if (rs.mElement_USHORT_3 == null) {
            synchronized (rs) {
                if (rs.mElement_USHORT_3 == null) {
                    rs.mElement_USHORT_3 = createVector(rs, DataType.UNSIGNED_16, 3);
                }
            }
        }
        return rs.mElement_USHORT_3;
    }

    public static Element U16_4(RenderScript rs) {
        if (rs.mElement_USHORT_4 == null) {
            synchronized (rs) {
                if (rs.mElement_USHORT_4 == null) {
                    rs.mElement_USHORT_4 = createVector(rs, DataType.UNSIGNED_16, 4);
                }
            }
        }
        return rs.mElement_USHORT_4;
    }

    public static Element I16_2(RenderScript rs) {
        if (rs.mElement_SHORT_2 == null) {
            synchronized (rs) {
                if (rs.mElement_SHORT_2 == null) {
                    rs.mElement_SHORT_2 = createVector(rs, DataType.SIGNED_16, 2);
                }
            }
        }
        return rs.mElement_SHORT_2;
    }

    public static Element I16_3(RenderScript rs) {
        if (rs.mElement_SHORT_3 == null) {
            synchronized (rs) {
                if (rs.mElement_SHORT_3 == null) {
                    rs.mElement_SHORT_3 = createVector(rs, DataType.SIGNED_16, 3);
                }
            }
        }
        return rs.mElement_SHORT_3;
    }

    public static Element I16_4(RenderScript rs) {
        if (rs.mElement_SHORT_4 == null) {
            synchronized (rs) {
                if (rs.mElement_SHORT_4 == null) {
                    rs.mElement_SHORT_4 = createVector(rs, DataType.SIGNED_16, 4);
                }
            }
        }
        return rs.mElement_SHORT_4;
    }

    public static Element U32_2(RenderScript rs) {
        if (rs.mElement_UINT_2 == null) {
            synchronized (rs) {
                if (rs.mElement_UINT_2 == null) {
                    rs.mElement_UINT_2 = createVector(rs, DataType.UNSIGNED_32, 2);
                }
            }
        }
        return rs.mElement_UINT_2;
    }

    public static Element U32_3(RenderScript rs) {
        if (rs.mElement_UINT_3 == null) {
            synchronized (rs) {
                if (rs.mElement_UINT_3 == null) {
                    rs.mElement_UINT_3 = createVector(rs, DataType.UNSIGNED_32, 3);
                }
            }
        }
        return rs.mElement_UINT_3;
    }

    public static Element U32_4(RenderScript rs) {
        if (rs.mElement_UINT_4 == null) {
            synchronized (rs) {
                if (rs.mElement_UINT_4 == null) {
                    rs.mElement_UINT_4 = createVector(rs, DataType.UNSIGNED_32, 4);
                }
            }
        }
        return rs.mElement_UINT_4;
    }

    public static Element I32_2(RenderScript rs) {
        if (rs.mElement_INT_2 == null) {
            synchronized (rs) {
                if (rs.mElement_INT_2 == null) {
                    rs.mElement_INT_2 = createVector(rs, DataType.SIGNED_32, 2);
                }
            }
        }
        return rs.mElement_INT_2;
    }

    public static Element I32_3(RenderScript rs) {
        if (rs.mElement_INT_3 == null) {
            synchronized (rs) {
                if (rs.mElement_INT_3 == null) {
                    rs.mElement_INT_3 = createVector(rs, DataType.SIGNED_32, 3);
                }
            }
        }
        return rs.mElement_INT_3;
    }

    public static Element I32_4(RenderScript rs) {
        if (rs.mElement_INT_4 == null) {
            synchronized (rs) {
                if (rs.mElement_INT_4 == null) {
                    rs.mElement_INT_4 = createVector(rs, DataType.SIGNED_32, 4);
                }
            }
        }
        return rs.mElement_INT_4;
    }

    public static Element U64_2(RenderScript rs) {
        if (rs.mElement_ULONG_2 == null) {
            synchronized (rs) {
                if (rs.mElement_ULONG_2 == null) {
                    rs.mElement_ULONG_2 = createVector(rs, DataType.UNSIGNED_64, 2);
                }
            }
        }
        return rs.mElement_ULONG_2;
    }

    public static Element U64_3(RenderScript rs) {
        if (rs.mElement_ULONG_3 == null) {
            synchronized (rs) {
                if (rs.mElement_ULONG_3 == null) {
                    rs.mElement_ULONG_3 = createVector(rs, DataType.UNSIGNED_64, 3);
                }
            }
        }
        return rs.mElement_ULONG_3;
    }

    public static Element U64_4(RenderScript rs) {
        if (rs.mElement_ULONG_4 == null) {
            synchronized (rs) {
                if (rs.mElement_ULONG_4 == null) {
                    rs.mElement_ULONG_4 = createVector(rs, DataType.UNSIGNED_64, 4);
                }
            }
        }
        return rs.mElement_ULONG_4;
    }

    public static Element I64_2(RenderScript rs) {
        if (rs.mElement_LONG_2 == null) {
            synchronized (rs) {
                if (rs.mElement_LONG_2 == null) {
                    rs.mElement_LONG_2 = createVector(rs, DataType.SIGNED_64, 2);
                }
            }
        }
        return rs.mElement_LONG_2;
    }

    public static Element I64_3(RenderScript rs) {
        if (rs.mElement_LONG_3 == null) {
            synchronized (rs) {
                if (rs.mElement_LONG_3 == null) {
                    rs.mElement_LONG_3 = createVector(rs, DataType.SIGNED_64, 3);
                }
            }
        }
        return rs.mElement_LONG_3;
    }

    public static Element I64_4(RenderScript rs) {
        if (rs.mElement_LONG_4 == null) {
            synchronized (rs) {
                if (rs.mElement_LONG_4 == null) {
                    rs.mElement_LONG_4 = createVector(rs, DataType.SIGNED_64, 4);
                }
            }
        }
        return rs.mElement_LONG_4;
    }

    public static Element YUV(RenderScript rs) {
        if (rs.mElement_YUV == null) {
            synchronized (rs) {
                if (rs.mElement_YUV == null) {
                    rs.mElement_YUV = createPixel(rs, DataType.UNSIGNED_8, DataKind.PIXEL_YUV);
                }
            }
        }
        return rs.mElement_YUV;
    }

    public static Element MATRIX_4X4(RenderScript rs) {
        if (rs.mElement_MATRIX_4X4 == null) {
            synchronized (rs) {
                if (rs.mElement_MATRIX_4X4 == null) {
                    rs.mElement_MATRIX_4X4 = createUser(rs, DataType.MATRIX_4X4);
                }
            }
        }
        return rs.mElement_MATRIX_4X4;
    }

    public static Element MATRIX4X4(RenderScript rs) {
        return MATRIX_4X4(rs);
    }

    public static Element MATRIX_3X3(RenderScript rs) {
        if (rs.mElement_MATRIX_3X3 == null) {
            synchronized (rs) {
                if (rs.mElement_MATRIX_3X3 == null) {
                    rs.mElement_MATRIX_3X3 = createUser(rs, DataType.MATRIX_3X3);
                }
            }
        }
        return rs.mElement_MATRIX_3X3;
    }

    public static Element MATRIX_2X2(RenderScript rs) {
        if (rs.mElement_MATRIX_2X2 == null) {
            synchronized (rs) {
                if (rs.mElement_MATRIX_2X2 == null) {
                    rs.mElement_MATRIX_2X2 = createUser(rs, DataType.MATRIX_2X2);
                }
            }
        }
        return rs.mElement_MATRIX_2X2;
    }

    Element(long id, RenderScript rs, Element[] e, String[] n, int[] as) {
        super(id, rs);
        this.mSize = 0;
        this.mVectorSize = 1;
        this.mElements = e;
        this.mElementNames = n;
        this.mArraySizes = as;
        this.mType = DataType.NONE;
        this.mKind = DataKind.USER;
        this.mOffsetInBytes = new int[this.mElements.length];
        for (int ct = 0; ct < this.mElements.length; ct++) {
            this.mOffsetInBytes[ct] = this.mSize;
            this.mSize += this.mElements[ct].mSize * this.mArraySizes[ct];
        }
        updateVisibleSubElements();
    }

    Element(long id, RenderScript rs, DataType dt, DataKind dk, boolean norm, int size) {
        super(id, rs);
        if (dt == DataType.UNSIGNED_5_6_5 || dt == DataType.UNSIGNED_4_4_4_4 || dt == DataType.UNSIGNED_5_5_5_1) {
            this.mSize = dt.mSize;
        } else if (size == 3) {
            this.mSize = dt.mSize * 4;
        } else {
            this.mSize = dt.mSize * size;
        }
        this.mType = dt;
        this.mKind = dk;
        this.mNormalized = norm;
        this.mVectorSize = size;
    }

    Element(long id, RenderScript rs) {
        super(id, rs);
    }

    /* access modifiers changed from: package-private */
    public void updateFromNative() {
        super.updateFromNative();
        int[] dataBuffer = new int[5];
        this.mRS.nElementGetNativeData(getID(this.mRS), dataBuffer);
        this.mNormalized = dataBuffer[2] == 1;
        this.mVectorSize = dataBuffer[3];
        this.mSize = 0;
        for (DataType dt : DataType.values()) {
            if (dt.mID == dataBuffer[0]) {
                this.mType = dt;
                this.mSize = this.mType.mSize * this.mVectorSize;
            }
        }
        for (DataKind dk : DataKind.values()) {
            if (dk.mID == dataBuffer[1]) {
                this.mKind = dk;
            }
        }
        int numSubElements = dataBuffer[4];
        if (numSubElements > 0) {
            this.mElements = new Element[numSubElements];
            this.mElementNames = new String[numSubElements];
            this.mArraySizes = new int[numSubElements];
            this.mOffsetInBytes = new int[numSubElements];
            long[] subElementIds = new long[numSubElements];
            this.mRS.nElementGetSubElements(getID(this.mRS), subElementIds, this.mElementNames, this.mArraySizes);
            for (int i = 0; i < numSubElements; i++) {
                this.mElements[i] = new Element(subElementIds[i], this.mRS);
                this.mElements[i].updateFromNative();
                this.mOffsetInBytes[i] = this.mSize;
                this.mSize += this.mElements[i].mSize * this.mArraySizes[i];
            }
        }
        updateVisibleSubElements();
    }

    static Element createUser(RenderScript rs, DataType dt) {
        DataKind dk = DataKind.USER;
        Element element = new Element(rs.nElementCreate((long) dt.mID, dk.mID, false, 1), rs, dt, dk, false, 1);
        return element;
    }

    public static Element createVector(RenderScript rs, DataType dt, int size) {
        if (size < 2 || size > 4) {
            throw new RSIllegalArgumentException("Vector size out of range 2-4.");
        }
        switch (dt) {
            case FLOAT_16:
            case FLOAT_32:
            case FLOAT_64:
            case SIGNED_8:
            case SIGNED_16:
            case SIGNED_32:
            case SIGNED_64:
            case UNSIGNED_8:
            case UNSIGNED_16:
            case UNSIGNED_32:
            case UNSIGNED_64:
            case BOOLEAN:
                DataKind dk = DataKind.USER;
                Element element = new Element(rs.nElementCreate((long) dt.mID, dk.mID, false, size), rs, dt, dk, false, size);
                return element;
            default:
                throw new RSIllegalArgumentException("Cannot create vector of non-primitive type.");
        }
    }

    public static Element createPixel(RenderScript rs, DataType dt, DataKind dk) {
        if (dk != DataKind.PIXEL_L && dk != DataKind.PIXEL_A && dk != DataKind.PIXEL_LA && dk != DataKind.PIXEL_RGB && dk != DataKind.PIXEL_RGBA && dk != DataKind.PIXEL_DEPTH && dk != DataKind.PIXEL_YUV) {
            throw new RSIllegalArgumentException("Unsupported DataKind");
        } else if (dt != DataType.UNSIGNED_8 && dt != DataType.UNSIGNED_16 && dt != DataType.UNSIGNED_5_6_5 && dt != DataType.UNSIGNED_4_4_4_4 && dt != DataType.UNSIGNED_5_5_5_1) {
            throw new RSIllegalArgumentException("Unsupported DataType");
        } else if (dt == DataType.UNSIGNED_5_6_5 && dk != DataKind.PIXEL_RGB) {
            throw new RSIllegalArgumentException("Bad kind and type combo");
        } else if (dt == DataType.UNSIGNED_5_5_5_1 && dk != DataKind.PIXEL_RGBA) {
            throw new RSIllegalArgumentException("Bad kind and type combo");
        } else if (dt == DataType.UNSIGNED_4_4_4_4 && dk != DataKind.PIXEL_RGBA) {
            throw new RSIllegalArgumentException("Bad kind and type combo");
        } else if (dt != DataType.UNSIGNED_16 || dk == DataKind.PIXEL_DEPTH) {
            int size = 1;
            switch (dk) {
                case PIXEL_LA:
                    size = 2;
                    break;
                case PIXEL_RGB:
                    size = 3;
                    break;
                case PIXEL_RGBA:
                    size = 4;
                    break;
                case PIXEL_DEPTH:
                    size = 2;
                    break;
            }
            Element element = new Element(rs.nElementCreate((long) dt.mID, dk.mID, true, size), rs, dt, dk, true, size);
            return element;
        } else {
            throw new RSIllegalArgumentException("Bad kind and type combo");
        }
    }

    public boolean isCompatible(Element e) {
        boolean z = true;
        if (equals(e)) {
            return true;
        }
        if (!(this.mSize == e.mSize && this.mType != DataType.NONE && this.mType == e.mType && this.mVectorSize == e.mVectorSize)) {
            z = false;
        }
        return z;
    }
}
