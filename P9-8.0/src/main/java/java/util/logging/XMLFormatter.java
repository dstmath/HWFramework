package java.util.logging;

import java.nio.charset.Charset;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

public class XMLFormatter extends Formatter {
    private LogManager manager = LogManager.getLogManager();

    private void a2(StringBuilder sb, int x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }

    private void appendISO8601(StringBuilder sb, long millis) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        sb.append(cal.get(1));
        sb.append('-');
        a2(sb, cal.get(2) + 1);
        sb.append('-');
        a2(sb, cal.get(5));
        sb.append('T');
        a2(sb, cal.get(11));
        sb.append(':');
        a2(sb, cal.get(12));
        sb.append(':');
        a2(sb, cal.get(13));
    }

    private void escape(StringBuilder sb, String text) {
        if (text == null) {
            text = "<null>";
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else if (ch == '&') {
                sb.append("&amp;");
            } else {
                sb.append(ch);
            }
        }
    }

    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<record>\n");
        sb.append("  <date>");
        appendISO8601(sb, record.getMillis());
        sb.append("</date>\n");
        sb.append("  <millis>");
        sb.append(record.getMillis());
        sb.append("</millis>\n");
        sb.append("  <sequence>");
        sb.append(record.getSequenceNumber());
        sb.append("</sequence>\n");
        String name = record.getLoggerName();
        if (name != null) {
            sb.append("  <logger>");
            escape(sb, name);
            sb.append("</logger>\n");
        }
        sb.append("  <level>");
        escape(sb, record.getLevel().toString());
        sb.append("</level>\n");
        if (record.getSourceClassName() != null) {
            sb.append("  <class>");
            escape(sb, record.getSourceClassName());
            sb.append("</class>\n");
        }
        if (record.getSourceMethodName() != null) {
            sb.append("  <method>");
            escape(sb, record.getSourceMethodName());
            sb.append("</method>\n");
        }
        sb.append("  <thread>");
        sb.append(record.getThreadID());
        sb.append("</thread>\n");
        if (record.getMessage() != null) {
            String message = formatMessage(record);
            sb.append("  <message>");
            escape(sb, message);
            sb.append("</message>");
            sb.append("\n");
        } else {
            sb.append("<message/>");
            sb.append("\n");
        }
        ResourceBundle bundle = record.getResourceBundle();
        if (bundle != null) {
            try {
                if (bundle.getString(record.getMessage()) != null) {
                    sb.append("  <key>");
                    escape(sb, record.getMessage());
                    sb.append("</key>\n");
                    sb.append("  <catalog>");
                    escape(sb, record.getResourceBundleName());
                    sb.append("</catalog>\n");
                }
            } catch (Exception e) {
            }
        }
        Object[] parameters = record.getParameters();
        if (!(parameters == null || parameters.length == 0 || record.getMessage().indexOf("{") != -1)) {
            for (Object obj : parameters) {
                sb.append("  <param>");
                try {
                    escape(sb, obj.toString());
                } catch (Exception e2) {
                    sb.append("???");
                }
                sb.append("</param>\n");
            }
        }
        if (record.getThrown() != null) {
            Throwable th = record.getThrown();
            sb.append("  <exception>\n");
            sb.append("    <message>");
            escape(sb, th.toString());
            sb.append("</message>\n");
            StackTraceElement[] trace = th.getStackTrace();
            for (StackTraceElement frame : trace) {
                sb.append("    <frame>\n");
                sb.append("      <class>");
                escape(sb, frame.getClassName());
                sb.append("</class>\n");
                sb.append("      <method>");
                escape(sb, frame.getMethodName());
                sb.append("</method>\n");
                if (frame.getLineNumber() >= 0) {
                    sb.append("      <line>");
                    sb.append(frame.getLineNumber());
                    sb.append("</line>\n");
                }
                sb.append("    </frame>\n");
            }
            sb.append("  </exception>\n");
        }
        sb.append("</record>\n");
        return sb.toString();
    }

    public String getHead(Handler h) {
        String encoding;
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"");
        if (h != null) {
            encoding = h.getEncoding();
        } else {
            encoding = null;
        }
        if (encoding == null) {
            encoding = Charset.defaultCharset().name();
        }
        try {
            encoding = Charset.forName(encoding).name();
        } catch (Exception e) {
        }
        sb.append(" encoding=\"");
        sb.append(encoding);
        sb.append("\"");
        sb.append(" standalone=\"no\"?>\n");
        sb.append("<!DOCTYPE log SYSTEM \"logger.dtd\">\n");
        sb.append("<log>\n");
        return sb.toString();
    }

    public String getTail(Handler h) {
        return "</log>\n";
    }
}
