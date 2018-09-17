package android.net.sip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SimpleSessionDescription {
    private final Fields mFields = new Fields("voscbtka");
    private final ArrayList<Media> mMedia = new ArrayList();

    private static class Fields {
        private final ArrayList<String> mLines = new ArrayList();
        private final String mOrder;

        Fields(String order) {
            this.mOrder = order;
        }

        public String getAddress() {
            String address = get("c", '=');
            if (address == null) {
                return null;
            }
            String[] parts = address.split(" ");
            if (parts.length != 3) {
                return null;
            }
            int slash = parts[2].indexOf(47);
            return slash < 0 ? parts[2] : parts[2].substring(0, slash);
        }

        public void setAddress(String address) {
            if (address != null) {
                address = (address.indexOf(58) < 0 ? "IN IP4 " : "IN IP6 ") + address;
            }
            set("c", '=', address);
        }

        public String getEncryptionMethod() {
            String encryption = get("k", '=');
            if (encryption == null) {
                return null;
            }
            int colon = encryption.indexOf(58);
            if (colon != -1) {
                encryption = encryption.substring(0, colon);
            }
            return encryption;
        }

        public String getEncryptionKey() {
            String str = null;
            String encryption = get("k", '=');
            if (encryption == null) {
                return null;
            }
            int colon = encryption.indexOf(58);
            if (colon != -1) {
                str = encryption.substring(0, colon + 1);
            }
            return str;
        }

        public void setEncryption(String method, String key) {
            String str = "k";
            if (!(method == null || key == null)) {
                method = method + ':' + key;
            }
            set(str, '=', method);
        }

        public String[] getBandwidthTypes() {
            return cut("b=", ':');
        }

        public int getBandwidth(String type) {
            String value = get("b=" + type, ':');
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    setBandwidth(type, -1);
                }
            }
            return -1;
        }

        public void setBandwidth(String type, int value) {
            set("b=" + type, ':', value < 0 ? null : String.valueOf(value));
        }

        public String[] getAttributeNames() {
            return cut("a=", ':');
        }

        public String getAttribute(String name) {
            return get("a=" + name, ':');
        }

        public void setAttribute(String name, String value) {
            set("a=" + name, ':', value);
        }

        private void write(StringBuilder buffer) {
            for (int i = 0; i < this.mOrder.length(); i++) {
                char type = this.mOrder.charAt(i);
                for (String line : this.mLines) {
                    if (line.charAt(0) == type) {
                        buffer.append(line).append("\r\n");
                    }
                }
            }
        }

        private void parse(String line) {
            char type = line.charAt(0);
            if (this.mOrder.indexOf(type) != -1) {
                char delimiter = '=';
                if (line.startsWith("a=rtpmap:") || line.startsWith("a=fmtp:")) {
                    delimiter = ' ';
                } else if (type == 'b' || type == 'a') {
                    delimiter = ':';
                }
                int i = line.indexOf(delimiter);
                if (i == -1) {
                    set(line, delimiter, "");
                } else {
                    set(line.substring(0, i), delimiter, line.substring(i + 1));
                }
            }
        }

        private String[] cut(String prefix, char delimiter) {
            String[] names = new String[this.mLines.size()];
            int length = 0;
            for (String line : this.mLines) {
                if (line.startsWith(prefix)) {
                    int i = line.indexOf(delimiter);
                    if (i == -1) {
                        i = line.length();
                    }
                    names[length] = line.substring(prefix.length(), i);
                    length++;
                }
            }
            return (String[]) Arrays.copyOf(names, length);
        }

        private int find(String key, char delimiter) {
            int length = key.length();
            for (int i = this.mLines.size() - 1; i >= 0; i--) {
                String line = (String) this.mLines.get(i);
                if (line.startsWith(key) && (line.length() == length || line.charAt(length) == delimiter)) {
                    return i;
                }
            }
            return -1;
        }

        private void set(String key, char delimiter, String value) {
            int index = find(key, delimiter);
            if (value != null) {
                if (value.length() != 0) {
                    key = key + delimiter + value;
                }
                if (index == -1) {
                    this.mLines.add(key);
                } else {
                    this.mLines.set(index, key);
                }
            } else if (index != -1) {
                this.mLines.remove(index);
            }
        }

        private String get(String key, char delimiter) {
            int index = find(key, delimiter);
            if (index == -1) {
                return null;
            }
            String line = (String) this.mLines.get(index);
            int length = key.length();
            return line.length() == length ? "" : line.substring(length + 1);
        }
    }

    public static class Media extends Fields {
        private ArrayList<String> mFormats;
        private final int mPort;
        private final int mPortCount;
        private final String mProtocol;
        private final String mType;

        /* synthetic */ Media(String type, int port, int portCount, String protocol, Media -this4) {
            this(type, port, portCount, protocol);
        }

        private Media(String type, int port, int portCount, String protocol) {
            super("icbka");
            this.mFormats = new ArrayList();
            this.mType = type;
            this.mPort = port;
            this.mPortCount = portCount;
            this.mProtocol = protocol;
        }

        public String getType() {
            return this.mType;
        }

        public int getPort() {
            return this.mPort;
        }

        public int getPortCount() {
            return this.mPortCount;
        }

        public String getProtocol() {
            return this.mProtocol;
        }

        public String[] getFormats() {
            return (String[]) this.mFormats.toArray(new String[this.mFormats.size()]);
        }

        public String getFmtp(String format) {
            return get("a=fmtp:" + format, ' ');
        }

        public void setFormat(String format, String fmtp) {
            this.mFormats.remove(format);
            this.mFormats.add(format);
            set("a=rtpmap:" + format, ' ', null);
            set("a=fmtp:" + format, ' ', fmtp);
        }

        public void removeFormat(String format) {
            this.mFormats.remove(format);
            set("a=rtpmap:" + format, ' ', null);
            set("a=fmtp:" + format, ' ', null);
        }

        public int[] getRtpPayloadTypes() {
            int[] types = new int[this.mFormats.size()];
            int length = 0;
            for (String format : this.mFormats) {
                try {
                    types[length] = Integer.parseInt(format);
                    length++;
                } catch (NumberFormatException e) {
                }
            }
            return Arrays.copyOf(types, length);
        }

        public String getRtpmap(int type) {
            return get("a=rtpmap:" + type, ' ');
        }

        public String getFmtp(int type) {
            return get("a=fmtp:" + type, ' ');
        }

        public void setRtpPayload(int type, String rtpmap, String fmtp) {
            String format = String.valueOf(type);
            this.mFormats.remove(format);
            this.mFormats.add(format);
            set("a=rtpmap:" + format, ' ', rtpmap);
            set("a=fmtp:" + format, ' ', fmtp);
        }

        public void removeRtpPayload(int type) {
            removeFormat(String.valueOf(type));
        }

        private void write(StringBuilder buffer) {
            buffer.append("m=").append(this.mType).append(' ').append(this.mPort);
            if (this.mPortCount != 1) {
                buffer.append('/').append(this.mPortCount);
            }
            buffer.append(' ').append(this.mProtocol);
            for (String format : this.mFormats) {
                buffer.append(' ').append(format);
            }
            buffer.append("\r\n");
            write(buffer);
        }
    }

    public SimpleSessionDescription(long sessionId, String address) {
        address = (address.indexOf(58) < 0 ? "IN IP4 " : "IN IP6 ") + address;
        this.mFields.parse("v=0");
        this.mFields.parse(String.format(Locale.US, "o=- %d %d %s", new Object[]{Long.valueOf(sessionId), Long.valueOf(System.currentTimeMillis()), address}));
        this.mFields.parse("s=-");
        this.mFields.parse("t=0 0");
        this.mFields.parse("c=" + address);
    }

    public SimpleSessionDescription(String message) {
        String[] lines = message.trim().replaceAll(" +", " ").split("[\r\n]+");
        Fields fields = this.mFields;
        int length = lines.length;
        int i = 0;
        while (i < length) {
            String line = lines[i];
            try {
                if (line.charAt(1) != '=') {
                    throw new IllegalArgumentException();
                }
                if (line.charAt(0) == 'm') {
                    String[] parts = line.substring(2).split(" ", 4);
                    String[] ports = parts[1].split("/", 2);
                    Fields media = newMedia(parts[0], Integer.parseInt(ports[0]), ports.length < 2 ? 1 : Integer.parseInt(ports[1]), parts[2]);
                    for (String format : parts[3].split(" ")) {
                        media.setFormat(format, null);
                    }
                    fields = media;
                } else {
                    fields.parse(line);
                }
                i++;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid SDP: " + line);
            }
        }
    }

    public Media newMedia(String type, int port, int portCount, String protocol) {
        Media media = new Media(type, port, portCount, protocol, null);
        this.mMedia.add(media);
        return media;
    }

    public Media[] getMedia() {
        return (Media[]) this.mMedia.toArray(new Media[this.mMedia.size()]);
    }

    public String encode() {
        StringBuilder buffer = new StringBuilder();
        this.mFields.write(buffer);
        for (Media media : this.mMedia) {
            media.write(buffer);
        }
        return buffer.toString();
    }

    public String getAddress() {
        return this.mFields.getAddress();
    }

    public void setAddress(String address) {
        this.mFields.setAddress(address);
    }

    public String getEncryptionMethod() {
        return this.mFields.getEncryptionMethod();
    }

    public String getEncryptionKey() {
        return this.mFields.getEncryptionKey();
    }

    public void setEncryption(String method, String key) {
        this.mFields.setEncryption(method, key);
    }

    public String[] getBandwidthTypes() {
        return this.mFields.getBandwidthTypes();
    }

    public int getBandwidth(String type) {
        return this.mFields.getBandwidth(type);
    }

    public void setBandwidth(String type, int value) {
        this.mFields.setBandwidth(type, value);
    }

    public String[] getAttributeNames() {
        return this.mFields.getAttributeNames();
    }

    public String getAttribute(String name) {
        return this.mFields.getAttribute(name);
    }

    public void setAttribute(String name, String value) {
        this.mFields.setAttribute(name, value);
    }
}
