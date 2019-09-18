package android.text.util;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionPlan;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.util.EventLog;
import android.util.Log;
import android.util.Patterns;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextLinksParams;
import android.webkit.WebView;
import android.widget.TextView;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.internal.util.Preconditions;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.util.EmptyArray;

public class Linkify {
    public static final int ALL = 15;
    public static final int EMAIL_ADDRESSES = 2;
    private static final String LOG_TAG = "Linkify";
    @Deprecated
    public static final int MAP_ADDRESSES = 8;
    public static final int PHONE_NUMBERS = 4;
    private static final int PHONE_NUMBER_MINIMUM_DIGITS = 5;
    public static final int WEB_URLS = 1;
    public static final MatchFilter sPhoneNumberMatchFilter = new MatchFilter() {
        public final boolean acceptMatch(CharSequence s, int start, int end) {
            int digitCount = 0;
            for (int i = start; i < end; i++) {
                if (Character.isDigit(s.charAt(i))) {
                    digitCount++;
                    if (digitCount >= 5) {
                        return true;
                    }
                }
            }
            return false;
        }
    };
    public static final TransformFilter sPhoneNumberTransformFilter = new TransformFilter() {
        public final String transformUrl(Matcher match, String url) {
            return Patterns.digitsAndPlusOnly(match);
        }
    };
    public static final MatchFilter sUrlMatchFilter = new MatchFilter() {
        public final boolean acceptMatch(CharSequence s, int start, int end) {
            if (start != 0 && s.charAt(start - 1) == '@') {
                return false;
            }
            return true;
        }
    };

    @Retention(RetentionPolicy.SOURCE)
    public @interface LinkifyMask {
    }

    public interface MatchFilter {
        boolean acceptMatch(CharSequence charSequence, int i, int i2);
    }

    public interface TransformFilter {
        String transformUrl(Matcher matcher, String str);
    }

    public static final boolean addLinks(Spannable text, int mask) {
        return addLinks(text, mask, (Context) null);
    }

    private static boolean addLinks(Spannable text, int mask, Context context) {
        if (text != null && containsUnsupportedCharacters(text.toString())) {
            EventLog.writeEvent(1397638484, "116321860", -1, "");
            return false;
        } else if (mask == 0) {
            return false;
        } else {
            URLSpan[] old = (URLSpan[]) text.getSpans(0, text.length(), URLSpan.class);
            for (int i = old.length - 1; i >= 0; i--) {
                text.removeSpan(old[i]);
            }
            ArrayList<LinkSpec> links = new ArrayList<>();
            if ((mask & 1) != 0) {
                gatherLinks(links, text, Patterns.getWebUrl(), new String[]{"http://", "https://", "rtsp://"}, sUrlMatchFilter, null);
            }
            if ((mask & 2) != 0) {
                gatherLinks(links, text, Patterns.AUTOLINK_EMAIL_ADDRESS, new String[]{WebView.SCHEME_MAILTO}, null, null);
            }
            if ((mask & 4) != 0) {
                gatherTelLinks(links, text, context);
            }
            if ((mask & 8) != 0) {
                gatherMapLinks(links, text);
            }
            pruneOverlaps(links);
            if (links.size() == 0) {
                return false;
            }
            Iterator<LinkSpec> it = links.iterator();
            while (it.hasNext()) {
                LinkSpec link = it.next();
                applyLink(link.url, link.start, link.end, text);
            }
            return true;
        }
    }

    public static boolean containsUnsupportedCharacters(String text) {
        if (text.contains("‬")) {
            Log.e(LOG_TAG, "Unsupported character for applying links: u202C");
            return true;
        } else if (text.contains("‭")) {
            Log.e(LOG_TAG, "Unsupported character for applying links: u202D");
            return true;
        } else if (!text.contains("‮")) {
            return false;
        } else {
            Log.e(LOG_TAG, "Unsupported character for applying links: u202E");
            return true;
        }
    }

