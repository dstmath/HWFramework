package android.util;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Path;

public class PathParser {
    static final String LOGTAG = PathParser.class.getSimpleName();

    private static native boolean nCanMorph(long j, long j2);

    /* access modifiers changed from: private */
    public static native long nCreateEmptyPathData();

    /* access modifiers changed from: private */
    public static native long nCreatePathData(long j);

    /* access modifiers changed from: private */
    public static native long nCreatePathDataFromString(String str, int i);

    private static native void nCreatePathFromPathData(long j, long j2);

    /* access modifiers changed from: private */
    public static native void nFinalize(long j);

    private static native boolean nInterpolatePathData(long j, long j2, long j3, float f);

    private static native void nParseStringForPath(long j, String str, int i);

    /* access modifiers changed from: private */
    public static native void nSetPathData(long j, long j2);

    @UnsupportedAppUsage
    public static Path createPathFromPathData(String pathString) {
        if (pathString != null) {
            Path path = new Path();
            nParseStringForPath(path.mNativePath, pathString, pathString.length());
            return path;
        }
        throw new IllegalArgumentException("Path string can not be null.");
    }

    public static void createPathFromPathData(Path outPath, PathData data) {
        nCreatePathFromPathData(outPath.mNativePath, data.mNativePathData);
    }

    public static boolean canMorph(PathData pathDataFrom, PathData pathDataTo) {
        return nCanMorph(pathDataFrom.mNativePathData, pathDataTo.mNativePathData);
    }

    public static class PathData {
        long mNativePathData;

        public PathData() {
            this.mNativePathData = 0;
            this.mNativePathData = PathParser.nCreateEmptyPathData();
        }

        public PathData(PathData data) {
            this.mNativePathData = 0;
            this.mNativePathData = PathParser.nCreatePathData(data.mNativePathData);
        }

        public PathData(String pathString) {
            this.mNativePathData = 0;
            this.mNativePathData = PathParser.nCreatePathDataFromString(pathString, pathString.length());
            if (this.mNativePathData == 0) {
                throw new IllegalArgumentException("Invalid pathData: " + pathString);
            }
        }

        public long getNativePtr() {
            return this.mNativePathData;
        }

        public void setPathData(PathData source) {
            PathParser.nSetPathData(this.mNativePathData, source.mNativePathData);
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            long j = this.mNativePathData;
            if (j != 0) {
                PathParser.nFinalize(j);
                this.mNativePathData = 0;
            }
            super.finalize();
        }
    }

    public static boolean interpolatePathData(PathData outData, PathData fromData, PathData toData, float fraction) {
        return nInterpolatePathData(outData.mNativePathData, fromData.mNativePathData, toData.mNativePathData, fraction);
    }
}
