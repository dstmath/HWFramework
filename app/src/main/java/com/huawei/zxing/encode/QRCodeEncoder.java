package com.huawei.zxing.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.telephony.MSimTelephonyConstants;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.android.provider.TelephonyEx.NumMatchs;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Contents;
import com.huawei.zxing.Contents.Type;
import com.huawei.zxing.WriterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class QRCodeEncoder {
    private static final int BLACK = -16777216;
    private static final String TAG = null;
    private static final int WHITE = -1;
    private String contents;
    private BarcodeFormat format;
    private Context mContext;
    private int mDimension;
    private int mStartX;
    private int mStartY;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.encode.QRCodeEncoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.encode.QRCodeEncoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.encode.QRCodeEncoder.<clinit>():void");
    }

    public QRCodeEncoder(Context context) throws WriterException {
        this.contents = null;
        this.mContext = context;
    }

    public Bitmap encodeQRCodeContents(Bundle bundle, Bitmap logo, String type, BarcodeFormat encodeFormat) {
        if (bundle == null) {
            return null;
        }
        if (this.format == null) {
            this.format = BarcodeFormat.QR_CODE;
        } else {
            this.format = encodeFormat;
        }
        String data;
        if (type.equals(Type.TEXT)) {
            if (bundle.containsKey(Contents.DATA)) {
                data = bundle.getString(Contents.DATA);
                if (data != null && data.length() > 0) {
                    this.contents = data;
                }
            }
        } else {
            if (type.equals(Type.EMAIL)) {
                if (bundle.containsKey(Contents.DATA)) {
                    data = bundle.getString(Contents.DATA);
                    if (data != null) {
                        this.contents = "mailto:" + data;
                    }
                }
            } else {
                if (type.equals(Type.PHONE)) {
                    if (bundle.containsKey(Contents.DATA)) {
                        data = bundle.getString(Contents.DATA);
                        if (data != null) {
                            this.contents = "tel:" + data;
                        }
                    }
                } else {
                    if (type.equals(Type.SMS)) {
                        if (bundle.containsKey(Contents.DATA)) {
                            data = bundle.getString(Contents.DATA);
                            if (data != null) {
                                this.contents = "sms:" + data;
                            }
                        }
                    } else {
                        if (!type.equals(Type.CONTACT)) {
                            if (type.equals(Type.LOCATION) && bundle != null) {
                                float latitude = 0.0f;
                                if (bundle.containsKey("LAT")) {
                                    latitude = bundle.getFloat("LAT", Float.MAX_VALUE);
                                }
                                float longitude = 0.0f;
                                if (bundle.containsKey("LONG")) {
                                    longitude = bundle.getFloat("LONG", Float.MAX_VALUE);
                                }
                                if (!(latitude == Float.MAX_VALUE || longitude == Float.MAX_VALUE)) {
                                    this.contents = "geo:" + latitude + ',' + longitude;
                                }
                            }
                        } else if (bundle != null) {
                            int x;
                            String name = MSimTelephonyConstants.MY_RADIO_PLATFORM;
                            if (bundle.containsKey(NumMatchs.NAME)) {
                                name = bundle.getString(NumMatchs.NAME);
                            }
                            String organization = MSimTelephonyConstants.MY_RADIO_PLATFORM;
                            if (bundle.containsKey("company")) {
                                organization = bundle.getString("company");
                            }
                            String address = MSimTelephonyConstants.MY_RADIO_PLATFORM;
                            if (bundle.containsKey("postal")) {
                                address = bundle.getString("postal");
                            }
                            Collection<String> phones = new ArrayList(Contents.PHONE_KEYS.length);
                            for (x = 0; x < Contents.PHONE_KEYS.length; x++) {
                                if (bundle.containsKey(Contents.PHONE_KEYS[x])) {
                                    phones.add(bundle.getString(Contents.PHONE_KEYS[x]));
                                }
                            }
                            Collection<String> emails = new ArrayList(Contents.EMAIL_KEYS.length);
                            for (x = 0; x < Contents.EMAIL_KEYS.length; x++) {
                                if (bundle.containsKey(Contents.EMAIL_KEYS[x])) {
                                    emails.add(bundle.getString(Contents.EMAIL_KEYS[x]));
                                }
                            }
                            String url = MSimTelephonyConstants.MY_RADIO_PLATFORM;
                            if (bundle.containsKey(Contents.URL_KEY)) {
                                url = bundle.getString(Contents.URL_KEY);
                            }
                            Iterable singletonList = url == null ? null : Collections.singletonList(url);
                            String title = MSimTelephonyConstants.MY_RADIO_PLATFORM;
                            if (bundle.containsKey("job_title")) {
                                title = bundle.getString("job_title");
                            }
                            String note = MSimTelephonyConstants.MY_RADIO_PLATFORM;
                            if (bundle.containsKey(Contents.NOTE_KEY)) {
                                note = bundle.getString(Contents.NOTE_KEY);
                            }
                            String[] encoded = new MECARDContactEncoder().encode(Collections.singleton(name), organization, Collections.singleton(address), phones, emails, singletonList, title, note);
                            if (encoded[1].length() > 0) {
                                this.contents = encoded[0];
                            }
                        }
                    }
                }
            }
        }
        try {
            Bitmap qrcodeBitmap = encodeAsBitmap();
            if (!(!type.equals(Type.CONTACT) || qrcodeBitmap == null || logo == null)) {
                qrcodeBitmap = overlap2Bitmap(qrcodeBitmap, logo, this.mStartX, this.mStartY, this.mDimension);
            }
            return qrcodeBitmap;
        } catch (WriterException e) {
            return null;
        }
    }

    Bitmap encodeAsBitmap() throws WriterException {
        QRCodeDecorator qrcodeDecorator = new QRCodeDecorator(this.mContext, this.contents);
        Bitmap bitmap = qrcodeDecorator.generateCustomizedQRCode();
        this.mStartX = qrcodeDecorator.qrcodeX;
        this.mStartY = qrcodeDecorator.qrcodeY;
        this.mDimension = qrcodeDecorator.qrDimension;
        return bitmap;
    }

    private int getDimension() {
        if (this.mContext == null) {
            throw new RuntimeException("Context can not be null");
        }
        int smallerDimension;
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if (width < height) {
            smallerDimension = width;
        } else {
            smallerDimension = height;
        }
        return (smallerDimension * 7) / 8;
    }

    private Bitmap overlap2Bitmap(Bitmap bmpSrc, Bitmap bmpLogo, int x, int y, int qrDimension) {
        Bitmap newBitmap;
        int w = bmpSrc.getWidth();
        int h = bmpSrc.getHeight();
        Bitmap tmpBitmap = Bitmap.createScaledBitmap(bmpLogo, qrDimension / 5, qrDimension / 5, true);
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        synchronized (canvas) {
            newBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            canvas.setBitmap(newBitmap);
            canvas.drawBitmap(bmpSrc, 0.0f, 0.0f, paint);
            canvas.drawBitmap(tmpBitmap, (float) (((qrDimension / 5) * 2) + x), (float) (((qrDimension / 5) * 2) + y), paint);
        }
        return newBitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > '\u00ff') {
                return "UTF-8";
            }
        }
        return null;
    }
}
