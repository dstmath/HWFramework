package com.huawei.zxing.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.MultiFormatWriter;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitMatrix;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class QRCodeDecorator {
    private static final boolean ATTACH_BG_FG_IMAGE = false;
    private static final int BLACK = -16777216;
    private static final String CFG_PATH = "data/cust/template/";
    private static final int DEFAULT_QRCODE_DIMENSION = 600;
    private static final String TAG = QRCodeDecorator.class.getSimpleName();
    private static final int WHITE = -1;
    private static int cur_template = 0;
    private static Random ran = new Random();
    private int aroundAreaColor;
    private boolean aroundAreaColorFlag;
    private int bgColor;
    private Bitmap bgImage;
    private String contents;
    private int fgColor;
    private Bitmap fgImage;
    private final Context mContext;
    public int qrDimension;
    public int qrcodeX;
    public int qrcodeY;
    private boolean replaceBgColor;
    private boolean replaceFgColor;

    public QRCodeDecorator(Context context, String contents) {
        this.mContext = context;
        this.contents = contents;
    }

    public Bitmap generateCustomizedQRCode() {
        readDecoratorFlagsFromCfgFile();
        String contentsToEncode = this.contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new EnumMap(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, new Integer(1));
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        try {
            int x;
            boolean curBit;
            int y;
            int offset;
            BitMatrix qrcode = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, this.qrDimension, this.qrDimension, hints);
            int firstNonEmptyBitWidth = 0;
            int firstNonEmptyBitHeight = 0;
            boolean preBit = false;
            for (x = 0; x < this.qrDimension; x++) {
                curBit = qrcode.get(x, this.qrDimension / 8);
                if (!preBit && curBit) {
                    firstNonEmptyBitWidth = x;
                    break;
                }
                preBit = curBit;
            }
            preBit = false;
            for (y = 0; y < this.qrDimension; y++) {
                curBit = qrcode.get(this.qrDimension / 8, y);
                if (!preBit && curBit) {
                    firstNonEmptyBitHeight = y;
                    break;
                }
                preBit = curBit;
            }
            int[] qrcodePixel = new int[(this.qrDimension * this.qrDimension)];
            y = 0;
            while (y < this.qrDimension) {
                offset = y * this.qrDimension;
                x = 0;
                while (x < this.qrDimension) {
                    int curPixel = qrcode.get(x, y) ? this.replaceFgColor ? this.fgColor : -16777216 : this.replaceBgColor ? this.bgColor : -1;
                    if ((y < firstNonEmptyBitHeight || y > this.qrDimension - firstNonEmptyBitHeight || x < firstNonEmptyBitWidth || x > this.qrDimension - firstNonEmptyBitWidth) && this.aroundAreaColorFlag) {
                        curPixel = this.aroundAreaColor;
                    }
                    qrcodePixel[offset + x] = curPixel;
                    x++;
                }
                y++;
            }
            Bitmap bitmap = null;
            if (this.bgImage != null) {
                int bgImageWidth = this.bgImage.getWidth();
                int bgImageHeight = this.bgImage.getHeight();
                int[] pixels = new int[(bgImageWidth * bgImageHeight)];
                this.bgImage.getPixels(pixels, 0, bgImageWidth, 0, 0, bgImageWidth, bgImageHeight);
                y = this.qrcodeY - 1;
                for (int qrY = 0; qrY < this.qrDimension; qrY++) {
                    int bgOffset = y * bgImageWidth;
                    offset = qrY * this.qrDimension;
                    x = this.qrcodeX - 1;
                    for (int qrX = 0; qrX < this.qrDimension; qrX++) {
                        int qrPixels = qrcodePixel[offset + qrX];
                        int bgPixels = pixels[bgOffset + x];
                        if (((float) Color.alpha(qrPixels)) / 255.0f == 1.0f) {
                            pixels[bgOffset + x] = qrPixels;
                        } else {
                            pixels[bgOffset + x] = bgPixels;
                        }
                        x++;
                    }
                    y++;
                }
                bitmap = Bitmap.createBitmap(bgImageWidth, bgImageHeight, this.bgImage.getConfig());
                bitmap.setPixels(pixels, 0, bgImageWidth, 0, 0, bgImageWidth, bgImageHeight);
            }
            if (this.bgImage == null && this.fgImage == null) {
                bitmap = Bitmap.createBitmap(this.qrDimension, this.qrDimension, Config.ARGB_8888);
                bitmap.setPixels(qrcodePixel, 0, this.qrDimension, 0, 0, this.qrDimension, this.qrDimension);
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (WriterException e2) {
            return null;
        }
    }

    private void reset() {
        this.replaceBgColor = false;
        this.replaceFgColor = false;
        this.aroundAreaColorFlag = false;
        this.bgColor = 0;
        this.fgColor = 0;
        this.aroundAreaColor = 0;
        this.bgImage = null;
        this.fgImage = null;
        this.qrDimension = 600;
        this.qrcodeX = 0;
        this.qrcodeY = 0;
    }

    private void readDecoratorFlagsFromCfgFile() {
        reset();
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readImage(String bgImagePath, String fgImagePath) {
        if (bgImagePath != null) {
            try {
                this.bgImage = BitmapFactory.decodeStream(new FileInputStream(new File(bgImagePath)));
                if (this.bgImage == null) {
                    Log.e(TAG, "load background image failed");
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                if (this.bgImage == null) {
                    Log.e(TAG, "load background image failed");
                }
            } catch (Throwable th) {
                if (this.bgImage == null) {
                    Log.e(TAG, "load background image failed");
                }
                throw th;
            }
        }
        if (fgImagePath != null) {
            try {
                this.fgImage = BitmapFactory.decodeStream(new FileInputStream(new File(fgImagePath)));
                if (this.fgImage == null) {
                    Log.e(TAG, "load foreground image failed");
                }
            } catch (IOException e2) {
                if (this.fgImage == null) {
                    Log.e(TAG, "load foreground image failed");
                }
            } catch (Throwable th2) {
                if (this.fgImage == null) {
                    Log.e(TAG, "load foreground image failed");
                }
                throw th2;
            }
        }
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 255) {
                return "UTF-8";
            }
        }
        return null;
    }

    private int combiningWithAlpha(int pixel1, int pixel2) {
        float alpha1 = ((float) Color.alpha(pixel1)) / 255.0f;
        int r1 = Color.red(pixel1);
        int g1 = Color.green(pixel1);
        int b1 = Color.blue(pixel1);
        float alpha2 = ((float) Color.alpha(pixel2)) / 255.0f;
        int r2 = Color.red(pixel2);
        int g2 = Color.green(pixel2);
        int b2 = Color.blue(pixel2);
        if (alpha1 == 1.0f && alpha2 == 1.0f) {
            return Color.argb(255, Math.min(r1 + r2, 255), Math.min(g1 + g2, 255), Math.min(b1 + b2, 255));
        }
        return Color.argb(255, (int) ((((float) r1) * alpha1) + ((((float) r2) * alpha2) * (1.0f - alpha1))), (int) ((((float) g1) * alpha1) + ((((float) g2) * alpha2) * (1.0f - alpha1))), (int) ((((float) b1) * alpha1) + ((((float) b2) * alpha2) * (1.0f - alpha1))));
    }
}
