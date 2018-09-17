package android.text;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.Telephony.BaseMmsColumns;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.format.DateFormat;
import android.text.style.AlignmentSpan.Standard;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.LogException;
import com.android.internal.R;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/* compiled from: Html */
class HtmlToSpannedConverter implements ContentHandler {
    private static final float[] HEADING_SIZES = new float[]{1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1.0f};
    private static Pattern sBackgroundColorPattern;
    private static final Map<String, Integer> sColorMap = new HashMap();
    private static Pattern sForegroundColorPattern;
    private static Pattern sTextAlignPattern;
    private static Pattern sTextDecorationPattern;
    private int mFlags;
    private ImageGetter mImageGetter;
    private XMLReader mReader;
    private String mSource;
    private SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder();
    private TagHandler mTagHandler;

    /* compiled from: Html */
    private static class Alignment {
        private android.text.Layout.Alignment mAlignment;

        public Alignment(android.text.Layout.Alignment alignment) {
            this.mAlignment = alignment;
        }
    }

    /* compiled from: Html */
    private static class Background {
        private int mBackgroundColor;

        public Background(int backgroundColor) {
            this.mBackgroundColor = backgroundColor;
        }
    }

    /* compiled from: Html */
    private static class Big {
        /* synthetic */ Big(Big -this0) {
            this();
        }

        private Big() {
        }
    }

    /* compiled from: Html */
    private static class Blockquote {
        /* synthetic */ Blockquote(Blockquote -this0) {
            this();
        }

        private Blockquote() {
        }
    }

    /* compiled from: Html */
    private static class Bold {
        /* synthetic */ Bold(Bold -this0) {
            this();
        }

        private Bold() {
        }
    }

    /* compiled from: Html */
    private static class Bullet {
        /* synthetic */ Bullet(Bullet -this0) {
            this();
        }

        private Bullet() {
        }
    }

    /* compiled from: Html */
    private static class Font {
        public String mFace;

        public Font(String face) {
            this.mFace = face;
        }
    }

    /* compiled from: Html */
    private static class Foreground {
        private int mForegroundColor;

        public Foreground(int foregroundColor) {
            this.mForegroundColor = foregroundColor;
        }
    }

    /* compiled from: Html */
    private static class Heading {
        private int mLevel;

        public Heading(int level) {
            this.mLevel = level;
        }
    }

    /* compiled from: Html */
    private static class Href {
        public String mHref;

        public Href(String href) {
            this.mHref = href;
        }
    }

    /* compiled from: Html */
    private static class Italic {
        /* synthetic */ Italic(Italic -this0) {
            this();
        }

        private Italic() {
        }
    }

    /* compiled from: Html */
    private static class Monospace {
        /* synthetic */ Monospace(Monospace -this0) {
            this();
        }

        private Monospace() {
        }
    }

    /* compiled from: Html */
    private static class Newline {
        private int mNumNewlines;

        public Newline(int numNewlines) {
            this.mNumNewlines = numNewlines;
        }
    }

    /* compiled from: Html */
    private static class Small {
        /* synthetic */ Small(Small -this0) {
            this();
        }

        private Small() {
        }
    }

    /* compiled from: Html */
    private static class Strikethrough {
        /* synthetic */ Strikethrough(Strikethrough -this0) {
            this();
        }

        private Strikethrough() {
        }
    }

    /* compiled from: Html */
    private static class Sub {
        /* synthetic */ Sub(Sub -this0) {
            this();
        }

        private Sub() {
        }
    }

    /* compiled from: Html */
    private static class Super {
        /* synthetic */ Super(Super -this0) {
            this();
        }

        private Super() {
        }
    }

    /* compiled from: Html */
    private static class Underline {
        /* synthetic */ Underline(Underline -this0) {
            this();
        }

        private Underline() {
        }
    }

    static {
        sColorMap.put("darkgray", Integer.valueOf(-5658199));
        sColorMap.put("gray", Integer.valueOf(-8355712));
        sColorMap.put("lightgray", Integer.valueOf(-2894893));
        sColorMap.put("darkgrey", Integer.valueOf(-5658199));
        sColorMap.put("grey", Integer.valueOf(-8355712));
        sColorMap.put("lightgrey", Integer.valueOf(-2894893));
        sColorMap.put("green", Integer.valueOf(-16744448));
    }

