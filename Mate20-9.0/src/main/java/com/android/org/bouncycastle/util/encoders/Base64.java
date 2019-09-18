package com.android.org.bouncycastle.util.encoders;

import com.android.org.bouncycastle.util.Strings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Base64 {
    private static final Encoder encoder = new Base64Encoder();

    public static String toBase64String(byte[] data) {
        return toBase64String(data, 0, data.length);
    }

    public static String toBase64String(byte[] data, int off, int length) {
        return Strings.fromByteArray(encode(data, off, length));
    }

    public static byte[] encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    public static byte[] encode(byte[] data, int off, int length) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(((length + 2) / 3) * 4);
        try {
            encoder.encode(data, off, length, bOut);
            return bOut.toByteArray();
        } catch (Exception e) {
            throw new EncoderException("exception encoding base64 string: " + e.getMessage(), e);
        }
    }

    public static int encode(byte[] data, OutputStream out) throws IOException {
        return encoder.encode(data, 0, data.length, out);
    }

    public static int encode(byte[] data, int off, int length, OutputStream out) throws IOException {
        return encoder.encode(data, off, length, out);
    }

    public static byte[] decode(byte[] data) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream((data.length / 4) * 3);
        try {
            encoder.decode(data, 0, data.length, bOut);
            return bOut.toByteArray();
        } catch (Exception e) {
            throw new DecoderException("unable to decode base64 data: " + e.getMessage(), e);
        }
    }

    public static byte[] decode(String data) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream((data.length() / 4) * 3);
        try {
            encoder.decode(data, bOut);
            return bOut.toByteArray();
        } catch (Exception e) {
            throw new DecoderException("unable to decode base64 string: " + e.getMessage(), e);
        }
    }

    public static int decode(String data, OutputStream out) throws IOException {
        return encoder.decode(data, out);
    }

    public static int decode(byte[] base64Data, int start, int length, OutputStream out) {
        try {
            return encoder.decode(base64Data, start, length, out);
        } catch (Exception e) {
            throw new DecoderException("unable to decode base64 data: " + e.getMessage(), e);
        }
    }
}
