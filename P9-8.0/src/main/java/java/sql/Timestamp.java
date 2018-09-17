package java.sql;

import java.time.Year;
import java.util.Date;
import sun.util.locale.LanguageTag;

public class Timestamp extends Date {
    static final long serialVersionUID = 2745179027874758501L;
    private int nanos;

    @Deprecated
    public Timestamp(int year, int month, int date, int hour, int minute, int second, int nano) {
        super(year, month, date, hour, minute, second);
        if (nano > Year.MAX_VALUE || nano < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        this.nanos = nano;
    }

    public Timestamp(long time) {
        super((time / 1000) * 1000);
        this.nanos = (int) ((time % 1000) * 1000000);
        if (this.nanos < 0) {
            this.nanos += 1000000000;
            super.setTime(((time / 1000) - 1) * 1000);
        }
    }

    public void setTime(long time) {
        super.setTime((time / 1000) * 1000);
        this.nanos = (int) ((time % 1000) * 1000000);
        if (this.nanos < 0) {
            this.nanos += 1000000000;
            super.setTime(((time / 1000) - 1) * 1000);
        }
    }

    public long getTime() {
        return ((long) (this.nanos / 1000000)) + super.getTime();
    }

    public static Timestamp valueOf(String s) {
        int year = 0;
        int month = 0;
        int day = 0;
        int a_nanos = 0;
        String formatError = "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]";
        String zeros = "000000000";
        String delimiterDate = LanguageTag.SEP;
        String delimiterTime = ":";
        if (s == null) {
            throw new IllegalArgumentException("null string");
        }
        s = s.trim();
        int dividingSpace = s.indexOf(32);
        if (dividingSpace > 0) {
            String date_s = s.substring(0, dividingSpace);
            String time_s = s.substring(dividingSpace + 1);
            int firstDash = date_s.indexOf(45);
            int secondDash = date_s.indexOf(45, firstDash + 1);
            if (time_s == null) {
                throw new IllegalArgumentException(formatError);
            }
            int firstColon = time_s.indexOf(58);
            int secondColon = time_s.indexOf(58, firstColon + 1);
            int period = time_s.indexOf(46, secondColon + 1);
            boolean parsedDate = false;
            if (firstDash > 0 && secondDash > 0 && secondDash < date_s.length() - 1) {
                String yyyy = date_s.substring(0, firstDash);
                String mm = date_s.substring(firstDash + 1, secondDash);
                String dd = date_s.substring(secondDash + 1);
                if (yyyy.length() == 4 && mm.length() >= 1 && mm.length() <= 2 && dd.length() >= 1 && dd.length() <= 2) {
                    year = Integer.parseInt(yyyy);
                    month = Integer.parseInt(mm);
                    day = Integer.parseInt(dd);
                    if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                        parsedDate = true;
                    }
                }
            }
            if (parsedDate) {
                if (((secondColon < time_s.length() + -1 ? 1 : 0) & ((firstColon > 0 ? 1 : 0) & (secondColon > 0 ? 1 : 0))) != 0) {
                    int second;
                    int hour = Integer.parseInt(time_s.substring(0, firstColon));
                    int minute = Integer.parseInt(time_s.substring(firstColon + 1, secondColon));
                    if (((period > 0 ? 1 : 0) & (period < time_s.length() + -1 ? 1 : 0)) != 0) {
                        second = Integer.parseInt(time_s.substring(secondColon + 1, period));
                        String nanos_s = time_s.substring(period + 1);
                        if (nanos_s.length() > 9) {
                            throw new IllegalArgumentException(formatError);
                        } else if (Character.isDigit(nanos_s.charAt(0))) {
                            a_nanos = Integer.parseInt(nanos_s + zeros.substring(0, 9 - nanos_s.length()));
                        } else {
                            throw new IllegalArgumentException(formatError);
                        }
                    } else if (period > 0) {
                        throw new IllegalArgumentException(formatError);
                    } else {
                        second = Integer.parseInt(time_s.substring(secondColon + 1));
                    }
                    return new Timestamp(year - 1900, month - 1, day, hour, minute, second, a_nanos);
                }
                throw new IllegalArgumentException(formatError);
            }
            throw new IllegalArgumentException(formatError);
        }
        throw new IllegalArgumentException(formatError);
    }

    public String toString() {
        String yearString;
        String monthString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;
        String nanosString;
        int year = super.getYear() + 1900;
        int month = super.getMonth() + 1;
        int day = super.getDate();
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
        String zeros = "000000000";
        String yearZeros = "0000";
        if (year < 1000) {
            yearString = "" + year;
            yearString = yearZeros.substring(0, 4 - yearString.length()) + yearString;
        } else {
            yearString = "" + year;
        }
        if (month < 10) {
            monthString = "0" + month;
        } else {
            monthString = Integer.toString(month);
        }
        if (day < 10) {
            dayString = "0" + day;
        } else {
            dayString = Integer.toString(day);
        }
        if (hour < 10) {
            hourString = "0" + hour;
        } else {
            hourString = Integer.toString(hour);
        }
        if (minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = Integer.toString(minute);
        }
        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = Integer.toString(second);
        }
        if (this.nanos == 0) {
            nanosString = "0";
        } else {
            nanosString = Integer.toString(this.nanos);
            nanosString = zeros.substring(0, 9 - nanosString.length()) + nanosString;
            char[] nanosChar = new char[nanosString.length()];
            nanosString.getChars(0, nanosString.length(), nanosChar, 0);
            int truncIndex = 8;
            while (nanosChar[truncIndex] == '0') {
                truncIndex--;
            }
            nanosString = new String(nanosChar, 0, truncIndex + 1);
        }
        StringBuffer timestampBuf = new StringBuffer(nanosString.length() + 20);
        timestampBuf.append(yearString);
        timestampBuf.append(LanguageTag.SEP);
        timestampBuf.append(monthString);
        timestampBuf.append(LanguageTag.SEP);
        timestampBuf.append(dayString);
        timestampBuf.append(" ");
        timestampBuf.append(hourString);
        timestampBuf.append(":");
        timestampBuf.append(minuteString);
        timestampBuf.append(":");
        timestampBuf.append(secondString);
        timestampBuf.append(".");
        timestampBuf.append(nanosString);
        return timestampBuf.toString();
    }

    public int getNanos() {
        return this.nanos;
    }

    public void setNanos(int n) {
        if (n > Year.MAX_VALUE || n < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        this.nanos = n;
    }

    public boolean equals(Timestamp ts) {
        if (super.equals(ts) && this.nanos == ts.nanos) {
            return true;
        }
        return false;
    }

    public boolean equals(Object ts) {
        if (ts instanceof Timestamp) {
            return equals((Timestamp) ts);
        }
        return false;
    }

    public boolean before(Timestamp ts) {
        return compareTo(ts) < 0;
    }

    public boolean after(Timestamp ts) {
        return compareTo(ts) > 0;
    }

    public int compareTo(Timestamp ts) {
        long thisTime = getTime();
        long anotherTime = ts.getTime();
        int i = thisTime < anotherTime ? -1 : thisTime == anotherTime ? 0 : 1;
        if (i == 0) {
            if (this.nanos > ts.nanos) {
                return 1;
            }
            if (this.nanos < ts.nanos) {
                return -1;
            }
        }
        return i;
    }

    public int compareTo(Date o) {
        if (o instanceof Timestamp) {
            return compareTo((Timestamp) o);
        }
        return compareTo(new Timestamp(o.getTime()));
    }

    public int hashCode() {
        return super.hashCode();
    }
}
