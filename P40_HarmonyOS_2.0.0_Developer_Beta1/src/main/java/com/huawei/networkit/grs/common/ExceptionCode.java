package com.huawei.networkit.grs.common;

import android.text.TextUtils;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;

public class ExceptionCode {
    public static final int CANCEL = 1104;
    private static final String CONNECT = "connect";
    public static final int CONNECTION_ABORT = 110205;
    public static final int CONNECTION_REFUSED = 110209;
    public static final int CONNECTION_RESET = 110204;
    public static final int CONNECT_FAILED = 110206;
    public static final int CRASH_EXCEPTION = 1103;
    public static final int INTERRUPT_CONNECT_CLOSE = 110214;
    public static final int INTERRUPT_EXCEPTION = 110213;
    public static final int NETWORK_IO_EXCEPTION = 1102;
    public static final int NETWORK_UNREACHABLE = 110208;
    private static final String READ = "read";
    public static final int READ_ERROR = 110203;
    public static final int ROUTE_FAILED = 110207;
    public static final int SOCKET_CLOSE = 110215;
    public static final int SOCKET_CONNECT_TIMEOUT = 110221;
    public static final int SOCKET_READ_TIMEOUT = 110223;
    public static final int SOCKET_TIMEOUT = 110200;
    public static final int SOCKET_WRITE_TIMEOUT = 110225;
    public static final int SSL_HANDSHAKE_EXCEPTION = 110211;
    public static final int SSL_PEERUNVERIFIED_EXCEPTION = 110212;
    public static final int SSL_PROTOCOL_EXCEPTION = 110210;
    public static final int UNABLE_TO_RESOLVE_HOST = 110202;
    public static final int UNEXPECTED_EOF = 110201;
    private static final String WRITE = "write";

    public static int getErrorCodeFromException(Exception e) {
        if (e == null) {
            return NETWORK_IO_EXCEPTION;
        }
        if (!(e instanceof IOException)) {
            return CRASH_EXCEPTION;
        }
        String errorMessage = e.getMessage();
        if (errorMessage == null) {
            return NETWORK_IO_EXCEPTION;
        }
        String errorMessage2 = StringUtils.toLowerCase(errorMessage);
        int code = getErrorCodeFromMsg(errorMessage2);
        if (code != 1102) {
            return code;
        }
        if (e instanceof SocketTimeoutException) {
            return getErrorCodeSocketTimeout(e);
        }
        if (e instanceof ConnectException) {
            return CONNECT_FAILED;
        }
        if (e instanceof NoRouteToHostException) {
            return ROUTE_FAILED;
        }
        if (e instanceof SSLProtocolException) {
            return SSL_PROTOCOL_EXCEPTION;
        }
        if (e instanceof SSLHandshakeException) {
            return SSL_HANDSHAKE_EXCEPTION;
        }
        if (e instanceof SSLPeerUnverifiedException) {
            return SSL_PEERUNVERIFIED_EXCEPTION;
        }
        if (e instanceof UnknownHostException) {
            return UNABLE_TO_RESOLVE_HOST;
        }
        if (!(e instanceof InterruptedIOException)) {
            return code;
        }
        if (errorMessage2.contains("connection has been shut down")) {
            return INTERRUPT_CONNECT_CLOSE;
        }
        return INTERRUPT_EXCEPTION;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004f A[RETURN] */
    private static int getErrorCodeSocketTimeout(Exception e) {
        char c;
        String result = checkExceptionContainsKey(e, CONNECT, READ, WRITE);
        int hashCode = result.hashCode();
        if (hashCode != 3496342) {
            if (hashCode != 113399775) {
                if (hashCode == 951351530 && result.equals(CONNECT)) {
                    c = 0;
                    if (c != 0) {
                        return SOCKET_CONNECT_TIMEOUT;
                    }
                    if (c == 1) {
                        return SOCKET_READ_TIMEOUT;
                    }
                    if (c != 2) {
                        return SOCKET_TIMEOUT;
                    }
                    return SOCKET_WRITE_TIMEOUT;
                }
            } else if (result.equals(WRITE)) {
                c = 2;
                if (c != 0) {
                }
            }
        } else if (result.equals(READ)) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    private static int getErrorCodeFromMsg(String errorMessage) {
        if (errorMessage.contains("unexpected end of stream")) {
            return UNEXPECTED_EOF;
        }
        if (errorMessage.contains("unable to resolve host")) {
            return UNABLE_TO_RESOLVE_HOST;
        }
        if (errorMessage.contains("read error")) {
            return READ_ERROR;
        }
        if (errorMessage.contains("connection reset")) {
            return CONNECTION_RESET;
        }
        if (errorMessage.contains("software caused connection abort")) {
            return CONNECTION_ABORT;
        }
        if (errorMessage.contains("failed to connect to")) {
            return CONNECT_FAILED;
        }
        if (errorMessage.contains("connection refused")) {
            return CONNECTION_REFUSED;
        }
        if (errorMessage.contains("connection timed out")) {
            return SOCKET_CONNECT_TIMEOUT;
        }
        if (errorMessage.contains("no route to host")) {
            return ROUTE_FAILED;
        }
        if (errorMessage.contains("network is unreachable")) {
            return NETWORK_UNREACHABLE;
        }
        if (errorMessage.contains("socket closed")) {
            return SOCKET_CLOSE;
        }
        return NETWORK_IO_EXCEPTION;
    }

    private static String checkExceptionContainsKey(Exception e, String... keys) {
        String result = checkStrContainsKey(StringUtils.toLowerCase(e.getMessage()), keys);
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        for (StackTraceElement element : e.getStackTrace()) {
            result = checkStrContainsKey(StringUtils.toLowerCase(element.toString()), keys);
            if (!TextUtils.isEmpty(result)) {
                return result;
            }
        }
        return result;
    }

    private static String checkStrContainsKey(String str, String... keys) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        for (String key : keys) {
            if (str.contains(key)) {
                return key;
            }
        }
        return "";
    }
}
