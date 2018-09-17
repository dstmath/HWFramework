package java.sql;

import java.util.Date;

public class Time extends Date {
    static final long serialVersionUID = 8397324403548013681L;

    @Deprecated
    public Time(int hour, int minute, int second) {
        super(70, 0, 1, hour, minute, second);
    }

    public Time(long time) {
        super(time);
    }

    public void setTime(long time) {
        super.setTime(time);
    }

    public static Time valueOf(String s) {
        int i = 1;
        if (s == null) {
            throw new IllegalArgumentException();
        }
        int i2;
        int firstColon = s.indexOf(58);
        int secondColon = s.indexOf(58, firstColon + 1);
        if (firstColon > 0) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        int i3 = (secondColon > 0 ? 1 : 0) & i2;
        if (secondColon >= s.length() - 1) {
            i = 0;
        }
        if ((i3 & i) != 0) {
            return new Time(Integer.parseInt(s.substring(0, firstColon)), Integer.parseInt(s.substring(firstColon + 1, secondColon)), Integer.parseInt(s.substring(secondColon + 1)));
        }
        throw new IllegalArgumentException();
    }

    public String toString() {
        String hourString;
        String minuteString;
        String secondString;
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
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
        return hourString + ":" + minuteString + ":" + secondString;
    }

    @Deprecated
    public int getYear() {
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getMonth() {
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getDay() {
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getDate() {
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setYear(int i) {
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setMonth(int i) {
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setDate(int i) {
        throw new IllegalArgumentException();
    }
}
