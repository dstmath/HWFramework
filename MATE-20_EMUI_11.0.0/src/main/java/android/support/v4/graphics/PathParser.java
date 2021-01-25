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
            if (!(nodesFrom[i].mType == nodesTo[i].mType && nodesFrom[i].mParams.length == nodesTo[i].mParams.length)) {
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

    /* access modifiers changed from: private */
    public static class ExtractFloatResult {
        int mEndPosition;
        boolean mEndWithNegOrDot;

        ExtractFloatResult() {
        }
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
                if (result.mEndWithNegOrDot) {
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
        boolean foundSeparator = false;
        boolean isExponential = false;
        result.mEndWithNegOrDot = false;
        boolean secondDot = false;
        for (int currentIndex = start; currentIndex < s.length(); currentIndex++) {
            isExponential = false;
            char currentChar = s.charAt(currentIndex);
            if (currentChar != ' ') {
                if (currentChar == 'E' || currentChar == 'e') {
                    isExponential = true;
                    if (foundSeparator) {
                        result.mEndPosition = currentIndex;
                    }
                } else {
                    switch (currentChar) {
                        case MotionEventCompat.AXIS_GENERIC_14 /* 45 */:
                            if (currentIndex != start && !isExponential) {
                                foundSeparator = true;
                                result.mEndWithNegOrDot = true;
                                break;
                            }
                        case MotionEventCompat.AXIS_GENERIC_15 /* 46 */:
                            if (!secondDot) {
                                secondDot = true;
                                break;
                            } else {
                                foundSeparator = true;
                                result.mEndWithNegOrDot = true;
                                break;
                            }
                    }
                    if (foundSeparator) {
                    }
                }
            }
            foundSeparator = true;
            if (foundSeparator) {
            }
        }
        result.mEndPosition = currentIndex;
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

        private static void addCommand(Path path, float[] current, char previousCmd, char cmd, float[] val) {
            int k;
            float ctrlPointX;
            float ctrlPointY;
            float reflectiveCtrlPointY;
            float reflectiveCtrlPointX;
            float ctrlPointY2;
            float ctrlPointX2;
            float reflectiveCtrlPointY2;
            float reflectiveCtrlPointX2;
            int incr = 2;
            boolean z = false;
            float currentX = current[0];
            float currentY = current[1];
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
                case CloseRangeConstant.CALLBACK_ONDEVICE /* 104 */:
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
                    currentX = currentSegmentStartX;
                    currentY = currentSegmentStartY;
                    ctrlPointX3 = currentSegmentStartX;
                    ctrlPointY3 = currentSegmentStartY;
                    path.moveTo(currentX, currentY);
                    break;
            }
            char previousCmd2 = previousCmd;
            float currentX2 = currentX;
            float currentY2 = currentY;
            float ctrlPointX4 = ctrlPointX3;
            float ctrlPointY4 = ctrlPointY3;
            float currentSegmentStartX2 = currentSegmentStartX;
            float currentSegmentStartY2 = currentSegmentStartY;
            int k2 = 0;
            while (k2 < val.length) {
                switch (cmd) {
                    case 'A':
                        k = k2;
                        drawArc(path, currentX2, currentY2, val[k + 5], val[k + 6], val[k + 0], val[k + 1], val[k + 2], val[k + 3] != 0.0f, val[k + 4] != 0.0f);
                        currentX2 = val[k + 5];
                        currentY2 = val[k + 6];
                        ctrlPointX = currentX2;
                        ctrlPointY = currentY2;
                        ctrlPointX4 = ctrlPointX;
                        ctrlPointY4 = ctrlPointY;
                        break;
                    case 'C':
                        k = k2;
                        path.cubicTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3], val[k + 4], val[k + 5]);
                        currentX2 = val[k + 4];
                        currentY2 = val[k + 5];
                        ctrlPointX = val[k + 2];
                        ctrlPointY = val[k + 3];
                        ctrlPointX4 = ctrlPointX;
                        ctrlPointY4 = ctrlPointY;
                        break;
                    case 'H':
                        k = k2;
                        path.lineTo(val[k + 0], currentY2);
                        currentX2 = val[k + 0];
                        break;
                    case 'L':
                        k = k2;
                        path.lineTo(val[k + 0], val[k + 1]);
                        currentX2 = val[k + 0];
                        currentY2 = val[k + 1];
                        break;
                    case 'M':
                        k = k2;
                        currentX2 = val[k + 0];
                        currentY2 = val[k + 1];
                        if (k <= 0) {
                            path.moveTo(val[k + 0], val[k + 1]);
                            currentSegmentStartX2 = currentX2;
                            currentSegmentStartY2 = currentY2;
                            break;
                        } else {
                            path.lineTo(val[k + 0], val[k + 1]);
                            break;
                        }
                    case 'Q':
                        k = k2;
                        path.quadTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3]);
                        ctrlPointX = val[k + 0];
                        ctrlPointY = val[k + 1];
                        currentX2 = val[k + 2];
                        currentY2 = val[k + 3];
                        ctrlPointX4 = ctrlPointX;
                        ctrlPointY4 = ctrlPointY;
                        break;
                    case 'S':
                        k = k2;
                        if (previousCmd2 == 'c' || previousCmd2 == 's' || previousCmd2 == 'C' || previousCmd2 == 'S') {
                            reflectiveCtrlPointX = (2.0f * currentX2) - ctrlPointX4;
                            reflectiveCtrlPointY = (2.0f * currentY2) - ctrlPointY4;
                        } else {
                            reflectiveCtrlPointX = currentX2;
                            reflectiveCtrlPointY = currentY2;
                        }
                        path.cubicTo(reflectiveCtrlPointX, reflectiveCtrlPointY, val[k + 0], val[k + 1], val[k + 2], val[k + 3]);
                        float ctrlPointX5 = val[k + 0];
                        float ctrlPointY5 = val[k + 1];
                        float currentX3 = val[k + 2];
                        currentY2 = val[k + 3];
                        ctrlPointX4 = ctrlPointX5;
                        ctrlPointY4 = ctrlPointY5;
                        currentX2 = currentX3;
                        break;
                    case 'T':
                        k = k2;
                        float reflectiveCtrlPointX3 = currentX2;
                        float reflectiveCtrlPointY3 = currentY2;
                        if (previousCmd2 == 'q' || previousCmd2 == 't' || previousCmd2 == 'Q' || previousCmd2 == 'T') {
                            reflectiveCtrlPointX3 = (2.0f * currentX2) - ctrlPointX4;
                            reflectiveCtrlPointY3 = (2.0f * currentY2) - ctrlPointY4;
                        }
                        path.quadTo(reflectiveCtrlPointX3, reflectiveCtrlPointY3, val[k + 0], val[k + 1]);
                        currentX2 = val[k + 0];
                        currentY2 = val[k + 1];
                        ctrlPointX4 = reflectiveCtrlPointX3;
                        ctrlPointY4 = reflectiveCtrlPointY3;
                        break;
                    case 'V':
                        k = k2;
                        path.lineTo(currentX2, val[k + 0]);
                        currentY2 = val[k + 0];
                        break;
                    case 'a':
                        k = k2;
                        drawArc(path, currentX2, currentY2, val[k + 5] + currentX2, val[k + 6] + currentY2, val[k + 0], val[k + 1], val[k + 2], val[k + 3] != 0.0f ? true : z, val[k + 4] != 0.0f ? true : z);
                        currentX2 += val[k + 5];
                        currentY2 += val[k + 6];
                        ctrlPointX = currentX2;
                        ctrlPointY = currentY2;
                        ctrlPointX4 = ctrlPointX;
                        ctrlPointY4 = ctrlPointY;
                        break;
                    case 'c':
                        k = k2;
                        path.rCubicTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3], val[k + 4], val[k + 5]);
                        ctrlPointX2 = val[k + 2] + currentX2;
                        ctrlPointY2 = val[k + 3] + currentY2;
                        currentX2 += val[k + 4];
                        currentY2 += val[k + 5];
                        ctrlPointX4 = ctrlPointX2;
                        ctrlPointY4 = ctrlPointY2;
                        break;
                    case CloseRangeConstant.CALLBACK_ONDEVICE /* 104 */:
                        k = k2;
                        path.rLineTo(val[k + 0], 0.0f);
                        currentX2 += val[k + 0];
                        break;
                    case 'l':
                        k = k2;
                        path.rLineTo(val[k + 0], val[k + 1]);
                        currentX2 += val[k + 0];
                        currentY2 += val[k + 1];
                        break;
                    case 'm':
                        k = k2;
                        currentX2 += val[k + 0];
                        currentY2 += val[k + 1];
                        if (k > 0) {
                            path.rLineTo(val[k + 0], val[k + 1]);
                        } else {
                            path.rMoveTo(val[k + 0], val[k + 1]);
                            currentSegmentStartX2 = currentX2;
                            currentSegmentStartY2 = currentY2;
                        }
                        break;
                    case 'q':
                        k = k2;
                        path.rQuadTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3]);
                        ctrlPointX2 = val[k + 0] + currentX2;
                        ctrlPointY2 = val[k + 1] + currentY2;
                        currentX2 += val[k + 2];
                        currentY2 += val[k + 3];
                        ctrlPointX4 = ctrlPointX2;
                        ctrlPointY4 = ctrlPointY2;
                        break;
                    case 's':
                        if (previousCmd2 == 'c' || previousCmd2 == 's' || previousCmd2 == 'C' || previousCmd2 == 'S') {
                            reflectiveCtrlPointX2 = currentX2 - ctrlPointX4;
                            reflectiveCtrlPointY2 = currentY2 - ctrlPointY4;
                        } else {
                            reflectiveCtrlPointX2 = 0.0f;
                            reflectiveCtrlPointY2 = 0.0f;
                        }
                        k = k2;
                        path.rCubicTo(reflectiveCtrlPointX2, reflectiveCtrlPointY2, val[k2 + 0], val[k2 + 1], val[k2 + 2], val[k2 + 3]);
                        ctrlPointX2 = val[k + 0] + currentX2;
                        ctrlPointY2 = val[k + 1] + currentY2;
                        currentX2 += val[k + 2];
                        currentY2 += val[k + 3];
                        ctrlPointX4 = ctrlPointX2;
                        ctrlPointY4 = ctrlPointY2;
                        break;
                    case 't':
                        float reflectiveCtrlPointX4 = 0.0f;
                        float reflectiveCtrlPointY4 = 0.0f;
                        if (previousCmd2 == 'q' || previousCmd2 == 't' || previousCmd2 == 'Q' || previousCmd2 == 'T') {
                            reflectiveCtrlPointX4 = currentX2 - ctrlPointX4;
                            reflectiveCtrlPointY4 = currentY2 - ctrlPointY4;
                        }
                        path.rQuadTo(reflectiveCtrlPointX4, reflectiveCtrlPointY4, val[k2 + 0], val[k2 + 1]);
                        float ctrlPointX6 = currentX2 + reflectiveCtrlPointX4;
                        float ctrlPointY6 = currentY2 + reflectiveCtrlPointY4;
                        currentX2 += val[k2 + 0];
                        currentY2 += val[k2 + 1];
                        ctrlPointX4 = ctrlPointX6;
                        ctrlPointY4 = ctrlPointY6;
                        k = k2;
                        break;
                    case 'v':
                        path.rLineTo(0.0f, val[k2 + 0]);
                        currentY2 += val[k2 + 0];
                        k = k2;
                        break;
                    default:
                        k = k2;
                        break;
                }
                previousCmd2 = cmd;
                k2 = k + incr;
                z = false;
            }
            current[0] = currentX2;
            current[1] = currentY2;
            current[2] = ctrlPointX4;
            current[3] = ctrlPointY4;
            current[4] = currentSegmentStartX2;
            current[5] = currentSegmentStartY2;
        }

        private static void drawArc(Path p, float x0, float y0, float x1, float y1, float a, float b, float theta, boolean isMoreThanHalf, boolean isPositiveArc) {
            double cy;
            double cx;
            double thetaD = Math.toRadians((double) theta);
            double cosTheta = Math.cos(thetaD);
            double sinTheta = Math.sin(thetaD);
            double x0p = ((((double) x0) * cosTheta) + (((double) y0) * sinTheta)) / ((double) a);
            double y0p = ((((double) (-x0)) * sinTheta) + (((double) y0) * cosTheta)) / ((double) b);
            double x1p = ((((double) x1) * cosTheta) + (((double) y1) * sinTheta)) / ((double) a);
            double y1p = ((((double) (-x1)) * sinTheta) + (((double) y1) * cosTheta)) / ((double) b);
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
                float adjust = (float) (Math.sqrt(dsq) / 1.99999d);
                drawArc(p, x0, y0, x1, y1, a * adjust, b * adjust, theta, isMoreThanHalf, isPositiveArc);
                return;
            }
            double s = Math.sqrt(disc);
            double sdx = s * dx;
            double sdy = s * dy;
            if (isMoreThanHalf == isPositiveArc) {
                cx = xm - sdy;
                cy = ym + sdx;
            } else {
                cx = xm + sdy;
                cy = ym - sdx;
            }
            double eta0 = Math.atan2(y0p - cy, x0p - cx);
            double sweep = Math.atan2(y1p - cy, x1p - cx) - eta0;
            if (isPositiveArc != (sweep >= 0.0d)) {
                if (sweep > 0.0d) {
                    sweep -= 6.283185307179586d;
                } else {
                    sweep += 6.283185307179586d;
                }
            }
            double cx2 = cx * ((double) a);
            double cy2 = cy * ((double) b);
            arcToBezier(p, (cx2 * cosTheta) - (cy2 * sinTheta), (cx2 * sinTheta) + (cy2 * cosTheta), (double) a, (double) b, (double) x0, (double) y0, thetaD, eta0, sweep);
        }

        /* JADX INFO: Multiple debug info for r4v1 double: [D('i' int), D('eta2' double)] */
        /* JADX INFO: Multiple debug info for r6v12 double: [D('anglePerSegment' double), D('e2x' double)] */
        /* JADX INFO: Multiple debug info for r6v13 double: [D('e2y' double), D('e2x' double)] */
        /* JADX INFO: Multiple debug info for r8v6 double: [D('q1y' double), D('numSegments' int)] */
        /* JADX INFO: Multiple debug info for r2v4 double: [D('q2y' double), D('alpha' double)] */
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
            while (i < numSegments) {
                double eta2 = eta12 + anglePerSegment;
                double sinEta2 = Math.sin(eta2);
                double cosEta2 = Math.cos(eta2);
                double e2x = (cx + ((eta1 * cosTheta) * cosEta2)) - ((b * sinTheta) * sinEta2);
                double e2x2 = cy + (eta1 * sinTheta * cosEta2) + (b * cosTheta * sinEta2);
                double ep2x = (((-eta1) * cosTheta) * sinEta2) - ((b * sinTheta) * cosEta2);
                double ep2y = ((-eta1) * sinTheta * sinEta2) + (b * cosTheta * cosEta2);
                double tanDiff2 = Math.tan((eta2 - eta12) / 2.0d);
                double alpha = (Math.sin(eta2 - eta12) * (Math.sqrt(4.0d + ((3.0d * tanDiff2) * tanDiff2)) - 1.0d)) / 3.0d;
                p.rLineTo(0.0f, 0.0f);
                p.cubicTo((float) (e1x2 + (alpha * ep1x)), (float) (e1y2 + (alpha * ep1y)), (float) (e2x - (alpha * ep2x)), (float) (e2x2 - (alpha * ep2y)), (float) e2x, (float) e2x2);
                e1x2 = e2x;
                e1y2 = e2x2;
                ep1x = ep2x;
                ep1y = ep2y;
                i++;
                eta12 = eta2;
                anglePerSegment = anglePerSegment;
                numSegments = numSegments;
                cosTheta = cosTheta;
                sinTheta = sinTheta;
                eta1 = a;
            }
        }
    }

    private PathParser() {
    }
}
