package android.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

class Utils {
    private static final String TAG = "Utils";

    Utils() {
    }

    public static <T extends Comparable<? super T>> void sortDistinctRanges(Range<T>[] ranges) {
        Arrays.sort(ranges, new Comparator<Range<T>>() {
            public int compare(Range<T> lhs, Range<T> rhs) {
                if (lhs.getUpper().compareTo(rhs.getLower()) < 0) {
                    return -1;
                }
                if (lhs.getLower().compareTo(rhs.getUpper()) > 0) {
                    return 1;
                }
                throw new IllegalArgumentException("sample rate ranges must be distinct (" + lhs + " and " + rhs + ")");
            }
        });
    }

    public static <T extends Comparable<? super T>> Range<T>[] intersectSortedDistinctRanges(Range<T>[] one, Range<T>[] another) {
        int ix = 0;
        Vector<Range<T>> result = new Vector();
        for (Range<T> range : another) {
            while (ix < one.length && one[ix].getUpper().compareTo(range.getLower()) < 0) {
                ix++;
            }
            while (ix < one.length && one[ix].getUpper().compareTo(range.getUpper()) < 0) {
                result.add(range.intersect(one[ix]));
                ix++;
            }
            if (ix == one.length) {
                break;
            }
            if (one[ix].getLower().compareTo(range.getUpper()) <= 0) {
                result.add(range.intersect(one[ix]));
            }
        }
        return (Range[]) result.toArray(new Range[result.size()]);
    }

    public static <T extends Comparable<? super T>> int binarySearchDistinctRanges(Range<T>[] ranges, T value) {
        return Arrays.binarySearch(ranges, Range.create(value, value), new Comparator<Range<T>>() {
            public int compare(Range<T> lhs, Range<T> rhs) {
                if (lhs.getUpper().compareTo(rhs.getLower()) < 0) {
                    return -1;
                }
                if (lhs.getLower().compareTo(rhs.getUpper()) > 0) {
                    return 1;
                }
                return 0;
            }
        });
    }

    static int gcd(int a, int b) {
        if (a == 0 && b == 0) {
            return 1;
        }
        if (b < 0) {
            b = -b;
        }
        if (a < 0) {
            a = -a;
        }
        while (a != 0) {
            int c = b % a;
            b = a;
            a = c;
        }
        return b;
    }

    static Range<Integer> factorRange(Range<Integer> range, int factor) {
        if (factor == 1) {
            return range;
        }
        return Range.create(Integer.valueOf(divUp(((Integer) range.getLower()).intValue(), factor)), Integer.valueOf(((Integer) range.getUpper()).intValue() / factor));
    }

    static Range<Long> factorRange(Range<Long> range, long factor) {
        if (factor == 1) {
            return range;
        }
        return Range.create(Long.valueOf(divUp(((Long) range.getLower()).longValue(), factor)), Long.valueOf(((Long) range.getUpper()).longValue() / factor));
    }

    private static Rational scaleRatio(Rational ratio, int num, int den) {
        int common = gcd(num, den);
        return new Rational((int) (((double) ratio.getNumerator()) * ((double) (num / common))), (int) (((double) ratio.getDenominator()) * ((double) (den / common))));
    }

    static Range<Rational> scaleRange(Range<Rational> range, int num, int den) {
        if (num == den) {
            return range;
        }
        return Range.create(scaleRatio((Rational) range.getLower(), num, den), scaleRatio((Rational) range.getUpper(), num, den));
    }

    static Range<Integer> alignRange(Range<Integer> range, int align) {
        return range.intersect(Integer.valueOf(divUp(((Integer) range.getLower()).intValue(), align) * align), Integer.valueOf((((Integer) range.getUpper()).intValue() / align) * align));
    }

    static int divUp(int num, int den) {
        return ((num + den) - 1) / den;
    }

    static long divUp(long num, long den) {
        return ((num + den) - 1) / den;
    }

    private static long lcm(int a, int b) {
        if (a != 0 && b != 0) {
            return (((long) a) * ((long) b)) / ((long) gcd(a, b));
        }
        throw new IllegalArgumentException("lce is not defined for zero arguments");
    }

    static Range<Integer> intRangeFor(double v) {
        return Range.create(Integer.valueOf((int) v), Integer.valueOf((int) Math.ceil(v)));
    }

    static Range<Long> longRangeFor(double v) {
        return Range.create(Long.valueOf((long) v), Long.valueOf((long) Math.ceil(v)));
    }

    static Size parseSize(Object o, Size fallback) {
        try {
            return Size.parseSize((String) o);
        } catch (ClassCastException e) {
        } catch (NumberFormatException e2) {
        } catch (NullPointerException e3) {
            return fallback;
        }
        Log.w(TAG, "could not parse size '" + o + "'");
        return fallback;
    }

    static int parseIntSafely(Object o, int fallback) {
        if (o == null) {
            return fallback;
        }
        try {
            return Integer.parseInt((String) o);
        } catch (ClassCastException e) {
        } catch (NumberFormatException e2) {
        } catch (NullPointerException e3) {
            return fallback;
        }
        Log.w(TAG, "could not parse integer '" + o + "'");
        return fallback;
    }

