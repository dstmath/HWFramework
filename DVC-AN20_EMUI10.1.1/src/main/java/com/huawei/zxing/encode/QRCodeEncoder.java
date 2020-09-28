package com.huawei.zxing.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Contents;
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
        String title;
        String note;
        String data;
        String data2;
        String data3;
        String data4;
        if (bundle == null) {
            return null;
        }
        if (this.format == null) {
            this.format = BarcodeFormat.QR_CODE;
        } else {
            this.format = encodeFormat;
        }
        if (type.equals(Contents.Type.TEXT)) {
            if (bundle.containsKey(Contents.DATA) && (data4 = bundle.getString(Contents.DATA)) != null && data4.length() > 0) {
                this.contents = data4;
            }
        } else if (type.equals(Contents.Type.EMAIL)) {
            if (bundle.containsKey(Contents.DATA) && (data3 = bundle.getString(Contents.DATA)) != null) {
                this.contents = "mailto:" + data3;
            }
        } else if (type.equals(Contents.Type.PHONE)) {
            if (bundle.containsKey(Contents.DATA) && (data2 = bundle.getString(Contents.DATA)) != null) {
                this.contents = "tel:" + data2;
            }
        } else if (type.equals(Contents.Type.SMS)) {
            if (bundle.containsKey(Contents.DATA) && (data = bundle.getString(Contents.DATA)) != null) {
                this.contents = "sms:" + data;
            }
        } else if (type.equals(Contents.Type.CONTACT)) {
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
            Collection<String> phones = new ArrayList<>(Contents.PHONE_KEYS.length);
            for (int x = 0; x < Contents.PHONE_KEYS.length; x++) {
                if (bundle.containsKey(Contents.PHONE_KEYS[x])) {
                    phones.add(bundle.getString(Contents.PHONE_KEYS[x]));
                }
            }
            Collection<String> emails = new ArrayList<>(Contents.EMAIL_KEYS.length);
            for (int x2 = 0; x2 < Contents.EMAIL_KEYS.length; x2++) {
                if (bundle.containsKey(Contents.EMAIL_KEYS[x2])) {
                    emails.add(bundle.getString(Contents.EMAIL_KEYS[x2]));
                }
            }
            String url = "";
            if (bundle.containsKey(Contents.URL_KEY)) {
                url = bundle.getString(Contents.URL_KEY);
            }
            Collection<String> urls = url == null ? null : Collections.singletonList(url);
            if (bundle.containsKey("job_title")) {
                title = bundle.getString("job_title");
            } else {
                title = "";
            }
            if (bundle.containsKey(Contents.NOTE_KEY)) {
                note = bundle.getString(Contents.NOTE_KEY);
            } else {
                note = "";
            }
            String[] encoded = new MECARDContactEncoder().encode(Collections.singleton(name), organization, Collections.singleton(address), phones, emails, urls, title, note);
            if (encoded[1].length() > 0) {
                this.contents = encoded[0];
            }
        } else if (type.equals(Contents.Type.LOCATION)) {
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
        try {
            Bitmap qrcodeBitmap = encodeAsBitmap();
            try {
                if (!type.equals(Contents.Type.CONTACT) || qrcodeBitmap == null || logo == null) {
                    return qrcodeBitmap;
                }
                return overlap2Bitmap(qrcodeBitmap, logo, this.mStartX, this.mStartY, this.mDimension);
            } catch (WriterException e) {
                return null;
            }
        } catch (WriterException e2) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public Bitmap encodeAsBitmap() throws WriterException {
        QRCodeDecorator qrcodeDecorator = new QRCodeDecorator(this.mContext, this.contents);
        Bitmap bitmap = qrcodeDecorator.generateCustomizedQRCode();
        this.mStartX = qrcodeDecorator.qrcodeX;
        this.mStartY = qrcodeDecorator.qrcodeY;
        this.mDimension = qrcodeDecorator.qrDimension;
        return bitmap;
    }

    private int getDimension() {
        Context context = this.mContext;
        if (context != null) {
            Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            return ((width < height ? width : height) * 7) / 8;
        }
        throw new RuntimeException("Context can not be null");
    }

    private Bitmap overlap2Bitmap(Bitmap bmpSrc, Bitmap bmpLogo, int x, int y, int qrDimension) {
        Bitmap newBitmap;
        int w = bmpSrc.getWidth();
        int h = bmpSrc.getHeight();
        Bitmap tmpBitmap = Bitmap.createScaledBitmap(bmpLogo, qrDimension / 5, qrDimension / 5, true);
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        synchronized (canvas) {
            newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(newBitmap);
            canvas.drawBitmap(bmpSrc, 0.0f, 0.0f, paint);
            canvas.drawBitmap(tmpBitmap, (float) (((qrDimension / 5) * 2) + x), (float) (((qrDimension / 5) * 2) + y), paint);
        }
        return newBitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents2) {
        for (int i = 0; i < contents2.length(); i++) {
            if (contents2.charAt(i) > 255) {
                return "UTF-8";
            }
        }
        return null;
    }
}
