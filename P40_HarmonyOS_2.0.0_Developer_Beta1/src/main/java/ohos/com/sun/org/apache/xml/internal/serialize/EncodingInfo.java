package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import ohos.com.sun.org.apache.xerces.internal.util.EncodingMap;

public class EncodingInfo {
    private Object[] fArgsForMethod = null;
    Object fCharToByteConverter = null;
    Object fCharsetEncoder = null;
    boolean fHaveTriedCToB = false;
    boolean fHaveTriedCharsetEncoder = false;
    String ianaName;
    String javaName;
    int lastPrintable;

    public EncodingInfo(String str, String str2, int i) {
        this.ianaName = str;
        this.javaName = EncodingMap.getIANA2JavaMapping(str);
        this.lastPrintable = i;
    }

    public String getIANAName() {
        return this.ianaName;
    }

    public Writer getWriter(OutputStream outputStream) throws UnsupportedEncodingException {
        String str = this.javaName;
        if (str != null) {
            return new OutputStreamWriter(outputStream, str);
        }
        this.javaName = EncodingMap.getIANA2JavaMapping(this.ianaName);
        String str2 = this.javaName;
        if (str2 == null) {
            return new OutputStreamWriter(outputStream, "UTF8");
        }
        return new OutputStreamWriter(outputStream, str2);
    }

    public boolean isPrintable(char c) {
        if (c <= this.lastPrintable) {
            return true;
        }
        return isPrintable0(c);
    }

    private boolean isPrintable0(char c) {
        if (this.fCharsetEncoder == null && CharsetMethods.fgNIOCharsetAvailable && !this.fHaveTriedCharsetEncoder) {
            if (this.fArgsForMethod == null) {
                this.fArgsForMethod = new Object[1];
            }
            try {
                this.fArgsForMethod[0] = this.javaName;
                Object invoke = CharsetMethods.fgCharsetForNameMethod.invoke(null, this.fArgsForMethod);
                if (((Boolean) CharsetMethods.fgCharsetCanEncodeMethod.invoke(invoke, null)).booleanValue()) {
                    this.fCharsetEncoder = CharsetMethods.fgCharsetNewEncoderMethod.invoke(invoke, null);
                } else {
                    this.fHaveTriedCharsetEncoder = true;
                }
            } catch (Exception unused) {
                this.fHaveTriedCharsetEncoder = true;
            }
        }
        if (this.fCharsetEncoder != null) {
            try {
                this.fArgsForMethod[0] = new Character(c);
                return ((Boolean) CharsetMethods.fgCharsetEncoderCanEncodeMethod.invoke(this.fCharsetEncoder, this.fArgsForMethod)).booleanValue();
            } catch (Exception unused2) {
                this.fCharsetEncoder = null;
                this.fHaveTriedCharsetEncoder = false;
            }
        }
        if (this.fCharToByteConverter == null) {
            if (!this.fHaveTriedCToB && CharToByteConverterMethods.fgConvertersAvailable) {
                if (this.fArgsForMethod == null) {
                    this.fArgsForMethod = new Object[1];
                }
                try {
                    this.fArgsForMethod[0] = this.javaName;
                    this.fCharToByteConverter = CharToByteConverterMethods.fgGetConverterMethod.invoke(null, this.fArgsForMethod);
                } catch (Exception unused3) {
                    this.fHaveTriedCToB = true;
                }
            }
            return false;
        }
        try {
            this.fArgsForMethod[0] = new Character(c);
            return ((Boolean) CharToByteConverterMethods.fgCanConvertMethod.invoke(this.fCharToByteConverter, this.fArgsForMethod)).booleanValue();
        } catch (Exception unused4) {
            this.fCharToByteConverter = null;
            this.fHaveTriedCToB = false;
            return false;
        }
    }

    public static void testJavaEncodingName(String str) throws UnsupportedEncodingException {
        new String("valid".getBytes(), str);
    }

    /* access modifiers changed from: package-private */
    public static class CharsetMethods {
        private static Method fgCharsetCanEncodeMethod = null;
        private static Method fgCharsetEncoderCanEncodeMethod = null;
        private static Method fgCharsetForNameMethod = null;
        private static Method fgCharsetNewEncoderMethod = null;
        private static boolean fgNIOCharsetAvailable = false;

        private CharsetMethods() {
        }

        static {
            try {
                Class<?> cls = Class.forName("java.nio.charset.Charset");
                Class<?> cls2 = Class.forName("java.nio.charset.CharsetEncoder");
                fgCharsetForNameMethod = cls.getMethod("forName", String.class);
                fgCharsetCanEncodeMethod = cls.getMethod("canEncode", new Class[0]);
                fgCharsetNewEncoderMethod = cls.getMethod("newEncoder", new Class[0]);
                fgCharsetEncoderCanEncodeMethod = cls2.getMethod("canEncode", Character.TYPE);
                fgNIOCharsetAvailable = true;
            } catch (Exception unused) {
                fgCharsetForNameMethod = null;
                fgCharsetCanEncodeMethod = null;
                fgCharsetEncoderCanEncodeMethod = null;
                fgCharsetNewEncoderMethod = null;
                fgNIOCharsetAvailable = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class CharToByteConverterMethods {
        private static Method fgCanConvertMethod = null;
        private static boolean fgConvertersAvailable = false;
        private static Method fgGetConverterMethod;

        private CharToByteConverterMethods() {
        }

        static {
            try {
                Class<?> cls = Class.forName("sun.io.CharToByteConverter");
                fgGetConverterMethod = cls.getMethod("getConverter", String.class);
                fgCanConvertMethod = cls.getMethod("canConvert", Character.TYPE);
                fgConvertersAvailable = true;
            } catch (Exception unused) {
                fgGetConverterMethod = null;
                fgCanConvertMethod = null;
                fgConvertersAvailable = false;
            }
        }
    }
}
