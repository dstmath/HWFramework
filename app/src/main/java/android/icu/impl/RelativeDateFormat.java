package android.icu.impl;

import android.icu.impl.coll.CollationSettings;
import android.icu.lang.UCharacter;
import android.icu.text.BreakIterator;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.icu.text.DisplayContext.Type;
import android.icu.text.MessageFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import dalvik.bytecode.Opcodes;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Comparator;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public class RelativeDateFormat extends DateFormat {
    private static final long serialVersionUID = 1131984966440549435L;
    private transient BreakIterator capitalizationBrkIter;
    private boolean capitalizationInfoIsSet;
    private boolean capitalizationOfRelativeUnitsForListOrMenu;
    private boolean capitalizationOfRelativeUnitsForStandAlone;
    private boolean combinedFormatHasDateAtStart;
    private MessageFormat fCombinedFormat;
    private DateFormat fDateFormat;
    private String fDatePattern;
    int fDateStyle;
    private SimpleDateFormat fDateTimeFormat;
    private transient URelativeString[] fDates;
    ULocale fLocale;
    private DateFormat fTimeFormat;
    private String fTimePattern;
    int fTimeStyle;

    public static class URelativeString {
        public int offset;
        public String string;

        URelativeString(int offset, String string) {
            this.offset = offset;
            this.string = string;
        }

        URelativeString(String offset, String string) {
            this.offset = Integer.parseInt(offset);
            this.string = string;
        }
    }

    public RelativeDateFormat(int timeStyle, int dateStyle, ULocale locale, Calendar cal) {
        this.fDateTimeFormat = null;
        this.fDatePattern = null;
        this.fTimePattern = null;
        this.fDates = null;
        this.combinedFormatHasDateAtStart = false;
        this.capitalizationInfoIsSet = false;
        this.capitalizationOfRelativeUnitsForListOrMenu = false;
        this.capitalizationOfRelativeUnitsForStandAlone = false;
        this.capitalizationBrkIter = null;
        this.calendar = cal;
        this.fLocale = locale;
        this.fTimeStyle = timeStyle;
        this.fDateStyle = dateStyle;
        DateFormat df;
        if (this.fDateStyle != -1) {
            df = DateFormat.getDateInstance(this.fDateStyle & -129, locale);
            if (df instanceof SimpleDateFormat) {
                this.fDateTimeFormat = (SimpleDateFormat) df;
                this.fDatePattern = this.fDateTimeFormat.toPattern();
                if (this.fTimeStyle != -1) {
                    df = DateFormat.getTimeInstance(this.fTimeStyle & -129, locale);
                    if (df instanceof SimpleDateFormat) {
                        this.fTimePattern = ((SimpleDateFormat) df).toPattern();
                    }
                }
            } else {
                throw new IllegalArgumentException("Can't create SimpleDateFormat for date style");
            }
        }
        df = DateFormat.getTimeInstance(this.fTimeStyle & -129, locale);
        if (df instanceof SimpleDateFormat) {
            this.fDateTimeFormat = (SimpleDateFormat) df;
            this.fTimePattern = this.fDateTimeFormat.toPattern();
        } else {
            throw new IllegalArgumentException("Can't create SimpleDateFormat for time style");
        }
        initializeCalendar(null, this.fLocale);
        loadDates();
        initializeCombinedFormat(this.calendar, this.fLocale);
    }

    public StringBuffer format(Calendar cal, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        String relativeDayString = null;
        DisplayContext capitalizationContext = getContext(Type.CAPITALIZATION);
        if (this.fDateStyle != -1) {
            relativeDayString = getStringForDay(dayDifference(cal));
        }
        if (this.fDateTimeFormat != null) {
            if (relativeDayString == null || this.fDatePattern == null || !(this.fTimePattern == null || this.fCombinedFormat == null || this.combinedFormatHasDateAtStart)) {
                this.fDateTimeFormat.setContext(capitalizationContext);
            } else {
                if (relativeDayString.length() > 0 && UCharacter.isLowerCase(relativeDayString.codePointAt(0)) && (capitalizationContext == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || ((capitalizationContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && this.capitalizationOfRelativeUnitsForListOrMenu) || (capitalizationContext == DisplayContext.CAPITALIZATION_FOR_STANDALONE && this.capitalizationOfRelativeUnitsForStandAlone)))) {
                    if (this.capitalizationBrkIter == null) {
                        this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.fLocale);
                    }
                    relativeDayString = UCharacter.toTitleCase(this.fLocale, relativeDayString, this.capitalizationBrkIter, (int) CollationSettings.CASE_FIRST_AND_UPPER_MASK);
                }
                this.fDateTimeFormat.setContext(DisplayContext.CAPITALIZATION_NONE);
            }
        }
        if (this.fDateTimeFormat == null || (this.fDatePattern == null && this.fTimePattern == null)) {
            if (this.fDateFormat != null) {
                if (relativeDayString != null) {
                    toAppendTo.append(relativeDayString);
                } else {
                    this.fDateFormat.format(cal, toAppendTo, fieldPosition);
                }
            }
        } else if (this.fDatePattern == null) {
            this.fDateTimeFormat.applyPattern(this.fTimePattern);
            this.fDateTimeFormat.format(cal, toAppendTo, fieldPosition);
        } else if (this.fTimePattern != null) {
            String datePattern = this.fDatePattern;
            if (relativeDayString != null) {
                datePattern = "'" + relativeDayString.replace("'", "''") + "'";
            }
            StringBuffer combinedPattern = new StringBuffer(XmlPullParser.NO_NAMESPACE);
            this.fCombinedFormat.format(new Object[]{this.fTimePattern, datePattern}, combinedPattern, new FieldPosition(0));
            this.fDateTimeFormat.applyPattern(combinedPattern.toString());
            this.fDateTimeFormat.format(cal, toAppendTo, fieldPosition);
        } else if (relativeDayString != null) {
            toAppendTo.append(relativeDayString);
        } else {
            this.fDateTimeFormat.applyPattern(this.fDatePattern);
            this.fDateTimeFormat.format(cal, toAppendTo, fieldPosition);
        }
        return toAppendTo;
    }

    public void parse(String text, Calendar cal, ParsePosition pos) {
        throw new UnsupportedOperationException("Relative Date parse is not implemented yet");
    }

    public void setContext(DisplayContext context) {
        super.setContext(context);
        if (!this.capitalizationInfoIsSet && (context == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || context == DisplayContext.CAPITALIZATION_FOR_STANDALONE)) {
            initCapitalizationContextInfo(this.fLocale);
            this.capitalizationInfoIsSet = true;
        }
        if (this.capitalizationBrkIter != null) {
            return;
        }
        if (context == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || ((context == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && this.capitalizationOfRelativeUnitsForListOrMenu) || (context == DisplayContext.CAPITALIZATION_FOR_STANDALONE && this.capitalizationOfRelativeUnitsForStandAlone))) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.fLocale);
        }
    }

    private String getStringForDay(int day) {
        if (this.fDates == null) {
            loadDates();
        }
        for (int i = 0; i < this.fDates.length; i++) {
            if (this.fDates[i].offset == day) {
                return this.fDates[i].string;
            }
        }
        return null;
    }

    private synchronized void loadDates() {
        ICUResourceBundle rdb = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, this.fLocale)).getWithFallback("fields/day/relative");
        Set<URelativeString> datesSet = new TreeSet(new Comparator<URelativeString>() {
            public int compare(URelativeString r1, URelativeString r2) {
                if (r1.offset == r2.offset) {
                    return 0;
                }
                if (r1.offset < r2.offset) {
                    return -1;
                }
                return 1;
            }
        });
        UResourceBundleIterator i = rdb.getIterator();
        while (i.hasNext()) {
            UResourceBundle line = i.next();
            datesSet.add(new URelativeString(line.getKey(), line.getString()));
        }
        this.fDates = (URelativeString[]) datesSet.toArray(new URelativeString[0]);
    }

    private void initCapitalizationContextInfo(ULocale locale) {
        boolean z = true;
        try {
            int[] intVector = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale)).getWithFallback("contextTransforms/relative").getIntVector();
            if (intVector.length >= 2) {
                boolean z2;
                if (intVector[0] != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.capitalizationOfRelativeUnitsForListOrMenu = z2;
                if (intVector[1] == 0) {
                    z = false;
                }
                this.capitalizationOfRelativeUnitsForStandAlone = z;
            }
        } catch (MissingResourceException e) {
        }
    }

    private static int dayDifference(Calendar until) {
        Calendar nowCal = (Calendar) until.clone();
        Date nowDate = new Date(System.currentTimeMillis());
        nowCal.clear();
        nowCal.setTime(nowDate);
        return until.get(20) - nowCal.get(20);
    }

    private Calendar initializeCalendar(TimeZone zone, ULocale locale) {
        if (this.calendar == null) {
            if (zone == null) {
                this.calendar = Calendar.getInstance(locale);
            } else {
                this.calendar = Calendar.getInstance(zone, locale);
            }
        }
        return this.calendar;
    }

    private MessageFormat initializeCombinedFormat(Calendar cal, ULocale locale) {
        String pattern = "{1} {0}";
        try {
            String[] patterns = new CalendarData(locale, cal.getType()).getDateTimePatterns();
            if (patterns != null && patterns.length >= 9) {
                int glueIndex = 8;
                if (patterns.length >= 13) {
                    switch (this.fDateStyle) {
                        case XmlPullParser.START_DOCUMENT /*0*/:
                        case NodeFilter.SHOW_COMMENT /*128*/:
                            glueIndex = 9;
                            break;
                        case NodeFilter.SHOW_ELEMENT /*1*/:
                        case Opcodes.OP_INT_TO_LONG /*129*/:
                            glueIndex = 10;
                            break;
                        case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                        case Opcodes.OP_INT_TO_FLOAT /*130*/:
                            glueIndex = 11;
                            break;
                        case XmlPullParser.END_TAG /*3*/:
                        case Opcodes.OP_INT_TO_DOUBLE /*131*/:
                            glueIndex = 12;
                            break;
                    }
                }
                pattern = patterns[glueIndex];
            }
        } catch (MissingResourceException e) {
        }
        this.combinedFormatHasDateAtStart = pattern.startsWith("{1}");
        this.fCombinedFormat = new MessageFormat(pattern, locale);
        return this.fCombinedFormat;
    }
}
