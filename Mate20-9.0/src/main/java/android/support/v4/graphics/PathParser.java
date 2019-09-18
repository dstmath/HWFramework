package android.support.v4.graphics;

import android.graphics.Path;
import android.support.annotation.RestrictTo;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import com.huawei.nearbysdk.closeRange.CloseRangeConstant;
import java.util.ArrayList;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class PathParser {
    private static final String LOGTAG = "PathParser";

    private static class ExtractFloatResult {
        int mEndPosition;
        boolean mEndWithNegOrDot;

        ExtractFloatResult() {
        }
    }

    public static class PathDataNode {
        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public float[] mParams;
        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public char mType;

        PathDataNode(char type, float[] params) {
            this.mType = type;
            this.mParams = params;
        }

        PathDataNode(PathDataNode n) {
            this.mType = n.mType;
            this.mParams = PathParser.copyOfRange(n.mParams, 0, n.mParams.length);
        }

        public static void nodesToPath(PathDataNode[] node, Path path) {
            float[] current = new float[6];
            char previousCommand = 'm';
            for (int i = 0; i < node.length; i++) {
                addCommand(path, current, previousCommand, node[i].mType, node[i].mParams);
                previousCommand = node[i].mType;
            }
        }

        public void interpolatePathDataNode(PathDataNode nodeFrom, PathDataNode nodeTo, float fraction) {
            for (int i = 0; i < nodeFrom.mParams.length; i++) {
                this.mParams[i] = (nodeFrom.mParams[i] * (1.0f - fraction)) + (nodeTo.mParams[i] * fraction);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x006d, code lost:
            r27 = r6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x019c, code lost:
            r21 = r0;
            r22 = r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x01a0, code lost:
            r14 = r7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x033f, code lost:
            r21 = r0;
            r22 = r1;
         */
        private static void addCommand(Path path, float[] current, char previousCmd, char cmd, float[] val) {
            int k;
            int k2;
            float currentX;
            float currentY;
            float ctrlPointX;
            float ctrlPointY;
            float reflectiveCtrlPointY;
            float reflectiveCtrlPointX;
            float ctrlPointY2;
            float ctrlPointX2;
            float currentY2;
            float reflectiveCtrlPointY2;
            float reflectiveCtrlPointX2;
            Path path2 = path;
            float[] fArr = val;
            int incr = 2;
            boolean z = false;
            float currentX2 = current[0];
            float currentY3 = current[1];
            float ctrlPointX3 = current[2];
            float ctrlPointY3 = current[3];
            float currentSegmentStartX = current[4];
            float currentSegmentStartY = current[5];
            switch (cmd) {
                case 'A':
                case 'a':
                    incr = 7;
                    break;
                case 'C':
                case 'c':
                    incr = 6;
                    break;
                case 'H':
                case 'V':
                case CloseRangeConstant.CALLBACK_ONDEVICE /*104*/:
                case 'v':
                    incr = 1;
                    break;
                case 'L':
                case 'M':
                case 'T':
                case 'l':
                case 'm':
                case 't':
                    incr = 2;
                    break;
                case 'Q':
                case 'S':
                case 'q':
                case 's':
                    incr = 4;
                    break;
                case 'Z':
                case 'z':
                    path.close();
                    currentX2 = currentSegmentStartX;
                    currentY3 = currentSegmentStartY;
                    ctrlPointX3 = currentSegmentStartX;
                    ctrlPointY3 = currentSegmentStartY;
                    path2.moveTo(currentX2, currentY3);
                    break;
            }
            int incr2 = incr;
            char previousCmd2 = previousCmd;
            float currentX3 = currentX2;
            float currentY4 = currentY3;
            float ctrlPointX4 = ctrlPointX3;
            float ctrlPointY4 = ctrlPointY3;
            float currentSegmentStartX2 = currentSegmentStartX;
            float currentSegmentStartY2 = currentSegmentStartY;
            int k3 = 0;
            while (true) {
                int k4 = k3;
                if (k4 < fArr.length) {
                    switch (cmd) {
                        case 'A':
                            k2 = k4;
                            char c = previousCmd2;
                            drawArc(path2, currentX3, currentY4, fArr[k2 + 5], fArr[k2 + 6], fArr[k2 + 0], fArr[k2 + 1], fArr[k2 + 2], fArr[k2 + 3] != 0.0f, fArr[k2 + 4] != 0.0f);
                            currentX = fArr[k2 + 5];
                            currentY = fArr[k2 + 6];
                            ctrlPointX = currentX;
                            ctrlPointY = currentY;
                            break;
                        case 'C':
                            k2 = k4;
                            char c2 = previousCmd2;
                            float f = currentY4;
                            float f2 = currentX3;
                            path2.cubicTo(fArr[k2 + 0], fArr[k2 + 1], fArr[k2 + 2], fArr[k2 + 3], fArr[k2 + 4], fArr[k2 + 5]);
                            currentX = fArr[k2 + 4];
                            currentY = fArr[k2 + 5];
                            ctrlPointX = fArr[k2 + 2];
                            ctrlPointY = fArr[k2 + 3];
                            break;
                        case 'H':
                            k = k4;
                            char c3 = previousCmd2;
                            float f3 = currentX3;
                            path2.lineTo(fArr[k + 0], currentY4);
                            currentX3 = fArr[k + 0];
                            break;
                        case 'L':
                            k = k4;
                            char c4 = previousCmd2;
                            float f4 = currentY4;
                            float f5 = currentX3;
                            path2.lineTo(fArr[k + 0], fArr[k + 1]);
                            currentX3 = fArr[k + 0];
                            currentY4 = fArr[k + 1];
                            break;
                        case 'M':
                            k = k4;
                            char c5 = previousCmd2;
                            float f6 = currentY4;
                            float f7 = currentX3;
                            currentX3 = fArr[k + 0];
                            currentY4 = fArr[k + 1];
                            if (k <= 0) {
                                path2.moveTo(fArr[k + 0], fArr[k + 1]);
                                currentSegmentStartX2 = currentX3;
                                currentSegmentStartY2 = currentY4;
                                break;
                            } else {
                                path2.lineTo(fArr[k + 0], fArr[k + 1]);
                                break;
                            }
                        case 'Q':
                            k2 = k4;
                            char c6 = previousCmd2;
                            float f8 = currentY4;
                            float f9 = currentX3;
                            path2.quadTo(fArr[k2 + 0], fArr[k2 + 1], fArr[k2 + 2], fArr[k2 + 3]);
                            ctrlPointX = fArr[k2 + 0];
                            ctrlPointY = fArr[k2 + 1];
                            currentX = fArr[k2 + 2];
                            currentY = fArr[k2 + 3];
                            break;
                        case 'S':
                            k = k4;
                            char previousCmd3 = previousCmd2;
                            float currentY5 = currentY4;
                            float currentX4 = currentX3;
                            float reflectiveCtrlPointX3 = currentX4;
                            float reflectiveCtrlPointY3 = currentY5;
                            if (previousCmd3 == 'c' || previousCmd3 == 's' || previousCmd3 == 'C' || previousCmd3 == 'S') {
                                reflectiveCtrlPointX = (2.0f * currentX4) - ctrlPointX4;
                                reflectiveCtrlPointY = (2.0f * currentY5) - ctrlPointY4;
                            } else {
                                reflectiveCtrlPointX = reflectiveCtrlPointX3;
                                reflectiveCtrlPointY = reflectiveCtrlPointY3;
                            }
                            path2.cubicTo(reflectiveCtrlPointX, reflectiveCtrlPointY, fArr[k + 0], fArr[k + 1], fArr[k + 2], fArr[k + 3]);
                            float ctrlPointX5 = fArr[k + 0];
                            float ctrlPointY5 = fArr[k + 1];
                            float currentX5 = fArr[k + 2];
                            currentY4 = fArr[k + 3];
                            ctrlPointX4 = ctrlPointX5;
                            ctrlPointY4 = ctrlPointY5;
                            currentX3 = currentX5;
                            break;
                        case 'T':
                            k = k4;
                            char previousCmd4 = previousCmd2;
                            float currentY6 = currentY4;
                            float currentX6 = currentX3;
                            float reflectiveCtrlPointX4 = currentX6;
                            float reflectiveCtrlPointY4 = currentY6;
                            if (previousCmd4 == 'q' || previousCmd4 == 't' || previousCmd4 == 'Q' || previousCmd4 == 'T') {
                                reflectiveCtrlPointX4 = (2.0f * currentX6) - ctrlPointX4;
                                reflectiveCtrlPointY4 = (2.0f * currentY6) - ctrlPointY4;
                            }
                            path2.quadTo(reflectiveCtrlPointX4, reflectiveCtrlPointY4, fArr[k + 0], fArr[k + 1]);
                            currentX3 = fArr[k + 0];
                            currentY4 = fArr[k + 1];
                            ctrlPointX4 = reflectiveCtrlPointX4;
                            ctrlPointY4 = reflectiveCtrlPointY4;
                            break;
                        case 'V':
                            k = k4;
                            char c7 = previousCmd2;
                            float f10 = currentY4;
                            path2.lineTo(currentX3, fArr[k + 0]);
                            currentY4 = fArr[k + 0];
                            break;
                        case 'a':
                            k2 = k4;
                            float f11 = fArr[k2 + 5] + currentX3;
                            float f12 = fArr[k2 + 6] + currentY4;
                            float f13 = fArr[k2 + 0];
                            float f14 = fArr[k2 + 1];
                            float f15 = fArr[k2 + 2];
                            boolean z2 = fArr[k2 + 3] != 0.0f ? true : z;
                            boolean z3 = fArr[k2 + 4] != 0.0f ? true : z;
                            char c8 = previousCmd2;
                            drawArc(path2, currentX3, currentY4, f11, f12, f13, f14, f15, z2, z3);
                            currentX = currentX3 + fArr[k2 + 5];
                            currentY = currentY4 + fArr[k2 + 6];
                            ctrlPointX = currentX;
                            ctrlPointY = currentY;
                            break;
                        case 'c':
                            k = k4;
                            path2.rCubicTo(fArr[k + 0], fArr[k + 1], fArr[k + 2], fArr[k + 3], fArr[k + 4], fArr[k + 5]);
                            ctrlPointX2 = fArr[k + 2] + currentX3;
                            ctrlPointY2 = fArr[k + 3] + currentY4;
                            currentX3 += fArr[k + 4];
                            currentY2 = currentY4 + fArr[k + 5];
                            break;
                        case CloseRangeConstant.CALLBACK_ONDEVICE /*104*/:
                            k = k4;
                            path2.rLineTo(fArr[k + 0], 0.0f);
                            currentX3 += fArr[k + 0];
                            break;
                        case 'l':
                            k = k4;
                            path2.rLineTo(fArr[k + 0], fArr[k + 1]);
                            currentX3 += fArr[k + 0];
                            currentY4 += fArr[k + 1];
                            break;
                        case 'm':
                            k = k4;
                            currentX3 += fArr[k + 0];
                            currentY4 += fArr[k + 1];
                            if (k <= 0) {
                                path2.rMoveTo(fArr[k + 0], fArr[k + 1]);
                                currentSegmentStartX2 = currentX3;
                                currentSegmentStartY2 = currentY4;
                                break;
                            } else {
                                path2.rLineTo(fArr[k + 0], fArr[k + 1]);
                                break;
                            }
                        case 'q':
                            k = k4;
                            path2.rQuadTo(fArr[k + 0], fArr[k + 1], fArr[k + 2], fArr[k + 3]);
                            ctrlPointX2 = fArr[k + 0] + currentX3;
                            ctrlPointY2 = fArr[k + 1] + currentY4;
                            currentX3 += fArr[k + 2];
                            currentY2 = currentY4 + fArr[k + 3];
                            break;
                        case 's':
                            if (previousCmd2 == 'c' || previousCmd2 == 's' || previousCmd2 == 'C' || previousCmd2 == 'S') {
                                reflectiveCtrlPointX2 = currentX3 - ctrlPointX4;
                                reflectiveCtrlPointY2 = currentY4 - ctrlPointY4;
                            } else {
                                reflectiveCtrlPointX2 = 0.0f;
                                reflectiveCtrlPointY2 = 0.0f;
                            }
                            k = k4;
                            path2.rCubicTo(reflectiveCtrlPointX2, reflectiveCtrlPointY2, fArr[k4 + 0], fArr[k4 + 1], fArr[k4 + 2], fArr[k4 + 3]);
                            ctrlPointX2 = fArr[k + 0] + currentX3;
                            ctrlPointY2 = fArr[k + 1] + currentY4;
                            currentX3 += fArr[k + 2];
                            currentY2 = currentY4 + fArr[k + 3];
                            break;
                        case 't':
                            float reflectiveCtrlPointX5 = 0.0f;
                            float reflectiveCtrlPointY5 = 0.0f;
                            if (previousCmd2 == 'q' || previousCmd2 == 't' || previousCmd2 == 'Q' || previousCmd2 == 'T') {
                                reflectiveCtrlPointX5 = currentX3 - ctrlPointX4;
                                reflectiveCtrlPointY5 = currentY4 - ctrlPointY4;
                            }
                            path2.rQuadTo(reflectiveCtrlPointX5, reflectiveCtrlPointY5, fArr[k4 + 0], fArr[k4 + 1]);
                            float ctrlPointX6 = currentX3 + reflectiveCtrlPointX5;
                            float ctrlPointY6 = currentY4 + reflectiveCtrlPointY5;
                            currentX3 += fArr[k4 + 0];
                            currentY4 += fArr[k4 + 1];
                            ctrlPointX4 = ctrlPointX6;
                            ctrlPointY4 = ctrlPointY6;
                            break;
                        case 'v':
                            path2.rLineTo(0.0f, fArr[k4 + 0]);
                            currentY4 += fArr[k4 + 0];
                            break;
                        default:
                            k = k4;
                            char c9 = previousCmd2;
                            float f16 = currentY4;
                            float f17 = currentX3;
                            break;
                    }
                } else {
                    char c10 = previousCmd2;
                    current[0] = currentX3;
                    current[1] = currentY4;
                    current[2] = ctrlPointX4;
                    current[3] = ctrlPointY4;
                    current[4] = currentSegmentStartX2;
                    current[5] = currentSegmentStartY2;
                    return;
                }
                previousCmd2 = cmd;
                k3 = k + incr2;
                z = false;
            }
        }

        private static void drawArc(Path p, float x0, float y0, float x1, float y1, float a, float b, float theta, boolean isMoreThanHalf, boolean isPositiveArc) {
            double cy;
            double cx;
            float f = x0;
            float f2 = y0;
            float adjust = x1;
            float f3 = y1;
            float f4 = a;
            float f5 = b;
            boolean z = isPositiveArc;
            float f6 = theta;
            double thetaD = Math.toRadians((double) f6);
            double cosTheta = Math.cos(thetaD);
            double sinTheta = Math.sin(thetaD);
            double x0p = ((((double) f) * cosTheta) + (((double) f2) * sinTheta)) / ((double) f4);
            double y0p = ((((double) (-f)) * sinTheta) + (((double) f2) * cosTheta)) / ((double) f5);
            double x1p = ((((double) adjust) * cosTheta) + (((double) f3) * sinTheta)) / ((double) f4);
            double y1p = ((((double) (-adjust)) * sinTheta) + (((double) f3) * cosTheta)) / ((double) f5);
            double dx = x0p - x1p;
            double dy = y0p - y1p;
            double xm = (x0p + x1p) / 2.0d;
            double ym = (y0p + y1p) / 2.0d;
            double dsq = (dx * dx) + (dy * dy);
            if (dsq == 0.0d) {
                Log.w(PathParser.LOGTAG, " Points are coincident");
                return;
            }
            double disc = (1.0d / dsq) - 0.25d;
            if (disc < 0.0d) {
                Log.w(PathParser.LOGTAG, "Points are too far apart " + dsq);
                float adjust2 = (float) (Math.sqrt(dsq) / 1.99999d);
                double d = disc;
                float f7 = adjust2;
                double d2 = dsq;
                double d3 = thetaD;
                drawArc(p, f, f2, adjust, f3, f4 * adjust2, f5 * adjust2, f6, isMoreThanHalf, isPositiveArc);
                return;
            }
            double d4 = dsq;
            double thetaD2 = thetaD;
            double s = Math.sqrt(disc);
            double sdx = s * dx;
            double sdy = s * dy;
            boolean z2 = isPositiveArc;
            if (isMoreThanHalf == z2) {
                cx = xm - sdy;
                cy = ym + sdx;
            } else {
                cx = xm + sdy;
                cy = ym - sdx;
            }
            double d5 = s;
            double eta0 = Math.atan2(y0p - cy, x0p - cx);
            double d6 = sdx;
            double eta1 = Math.atan2(y1p - cy, x1p - cx);
            double sweep = eta1 - eta0;
            if (z2 != (sweep >= 0.0d)) {
                if (sweep > 0.0d) {
                    sweep -= 6.283185307179586d;
                } else {
                    sweep += 6.283185307179586d;
                }
            }
            double d7 = sdy;
            float f8 = a;
            double cx2 = cx * ((double) f8);
            float f9 = b;
            double cy2 = cy * ((double) f9);
            double tcx = cx2;
            double d8 = tcx;
            arcToBezier(p, (cx2 * cosTheta) - (cy2 * sinTheta), (tcx * sinTheta) + (cy2 * cosTheta), (double) f8, (double) f9, (double) f, (double) f2, thetaD2, eta0, sweep);
        }

        private static void arcToBezier(Path p, double cx, double cy, double a, double b, double e1x, double e1y, double theta, double start, double sweep) {
            double eta1 = a;
            int numSegments = (int) Math.ceil(Math.abs((sweep * 4.0d) / 3.141592653589793d));
            double eta12 = start;
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            double cosEta1 = Math.cos(eta12);
            double sinEta1 = Math.sin(eta12);
            double anglePerSegment = sweep / ((double) numSegments);
            int i = 0;
            double ep1y = ((-eta1) * sinTheta * sinEta1) + (b * cosTheta * cosEta1);
            double e1y2 = e1y;
            double ep1x = (((-eta1) * cosTheta) * sinEta1) - ((b * sinTheta) * cosEta1);
            double e1x2 = e1x;
            while (true) {
                int i2 = i;
                if (i2 < numSegments) {
                    int i3 = i2;
                    double eta2 = eta12 + anglePerSegment;
                    double sinEta2 = Math.sin(eta2);
                    double cosEta2 = Math.cos(eta2);
                    double anglePerSegment2 = anglePerSegment;
                    double e2x = (cx + ((eta1 * cosTheta) * cosEta2)) - ((b * sinTheta) * sinEta2);
                    double e2y = cy + (eta1 * sinTheta * cosEta2) + (b * cosTheta * sinEta2);
                    double ep2x = (((-eta1) * cosTheta) * sinEta2) - ((b * sinTheta) * cosEta2);
                    double ep2y = ((-eta1) * sinTheta * sinEta2) + (b * cosTheta * cosEta2);
                    double tanDiff2 = Math.tan((eta2 - eta12) / 2.0d);
                    double d = tanDiff2;
                    double alpha = (Math.sin(eta2 - eta12) * (Math.sqrt(4.0d + ((3.0d * tanDiff2) * tanDiff2)) - 1.0d)) / 3.0d;
                    double q1x = e1x2 + (alpha * ep1x);
                    int numSegments2 = numSegments;
                    double d2 = eta12;
                    double q1y = e1y2 + (alpha * ep1y);
                    double q2x = e2x - (alpha * ep2x);
                    double d3 = alpha;
                    double alpha2 = e2y - (alpha * ep2y);
                    double sinTheta2 = sinTheta;
                    Path path = p;
                    path.rLineTo(0.0f, 0.0f);
                    double d4 = q1x;
                    float f = (float) alpha2;
                    double d5 = alpha2;
                    double d6 = q1y;
                    double q2y = e2x;
                    double d7 = q2x;
                    double e2y2 = e2y;
                    path.cubicTo((float) q1x, (float) q1y, (float) q2x, f, (float) q2y, (float) e2y2);
                    e1x2 = q2y;
                    e1y2 = e2y2;
                    ep1x = ep2x;
                    ep1y = ep2y;
                    i = i3 + 1;
                    eta12 = eta2;
                    anglePerSegment = anglePerSegment2;
                    numSegments = numSegments2;
                    cosTheta = cosTheta;
                    sinTheta = sinTheta2;
                    eta1 = a;
                } else {
                    int i4 = numSegments;
                    double d8 = eta12;
                    double d9 = cosTheta;
                    double d10 = sinTheta;
                    Path path2 = p;
                    return;
                }
            }
        }
    }

    static float[] copyOfRange(float[] original, int start, int end) {
        if (start <= end) {
            int originalLength = original.length;
            if (start < 0 || start > originalLength) {
                throw new ArrayIndexOutOfBoundsException();
            }
            int resultLength = end - start;
            float[] result = new float[resultLength];
            System.arraycopy(original, start, result, 0, Math.min(resultLength, originalLength - start));
            return result;
        }
        throw new IllegalArgumentException();
    }

    public static Path createPathFromPathData(String pathData) {
        Path path = new Path();
        PathDataNode[] nodes = createNodesFromPathData(pathData);
        if (nodes == null) {
            return null;
        }
        try {
            PathDataNode.nodesToPath(nodes, path);
            return path;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error in parsing " + pathData, e);
        }
    }

    public static PathDataNode[] createNodesFromPathData(String pathData) {
        if (pathData == null) {
            return null;
        }
        int start = 0;
        int end = 1;
        ArrayList<PathDataNode> list = new ArrayList<>();
        while (end < pathData.length()) {
            int end2 = nextStart(pathData, end);
            String s = pathData.substring(start, end2).trim();
            if (s.length() > 0) {
                addNode(list, s.charAt(0), getFloats(s));
            }
            start = end2;
            end = end2 + 1;
        }
        if (end - start == 1 && start < pathData.length()) {
            addNode(list, pathData.charAt(start), new float[0]);
        }
        return (PathDataNode[]) list.toArray(new PathDataNode[list.size()]);
    }

    public static PathDataNode[] deepCopyNodes(PathDataNode[] source) {
        if (source == null) {
            return null;
        }
        PathDataNode[] copy = new PathDataNode[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new PathDataNode(source[i]);
        }
        return copy;
    }

    public static boolean canMorph(PathDataNode[] nodesFrom, PathDataNode[] nodesTo) {
        if (nodesFrom == null || nodesTo == null || nodesFrom.length != nodesTo.length) {
            return false;
        }
        for (int i = 0; i < nodesFrom.length; i++) {
            if (nodesFrom[i].mType != nodesTo[i].mType || nodesFrom[i].mParams.length != nodesTo[i].mParams.length) {
                return false;
            }
        }
        return true;
    }

    public static void updateNodes(PathDataNode[] target, PathDataNode[] source) {
        for (int i = 0; i < source.length; i++) {
            target[i].mType = source[i].mType;
            for (int j = 0; j < source[i].mParams.length; j++) {
                target[i].mParams[j] = source[i].mParams[j];
            }
        }
    }

    private static int nextStart(String s, int end) {
        while (end < s.length()) {
            char c = s.charAt(end);
            if (((c - 'A') * (c - 'Z') <= 0 || (c - 'a') * (c - 'z') <= 0) && c != 'e' && c != 'E') {
                return end;
            }
            end++;
        }
        return end;
    }

    private static void addNode(ArrayList<PathDataNode> list, char cmd, float[] val) {
        list.add(new PathDataNode(cmd, val));
    }

    private static float[] getFloats(String s) {
        if (s.charAt(0) == 'z' || s.charAt(0) == 'Z') {
            return new float[0];
        }
        try {
            float[] results = new float[s.length()];
            int count = 0;
            int startPosition = 1;
            ExtractFloatResult result = new ExtractFloatResult();
            int totalLength = s.length();
            while (startPosition < totalLength) {
                extract(s, startPosition, result);
                int endPosition = result.mEndPosition;
                if (startPosition < endPosition) {
                    results[count] = Float.parseFloat(s.substring(startPosition, endPosition));
                    count++;
                }
                if (result.mEndWithNegOrDot != 0) {
                    startPosition = endPosition;
                } else {
                    startPosition = endPosition + 1;
                }
            }
            return copyOfRange(results, 0, count);
        } catch (NumberFormatException e) {
            throw new RuntimeException("error in parsing \"" + s + "\"", e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b A[LOOP:0: B:1:0x0007->B:20:0x003b, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x003e A[SYNTHETIC] */
    private static void extract(String s, int start, ExtractFloatResult result) {
        int currentIndex = start;
        boolean foundSeparator = false;
        boolean isExponential = false;
        result.mEndWithNegOrDot = false;
        boolean secondDot = false;
        while (currentIndex < s.length()) {
            boolean isPrevExponential = isExponential;
            isExponential = false;
            char currentChar = s.charAt(currentIndex);
            if (currentChar != ' ') {
                if (currentChar != 'E' && currentChar != 'e') {
                    switch (currentChar) {
                        case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
                            break;
                        case MotionEventCompat.AXIS_GENERIC_14 /*45*/:
                            if (currentIndex != start && !isPrevExponential) {
                                foundSeparator = true;
                                result.mEndWithNegOrDot = true;
                                break;
                            }
                        case MotionEventCompat.AXIS_GENERIC_15 /*46*/:
                            if (secondDot) {
                                foundSeparator = true;
                                result.mEndWithNegOrDot = true;
                                break;
                            } else {
                                secondDot = true;
                                break;
                            }
                    }
                } else {
                    isExponential = true;
                    if (!foundSeparator) {
                        result.mEndPosition = currentIndex;
                    }
                    currentIndex++;
                }
            }
            foundSeparator = true;
            if (!foundSeparator) {
            }
        }
        result.mEndPosition = currentIndex;
    }

    private PathParser() {
    }
}
