package javax.obex;

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
    public static final byte OBEX_SRMP_WAIT = (byte) 1;
    public static final byte OBEX_SRM_DISABLE = (byte) 0;
    public static final byte OBEX_SRM_ENABLE = (byte) 1;
    public static final byte OBEX_SRM_SUPPORT = (byte) 2;
    private static final String TAG = "ObexHelper";
    public static final boolean VDBG = false;

    private ObexHelper() {
    }

    public static byte[] updateHeaderSet(HeaderSet header, byte[] headerArray) throws IOException {
        int index = OBEX_AUTH_REALM_CHARSET_ASCII;
        byte[] body = null;
        HeaderSet headerImpl = header;
        while (index < headerArray.length) {
            try {
                int headerID = headerArray[index] & OBEX_OPCODE_ABORT;
                byte[] value;
                Calendar temp;
                switch (headerID & ResponseCodes.OBEX_HTTP_BAD_REQUEST) {
                    case OBEX_AUTH_REALM_CHARSET_ASCII /*0*/:
                    case 64:
                        boolean trimTail = true;
                        index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                        index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                        int length = (((headerArray[index] & OBEX_OPCODE_ABORT) << OBEX_AUTH_REALM_CHARSET_ISO_8859_8) + (headerArray[index] & OBEX_OPCODE_ABORT)) - 3;
                        index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                        value = new byte[length];
                        System.arraycopy(headerArray, index, value, OBEX_AUTH_REALM_CHARSET_ASCII, length);
                        if (length == 0 || (length > 0 && value[length - 1] != null)) {
                            trimTail = false;
                        }
                        switch (headerID) {
                            case HeaderSet.TYPE /*66*/:
                                if (!trimTail) {
                                    header.setHeader(headerID, new String(value, OBEX_AUTH_REALM_CHARSET_ASCII, value.length, "ISO8859_1"));
                                    break;
                                }
                                header.setHeader(headerID, new String(value, OBEX_AUTH_REALM_CHARSET_ASCII, value.length - 1, "ISO8859_1"));
                                break;
                            case HeaderSet.TIME_ISO_8601 /*68*/:
                                String dateString = new String(value, "ISO8859_1");
                                temp = Calendar.getInstance();
                                if (dateString.length() == 16 && dateString.charAt(15) == 'Z') {
                                    temp.setTimeZone(TimeZone.getTimeZone("UTC"));
                                }
                                temp.set(OBEX_AUTH_REALM_CHARSET_ISO_8859_1, Integer.parseInt(dateString.substring(OBEX_AUTH_REALM_CHARSET_ASCII, OBEX_OPCODE_RESERVED)));
                                temp.set(OBEX_OPCODE_PUT, Integer.parseInt(dateString.substring(OBEX_OPCODE_RESERVED, OBEX_AUTH_REALM_CHARSET_ISO_8859_6)));
                                temp.set(OBEX_AUTH_REALM_CHARSET_ISO_8859_5, Integer.parseInt(dateString.substring(OBEX_AUTH_REALM_CHARSET_ISO_8859_6, OBEX_AUTH_REALM_CHARSET_ISO_8859_8)));
                                temp.set(11, Integer.parseInt(dateString.substring(OBEX_AUTH_REALM_CHARSET_ISO_8859_9, 11)));
                                temp.set(12, Integer.parseInt(dateString.substring(11, 13)));
                                temp.set(13, Integer.parseInt(dateString.substring(13, 15)));
                                header.setHeader(68, temp);
                                break;
                            case HeaderSet.BODY /*72*/:
                            case HeaderSet.END_OF_BODY /*73*/:
                                body = new byte[(length + OBEX_AUTH_REALM_CHARSET_ISO_8859_1)];
                                body[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) headerID;
                                System.arraycopy(headerArray, index, body, OBEX_AUTH_REALM_CHARSET_ISO_8859_1, length);
                                break;
                            case HeaderSet.AUTH_CHALLENGE /*77*/:
                                header.mAuthChall = new byte[length];
                                System.arraycopy(headerArray, index, header.mAuthChall, OBEX_AUTH_REALM_CHARSET_ASCII, length);
                                break;
                            case HeaderSet.AUTH_RESPONSE /*78*/:
                                header.mAuthResp = new byte[length];
                                System.arraycopy(headerArray, index, header.mAuthResp, OBEX_AUTH_REALM_CHARSET_ASCII, length);
                                break;
                            default:
                                if ((headerID & ResponseCodes.OBEX_HTTP_BAD_REQUEST) != 0) {
                                    header.setHeader(headerID, value);
                                    break;
                                }
                                header.setHeader(headerID, convertToUnicode(value, true));
                                break;
                        }
                        index += length;
                        break;
                    case OBEX_OPCODE_FINAL_BIT_MASK /*128*/:
                        index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                        try {
                            header.setHeader(headerID, Byte.valueOf(headerArray[index]));
                        } catch (Exception e) {
                        }
                        index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                        break;
                    case ResponseCodes.OBEX_HTTP_BAD_REQUEST /*192*/:
                        index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                        value = new byte[OBEX_OPCODE_RESERVED];
                        System.arraycopy(headerArray, index, value, OBEX_AUTH_REALM_CHARSET_ASCII, OBEX_OPCODE_RESERVED);
                        if (headerID == ResponseCodes.OBEX_HTTP_NOT_FOUND) {
                            temp = Calendar.getInstance();
                            temp.setTime(new Date(convertToLong(value) * 1000));
                            header.setHeader(ResponseCodes.OBEX_HTTP_NOT_FOUND, temp);
                        } else if (headerID == ResponseCodes.OBEX_HTTP_LENGTH_REQUIRED) {
                            header.mConnectionID = new byte[OBEX_OPCODE_RESERVED];
                            System.arraycopy(value, OBEX_AUTH_REALM_CHARSET_ASCII, header.mConnectionID, OBEX_AUTH_REALM_CHARSET_ASCII, OBEX_OPCODE_RESERVED);
                        } else {
                            header.setHeader(headerID, Long.valueOf(convertToLong(value)));
                        }
                        index += OBEX_OPCODE_RESERVED;
                        break;
                    default:
                        break;
                }
            } catch (Exception e2) {
                throw new IOException("Header was not formatted properly", e2);
            } catch (UnsupportedEncodingException e3) {
                throw e3;
            } catch (UnsupportedEncodingException e32) {
                throw e32;
            } catch (IOException e4) {
                throw new IOException("Header was not formatted properly", e4);
            }
        }
        return body;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] createHeader(HeaderSet head, boolean nullOut) {
        Throwable th;
        byte[] result;
        byte[] lengthArray = new byte[OBEX_OPCODE_PUT];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HeaderSet headImpl = head;
        try {
            byte[] value;
            int length;
            int i;
            Byte byteHeader;
            if (head.mConnectionID != null && head.getHeader(70) == null) {
                out.write(-53);
                out.write(head.mConnectionID);
            }
            Long intHeader = (Long) head.getHeader(ResponseCodes.OBEX_HTTP_BAD_REQUEST);
            if (intHeader != null) {
                out.write(-64);
                out.write(convertToByteArray(intHeader.longValue()));
                if (nullOut) {
                    head.setHeader(ResponseCodes.OBEX_HTTP_BAD_REQUEST, null);
                }
            }
            String stringHeader = (String) head.getHeader(OBEX_AUTH_REALM_CHARSET_ISO_8859_1);
            if (stringHeader != null) {
                out.write(OBEX_AUTH_REALM_CHARSET_ISO_8859_1);
                value = convertToUnicodeByteArray(stringHeader);
                length = value.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    head.setHeader(OBEX_AUTH_REALM_CHARSET_ISO_8859_1, null);
                }
            } else if (head.getEmptyNameHeader()) {
                out.write(OBEX_AUTH_REALM_CHARSET_ISO_8859_1);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = OBEX_SRM_DISABLE;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) 3;
                out.write(lengthArray);
            }
            stringHeader = (String) head.getHeader(66);
            if (stringHeader != null) {
                out.write(66);
                value = stringHeader.getBytes("ISO8859_1");
                length = value.length + OBEX_OPCODE_RESERVED;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                out.write(OBEX_AUTH_REALM_CHARSET_ASCII);
                if (nullOut) {
                    head.setHeader(66, null);
                }
            }
            intHeader = (Long) head.getHeader(ResponseCodes.OBEX_HTTP_FORBIDDEN);
            if (intHeader != null) {
                out.write(-61);
                out.write(convertToByteArray(intHeader.longValue()));
                if (nullOut) {
                    head.setHeader(ResponseCodes.OBEX_HTTP_FORBIDDEN, null);
                }
            }
            Calendar dateHeader = (Calendar) head.getHeader(68);
            if (dateHeader != null) {
                StringBuffer buffer = new StringBuffer();
                StringBuffer stringBuffer;
                try {
                    int temp = dateHeader.get(OBEX_AUTH_REALM_CHARSET_ISO_8859_1);
                    for (i = temp; i < 1000; i *= 10) {
                        buffer.append("0");
                    }
                    buffer.append(temp);
                    temp = dateHeader.get(OBEX_OPCODE_PUT);
                    if (temp < 10) {
                        buffer.append("0");
                    }
                    buffer.append(temp);
                    temp = dateHeader.get(OBEX_AUTH_REALM_CHARSET_ISO_8859_5);
                    if (temp < 10) {
                        buffer.append("0");
                    }
                    buffer.append(temp);
                    buffer.append("T");
                    temp = dateHeader.get(11);
                    if (temp < 10) {
                        buffer.append("0");
                    }
                    buffer.append(temp);
                    temp = dateHeader.get(12);
                    if (temp < 10) {
                        buffer.append("0");
                    }
                    buffer.append(temp);
                    temp = dateHeader.get(13);
                    if (temp < 10) {
                        buffer.append("0");
                    }
                    buffer.append(temp);
                    if (dateHeader.getTimeZone().getID().equals("UTC")) {
                        buffer.append("Z");
                    }
                    value = buffer.toString().getBytes("ISO8859_1");
                    length = value.length + OBEX_OPCODE_GET;
                    lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                    lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                    out.write(68);
                    out.write(lengthArray);
                    out.write(value);
                    if (nullOut) {
                        head.setHeader(68, null);
                        stringBuffer = buffer;
                    }
                } catch (UnsupportedEncodingException e) {
                    throw e;
                } catch (IOException e2) {
                } catch (Throwable th2) {
                    th = th2;
                    stringBuffer = buffer;
                    result = out.toByteArray();
                    try {
                        out.close();
                    } catch (Exception e3) {
                    }
                    throw th;
                }
            }
            dateHeader = (Calendar) head.getHeader(ResponseCodes.OBEX_HTTP_NOT_FOUND);
            if (dateHeader != null) {
                out.write(ResponseCodes.OBEX_HTTP_NOT_FOUND);
                out.write(convertToByteArray(dateHeader.getTime().getTime() / 1000));
                if (nullOut) {
                    head.setHeader(ResponseCodes.OBEX_HTTP_NOT_FOUND, null);
                }
            }
            stringHeader = (String) head.getHeader(OBEX_AUTH_REALM_CHARSET_ISO_8859_5);
            if (stringHeader != null) {
                out.write(OBEX_AUTH_REALM_CHARSET_ISO_8859_5);
                value = convertToUnicodeByteArray(stringHeader);
                length = value.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    head.setHeader(OBEX_AUTH_REALM_CHARSET_ISO_8859_5, null);
                }
            }
            value = (byte[]) head.getHeader(70);
            if (value != null) {
                out.write(70);
                length = value.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    head.setHeader(70, null);
                }
            }
            value = (byte[]) head.getHeader(71);
            if (value != null) {
                out.write(71);
                length = value.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    head.setHeader(71, null);
                }
            }
            value = (byte[]) head.getHeader(74);
            if (value != null) {
                out.write(74);
                length = value.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    head.setHeader(74, null);
                }
            }
            value = (byte[]) head.getHeader(76);
            if (value != null) {
                out.write(76);
                length = value.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    head.setHeader(76, null);
                }
            }
            value = (byte[]) head.getHeader(79);
            if (value != null) {
                out.write(79);
                length = value.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    head.setHeader(79, null);
                }
            }
            for (i = OBEX_AUTH_REALM_CHARSET_ASCII; i < 16; i += OBEX_AUTH_REALM_CHARSET_ISO_8859_1) {
                stringHeader = (String) head.getHeader(i + 48);
                if (stringHeader != null) {
                    out.write(((byte) i) + 48);
                    value = convertToUnicodeByteArray(stringHeader);
                    length = value.length + OBEX_OPCODE_GET;
                    lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                    lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                    out.write(lengthArray);
                    out.write(value);
                    if (nullOut) {
                        head.setHeader(i + 48, null);
                    }
                }
                value = (byte[]) head.getHeader(i + 112);
                if (value != null) {
                    out.write(((byte) i) + 112);
                    length = value.length + OBEX_OPCODE_GET;
                    lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                    lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                    out.write(lengthArray);
                    out.write(value);
                    if (nullOut) {
                        head.setHeader(i + 112, null);
                    }
                }
                byteHeader = (Byte) head.getHeader(i + ResponseCodes.OBEX_HTTP_MULT_CHOICE);
                if (byteHeader != null) {
                    out.write(((byte) i) + ResponseCodes.OBEX_HTTP_MULT_CHOICE);
                    out.write(byteHeader.byteValue());
                    if (nullOut) {
                        head.setHeader(i + ResponseCodes.OBEX_HTTP_MULT_CHOICE, null);
                    }
                }
                intHeader = (Long) head.getHeader(i + 240);
                if (intHeader != null) {
                    out.write(((byte) i) + 240);
                    out.write(convertToByteArray(intHeader.longValue()));
                    if (nullOut) {
                        head.setHeader(i + 240, null);
                    }
                }
            }
            if (head.mAuthChall != null) {
                out.write(77);
                length = head.mAuthChall.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(head.mAuthChall);
                if (nullOut) {
                    head.mAuthChall = null;
                }
            }
            if (head.mAuthResp != null) {
                out.write(78);
                length = head.mAuthResp.length + OBEX_OPCODE_GET;
                lengthArray[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((length >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & OBEX_OPCODE_ABORT);
                lengthArray[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) (length & OBEX_OPCODE_ABORT);
                out.write(lengthArray);
                out.write(head.mAuthResp);
                if (nullOut) {
                    head.mAuthResp = null;
                }
            }
            byteHeader = (Byte) head.getHeader(HeaderSet.SINGLE_RESPONSE_MODE);
            if (byteHeader != null) {
                out.write(-105);
                out.write(byteHeader.byteValue());
                if (nullOut) {
                    head.setHeader(HeaderSet.SINGLE_RESPONSE_MODE, null);
                }
            }
            byteHeader = (Byte) head.getHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER);
            if (byteHeader != null) {
                out.write(-104);
                out.write(byteHeader.byteValue());
                if (nullOut) {
                    head.setHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER, null);
                }
            }
            result = out.toByteArray();
            try {
                out.close();
            } catch (Exception e4) {
            }
        } catch (UnsupportedEncodingException e5) {
            throw e5;
        } catch (IOException e6) {
        } catch (Throwable th3) {
            th = th3;
            result = out.toByteArray();
            out.close();
            throw th;
        }
        return result;
    }

    public static int findHeaderEnd(byte[] headerArray, int start, int maxSize) {
        int fullLength = OBEX_AUTH_REALM_CHARSET_ASCII;
        int lastLength = -1;
        int index = start;
        while (fullLength < maxSize && index < headerArray.length) {
            lastLength = fullLength;
            switch ((headerArray[index] < null ? headerArray[index] + 256 : headerArray[index]) & ResponseCodes.OBEX_HTTP_BAD_REQUEST) {
                case OBEX_AUTH_REALM_CHARSET_ASCII /*0*/:
                case 64:
                    int length;
                    int i;
                    index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                    if (headerArray[index] < null) {
                        length = headerArray[index] + 256;
                    } else {
                        length = headerArray[index];
                    }
                    length <<= OBEX_AUTH_REALM_CHARSET_ISO_8859_8;
                    index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                    if (headerArray[index] < null) {
                        i = headerArray[index] + 256;
                    } else {
                        i = headerArray[index];
                    }
                    length = (length + i) - 3;
                    index = (index + OBEX_AUTH_REALM_CHARSET_ISO_8859_1) + length;
                    fullLength += length + OBEX_OPCODE_GET;
                    break;
                case OBEX_OPCODE_FINAL_BIT_MASK /*128*/:
                    index = (index + OBEX_AUTH_REALM_CHARSET_ISO_8859_1) + OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
                    fullLength += OBEX_OPCODE_PUT;
                    break;
                case ResponseCodes.OBEX_HTTP_BAD_REQUEST /*192*/:
                    index += OBEX_AUTH_REALM_CHARSET_ISO_8859_5;
                    fullLength += OBEX_AUTH_REALM_CHARSET_ISO_8859_5;
                    break;
                default:
                    break;
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
        byte[] b = new byte[OBEX_OPCODE_RESERVED];
        b[OBEX_AUTH_REALM_CHARSET_ASCII] = (byte) ((int) ((l >> 24) & 255));
        b[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) ((int) ((l >> 16) & 255));
        b[OBEX_OPCODE_PUT] = (byte) ((int) ((l >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8) & 255));
        b[OBEX_OPCODE_GET] = (byte) ((int) (255 & l));
        return b;
    }

    public static byte[] convertToUnicodeByteArray(String s) {
        if (s == null) {
            return null;
        }
        char[] c = s.toCharArray();
        byte[] result = new byte[((c.length * OBEX_OPCODE_PUT) + OBEX_OPCODE_PUT)];
        for (int i = OBEX_AUTH_REALM_CHARSET_ASCII; i < c.length; i += OBEX_AUTH_REALM_CHARSET_ISO_8859_1) {
            result[i * OBEX_OPCODE_PUT] = (byte) (c[i] >> OBEX_AUTH_REALM_CHARSET_ISO_8859_8);
            result[(i * OBEX_OPCODE_PUT) + OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) c[i];
        }
        result[result.length - 2] = OBEX_SRM_DISABLE;
        result[result.length - 1] = OBEX_SRM_DISABLE;
        return result;
    }

    public static byte[] getTagValue(byte tag, byte[] triplet) {
        int index = findTag(tag, triplet);
        if (index == -1) {
            return null;
        }
        index += OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
        int length = triplet[index] & OBEX_OPCODE_ABORT;
        byte[] result = new byte[length];
        System.arraycopy(triplet, index + OBEX_AUTH_REALM_CHARSET_ISO_8859_1, result, OBEX_AUTH_REALM_CHARSET_ASCII, length);
        return result;
    }

    public static int findTag(byte tag, byte[] value) {
        if (value == null) {
            return -1;
        }
        int index = OBEX_AUTH_REALM_CHARSET_ASCII;
        while (index < value.length && value[index] != tag) {
            index += (value[index + OBEX_AUTH_REALM_CHARSET_ISO_8859_1] & OBEX_OPCODE_ABORT) + OBEX_OPCODE_PUT;
        }
        if (index >= value.length) {
            return -1;
        }
        return index;
    }

    public static String convertToUnicode(byte[] b, boolean includesNull) {
        if (b == null || b.length == 0) {
            return null;
        }
        int arrayLength = b.length;
        if (arrayLength % OBEX_OPCODE_PUT != 0) {
            throw new IllegalArgumentException("Byte array not of a valid form");
        }
        arrayLength >>= OBEX_AUTH_REALM_CHARSET_ISO_8859_1;
        if (includesNull) {
            arrayLength--;
        }
        char[] c = new char[arrayLength];
        for (int i = OBEX_AUTH_REALM_CHARSET_ASCII; i < arrayLength; i += OBEX_AUTH_REALM_CHARSET_ISO_8859_1) {
            int upper = b[i * OBEX_OPCODE_PUT];
            int lower = b[(i * OBEX_OPCODE_PUT) + OBEX_AUTH_REALM_CHARSET_ISO_8859_1];
            if (upper < 0) {
                upper += 256;
            }
            if (lower < 0) {
                lower += 256;
            }
            if (upper == 0 && lower == 0) {
                return new String(c, OBEX_AUTH_REALM_CHARSET_ASCII, i);
            }
            c[i] = (char) ((upper << OBEX_AUTH_REALM_CHARSET_ISO_8859_8) | lower);
        }
        return new String(c);
    }

    public static byte[] computeMd5Hash(byte[] in) {
        try {
            return MessageDigest.getInstance("MD5").digest(in);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] computeAuthenticationChallenge(byte[] nonce, String realm, boolean access, boolean userID) throws IOException {
        if (nonce.length != 16) {
            throw new IllegalArgumentException("Nonce must be 16 bytes long");
        }
        byte[] authChall;
        if (realm == null) {
            authChall = new byte[21];
        } else if (realm.length() >= OBEX_OPCODE_ABORT) {
            throw new IllegalArgumentException("Realm must be less then 255 bytes");
        } else {
            authChall = new byte[(realm.length() + 24)];
            authChall[21] = OBEX_SRM_SUPPORT;
            authChall[22] = (byte) (realm.length() + OBEX_AUTH_REALM_CHARSET_ISO_8859_1);
            authChall[23] = OBEX_SRM_ENABLE;
            System.arraycopy(realm.getBytes("ISO8859_1"), OBEX_AUTH_REALM_CHARSET_ASCII, authChall, 24, realm.length());
        }
        authChall[OBEX_AUTH_REALM_CHARSET_ASCII] = OBEX_SRM_DISABLE;
        authChall[OBEX_AUTH_REALM_CHARSET_ISO_8859_1] = (byte) 16;
        System.arraycopy(nonce, OBEX_AUTH_REALM_CHARSET_ASCII, authChall, OBEX_OPCODE_PUT, 16);
        authChall[18] = OBEX_SRM_ENABLE;
        authChall[19] = OBEX_SRM_ENABLE;
        authChall[20] = OBEX_SRM_DISABLE;
        if (!access) {
            authChall[20] = (byte) (authChall[20] | OBEX_OPCODE_PUT);
        }
        if (userID) {
            authChall[20] = (byte) (authChall[20] | OBEX_AUTH_REALM_CHARSET_ISO_8859_1);
        }
        return authChall;
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
        if (size >= OBEX_OPCODE_ABORT) {
            return size;
        }
        throw new IllegalArgumentException(size + " is less that the lower limit: " + OBEX_OPCODE_ABORT);
    }
}