    private static Pattern getTextAlignPattern() {
        if (sTextAlignPattern == null) {
            sTextAlignPattern = Pattern.compile("(?:\\s+|\\A)text-align\\s*:\\s*(\\S*)\\b");
        }
        return sTextAlignPattern;
    }

    private static Pattern getForegroundColorPattern() {
        if (sForegroundColorPattern == null) {
            sForegroundColorPattern = Pattern.compile("(?:\\s+|\\A)color\\s*:\\s*(\\S*)\\b");
        }
        return sForegroundColorPattern;
    }

    private static Pattern getBackgroundColorPattern() {
        if (sBackgroundColorPattern == null) {
            sBackgroundColorPattern = Pattern.compile("(?:\\s+|\\A)background(?:-color)?\\s*:\\s*(\\S*)\\b");
        }
        return sBackgroundColorPattern;
    }

    private static Pattern getTextDecorationPattern() {
        if (sTextDecorationPattern == null) {
            sTextDecorationPattern = Pattern.compile("(?:\\s+|\\A)text-decoration\\s*:\\s*(\\S*)\\b");
        }
        return sTextDecorationPattern;
    }

    public HtmlToSpannedConverter(String source, ImageGetter imageGetter, TagHandler tagHandler, Parser parser, int flags) {
        this.mSource = source;
        this.mImageGetter = imageGetter;
        this.mTagHandler = tagHandler;
        this.mReader = parser;
        this.mFlags = flags;
    }

    public Spanned convert() {
        this.mReader.setContentHandler(this);
        try {
            this.mReader.parse(new InputSource(new StringReader(this.mSource)));
            Object[] obj = this.mSpannableStringBuilder.getSpans(0, this.mSpannableStringBuilder.length(), ParagraphStyle.class);
            for (int i = 0; i < obj.length; i++) {
                int start = this.mSpannableStringBuilder.getSpanStart(obj[i]);
                int end = this.mSpannableStringBuilder.getSpanEnd(obj[i]);
                if (end - 2 >= 0 && this.mSpannableStringBuilder.charAt(end - 1) == 10 && this.mSpannableStringBuilder.charAt(end - 2) == 10) {
                    end--;
                }
                if (end == start) {
                    this.mSpannableStringBuilder.removeSpan(obj[i]);
                } else {
                    this.mSpannableStringBuilder.setSpan(obj[i], start, end, 51);
                }
            }
            return this.mSpannableStringBuilder;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e2) {
            throw new RuntimeException(e2);
        }
    }

