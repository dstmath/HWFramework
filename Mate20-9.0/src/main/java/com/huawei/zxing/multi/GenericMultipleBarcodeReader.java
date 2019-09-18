package com.huawei.zxing.multi;

import com.huawei.zxing.BinaryBitmap;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Reader;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultPoint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GenericMultipleBarcodeReader implements MultipleBarcodeReader {
    private static final int MAX_DEPTH = 4;
    private static final int MIN_DIMENSION_TO_RECUR = 100;
    private final Reader delegate;

    public GenericMultipleBarcodeReader(Reader delegate2) {
        this.delegate = delegate2;
    }

    public Result[] decodeMultiple(BinaryBitmap image) throws NotFoundException {
        return decodeMultiple(image, null);
    }

    public Result[] decodeMultiple(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        ArrayList arrayList = new ArrayList();
        doDecodeMultiple(image, hints, arrayList, 0, 0, 0);
        if (!arrayList.isEmpty()) {
            return (Result[]) arrayList.toArray(new Result[arrayList.size()]);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void doDecodeMultiple(BinaryBitmap image, Map<DecodeHintType, ?> hints, List<Result> results, int xOffset, int yOffset, int currentDepth) {
        List<Result> list;
        int height;
        float maxX;
        float maxY;
        float maxX2;
        int width;
        int width2;
        int width3;
        int height2;
        BinaryBitmap binaryBitmap = image;
        int i = xOffset;
        int width4 = yOffset;
        int i2 = currentDepth;
        if (i2 <= 4) {
            try {
                Map<DecodeHintType, ?> map = hints;
                try {
                    Result result = this.delegate.decode(binaryBitmap, map);
                    boolean alreadyFound = false;
                    Iterator<Result> it = results.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            if (it.next().getText().equals(result.getText())) {
                                alreadyFound = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    boolean alreadyFound2 = alreadyFound;
                    if (!alreadyFound2) {
                        list = results;
                        list.add(translateResultPoints(result, i, width4));
                    } else {
                        list = results;
                    }
                    ResultPoint[] resultPoints = result.getResultPoints();
                    if (resultPoints == null) {
                        ResultPoint[] resultPointArr = resultPoints;
                        boolean z = alreadyFound2;
                    } else if (resultPoints.length == 0) {
                        Result result2 = result;
                        ResultPoint[] resultPointArr2 = resultPoints;
                        boolean z2 = alreadyFound2;
                    } else {
                        int width5 = image.getWidth();
                        int height3 = image.getHeight();
                        Result result3 = result;
                        int length = resultPoints.length;
                        boolean z3 = alreadyFound2;
                        float maxY2 = 0.0f;
                        float minX = (float) width5;
                        int i3 = 0;
                        float minY = (float) height3;
                        float maxX3 = 0.0f;
                        while (i3 < length) {
                            int i4 = length;
                            ResultPoint point = resultPoints[i3];
                            float x = point.getX();
                            float y = point.getY();
                            if (x < minX) {
                                minX = x;
                            }
                            if (y < minY) {
                                minY = y;
                            }
                            if (x > maxX3) {
                                maxX3 = x;
                            }
                            if (y > maxY2) {
                                maxY2 = y;
                            }
                            i3++;
                            length = i4;
                        }
                        if (minX > 100.0f) {
                            BinaryBitmap crop = binaryBitmap.crop(0, 0, (int) minX, height3);
                            maxY = maxY2;
                            maxX = maxX3;
                            maxX2 = minY;
                            float f = minX;
                            height = height3;
                            int height4 = i;
                            width = width5;
                            ResultPoint[] resultPointArr3 = resultPoints;
                            doDecodeMultiple(crop, map, list, height4, width4, i2 + 1);
                        } else {
                            maxX = maxX3;
                            float f2 = minX;
                            height = height3;
                            width = width5;
                            ResultPoint[] resultPointArr4 = resultPoints;
                            maxY = maxY2;
                            maxX2 = minY;
                        }
                        if (maxX2 > 100.0f) {
                            width3 = width;
                            width2 = xOffset;
                            doDecodeMultiple(binaryBitmap.crop(0, 0, width, (int) maxX2), map, list, width2, width4, i2 + 1);
                        } else {
                            width3 = width;
                            width2 = xOffset;
                        }
                        if (maxX < ((float) (width3 - 100))) {
                            float maxX4 = maxX;
                            int height5 = height;
                            height2 = height5;
                            float f3 = maxX4;
                            doDecodeMultiple(binaryBitmap.crop((int) maxX4, 0, width3 - ((int) maxX4), height5), map, list, width2 + ((int) maxX4), width4, i2 + 1);
                        } else {
                            height2 = height;
                        }
                        if (maxY < ((float) (height2 - 100))) {
                            float maxY3 = maxY;
                            float f4 = maxY3;
                            doDecodeMultiple(binaryBitmap.crop(0, (int) maxY3, width3, height2 - ((int) maxY3)), map, list, width2, width4 + ((int) maxY3), i2 + 1);
                        }
                    }
                } catch (ReaderException e) {
                    List<Result> list2 = results;
                }
            } catch (ReaderException e2) {
                Map<DecodeHintType, ?> map2 = hints;
                List<Result> list22 = results;
            }
        }
    }

    private static Result translateResultPoints(Result result, int xOffset, int yOffset) {
        ResultPoint[] oldResultPoints = result.getResultPoints();
        if (oldResultPoints == null) {
            return result;
        }
        ResultPoint[] newResultPoints = new ResultPoint[oldResultPoints.length];
        for (int i = 0; i < oldResultPoints.length; i++) {
            ResultPoint oldPoint = oldResultPoints[i];
            newResultPoints[i] = new ResultPoint(oldPoint.getX() + ((float) xOffset), oldPoint.getY() + ((float) yOffset));
        }
        Result newResult = new Result(result.getText(), result.getRawBytes(), newResultPoints, result.getBarcodeFormat());
        newResult.putAllMetadata(result.getResultMetadata());
        return newResult;
    }
}
