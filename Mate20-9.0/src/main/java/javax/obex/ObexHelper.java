package javax.obex;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class ObexHelper {
    public static final int BASE_PACKET_LENGTH = 3;
    public static final int LOWER_LIMIT_MAX_PACKET_SIZE = 255;
    public static final int MAX_CLIENT_PACKET_SIZE = 64512;
    public static final int MAX_PACKET_SIZE_INT = 65534;
    public static final int OBEX_AUTH_REALM_CHARSET_ASCII = 0;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_1 = 1;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_2 = 2;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_3 = 3;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_4 = 4;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_5 = 5;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_6 = 6;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_7 = 7;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_8 = 8;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_9 = 9;
    public static final int OBEX_AUTH_REALM_CHARSET_UNICODE = 255;
    public static final int OBEX_BYTE_SEQ_HEADER_LEN = 3;
    public static final int OBEX_OPCODE_ABORT = 255;
    public static final int OBEX_OPCODE_CONNECT = 128;
    public static final int OBEX_OPCODE_DISCONNECT = 129;
    public static final int OBEX_OPCODE_FINAL_BIT_MASK = 128;
    public static final int OBEX_OPCODE_GET = 3;
    public static final int OBEX_OPCODE_GET_FINAL = 131;
    public static final int OBEX_OPCODE_PUT = 2;
    public static final int OBEX_OPCODE_PUT_FINAL = 130;
    public static final int OBEX_OPCODE_RESERVED = 4;
    public static final int OBEX_OPCODE_RESERVED_FINAL = 132;
    public static final int OBEX_OPCODE_SETPATH = 133;
    public static final byte OBEX_SRMP_WAIT = 1;
    public static final byte OBEX_SRM_DISABLE = 0;
    public static final byte OBEX_SRM_ENABLE = 1;
    public static final byte OBEX_SRM_SUPPORT = 2;
    private static final String TAG = "ObexHelper";
    public static final boolean VDBG = false;

    private ObexHelper() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x00cc A[Catch:{ UnsupportedEncodingException -> 0x0185, UnsupportedEncodingException -> 0x0175, Exception -> 0x006a, IOException -> 0x01a5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00d6 A[Catch:{ UnsupportedEncodingException -> 0x0185, UnsupportedEncodingException -> 0x0175, Exception -> 0x006a, IOException -> 0x01a5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00e1 A[Catch:{ UnsupportedEncodingException -> 0x0185, UnsupportedEncodingException -> 0x0175, Exception -> 0x006a, IOException -> 0x01a5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ec A[Catch:{ UnsupportedEncodingException -> 0x0185, UnsupportedEncodingException -> 0x0175, Exception -> 0x006a, IOException -> 0x01a5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00f9 A[SYNTHETIC, Splitter:B:54:0x00f9] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0177  */
    public static byte[] updateHeaderSet(HeaderSet header, byte[] headerArray) throws IOException {
        byte[] bArr = headerArray;
        byte[] body = null;
        int index = 0;
        HeaderSet headerImpl = header;
        while (true) {
            HeaderSet headerImpl2 = headerImpl;
            try {
                if (index >= bArr.length) {
                    return body;
                }
                int headerID = 255 & bArr[index];
                int i = headerID & 192;
                if (i == 0 || i == 64) {
                    boolean trimTail = true;
                    int index2 = index + 1;
                    int length = ((bArr[index2] & 255) << 8) + (255 & bArr[index2 + 1]);
                    index = index2 + 2;
                    if (length <= 3) {
                        Log.e(TAG, "Remote sent an OBEX packet with incorrect header length = " + length);
                    } else {
                        int length2 = length - 3;
                        byte[] value = new byte[length2];
                        System.arraycopy(bArr, index, value, 0, length2);
                        if (length2 != 0) {
                            if (length2 > 0 && value[length2 - 1] != 0) {
                            }
                            boolean trimTail2 = trimTail;
                            switch (headerID) {
                                case HeaderSet.TYPE /*66*/:
                                    if (trimTail2) {
                                        headerImpl2.setHeader(headerID, new String(value, 0, value.length - 1, "ISO8859_1"));
                                        break;
                                    } else {
                                        headerImpl2.setHeader(headerID, new String(value, 0, value.length, "ISO8859_1"));
                                        break;
                                    }
                                case HeaderSet.TIME_ISO_8601 /*68*/:
                                    String dateString = new String(value, "ISO8859_1");
                                    Calendar temp = Calendar.getInstance();
                                    if (dateString.length() == 16 && dateString.charAt(15) == 'Z') {
                                        temp.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    }
                                    temp.set(1, Integer.parseInt(dateString.substring(0, 4)));
                                    temp.set(2, Integer.parseInt(dateString.substring(4, 6)));
                                    temp.set(5, Integer.parseInt(dateString.substring(6, 8)));
                                    temp.set(11, Integer.parseInt(dateString.substring(9, 11)));
                                    temp.set(12, Integer.parseInt(dateString.substring(11, 13)));
                                    temp.set(13, Integer.parseInt(dateString.substring(13, 15)));
                                    headerImpl2.setHeader(68, temp);
                                    break;
                                case HeaderSet.BODY /*72*/:
                                case HeaderSet.END_OF_BODY /*73*/:
                                    body = new byte[(length2 + 1)];
                                    body[0] = (byte) headerID;
                                    System.arraycopy(bArr, index, body, 1, length2);
                                    break;
                                case HeaderSet.AUTH_CHALLENGE /*77*/:
                                    headerImpl2.mAuthChall = new byte[length2];
                                    System.arraycopy(bArr, index, headerImpl2.mAuthChall, 0, length2);
                                    break;
                                case HeaderSet.AUTH_RESPONSE /*78*/:
                                    headerImpl2.mAuthResp = new byte[length2];
                                    System.arraycopy(bArr, index, headerImpl2.mAuthResp, 0, length2);
                                    break;
                                default:
                                    if ((headerID & 192) != 0) {
                                        headerImpl2.setHeader(headerID, value);
                                        break;
                                    } else {
                                        headerImpl2.setHeader(headerID, convertToUnicode(value, true));
                                        break;
                                    }
                            }
                            index += length2;
                        }
                        trimTail = false;
                        boolean trimTail22 = trimTail;
                        switch (headerID) {
                            case HeaderSet.TYPE /*66*/:
                                break;
                            case HeaderSet.TIME_ISO_8601 /*68*/:
                                break;
                            case HeaderSet.BODY /*72*/:
                            case HeaderSet.END_OF_BODY /*73*/:
                                break;
                            case HeaderSet.AUTH_CHALLENGE /*77*/:
                                break;
                            case HeaderSet.AUTH_RESPONSE /*78*/:
                                break;
                        }
                        index += length2;
                    }
                } else if (i == 128) {
                    int index3 = index + 1;
                    try {
                        headerImpl2.setHeader(headerID, Byte.valueOf(bArr[index3]));
                    } catch (Exception e) {
                    }
                    index = index3 + 1;
                } else if (i == 192) {
                    int index4 = index + 1;
                    byte[] value2 = new byte[4];
                    System.arraycopy(bArr, index4, value2, 0, 4);
                    if (headerID == 196) {
                        Calendar temp2 = Calendar.getInstance();
                        temp2.setTime(new Date(convertToLong(value2) * 1000));
                        headerImpl2.setHeader(196, temp2);
                    } else if (headerID == 203) {
                        headerImpl2.mConnectionID = new byte[4];
                        System.arraycopy(value2, 0, headerImpl2.mConnectionID, 0, 4);
                    } else {
                        headerImpl2.setHeader(headerID, Long.valueOf(convertToLong(value2)));
                    }
                    index = index4 + 4;
                }
                headerImpl = headerImpl2;
            } catch (UnsupportedEncodingException e2) {
                throw e2;
            } catch (UnsupportedEncodingException e3) {
                throw e3;
            } catch (Exception e4) {
                throw new IOException("Header was not formatted properly", e4);
            } catch (IOException e5) {
                throw new IOException("Header was not formatted properly", e5);
            }
        }
    }

    public static byte[] createHeader(HeaderSet head, boolean nullOut) {
        byte[] result;
        Long intHeader = null;
        String stringHeader = null;
        Calendar dateHeader = null;
        StringBuffer buffer = null;
        byte[] value = null;
        byte[] lengthArray = new byte[2];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HeaderSet headImpl = head;
        try {
            if (headImpl.mConnectionID != null && headImpl.getHeader(70) == null) {
                out.write(-53);
                out.write(headImpl.mConnectionID);
            }
            intHeader = (Long) headImpl.getHeader(192);
            if (intHeader != null) {
                out.write(-64);
                value = convertToByteArray(intHeader.longValue());
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(192, null);
                }
            }
            String stringHeader2 = (String) headImpl.getHeader(1);
            if (stringHeader2 != null) {
                out.write(1);
                value = convertToUnicodeByteArray(stringHeader2);
                int length = value.length + 3;
                lengthArray[0] = (byte) (255 & (length >> 8));
                lengthArray[1] = (byte) (255 & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(1, null);
                }
            } else if (headImpl.getEmptyNameHeader() != 0) {
                out.write(1);
                lengthArray[0] = 0;
                lengthArray[1] = 3;
                out.write(lengthArray);
            }
            stringHeader = (String) headImpl.getHeader(66);
            if (stringHeader != null) {
                out.write(66);
                value = stringHeader.getBytes("ISO8859_1");
                int length2 = value.length + 4;
                lengthArray[0] = (byte) (255 & (length2 >> 8));
                lengthArray[1] = (byte) (255 & length2);
                out.write(lengthArray);
                out.write(value);
                out.write(0);
                if (nullOut) {
                    headImpl.setHeader(66, null);
                }
            }
            intHeader = (Long) headImpl.getHeader(195);
            if (intHeader != null) {
                out.write(-61);
                value = convertToByteArray(intHeader.longValue());
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(195, null);
                }
            }
            dateHeader = (Calendar) headImpl.getHeader(68);
            if (dateHeader != null) {
                buffer = new StringBuffer();
                int i = dateHeader.get(1);
                int temp = i;
                while (i < 1000) {
                    buffer.append("0");
                    i *= 10;
                }
                buffer.append(temp);
                int temp2 = dateHeader.get(2);
                if (temp2 < 10) {
                    buffer.append("0");
                }
                buffer.append(temp2);
                int temp3 = dateHeader.get(5);
                if (temp3 < 10) {
                    buffer.append("0");
                }
                buffer.append(temp3);
                buffer.append("T");
                int temp4 = dateHeader.get(11);
                if (temp4 < 10) {
                    buffer.append("0");
                }
                buffer.append(temp4);
                int temp5 = dateHeader.get(12);
                if (temp5 < 10) {
                    buffer.append("0");
                }
                buffer.append(temp5);
                int temp6 = dateHeader.get(13);
                if (temp6 < 10) {
                    buffer.append("0");
                }
                buffer.append(temp6);
                if (dateHeader.getTimeZone().getID().equals("UTC")) {
                    buffer.append("Z");
                }
                value = buffer.toString().getBytes("ISO8859_1");
                int length3 = value.length + 3;
                lengthArray[0] = (byte) ((length3 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length3);
                out.write(68);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(68, null);
                }
            }
            Calendar dateHeader2 = (Calendar) headImpl.getHeader(196);
            if (dateHeader2 != null) {
                out.write(196);
                out.write(convertToByteArray(dateHeader2.getTime().getTime() / 1000));
                if (nullOut) {
                    headImpl.setHeader(196, null);
                }
            }
            String stringHeader3 = (String) headImpl.getHeader(5);
            if (stringHeader3 != null) {
                out.write(5);
                byte[] value2 = convertToUnicodeByteArray(stringHeader3);
                int length4 = value2.length + 3;
                lengthArray[0] = (byte) ((length4 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length4);
                out.write(lengthArray);
                out.write(value2);
                if (nullOut) {
                    headImpl.setHeader(5, null);
                }
            }
            byte[] value3 = (byte[]) headImpl.getHeader(70);
            if (value3 != null) {
                out.write(70);
                int length5 = value3.length + 3;
                lengthArray[0] = (byte) ((length5 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length5);
                out.write(lengthArray);
                out.write(value3);
                if (nullOut) {
                    headImpl.setHeader(70, null);
                }
            }
            byte[] value4 = (byte[]) headImpl.getHeader(71);
            if (value4 != null) {
                out.write(71);
                int length6 = value4.length + 3;
                lengthArray[0] = (byte) ((length6 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length6);
                out.write(lengthArray);
                out.write(value4);
                if (nullOut) {
                    headImpl.setHeader(71, null);
                }
            }
            byte[] value5 = (byte[]) headImpl.getHeader(74);
            if (value5 != null) {
                out.write(74);
                int length7 = value5.length + 3;
                lengthArray[0] = (byte) ((length7 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length7);
                out.write(lengthArray);
                out.write(value5);
                if (nullOut) {
                    headImpl.setHeader(74, null);
                }
            }
            byte[] value6 = (byte[]) headImpl.getHeader(76);
            if (value6 != null) {
                out.write(76);
                int length8 = value6.length + 3;
                lengthArray[0] = (byte) ((length8 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length8);
                out.write(lengthArray);
                out.write(value6);
                if (nullOut) {
                    headImpl.setHeader(76, null);
                }
            }
            byte[] value7 = (byte[]) headImpl.getHeader(79);
            if (value7 != null) {
                out.write(79);
                int length9 = value7.length + 3;
                lengthArray[0] = (byte) ((length9 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length9);
                out.write(lengthArray);
                out.write(value7);
                if (nullOut) {
                    headImpl.setHeader(79, null);
                }
            }
            for (int i2 = 0; i2 < 16; i2++) {
                String stringHeader4 = (String) headImpl.getHeader(i2 + 48);
                if (stringHeader4 != null) {
                    out.write(((byte) i2) + 48);
                    byte[] value8 = convertToUnicodeByteArray(stringHeader4);
                    int length10 = value8.length + 3;
                    lengthArray[0] = (byte) ((length10 >> 8) & 255);
                    lengthArray[1] = (byte) (255 & length10);
                    out.write(lengthArray);
                    out.write(value8);
                    if (nullOut) {
                        headImpl.setHeader(i2 + 48, null);
                    }
                }
                byte[] value9 = (byte[]) headImpl.getHeader(i2 + 112);
                if (value9 != null) {
                    out.write(((byte) i2) + 112);
                    int length11 = value9.length + 3;
                    lengthArray[0] = (byte) ((length11 >> 8) & 255);
                    lengthArray[1] = (byte) (255 & length11);
                    out.write(lengthArray);
                    out.write(value9);
                    if (nullOut) {
                        headImpl.setHeader(i2 + 112, null);
                    }
                }
                Byte byteHeader = (Byte) headImpl.getHeader(i2 + ResponseCodes.OBEX_HTTP_MULT_CHOICE);
                if (byteHeader != null) {
                    out.write(((byte) i2) + 176);
                    out.write(byteHeader.byteValue());
                    if (nullOut) {
                        headImpl.setHeader(i2 + ResponseCodes.OBEX_HTTP_MULT_CHOICE, null);
                    }
                }
                Long intHeader2 = (Long) headImpl.getHeader(i2 + 240);
                if (intHeader2 != null) {
                    out.write(((byte) i2) + 240);
                    out.write(convertToByteArray(intHeader2.longValue()));
                    if (nullOut) {
                        headImpl.setHeader(i2 + 240, null);
                    }
                }
            }
            if (headImpl.mAuthChall != null) {
                out.write(77);
                int length12 = headImpl.mAuthChall.length + 3;
                lengthArray[0] = (byte) ((length12 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length12);
                out.write(lengthArray);
                out.write(headImpl.mAuthChall);
                if (nullOut) {
                    headImpl.mAuthChall = null;
                }
            }
            if (headImpl.mAuthResp != null) {
                out.write(78);
                int length13 = headImpl.mAuthResp.length + 3;
                lengthArray[0] = (byte) ((length13 >> 8) & 255);
                lengthArray[1] = (byte) (255 & length13);
                out.write(lengthArray);
                out.write(headImpl.mAuthResp);
                if (nullOut) {
                    headImpl.mAuthResp = null;
                }
            }
            Byte byteHeader2 = (Byte) headImpl.getHeader(HeaderSet.SINGLE_RESPONSE_MODE);
            if (byteHeader2 != null) {
                out.write(-105);
                out.write(byteHeader2.byteValue());
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.SINGLE_RESPONSE_MODE, null);
                }
            }
            Byte byteHeader3 = (Byte) headImpl.getHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER);
            if (byteHeader3 != null) {
                out.write(-104);
                out.write(byteHeader3.byteValue());
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER, null);
                }
            }
            result = out.toByteArray();
            try {
                out.close();
            } catch (Exception e) {
            }
        } catch (UnsupportedEncodingException e2) {
            throw e2;
        } catch (UnsupportedEncodingException e3) {
            throw e3;
        } catch (IOException e4) {
            result = out.toByteArray();
            out.close();
        } catch (Throwable th) {
            byte[] bArr = value;
            StringBuffer stringBuffer = buffer;
            Calendar calendar = dateHeader;
            String str = stringHeader;
            Long l = intHeader;
            Throwable th2 = th;
            byte[] result2 = out.toByteArray();
            try {
                out.close();
            } catch (Exception e5) {
            }
            throw th2;
        }
        return result;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v5, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v12, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v13, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v14, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v15, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v9, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v10, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static int findHeaderEnd(byte[] headerArray, int start, int maxSize) {
        int length;
        int i;
        int fullLength = 0;
        int lastLength = -1;
        int index = start;
        while (fullLength < maxSize && index < headerArray.length) {
            lastLength = fullLength;
            int i2 = (headerArray[index] < 0 ? headerArray[index] + 256 : headerArray[index]) & 192;
            if (i2 == 0 || i2 == 64) {
                int index2 = index + 1;
                if (headerArray[index2] < 0) {
                    length = headerArray[index2] + 256;
                } else {
                    length = headerArray[index2];
                }
                int length2 = length << 8;
                int index3 = index2 + 1;
                if (headerArray[index3] < 0) {
                    i = headerArray[index3] + 256;
                } else {
                    i = headerArray[index3];
                }
                int length3 = (length2 + i) - 3;
                index = index3 + 1 + length3;
                fullLength += length3 + 3;
            } else if (i2 == 128) {
                index = index + 1 + 1;
                fullLength += 2;
            } else if (i2 == 192) {
                index += 5;
                fullLength += 5;
            }
        }
        if (lastLength != 0) {
            return lastLength + start;
        }
        if (fullLength < maxSize) {
            return headerArray.length;
        }
        return -1;
    }

    public static long convertToLong(byte[] b) {
        long result = 0;
        long power = 0;
        for (int i = b.length - 1; i >= 0; i--) {
            long value = (long) b[i];
            if (value < 0) {
                value += 256;
            }
            result |= value << ((int) power);
            power += 8;
        }
        return result;
    }

    public static byte[] convertToByteArray(long l) {
        return new byte[]{(byte) ((int) ((l >> 24) & 255)), (byte) ((int) ((l >> 16) & 255)), (byte) ((int) ((l >> 8) & 255)), (byte) ((int) (255 & l))};
    }

    public static byte[] convertToUnicodeByteArray(String s) {
        if (s == null) {
            return null;
        }
        char[] c = s.toCharArray();
        byte[] result = new byte[((c.length * 2) + 2)];
        for (int i = 0; i < c.length; i++) {
            result[i * 2] = (byte) (c[i] >> 8);
            result[(i * 2) + 1] = (byte) c[i];
        }
        result[result.length - 2] = 0;
        result[result.length - 1] = 0;
        return result;
    }

    public static byte[] getTagValue(byte tag, byte[] triplet) {
        int index = findTag(tag, triplet);
        if (index == -1) {
            return null;
        }
        int index2 = index + 1;
        int length = triplet[index2] & 255;
        byte[] result = new byte[length];
        System.arraycopy(triplet, index2 + 1, result, 0, length);
        return result;
    }

    public static int findTag(byte tag, byte[] value) {
        if (value == null) {
            return -1;
        }
        int index = 0;
        while (index < value.length && value[index] != tag) {
            index += (value[index + 1] & 255) + 2;
        }
        if (index >= value.length) {
            return -1;
        }
        return index;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v5, resolved type: byte} */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r4v3, types: [byte, int] */
    /* JADX WARNING: Multi-variable type inference failed */
    public static String convertToUnicode(byte[] b, boolean includesNull) {
        if (b == null || b.length == 0) {
            return null;
        }
        int arrayLength = b.length;
        if (arrayLength % 2 == 0) {
            int arrayLength2 = arrayLength >> 1;
            if (includesNull) {
                arrayLength2--;
            }
            char[] c = new char[arrayLength2];
            for (int i = 0; i < arrayLength2; i++) {
                byte upper = b[2 * i];
                int lower = b[(2 * i) + 1];
                int upper2 = upper;
                if (upper < 0) {
                    upper2 = upper + 256;
                }
                if (lower < 0) {
                    lower += 256;
                }
                if (upper2 == 0 && lower == 0) {
                    return new String(c, 0, i);
                }
                c[i] = (char) ((upper2 << 8) | lower);
            }
            return new String(c);
        }
        throw new IllegalArgumentException("Byte array not of a valid form");
    }

    public static byte[] computeMd5Hash(byte[] in) {
        try {
            return MessageDigest.getInstance("MD5").digest(in);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] computeAuthenticationChallenge(byte[] nonce, String realm, boolean access, boolean userID) throws IOException {
        byte[] authChall;
        if (nonce.length == 16) {
            if (realm == null) {
                authChall = new byte[21];
            } else if (realm.length() < 255) {
                authChall = new byte[(realm.length() + 24)];
                authChall[21] = 2;
                authChall[22] = (byte) (realm.length() + 1);
                authChall[23] = 1;
                System.arraycopy(realm.getBytes("ISO8859_1"), 0, authChall, 24, realm.length());
            } else {
                throw new IllegalArgumentException("Realm must be less then 255 bytes");
            }
            authChall[0] = 0;
            authChall[1] = 16;
            System.arraycopy(nonce, 0, authChall, 2, 16);
            authChall[18] = 1;
            authChall[19] = 1;
            authChall[20] = 0;
            if (!access) {
                authChall[20] = (byte) (authChall[20] | 2);
            }
            if (userID) {
                authChall[20] = (byte) (authChall[20] | 1);
            }
            return authChall;
        }
        throw new IllegalArgumentException("Nonce must be 16 bytes long");
    }

    public static int getMaxTxPacketSize(ObexTransport transport) {
        return validateMaxPacketSize(transport.getMaxTransmitPacketSize());
    }

    public static int getMaxRxPacketSize(ObexTransport transport) {
        return validateMaxPacketSize(transport.getMaxReceivePacketSize());
    }

    private static int validateMaxPacketSize(int size) {
        if (size == -1) {
            return MAX_PACKET_SIZE_INT;
        }
        if (size >= 255) {
            return size;
        }
        throw new IllegalArgumentException(size + " is less that the lower limit: " + 255);
    }
}