    public static final boolean addLinks(TextView text, int mask) {
        if (mask == 0) {
            return false;
        }
        Context context = text.getContext();
        CharSequence t = text.getText();
        if (!(t instanceof Spannable)) {
            SpannableString s = SpannableString.valueOf(t);
            if (!addLinks((Spannable) s, mask, context)) {
                return false;
            }
            addLinkMovementMethod(text);
            text.setText((CharSequence) s);
            return true;
        } else if (!addLinks((Spannable) t, mask, context)) {
            return false;
        } else {
            addLinkMovementMethod(text);
            return true;
        }
    }

    private static final void addLinkMovementMethod(TextView t) {
        MovementMethod m = t.getMovementMethod();
        if ((m == null || !(m instanceof LinkMovementMethod)) && t.getLinksClickable()) {
            t.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static final void addLinks(TextView text, Pattern pattern, String scheme) {
        addLinks(text, pattern, scheme, (String[]) null, (MatchFilter) null, (TransformFilter) null);
    }

    public static final void addLinks(TextView text, Pattern pattern, String scheme, MatchFilter matchFilter, TransformFilter transformFilter) {
        addLinks(text, pattern, scheme, (String[]) null, matchFilter, transformFilter);
    }

    public static final void addLinks(TextView text, Pattern pattern, String defaultScheme, String[] schemes, MatchFilter matchFilter, TransformFilter transformFilter) {
        SpannableString spannable = SpannableString.valueOf(text.getText());
        if (addLinks((Spannable) spannable, pattern, defaultScheme, schemes, matchFilter, transformFilter)) {
            text.setText((CharSequence) spannable);
            addLinkMovementMethod(text);
        }
    }

    public static final boolean addLinks(Spannable text, Pattern pattern, String scheme) {
        return addLinks(text, pattern, scheme, (String[]) null, (MatchFilter) null, (TransformFilter) null);
    }

    public static final boolean addLinks(Spannable spannable, Pattern pattern, String scheme, MatchFilter matchFilter, TransformFilter transformFilter) {
        return addLinks(spannable, pattern, scheme, (String[]) null, matchFilter, transformFilter);
    }

    public static final boolean addLinks(Spannable spannable, Pattern pattern, String defaultScheme, String[] schemes, MatchFilter matchFilter, TransformFilter transformFilter) {
        if (spannable == null || !containsUnsupportedCharacters(spannable.toString())) {
            if (defaultScheme == null) {
                defaultScheme = "";
            }
            if (schemes == null || schemes.length < 1) {
                schemes = EmptyArray.STRING;
            }
            String[] schemesCopy = new String[(schemes.length + 1)];
            schemesCopy[0] = defaultScheme.toLowerCase(Locale.ROOT);
            for (int index = 0; index < schemes.length; index++) {
                String scheme = schemes[index];
                schemesCopy[index + 1] = scheme == null ? "" : scheme.toLowerCase(Locale.ROOT);
            }
            boolean hasMatches = false;
            Matcher m = pattern.matcher(spannable);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                boolean allowed = true;
                if (matchFilter != null) {
                    allowed = matchFilter.acceptMatch(spannable, start, end);
                }
                if (allowed) {
                    applyLink(makeUrl(m.group(0), schemesCopy, m, transformFilter), start, end, spannable);
                    hasMatches = true;
                }
            }
            return hasMatches;
        }
        EventLog.writeEvent(1397638484, "116321860", -1, "");
        return false;
    }

    public static Future<Void> addLinksAsync(TextView textView, TextLinksParams params) {
        return addLinksAsync(textView, params, null, null);
    }

    public static Future<Void> addLinksAsync(TextView textView, int mask) {
        return addLinksAsync(textView, TextLinksParams.fromLinkMask(mask), null, null);
    }

    public static Future<Void> addLinksAsync(TextView textView, TextLinksParams params, Executor executor, Consumer<Integer> callback) {
        Preconditions.checkNotNull(textView);
        CharSequence text = textView.getText();
        Spannable spannable = text instanceof Spannable ? (Spannable) text : SpannableString.valueOf(text);
        return addLinksAsync(spannable, textView.getTextClassifier(), params, executor, callback, new Runnable(spannable, text) {
            private final /* synthetic */ Spannable f$1;
            private final /* synthetic */ CharSequence f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                Linkify.lambda$addLinksAsync$0(TextView.this, this.f$1, this.f$2);
            }
        });
    }

