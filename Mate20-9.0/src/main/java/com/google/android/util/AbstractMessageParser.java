package com.google.android.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMessageParser {
    public static final String musicNote = "♫ ";
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

    public static class Acronym extends Token {
        private String value;

        public Acronym(String text, String value2) {
            super(Token.Type.ACRONYM, text);
            this.value = value2;
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
        private static final Pattern GROUPING_PATTERN = Pattern.compile("http://(?:www.)?flickr.com/photos/([^/?#&]+)/(tags|sets)/([^/?#&]+)/?");
        private static final String SETS = "sets";
        private static final String TAGS = "tags";
        private static final Pattern URL_PATTERN = Pattern.compile("http://(?:www.)?flickr.com/photos/([^/?#&]+)/?([^/?#&]+)?/?.*");
        private String grouping;
        private String groupingId;
        private String photo;
        private String user;

        public FlickrPhoto(String user2, String photo2, String grouping2, String groupingId2, String text) {
            super(Token.Type.FLICKR, text);
            String str = null;
            if (!TAGS.equals(user2)) {
                this.user = user2;
                this.photo = !"show".equals(photo2) ? photo2 : str;
                this.grouping = grouping2;
                this.groupingId = groupingId2;
                return;
            }
            this.user = null;
            this.photo = null;
            this.grouping = TAGS;
            this.groupingId = photo2;
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
                FlickrPhoto flickrPhoto = new FlickrPhoto(m.group(1), null, m.group(2), m.group(3), text);
                return flickrPhoto;
            }
            Matcher m2 = URL_PATTERN.matcher(url);
            if (!m2.matches()) {
                return null;
            }
            FlickrPhoto flickrPhoto2 = new FlickrPhoto(m2.group(1), m2.group(2), null, null, text);
            return flickrPhoto2;
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

        public static String getRssUrl(String user2) {
            return null;
        }

        public static String getTagsURL(String tag) {
            return "http://flickr.com/photos/tags/" + tag;
        }

        public static String getUserURL(String user2) {
            return "http://flickr.com/photos/" + user2;
        }

        public static String getPhotoURL(String user2, String photo2) {
            return "http://flickr.com/photos/" + user2 + "/" + photo2;
        }

        public static String getUserTagsURL(String user2, String tagId) {
            return "http://flickr.com/photos/" + user2 + "/tags/" + tagId;
        }

        public static String getUserSetsURL(String user2, String setId) {
            return "http://flickr.com/photos/" + user2 + "/sets/" + setId;
        }
    }

    public static class Format extends Token {
        private char ch;
        private boolean matched;
        private boolean start;

        public Format(char ch2, boolean start2) {
            super(Token.Type.FORMAT, String.valueOf(ch2));
            this.ch = ch2;
            this.start = start2;
        }

        public void setMatched(boolean matched2) {
            this.matched = matched2;
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

        private String getFormatStart(char ch2) {
            if (ch2 == '\"') {
                return "<font color=\"#999999\">“";
            }
            if (ch2 == '*') {
                return "<b>";
            }
            switch (ch2) {
                case '^':
                    return "<b><font color=\"#005FFF\">";
                case '_':
                    return "<i>";
                default:
                    throw new AssertionError("unknown format '" + ch2 + "'");
            }
        }

        private String getFormatEnd(char ch2) {
            if (ch2 == '\"') {
                return "”</font>";
            }
            if (ch2 == '*') {
                return "</b>";
            }
            switch (ch2) {
                case '^':
                    return "</font></b>";
                case '_':
                    return "</i>";
                default:
                    throw new AssertionError("unknown format '" + ch2 + "'");
            }
        }
    }

    public static class Html extends Token {
        private String html;

        public Html(String text, String html2) {
            super(Token.Type.HTML, text);
            this.html = html2;
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

        public Link(String url2, String text) {
            super(Token.Type.LINK, text);
            this.url = url2;
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

        public MusicTrack(String track2) {
            super(Token.Type.MUSIC, track2);
            this.track = track2;
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
        private ArrayList<Token> tokens = new ArrayList<>();

        public String getType(boolean isSend) {
            StringBuilder sb = new StringBuilder();
            sb.append(isSend ? "s" : "r");
            sb.append(getPartType());
            return sb.toString();
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
            return this.tokens.size() == 1 && this.tokens.get(0).isMedia();
        }

        public Token getMediaToken() {
            if (isMedia()) {
                return this.tokens.get(0);
            }
            return null;
        }

        public void add(Token token) {
            if (!isMedia()) {
                this.tokens.add(token);
                return;
            }
            throw new AssertionError("media ");
        }

        public void setMeText(String meText2) {
            this.meText = meText2;
        }

        public String getRawText() {
            StringBuilder buf = new StringBuilder();
            if (this.meText != null) {
                buf.append(this.meText);
            }
            for (int i = 0; i < this.tokens.size(); i++) {
                buf.append(this.tokens.get(i).getRawText());
            }
            return buf.toString();
        }

        public ArrayList<Token> getTokens() {
            return this.tokens;
        }
    }

    public static class Photo extends Token {
        private static final Pattern URL_PATTERN = Pattern.compile("http://picasaweb.google.com/([^/?#&]+)/+((?!searchbrowse)[^/?#&]+)(?:/|/photo)?(?:\\?[^#]*)?(?:#(.*))?");
        private String album;
        private String photo;
        private String user;

        public Photo(String user2, String album2, String photo2, String text) {
            super(Token.Type.PHOTO, text);
            this.user = user2;
            this.album = album2;
            this.photo = photo2;
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
                info.add(null);
            }
            return info;
        }

        public static String getRssUrl(String user2) {
            return "http://picasaweb.google.com/data/feed/api/user/" + user2 + "?category=album&alt=rss";
        }

        public static String getAlbumURL(String user2, String album2) {
            return "http://picasaweb.google.com/" + user2 + "/" + album2;
        }

        public static String getPhotoURL(String user2, String album2, String photo2) {
            return "http://picasaweb.google.com/" + user2 + "/" + album2 + "/photo#" + photo2;
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
            super(Token.Type.SMILEY, text);
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

    public static abstract class Token {
        protected String text;
        protected Type type;

        public enum Type {
            HTML("html"),
            FORMAT("format"),
            LINK("l"),
            SMILEY("e"),
            ACRONYM("a"),
            MUSIC("m"),
            GOOGLE_VIDEO("v"),
            YOUTUBE_VIDEO("yt"),
            PHOTO("p"),
            FLICKR("f");
            
            private String stringRep;

            private Type(String stringRep2) {
                this.stringRep = stringRep2;
            }

            public String toString() {
                return this.stringRep;
            }
        }

        public abstract boolean isHtml();

        protected Token(Type type2, String text2) {
            this.type = type2;
            this.text = text2;
        }

        public Type getType() {
            return this.type;
        }

        public List<String> getInfo() {
            List<String> info = new ArrayList<>();
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

    public static class TrieNode {
        private final HashMap<Character, TrieNode> children;
        private String text;
        private String value;

        public TrieNode() {
            this("");
        }

        public TrieNode(String text2) {
            this.children = new HashMap<>();
            this.text = text2;
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

        public void setValue(String value2) {
            this.value = value2;
        }

        public TrieNode getChild(char ch) {
            return this.children.get(Character.valueOf(ch));
        }

        public TrieNode getOrCreateChild(char ch) {
            Character key = Character.valueOf(ch);
            TrieNode node = this.children.get(key);
            if (node != null) {
                return node;
            }
            TrieNode node2 = new TrieNode(this.text + String.valueOf(ch));
            this.children.put(key, node2);
            return node2;
        }

        public static void addToTrie(TrieNode root, String str, String value2) {
            for (int index = 0; index < str.length(); index++) {
                root = root.getOrCreateChild(str.charAt(index));
            }
            root.setValue(value2);
        }
    }

    public static class Video extends Token {
        private static final Pattern URL_PATTERN = Pattern.compile("(?i)http://video\\.google\\.[a-z0-9]+(?:\\.[a-z0-9]+)?/videoplay\\?.*?\\bdocid=(-?\\d+).*");
        private String docid;

        public Video(String docid2, String text) {
            super(Token.Type.GOOGLE_VIDEO, text);
            this.docid = docid2;
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

        public static String getRssUrl(String docid2) {
            return "http://video.google.com/videofeed?type=docid&output=rss&sourceid=gtalk&docid=" + docid2;
        }

        public static String getURL(String docid2) {
            return getURL(docid2, null);
        }

        public static String getURL(String docid2, String extraParams) {
            if (extraParams == null) {
                extraParams = "";
            } else if (extraParams.length() > 0) {
                extraParams = extraParams + "&";
            }
            return "http://video.google.com/videoplay?" + extraParams + "docid=" + docid2;
        }
    }

    public static class YouTubeVideo extends Token {
        private static final Pattern URL_PATTERN = Pattern.compile("(?i)http://(?:[a-z0-9]+\\.)?youtube\\.[a-z0-9]+(?:\\.[a-z0-9]+)?/watch\\?.*\\bv=([-_a-zA-Z0-9=]+).*");
        private String docid;

        public YouTubeVideo(String docid2, String text) {
            super(Token.Type.YOUTUBE_VIDEO, text);
            this.docid = docid2;
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

        public static String getRssUrl(String docid2) {
            return "http://youtube.com/watch?v=" + docid2;
        }

        public static String getURL(String docid2) {
            return getURL(docid2, null);
        }

        public static String getURL(String docid2, String extraParams) {
            if (extraParams == null) {
                extraParams = "";
            } else if (extraParams.length() > 0) {
                extraParams = extraParams + "&";
            }
            return "http://youtube.com/watch?" + extraParams + "v=" + docid2;
        }

        public static String getPrefixedURL(boolean http, String prefix, String docid2, String extraParams) {
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
            return protocol + prefix + "youtube.com/watch?" + extraParams + "v=" + docid2;
        }
    }

    /* access modifiers changed from: protected */
    public abstract Resources getResources();

    public AbstractMessageParser(String text2) {
        this(text2, true, true, true, true, true, true);
    }

    public AbstractMessageParser(String text2, boolean parseSmilies2, boolean parseAcronyms2, boolean parseFormatting2, boolean parseUrls2, boolean parseMusic2, boolean parseMeText2) {
        this.text = text2;
        this.nextChar = 0;
        this.nextClass = 10;
        this.parts = new ArrayList<>();
        this.tokens = new ArrayList<>();
        this.formatStart = new HashMap<>();
        this.parseSmilies = parseSmilies2;
        this.parseAcronyms = parseAcronyms2;
        this.parseFormatting = parseFormatting2;
        this.parseUrls = parseUrls2;
        this.parseMusic = parseMusic2;
        this.parseMeText = parseMeText2;
    }

    public final String getRawText() {
        return this.text;
    }

    public final int getPartCount() {
        return this.parts.size();
    }

    public final Part getPart(int index) {
        return this.parts.get(index);
    }

    public final List<Part> getParts() {
        return this.parts;
    }

    public void parse() {
        if (parseMusicTrack()) {
            buildParts(null);
            return;
        }
        String meText = null;
        int i = 0;
        if (this.parseMeText && this.text.startsWith("/me") && this.text.length() > 3 && Character.isWhitespace(this.text.charAt(3))) {
            meText = this.text.substring(0, 4);
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
                if (!parseAcronym() && !parseURL() && !parseFormatting()) {
                    parseText();
                }
            }
        }
        for (int i2 = 0; i2 < this.tokens.size(); i2++) {
            if (this.tokens.get(i2).isMedia()) {
                if (i2 > 0 && (this.tokens.get(i2 - 1) instanceof Html)) {
                    ((Html) this.tokens.get(i2 - 1)).trimLeadingWhitespace();
                }
                if (i2 + 1 < this.tokens.size() && (this.tokens.get(i2 + 1) instanceof Html)) {
                    ((Html) this.tokens.get(i2 + 1)).trimTrailingWhitespace();
                }
            }
        }
        while (i < this.tokens.size()) {
            if (this.tokens.get(i).isHtml() && this.tokens.get(i).toHtml(true).length() == 0) {
                this.tokens.remove(i);
                i--;
            }
            i++;
        }
        buildParts(meText);
    }

    public static Token tokenForUrl(String url, String text2) {
        if (url == null) {
            return null;
        }
        Video video = Video.matchURL(url, text2);
        if (video != null) {
            return video;
        }
        YouTubeVideo ytVideo = YouTubeVideo.matchURL(url, text2);
        if (ytVideo != null) {
            return ytVideo;
        }
        Photo photo = Photo.matchURL(url, text2);
        if (photo != null) {
            return photo;
        }
        FlickrPhoto flickrPhoto = FlickrPhoto.matchURL(url, text2);
        if (flickrPhoto != null) {
            return flickrPhoto;
        }
        return new Link(url, text2);
    }

    private void buildParts(String meText) {
        for (int i = 0; i < this.tokens.size(); i++) {
            Token token = this.tokens.get(i);
            if (token.isMedia() || this.parts.size() == 0 || lastPart().isMedia()) {
                this.parts.add(new Part());
            }
            lastPart().add(token);
        }
        if (this.parts.size() > 0) {
            this.parts.get(0).setMeText(meText);
        }
    }

    private Part lastPart() {
        return this.parts.get(this.parts.size() - 1);
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
            if (ch == 10) {
                buf.append("<br>");
            } else if (ch == '\"') {
                buf.append("&quot;");
            } else if (ch == '<') {
                buf.append("&lt;");
            } else if (ch != '>') {
                switch (ch) {
                    case '&':
                        buf.append("&amp;");
                        break;
                    case '\'':
                        buf.append("&apos;");
                        break;
                    default:
                        buf.append(ch);
                        break;
                }
            } else {
                buf.append("&gt;");
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
        return c == '-' || Character.isLetter(c) || Character.isDigit(c);
    }

    private boolean isValidDomain(String domain) {
        if (matches(getResources().getDomainSuffixes(), reverse(domain))) {
            return true;
        }
        return false;
    }

    private boolean parseURL() {
        boolean done;
        if (!this.parseUrls || !isURLBreak(this.nextChar)) {
            return false;
        }
        int start = this.nextChar;
        int index = start;
        while (index < this.text.length() && isDomainChar(this.text.charAt(index))) {
            index++;
        }
        String url = "";
        boolean done2 = false;
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
            while (index < this.text.length()) {
                char ch = this.text.charAt(index);
                if (ch != '.' && !isDomainChar(ch)) {
                    break;
                }
                index++;
            }
            if (!isValidDomain(this.text.substring(this.nextChar, index))) {
                return false;
            }
            if (index + 1 < this.text.length() && this.text.charAt(index) == ':' && Character.isDigit(this.text.charAt(index + 1))) {
                while (true) {
                    index++;
                    if (index >= this.text.length() || !Character.isDigit(this.text.charAt(index))) {
                        break;
                    }
                }
            }
            if (index == this.text.length()) {
                done = true;
            } else {
                char ch2 = this.text.charAt(index);
                if (ch2 == '?') {
                    if (index + 1 == this.text.length()) {
                        done = true;
                    } else {
                        char ch22 = this.text.charAt(index + 1);
                        if (Character.isWhitespace(ch22) || isPunctuation(ch22)) {
                            done2 = true;
                        }
                        url = "http://";
                    }
                } else if (isPunctuation(ch2)) {
                    done = true;
                } else if (Character.isWhitespace(ch2)) {
                    done = true;
                } else {
                    if (!(ch2 == '/' || ch2 == '#')) {
                        return false;
                    }
                    url = "http://";
                }
            }
            done2 = done;
            url = "http://";
        }
        if (!done2) {
            while (index < this.text.length() && !Character.isWhitespace(this.text.charAt(index))) {
                index++;
            }
        }
        String urlText = this.text.substring(start, index);
        addURLToken(url + urlText, urlText);
        this.nextChar = index;
        return true;
    }

    private void addURLToken(String url, String text2) {
        addToken(tokenForUrl(url, text2));
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
        LinkedHashMap<Character, Boolean> seenCharacters = new LinkedHashMap<>();
        for (int index = this.nextChar; index < endChar; index++) {
            char ch = this.text.charAt(index);
            Character key = Character.valueOf(ch);
            if (seenCharacters.containsKey(key)) {
                addToken(new Format(ch, false));
            } else {
                Format start = this.formatStart.get(key);
                if (start != null) {
                    start.setMatched(true);
                    this.formatStart.remove(key);
                    seenCharacters.put(key, Boolean.TRUE);
                } else {
                    Format start2 = new Format(ch, true);
                    this.formatStart.put(key, start2);
                    addToken(start2);
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
            case 2:
            case 3:
            case 4:
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
        if (!(c1 == '$' || c1 == '&' || c1 == '-' || c1 == '/' || c1 == '@')) {
            switch (c1) {
                case '*':
                case '+':
                    break;
                default:
                    switch (c1) {
                        case '<':
                        case '=':
                        case '>':
                            break;
                        default:
                            switch (c1) {
                                case '[':
                                case '\\':
                                case ']':
                                case '^':
                                    break;
                                default:
                                    switch (c1) {
                                        case '|':
                                        case '}':
                                        case '~':
                                            break;
                                    }
                            }
                    }
            }
        }
        switch (c2) {
            case '#':
            case '$':
            case '%':
            case '*':
            case '/':
            case '<':
            case '=':
            case '>':
            case '@':
            case '[':
            case '\\':
            case '^':
            case '~':
                return true;
        }
        return false;
    }

    private static boolean isPunctuation(char ch) {
        switch (ch) {
            case '!':
            case '\"':
            case '(':
            case ')':
            case ',':
            case '.':
            case ':':
            case ';':
            case '?':
                return true;
            default:
                return false;
        }
    }

    private static boolean isFormatChar(char ch) {
        if (ch != '*') {
            switch (ch) {
                case '^':
                case '_':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private void addToken(Token token) {
        this.tokens.add(token);
    }

    public String toHtml() {
        StringBuilder html = new StringBuilder();
        Iterator<Part> it = this.parts.iterator();
        while (it.hasNext()) {
            boolean caps = false;
            html.append("<p>");
            Iterator<Token> it2 = it.next().getTokens().iterator();
            while (it2.hasNext()) {
                Token token = it2.next();
                if (token.isHtml()) {
                    html.append(token.toHtml(caps));
                } else {
                    switch (token.getType()) {
                        case LINK:
                            html.append("<a href=\"");
                            html.append(((Link) token).getURL());
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case SMILEY:
                            html.append(token.getRawText());
                            break;
                        case ACRONYM:
                            html.append(token.getRawText());
                            break;
                        case MUSIC:
                            html.append(((MusicTrack) token).getTrack());
                            break;
                        case GOOGLE_VIDEO:
                            html.append("<a href=\"");
                            Video video = (Video) token;
                            html.append(Video.getURL(((Video) token).getDocID()));
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case YOUTUBE_VIDEO:
                            html.append("<a href=\"");
                            YouTubeVideo youTubeVideo = (YouTubeVideo) token;
                            html.append(YouTubeVideo.getURL(((YouTubeVideo) token).getDocID()));
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case PHOTO:
                            html.append("<a href=\"");
                            html.append(Photo.getAlbumURL(((Photo) token).getUser(), ((Photo) token).getAlbum()));
                            html.append("\">");
                            html.append(token.getRawText());
                            html.append("</a>");
                            break;
                        case FLICKR:
                            html.append("<a href=\"");
                            html.append(((FlickrPhoto) token).getUrl());
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
        TrieNode root2 = root;
        int index = 0;
        while (true) {
            if (index >= str.length()) {
                break;
            }
            int index2 = index + 1;
            root2 = root2.getChild(str.charAt(index));
            if (root2 == null) {
                int i = index2;
                break;
            } else if (root2.exists()) {
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
        TrieNode bestMatch = null;
        while (true) {
            if (index >= p.getRawText().length()) {
                break;
            }
            int index2 = index + 1;
            root = root.getChild(p.getRawText().charAt(index));
            if (root == null) {
                int i = index2;
                break;
            }
            if (root.exists()) {
                if (p.isWordBreak(index2)) {
                    bestMatch = root;
                } else if (smiley && p.isSmileyBreak(index2)) {
                    bestMatch = root;
                }
            }
            index = index2;
        }
        return bestMatch;
    }
}
