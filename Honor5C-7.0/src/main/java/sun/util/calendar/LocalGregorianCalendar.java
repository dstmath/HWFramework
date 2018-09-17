package sun.util.calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class LocalGregorianCalendar extends BaseCalendar {
    private Era[] eras;
    private String name;

    /* renamed from: sun.util.calendar.LocalGregorianCalendar.1 */
    static class AnonymousClass1 implements PrivilegedExceptionAction {
        final /* synthetic */ String val$fname;

        AnonymousClass1(String val$fname) {
            this.val$fname = val$fname;
        }

        public Object run() throws IOException {
            Throwable th;
            InputStream inputStream;
            Throwable th2 = null;
            Properties props = new Properties();
            FileInputStream fileInputStream = null;
            try {
                InputStream fis = new FileInputStream(this.val$fname);
                try {
                    props.load(fis);
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 == null) {
                        return props;
                    }
                    throw th2;
                } catch (Throwable th4) {
                    th = th4;
                    inputStream = fis;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        throw th2;
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (th2 == null) {
                    throw th;
                }
                throw th2;
            }
        }
    }

    public static class Date extends sun.util.calendar.BaseCalendar.Date {
        private int gregorianYear;

        protected Date() {
            this.gregorianYear = PlatformLogger.ALL;
        }

        protected Date(TimeZone zone) {
            super(zone);
            this.gregorianYear = PlatformLogger.ALL;
        }

        public Date setEra(Era era) {
            if (getEra() != era) {
                super.setEra(era);
                this.gregorianYear = PlatformLogger.ALL;
            }
            return this;
        }

        public Date addYear(int localYear) {
            super.addYear(localYear);
            this.gregorianYear += localYear;
            return this;
        }

        public Date setYear(int localYear) {
            if (getYear() != localYear) {
                super.setYear(localYear);
                this.gregorianYear = PlatformLogger.ALL;
            }
            return this;
        }

        public int getNormalizedYear() {
            return this.gregorianYear;
        }

        public void setNormalizedYear(int normalizedYear) {
            this.gregorianYear = normalizedYear;
        }

        void setLocalEra(Era era) {
            super.setEra(era);
        }

        void setLocalYear(int year) {
            super.setYear(year);
        }

        public String toString() {
            String time = super.toString();
            time = time.substring(time.indexOf(84));
            StringBuffer sb = new StringBuffer();
            Era era = getEra();
            if (era != null) {
                String abbr = era.getAbbreviation();
                if (abbr != null) {
                    sb.append(abbr);
                }
            }
            sb.append(getYear()).append('.');
            CalendarUtils.sprintf0d(sb, getMonth(), 2).append('.');
            CalendarUtils.sprintf0d(sb, getDayOfMonth(), 2);
            sb.append(time);
            return sb.toString();
        }
    }

    static LocalGregorianCalendar getLocalGregorianCalendar(String name) {
        try {
            String homeDir = (String) AccessController.doPrivileged(new GetPropertyAction("java.home"));
            Properties calendarProps = (Properties) AccessController.doPrivileged(new AnonymousClass1(homeDir + File.separator + "lib" + File.separator + "calendars.properties"));
            String props = calendarProps.getProperty("calendar." + name + ".eras");
            if (props == null) {
                return null;
            }
            List<Era> eras = new ArrayList();
            StringTokenizer eraTokens = new StringTokenizer(props, ";");
            while (eraTokens.hasMoreTokens()) {
                StringTokenizer stringTokenizer = new StringTokenizer(eraTokens.nextToken().trim(), ",");
                String eraName = null;
                boolean localTime = true;
                long since = 0;
                String str = null;
                while (stringTokenizer.hasMoreTokens()) {
                    String item = stringTokenizer.nextToken();
                    int index = item.indexOf(61);
                    if (index == -1) {
                        return null;
                    }
                    String key = item.substring(0, index);
                    String value = item.substring(index + 1);
                    if ("name".equals(key)) {
                        eraName = value;
                    } else {
                        if (!"since".equals(key)) {
                            if ("abbr".equals(key)) {
                                str = value;
                            } else {
                                throw new RuntimeException("Unknown key word: " + key);
                            }
                        } else if (value.endsWith("u")) {
                            localTime = false;
                            since = Long.parseLong(value.substring(0, value.length() - 1));
                        } else {
                            since = Long.parseLong(value);
                        }
                    }
                }
                eras.add(new Era(eraName, str, since, localTime));
            }
            Era[] eraArray = new Era[eras.size()];
            eras.toArray(eraArray);
            return new LocalGregorianCalendar(name, eraArray);
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e.getException());
        }
    }

    private LocalGregorianCalendar(String name, Era[] eras) {
        this.name = name;
        this.eras = eras;
        setEras(eras);
    }

    public String getName() {
        return this.name;
    }

    public Date getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
    }

    public Date getCalendarDate(long millis) {
        return getCalendarDate(millis, newCalendarDate());
    }

    public Date getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, newCalendarDate(zone));
    }

    public Date getCalendarDate(long millis, CalendarDate date) {
        Date ldate = (Date) super.getCalendarDate(millis, date);
        return adjustYear(ldate, millis, ldate.getZoneOffset());
    }

    private Date adjustYear(Date ldate, long millis, int zoneOffset) {
        int i = this.eras.length - 1;
        while (i >= 0) {
            Era era = this.eras[i];
            long since = era.getSince(null);
            if (era.isLocalTime()) {
                since -= (long) zoneOffset;
            }
            if (millis >= since) {
                ldate.setLocalEra(era);
                ldate.setLocalYear((ldate.getNormalizedYear() - era.getSinceDate().getYear()) + 1);
                break;
            }
            i--;
        }
        if (i < 0) {
            ldate.setLocalEra(null);
            ldate.setLocalYear(ldate.getNormalizedYear());
        }
        ldate.setNormalized(true);
        return ldate;
    }

    public Date newCalendarDate() {
        return new Date();
    }

    public Date newCalendarDate(TimeZone zone) {
        return new Date(zone);
    }

    public boolean validate(CalendarDate date) {
        Date ldate = (Date) date;
        Era era = ldate.getEra();
        if (era == null) {
            ldate.setNormalizedYear(ldate.getYear());
        } else if (!validateEra(era)) {
            return false;
        } else {
            ldate.setNormalizedYear(era.getSinceDate().getYear() + ldate.getYear());
        }
        return super.validate(ldate);
    }

    private boolean validateEra(Era era) {
        for (Era era2 : this.eras) {
            if (era == era2) {
                return true;
            }
        }
        return false;
    }

    public boolean normalize(CalendarDate date) {
        if (date.isNormalized()) {
            return true;
        }
        normalizeYear(date);
        Date ldate = (Date) date;
        super.normalize(ldate);
        boolean hasMillis = false;
        long millis = 0;
        int year = ldate.getNormalizedYear();
        Era era = null;
        int i = this.eras.length - 1;
        while (i >= 0) {
            era = this.eras[i];
            if (!era.isLocalTime()) {
                if (!hasMillis) {
                    millis = super.getTime(date);
                    hasMillis = true;
                }
                if (millis >= era.getSince(date.getZone())) {
                    break;
                }
            } else {
                CalendarDate sinceDate = era.getSinceDate();
                int sinceYear = sinceDate.getYear();
                if (year > sinceYear) {
                    break;
                } else if (year == sinceYear) {
                    int month = ldate.getMonth();
                    int sinceMonth = sinceDate.getMonth();
                    if (month > sinceMonth) {
                        break;
                    } else if (month == sinceMonth) {
                        int day = ldate.getDayOfMonth();
                        int sinceDay = sinceDate.getDayOfMonth();
                        if (day > sinceDay) {
                            break;
                        } else if (day == sinceDay) {
                            if (ldate.getTimeOfDay() < sinceDate.getTimeOfDay()) {
                                i--;
                            }
                        }
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            i--;
        }
        if (i >= 0) {
            ldate.setLocalEra(era);
            ldate.setLocalYear((ldate.getNormalizedYear() - era.getSinceDate().getYear()) + 1);
        } else {
            ldate.setEra(null);
            ldate.setLocalYear(year);
            ldate.setNormalizedYear(year);
        }
        ldate.setNormalized(true);
        return true;
    }

    void normalizeMonth(CalendarDate date) {
        normalizeYear(date);
        super.normalizeMonth(date);
    }

    void normalizeYear(CalendarDate date) {
        Date ldate = (Date) date;
        Era era = ldate.getEra();
        if (era == null || !validateEra(era)) {
            ldate.setNormalizedYear(ldate.getYear());
        } else {
            ldate.setNormalizedYear((era.getSinceDate().getYear() + ldate.getYear()) - 1);
        }
    }

    public boolean isLeapYear(int gregorianYear) {
        return CalendarUtils.isGregorianLeapYear(gregorianYear);
    }

    public boolean isLeapYear(Era era, int year) {
        if (era == null) {
            return isLeapYear(year);
        }
        return isLeapYear((era.getSinceDate().getYear() + year) - 1);
    }

    public void getCalendarDateFromFixedDate(CalendarDate date, long fixedDate) {
        Date ldate = (Date) date;
        super.getCalendarDateFromFixedDate(ldate, fixedDate);
        adjustYear(ldate, (fixedDate - 719163) * 86400000, 0);
    }
}
