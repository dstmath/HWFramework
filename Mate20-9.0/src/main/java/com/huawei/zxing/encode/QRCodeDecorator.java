package com.huawei.zxing.encode;

import android.content.Context;
import android.graphics.Bitmap;
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

    public QRCodeDecorator(Context context, String contents2) {
        this.mContext = context;
        this.contents = contents2;
    }

    public Bitmap generateCustomizedQRCode() {
        Bitmap bitmap;
        int curPixel;
        readDecoratorFlagsFromCfgFile();
        String contentsToEncode = this.contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        int i = 1;
        hints.put(EncodeHintType.MARGIN, new Integer(1));
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        try {
            BitMatrix qrcode = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, this.qrDimension, this.qrDimension, hints);
            int firstNonEmptyBitWidth = 0;
            int firstNonEmptyBitHeight = 0;
            boolean preBit = false;
            int x = 0;
            while (true) {
                if (x >= this.qrDimension) {
                    break;
                }
                boolean curBit = qrcode.get(x, this.qrDimension / 8);
                if (!preBit && curBit) {
                    firstNonEmptyBitWidth = x;
                    break;
                }
                preBit = curBit;
                x++;
            }
            boolean preBit2 = false;
            int y = 0;
            while (true) {
                if (y >= this.qrDimension) {
                    break;
                }
                boolean curBit2 = qrcode.get(this.qrDimension / 8, y);
                if (!preBit2 && curBit2) {
                    firstNonEmptyBitHeight = y;
                    break;
                }
                preBit2 = curBit2;
                y++;
            }
            int[] qrcodePixel = new int[(this.qrDimension * this.qrDimension)];
            for (int y2 = 0; y2 < this.qrDimension; y2++) {
                int offset = this.qrDimension * y2;
                for (int x2 = 0; x2 < this.qrDimension; x2++) {
                    if (qrcode.get(x2, y2)) {
                        curPixel = this.replaceFgColor ? this.fgColor : -16777216;
                    } else {
                        curPixel = this.replaceBgColor ? this.bgColor : -1;
                    }
                    if ((y2 < firstNonEmptyBitHeight || y2 > this.qrDimension - firstNonEmptyBitHeight || x2 < firstNonEmptyBitWidth || x2 > this.qrDimension - firstNonEmptyBitWidth) && this.aroundAreaColorFlag) {
                        curPixel = this.aroundAreaColor;
                    }
                    qrcodePixel[offset + x2] = curPixel;
                }
            }
            if (this.bgImage != null) {
                int bgImageWidth = this.bgImage.getWidth();
                int bgImageHeight = this.bgImage.getHeight();
                int[] pixels = new int[(bgImageWidth * bgImageHeight)];
                int[] pixels2 = pixels;
                int bgImageHeight2 = bgImageHeight;
                this.bgImage.getPixels(pixels, 0, bgImageWidth, 0, 0, bgImageWidth, bgImageHeight2);
                int y3 = this.qrcodeY - 1;
                int qrY = 0;
                while (qrY < this.qrDimension) {
                    int bgOffset = y3 * bgImageWidth;
                    int offset2 = this.qrDimension * qrY;
                    BitMatrix qrcode2 = qrcode;
                    int x3 = this.qrcodeX - i;
                    int qrX = 0;
                    while (qrX < this.qrDimension) {
                        int qrPixels = qrcodePixel[offset2 + qrX];
                        int bgPixels = pixels2[bgOffset + x3];
                        int firstNonEmptyBitWidth2 = firstNonEmptyBitWidth;
                        if (((float) Color.alpha(qrPixels)) / 255.0f == 1.0f) {
                            pixels2[bgOffset + x3] = qrPixels;
                        } else {
                            pixels2[bgOffset + x3] = bgPixels;
                        }
                        x3++;
                        qrX++;
                        firstNonEmptyBitWidth = firstNonEmptyBitWidth2;
                    }
                    y3++;
                    qrY++;
                    qrcode = qrcode2;
                    i = 1;
                }
                int i2 = firstNonEmptyBitWidth;
                int bgImageHeight3 = bgImageHeight2;
                bitmap = Bitmap.createBitmap(bgImageWidth, bgImageHeight3, this.bgImage.getConfig());
                bitmap.setPixels(pixels2, 0, bgImageWidth, 0, 0, bgImageWidth, bgImageHeight3);
            } else {
                int i3 = firstNonEmptyBitWidth;
                bitmap = null;
            }
            if (this.bgImage == null && this.fgImage == null) {
                bitmap = Bitmap.createBitmap(this.qrDimension, this.qrDimension, Bitmap.Config.ARGB_8888);
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0025, code lost:
        if (r3.bgImage != null) goto L_0x003b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0027, code lost:
        android.util.Log.e(TAG, "load background image failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0014, code lost:
        if (r3.bgImage == null) goto L_0x0027;
     */
    private void readImage(String bgImagePath, String fgImagePath) {
        if (bgImagePath != null) {
            try {
                this.bgImage = BitmapFactory.decodeStream(new FileInputStream(new File(bgImagePath)));
            } catch (IOException e) {
                Log.e(TAG, e.toString());
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
                if (this.fgImage != null) {
                    return;
                }
            } catch (IOException e2) {
                if (this.fgImage != null) {
                    return;
                }
            } catch (Throwable th2) {
                if (this.fgImage == null) {
                    Log.e(TAG, "load foreground image failed");
                }
                throw th2;
            }
            Log.e(TAG, "load foreground image failed");
        }
    }

    private static String guessAppropriateEncoding(CharSequence contents2) {
        for (int i = 0; i < contents2.length(); i++) {
            if (contents2.charAt(i) > 255) {
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
        return Color.argb(255, (int) ((((float) r1) * alpha1) + (((float) r2) * alpha2 * (1.0f - alpha1))), (int) ((((float) g1) * alpha1) + (((float) g2) * alpha2 * (1.0f - alpha1))), (int) ((((float) b1) * alpha1) + (((float) b2) * alpha2 * (1.0f - alpha1))));
    }
}