    static /* synthetic */ void lambda$addLinksAsync$0(TextView textView, Spannable spannable, CharSequence text) {
        addLinkMovementMethod(textView);
        if (spannable != text) {
            textView.setText((CharSequence) spannable);
        }
    }

    public static Future<Void> addLinksAsync(Spannable text, TextClassifier classifier, TextLinksParams params) {
        return addLinksAsync(text, classifier, params, null, null);
    }

    public static Future<Void> addLinksAsync(Spannable text, TextClassifier classifier, int mask) {
        return addLinksAsync(text, classifier, TextLinksParams.fromLinkMask(mask), null, null);
    }

    public static Future<Void> addLinksAsync(Spannable text, TextClassifier classifier, TextLinksParams params, Executor executor, Consumer<Integer> callback) {
        return addLinksAsync(text, classifier, params, executor, callback, null);
    }

    private static Future<Void> addLinksAsync(Spannable text, TextClassifier classifier, TextLinksParams params, Executor executor, Consumer<Integer> callback, Runnable modifyTextView) {
        Executor executor2 = executor;
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(classifier);
        Spannable spannable = text;
        CharSequence truncatedText = spannable.subSequence(0, Math.min(text.length(), classifier.getMaxGenerateLinksTextLength()));
        Supplier<TextLinks> supplier = new Supplier(new TextLinks.Request.Builder(truncatedText).setLegacyFallback(true).setEntityConfig(params == null ? null : params.getEntityConfig()).build()) {
            private final /* synthetic */ TextLinks.Request f$1;

            {
                this.f$1 = r2;
            }

            public final Object get() {
                return TextClassifier.this.generateLinks(this.f$1);
            }
        };
        $$Lambda$Linkify$ZGgxzuKYqBkZXo_7HE4xwOLsh0 r3 = new Consumer(callback, spannable, truncatedText, params, modifyTextView) {
            private final /* synthetic */ Consumer f$0;
            private final /* synthetic */ Spannable f$1;
            private final /* synthetic */ CharSequence f$2;
            private final /* synthetic */ TextLinksParams f$3;
            private final /* synthetic */ Runnable f$4;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void accept(Object obj) {
                Linkify.lambda$addLinksAsync$2(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, (TextLinks) obj);
            }
        };
        if (executor2 == null) {
            return CompletableFuture.supplyAsync(supplier).thenAccept(r3);
        }
        return CompletableFuture.supplyAsync(supplier, executor2).thenAccept(r3);
    }

    static /* synthetic */ void lambda$addLinksAsync$2(Consumer callback, Spannable text, CharSequence truncatedText, TextLinksParams params, Runnable modifyTextView, TextLinks links) {
        if (links.getLinks().isEmpty()) {
            if (callback != null) {
                callback.accept(1);
            }
            return;
        }
        TextLinks.TextLinkSpan[] old = (TextLinks.TextLinkSpan[]) text.getSpans(0, truncatedText.length(), TextLinks.TextLinkSpan.class);
        int i = old.length - 1;
        while (true) {
            int i2 = i;
            if (i2 < 0) {
                break;
            }
            text.removeSpan(old[i2]);
            i = i2 - 1;
        }
        int result = params.apply(text, links);
        if (result == 0 && modifyTextView != null) {
            modifyTextView.run();
        }
        if (callback != null) {
            callback.accept(Integer.valueOf(result));
        }
    }

    private static final void applyLink(String url, int start, int end, Spannable text) {
        text.setSpan(new URLSpan(url), start, end, 33);
    }

