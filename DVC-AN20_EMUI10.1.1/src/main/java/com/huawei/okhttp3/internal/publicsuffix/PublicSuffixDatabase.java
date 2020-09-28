package com.huawei.okhttp3.internal.publicsuffix;

import com.huawei.internal.telephony.PhoneConstantsEx;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.platform.Platform;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.GzipSource;
import com.huawei.okio.Okio;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.IDN;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PublicSuffixDatabase {
    private static final String[] EMPTY_RULE = new String[0];
    private static final byte EXCEPTION_MARKER = 33;
    private static final String[] PREVAILING_RULE = {PhoneConstantsEx.APN_TYPE_ALL};
    public static final String PUBLIC_SUFFIX_RESOURCE = "publicsuffixes.gz";
    private static final byte[] WILDCARD_LABEL = {42};
    private static final PublicSuffixDatabase instance = new PublicSuffixDatabase();
    private final AtomicBoolean listRead = new AtomicBoolean(false);
    private byte[] publicSuffixExceptionListBytes;
    private byte[] publicSuffixListBytes;
    private final CountDownLatch readCompleteLatch = new CountDownLatch(1);

    public static PublicSuffixDatabase get() {
        return instance;
    }

    public String getEffectiveTldPlusOne(String domain) {
        int firstLabelOffset;
        if (domain != null) {
            String[] domainLabels = IDN.toUnicode(domain).split("\\.");
            String[] rule = findMatchingRule(domainLabels);
            if (domainLabels.length == rule.length && rule[0].charAt(0) != '!') {
                return null;
            }
            if (rule[0].charAt(0) == '!') {
                firstLabelOffset = domainLabels.length - rule.length;
            } else {
                firstLabelOffset = domainLabels.length - (rule.length + 1);
            }
            StringBuilder effectiveTldPlusOne = new StringBuilder();
            String[] punycodeLabels = domain.split("\\.");
            for (int i = firstLabelOffset; i < punycodeLabels.length; i++) {
                effectiveTldPlusOne.append(punycodeLabels[i]);
                effectiveTldPlusOne.append('.');
            }
            effectiveTldPlusOne.deleteCharAt(effectiveTldPlusOne.length() - 1);
            return effectiveTldPlusOne.toString();
        }
        throw new NullPointerException("domain == null");
    }

    private String[] findMatchingRule(String[] domainLabels) {
        String[] exactRuleLabels;
        String[] wildcardRuleLabels;
        if (this.listRead.get() || !this.listRead.compareAndSet(false, true)) {
            try {
                this.readCompleteLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            readTheListUninterruptibly();
        }
        synchronized (this) {
            if (this.publicSuffixListBytes == null) {
                throw new IllegalStateException("Unable to load publicsuffixes.gz resource from the classpath.");
            }
        }
        byte[][] domainLabelsUtf8Bytes = new byte[domainLabels.length][];
        for (int i = 0; i < domainLabels.length; i++) {
            domainLabelsUtf8Bytes[i] = domainLabels[i].getBytes(Util.UTF_8);
        }
        String exactMatch = null;
        int i2 = 0;
        while (true) {
            if (i2 >= domainLabelsUtf8Bytes.length) {
                break;
            }
            String rule = binarySearchBytes(this.publicSuffixListBytes, domainLabelsUtf8Bytes, i2);
            if (rule != null) {
                exactMatch = rule;
                break;
            }
            i2++;
        }
        String wildcardMatch = null;
        if (domainLabelsUtf8Bytes.length > 1) {
            byte[][] labelsWithWildcard = (byte[][]) domainLabelsUtf8Bytes.clone();
            int labelIndex = 0;
            while (true) {
                if (labelIndex >= labelsWithWildcard.length - 1) {
                    break;
                }
                labelsWithWildcard[labelIndex] = WILDCARD_LABEL;
                String rule2 = binarySearchBytes(this.publicSuffixListBytes, labelsWithWildcard, labelIndex);
                if (rule2 != null) {
                    wildcardMatch = rule2;
                    break;
                }
                labelIndex++;
            }
        }
        String exception = null;
        if (wildcardMatch != null) {
            int labelIndex2 = 0;
            while (true) {
                if (labelIndex2 >= domainLabelsUtf8Bytes.length - 1) {
                    break;
                }
                String rule3 = binarySearchBytes(this.publicSuffixExceptionListBytes, domainLabelsUtf8Bytes, labelIndex2);
                if (rule3 != null) {
                    exception = rule3;
                    break;
                }
                labelIndex2++;
            }
        }
        if (exception != null) {
            return ("!" + exception).split("\\.");
        } else if (exactMatch == null && wildcardMatch == null) {
            return PREVAILING_RULE;
        } else {
            if (exactMatch != null) {
                exactRuleLabels = exactMatch.split("\\.");
            } else {
                exactRuleLabels = EMPTY_RULE;
            }
            if (wildcardMatch != null) {
                wildcardRuleLabels = wildcardMatch.split("\\.");
            } else {
                wildcardRuleLabels = EMPTY_RULE;
            }
            return exactRuleLabels.length > wildcardRuleLabels.length ? exactRuleLabels : wildcardRuleLabels;
        }
    }

    /* JADX INFO: Multiple debug info for r12v1 int: [D('low' int), D('publicSuffixBytesLeft' int)] */
    private static String binarySearchBytes(byte[] bytesToSearch, byte[][] labels, int labelIndex) {
        int byte0;
        int compareResult;
        int low;
        int low2 = 0;
        int high = bytesToSearch.length;
        while (low2 < high) {
            int mid = (low2 + high) / 2;
            while (mid > -1 && bytesToSearch[mid] != 10) {
                mid--;
            }
            int mid2 = mid + 1;
            int end = 1;
            while (bytesToSearch[mid2 + end] != 10) {
                end++;
            }
            int publicSuffixLength = (mid2 + end) - mid2;
            int currentLabelIndex = labelIndex;
            int currentLabelByteIndex = 0;
            int publicSuffixByteIndex = 0;
            boolean expectDot = false;
            while (true) {
                if (expectDot) {
                    byte0 = 46;
                    expectDot = false;
                } else {
                    byte0 = labels[currentLabelIndex][currentLabelByteIndex] & 255;
                }
                compareResult = byte0 - (bytesToSearch[mid2 + publicSuffixByteIndex] & 255);
                if (compareResult == 0) {
                    publicSuffixByteIndex++;
                    currentLabelByteIndex++;
                    if (publicSuffixByteIndex == publicSuffixLength) {
                        break;
                    }
                    if (labels[currentLabelIndex].length != currentLabelByteIndex) {
                        low = low2;
                    } else if (currentLabelIndex == labels.length - 1) {
                        break;
                    } else {
                        low = low2;
                        currentLabelIndex++;
                        expectDot = true;
                        currentLabelByteIndex = -1;
                    }
                    low2 = low;
                } else {
                    break;
                }
            }
            if (compareResult < 0) {
                high = mid2 - 1;
            } else if (compareResult > 0) {
                low2 = mid2 + end + 1;
            } else {
                int publicSuffixBytesLeft = publicSuffixLength - publicSuffixByteIndex;
                int labelBytesLeft = labels[currentLabelIndex].length - currentLabelByteIndex;
                int i = currentLabelIndex + 1;
                while (i < labels.length) {
                    labelBytesLeft += labels[i].length;
                    i++;
                    low2 = low2;
                }
                if (labelBytesLeft < publicSuffixBytesLeft) {
                    high = mid2 - 1;
                    low2 = low2;
                } else if (labelBytesLeft <= publicSuffixBytesLeft) {
                    return new String(bytesToSearch, mid2, publicSuffixLength, Util.UTF_8);
                } else {
                    low2 = mid2 + end + 1;
                }
            }
        }
        return null;
    }

    private void readTheListUninterruptibly() {
        boolean interrupted = false;
        while (true) {
            try {
                readTheList();
                break;
            } catch (InterruptedIOException e) {
                Thread.interrupted();
                interrupted = true;
            } catch (IOException e2) {
                Platform.get().log(5, "Failed to read public suffix list", e2);
                if (interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
                return;
            } catch (Throwable th) {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                throw th;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void readTheList() throws IOException {
        InputStream resource = PublicSuffixDatabase.class.getResourceAsStream(PUBLIC_SUFFIX_RESOURCE);
        if (resource != null) {
            BufferedSource bufferedSource = Okio.buffer(new GzipSource(Okio.source(resource)));
            try {
                byte[] publicSuffixListBytes2 = new byte[bufferedSource.readInt()];
                bufferedSource.readFully(publicSuffixListBytes2);
                byte[] publicSuffixExceptionListBytes2 = new byte[bufferedSource.readInt()];
                bufferedSource.readFully(publicSuffixExceptionListBytes2);
                synchronized (this) {
                    this.publicSuffixListBytes = publicSuffixListBytes2;
                    this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes2;
                }
                this.readCompleteLatch.countDown();
            } finally {
                Util.closeQuietly(bufferedSource);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setListBytes(byte[] publicSuffixListBytes2, byte[] publicSuffixExceptionListBytes2) {
        this.publicSuffixListBytes = publicSuffixListBytes2;
        this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes2;
        this.listRead.set(true);
        this.readCompleteLatch.countDown();
    }
}
