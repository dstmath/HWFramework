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
        if (headerID != 1) {
            if (headerID != 5) {
                if (headerID != 66) {
                    if (headerID != 68) {
                        if (headerID != 74) {
                            if (headerID != 76) {
                                if (headerID != 79) {
                                    if (headerID != 192) {
                                        if (headerID != 70) {
                                            if (headerID != 71) {
                                                if (headerID != 151) {
                                                    if (headerID != 152) {
                                                        if (headerID != 195) {
                                                            if (headerID != 196) {
                                                                if (headerID < 48 || headerID > 63) {
                                                                    if (headerID < 112 || headerID > 127) {
                                                                        if (headerID < 176 || headerID > 191) {
                                                                            if (headerID < 240 || headerID > 255) {
                                                                                throw new IllegalArgumentException("Invalid Header Identifier");
                                                                            } else if (headerValue instanceof Long) {
                                                                                long temp = ((Long) headerValue).longValue();
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
                                                                        byte[][] bArr = this.mSequenceUserDefined;
                                                                        bArr[headerID - 112] = new byte[((byte[]) headerValue).length];
                                                                        System.arraycopy(headerValue, 0, bArr[headerID - 112], 0, bArr[headerID - 112].length);
                                                                    } else {
                                                                        throw new IllegalArgumentException("Byte Sequence User Defined must be a byte array");
                                                                    }
                                                                } else if (headerValue == null || (headerValue instanceof String)) {
                                                                    this.mUnicodeUserDefined[headerID - 48] = (String) headerValue;
                                                                } else {
                                                                    throw new IllegalArgumentException("Unicode String User Defined must be a String");
                                                                }
                                                            } else if (headerValue == null || (headerValue instanceof Calendar)) {
                                                                this.mByteTime = (Calendar) headerValue;
                                                            } else {
                                                                throw new IllegalArgumentException("Time 4 Byte must be a Calendar");
                                                            }
                                                        } else if (headerValue instanceof Long) {
                                                            long temp2 = ((Long) headerValue).longValue();
                                                            if (temp2 < 0 || temp2 > 4294967295L) {
                                                                throw new IllegalArgumentException("Length must be between 0 and 0xFFFFFFFF");
                                                            }
                                                            this.mLength = (Long) headerValue;
                                                        } else if (headerValue == null) {
                                                            this.mLength = null;
                                                        } else {
                                                            throw new IllegalArgumentException("Length must be a Long");
                                                        }
                                                    } else if (headerValue == null) {
                                                        this.mSrmParam = null;
                                                    } else if (headerValue instanceof Byte) {
                                                        this.mSrmParam = (Byte) headerValue;
                                                    } else {
                                                        throw new IllegalArgumentException("Single Response Mode Parameter must be a Byte");
                                                    }
                                                } else if (headerValue == null) {
                                                    this.mSingleResponseMode = null;
                                                } else if (headerValue instanceof Byte) {
                                                    this.mSingleResponseMode = (Byte) headerValue;
                                                } else {
                                                    throw new IllegalArgumentException("Single Response Mode must be a Byte");
                                                }
                                            } else if (headerValue == null) {
                                                this.mHttpHeader = null;
                                            } else if (headerValue instanceof byte[]) {
                                                this.mHttpHeader = new byte[((byte[]) headerValue).length];
                                                byte[] bArr2 = this.mHttpHeader;
                                                System.arraycopy(headerValue, 0, bArr2, 0, bArr2.length);
                                            } else {
                                                throw new IllegalArgumentException("HTTP must be a byte array");
                                            }
                                        } else if (headerValue == null) {
                                            this.mTarget = null;
                                        } else if (headerValue instanceof byte[]) {
                                            this.mTarget = new byte[((byte[]) headerValue).length];
                                            byte[] bArr3 = this.mTarget;
                                            System.arraycopy(headerValue, 0, bArr3, 0, bArr3.length);
                                        } else {
                                            throw new IllegalArgumentException("Target must be a byte array");
                                        }
                                    } else if (headerValue instanceof Long) {
                                        long temp3 = ((Long) headerValue).longValue();
                                        if (temp3 < 0 || temp3 > 4294967295L) {
                                            throw new IllegalArgumentException("Count must be between 0 and 0xFFFFFFFF");
                                        }
                                        this.mCount = (Long) headerValue;
                                    } else if (headerValue == null) {
                                        this.mCount = null;
                                    } else {
                                        throw new IllegalArgumentException("Count must be a Long");
                                    }
                                } else if (headerValue == null) {
                                    this.mObjectClass = null;
                                } else if (headerValue instanceof byte[]) {
                                    this.mObjectClass = new byte[((byte[]) headerValue).length];
                                    byte[] bArr4 = this.mObjectClass;
                                    System.arraycopy(headerValue, 0, bArr4, 0, bArr4.length);
                                } else {
                                    throw new IllegalArgumentException("Object Class must be a byte array");
                                }
                            } else if (headerValue == null) {
                                this.mAppParam = null;
                            } else if (headerValue instanceof byte[]) {
                                this.mAppParam = new byte[((byte[]) headerValue).length];
                                byte[] bArr5 = this.mAppParam;
                                System.arraycopy(headerValue, 0, bArr5, 0, bArr5.length);
                            } else {
                                throw new IllegalArgumentException("Application Parameter must be a byte array");
                            }
                        } else if (headerValue == null) {
                            this.mWho = null;
                        } else if (headerValue instanceof byte[]) {
                            this.mWho = new byte[((byte[]) headerValue).length];
                            byte[] bArr6 = this.mWho;
                            System.arraycopy(headerValue, 0, bArr6, 0, bArr6.length);
                        } else {
                            throw new IllegalArgumentException("WHO must be a byte array");
                        }
                    } else if (headerValue == null || (headerValue instanceof Calendar)) {
                        this.mIsoTime = (Calendar) headerValue;
                    } else {
                        throw new IllegalArgumentException("Time ISO 8601 must be a Calendar");
                    }
                } else if (headerValue == null || (headerValue instanceof String)) {
                    this.mType = (String) headerValue;
                } else {
                    throw new IllegalArgumentException("Type must be a String");
                }
            } else if (headerValue == null || (headerValue instanceof String)) {
                this.mDescription = (String) headerValue;
            } else {
                throw new IllegalArgumentException("Description must be a String");
            }
        } else if (headerValue == null || (headerValue instanceof String)) {
            this.mEmptyName = false;
            this.mName = (String) headerValue;
        } else {
            throw new IllegalArgumentException("Name must be a String");
        }
    }

    public Object getHeader(int headerID) throws IOException {
        if (headerID == 1) {
            return this.mName;
        }
        if (headerID == 5) {
            return this.mDescription;
        }
        if (headerID == 66) {
            return this.mType;
        }
        if (headerID == 68) {
            return this.mIsoTime;
        }
        if (headerID == 74) {
            return this.mWho;
        }
        if (headerID == 76) {
            return this.mAppParam;
        }
        if (headerID == 79) {
            return this.mObjectClass;
        }
        if (headerID == 192) {
            return this.mCount;
        }
        if (headerID == 203) {
            return this.mConnectionID;
        }
        if (headerID == 70) {
            return this.mTarget;
        }
        if (headerID == 71) {
            return this.mHttpHeader;
        }
        if (headerID == 151) {
            return this.mSingleResponseMode;
        }
        if (headerID == 152) {
            return this.mSrmParam;
        }
        if (headerID == 195) {
            return this.mLength;
        }
        if (headerID == 196) {
            return this.mByteTime;
        }
        if (headerID >= 48 && headerID <= 63) {
            return this.mUnicodeUserDefined[headerID - 48];
        }
        if (headerID >= 112 && headerID <= 127) {
            return this.mSequenceUserDefined[headerID - 112];
        }
        if (headerID >= 176 && headerID <= 191) {
            return this.mByteUserDefined[headerID - 176];
        }
        if (headerID >= 240 && headerID <= 255) {
            return this.mIntegerUserDefined[headerID - 240];
        }
        throw new IllegalArgumentException("Invalid Header Identifier");
    }

    public int[] getHeaderList() throws IOException {
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
        for (int i = 48; i < 64; i++) {
            if (this.mUnicodeUserDefined[i - 48] != null) {
                out.write(i);
            }
        }
        for (int i2 = 112; i2 < 128; i2++) {
            if (this.mSequenceUserDefined[i2 - 112] != null) {
                out.write(i2);
            }
        }
        for (int i3 = ResponseCodes.OBEX_HTTP_MULT_CHOICE; i3 < 192; i3++) {
            if (this.mByteUserDefined[i3 - 176] != null) {
                out.write(i3);
            }
        }
        for (int i4 = 240; i4 < 256; i4++) {
            if (this.mIntegerUserDefined[i4 - 240] != null) {
                out.write(i4);
            }
        }
        byte[] headers = out.toByteArray();
        out.close();
        if (headers == null || headers.length == 0) {
            return null;
        }
        int[] result = new int[headers.length];
        for (int i5 = 0; i5 < headers.length; i5++) {
            result[i5] = headers[i5] & 255;
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
        int i = this.responseCode;
        if (i != -1) {
            return i;
        }
        throw new IOException("May not be called on a server");
    }
}
