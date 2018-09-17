package com.huawei.zxing.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Contents;
import com.huawei.zxing.Contents.Type;
import com.huawei.zxing.WriterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class QRCodeEncoder {
    private static final int BLACK = -16777216;
    private static final String TAG = QRCodeEncoder.class.getSimpleName();
    private static final int WHITE = -1;
    private String contents = null;
    private BarcodeFormat format;
    private Context mContext;
    private int mDimension;
    private int mStartX;
    private int mStartY;

    public QRCodeEncoder(Context context) throws WriterException {
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
                            String name = "";
                            if (bundle.containsKey("name")) {
                                name = bundle.getString("name");
                            }
                            String organization = "";
                            if (bundle.containsKey("company")) {
                                organization = bundle.getString("company");
                            }
                            String address = "";
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
                            String url = "";
                            if (bundle.containsKey(Contents.URL_KEY)) {
                                url = bundle.getString(Contents.URL_KEY);
                            }
                            Iterable urls = url == null ? null : Collections.singletonList(url);
                            String title = "";
                            if (bundle.containsKey("job_title")) {
                                title = bundle.getString("job_title");
                            }
                            String note = "";
                            if (bundle.containsKey(Contents.NOTE_KEY)) {
                                note = bundle.getString(Contents.NOTE_KEY);
                            }
                            String[] encoded = new MECARDContactEncoder().encode(Collections.singleton(name), organization, Collections.singleton(address), phones, emails, urls, title, note);
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
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        return ((width < height ? width : height) * 7) / 8;
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
            if (contents.charAt(i) > 255) {
                return "UTF-8";
            }
        }
        return null;
    }
}