    static Range<Integer> parseIntRange(Object o, Range<Integer> fallback) {
        try {
            String s = (String) o;
            int ix = s.indexOf(45);
            if (ix >= 0) {
                return Range.create(Integer.valueOf(Integer.parseInt(s.substring(0, ix), 10)), Integer.valueOf(Integer.parseInt(s.substring(ix + 1), 10)));
            }
            int value = Integer.parseInt(s);
            return Range.create(Integer.valueOf(value), Integer.valueOf(value));
        } catch (ClassCastException e) {
            Log.w(TAG, "could not parse integer range '" + o + "'");
            return fallback;
        } catch (NumberFormatException e2) {
            Log.w(TAG, "could not parse integer range '" + o + "'");
            return fallback;
        } catch (NullPointerException e3) {
            return fallback;
        } catch (IllegalArgumentException e4) {
            Log.w(TAG, "could not parse integer range '" + o + "'");
            return fallback;
        }
    }

    static Range<Long> parseLongRange(Object o, Range<Long> fallback) {
        try {
            String s = (String) o;
            int ix = s.indexOf(45);
            if (ix >= 0) {
                return Range.create(Long.valueOf(Long.parseLong(s.substring(0, ix), 10)), Long.valueOf(Long.parseLong(s.substring(ix + 1), 10)));
            }
            long value = Long.parseLong(s);
            return Range.create(Long.valueOf(value), Long.valueOf(value));
        } catch (ClassCastException e) {
            Log.w(TAG, "could not parse long range '" + o + "'");
            return fallback;
        } catch (NumberFormatException e2) {
            Log.w(TAG, "could not parse long range '" + o + "'");
            return fallback;
        } catch (NullPointerException e3) {
            return fallback;
        } catch (IllegalArgumentException e4) {
            Log.w(TAG, "could not parse long range '" + o + "'");
            return fallback;
        }
    }

    static Range<Rational> parseRationalRange(Object o, Range<Rational> fallback) {
        try {
            String s = (String) o;
            int ix = s.indexOf(45);
            if (ix >= 0) {
                return Range.create(Rational.parseRational(s.substring(0, ix)), Rational.parseRational(s.substring(ix + 1)));
            }
            Rational value = Rational.parseRational(s);
            return Range.create(value, value);
        } catch (ClassCastException e) {
            Log.w(TAG, "could not parse rational range '" + o + "'");
            return fallback;
        } catch (NumberFormatException e2) {
            Log.w(TAG, "could not parse rational range '" + o + "'");
            return fallback;
        } catch (NullPointerException e3) {
            return fallback;
        } catch (IllegalArgumentException e4) {
            Log.w(TAG, "could not parse rational range '" + o + "'");
            return fallback;
        }
    }

    static Pair<Size, Size> parseSizeRange(Object o) {
        try {
            String s = (String) o;
            int ix = s.indexOf(45);
            if (ix >= 0) {
                return Pair.create(Size.parseSize(s.substring(0, ix)), Size.parseSize(s.substring(ix + 1)));
            }
            Size value = Size.parseSize(s);
            return Pair.create(value, value);
        } catch (ClassCastException e) {
            Log.w(TAG, "could not parse size range '" + o + "'");
            return null;
        } catch (NumberFormatException e2) {
            Log.w(TAG, "could not parse size range '" + o + "'");
            return null;
        } catch (NullPointerException e3) {
            return null;
        } catch (IllegalArgumentException e4) {
            Log.w(TAG, "could not parse size range '" + o + "'");
            return null;
        }
    }

    public static File getUniqueExternalFile(Context context, String subdirectory, String fileName, String mimeType) {
        File externalStorage = Environment.getExternalStoragePublicDirectory(subdirectory);
        externalStorage.mkdirs();
        try {
            return FileUtils.buildUniqueFile(externalStorage, mimeType, fileName);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to get a unique file name: " + e);
            return null;
        }
    }

    static String getFileDisplayNameFromUri(Context context, Uri uri) {
        Cursor cursor;
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            return uri.getLastPathSegment();
        }
        if ("content".equals(scheme)) {
            cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
                if (cursor == null || cursor.getCount() == 0) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th4) {
                            th3 = th4;
                        }
                    }
                    if (th3 != null) {
                        throw th3;
                    }
                }
                cursor.moveToFirst();
                String string = cursor.getString(cursor.getColumnIndex("_display_name"));
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable th5) {
                        th3 = th5;
                    }
                }
                if (th3 == null) {
                    return string;
                }
                throw th3;
            } catch (Throwable th22) {
                Throwable th6 = th22;
                th22 = th;
                th = th6;
            }
        }
        return uri.toString();
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable th7) {
                if (th22 == null) {
                    th22 = th7;
                } else if (th22 != th7) {
                    th22.addSuppressed(th7);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }
}
