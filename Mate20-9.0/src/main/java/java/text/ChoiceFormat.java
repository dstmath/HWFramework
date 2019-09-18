package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import sun.misc.DoubleConsts;

public class ChoiceFormat extends NumberFormat {
    static final long EXPONENT = 9218868437227405312L;
    static final long POSITIVEINFINITY = 9218868437227405312L;
    static final long SIGN = Long.MIN_VALUE;
    private static final long serialVersionUID = 1795184449645032964L;
    private String[] choiceFormats;
    private double[] choiceLimits;

    public void applyPattern(String newPattern) {
        int part;
        String str = newPattern;
        StringBuffer[] segments = new StringBuffer[2];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = new StringBuffer();
        }
        double[] newChoiceLimits = new double[30];
        int count = 0;
        int part2 = 0;
        boolean inQuote = false;
        double oldStartValue = Double.NaN;
        double startValue = 0.0d;
        String[] newChoiceFormats = new String[30];
        int i2 = 0;
        while (i2 < newPattern.length()) {
            char ch = str.charAt(i2);
            if (ch == '\'') {
                if (i2 + 1 >= newPattern.length() || str.charAt(i2 + 1) != ch) {
                    inQuote = !inQuote;
                } else {
                    segments[part2].append(ch);
                    i2++;
                }
            } else if (inQuote) {
                segments[part2].append(ch);
            } else {
                if (ch == '<' || ch == '#' || ch == 8804) {
                    if (segments[0].length() != 0) {
                        try {
                            String tempBuffer = segments[0].toString();
                            if (tempBuffer.equals("∞")) {
                                startValue = Double.POSITIVE_INFINITY;
                            } else if (tempBuffer.equals("-∞")) {
                                startValue = Double.NEGATIVE_INFINITY;
                            } else {
                                String str2 = tempBuffer;
                                startValue = Double.parseDouble(segments[0].toString());
                            }
                            if (!(ch != '<' || startValue == Double.POSITIVE_INFINITY || startValue == Double.NEGATIVE_INFINITY)) {
                                startValue = nextDouble(startValue);
                            }
                            if (startValue > oldStartValue) {
                                segments[0].setLength(0);
                                part = 1;
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else if (ch == '|') {
                    if (count == newChoiceLimits.length) {
                        newChoiceLimits = doubleArraySize(newChoiceLimits);
                        newChoiceFormats = doubleArraySize(newChoiceFormats);
                    }
                    newChoiceLimits[count] = startValue;
                    newChoiceFormats[count] = segments[1].toString();
                    count++;
                    oldStartValue = startValue;
                    segments[1].setLength(0);
                    part = 0;
                } else {
                    segments[part2].append(ch);
                }
                part2 = part;
            }
            i2++;
        }
        if (part2 == 1) {
            if (count == newChoiceLimits.length) {
                newChoiceLimits = doubleArraySize(newChoiceLimits);
                newChoiceFormats = doubleArraySize(newChoiceFormats);
            }
            newChoiceLimits[count] = startValue;
            newChoiceFormats[count] = segments[1].toString();
            count++;
        }
        this.choiceLimits = new double[count];
        System.arraycopy((Object) newChoiceLimits, 0, (Object) this.choiceLimits, 0, count);
        this.choiceFormats = new String[count];
        System.arraycopy((Object) newChoiceFormats, 0, (Object) this.choiceFormats, 0, count);
    }

    public String toPattern() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < this.choiceLimits.length; i++) {
            if (i != 0) {
                result.append('|');
            }
            if (Math.abs(Math.IEEEremainder(this.choiceLimits[i], 1.0d)) < Math.abs(Math.IEEEremainder(previousDouble(this.choiceLimits[i]), 1.0d))) {
                result.append("" + this.choiceLimits[i]);
                result.append('#');
            } else {
                if (this.choiceLimits[i] == Double.POSITIVE_INFINITY) {
                    result.append("∞");
                } else if (this.choiceLimits[i] == Double.NEGATIVE_INFINITY) {
                    result.append("-∞");
                } else {
                    result.append("" + less);
                }
                result.append('<');
            }
            String text = this.choiceFormats[i];
            boolean needQuote = text.indexOf(60) >= 0 || text.indexOf(35) >= 0 || text.indexOf(8804) >= 0 || text.indexOf(124) >= 0;
            if (needQuote) {
                result.append('\'');
            }
            if (text.indexOf(39) < 0) {
                result.append(text);
            } else {
                for (int j = 0; j < text.length(); j++) {
                    char c = text.charAt(j);
                    result.append(c);
                    if (c == '\'') {
                        result.append(c);
                    }
                }
            }
            if (needQuote) {
                result.append('\'');
            }
        }
        return result.toString();
    }

    public ChoiceFormat(String newPattern) {
        applyPattern(newPattern);
    }

    public ChoiceFormat(double[] limits, String[] formats) {
        setChoices(limits, formats);
    }

    public void setChoices(double[] limits, String[] formats) {
        if (limits.length == formats.length) {
            this.choiceLimits = Arrays.copyOf(limits, limits.length);
            this.choiceFormats = (String[]) Arrays.copyOf((T[]) formats, formats.length);
            return;
        }
        throw new IllegalArgumentException("Array and limit arrays must be of the same length.");
    }

    public double[] getLimits() {
        return Arrays.copyOf(this.choiceLimits, this.choiceLimits.length);
    }

    public Object[] getFormats() {
        return Arrays.copyOf((T[]) this.choiceFormats, this.choiceFormats.length);
    }

    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition status) {
        return format((double) number, toAppendTo, status);
    }

    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition status) {
        int i = 0;
        while (i < this.choiceLimits.length && number >= this.choiceLimits[i]) {
            i++;
        }
        int i2 = i - 1;
        if (i2 < 0) {
            i2 = 0;
        }
        toAppendTo.append(this.choiceFormats[i2]);
        return toAppendTo;
    }

    public Number parse(String text, ParsePosition status) {
        int start = status.index;
        double bestNumber = Double.NaN;
        int furthest = start;
        for (int i = 0; i < this.choiceFormats.length; i++) {
            String tempString = this.choiceFormats[i];
            if (text.regionMatches(start, tempString, 0, tempString.length())) {
                status.index = tempString.length() + start;
                double tempNumber = this.choiceLimits[i];
                if (status.index > furthest) {
                    furthest = status.index;
                    bestNumber = tempNumber;
                    if (furthest == text.length()) {
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        status.index = furthest;
        if (status.index == start) {
            status.errorIndex = furthest;
        }
        return new Double(bestNumber);
    }

    public static final double nextDouble(double d) {
        return nextDouble(d, true);
    }

    public static final double previousDouble(double d) {
        return nextDouble(d, false);
    }

    public Object clone() {
        ChoiceFormat other = (ChoiceFormat) super.clone();
        other.choiceLimits = (double[]) this.choiceLimits.clone();
        other.choiceFormats = (String[]) this.choiceFormats.clone();
        return other;
    }

    public int hashCode() {
        int result = this.choiceLimits.length;
        if (this.choiceFormats.length > 0) {
            return result ^ this.choiceFormats[this.choiceFormats.length - 1].hashCode();
        }
        return result;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChoiceFormat other = (ChoiceFormat) obj;
        if (Arrays.equals(this.choiceLimits, other.choiceLimits) && Arrays.equals((Object[]) this.choiceFormats, (Object[]) other.choiceFormats)) {
            z = true;
        }
        return z;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.choiceLimits.length != this.choiceFormats.length) {
            throw new InvalidObjectException("limits and format arrays of different length.");
        }
    }

    public static double nextDouble(double d, boolean positive) {
        if (Double.isNaN(d)) {
            return d;
        }
        if (d == 0.0d) {
            double smallestPositiveDouble = Double.longBitsToDouble(1);
            if (positive) {
                return smallestPositiveDouble;
            }
            return -smallestPositiveDouble;
        }
        long bits = Double.doubleToLongBits(d);
        long magnitude = Long.MAX_VALUE & bits;
        if ((bits > 0) != positive) {
            magnitude--;
        } else if (magnitude != DoubleConsts.EXP_BIT_MASK) {
            magnitude++;
        }
        return Double.longBitsToDouble(magnitude | (Long.MIN_VALUE & bits));
    }

    private static double[] doubleArraySize(double[] array) {
        int oldSize = array.length;
        double[] newArray = new double[(oldSize * 2)];
        System.arraycopy((Object) array, 0, (Object) newArray, 0, oldSize);
        return newArray;
    }

    private String[] doubleArraySize(String[] array) {
        int oldSize = array.length;
        String[] newArray = new String[(oldSize * 2)];
        System.arraycopy((Object) array, 0, (Object) newArray, 0, oldSize);
        return newArray;
    }
}
