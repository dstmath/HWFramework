package sun.util.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import sun.util.calendar.BaseCalendar;

public class LocalGregorianCalendar extends BaseCalendar {
    private Era[] eras;
    private String name;

    public static class Date extends BaseCalendar.Date {
        private int gregorianYear = Integer.MIN_VALUE;

        protected Date() {
        }

        protected Date(TimeZone zone) {
            super(zone);
        }

        public Date setEra(Era era) {
            if (getEra() != era) {
                super.setEra(era);
                this.gregorianYear = Integer.MIN_VALUE;
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
                this.gregorianYear = Integer.MIN_VALUE;
            }
            return this;
        }

        public int getNormalizedYear() {
            return this.gregorianYear;
        }

        public void setNormalizedYear(int normalizedYear) {
            this.gregorianYear = normalizedYear;
        }

        /* access modifiers changed from: package-private */
        public void setLocalEra(Era era) {
            super.setEra(era);
        }

        /* access modifiers changed from: package-private */
        public void setLocalYear(int year) {
            super.setYear(year);
        }

        public String toString() {
            String time = super.toString();
            String time2 = time.substring(time.indexOf(84));
            StringBuffer sb = new StringBuffer();
            Era era = getEra();
            if (era != null) {
                String abbr = era.getAbbreviation();
                if (abbr != null) {
                    sb.append(abbr);
                }
            }
            sb.append(getYear());
            sb.append('.');
            CalendarUtils.sprintf0d(sb, getMonth(), 2).append('.');
            CalendarUtils.sprintf0d(sb, getDayOfMonth(), 2);
            sb.append(time2);
            return sb.toString();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: sun.util.calendar.LocalGregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: sun.util.calendar.LocalGregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: sun.util.calendar.LocalGregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v6, resolved type: sun.util.calendar.LocalGregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v8, resolved type: sun.util.calendar.LocalGregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v7, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    static LocalGregorianCalendar getLocalGregorianCalendar(String name2) {
        String abbr;
        String str = name2;
        try {
            Properties calendarProps = CalendarSystem.getCalendarProperties();
            String props = calendarProps.getProperty("calendar." + str + ".eras");
            LocalGregorianCalendar localGregorianCalendar = null;
            if (props == null) {
                return null;
            }
            List<Era> eras2 = new ArrayList<>();
            StringTokenizer eraTokens = new StringTokenizer(props, ";");
            while (eraTokens.hasMoreTokens()) {
                StringTokenizer itemTokens = new StringTokenizer(eraTokens.nextToken().trim(), ",");
                String eraName = null;
                boolean localTime = true;
                long since = 0;
                String abbr2 = localGregorianCalendar;
                while (itemTokens.hasMoreTokens()) {
                    String item = itemTokens.nextToken();
                    int index = item.indexOf(61);
                    if (index == -1) {
                        return localGregorianCalendar;
                    }
                    String key = item.substring(0, index);
                    String value = item.substring(index + 1);
                    Properties calendarProps2 = calendarProps;
                    if ("name".equals(key)) {
                        eraName = value;
                        abbr = abbr2;
                    } else if ("since".equals(key)) {
                        if (value.endsWith("u")) {
                            since = Long.parseLong(value.substring(0, value.length() - 1));
                            localTime = false;
                            abbr = abbr2;
                        } else {
                            since = Long.parseLong(value);
                            abbr = abbr2;
                        }
                    } else if ("abbr".equals(key)) {
                        abbr = value;
                    } else {
                        String str2 = props;
                        StringBuilder sb = new StringBuilder();
                        StringTokenizer stringTokenizer = eraTokens;
                        sb.append("Unknown key word: ");
                        sb.append(key);
                        throw new RuntimeException(sb.toString());
                    }
                    calendarProps = calendarProps2;
                    localGregorianCalendar = null;
                    abbr2 = abbr;
                }
                String str3 = props;
                StringTokenizer stringTokenizer2 = eraTokens;
                Era era = new Era(eraName, abbr2, since, localTime);
                eras2.add(era);
                calendarProps = calendarProps;
                localGregorianCalendar = null;
            }
            String str4 = props;
            StringTokenizer stringTokenizer3 = eraTokens;
            if (!eras2.isEmpty()) {
                Era[] eraArray = new Era[eras2.size()];
                eras2.toArray(eraArray);
                return new LocalGregorianCalendar(str, eraArray);
            }
            throw new RuntimeException("No eras for " + str);
        } catch (IOException | IllegalArgumentException e) {
            Exception exc = e;
            throw new InternalError((Throwable) e);
        }
    }

    private LocalGregorianCalendar(String name2, Era[] eras2) {
        this.name = name2;
        this.eras = eras2;
        setEras(eras2);
    }

    public String getName() {
        return this.name;
    }

    public Date getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), (CalendarDate) newCalendarDate());
    }

    public Date getCalendarDate(long millis) {
        return getCalendarDate(millis, (CalendarDate) newCalendarDate());
    }

    public Date getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, (CalendarDate) newCalendarDate(zone));
    }

    public Date getCalendarDate(long millis, CalendarDate date) {
        Date ldate = (Date) super.getCalendarDate(millis, date);
        return adjustYear(ldate, millis, ldate.getZoneOffset());
    }

    private Date adjustYear(Date ldate, long millis, int zoneOffset) {
        int i = this.eras.length - 1;
        while (true) {
            if (i < 0) {
                break;
            }
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
        if (era != null) {
            if (!validateEra(era)) {
                return false;
            }
            ldate.setNormalizedYear((era.getSinceDate().getYear() + ldate.getYear()) - 1);
            Date tmp = newCalendarDate(date.getZone());
            tmp.setEra(era).setDate(date.getYear(), date.getMonth(), date.getDayOfMonth());
            normalize(tmp);
            if (tmp.getEra() != era) {
                return false;
            }
        } else if (date.getYear() >= this.eras[0].getSinceDate().getYear()) {
            return false;
        } else {
            ldate.setNormalizedYear(ldate.getYear());
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
        while (true) {
            if (i < 0) {
                break;
            }
            era = this.eras[i];
            if (era.isLocalTime()) {
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
            } else {
                if (!hasMillis) {
                    millis = super.getTime(date);
                    hasMillis = true;
                }
                if (millis >= era.getSince(date.getZone())) {
                    break;
                }
            }
            i--;
        }
        if (i >= 0) {
            ldate.setLocalEra(era);
            ldate.setLocalYear((ldate.getNormalizedYear() - era.getSinceDate().getYear()) + 1);
        } else {
            ldate.setEra((Era) null);
            ldate.setLocalYear(year);
            ldate.setNormalizedYear(year);
        }
        ldate.setNormalized(true);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void normalizeMonth(CalendarDate date) {
        normalizeYear(date);
        super.normalizeMonth(date);
    }

    /* access modifiers changed from: package-private */
    public void normalizeYear(CalendarDate date) {
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
