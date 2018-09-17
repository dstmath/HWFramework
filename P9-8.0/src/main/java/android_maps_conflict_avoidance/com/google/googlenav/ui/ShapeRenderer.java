package android_maps_conflict_avoidance.com.google.googlenav.ui;

import android_maps_conflict_avoidance.com.google.map.MapPoint;
import android_maps_conflict_avoidance.com.google.map.Zoom;

public class ShapeRenderer {
    private Zoom pixelZoom;
    private long[][][] polyBoundaryPixelXY;
    private final RenderableShape[] shapes;

    public interface Painter {
        void addLineSegment(int[] iArr, int[] iArr2, boolean z);

        void endLine();

        void paintEllipse(int i, int i2, int i3, int i4, int i5, int i6, int i7);

        void paintPolygon(long[][] jArr, int i, int i2, int i3);

        void startLine(int i, int i2, int i3);
    }

    public int getImageVersion() {
        int id = 0;
        for (int p = 0; p < this.shapes.length; p++) {
            if (!this.shapes[p].isAvailable()) {
                return 0;
            }
            id = (id * 29) + this.shapes[p].getId();
        }
        return id;
    }

    private void precalculatePixels(Zoom zoom) {
        if (zoom != this.pixelZoom) {
            this.polyBoundaryPixelXY = new long[this.shapes.length][][];
            for (int p = 0; p < this.shapes.length; p++) {
                if (this.shapes[p] instanceof RenderablePoly) {
                    RenderablePoly poly = this.shapes[p];
                    int boundaryCount = getBoundaryCount(poly);
                    this.polyBoundaryPixelXY[p] = new long[boundaryCount][];
                    MapPoint[][] boundaries = getBoundaries(poly);
                    for (int b = 0; b < boundaryCount; b++) {
                        MapPoint[] boundary = boundaries[b];
                        long[] pixelXY = new long[boundary.length];
                        int point = 1;
                        this.pixelZoom = zoom;
                        pixelXY[0] = getXY(boundary[0].getXPixel(this.pixelZoom), boundary[0].getYPixel(this.pixelZoom));
                        int i = 1;
                        while (i < boundary.length) {
                            int x = boundary[i].getXPixel(this.pixelZoom);
                            int y = boundary[i].getYPixel(this.pixelZoom);
                            if (Math.abs(x - getX(pixelXY[point - 1])) > 2 || Math.abs(y - getY(pixelXY[point - 1])) > 2 || i == boundary.length - 1) {
                                pixelXY[point] = getXY(x, y);
                                point++;
                            }
                            i++;
                        }
                        this.polyBoundaryPixelXY[p][b] = new long[point];
                        System.arraycopy(pixelXY, 0, this.polyBoundaryPixelXY[p][b], 0, point);
                    }
                }
            }
        }
    }

    private static int outcode(int width, int height, int x, int y) {
        int i = 0;
        int i2 = x >= 0 ? x <= width ? 0 : 4 : 8;
        if (y < 0) {
            i = 2;
        } else if (y > height) {
            i = 1;
        }
        return i | i2;
    }

    public void render(Painter painter, int x, int y, int width, int height, Zoom zoom) {
        if (getImageVersion() != 0) {
            precalculatePixels(zoom);
            for (int p = 0; p < this.shapes.length; p++) {
                if (this.shapes[p] instanceof RenderableEllipse) {
                    renderEllipse(painter, x, y, width, height, (RenderableEllipse) this.shapes[p], zoom);
                } else {
                    RenderablePoly renderablePoly = this.shapes[p];
                    if (this.shapes[p].isFilled()) {
                        renderPolygonFill(painter, x, y, width, height, this.polyBoundaryPixelXY[p], renderablePoly, zoom);
                    } else {
                        for (long[] renderLine : this.polyBoundaryPixelXY[p]) {
                            renderLine(painter, x, y, width, height, renderLine, renderablePoly, zoom);
                        }
                    }
                }
            }
        }
    }

    private void renderEllipse(Painter painter, int x, int y, int screenWidth, int screenHeight, RenderableEllipse ellipse, Zoom zoom) {
        if (ellipse.getLineColor() != -1 || ellipse.getFillColor() != -1) {
            MapPoint center = ellipse.getCenter();
            int ellipseWidth = zoom.getPixelsForDistance(ellipse.getEllipseWidth());
            int ellipseHeight = zoom.getPixelsForDistance(ellipse.getEllipseHeight());
            int centerX = center.getXPixel(zoom) - x;
            int centerY = center.getYPixel(zoom) - y;
            if ((outcode(screenWidth, screenHeight, centerX - (ellipseWidth / 2), centerY - (ellipseHeight / 2)) & outcode(screenWidth, screenHeight, (ellipseWidth / 2) + centerX, (ellipseHeight / 2) + centerY)) == 0) {
                painter.paintEllipse(centerX, centerY, ellipseWidth, ellipseHeight, ellipse.getLineWidthForZoom(zoom), ellipse.getLineColor(), ellipse.getFillColor());
            }
        }
    }

    static void makeInRange(int endX, int endY, int startX, int startY, int[] outPoint) {
        int width = endX - startX;
        int height = endY - startY;
        if (endX > 4000 || endX < -4000) {
            if (endX <= 0) {
                endX = -4000;
            } else {
                endX = 4000;
            }
            endY = startY + ((int) ((((long) (endX - startX)) * ((long) height)) / ((long) width)));
        }
        if (endY > 4000 || endY < -4000) {
            if (height + startY <= 0) {
                endY = -4000;
            } else {
                endY = 4000;
            }
            endX = startX + ((int) ((((long) (endY - startY)) * ((long) width)) / ((long) height)));
        }
        outPoint[0] = endX;
        outPoint[1] = endY;
    }

