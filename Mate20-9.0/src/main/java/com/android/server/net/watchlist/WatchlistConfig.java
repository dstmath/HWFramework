package com.android.server.net.watchlist;

import android.os.FileUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.HexDump;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class WatchlistConfig {
    private static final String NETWORK_WATCHLIST_DB_FOR_TEST_PATH = "/data/misc/network_watchlist/network_watchlist_for_test.xml";
    private static final String NETWORK_WATCHLIST_DB_PATH = "/data/misc/network_watchlist/network_watchlist.xml";
    private static final String TAG = "WatchlistConfig";
    private static final WatchlistConfig sInstance = new WatchlistConfig();
    private volatile CrcShaDigests mDomainDigests;
    private volatile CrcShaDigests mIpDigests;
    private boolean mIsSecureConfig;
    private File mXmlFile;

    private static class CrcShaDigests {
        final HarmfulDigests crc32Digests;
        final HarmfulDigests sha256Digests;

        public CrcShaDigests(HarmfulDigests crc32Digests2, HarmfulDigests sha256Digests2) {
            this.crc32Digests = crc32Digests2;
            this.sha256Digests = sha256Digests2;
        }
    }

    private static class XmlTags {
        private static final String CRC32_DOMAIN = "crc32-domain";
        private static final String CRC32_IP = "crc32-ip";
        private static final String HASH = "hash";
        private static final String SHA256_DOMAIN = "sha256-domain";
        private static final String SHA256_IP = "sha256-ip";
        private static final String WATCHLIST_CONFIG = "watchlist-config";

        private XmlTags() {
        }
    }

    public static WatchlistConfig getInstance() {
        return sInstance;
    }

    private WatchlistConfig() {
        this(new File(NETWORK_WATCHLIST_DB_PATH));
    }

    @VisibleForTesting
    protected WatchlistConfig(File xmlFile) {
        this.mIsSecureConfig = true;
        this.mXmlFile = xmlFile;
        reloadConfig();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00fa, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0103, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0104, code lost:
        android.util.Slog.e(TAG, "Failed parsing xml", r0);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008e A[Catch:{ all -> 0x00f0, IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0091 A[Catch:{ all -> 0x00f0, IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0095 A[Catch:{ all -> 0x00f0, IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0099 A[Catch:{ all -> 0x00f0, IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009d A[Catch:{ all -> 0x00f0, IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0103 A[ExcHandler: IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException (r0v2 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:3:0x0009] */
    public void reloadConfig() {
        FileInputStream stream;
        if (this.mXmlFile.exists()) {
            try {
                stream = new FileInputStream(this.mXmlFile);
                List<byte[]> crc32DomainList = new ArrayList<>();
                List<byte[]> sha256DomainList = new ArrayList<>();
                List<byte[]> crc32IpList = new ArrayList<>();
                List<byte[]> sha256IpList = new ArrayList<>();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                parser.nextTag();
                parser.require(2, null, "watchlist-config");
                while (true) {
                    char c = 3;
                    if (parser.nextTag() == 2) {
                        String tagName = parser.getName();
                        int hashCode = tagName.hashCode();
                        if (hashCode == -1862636386) {
                            if (tagName.equals("crc32-domain")) {
                                c = 0;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == -14835926) {
                            if (tagName.equals("sha256-domain")) {
                                c = 2;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == 835385997) {
                            if (tagName.equals("sha256-ip")) {
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == 1718657537) {
                            if (tagName.equals("crc32-ip")) {
                                c = 1;
                                switch (c) {
                                    case 0:
                                        parseHashes(parser, tagName, crc32DomainList);
                                        break;
                                    case 1:
                                        parseHashes(parser, tagName, crc32IpList);
                                        break;
                                    case 2:
                                        parseHashes(parser, tagName, sha256DomainList);
                                        break;
                                    case 3:
                                        parseHashes(parser, tagName, sha256IpList);
                                        break;
                                    default:
                                        Log.w(TAG, "Unknown element: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                        break;
                                }
                            }
                        }
                        c = 65535;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                    } else {
                        parser.require(3, null, "watchlist-config");
                        this.mDomainDigests = new CrcShaDigests(new HarmfulDigests(crc32DomainList), new HarmfulDigests(sha256DomainList));
                        this.mIpDigests = new CrcShaDigests(new HarmfulDigests(crc32IpList), new HarmfulDigests(sha256IpList));
                        Log.i(TAG, "Reload watchlist done");
                        stream.close();
                        return;
                    }
                }
            } catch (IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e) {
            } catch (Throwable th) {
                if (r1 != null) {
                    stream.close();
                } else {
                    stream.close();
                }
                throw th;
            }
        }
    }

    private void parseHashes(XmlPullParser parser, String tagName, List<byte[]> hashList) throws IOException, XmlPullParserException {
        parser.require(2, null, tagName);
        while (parser.nextTag() == 2) {
            parser.require(2, null, "hash");
            byte[] hash = HexDump.hexStringToByteArray(parser.nextText());
            parser.require(3, null, "hash");
            hashList.add(hash);
        }
        parser.require(3, null, tagName);
    }

    public boolean containsDomain(String domain) {
        CrcShaDigests domainDigests = this.mDomainDigests;
        if (domainDigests == null) {
            return false;
        }
        if (!domainDigests.crc32Digests.contains(getCrc32(domain))) {
            return false;
        }
        return domainDigests.sha256Digests.contains(getSha256(domain));
    }

    public boolean containsIp(String ip) {
        CrcShaDigests ipDigests = this.mIpDigests;
        if (ipDigests == null) {
            return false;
        }
        if (!ipDigests.crc32Digests.contains(getCrc32(ip))) {
            return false;
        }
        return ipDigests.sha256Digests.contains(getSha256(ip));
    }

    private byte[] getCrc32(String str) {
        CRC32 crc = new CRC32();
        crc.update(str.getBytes());
        long tmp = crc.getValue();
        return new byte[]{(byte) ((int) ((tmp >> 24) & 255)), (byte) ((int) ((tmp >> 16) & 255)), (byte) ((int) ((tmp >> 8) & 255)), (byte) ((int) (tmp & 255))};
    }

    private byte[] getSha256(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            messageDigest.update(str.getBytes());
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public boolean isConfigSecure() {
        return this.mIsSecureConfig;
    }

    public byte[] getWatchlistConfigHash() {
        if (!this.mXmlFile.exists()) {
            return null;
        }
        try {
            return DigestUtils.getSha256Hash(this.mXmlFile);
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e(TAG, "Unable to get watchlist config hash", e);
            return null;
        }
    }

    public void setTestMode(InputStream testConfigInputStream) throws IOException {
        Log.i(TAG, "Setting watchlist testing config");
        FileUtils.copyToFileOrThrow(testConfigInputStream, new File(NETWORK_WATCHLIST_DB_FOR_TEST_PATH));
        this.mIsSecureConfig = false;
        this.mXmlFile = new File(NETWORK_WATCHLIST_DB_FOR_TEST_PATH);
        reloadConfig();
    }

    public void removeTestModeConfig() {
        try {
            File f = new File(NETWORK_WATCHLIST_DB_FOR_TEST_PATH);
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to delete test config");
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        byte[] hash = getWatchlistConfigHash();
        StringBuilder sb = new StringBuilder();
        sb.append("Watchlist config hash: ");
        sb.append(hash != null ? HexDump.toHexString(hash) : null);
        pw.println(sb.toString());
        pw.println("Domain CRC32 digest list:");
        if (this.mDomainDigests != null) {
            this.mDomainDigests.crc32Digests.dump(fd, pw, args);
        }
        pw.println("Domain SHA256 digest list:");
        if (this.mDomainDigests != null) {
            this.mDomainDigests.sha256Digests.dump(fd, pw, args);
        }
        pw.println("Ip CRC32 digest list:");
        if (this.mIpDigests != null) {
            this.mIpDigests.crc32Digests.dump(fd, pw, args);
        }
        pw.println("Ip SHA256 digest list:");
        if (this.mIpDigests != null) {
            this.mIpDigests.sha256Digests.dump(fd, pw, args);
        }
    }
}
