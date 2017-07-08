package gov.nist.javax.sip.message;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;

public class MultipartMimeContentImpl implements MultipartMimeContent {
    public static String BOUNDARY;
    private String boundary;
    private List<Content> contentList;
    private ContentTypeHeader multipartMimeContentTypeHeader;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.message.MultipartMimeContentImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.message.MultipartMimeContentImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MultipartMimeContentImpl.<clinit>():void");
    }

    public MultipartMimeContentImpl(ContentTypeHeader contentTypeHeader) {
        this.contentList = new LinkedList();
        this.multipartMimeContentTypeHeader = contentTypeHeader;
        this.boundary = contentTypeHeader.getParameter(BOUNDARY);
    }

    public boolean add(Content content) {
        return this.contentList.add((ContentImpl) content);
    }

    public ContentTypeHeader getContentTypeHeader() {
        return this.multipartMimeContentTypeHeader;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Content content : this.contentList) {
            stringBuffer.append(content.toString());
        }
        return stringBuffer.toString();
    }

    public void createContentList(String body) throws ParseException {
        try {
            HeaderFactoryExt headerFactory = new HeaderFactoryImpl();
            String delimiter = getContentTypeHeader().getParameter(BOUNDARY);
            ContentImpl content;
            if (delimiter == null) {
                this.contentList = new LinkedList();
                content = new ContentImpl(body, delimiter);
                content.setContentTypeHeader(getContentTypeHeader());
                this.contentList.add(content);
                return;
            }
            String[] fragments = body.split("--" + delimiter + Separators.NEWLINE);
            int length = fragments.length;
            int i = 0;
            while (i < length) {
                String nextPart = fragments[i];
                if (nextPart != null) {
                    StringBuffer strbuf = new StringBuffer(nextPart);
                    while (strbuf.length() > 0 && (strbuf.charAt(0) == '\r' || strbuf.charAt(0) == '\n')) {
                        strbuf.deleteCharAt(0);
                    }
                    if (strbuf.length() != 0) {
                        nextPart = strbuf.toString();
                        int position = nextPart.indexOf("\r\n\r\n");
                        int off = 4;
                        if (position == -1) {
                            position = nextPart.indexOf(Separators.RETURN);
                            off = 2;
                        }
                        if (position == -1) {
                            throw new ParseException("no content type header found in " + nextPart, 0);
                        }
                        String rest = nextPart.substring(position + off);
                        if (rest == null) {
                            throw new ParseException("No content [" + nextPart + "]", 0);
                        }
                        String headers = nextPart.substring(0, position);
                        content = new ContentImpl(rest, this.boundary);
                        for (String hdr : headers.split(Separators.NEWLINE)) {
                            Header header = headerFactory.createHeader(hdr);
                            if (header instanceof ContentTypeHeader) {
                                content.setContentTypeHeader((ContentTypeHeader) header);
                            } else if (header instanceof ContentDispositionHeader) {
                                content.setContentDispositionHeader((ContentDispositionHeader) header);
                            } else {
                                throw new ParseException("Unexpected header type " + header.getName(), 0);
                            }
                            this.contentList.add(content);
                        }
                        continue;
                    }
                    i++;
                } else {
                    return;
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParseException("Invalid Multipart mime format", 0);
        }
    }

    public Content getContentByType(String contentType, String contentSubtype) {
        Content retval = null;
        if (this.contentList == null) {
            return null;
        }
        for (Content content : this.contentList) {
            if (content.getContentTypeHeader().getContentType().equalsIgnoreCase(contentType) && content.getContentTypeHeader().getContentSubType().equalsIgnoreCase(contentSubtype)) {
                retval = content;
                break;
            }
        }
        return retval;
    }

    public void addContent(Content content) {
        add(content);
    }

    public Iterator<Content> getContents() {
        return this.contentList.iterator();
    }

    public int getContentCount() {
        return this.contentList.size();
    }
}
