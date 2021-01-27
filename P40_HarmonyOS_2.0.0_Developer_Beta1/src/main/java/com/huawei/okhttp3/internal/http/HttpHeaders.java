package com.huawei.okhttp3.internal.http;

import com.huawei.android.smcs.SmartTrimProcessEvent;
import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.internal.telephony.PhoneConstantsEx;
import com.huawei.okhttp3.Challenge;
import com.huawei.okhttp3.Cookie;
import com.huawei.okhttp3.CookieJar;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.Buffer;
import com.huawei.okio.ByteString;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Deprecated
public final class HttpHeaders {
    private static final ByteString QUOTED_STRING_DELIMITERS = ByteString.encodeUtf8("\"\\");
    private static final ByteString TOKEN_DELIMITERS = ByteString.encodeUtf8("\t ,=");

    private HttpHeaders() {
    }

    public static long contentLength(Response response) {
        return contentLength(response.headers());
    }

    public static long contentLength(Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }

    private static long stringToLong(String s) {
        if (s == null) {
            return -1;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean varyMatches(Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!Objects.equals(cachedRequest.values(field), newRequest.headers(field))) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasVaryAll(Response response) {
        return hasVaryAll(response.headers());
    }

    public static boolean hasVaryAll(Headers responseHeaders) {
        return varyFields(responseHeaders).contains(PhoneConstantsEx.APN_TYPE_ALL);
    }

    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    public static Set<String> varyFields(Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        int size = responseHeaders.size();
        for (int i = 0; i < size; i++) {
            if ("Vary".equalsIgnoreCase(responseHeaders.name(i))) {
                String value = responseHeaders.value(i);
                if (result.isEmpty()) {
                    result = new TreeSet(String.CASE_INSENSITIVE_ORDER);
                }
                for (String varyField : value.split(SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN)) {
                    result.add(varyField.trim());
                }
            }
        }
        return result;
    }

    public static Headers varyHeaders(Response response) {
        return varyHeaders(response.networkResponse().request().headers(), response.headers());
    }

    public static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
        Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty()) {
            return Util.EMPTY_HEADERS;
        }
        Headers.Builder result = new Headers.Builder();
        int size = requestHeaders.size();
        for (int i = 0; i < size; i++) {
            String fieldName = requestHeaders.name(i);
            if (varyFields.contains(fieldName)) {
                result.add(fieldName, requestHeaders.value(i));
            }
        }
        return result.build();
    }

    public static List<Challenge> parseChallenges(Headers responseHeaders, String headerName) {
        List<Challenge> result = new ArrayList<>();
        for (int h = 0; h < responseHeaders.size(); h++) {
            if (headerName.equalsIgnoreCase(responseHeaders.name(h))) {
                parseChallengeHeader(result, new Buffer().writeUtf8(responseHeaders.value(h)));
            }
        }
        return result;
    }

    private static void parseChallengeHeader(List<Challenge> result, Buffer header) {
        String parameterValue;
        String peek = null;
        while (true) {
            if (peek == null) {
                skipWhitespaceAndCommas(header);
                peek = readToken(header);
                if (peek == null) {
                    return;
                }
            }
            boolean commaPrefixed = skipWhitespaceAndCommas(header);
            peek = readToken(header);
            if (peek != null) {
                int eqCount = skipAll(header, (byte) 61);
                boolean commaSuffixed = skipWhitespaceAndCommas(header);
                if (commaPrefixed || (!commaSuffixed && !header.exhausted())) {
                    Map<String, String> parameters = new LinkedHashMap<>();
                    int eqCount2 = eqCount + skipAll(header, (byte) 61);
                    while (true) {
                        if (peek == null) {
                            peek = readToken(header);
                            if (skipWhitespaceAndCommas(header)) {
                                break;
                            }
                            eqCount2 = skipAll(header, (byte) 61);
                        }
                        if (eqCount2 == 0) {
                            break;
                        } else if (eqCount2 <= 1 && !skipWhitespaceAndCommas(header)) {
                            if (header.exhausted() || header.getByte(0) != 34) {
                                parameterValue = readToken(header);
                            } else {
                                parameterValue = readQuotedString(header);
                            }
                            if (parameterValue != null) {
                                String replaced = parameters.put(peek, parameterValue);
                                peek = null;
                                if (replaced == null) {
                                    if (!skipWhitespaceAndCommas(header) && !header.exhausted()) {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                    result.add(new Challenge(peek, parameters));
                } else {
                    result.add(new Challenge(peek, Collections.singletonMap(null, peek + repeat('=', eqCount))));
                    peek = null;
                }
            } else if (header.exhausted()) {
                result.add(new Challenge(peek, Collections.emptyMap()));
                return;
            } else {
                return;
            }
        }
    }

    private static boolean skipWhitespaceAndCommas(Buffer buffer) {
        boolean commaFound = false;
        while (!buffer.exhausted()) {
            byte b = buffer.getByte(0);
            if (b != 44) {
                if (b != 32 && b != 9) {
                    break;
                }
                buffer.readByte();
            } else {
                buffer.readByte();
                commaFound = true;
            }
        }
        return commaFound;
    }

    private static int skipAll(Buffer buffer, byte b) {
        int count = 0;
        while (!buffer.exhausted() && buffer.getByte(0) == b) {
            count++;
            buffer.readByte();
        }
        return count;
    }

    private static String readQuotedString(Buffer buffer) {
        if (buffer.readByte() == 34) {
            Buffer result = new Buffer();
            while (true) {
                long i = buffer.indexOfElement(QUOTED_STRING_DELIMITERS);
                if (i == -1) {
                    return null;
                }
                if (buffer.getByte(i) == 34) {
                    result.write(buffer, i);
                    buffer.readByte();
                    return result.readUtf8();
                } else if (buffer.size() == i + 1) {
                    return null;
                } else {
                    result.write(buffer, i);
                    buffer.readByte();
                    result.write(buffer, 1L);
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String readToken(Buffer buffer) {
        try {
            long tokenSize = buffer.indexOfElement(TOKEN_DELIMITERS);
            if (tokenSize == -1) {
                tokenSize = buffer.size();
            }
            if (tokenSize != 0) {
                return buffer.readUtf8(tokenSize);
            }
            return null;
        } catch (EOFException e) {
            throw new AssertionError();
        }
    }

    private static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }

    public static void receiveHeaders(CookieJar cookieJar, HttpUrl url, Headers headers) {
        if (cookieJar != CookieJar.NO_COOKIES) {
            List<Cookie> cookies = Cookie.parseAll(url, headers);
            if (!cookies.isEmpty()) {
                cookieJar.saveFromResponse(url, cookies);
            }
        }
    }

    public static boolean hasBody(Response response) {
        if (response.request().method().equals("HEAD")) {
            return false;
        }
        int responseCode = response.code();
        if (((responseCode >= 100 && responseCode < 200) || responseCode == 204 || responseCode == 304) && contentLength(response) == -1 && !"chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return false;
        }
        return true;
    }

    public static int skipUntil(String input, int pos, String characters) {
        while (pos < input.length() && characters.indexOf(input.charAt(pos)) == -1) {
            pos++;
        }
        return pos;
    }

    public static int skipWhitespace(String input, int pos) {
        while (pos < input.length() && ((c = input.charAt(pos)) == ' ' || c == '\t')) {
            pos++;
        }
        return pos;
    }

    public static int parseSeconds(String value, int defaultValue) {
        try {
            long seconds = Long.parseLong(value);
            if (seconds > 2147483647L) {
                return SignalStrengthEx.INVALID;
            }
            if (seconds < 0) {
                return 0;
            }
            return (int) seconds;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
