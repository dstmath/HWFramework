package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.DERBitString;

public class PKIFailureInfo extends DERBitString {
    public static final int ADD_INFO_NOT_AVAILABLE = 4194304;
    public static final int BAD_ALG = 128;
    public static final int BAD_CERT_ID = 8;
    public static final int BAD_DATA_FORMAT = 4;
    public static final int BAD_MESSAGE_CHECK = 64;
    public static final int BAD_POP = 16384;
    public static final int BAD_REQUEST = 32;
    public static final int BAD_TIME = 16;
    public static final int INCORRECT_DATA = 1;
    public static final int MISSING_TIME_STAMP = 32768;
    public static final int SYSTEM_FAILURE = 1073741824;
    public static final int TIME_NOT_AVAILABLE = 512;
    public static final int UNACCEPTED_EXTENSION = 8388608;
    public static final int UNACCEPTED_POLICY = 256;
    public static final int WRONG_AUTHORITY = 2;
    public static final int addInfoNotAvailable = 4194304;
    public static final int badAlg = 128;
    public static final int badCertId = 8;
    public static final int badCertTemplate = 1048576;
    public static final int badDataFormat = 4;
    public static final int badMessageCheck = 64;
    public static final int badPOP = 16384;
    public static final int badRecipientNonce = 1024;
    public static final int badRequest = 32;
    public static final int badSenderNonce = 2097152;
    public static final int badTime = 16;
    public static final int certConfirmed = 4096;
    public static final int certRevoked = 8192;
    public static final int duplicateCertReq = 536870912;
    public static final int incorrectData = 1;
    public static final int missingTimeStamp = 32768;
    public static final int notAuthorized = 65536;
    public static final int signerNotTrusted = 524288;
    public static final int systemFailure = 1073741824;
    public static final int systemUnavail = Integer.MIN_VALUE;
    public static final int timeNotAvailable = 512;
    public static final int transactionIdInUse = 262144;
    public static final int unacceptedExtension = 8388608;
    public static final int unacceptedPolicy = 256;
    public static final int unsupportedVersion = 131072;
    public static final int wrongAuthority = 2;
    public static final int wrongIntegrity = 2048;

    public PKIFailureInfo(int i) {
        super(getBytes(i), getPadBits(i));
    }

    public PKIFailureInfo(DERBitString dERBitString) {
        super(dERBitString.getBytes(), dERBitString.getPadBits());
    }

    public String toString() {
        return "PKIFailureInfo: 0x" + Integer.toHexString(intValue());
    }
}
