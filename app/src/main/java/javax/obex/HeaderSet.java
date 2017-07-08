package javax.obex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;

public final class HeaderSet {
    public static final int APPLICATION_PARAMETER = 76;
    public static final int AUTH_CHALLENGE = 77;
    public static final int AUTH_RESPONSE = 78;
    public static final int BODY = 72;
    public static final int CONNECTION_ID = 203;
    public static final int COUNT = 192;
    public static final int DESCRIPTION = 5;
    public static final int END_OF_BODY = 73;
    public static final int HTTP = 71;
    public static final int LENGTH = 195;
    public static final int NAME = 1;
    public static final int OBJECT_CLASS = 79;
    public static final int SINGLE_RESPONSE_MODE = 151;
    public static final int SINGLE_RESPONSE_MODE_PARAMETER = 152;
    public static final int TARGET = 70;
    public static final int TIME_4_BYTE = 196;
    public static final int TIME_ISO_8601 = 68;
    public static final int TYPE = 66;
    public static final int WHO = 74;
    private byte[] mAppParam;
    public byte[] mAuthChall;
    public byte[] mAuthResp;
    private Calendar mByteTime;
    private Byte[] mByteUserDefined;
    public byte[] mConnectionID;
    private Long mCount;
    private String mDescription;
    private boolean mEmptyName;
    private byte[] mHttpHeader;
    private Long[] mIntegerUserDefined;
    private Calendar mIsoTime;
    private Long mLength;
    private String mName;
    private byte[] mObjectClass;
    private SecureRandom mRandom;
    private byte[][] mSequenceUserDefined;
    private Byte mSingleResponseMode;
    private Byte mSrmParam;
    private byte[] mTarget;
    private String mType;
    private String[] mUnicodeUserDefined;
    private byte[] mWho;
    byte[] nonce;
    public int responseCode;

    public HeaderSet() {
        this.mRandom = null;
        this.mUnicodeUserDefined = new String[16];
        this.mSequenceUserDefined = new byte[16][];
        this.mByteUserDefined = new Byte[16];
        this.mIntegerUserDefined = new Long[16];
        this.responseCode = -1;
    }

    public void setEmptyNameHeader() {
        this.mName = null;
        this.mEmptyName = true;
    }

    public boolean getEmptyNameHeader() {
        return this.mEmptyName;
    }