    private void handleStartTag(String tag, Attributes attributes) {
        if (!tag.equalsIgnoreCase("br")) {
            if (tag.equalsIgnoreCase("p")) {
                startBlockElement(this.mSpannableStringBuilder, attributes, getMarginParagraph());
                startCssStyle(this.mSpannableStringBuilder, attributes);
            } else if (tag.equalsIgnoreCase("ul")) {
                startBlockElement(this.mSpannableStringBuilder, attributes, getMarginList());
            } else if (tag.equalsIgnoreCase("li")) {
                startLi(this.mSpannableStringBuilder, attributes);
            } else if (tag.equalsIgnoreCase("div")) {
                startBlockElement(this.mSpannableStringBuilder, attributes, getMarginDiv());
            } else if (tag.equalsIgnoreCase("span")) {
                startCssStyle(this.mSpannableStringBuilder, attributes);
            } else if (tag.equalsIgnoreCase("strong")) {
                start(this.mSpannableStringBuilder, new Bold());
            } else if (tag.equalsIgnoreCase("b")) {
                start(this.mSpannableStringBuilder, new Bold());
            } else if (tag.equalsIgnoreCase("em")) {
                start(this.mSpannableStringBuilder, new Italic());
            } else if (tag.equalsIgnoreCase("cite")) {
                start(this.mSpannableStringBuilder, new Italic());
            } else if (tag.equalsIgnoreCase("dfn")) {
                start(this.mSpannableStringBuilder, new Italic());
            } else if (tag.equalsIgnoreCase("i")) {
                start(this.mSpannableStringBuilder, new Italic());
            } else if (tag.equalsIgnoreCase("big")) {
                start(this.mSpannableStringBuilder, new Big());
            } else if (tag.equalsIgnoreCase("small")) {
                start(this.mSpannableStringBuilder, new Small());
            } else if (tag.equalsIgnoreCase("font")) {
                startFont(this.mSpannableStringBuilder, attributes);
            } else if (tag.equalsIgnoreCase("blockquote")) {
                startBlockquote(this.mSpannableStringBuilder, attributes);
            } else if (tag.equalsIgnoreCase("tt")) {
                start(this.mSpannableStringBuilder, new Monospace());
            } else if (tag.equalsIgnoreCase("a")) {
                startA(this.mSpannableStringBuilder, attributes);
            } else if (tag.equalsIgnoreCase("u")) {
                start(this.mSpannableStringBuilder, new Underline());
            } else if (tag.equalsIgnoreCase("del")) {
                start(this.mSpannableStringBuilder, new Strikethrough());
            } else if (tag.equalsIgnoreCase("s")) {
                start(this.mSpannableStringBuilder, new Strikethrough());
            } else if (tag.equalsIgnoreCase("strike")) {
                start(this.mSpannableStringBuilder, new Strikethrough());
            } else if (tag.equalsIgnoreCase("sup")) {
                start(this.mSpannableStringBuilder, new Super());
            } else if (tag.equalsIgnoreCase(BaseMmsColumns.SUBJECT)) {
                start(this.mSpannableStringBuilder, new Sub());
            } else if (tag.length() == 2 && Character.toLowerCase(tag.charAt(0)) == DateFormat.HOUR && tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
                startHeading(this.mSpannableStringBuilder, attributes, tag.charAt(1) - 49);
            } else if (tag.equalsIgnoreCase("img")) {
                startImg(this.mSpannableStringBuilder, attributes, this.mImageGetter);
            } else if (this.mTagHandler != null) {
                this.mTagHandler.handleTag(true, tag, this.mSpannableStringBuilder, this.mReader);
            }
        }
    }

