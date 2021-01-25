package android.view.textclassifier.intent;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Browser;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.view.textclassifier.Log;
import android.view.textclassifier.TextClassifier;
import com.android.internal.R;
import com.google.android.textclassifier.AnnotatorModel;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class LegacyClassificationIntentFactory implements ClassificationIntentFactory {
    private static final long DEFAULT_EVENT_DURATION = TimeUnit.HOURS.toMillis(1);
    private static final long MIN_EVENT_FUTURE_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private static final String TAG = "LegacyClassificationIntentFactory";

    @Override // android.view.textclassifier.intent.ClassificationIntentFactory
    public List<LabeledIntent> create(Context context, String text, boolean foreignText, Instant referenceTime, AnnotatorModel.ClassificationResult classification) {
        String type;
        List<LabeledIntent> actions;
        if (classification != null) {
            type = classification.getCollection().trim().toLowerCase(Locale.ENGLISH);
        } else {
            type = "";
        }
        String text2 = text.trim();
        char c = 65535;
        switch (type.hashCode()) {
            case -1271823248:
                if (type.equals(TextClassifier.TYPE_FLIGHT_NUMBER)) {
                    c = 6;
                    break;
                }
                break;
            case -1147692044:
                if (type.equals("address")) {
                    c = 2;
                    break;
                }
                break;
            case 116079:
                if (type.equals("url")) {
                    c = 3;
                    break;
                }
                break;
            case 3076014:
                if (type.equals("date")) {
                    c = 4;
                    break;
                }
                break;
            case 96619420:
                if (type.equals("email")) {
                    c = 0;
                    break;
                }
                break;
            case 106642798:
                if (type.equals("phone")) {
                    c = 1;
                    break;
                }
                break;
            case 447049878:
                if (type.equals(TextClassifier.TYPE_DICTIONARY)) {
                    c = 7;
                    break;
                }
                break;
            case 1793702779:
                if (type.equals(TextClassifier.TYPE_DATE_TIME)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                actions = createForEmail(context, text2);
                break;
            case 1:
                actions = createForPhone(context, text2);
                break;
            case 2:
                actions = createForAddress(context, text2);
                break;
            case 3:
                actions = createForUrl(context, text2);
                break;
            case 4:
            case 5:
                if (classification.getDatetimeResult() == null) {
                    actions = new ArrayList<>();
                    break;
                } else {
                    actions = createForDatetime(context, type, referenceTime, Instant.ofEpochMilli(classification.getDatetimeResult().getTimeMsUtc()));
                    break;
                }
            case 6:
                actions = createForFlight(context, text2);
                break;
            case 7:
                actions = createForDictionary(context, text2);
                break;
            default:
                actions = new ArrayList<>();
                break;
        }
        if (foreignText) {
            ClassificationIntentFactory.insertTranslateAction(actions, context, text2);
        }
        return actions;
    }

    private static List<LabeledIntent> createForEmail(Context context, String text) {
        List<LabeledIntent> actions = new ArrayList<>();
        actions.add(new LabeledIntent(context.getString(R.string.email), null, context.getString(R.string.email_desc), null, new Intent(Intent.ACTION_SENDTO).setData(Uri.parse(String.format("mailto:%s", text))), 0));
        actions.add(new LabeledIntent(context.getString(R.string.add_contact), null, context.getString(R.string.add_contact_desc), null, new Intent(Intent.ACTION_INSERT_OR_EDIT).setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE).putExtra("email", text), text.hashCode()));
        return actions;
    }

    private static List<LabeledIntent> createForPhone(Context context, String text) {
        List<LabeledIntent> actions = new ArrayList<>();
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        Bundle userRestrictions = userManager != null ? userManager.getUserRestrictions() : new Bundle();
        if (!userRestrictions.getBoolean(UserManager.DISALLOW_OUTGOING_CALLS, false)) {
            actions.add(new LabeledIntent(context.getString(R.string.dial), null, context.getString(R.string.dial_desc), null, new Intent(Intent.ACTION_DIAL).setData(Uri.parse(String.format("tel:%s", text))), 0));
        }
        actions.add(new LabeledIntent(context.getString(R.string.add_contact), null, context.getString(R.string.add_contact_desc), null, new Intent(Intent.ACTION_INSERT_OR_EDIT).setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE).putExtra("phone", text), text.hashCode()));
        if (!userRestrictions.getBoolean(UserManager.DISALLOW_SMS, false)) {
            actions.add(new LabeledIntent(context.getString(R.string.sms), null, context.getString(R.string.sms_desc), null, new Intent(Intent.ACTION_SENDTO).setData(Uri.parse(String.format("smsto:%s", text))), 0));
        }
        return actions;
    }

    private static List<LabeledIntent> createForAddress(Context context, String text) {
        List<LabeledIntent> actions = new ArrayList<>();
        try {
            actions.add(new LabeledIntent(context.getString(R.string.map), null, context.getString(R.string.map_desc), null, new Intent("android.intent.action.VIEW").setData(Uri.parse(String.format("geo:0,0?q=%s", URLEncoder.encode(text, "UTF-8")))), 0));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Could not encode address", e);
        }
        return actions;
    }

    private static List<LabeledIntent> createForUrl(Context context, String text) {
        if (Uri.parse(text).getScheme() == null) {
            text = "http://" + text;
        }
        List<LabeledIntent> actions = new ArrayList<>();
        actions.add(new LabeledIntent(context.getString(R.string.browse), null, context.getString(R.string.browse_desc), null, new Intent("android.intent.action.VIEW").setDataAndNormalize(Uri.parse(text)).putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName()), 0));
        return actions;
    }

    private static List<LabeledIntent> createForDatetime(Context context, String type, Instant referenceTime, Instant parsedTime) {
        if (referenceTime == null) {
            referenceTime = Instant.now();
        }
        List<LabeledIntent> actions = new ArrayList<>();
        actions.add(createCalendarViewIntent(context, parsedTime));
        if (referenceTime.until(parsedTime, ChronoUnit.MILLIS) > MIN_EVENT_FUTURE_MILLIS) {
            actions.add(createCalendarCreateEventIntent(context, parsedTime, type));
        }
        return actions;
    }

    private static List<LabeledIntent> createForFlight(Context context, String text) {
        List<LabeledIntent> actions = new ArrayList<>();
        actions.add(new LabeledIntent(context.getString(R.string.view_flight), null, context.getString(R.string.view_flight_desc), null, new Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", text), text.hashCode()));
        return actions;
    }

    private static LabeledIntent createCalendarViewIntent(Context context, Instant parsedTime) {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, parsedTime.toEpochMilli());
        return new LabeledIntent(context.getString(R.string.view_calendar), null, context.getString(R.string.view_calendar_desc), null, new Intent("android.intent.action.VIEW").setData(builder.build()), 0);
    }

    private static LabeledIntent createCalendarCreateEventIntent(Context context, Instant parsedTime, String type) {
        return new LabeledIntent(context.getString(R.string.add_calendar_event), null, context.getString(R.string.add_calendar_event_desc), null, new Intent("android.intent.action.INSERT").setData(CalendarContract.Events.CONTENT_URI).putExtra("allDay", "date".equals(type)).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, parsedTime.toEpochMilli()).putExtra(CalendarContract.EXTRA_EVENT_END_TIME, parsedTime.toEpochMilli() + DEFAULT_EVENT_DURATION), parsedTime.hashCode());
    }

    private static List<LabeledIntent> createForDictionary(Context context, String text) {
        List<LabeledIntent> actions = new ArrayList<>();
        actions.add(new LabeledIntent(context.getString(R.string.define), null, context.getString(R.string.define_desc), null, new Intent(Intent.ACTION_DEFINE).putExtra(Intent.EXTRA_TEXT, text), text.hashCode()));
        return actions;
    }
}
