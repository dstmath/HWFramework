package android.media;

import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/* compiled from: TtmlRenderer */
class TtmlParser {
    private static final int DEFAULT_FRAMERATE = 30;
    private static final int DEFAULT_SUBFRAMERATE = 1;
    private static final int DEFAULT_TICKRATE = 1;
    static final String TAG = "TtmlParser";
    private long mCurrentRunId;
    private final TtmlNodeListener mListener;
    private XmlPullParser mParser;

    public TtmlParser(TtmlNodeListener listener) {
        this.mListener = listener;
    }

    public void parse(String ttmlText, long runId) throws XmlPullParserException, IOException {
        this.mParser = null;
        this.mCurrentRunId = runId;
        loadParser(ttmlText);
        parseTtml();
    }

    private void loadParser(String ttmlFragment) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        this.mParser = factory.newPullParser();
        this.mParser.setInput(new StringReader(ttmlFragment));
    }

    private void extractAttribute(XmlPullParser parser, int i, StringBuilder out) {
        out.append(" ");
        out.append(parser.getAttributeName(i));
        out.append("=\"");
        out.append(parser.getAttributeValue(i));
        out.append("\"");
    }

    private void parseTtml() throws XmlPullParserException, IOException {
        LinkedList<TtmlNode> nodeStack = new LinkedList<>();
        int depthInUnsupportedTag = 0;
        boolean active = true;
        while (!isEndOfDoc()) {
            int eventType = this.mParser.getEventType();
            TtmlNode parent = nodeStack.peekLast();
            if (active) {
                if (eventType == 2) {
                    if (!isSupportedTag(this.mParser.getName())) {
                        Log.w(TAG, "Unsupported tag " + this.mParser.getName() + " is ignored.");
                        depthInUnsupportedTag++;
                        active = false;
                    } else {
                        TtmlNode node = parseNode(parent);
                        nodeStack.addLast(node);
                        if (parent != null) {
                            parent.mChildren.add(node);
                        }
                    }
                } else if (eventType == 4) {
                    String text = TtmlUtils.applyDefaultSpacePolicy(this.mParser.getText());
                    if (!TextUtils.isEmpty(text)) {
                        List<TtmlNode> list = parent.mChildren;
                        TtmlNode ttmlNode = r6;
                        String str = text;
                        TtmlNode ttmlNode2 = new TtmlNode(TtmlUtils.PCDATA, "", text, 0, Long.MAX_VALUE, parent, this.mCurrentRunId);
                        list.add(ttmlNode);
                    }
                } else if (eventType == 3) {
                    if (this.mParser.getName().equals(TtmlUtils.TAG_P)) {
                        this.mListener.onTtmlNodeParsed(nodeStack.getLast());
                    } else if (this.mParser.getName().equals(TtmlUtils.TAG_TT)) {
                        this.mListener.onRootNodeParsed(nodeStack.getLast());
                    }
                    nodeStack.removeLast();
                }
            } else if (eventType == 2) {
                depthInUnsupportedTag++;
            } else if (eventType == 3) {
                depthInUnsupportedTag--;
                if (depthInUnsupportedTag == 0) {
                    active = true;
                }
            }
            this.mParser.next();
        }
    }

    private TtmlNode parseNode(TtmlNode parent) throws XmlPullParserException, IOException {
        long end;
        TtmlNode ttmlNode = parent;
        if (this.mParser.getEventType() != 2) {
            return null;
        }
        StringBuilder attrStr = new StringBuilder();
        long start = 0;
        long end2 = Long.MAX_VALUE;
        int i = 0;
        long dur = 0;
        while (true) {
            int i2 = i;
            if (i2 >= this.mParser.getAttributeCount()) {
                break;
            }
            String attr = this.mParser.getAttributeName(i2);
            String value = this.mParser.getAttributeValue(i2);
            String attr2 = attr.replaceFirst("^.*:", "");
            if (attr2.equals(TtmlUtils.ATTR_BEGIN)) {
                start = TtmlUtils.parseTimeExpression(value, 30, 1, 1);
            } else if (attr2.equals(TtmlUtils.ATTR_END)) {
                end2 = TtmlUtils.parseTimeExpression(value, 30, 1, 1);
            } else if (attr2.equals(TtmlUtils.ATTR_DURATION)) {
                dur = TtmlUtils.parseTimeExpression(value, 30, 1, 1);
            } else {
                extractAttribute(this.mParser, i2, attrStr);
            }
            i = i2 + 1;
        }
        if (ttmlNode != null) {
            start += ttmlNode.mStartTimeMs;
            if (end2 != Long.MAX_VALUE) {
                end2 += ttmlNode.mStartTimeMs;
            }
        }
        long start2 = start;
        if (dur > 0) {
            if (end2 != Long.MAX_VALUE) {
                Log.e(TAG, "'dur' and 'end' attributes are defined at the same time.'end' value is ignored.");
            }
            end2 = start2 + dur;
        }
        if (ttmlNode == null || end2 != Long.MAX_VALUE || ttmlNode.mEndTimeMs == Long.MAX_VALUE || end2 <= ttmlNode.mEndTimeMs) {
            end = end2;
        } else {
            end = ttmlNode.mEndTimeMs;
        }
        TtmlNode node = new TtmlNode(this.mParser.getName(), attrStr.toString(), null, start2, end, ttmlNode, this.mCurrentRunId);
        return node;
    }

    private boolean isEndOfDoc() throws XmlPullParserException {
        return this.mParser.getEventType() == 1;
    }

    private static boolean isSupportedTag(String tag) {
        if (tag.equals(TtmlUtils.TAG_TT) || tag.equals(TtmlUtils.TAG_HEAD) || tag.equals(TtmlUtils.TAG_BODY) || tag.equals(TtmlUtils.TAG_DIV) || tag.equals(TtmlUtils.TAG_P) || tag.equals(TtmlUtils.TAG_SPAN) || tag.equals(TtmlUtils.TAG_BR) || tag.equals("style") || tag.equals(TtmlUtils.TAG_STYLING) || tag.equals("layout") || tag.equals(TtmlUtils.TAG_REGION) || tag.equals(TtmlUtils.TAG_METADATA) || tag.equals(TtmlUtils.TAG_SMPTE_IMAGE) || tag.equals(TtmlUtils.TAG_SMPTE_DATA) || tag.equals(TtmlUtils.TAG_SMPTE_INFORMATION)) {
            return true;
        }
        return false;
    }
}