    protected static boolean isInRange(int[] pointXy) {
        return pointXy[0] <= 4000 && pointXy[0] >= -4000 && pointXy[1] <= 4000 && pointXy[1] >= -4000;
    }

    private void renderLine(Painter painter, int x, int y, int width, int height, long[] pixelXY, RenderablePoly poly, Zoom zoom) {
        int[] xyDiff = new int[2];
        xyDiffLast = new int[2];
        int[] rangeAdjustedXy = new int[]{getX(pixelXY[0]) - x, getY(pixelXY[0]) - y};
        int lastOutcode = outcode(width, height, xyDiffLast[0], xyDiffLast[1]);
        boolean skipTo = true;
        boolean lineStarted = false;
        for (int i = 1; i < pixelXY.length; i++) {
            int x2 = getX(pixelXY[i]) - x;
            xyDiff[0] = x2;
            int y2 = getY(pixelXY[i]) - y;
            xyDiff[1] = y2;
            int outcode = outcode(width, height, x2, y2);
            if ((lastOutcode & outcode) != 0) {
                skipTo = true;
            } else {
                if (!lineStarted) {
                    painter.startLine(poly.getLineColor(), poly.getLineWidthForZoom(zoom), poly.getLineStyle());
                    lineStarted = true;
                }
                boolean inRange = isInRange(xyDiff);
                if (!inRange) {
                    makeInRange(xyDiff[0], xyDiff[1], xyDiffLast[0], xyDiffLast[1], rangeAdjustedXy);
                }
                if (!isInRange(xyDiffLast)) {
                    makeInRange(xyDiffLast[0], xyDiffLast[1], xyDiff[0], xyDiff[1], xyDiffLast);
                }
                if (inRange) {
                    painter.addLineSegment(xyDiff, xyDiffLast, skipTo);
                } else {
                    painter.addLineSegment(rangeAdjustedXy, xyDiffLast, skipTo);
                }
                if (inRange) {
                    skipTo = false;
                } else {
                    skipTo = true;
                }
            }
            xyDiffLast[0] = xyDiff[0];
            xyDiffLast[1] = xyDiff[1];
            lastOutcode = outcode;
        }
        if (lineStarted) {
            painter.endLine();
        }
    }

    private void renderPolygonFill(Painter painter, int x, int y, int width, int height, long[][] boundaryPixelXY, RenderablePoly poly, Zoom zoom) {
        int i;
        boolean overlap = false;
        int boundaries = getBoundaryCount(poly);
        long[][] boundaryPixelXYOnScreen = new long[boundaries][];
        boundaryPixelXYOnScreen[0] = getPixelXYOnScreen(x, y, boundaryPixelXY[0]);
        int lastOutcode = outcode(width, height, getX(boundaryPixelXYOnScreen[0][0]), getY(boundaryPixelXYOnScreen[0][0]));
        int cumulativeOutcode = lastOutcode;
        for (i = 1; i < boundaryPixelXYOnScreen[0].length; i++) {
            int outcode = outcode(width, height, getX(boundaryPixelXYOnScreen[0][i]), getY(boundaryPixelXYOnScreen[0][i]));
            if ((lastOutcode & outcode) == 0) {
                overlap = true;
                break;
            }
            cumulativeOutcode |= outcode;
            lastOutcode = outcode;
        }
        if (cumulativeOutcode == 15) {
            overlap = true;
        }
        if (overlap) {
            for (i = 1; i < boundaries; i++) {
                boundaryPixelXYOnScreen[i] = getPixelXYOnScreen(x, y, boundaryPixelXY[i]);
            }
            painter.paintPolygon(boundaryPixelXYOnScreen, poly.getLineColor(), poly.getLineWidthForZoom(zoom), poly.getFillColor());
        }
    }

    private static long[] getPixelXYOnScreen(int x, int y, long[] pixelXY) {
        long[] xy = new long[pixelXY.length];
        for (int i = 0; i < pixelXY.length; i++) {
            xy[i] = getXY(getX(pixelXY[i]) - x, getY(pixelXY[i]) - y);
        }
        return xy;
    }

    private static int getBoundaryCount(RenderablePoly poly) {
        if (poly.getInnerBoundaries() != null) {
            return poly.getInnerBoundaries().length + 1;
        }
        return 1;
    }

    private static MapPoint[][] getBoundaries(RenderablePoly poly) {
        if (poly.getInnerBoundaries() != null) {
            MapPoint[][] boundaries = new MapPoint[getBoundaryCount(poly)][];
            boundaries[0] = poly.getLine();
            for (int i = 1; i < boundaries.length; i++) {
                boundaries[i] = poly.getInnerBoundaries()[i - 1];
            }
            return boundaries;
        }
        return new MapPoint[][]{poly.getLine()};
    }

    public static long getXY(int x, int y) {
        return (((long) x) << 32) | ((((long) y) << 32) >>> 32);
    }

    public static int getX(long xy) {
        return (int) (xy >> 32);
    }

    public static int getY(long xy) {
        return (int) (4294967295L & xy);
    }
}