    private void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            handleBr(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("p")) {
            endCssStyle(this.mSpannableStringBuilder);
            endBlockElement(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("ul")) {
            endBlockElement(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("li")) {
            endLi(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            endBlockElement(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("span")) {
            endCssStyle(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            end(this.mSpannableStringBuilder, Bold.class, new StyleSpan(1));
        } else if (tag.equalsIgnoreCase("b")) {
            end(this.mSpannableStringBuilder, Bold.class, new StyleSpan(1));
        } else if (tag.equalsIgnoreCase("em")) {
            end(this.mSpannableStringBuilder, Italic.class, new StyleSpan(2));
        } else if (tag.equalsIgnoreCase("cite")) {
            end(this.mSpannableStringBuilder, Italic.class, new StyleSpan(2));
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(this.mSpannableStringBuilder, Italic.class, new StyleSpan(2));
        } else if (tag.equalsIgnoreCase("i")) {
            end(this.mSpannableStringBuilder, Italic.class, new StyleSpan(2));
        } else if (tag.equalsIgnoreCase("big")) {
            end(this.mSpannableStringBuilder, Big.class, new RelativeSizeSpan(1.25f));
        } else if (tag.equalsIgnoreCase("small")) {
            end(this.mSpannableStringBuilder, Small.class, new RelativeSizeSpan(0.8f));
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            endBlockquote(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("tt")) {
            end(this.mSpannableStringBuilder, Monospace.class, new TypefaceSpan("monospace"));
        } else if (tag.equalsIgnoreCase("a")) {
            endA(this.mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("u")) {
            end(this.mSpannableStringBuilder, Underline.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("del")) {
            end(this.mSpannableStringBuilder, Strikethrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("s")) {
            end(this.mSpannableStringBuilder, Strikethrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("strike")) {
            end(this.mSpannableStringBuilder, Strikethrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("sup")) {
            end(this.mSpannableStringBuilder, Super.class, new SuperscriptSpan());
        } else if (tag.equalsIgnoreCase(BaseMmsColumns.SUBJECT)) {
            end(this.mSpannableStringBuilder, Sub.class, new SubscriptSpan());
        } else if (tag.length() == 2 && Character.toLowerCase(tag.charAt(0)) == DateFormat.HOUR && tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            endHeading(this.mSpannableStringBuilder);
        } else if (this.mTagHandler != null) {
            this.mTagHandler.handleTag(false, tag, this.mSpannableStringBuilder, this.mReader);
        }
    }

    private int getMarginParagraph() {
        return getMargin(1);
    }

    private int getMarginHeading() {
        return getMargin(2);
    }

    private int getMarginListItem() {
        return getMargin(4);
    }

    private int getMarginList() {
        return getMargin(8);
    }

    private int getMarginDiv() {
        return getMargin(16);
    }

    private int getMarginBlockquote() {
        return getMargin(32);
    }

    private int getMargin(int flag) {
        if ((this.mFlags & flag) != 0) {
            return 1;
        }
        return 2;
    }

    private static void appendNewlines(Editable text, int minNewline) {
        int len = text.length();
        if (len != 0) {
            int existingNewlines = 0;
            int i = len - 1;
            while (i >= 0 && text.charAt(i) == 10) {
                existingNewlines++;
                i--;
            }
            for (int j = existingNewlines; j < minNewline; j++) {
                text.append((CharSequence) "\n");
            }
        }
    }

    private static void startBlockElement(Editable text, Attributes attributes, int margin) {
        int len = text.length();
        if (margin > 0) {
            appendNewlines(text, margin);
            start(text, new Newline(margin));
        }
        String style = attributes.getValue(LogException.NO_VALUE, "style");
        if (style != null) {
            Matcher m = getTextAlignPattern().matcher(style);
            if (m.find()) {
                String alignment = m.group(1);
                if (alignment.equalsIgnoreCase(BaseMmsColumns.START)) {
                    start(text, new Alignment(android.text.Layout.Alignment.ALIGN_NORMAL));
                } else if (alignment.equalsIgnoreCase("center")) {
                    start(text, new Alignment(android.text.Layout.Alignment.ALIGN_CENTER));
                } else if (alignment.equalsIgnoreCase("end")) {
                    start(text, new Alignment(android.text.Layout.Alignment.ALIGN_OPPOSITE));
                }
            }
        }
    }

    private static void endBlockElement(Editable text) {
        Newline n = (Newline) getLast(text, Newline.class);
        if (n != null) {
            appendNewlines(text, n.mNumNewlines);
            text.removeSpan(n);
        }
        Alignment a = (Alignment) getLast(text, Alignment.class);
        if (a != null) {
            setSpanFromMark(text, a, new Standard(a.mAlignment));
        }
    }

    private static void handleBr(Editable text) {
        text.append(10);
    }

    private void startLi(Editable text, Attributes attributes) {
        startBlockElement(text, attributes, getMarginListItem());
        start(text, new Bullet());
        startCssStyle(text, attributes);
    }

    private static void endLi(Editable text) {
        endCssStyle(text);
        endBlockElement(text);
        end(text, Bullet.class, new BulletSpan());
    }

    private void startBlockquote(Editable text, Attributes attributes) {
        startBlockElement(text, attributes, getMarginBlockquote());
        start(text, new Blockquote());
    }

    private static void endBlockquote(Editable text) {
        endBlockElement(text);
        end(text, Blockquote.class, new QuoteSpan());
    }

    private void startHeading(Editable text, Attributes attributes, int level) {
        startBlockElement(text, attributes, getMarginHeading());
        start(text, new Heading(level));
    }

    private static void endHeading(Editable text) {
        Heading h = (Heading) getLast(text, Heading.class);
        if (h != null) {
            setSpanFromMark(text, h, new RelativeSizeSpan(HEADING_SIZES[h.mLevel]), new StyleSpan(1));
        }
        endBlockElement(text);
    }

    private static <T> T getLast(Spanned text, Class<T> kind) {
        T[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        }
        return objs[objs.length - 1];
    }

    private static void setSpanFromMark(Spannable text, Object mark, Object... spans) {
        int where = text.getSpanStart(mark);
        text.removeSpan(mark);
        int len = text.length();
        if (where != len) {
            for (Object span : spans) {
                text.setSpan(span, where, len, 33);
            }
        }
    }

    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, 17);
    }

    private static void end(Editable text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        if (obj != null) {
            setSpanFromMark(text, obj, repl);
        }
    }

    private void startCssStyle(Editable text, Attributes attributes) {
        String style = attributes.getValue(LogException.NO_VALUE, "style");
        if (style != null) {
            int c;
            Matcher m = getForegroundColorPattern().matcher(style);
            if (m.find()) {
                c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Foreground(c | -16777216));
                }
            }
            m = getBackgroundColorPattern().matcher(style);
            if (m.find()) {
                c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Background(c | -16777216));
                }
            }
            m = getTextDecorationPattern().matcher(style);
            if (m.find() && m.group(1).equalsIgnoreCase("line-through")) {
                start(text, new Strikethrough());
            }
        }
    }

    private static void endCssStyle(Editable text) {
        Strikethrough s = (Strikethrough) getLast(text, Strikethrough.class);
        if (s != null) {
            setSpanFromMark(text, s, new StrikethroughSpan());
        }
        Background b = (Background) getLast(text, Background.class);
        if (b != null) {
            setSpanFromMark(text, b, new BackgroundColorSpan(b.mBackgroundColor));
        }
        Foreground f = (Foreground) getLast(text, Foreground.class);
        if (f != null) {
            setSpanFromMark(text, f, new ForegroundColorSpan(f.mForegroundColor));
        }
    }

    private static void startImg(Editable text, Attributes attributes, ImageGetter img) {
        String src = attributes.getValue(LogException.NO_VALUE, "src");
        Drawable d = null;
        if (img != null) {
            d = img.getDrawable(src);
        }
        if (d == null) {
            d = Resources.getSystem().getDrawable(R.drawable.unknown_image);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }
        int len = text.length();
        text.append((CharSequence) "￼");
        text.setSpan(new ImageSpan(d, src), len, text.length(), 33);
    }

    private void startFont(Editable text, Attributes attributes) {
        String color = attributes.getValue(LogException.NO_VALUE, "color");
        String face = attributes.getValue(LogException.NO_VALUE, "face");
        if (!TextUtils.isEmpty(color)) {
            int c = getHtmlColor(color);
            if (c != -1) {
                start(text, new Foreground(-16777216 | c));
            }
        }
        if (!TextUtils.isEmpty(face)) {
            start(text, new Font(face));
        }
    }

    private static void endFont(Editable text) {
        Font font = (Font) getLast(text, Font.class);
        if (font != null) {
            setSpanFromMark(text, font, new TypefaceSpan(font.mFace));
        }
        Foreground foreground = (Foreground) getLast(text, Foreground.class);
        if (foreground != null) {
            setSpanFromMark(text, foreground, new ForegroundColorSpan(foreground.mForegroundColor));
        }
    }

    private static void startA(Editable text, Attributes attributes) {
        start(text, new Href(attributes.getValue(LogException.NO_VALUE, "href")));
    }

    private static void endA(Editable text) {
        Href h = (Href) getLast(text, Href.class);
        if (h != null && h.mHref != null) {
            setSpanFromMark(text, h, new URLSpan(h.mHref));
        }
    }

    private int getHtmlColor(String color) {
        if ((this.mFlags & 256) == 256) {
            Integer i = (Integer) sColorMap.get(color.toLowerCase(Locale.US));
            if (i != null) {
                return i.intValue();
            }
        }
        return Color.getHtmlColor(color);
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        handleStartTag(localName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        handleEndTag(localName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        CharSequence sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = ch[i + start];
            if (c == ' ' || c == 10) {
                char pred;
                int len = sb.length();
                if (len == 0) {
                    len = this.mSpannableStringBuilder.length();
                    if (len == 0) {
                        pred = 10;
                    } else {
                        pred = this.mSpannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }
                if (!(pred == ' ' || pred == 10)) {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }
        this.mSpannableStringBuilder.append(sb);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