    public void setHeader(int headerID, Object headerValue) {
        long temp;
        switch (headerID) {
            case NAME /*1*/:
                if (headerValue == null || (headerValue instanceof String)) {
                    this.mEmptyName = false;
                    this.mName = (String) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Name must be a String");
            case DESCRIPTION /*5*/:
                if (headerValue == null || (headerValue instanceof String)) {
                    this.mDescription = (String) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Description must be a String");
            case TYPE /*66*/:
                if (headerValue == null || (headerValue instanceof String)) {
                    this.mType = (String) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Type must be a String");
            case TIME_ISO_8601 /*68*/:
                if (headerValue == null || (headerValue instanceof Calendar)) {
                    this.mIsoTime = (Calendar) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Time ISO 8601 must be a Calendar");
            case TARGET /*70*/:
                if (headerValue == null) {
                    this.mTarget = null;
                } else if (headerValue instanceof byte[]) {
                    this.mTarget = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mTarget, 0, this.mTarget.length);
                } else {
                    throw new IllegalArgumentException("Target must be a byte array");
                }
            case HTTP /*71*/:
                if (headerValue == null) {
                    this.mHttpHeader = null;
                } else if (headerValue instanceof byte[]) {
                    this.mHttpHeader = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mHttpHeader, 0, this.mHttpHeader.length);
                } else {
                    throw new IllegalArgumentException("HTTP must be a byte array");
                }
            case WHO /*74*/:
                if (headerValue == null) {
                    this.mWho = null;
                } else if (headerValue instanceof byte[]) {
                    this.mWho = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mWho, 0, this.mWho.length);
                } else {
                    throw new IllegalArgumentException("WHO must be a byte array");
                }
            case APPLICATION_PARAMETER /*76*/:
                if (headerValue == null) {
                    this.mAppParam = null;
                } else if (headerValue instanceof byte[]) {
                    this.mAppParam = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mAppParam, 0, this.mAppParam.length);
                } else {
                    throw new IllegalArgumentException("Application Parameter must be a byte array");
                }
            case OBJECT_CLASS /*79*/:
                if (headerValue == null) {
                    this.mObjectClass = null;
                } else if (headerValue instanceof byte[]) {
                    this.mObjectClass = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mObjectClass, 0, this.mObjectClass.length);
                } else {
                    throw new IllegalArgumentException("Object Class must be a byte array");
                }
            case SINGLE_RESPONSE_MODE /*151*/:
                if (headerValue == null) {
                    this.mSingleResponseMode = null;
                } else if (headerValue instanceof Byte) {
                    this.mSingleResponseMode = (Byte) headerValue;
                } else {
                    throw new IllegalArgumentException("Single Response Mode must be a Byte");
                }
            case SINGLE_RESPONSE_MODE_PARAMETER /*152*/:
                if (headerValue == null) {
                    this.mSrmParam = null;
                } else if (headerValue instanceof Byte) {
                    this.mSrmParam = (Byte) headerValue;
                } else {
                    throw new IllegalArgumentException("Single Response Mode Parameter must be a Byte");
                }
            case COUNT /*192*/:
                if (headerValue instanceof Long) {
                    temp = ((Long) headerValue).longValue();
                    if (temp < 0 || temp > 4294967295L) {
                        throw new IllegalArgumentException("Count must be between 0 and 0xFFFFFFFF");
                    }
                    this.mCount = (Long) headerValue;
                } else if (headerValue == null) {
                    this.mCount = null;
                } else {
                    throw new IllegalArgumentException("Count must be a Long");
                }
            case LENGTH /*195*/:
                if (headerValue instanceof Long) {
                    temp = ((Long) headerValue).longValue();
                    if (temp < 0 || temp > 4294967295L) {
                        throw new IllegalArgumentException("Length must be between 0 and 0xFFFFFFFF");
                    }
                    this.mLength = (Long) headerValue;
                } else if (headerValue == null) {
                    this.mLength = null;
                } else {
                    throw new IllegalArgumentException("Length must be a Long");
                }
            case TIME_4_BYTE /*196*/:
                if (headerValue == null || (headerValue instanceof Calendar)) {
                    this.mByteTime = (Calendar) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Time 4 Byte must be a Calendar");
            default:
                if (headerID < 48 || headerID > 63) {
                    if (headerID < 112 || headerID > 127) {
                        if (headerID < ResponseCodes.OBEX_HTTP_MULT_CHOICE || headerID > 191) {
                            if (headerID < 240 || headerID > ObexHelper.OBEX_OPCODE_ABORT) {
                                throw new IllegalArgumentException("Invalid Header Identifier");
                            } else if (headerValue instanceof Long) {
                                temp = ((Long) headerValue).longValue();
                                if (temp < 0 || temp > 4294967295L) {
                                    throw new IllegalArgumentException("Integer User Defined must be between 0 and 0xFFFFFFFF");
                                }
                                this.mIntegerUserDefined[headerID - 240] = (Long) headerValue;
                            } else if (headerValue == null) {
                                this.mIntegerUserDefined[headerID - 240] = null;
                            } else {
                                throw new IllegalArgumentException("Integer User Defined must be a Long");
                            }
                        } else if (headerValue == null || (headerValue instanceof Byte)) {
                            this.mByteUserDefined[headerID - 176] = (Byte) headerValue;
                        } else {
                            throw new IllegalArgumentException("ByteUser Defined must be a Byte");
                        }
                    } else if (headerValue == null) {
                        this.mSequenceUserDefined[headerID - 112] = null;
                    } else if (headerValue instanceof byte[]) {
                        this.mSequenceUserDefined[headerID - 112] = new byte[((byte[]) headerValue).length];
                        System.arraycopy(headerValue, 0, this.mSequenceUserDefined[headerID - 112], 0, this.mSequenceUserDefined[headerID - 112].length);
                    } else {
                        throw new IllegalArgumentException("Byte Sequence User Defined must be a byte array");
                    }
                } else if (headerValue == null || (headerValue instanceof String)) {
                    this.mUnicodeUserDefined[headerID - 48] = (String) headerValue;
                } else {
                    throw new IllegalArgumentException("Unicode String User Defined must be a String");
                }
        }
    }

