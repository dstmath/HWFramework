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
        Bundle bundle2 = bundle;
        String str = type;
        if (bundle2 == null) {
            return null;
        }
        if (this.format == null) {
            this.format = BarcodeFormat.QR_CODE;
            BarcodeFormat barcodeFormat = encodeFormat;
        } else {
            this.format = encodeFormat;
        }
        if (str.equals(Contents.Type.TEXT)) {
            if (bundle2.containsKey(Contents.DATA)) {
                String data = bundle2.getString(Contents.DATA);
                if (data != null && data.length() > 0) {
                    this.contents = data;
                }
            }
        } else if (str.equals(Contents.Type.EMAIL)) {
            if (bundle2.containsKey(Contents.DATA)) {
                if (bundle2.getString(Contents.DATA) != null) {
                    this.contents = "mailto:" + data;
                }
            }
        } else if (str.equals(Contents.Type.PHONE)) {
            if (bundle2.containsKey(Contents.DATA)) {
                if (bundle2.getString(Contents.DATA) != null) {
                    this.contents = "tel:" + data;
                }
            }
        } else if (str.equals(Contents.Type.SMS)) {
            if (bundle2.containsKey(Contents.DATA)) {
                if (bundle2.getString(Contents.DATA) != null) {
                    this.contents = "sms:" + data;
                }
            }
        } else if (str.equals(Contents.Type.CONTACT)) {
            if (bundle2 != null) {
                String name = "";
                if (bundle2.containsKey("name")) {
                    name = bundle2.getString("name");
                }
                String organization = "";
                if (bundle2.containsKey("company")) {
                    organization = bundle2.getString("company");
                }
                String address = "";
                if (bundle2.containsKey("postal")) {
                    address = bundle2.getString("postal");
                }
                ArrayList arrayList = new ArrayList(Contents.PHONE_KEYS.length);
                for (int x = 0; x < Contents.PHONE_KEYS.length; x++) {
                    if (bundle2.containsKey(Contents.PHONE_KEYS[x])) {
                        arrayList.add(bundle2.getString(Contents.PHONE_KEYS[x]));
                    }
                }
                ArrayList arrayList2 = new ArrayList(Contents.EMAIL_KEYS.length);
                for (int x2 = 0; x2 < Contents.EMAIL_KEYS.length; x2++) {
                    if (bundle2.containsKey(Contents.EMAIL_KEYS[x2])) {
                        arrayList2.add(bundle2.getString(Contents.EMAIL_KEYS[x2]));
                    }
                }
                String url = "";
                if (bundle2.containsKey(Contents.URL_KEY)) {
                    url = bundle2.getString(Contents.URL_KEY);
                }
                Collection<String> urls = url == null ? null : Collections.singletonList(url);
                String title = "";
                if (bundle2.containsKey("job_title")) {
                    title = bundle2.getString("job_title");
                }
                String title2 = title;
                String note = "";
                if (bundle2.containsKey(Contents.NOTE_KEY)) {
                    note = bundle2.getString(Contents.NOTE_KEY);
                }
                String[] encoded = new MECARDContactEncoder().encode(Collections.singleton(name), organization, Collections.singleton(address), arrayList, arrayList2, urls, title2, note);
                if (encoded[1].length() > 0) {
                    this.contents = encoded[0];
                }
            }
        } else if (str.equals(Contents.Type.LOCATION) && bundle2 != null) {
            float latitude = 0.0f;
            if (bundle2.containsKey("LAT")) {
                latitude = bundle2.getFloat("LAT", Float.MAX_VALUE);
            }
            float longitude = 0.0f;
            if (bundle2.containsKey("LONG")) {
                longitude = bundle2.getFloat("LONG", Float.MAX_VALUE);
            }
            if (!(latitude == Float.MAX_VALUE || longitude == Float.MAX_VALUE)) {
                this.contents = "geo:" + latitude + ',' + longitude;
            }
        }
        try {
            Bitmap qrcodeBitmap = encodeAsBitmap();
            try {
                if (!(!str.equals(Contents.Type.CONTACT) || qrcodeBitmap == null || logo == null)) {
                    qrcodeBitmap = overlap2Bitmap(qrcodeBitmap, logo, this.mStartX, this.mStartY, this.mDimension);
                }
                return qrcodeBitmap;
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
        if (this.mContext != null) {
            Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
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
