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
    private Byte[] mByteUserDefined = new Byte[16];
    public byte[] mConnectionID;
    private Long mCount;
    private String mDescription;
    private boolean mEmptyName;
    private byte[] mHttpHeader;
    private Long[] mIntegerUserDefined = new Long[16];
    private Calendar mIsoTime;
    private Long mLength;
    private String mName;
    private byte[] mObjectClass;
    private SecureRandom mRandom = null;
    private byte[][] mSequenceUserDefined = new byte[16][];
    private Byte mSingleResponseMode;
    private Byte mSrmParam;
    private byte[] mTarget;
    private String mType;
    private String[] mUnicodeUserDefined = new String[16];
    private byte[] mWho;
    byte[] nonce;
    public int responseCode = -1;

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
            case 1:
                if (headerValue == null || ((headerValue instanceof String) ^ 1) == 0) {
                    this.mEmptyName = false;
                    this.mName = (String) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Name must be a String");
            case 5:
                if (headerValue == null || ((headerValue instanceof String) ^ 1) == 0) {
                    this.mDescription = (String) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Description must be a String");
            case TYPE /*66*/:
                if (headerValue == null || ((headerValue instanceof String) ^ 1) == 0) {
                    this.mType = (String) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Type must be a String");
            case TIME_ISO_8601 /*68*/:
                if (headerValue == null || ((headerValue instanceof Calendar) ^ 1) == 0) {
                    this.mIsoTime = (Calendar) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Time ISO 8601 must be a Calendar");
            case TARGET /*70*/:
                if (headerValue == null) {
                    this.mTarget = null;
                    return;
                } else if (headerValue instanceof byte[]) {
                    this.mTarget = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mTarget, 0, this.mTarget.length);
                    return;
                } else {
                    throw new IllegalArgumentException("Target must be a byte array");
                }
            case HTTP /*71*/:
                if (headerValue == null) {
                    this.mHttpHeader = null;
                    return;
                } else if (headerValue instanceof byte[]) {
                    this.mHttpHeader = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mHttpHeader, 0, this.mHttpHeader.length);
                    return;
                } else {
                    throw new IllegalArgumentException("HTTP must be a byte array");
                }
            case WHO /*74*/:
                if (headerValue == null) {
                    this.mWho = null;
                    return;
                } else if (headerValue instanceof byte[]) {
                    this.mWho = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mWho, 0, this.mWho.length);
                    return;
                } else {
                    throw new IllegalArgumentException("WHO must be a byte array");
                }
            case APPLICATION_PARAMETER /*76*/:
                if (headerValue == null) {
                    this.mAppParam = null;
                    return;
                } else if (headerValue instanceof byte[]) {
                    this.mAppParam = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mAppParam, 0, this.mAppParam.length);
                    return;
                } else {
                    throw new IllegalArgumentException("Application Parameter must be a byte array");
                }
            case OBJECT_CLASS /*79*/:
                if (headerValue == null) {
                    this.mObjectClass = null;
                    return;
                } else if (headerValue instanceof byte[]) {
                    this.mObjectClass = new byte[((byte[]) headerValue).length];
                    System.arraycopy(headerValue, 0, this.mObjectClass, 0, this.mObjectClass.length);
                    return;
                } else {
                    throw new IllegalArgumentException("Object Class must be a byte array");
                }
            case SINGLE_RESPONSE_MODE /*151*/:
                if (headerValue == null) {
                    this.mSingleResponseMode = null;
                    return;
                } else if (headerValue instanceof Byte) {
                    this.mSingleResponseMode = (Byte) headerValue;
                    return;
                } else {
                    throw new IllegalArgumentException("Single Response Mode must be a Byte");
                }
            case SINGLE_RESPONSE_MODE_PARAMETER /*152*/:
                if (headerValue == null) {
                    this.mSrmParam = null;
                    return;
                } else if (headerValue instanceof Byte) {
                    this.mSrmParam = (Byte) headerValue;
                    return;
                } else {
                    throw new IllegalArgumentException("Single Response Mode Parameter must be a Byte");
                }
            case 192:
                if (headerValue instanceof Long) {
                    temp = ((Long) headerValue).longValue();
                    if (temp < 0 || temp > 4294967295L) {
                        throw new IllegalArgumentException("Count must be between 0 and 0xFFFFFFFF");
                    }
                    this.mCount = (Long) headerValue;
                    return;
                } else if (headerValue == null) {
                    this.mCount = null;
                    return;
                } else {
                    throw new IllegalArgumentException("Count must be a Long");
                }
            case 195:
                if (headerValue instanceof Long) {
                    temp = ((Long) headerValue).longValue();
                    if (temp < 0 || temp > 4294967295L) {
                        throw new IllegalArgumentException("Length must be between 0 and 0xFFFFFFFF");
                    }
                    this.mLength = (Long) headerValue;
                    return;
                } else if (headerValue == null) {
                    this.mLength = null;
                    return;
                } else {
                    throw new IllegalArgumentException("Length must be a Long");
                }
            case 196:
                if (headerValue == null || ((headerValue instanceof Calendar) ^ 1) == 0) {
                    this.mByteTime = (Calendar) headerValue;
                    return;
                }
                throw new IllegalArgumentException("Time 4 Byte must be a Calendar");
            default:
                if (headerID < 48 || headerID > 63) {
                    if (headerID < 112 || headerID > 127) {
                        if (headerID < ResponseCodes.OBEX_HTTP_MULT_CHOICE || headerID > 191) {
                            if (headerID < 240 || headerID > 255) {
                                throw new IllegalArgumentException("Invalid Header Identifier");
                            } else if (headerValue instanceof Long) {
                                temp = ((Long) headerValue).longValue();
                                if (temp < 0 || temp > 4294967295L) {
                                    throw new IllegalArgumentException("Integer User Defined must be between 0 and 0xFFFFFFFF");
                                }
                                this.mIntegerUserDefined[headerID - 240] = (Long) headerValue;
                                return;
                            } else if (headerValue == null) {
                                this.mIntegerUserDefined[headerID - 240] = null;
                                return;
                            } else {
                                throw new IllegalArgumentException("Integer User Defined must be a Long");
                            }
                        } else if (headerValue == null || ((headerValue instanceof Byte) ^ 1) == 0) {
                            this.mByteUserDefined[headerID - 176] = (Byte) headerValue;
                            return;
                        } else {
                            throw new IllegalArgumentException("ByteUser Defined must be a Byte");
                        }
                    } else if (headerValue == null) {
                        this.mSequenceUserDefined[headerID - 112] = null;
                        return;
                    } else if (headerValue instanceof byte[]) {
                        this.mSequenceUserDefined[headerID - 112] = new byte[((byte[]) headerValue).length];
                        System.arraycopy(headerValue, 0, this.mSequenceUserDefined[headerID - 112], 0, this.mSequenceUserDefined[headerID - 112].length);
                        return;
                    } else {
                        throw new IllegalArgumentException("Byte Sequence User Defined must be a byte array");
                    }
                } else if (headerValue == null || ((headerValue instanceof String) ^ 1) == 0) {
                    this.mUnicodeUserDefined[headerID - 48] = (String) headerValue;
                    return;
                } else {
                    throw new IllegalArgumentException("Unicode String User Defined must be a String");
                }
        }
    }

    public Object getHeader(int headerID) throws IOException {
        switch (headerID) {
            case 1:
                return this.mName;
            case 5:
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
            case 192:
                return this.mCount;
            case 195:
                return this.mLength;
            case 196:
                return this.mByteTime;
            case 203:
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
                if (headerID >= 240 && headerID <= 255) {
                    return this.mIntegerUserDefined[headerID - 240];
                }
                throw new IllegalArgumentException("Invalid Header Identifier");
        }
    }

    public int[] getHeaderList() throws IOException {
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (this.mCount != null) {
            out.write(192);
        }
        if (this.mName != null) {
            out.write(1);
        }
        if (this.mType != null) {
            out.write(66);
        }
        if (this.mLength != null) {
            out.write(195);
        }
        if (this.mIsoTime != null) {
            out.write(68);
        }
        if (this.mByteTime != null) {
            out.write(196);
        }
        if (this.mDescription != null) {
            out.write(5);
        }
        if (this.mTarget != null) {
            out.write(70);
        }
        if (this.mHttpHeader != null) {
            out.write(71);
        }
        if (this.mWho != null) {
            out.write(74);
        }
        if (this.mAppParam != null) {
            out.write(76);
        }
        if (this.mObjectClass != null) {
            out.write(79);
        }
        if (this.mSingleResponseMode != null) {
            out.write(SINGLE_RESPONSE_MODE);
        }
        if (this.mSrmParam != null) {
            out.write(SINGLE_RESPONSE_MODE_PARAMETER);
        }
        for (i = 48; i < 64; i++) {
            if (this.mUnicodeUserDefined[i - 48] != null) {
                out.write(i);
            }
        }
        for (i = 112; i < 128; i++) {
            if (this.mSequenceUserDefined[i - 112] != null) {
                out.write(i);
            }
        }
        for (i = ResponseCodes.OBEX_HTTP_MULT_CHOICE; i < 192; i++) {
            if (this.mByteUserDefined[i - 176] != null) {
                out.write(i);
            }
        }
        for (i = 240; i < 256; i++) {
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
        for (i = 0; i < headers.length; i++) {
            result[i] = headers[i] & 255;
        }
        return result;
    }

    public void createAuthenticationChallenge(String realm, boolean userID, boolean access) throws IOException {
        this.nonce = new byte[16];
        if (this.mRandom == null) {
            this.mRandom = new SecureRandom();
        }
        for (int i = 0; i < 16; i++) {
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
