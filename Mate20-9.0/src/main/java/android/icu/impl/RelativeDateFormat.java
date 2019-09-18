package android.icu.impl;

import android.icu.impl.UResource;
import android.icu.impl.coll.CollationSettings;
import android.icu.lang.UCharacter;
import android.icu.text.BreakIterator;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.icu.text.MessageFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;

public class RelativeDateFormat extends DateFormat {
    private static final long serialVersionUID = 1131984966440549435L;
    private transient BreakIterator capitalizationBrkIter = null;
    private boolean capitalizationInfoIsSet = false;
    private boolean capitalizationOfRelativeUnitsForListOrMenu = false;
    private boolean capitalizationOfRelativeUnitsForStandAlone = false;
    private boolean combinedFormatHasDateAtStart = false;
    private MessageFormat fCombinedFormat;
    private DateFormat fDateFormat;
    private String fDatePattern = null;
    int fDateStyle;
    private SimpleDateFormat fDateTimeFormat = null;
    /* access modifiers changed from: private */
    public transient List<URelativeString> fDates = null;
    ULocale fLocale;
    private DateFormat fTimeFormat;
    private String fTimePattern = null;
    int fTimeStyle;

    private final class RelDateFmtDataSink extends UResource.Sink {
        private RelDateFmtDataSink() {
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            if (value.getType() != 3) {
                UResource.Table table = value.getTable();
                int i = 0;
                while (table.getKeyAndValue(i, key, value)) {
                    try {
                        int keyOffset = Integer.parseInt(key.toString());
                        if (RelativeDateFormat.this.getStringForDay(keyOffset) == null) {
                            RelativeDateFormat.this.fDates.add(new URelativeString(keyOffset, value.getString()));
                        }
                        i++;
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
            }
        }
    }

    public static class URelativeString {
        public int offset;
        public String string;

        URelativeString(int offset2, String string2) {
            this.offset = offset2;
            this.string = string2;
        }

        URelativeString(String offset2, String string2) {
            this.offset = Integer.parseInt(offset2);
            this.string = string2;
        }
    }

    public RelativeDateFormat(int timeStyle, int dateStyle, ULocale locale, Calendar cal) {
        this.calendar = cal;
        this.fLocale = locale;
        this.fTimeStyle = timeStyle;
        this.fDateStyle = dateStyle;
        if (this.fDateStyle != -1) {
            DateFormat df = DateFormat.getDateInstance(this.fDateStyle & -129, locale);
            if (df instanceof SimpleDateFormat) {
                this.fDateTimeFormat = (SimpleDateFormat) df;
                this.fDatePattern = this.fDateTimeFormat.toPattern();
                if (this.fTimeStyle != -1) {
                    DateFormat df2 = DateFormat.getTimeInstance(this.fTimeStyle & -129, locale);
                    if (df2 instanceof SimpleDateFormat) {
                        this.fTimePattern = ((SimpleDateFormat) df2).toPattern();
                    }
                }
            } else {
                throw new IllegalArgumentException("Can't create SimpleDateFormat for date style");
            }
        } else {
            DateFormat df3 = DateFormat.getTimeInstance(this.fTimeStyle & -129, locale);
            if (df3 instanceof SimpleDateFormat) {
                this.fDateTimeFormat = (SimpleDateFormat) df3;
                this.fTimePattern = this.fDateTimeFormat.toPattern();
            } else {
                throw new IllegalArgumentException("Can't create SimpleDateFormat for time style");
            }
        }
        initializeCalendar(null, this.fLocale);
        loadDates();
        initializeCombinedFormat(this.calendar, this.fLocale);
    }

    public StringBuffer format(Calendar cal, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        String relativeDayString = null;
        DisplayContext capitalizationContext = getContext(DisplayContext.Type.CAPITALIZATION);
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
            StringBuffer combinedPattern = new StringBuffer("");
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

    /* access modifiers changed from: private */
    public String getStringForDay(int day) {
        if (this.fDates == null) {
            loadDates();
        }
        for (URelativeString dayItem : this.fDates) {
            if (dayItem.offset == day) {
                return dayItem.string;
            }
        }
        return null;
    }

    private synchronized void loadDates() {
        this.fDates = new ArrayList();
        ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, this.fLocale)).getAllItemsWithFallback("fields/day/relative", new RelDateFmtDataSink());
    }

    private void initCapitalizationContextInfo(ULocale locale) {
        try {
            int[] intVector = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale)).getWithFallback("contextTransforms/relative").getIntVector();
            if (intVector.length >= 2) {
                boolean z = false;
                this.capitalizationOfRelativeUnitsForListOrMenu = intVector[0] != 0;
                if (intVector[1] != 0) {
                    z = true;
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
        String pattern;
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
        ICUResourceBundle patternsRb = rb.findWithFallback("calendar/" + cal.getType() + "/DateTimePatterns");
        if (patternsRb == null && !cal.getType().equals("gregorian")) {
            patternsRb = rb.findWithFallback("calendar/gregorian/DateTimePatterns");
        }
        if (patternsRb == null || patternsRb.getSize() < 9) {
            pattern = "{1} {0}";
        } else {
            int glueIndex = 8;
            if (patternsRb.getSize() >= 13) {
                if (this.fDateStyle >= 0 && this.fDateStyle <= 3) {
                    glueIndex = 8 + this.fDateStyle + 1;
                } else if (this.fDateStyle >= 128 && this.fDateStyle <= 131) {
                    glueIndex = 8 + ((this.fDateStyle + 1) - 128);
                }
            }
            if (patternsRb.get(glueIndex).getType() == 8) {
                pattern = patternsRb.get(glueIndex).getString(0);
            } else {
                pattern = patternsRb.getString(glueIndex);
            }
        }
        String pattern2 = pattern;
        this.combinedFormatHasDateAtStart = pattern2.startsWith("{1}");
        this.fCombinedFormat = new MessageFormat(pattern2, locale);
        return this.fCombinedFormat;
    }
}
