package com.google.android.util;

import android.view.ThreadedRenderer;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMessageParser {
    private static final /* synthetic */ int[] -com-google-android-util-AbstractMessageParser$Token$TypeSwitchesValues = null;
    public static final String musicNote = "\u266b ";
    private HashMap<Character, Format> formatStart;
    private int nextChar;
    private int nextClass;
    private boolean parseAcronyms;
    private boolean parseFormatting;
    private boolean parseMeText;
    private boolean parseMusic;
    private boolean parseSmilies;
    private boolean parseUrls;
    private ArrayList<Part> parts;
    private String text;
    private ArrayList<Token> tokens;

    public static abstract class Token {
        protected String text;
        protected Type type;

        public enum Type {
            ;
            
            private String stringRep;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.util.AbstractMessageParser.Token.Type.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.util.AbstractMessageParser.Token.Type.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.util.AbstractMessageParser.Token.Type.<clinit>():void");
            }

            private Type(String stringRep) {
                this.stringRep = stringRep;
            }

            public String toString() {
                return this.stringRep;
            }
        }

        public abstract boolean isHtml();

        protected Token(Type type, String text) {
            this.type = type;
            this.text = text;
        }

        public Type getType() {
            return this.type;
        }

        public List<String> getInfo() {
            List<String> info = new ArrayList();
            info.add(getType().toString());
            return info;
        }

        public String getRawText() {
            return this.text;
        }

        public boolean isMedia() {
            return false;
        }

        public boolean isArray() {
            return !isHtml();
        }

        public String toHtml(boolean caps) {
            throw new AssertionError("not html");
        }

        public boolean controlCaps() {
            return false;
        }

        public boolean setCaps() {
            return false;
        }
    }

    public static class Acronym extends Token {
        private String value;

        public Acronym(String text, String value) {
            super(Type.ACRONYM, text);
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public boolean isHtml() {
            return false;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getRawText());
            info.add(getValue());
            return info;
        }
    }

    public static class FlickrPhoto extends Token {
        private static final Pattern GROUPING_PATTERN = null;
        private static final String SETS = "sets";
        private static final String TAGS = "tags";
        private static final Pattern URL_PATTERN = null;
        private String grouping;
        private String groupingId;
        private String photo;
        private String user;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.util.AbstractMessageParser.FlickrPhoto.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.util.AbstractMessageParser.FlickrPhoto.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.util.AbstractMessageParser.FlickrPhoto.<clinit>():void");
        }

        public FlickrPhoto(String user, String photo, String grouping, String groupingId, String text) {
            super(Type.FLICKR, text);
            if (TAGS.equals(user)) {
                this.user = null;
                this.photo = null;
                this.grouping = TAGS;
                this.groupingId = photo;
                return;
            }
            this.user = user;
            if (ThreadedRenderer.OVERDRAW_PROPERTY_SHOW.equals(photo)) {
                photo = null;
            }
            this.photo = photo;
            this.grouping = grouping;
            this.groupingId = groupingId;
        }

        public String getUser() {
            return this.user;
        }

        public String getPhoto() {
            return this.photo;
        }

        public String getGrouping() {
            return this.grouping;
        }

        public String getGroupingId() {
            return this.groupingId;
        }

        public boolean isHtml() {
            return false;
        }

        public boolean isMedia() {
            return true;
        }

        public static FlickrPhoto matchURL(String url, String text) {
            Matcher m = GROUPING_PATTERN.matcher(url);
            if (m.matches()) {
                return new FlickrPhoto(m.group(1), null, m.group(2), m.group(3), text);
            }
            m = URL_PATTERN.matcher(url);
            if (m.matches()) {
                return new FlickrPhoto(m.group(1), m.group(2), null, null, text);
            }
            return null;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getUrl());
            info.add(getUser() != null ? getUser() : "");
            info.add(getPhoto() != null ? getPhoto() : "");
            info.add(getGrouping() != null ? getGrouping() : "");
            info.add(getGroupingId() != null ? getGroupingId() : "");
            return info;
        }

        public String getUrl() {
            if (SETS.equals(this.grouping)) {
                return getUserSetsURL(this.user, this.groupingId);
            }
            if (TAGS.equals(this.grouping)) {
                if (this.user != null) {
                    return getUserTagsURL(this.user, this.groupingId);
                }
                return getTagsURL(this.groupingId);
            } else if (this.photo != null) {
                return getPhotoURL(this.user, this.photo);
            } else {
                return getUserURL(this.user);
            }
        }

        public static String getRssUrl(String user) {
            return null;
        }

        public static String getTagsURL(String tag) {
            return "http://flickr.com/photos/tags/" + tag;
        }

        public static String getUserURL(String user) {
            return "http://flickr.com/photos/" + user;
        }

        public static String getPhotoURL(String user, String photo) {
            return "http://flickr.com/photos/" + user + "/" + photo;
        }

        public static String getUserTagsURL(String user, String tagId) {
            return "http://flickr.com/photos/" + user + "/tags/" + tagId;
        }

        public static String getUserSetsURL(String user, String setId) {
            return "http://flickr.com/photos/" + user + "/sets/" + setId;
        }
    }

    public static class Format extends Token {
        private char ch;
        private boolean matched;
        private boolean start;

        public Format(char ch, boolean start) {
            super(Type.FORMAT, String.valueOf(ch));
            this.ch = ch;
            this.start = start;
        }

        public void setMatched(boolean matched) {
            this.matched = matched;
        }

        public boolean isHtml() {
            return true;
        }

        public String toHtml(boolean caps) {
            if (this.matched) {
                return this.start ? getFormatStart(this.ch) : getFormatEnd(this.ch);
            }
            return this.ch == '\"' ? "&quot;" : String.valueOf(this.ch);
        }

        public List<String> getInfo() {
            throw new UnsupportedOperationException();
        }

        public boolean controlCaps() {
            return this.ch == '^';
        }

        public boolean setCaps() {
            return this.start;
        }

        private String getFormatStart(char ch) {
            switch (ch) {
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    return "<font color=\"#999999\">\u201c";
                case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
                    return "<b>";
                case RILConstants.RIL_REQUEST_CDMA_BROADCAST_ACTIVATION /*94*/:
                    return "<b><font color=\"#005FFF\">";
                case RILConstants.RIL_REQUEST_CDMA_SUBSCRIPTION /*95*/:
                    return "<i>";
                default:
                    throw new AssertionError("unknown format '" + ch + "'");
            }
        }

        private String getFormatEnd(char ch) {
            switch (ch) {
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    return "\u201d</font>";
                case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
                    return "</b>";
                case RILConstants.RIL_REQUEST_CDMA_BROADCAST_ACTIVATION /*94*/:
                    return "</font></b>";
                case RILConstants.RIL_REQUEST_CDMA_SUBSCRIPTION /*95*/:
                    return "</i>";
                default:
                    throw new AssertionError("unknown format '" + ch + "'");
            }
        }
    }

    public static class Html extends Token {
        private String html;

        public Html(String text, String html) {
            super(Type.HTML, text);
            this.html = html;
        }

        public boolean isHtml() {
            return true;
        }

        public String toHtml(boolean caps) {
            return caps ? this.html.toUpperCase() : this.html;
        }

        public List<String> getInfo() {
            throw new UnsupportedOperationException();
        }

        public void trimLeadingWhitespace() {
            this.text = trimLeadingWhitespace(this.text);
            this.html = trimLeadingWhitespace(this.html);
        }

        public void trimTrailingWhitespace() {
            this.text = trimTrailingWhitespace(this.text);
            this.html = trimTrailingWhitespace(this.html);
        }

        private static String trimLeadingWhitespace(String text) {
            int index = 0;
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            return text.substring(index);
        }

        public static String trimTrailingWhitespace(String text) {
            int index = text.length();
            while (index > 0 && Character.isWhitespace(text.charAt(index - 1))) {
                index--;
            }
            return text.substring(0, index);
        }
    }

    public static class Link extends Token {
        private String url;

        public Link(String url, String text) {
            super(Type.LINK, text);
            this.url = url;
        }

        public String getURL() {
            return this.url;
        }

        public boolean isHtml() {
            return false;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getURL());
            info.add(getRawText());
            return info;
        }
    }

    public static class MusicTrack extends Token {
        private String track;

        public MusicTrack(String track) {
            super(Type.MUSIC, track);
            this.track = track;
        }

        public String getTrack() {
            return this.track;
        }

        public boolean isHtml() {
            return false;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getTrack());
            return info;
        }
    }

    public static class Part {
        private String meText;
        private ArrayList<Token> tokens;

        public Part() {
            this.tokens = new ArrayList();
        }

        public String getType(boolean isSend) {
            return (isSend ? "s" : "r") + getPartType();
        }

        private String getPartType() {
            if (isMedia()) {
                return "d";
            }
            if (this.meText != null) {
                return "m";
            }
            return "";
        }

        public boolean isMedia() {
            return this.tokens.size() == 1 ? ((Token) this.tokens.get(0)).isMedia() : false;
        }

        public Token getMediaToken() {
            if (isMedia()) {
                return (Token) this.tokens.get(0);
            }
            return null;
        }

        public void add(Token token) {
            if (isMedia()) {
                throw new AssertionError("media ");
            }
            this.tokens.add(token);
        }

        public void setMeText(String meText) {
            this.meText = meText;
        }

        public String getRawText() {
            StringBuilder buf = new StringBuilder();
            if (this.meText != null) {
                buf.append(this.meText);
            }
            for (int i = 0; i < this.tokens.size(); i++) {
                buf.append(((Token) this.tokens.get(i)).getRawText());
            }
            return buf.toString();
        }

        public ArrayList<Token> getTokens() {
            return this.tokens;
        }
    }

    public static class Photo extends Token {
        private static final Pattern URL_PATTERN = null;
        private String album;
        private String photo;
        private String user;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.util.AbstractMessageParser.Photo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.util.AbstractMessageParser.Photo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.util.AbstractMessageParser.Photo.<clinit>():void");
        }

        public Photo(String user, String album, String photo, String text) {
            super(Type.PHOTO, text);
            this.user = user;
            this.album = album;
            this.photo = photo;
        }

        public String getUser() {
            return this.user;
        }

        public String getAlbum() {
            return this.album;
        }

        public String getPhoto() {
            return this.photo;
        }

        public boolean isHtml() {
            return false;
        }

        public boolean isMedia() {
            return true;
        }

        public static Photo matchURL(String url, String text) {
            Matcher m = URL_PATTERN.matcher(url);
            if (m.matches()) {
                return new Photo(m.group(1), m.group(2), m.group(3), text);
            }
            return null;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getRssUrl(getUser()));
            info.add(getAlbumURL(getUser(), getAlbum()));
            if (getPhoto() != null) {
                info.add(getPhotoURL(getUser(), getAlbum(), getPhoto()));
            } else {
                info.add((String) null);
            }
            return info;
        }

        public static String getRssUrl(String user) {
            return "http://picasaweb.google.com/data/feed/api/user/" + user + "?category=album&alt=rss";
        }

        public static String getAlbumURL(String user, String album) {
            return "http://picasaweb.google.com/" + user + "/" + album;
        }

        public static String getPhotoURL(String user, String album, String photo) {
            return "http://picasaweb.google.com/" + user + "/" + album + "/photo#" + photo;
        }
    }

    public interface Resources {
        TrieNode getAcronyms();

        TrieNode getDomainSuffixes();

        Set<String> getSchemes();

        TrieNode getSmileys();
    }

    public static class Smiley extends Token {
        public Smiley(String text) {
            super(Type.SMILEY, text);
        }

        public boolean isHtml() {
            return false;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getRawText());
            return info;
        }
    }

    public static class TrieNode {
        private final HashMap<Character, TrieNode> children;
        private String text;
        private String value;

        public TrieNode() {
            this("");
        }

        public TrieNode(String text) {
            this.children = new HashMap();
            this.text = text;
        }

        public final boolean exists() {
            return this.value != null;
        }

        public final String getText() {
            return this.text;
        }

        public final String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public TrieNode getChild(char ch) {
            return (TrieNode) this.children.get(Character.valueOf(ch));
        }

        public TrieNode getOrCreateChild(char ch) {
            Character key = Character.valueOf(ch);
            TrieNode node = (TrieNode) this.children.get(key);
            if (node != null) {
                return node;
            }
            node = new TrieNode(this.text + String.valueOf(ch));
            this.children.put(key, node);
            return node;
        }

        public static void addToTrie(TrieNode root, String str, String value) {
            int index = 0;
            while (index < str.length()) {
                int index2 = index + 1;
                root = root.getOrCreateChild(str.charAt(index));
                index = index2;
            }
            root.setValue(value);
        }
    }

    public static class Video extends Token {
        private static final Pattern URL_PATTERN = null;
        private String docid;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.util.AbstractMessageParser.Video.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.util.AbstractMessageParser.Video.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.util.AbstractMessageParser.Video.<clinit>():void");
        }

        public Video(String docid, String text) {
            super(Type.GOOGLE_VIDEO, text);
            this.docid = docid;
        }

        public String getDocID() {
            return this.docid;
        }

        public boolean isHtml() {
            return false;
        }

        public boolean isMedia() {
            return true;
        }

        public static Video matchURL(String url, String text) {
            Matcher m = URL_PATTERN.matcher(url);
            if (m.matches()) {
                return new Video(m.group(1), text);
            }
            return null;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getRssUrl(this.docid));
            info.add(getURL(this.docid));
            return info;
        }

        public static String getRssUrl(String docid) {
            return "http://video.google.com/videofeed?type=docid&output=rss&sourceid=gtalk&docid=" + docid;
        }

        public static String getURL(String docid) {
            return getURL(docid, null);
        }

        public static String getURL(String docid, String extraParams) {
            if (extraParams == null) {
                extraParams = "";
            } else if (extraParams.length() > 0) {
                extraParams = extraParams + "&";
            }
            return "http://video.google.com/videoplay?" + extraParams + "docid=" + docid;
        }
    }

    public static class YouTubeVideo extends Token {
        private static final Pattern URL_PATTERN = null;
        private String docid;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.util.AbstractMessageParser.YouTubeVideo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.util.AbstractMessageParser.YouTubeVideo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.util.AbstractMessageParser.YouTubeVideo.<clinit>():void");
        }

        public YouTubeVideo(String docid, String text) {
            super(Type.YOUTUBE_VIDEO, text);
            this.docid = docid;
        }

        public String getDocID() {
            return this.docid;
        }

        public boolean isHtml() {
            return false;
        }

        public boolean isMedia() {
            return true;
        }

        public static YouTubeVideo matchURL(String url, String text) {
            Matcher m = URL_PATTERN.matcher(url);
            if (m.matches()) {
                return new YouTubeVideo(m.group(1), text);
            }
            return null;
        }

        public List<String> getInfo() {
            List<String> info = super.getInfo();
            info.add(getRssUrl(this.docid));
            info.add(getURL(this.docid));
            return info;
        }

        public static String getRssUrl(String docid) {
            return "http://youtube.com/watch?v=" + docid;
        }

        public static String getURL(String docid) {
            return getURL(docid, null);
        }

        public static String getURL(String docid, String extraParams) {
            if (extraParams == null) {
                extraParams = "";
            } else if (extraParams.length() > 0) {
                extraParams = extraParams + "&";
            }
            return "http://youtube.com/watch?" + extraParams + "v=" + docid;
        }

        public static String getPrefixedURL(boolean http, String prefix, String docid, String extraParams) {
            String protocol = "";
            if (http) {
                protocol = "http://";
            }
            if (prefix == null) {
                prefix = "";
            }
            if (extraParams == null) {
                extraParams = "";
            } else if (extraParams.length() > 0) {
                extraParams = extraParams + "&";
            }
            return protocol + prefix + "youtube.com/watch?" + extraParams + "v=" + docid;
        }
    }

    private static /* synthetic */ int[] -getcom-google-android-util-AbstractMessageParser$Token$TypeSwitchesValues() {
        if (-com-google-android-util-AbstractMessageParser$Token$TypeSwitchesValues != null) {
            return -com-google-android-util-AbstractMessageParser$Token$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.ACRONYM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.FLICKR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.FORMAT.ordinal()] = 9;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Type.GOOGLE_VIDEO.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Type.HTML.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Type.LINK.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Type.MUSIC.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Type.PHOTO.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Type.SMILEY.ordinal()] = 7;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Type.YOUTUBE_VIDEO.ordinal()] = 8;
        } catch (NoSuchFieldError e10) {
        }
        -com-google-android-util-AbstractMessageParser$Token$TypeSwitchesValues = iArr;
        return iArr;
    }

    protected abstract Resources getResources();

    public AbstractMessageParser(String text) {
        this(text, true, true, true, true, true, true);
    }

    public AbstractMessageParser(String text, boolean parseSmilies, boolean parseAcronyms, boolean parseFormatting, boolean parseUrls, boolean parseMusic, boolean parseMeText) {
        this.text = text;
        this.nextChar = 0;
        this.nextClass = 10;
        this.parts = new ArrayList();
        this.tokens = new ArrayList();
        this.formatStart = new HashMap();
        this.parseSmilies = parseSmilies;
        this.parseAcronyms = parseAcronyms;
        this.parseFormatting = parseFormatting;
        this.parseUrls = parseUrls;
        this.parseMusic = parseMusic;
        this.parseMeText = parseMeText;
    }

    public final String getRawText() {
        return this.text;
    }

    public final int getPartCount() {
        return this.parts.size();
    }

    public final Part getPart(int index) {
        return (Part) this.parts.get(index);
    }

    public final List<Part> getParts() {
        return this.parts;
    }

    public void parse() {
        if (parseMusicTrack()) {
            buildParts(null);
            return;
        }
        String str = null;
        if (this.parseMeText && this.text.startsWith("/me") && this.text.length() > 3 && Character.isWhitespace(this.text.charAt(3))) {
            str = this.text.substring(0, 4);
            this.text = this.text.substring(4);
        }
        boolean wasSmiley = false;
        while (this.nextChar < this.text.length()) {
            if (!isWordBreak(this.nextChar) && (!wasSmiley || !isSmileyBreak(this.nextChar))) {
                throw new AssertionError("last chunk did not end at word break");
            } else if (parseSmiley()) {
                wasSmiley = true;
            } else {
                wasSmiley = false;
                if (!(parseAcronym() || parseURL() || parseFormatting())) {
                    parseText();
                }
            }
        }
        int i = 0;
        while (i < this.tokens.size()) {
            if (((Token) this.tokens.get(i)).isMedia()) {
                if (i > 0 && (this.tokens.get(i - 1) instanceof Html)) {
                    ((Html) this.tokens.get(i - 1)).trimLeadingWhitespace();
                }
                if (i + 1 < this.tokens.size() && (this.tokens.get(i + 1) instanceof Html)) {
                    ((Html) this.tokens.get(i + 1)).trimTrailingWhitespace();
                }
            }
            i++;
        }
        i = 0;
        while (i < this.tokens.size()) {
            if (((Token) this.tokens.get(i)).isHtml() && ((Token) this.tokens.get(i)).toHtml(true).length() == 0) {
                this.tokens.remove(i);
                i--;
            }
            i++;
        }
        buildParts(str);
    }

    public static Token tokenForUrl(String url, String text) {
        if (url == null) {
            return null;
        }
        Video video = Video.matchURL(url, text);
        if (video != null) {
            return video;
        }
        YouTubeVideo ytVideo = YouTubeVideo.matchURL(url, text);
        if (ytVideo != null) {
            return ytVideo;
        }
        Photo photo = Photo.matchURL(url, text);
        if (photo != null) {
            return photo;
        }
        FlickrPhoto flickrPhoto = FlickrPhoto.matchURL(url, text);
        if (flickrPhoto != null) {
            return flickrPhoto;
        }
        return new Link(url, text);
    }

    private void buildParts(String meText) {
        for (int i = 0; i < this.tokens.size(); i++) {
            Token token = (Token) this.tokens.get(i);
            if (!(token.isMedia() || this.parts.size() == 0)) {
                if (!lastPart().isMedia()) {
                    lastPart().add(token);
                }
            }
            this.parts.add(new Part());
            lastPart().add(token);
        }
        if (this.parts.size() > 0) {
            ((Part) this.parts.get(0)).setMeText(meText);
        }
    }

    private Part lastPart() {
        return (Part) this.parts.get(this.parts.size() - 1);
    }

    private boolean parseMusicTrack() {
        if (!this.parseMusic || !this.text.startsWith(musicNote)) {
            return false;
        }
        addToken(new MusicTrack(this.text.substring(musicNote.length())));
        this.nextChar = this.text.length();
        return true;
    }

    private void parseText() {
        StringBuilder buf = new StringBuilder();
        int start = this.nextChar;
        do {
            String str = this.text;
            int i = this.nextChar;
            this.nextChar = i + 1;
            char ch = str.charAt(i);
            switch (ch) {
                case PGSdk.TYPE_CLOCK /*10*/:
                    buf.append("<br>");
                    break;
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    buf.append("&quot;");
                    break;
                case PerfHub.PERF_TAG_CTRL_TYPE_NEW /*38*/:
                    buf.append("&amp;");
                    break;
                case PerfHub.PERF_TAG_MAX /*39*/:
                    buf.append("&apos;");
                    break;
                case StatisticalConstant.TYPE_MEDIA_FOUNCTION_STATICS /*60*/:
                    buf.append("&lt;");
                    break;
                case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
                    buf.append("&gt;");
                    break;
                default:
                    buf.append(ch);
                    break;
            }
        } while (!isWordBreak(this.nextChar));
        addToken(new Html(this.text.substring(start, this.nextChar), buf.toString()));
    }

    private boolean parseSmiley() {
        if (!this.parseSmilies) {
            return false;
        }
        TrieNode match = longestMatch(getResources().getSmileys(), this, this.nextChar, true);
        if (match == null) {
            return false;
        }
        int previousCharClass = getCharClass(this.nextChar - 1);
        int nextCharClass = getCharClass(this.nextChar + match.getText().length());
        if ((previousCharClass == 2 || previousCharClass == 3) && (nextCharClass == 2 || nextCharClass == 3)) {
            return false;
        }
        addToken(new Smiley(match.getText()));
        this.nextChar += match.getText().length();
        return true;
    }

    private boolean parseAcronym() {
        if (!this.parseAcronyms) {
            return false;
        }
        TrieNode match = longestMatch(getResources().getAcronyms(), this, this.nextChar);
        if (match == null) {
            return false;
        }
        addToken(new Acronym(match.getText(), match.getValue()));
        this.nextChar += match.getText().length();
        return true;
    }

    private boolean isDomainChar(char c) {
        return (c == '-' || Character.isLetter(c)) ? true : Character.isDigit(c);
    }

    private boolean isValidDomain(String domain) {
        if (matches(getResources().getDomainSuffixes(), reverse(domain))) {
            return true;
        }
        return false;
    }

    private boolean parseURL() {
        if (!this.parseUrls || !isURLBreak(this.nextChar)) {
            return false;
        }
        int start = this.nextChar;
        int index = start;
        while (index < this.text.length() && isDomainChar(this.text.charAt(index))) {
            index++;
        }
        String url = "";
        boolean done = false;
        if (index == this.text.length()) {
            return false;
        }
        if (this.text.charAt(index) == ':') {
            if (!getResources().getSchemes().contains(this.text.substring(this.nextChar, index))) {
                return false;
            }
        } else if (this.text.charAt(index) != '.') {
            return false;
        } else {
            char ch;
            while (index < this.text.length()) {
                ch = this.text.charAt(index);
                if (ch != '.' && !isDomainChar(ch)) {
                    break;
                }
                index++;
            }
            if (!isValidDomain(this.text.substring(this.nextChar, index))) {
                return false;
            }
            if (index + 1 < this.text.length() && this.text.charAt(index) == ':' && Character.isDigit(this.text.charAt(index + 1))) {
                index++;
                while (index < this.text.length() && Character.isDigit(this.text.charAt(index))) {
                    index++;
                }
            }
            if (index == this.text.length()) {
                done = true;
            } else {
                ch = this.text.charAt(index);
                if (ch == '?') {
                    if (index + 1 == this.text.length()) {
                        done = true;
                    } else {
                        char ch2 = this.text.charAt(index + 1);
                        if (Character.isWhitespace(ch2) || isPunctuation(ch2)) {
                            done = true;
                        }
                    }
                } else if (isPunctuation(ch)) {
                    done = true;
                } else if (Character.isWhitespace(ch)) {
                    done = true;
                } else if (!(ch == '/' || ch == '#')) {
                    return false;
                }
            }
            url = "http://";
        }
        if (!done) {
            while (index < this.text.length() && !Character.isWhitespace(this.text.charAt(index))) {
                index++;
            }
        }
        String urlText = this.text.substring(start, index);
        addURLToken(url + urlText, urlText);
        this.nextChar = index;
        return true;
    }

    private void addURLToken(String url, String text) {
        addToken(tokenForUrl(url, text));
    }

    private boolean parseFormatting() {
        if (!this.parseFormatting) {
            return false;
        }
        int endChar = this.nextChar;
        while (endChar < this.text.length() && isFormatChar(this.text.charAt(endChar))) {
            endChar++;
        }
        if (endChar == this.nextChar || !isWordBreak(endChar)) {
            return false;
        }
        LinkedHashMap<Character, Boolean> seenCharacters = new LinkedHashMap();
        for (int index = this.nextChar; index < endChar; index++) {
            char ch = this.text.charAt(index);
            Character key = Character.valueOf(ch);
            if (seenCharacters.containsKey(key)) {
                addToken(new Format(ch, false));
            } else {
                Format start = (Format) this.formatStart.get(key);
                if (start != null) {
                    start.setMatched(true);
                    this.formatStart.remove(key);
                    seenCharacters.put(key, Boolean.TRUE);
                } else {
                    start = new Format(ch, true);
                    this.formatStart.put(key, start);
                    addToken(start);
                    seenCharacters.put(key, Boolean.FALSE);
                }
            }
        }
        for (Character key2 : seenCharacters.keySet()) {
            if (seenCharacters.get(key2) == Boolean.TRUE) {
                Format end = new Format(key2.charValue(), false);
                end.setMatched(true);
                addToken(end);
            }
        }
        this.nextChar = endChar;
        return true;
    }

    private boolean isWordBreak(int index) {
        return getCharClass(index + -1) != getCharClass(index);
    }

    private boolean isSmileyBreak(int index) {
        if (index <= 0 || index >= this.text.length() || !isSmileyBreak(this.text.charAt(index - 1), this.text.charAt(index))) {
            return false;
        }
        return true;
    }

    private boolean isURLBreak(int index) {
        switch (getCharClass(index - 1)) {
            case HwCfgFilePolicy.PC /*2*/:
            case HwCfgFilePolicy.BASE /*3*/:
            case HwCfgFilePolicy.CUST /*4*/:
                return false;
            default:
                return true;
        }
    }

    private int getCharClass(int index) {
        if (index < 0 || this.text.length() <= index) {
            return 0;
        }
        char ch = this.text.charAt(index);
        if (Character.isWhitespace(ch)) {
            return 1;
        }
        if (Character.isLetter(ch)) {
            return 2;
        }
        if (Character.isDigit(ch)) {
            return 3;
        }
        if (!isPunctuation(ch)) {
            return 4;
        }
        int i = this.nextClass + 1;
        this.nextClass = i;
        return i;
    }

    private static boolean isSmileyBreak(char c1, char c2) {
        switch (c1) {
            case PerfHub.PERF_TAG_AVL_GPU_FREQ_LIST /*36*/:
            case PerfHub.PERF_TAG_CTRL_TYPE_NEW /*38*/:
            case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
            case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
            case RILConstants.RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE /*45*/:
            case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL /*47*/:
            case StatisticalConstant.TYPE_MEDIA_FOUNCTION_STATICS /*60*/:
            case StatisticalConstant.TYPE_WIFI_END /*61*/:
            case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
            case RILConstants.RIL_REQUEST_DELETE_SMS_ON_SIM /*64*/:
            case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
            case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
            case RILConstants.RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG /*93*/:
            case RILConstants.RIL_REQUEST_CDMA_BROADCAST_ACTIVATION /*94*/:
            case LogPower.NOTIFICATION_CANCEL_ALL /*124*/:
            case LogPower.TOUCH_DOWN /*125*/:
            case LogPower.TOUCH_UP /*126*/:
                switch (c2) {
                    case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                    case PerfHub.PERF_TAG_AVL_GPU_FREQ_LIST /*36*/:
                    case PerfHub.PERF_TAG_AVL_DDR_FREQ_LIST /*37*/:
                    case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
                    case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL /*47*/:
                    case StatisticalConstant.TYPE_MEDIA_FOUNCTION_STATICS /*60*/:
                    case StatisticalConstant.TYPE_WIFI_END /*61*/:
                    case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
                    case RILConstants.RIL_REQUEST_DELETE_SMS_ON_SIM /*64*/:
                    case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
                    case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
                    case RILConstants.RIL_REQUEST_CDMA_BROADCAST_ACTIVATION /*94*/:
                    case LogPower.TOUCH_UP /*126*/:
                        return true;
                    default:
                        break;
                }
        }
        return false;
    }

    private static boolean isPunctuation(char ch) {
        switch (ch) {
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
            case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
            case StatisticalConstant.TYPE_SINGLEHAND_START /*40*/:
            case StatisticalConstant.TYPE_SINGLEHAND_ENTER /*41*/:
            case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
            case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_AUTOMATIC /*46*/:
            case RILConstants.RIL_REQUEST_RESET_RADIO /*58*/:
            case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
            case RILConstants.RIL_REQUEST_WRITE_SMS_TO_SIM /*63*/:
                return true;
            default:
                return false;
        }
    }

    private static boolean isFormatChar(char ch) {
        switch (ch) {
            case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
            case RILConstants.RIL_REQUEST_CDMA_BROADCAST_ACTIVATION /*94*/:
            case RILConstants.RIL_REQUEST_CDMA_SUBSCRIPTION /*95*/:
                return true;
            default:
                return false;
        }
    }

    private void addToken(Token token) {
        this.tokens.add(token);
    }

    public String toHtml() {
        StringBuilder html = new StringBuilder();
        for (Part part : this.parts) {
            boolean caps = false;
            html.append("<p>");
            for (Token token : part.getTokens()) {
                if (token.isHtml()) {
                    html.append(token.toHtml(caps));
                } else {
                    switch (-getcom-google-android-util-AbstractMessageParser$Token$TypeSwitchesValues()[token.getType().ordinal()]) {
                        case HwCfgFilePolicy.EMUI /*1*/:
                            html.append(token.getRawText());
                            break;
                        case HwCfgFilePolicy.PC /*2*/:
                            Photo p = (Photo) token;
                            html.append("<a href=\"");
                            html.append(((FlickrPhoto) token).getUrl());
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case HwCfgFilePolicy.BASE /*3*/:
                            html.append("<a href=\"");
                            Video video = (Video) token;
                            html.append(Video.getURL(((Video) token).getDocID()));
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case HwCfgFilePolicy.CUST /*4*/:
                            html.append("<a href=\"");
                            html.append(((Link) token).getURL());
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                            html.append(((MusicTrack) token).getTrack());
                            break;
                        case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                            html.append("<a href=\"");
                            html.append(Photo.getAlbumURL(((Photo) token).getUser(), ((Photo) token).getAlbum()));
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case HwCfgFilePolicy.CLOUD_APN /*7*/:
                            html.append(token.getRawText());
                            break;
                        case PGSdk.TYPE_VIDEO /*8*/:
                            html.append("<a href=\"");
                            YouTubeVideo youTubeVideo = (YouTubeVideo) token;
                            html.append(YouTubeVideo.getURL(((YouTubeVideo) token).getDocID()));
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        default:
                            throw new AssertionError("unknown token type: " + token.getType());
                    }
                }
                if (token.controlCaps()) {
                    caps = token.setCaps();
                }
            }
            html.append("</p>\n");
        }
        return html.toString();
    }

    protected static String reverse(String str) {
        StringBuilder buf = new StringBuilder();
        for (int i = str.length() - 1; i >= 0; i--) {
            buf.append(str.charAt(i));
        }
        return buf.toString();
    }

    private static boolean matches(TrieNode root, String str) {
        int index = 0;
        while (index < str.length()) {
            int index2 = index + 1;
            root = root.getChild(str.charAt(index));
            if (root == null) {
                index = index2;
                break;
            } else if (root.exists()) {
                return true;
            } else {
                index = index2;
            }
        }
        return false;
    }

    private static TrieNode longestMatch(TrieNode root, AbstractMessageParser p, int start) {
        return longestMatch(root, p, start, false);
    }

    private static TrieNode longestMatch(TrieNode root, AbstractMessageParser p, int start, boolean smiley) {
        int index = start;
        TrieNode trieNode = null;
        while (index < p.getRawText().length()) {
            int index2 = index + 1;
            root = root.getChild(p.getRawText().charAt(index));
            if (root == null) {
                index = index2;
                break;
            }
            if (root.exists()) {
                if (p.isWordBreak(index2)) {
                    trieNode = root;
                } else if (smiley && p.isSmileyBreak(index2)) {
                    trieNode = root;
                }
            }
            index = index2;
        }
        return trieNode;
    }
}