    public Object getHeader(int headerID) throws IOException {
        switch (headerID) {
            case NAME /*1*/:
                return this.mName;
            case DESCRIPTION /*5*/:
                return this.mDescription;
            case TYPE /*66*/:
                return this.mType;
            case TIME_ISO_8601 /*68*/:
                return this.mIsoTime;
            case TARGET /*70*/:
                return this.mTarget;
            case HTTP /*71*/:
                return this.mHttpHeader;
            case WHO /*74*/:
                return this.mWho;
            case APPLICATION_PARAMETER /*76*/:
                return this.mAppParam;
            case OBJECT_CLASS /*79*/:
                return this.mObjectClass;
            case SINGLE_RESPONSE_MODE /*151*/:
                return this.mSingleResponseMode;
            case SINGLE_RESPONSE_MODE_PARAMETER /*152*/:
                return this.mSrmParam;
            case COUNT /*192*/:
                return this.mCount;
            case LENGTH /*195*/:
                return this.mLength;
            case TIME_4_BYTE /*196*/:
                return this.mByteTime;
            case CONNECTION_ID /*203*/:
                return this.mConnectionID;
            default:
                if (headerID >= 48 && headerID <= 63) {
                    return this.mUnicodeUserDefined[headerID - 48];
                }
                if (headerID >= 112 && headerID <= 127) {
                    return this.mSequenceUserDefined[headerID - 112];
                }
                if (headerID >= ResponseCodes.OBEX_HTTP_MULT_CHOICE && headerID <= 191) {
                    return this.mByteUserDefined[headerID - 176];
                }
                if (headerID >= 240 && headerID <= ObexHelper.OBEX_OPCODE_ABORT) {
                    return this.mIntegerUserDefined[headerID - 240];
                }
                throw new IllegalArgumentException("Invalid Header Identifier");
        }
    }

    public int[] getHeaderList() throws IOException {
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (this.mCount != null) {
            out.write(COUNT);
        }
        if (this.mName != null) {
            out.write(NAME);
        }
        if (this.mType != null) {
            out.write(TYPE);
        }
        if (this.mLength != null) {
            out.write(LENGTH);
        }
        if (this.mIsoTime != null) {
            out.write(TIME_ISO_8601);
        }
        if (this.mByteTime != null) {
            out.write(TIME_4_BYTE);
        }
        if (this.mDescription != null) {
            out.write(DESCRIPTION);
        }
        if (this.mTarget != null) {
            out.write(TARGET);
        }
        if (this.mHttpHeader != null) {
            out.write(HTTP);
        }
        if (this.mWho != null) {
            out.write(WHO);
        }
        if (this.mAppParam != null) {
            out.write(APPLICATION_PARAMETER);
        }
        if (this.mObjectClass != null) {
            out.write(OBJECT_CLASS);
        }
        if (this.mSingleResponseMode != null) {
            out.write(SINGLE_RESPONSE_MODE);
        }
        if (this.mSrmParam != null) {
            out.write(SINGLE_RESPONSE_MODE_PARAMETER);
        }
        for (i = 48; i < 64; i += NAME) {
            if (this.mUnicodeUserDefined[i - 48] != null) {
                out.write(i);
            }
        }
        for (i = 112; i < ObexHelper.OBEX_OPCODE_FINAL_BIT_MASK; i += NAME) {
            if (this.mSequenceUserDefined[i - 112] != null) {
                out.write(i);
            }
        }
        for (i = ResponseCodes.OBEX_HTTP_MULT_CHOICE; i < COUNT; i += NAME) {
            if (this.mByteUserDefined[i - 176] != null) {
                out.write(i);
            }
        }
        for (i = 240; i < 256; i += NAME) {
            if (this.mIntegerUserDefined[i - 240] != null) {
                out.write(i);
            }
        }
        byte[] headers = out.toByteArray();
        out.close();
        if (headers == null || headers.length == 0) {
            return null;
        }
        int[] result = new int[headers.length];
        for (i = 0; i < headers.length; i += NAME) {
            result[i] = headers[i] & ObexHelper.OBEX_OPCODE_ABORT;
        }
        return result;
    }

    public void createAuthenticationChallenge(String realm, boolean userID, boolean access) throws IOException {
        this.nonce = new byte[16];
        if (this.mRandom == null) {
            this.mRandom = new SecureRandom();
        }
        for (int i = 0; i < 16; i += NAME) {
            this.nonce[i] = (byte) this.mRandom.nextInt();
        }
        this.mAuthChall = ObexHelper.computeAuthenticationChallenge(this.nonce, realm, access, userID);
    }

    public int getResponseCode() throws IOException {
        if (this.responseCode != -1) {
            return this.responseCode;
        }
        throw new IOException("May not be called on a server");
    }
}
