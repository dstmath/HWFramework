package java.sql;

import java.util.Date;
import sun.util.locale.LanguageTag;

public class Timestamp extends Date {
    static final long serialVersionUID = 2745179027874758501L;
    private int nanos;

    @Deprecated
    public Timestamp(int year, int month, int date, int hour, int minute, int second, int nano) {
        super(year, month, date, hour, minute, second);
        if (nano > 999999999 || nano < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        this.nanos = nano;
    }

    public Timestamp(long time) {
        super((time / 1000) * 1000);
        this.nanos = (int) ((time % 1000) * 1000000);
        if (this.nanos < 0) {
            this.nanos = 1000000000 + this.nanos;
            super.setTime(((time / 1000) - 1) * 1000);
        }
    }

    public void setTime(long time) {
        super.setTime((time / 1000) * 1000);
        this.nanos = (int) ((time % 1000) * 1000000);
        if (this.nanos < 0) {
            this.nanos = 1000000000 + this.nanos;
            super.setTime(((time / 1000) - 1) * 1000);
        }
    }

    public long getTime() {
        return ((long) (this.nanos / 1000000)) + super.getTime();
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01bc  */
    public static Timestamp valueOf(String s) {
        int month;
        int year;
        int second;
        int month2;
        int day = 0;
        if (s != null) {
            String s2 = s.trim();
            int dividingSpace = s2.indexOf(32);
            if (dividingSpace > 0) {
                String date_s = s2.substring(0, dividingSpace);
                String time_s = s2.substring(dividingSpace + 1);
                String str = s2;
                int i = dividingSpace;
                int dividingSpace2 = date_s.indexOf(45);
                int secondDash = date_s.indexOf(45, dividingSpace2 + 1);
                if (time_s != null) {
                    int firstColon = time_s.indexOf(58);
                    int secondColon = time_s.indexOf(58, firstColon + 1);
                    int period = time_s.indexOf(46, secondColon + 1);
                    boolean parsedDate = false;
                    if (dividingSpace2 <= 0 || secondDash <= 0) {
                        int i2 = dividingSpace2;
                        String str2 = date_s;
                        month2 = 0;
                    } else {
                        month2 = 0;
                        if (secondDash < date_s.length() - 1) {
                            String yyyy = date_s.substring(0, dividingSpace2);
                            String mm = date_s.substring(dividingSpace2 + 1, secondDash);
                            int i3 = dividingSpace2;
                            String dd = date_s.substring(secondDash + 1);
                            int i4 = secondDash;
                            String str3 = date_s;
                            if (yyyy.length() == 4 && mm.length() >= 1 && mm.length() <= 2 && dd.length() >= 1 && dd.length() <= 2) {
                                int year2 = Integer.parseInt(yyyy);
                                month = Integer.parseInt(mm);
                                day = Integer.parseInt(dd);
                                int year3 = year2;
                                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                                    parsedDate = true;
                                }
                                year = year3;
                                if (!parsedDate) {
                                    if (((firstColon > 0) & (secondColon > 0)) && (secondColon < time_s.length() - 1)) {
                                        int hour = Integer.parseInt(time_s.substring(0, firstColon));
                                        int minute = Integer.parseInt(time_s.substring(firstColon + 1, secondColon));
                                        boolean z = period > 0;
                                        int a_nanos = 0;
                                        boolean z2 = true;
                                        if (period >= time_s.length() - 1) {
                                            z2 = false;
                                        }
                                        if (z && z2) {
                                            int second2 = Integer.parseInt(time_s.substring(secondColon + 1, period));
                                            String nanos_s = time_s.substring(period + 1);
                                            int second3 = second2;
                                            int i5 = firstColon;
                                            if (nanos_s.length() > 9) {
                                                throw new IllegalArgumentException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
                                            } else if (Character.isDigit(nanos_s.charAt(0))) {
                                                StringBuilder sb = new StringBuilder();
                                                sb.append(nanos_s);
                                                boolean z3 = parsedDate;
                                                sb.append("000000000".substring(0, 9 - nanos_s.length()));
                                                a_nanos = Integer.parseInt(sb.toString());
                                                second = second3;
                                            } else {
                                                throw new IllegalArgumentException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
                                            }
                                        } else {
                                            boolean z4 = parsedDate;
                                            if (period <= 0) {
                                                second = Integer.parseInt(time_s.substring(secondColon + 1));
                                            } else {
                                                throw new IllegalArgumentException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
                                            }
                                        }
                                        Timestamp timestamp = new Timestamp(year - 1900, month - 1, day, hour, minute, second, a_nanos);
                                        return timestamp;
                                    }
                                    int i6 = firstColon;
                                    boolean z5 = parsedDate;
                                    throw new IllegalArgumentException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
                                }
                                int i7 = firstColon;
                                boolean z6 = parsedDate;
                                throw new IllegalArgumentException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
                            }
                        } else {
                            int i8 = dividingSpace2;
                            String str4 = date_s;
                        }
                    }
                    year = 0;
                    month = month2;
                    if (!parsedDate) {
                    }
                } else {
                    int i9 = dividingSpace2;
                    String str5 = date_s;
                    throw new IllegalArgumentException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
                }
            } else {
                int i10 = dividingSpace;
                throw new IllegalArgumentException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
            }
        } else {
            throw new IllegalArgumentException("null string");
        }
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
        if (year < 1000) {
            String yearString2 = "" + year;
            yearString = "0000".substring(0, 4 - yearString2.length()) + yearString2;
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
            int i = year;
            int i2 = month;
        } else {
            String nanosString2 = Integer.toString(this.nanos);
            StringBuilder sb = new StringBuilder();
            int i3 = year;
            int i4 = month;
            sb.append("000000000".substring(0, 9 - nanosString2.length()));
            sb.append(nanosString2);
            String nanosString3 = sb.toString();
            char[] nanosChar = new char[nanosString3.length()];
            nanosString3.getChars(0, nanosString3.length(), nanosChar, 0);
            int truncIndex = 8;
            while (true) {
                String nanosString4 = nanosString3;
                if (nanosChar[truncIndex] != '0') {
                    break;
                }
                truncIndex--;
                nanosString3 = nanosString4;
            }
            int i5 = truncIndex;
            nanosString = new String(nanosChar, 0, truncIndex + 1);
        }
        StringBuffer timestampBuf = new StringBuffer(20 + nanosString.length());
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
        if (n > 999999999 || n < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        this.nanos = n;
    }

    public boolean equals(Timestamp ts) {
        if (!super.equals(ts) || this.nanos != ts.nanos) {
            return false;
        }
        return true;
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