    private static final String makeUrl(String url, String[] prefixes, Matcher matcher, TransformFilter filter) {
        if (filter != null) {
            url = filter.transformUrl(matcher, url);
        }
        boolean hasPrefix = false;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= prefixes.length) {
                break;
            }
            if (url.regionMatches(true, 0, prefixes[i2], 0, prefixes[i2].length())) {
                hasPrefix = true;
                if (!url.regionMatches(false, 0, prefixes[i2], 0, prefixes[i2].length())) {
                    url = prefixes[i2] + url.substring(prefixes[i2].length());
                }
            } else {
                i = i2 + 1;
            }
        }
        if (hasPrefix || prefixes.length <= 0) {
            return url;
        }
        return prefixes[0] + url;
    }

    private static final void gatherLinks(ArrayList<LinkSpec> links, Spannable s, Pattern pattern, String[] schemes, MatchFilter matchFilter, TransformFilter transformFilter) {
        Matcher m = pattern.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
                LinkSpec spec = new LinkSpec();
                spec.url = makeUrl(m.group(0), schemes, m, transformFilter);
                spec.start = start;
                spec.end = end;
                links.add(spec);
            }
        }
    }

    private static void gatherTelLinks(ArrayList<LinkSpec> links, Spannable s, Context context) {
        TelephonyManager tm;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (context == null) {
            tm = TelephonyManager.getDefault();
        } else {
            tm = TelephonyManager.from(context);
        }
        for (PhoneNumberMatch match : phoneUtil.findNumbers(s.toString(), tm.getSimCountryIso().toUpperCase(Locale.US), PhoneNumberUtil.Leniency.POSSIBLE, SubscriptionPlan.BYTES_UNLIMITED)) {
            LinkSpec spec = new LinkSpec();
            spec.url = WebView.SCHEME_TEL + PhoneNumberUtils.normalizeNumber(match.rawString());
            spec.start = match.start();
            spec.end = match.end();
            links.add(spec);
        }
    }

    private static final void gatherMapLinks(ArrayList<LinkSpec> links, Spannable s) {
        String address;
        String string = s.toString();
        int base = 0;
        while (true) {
            try {
                String findAddress = WebView.findAddress(string);
                address = findAddress;
                if (findAddress == null) {
                    break;
                }
                int start = string.indexOf(address);
                if (start < 0) {
                    break;
                }
                LinkSpec spec = new LinkSpec();
                int end = start + address.length();
                spec.start = base + start;
                spec.end = base + end;
                string = string.substring(end);
                base += end;
                try {
                    String encodedAddress = URLEncoder.encode(address, "UTF-8");
                    spec.url = WebView.SCHEME_GEO + encodedAddress;
                    links.add(spec);
                } catch (UnsupportedEncodingException e) {
                }
            } catch (UnsupportedOperationException e2) {
                return;
            }
        }
        String str = address;
    }

    private static final void pruneOverlaps(ArrayList<LinkSpec> links) {
        Collections.sort(links, new Comparator<LinkSpec>() {
            public final int compare(LinkSpec a, LinkSpec b) {
                if (a.start < b.start) {
                    return -1;
                }
                if (a.start > b.start || a.end < b.end) {
                    return 1;
                }
                if (a.end > b.end) {
                    return -1;
                }
                return 0;
            }
        });
        int len = links.size();
        int i = 0;
        while (i < len - 1) {
            LinkSpec a = links.get(i);
            LinkSpec b = links.get(i + 1);
            int remove = -1;
            if (a.start <= b.start && a.end > b.start) {
                if (b.end <= a.end) {
                    remove = i + 1;
                } else if (a.end - a.start > b.end - b.start) {
                    remove = i + 1;
                } else if (a.end - a.start < b.end - b.start) {
                    remove = i;
                }
                if (remove != -1) {
                    links.remove(remove);
                    len--;
                }
            }
            i++;
        }
    }
}
