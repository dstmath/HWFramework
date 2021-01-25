package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class IntRangeManager {
    private static final int INITIAL_CLIENTS_ARRAY_SIZE = 4;
    @UnsupportedAppUsage
    private ArrayList<IntRange> mRanges = new ArrayList<>();

    /* access modifiers changed from: protected */
    public abstract void addRange(int i, int i2, boolean z);

    /* access modifiers changed from: protected */
    public abstract boolean finishUpdate();

    /* access modifiers changed from: protected */
    public abstract void startUpdate();

    /* access modifiers changed from: private */
    public class IntRange {
        final ArrayList<ClientRange> mClients;
        int mEndId;
        int mStartId;

        IntRange(int startId, int endId, String client) {
            this.mStartId = startId;
            this.mEndId = endId;
            this.mClients = new ArrayList<>(4);
            this.mClients.add(new ClientRange(startId, endId, client));
        }

        IntRange(ClientRange clientRange) {
            this.mStartId = clientRange.mStartId;
            this.mEndId = clientRange.mEndId;
            this.mClients = new ArrayList<>(4);
            this.mClients.add(clientRange);
        }

        IntRange(IntRange intRange, int numElements) {
            this.mStartId = intRange.mStartId;
            this.mEndId = intRange.mEndId;
            this.mClients = new ArrayList<>(intRange.mClients.size());
            for (int i = 0; i < numElements; i++) {
                this.mClients.add(intRange.mClients.get(i));
            }
        }

        /* access modifiers changed from: package-private */
        public void insert(ClientRange range) {
            int len = this.mClients.size();
            int insert = -1;
            for (int i = 0; i < len; i++) {
                ClientRange nextRange = this.mClients.get(i);
                if (range.mStartId <= nextRange.mStartId) {
                    if (range.equals(nextRange)) {
                        return;
                    }
                    if (range.mStartId == nextRange.mStartId && range.mEndId > nextRange.mEndId) {
                        insert = i + 1;
                        if (insert >= len) {
                            break;
                        }
                    } else {
                        this.mClients.add(i, range);
                        return;
                    }
                }
            }
            if (insert == -1 || insert >= len) {
                this.mClients.add(range);
            } else {
                this.mClients.add(insert, range);
            }
        }
    }

    /* access modifiers changed from: private */
    public class ClientRange {
        final String mClient;
        final int mEndId;
        final int mStartId;

        ClientRange(int startId, int endId, String client) {
            this.mStartId = startId;
            this.mEndId = endId;
            this.mClient = client;
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof ClientRange)) {
                return false;
            }
            ClientRange other = (ClientRange) o;
            if (this.mStartId == other.mStartId && this.mEndId == other.mEndId && this.mClient.equals(other.mClient)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (((this.mStartId * 31) + this.mEndId) * 31) + this.mClient.hashCode();
        }
    }

    protected IntRangeManager() {
    }

    public synchronized boolean enableRange(int startId, int endId, String client) {
        int len = this.mRanges.size();
        if (len != 0) {
            for (int startIndex = 0; startIndex < len; startIndex++) {
                IntRange range = this.mRanges.get(startIndex);
                if (startId >= range.mStartId && endId <= range.mEndId) {
                    range.insert(new ClientRange(startId, endId, client));
                    return true;
                } else if (startId - 1 == range.mEndId) {
                    int newRangeEndId = endId;
                    IntRange nextRange = null;
                    if (startIndex + 1 < len) {
                        nextRange = this.mRanges.get(startIndex + 1);
                        if (nextRange.mStartId - 1 > endId) {
                            nextRange = null;
                        } else if (endId <= nextRange.mEndId) {
                            newRangeEndId = nextRange.mStartId - 1;
                        }
                    }
                    if (!tryAddRanges(startId, newRangeEndId, true)) {
                        return false;
                    }
                    range.mEndId = endId;
                    range.insert(new ClientRange(startId, endId, client));
                    if (nextRange != null) {
                        if (range.mEndId < nextRange.mEndId) {
                            range.mEndId = nextRange.mEndId;
                        }
                        range.mClients.addAll(nextRange.mClients);
                        this.mRanges.remove(nextRange);
                    }
                    return true;
                } else if (startId < range.mStartId) {
                    if (endId + 1 < range.mStartId) {
                        if (!tryAddRanges(startId, endId, true)) {
                            return false;
                        }
                        this.mRanges.add(startIndex, new IntRange(startId, endId, client));
                        return true;
                    } else if (endId > range.mEndId) {
                        for (int endIndex = startIndex + 1; endIndex < len; endIndex++) {
                            IntRange endRange = this.mRanges.get(endIndex);
                            if (endId + 1 < endRange.mStartId) {
                                if (!tryAddRanges(startId, endId, true)) {
                                    return false;
                                }
                                range.mStartId = startId;
                                range.mEndId = endId;
                                range.mClients.add(0, new ClientRange(startId, endId, client));
                                int joinIndex = startIndex + 1;
                                for (int i = joinIndex; i < endIndex; i++) {
                                    IntRange joinRange = this.mRanges.get(joinIndex);
                                    range.mClients.addAll(joinRange.mClients);
                                    this.mRanges.remove(joinRange);
                                }
                                return true;
                            } else if (endId <= endRange.mEndId) {
                                if (!tryAddRanges(startId, endRange.mStartId - 1, true)) {
                                    return false;
                                } else {
                                    range.mStartId = startId;
                                    range.mEndId = endRange.mEndId;
                                    range.mClients.add(0, new ClientRange(startId, endId, client));
                                    int joinIndex2 = startIndex + 1;
                                    for (int i2 = joinIndex2; i2 <= endIndex; i2++) {
                                        IntRange joinRange2 = this.mRanges.get(joinIndex2);
                                        range.mClients.addAll(joinRange2.mClients);
                                        this.mRanges.remove(joinRange2);
                                    }
                                    return true;
                                }
                            }
                        }
                        if (!tryAddRanges(startId, endId, true)) {
                            return false;
                        }
                        range.mStartId = startId;
                        range.mEndId = endId;
                        range.mClients.add(0, new ClientRange(startId, endId, client));
                        int joinIndex3 = startIndex + 1;
                        for (int i3 = joinIndex3; i3 < len; i3++) {
                            IntRange joinRange3 = this.mRanges.get(joinIndex3);
                            range.mClients.addAll(joinRange3.mClients);
                            this.mRanges.remove(joinRange3);
                        }
                        return true;
                    } else if (!tryAddRanges(startId, range.mStartId - 1, true)) {
                        return false;
                    } else {
                        range.mStartId = startId;
                        range.mClients.add(0, new ClientRange(startId, endId, client));
                        return true;
                    }
                } else if (startId + 1 <= range.mEndId) {
                    if (endId <= range.mEndId) {
                        range.insert(new ClientRange(startId, endId, client));
                        return true;
                    } else {
                        int endIndex2 = startIndex;
                        int testIndex = startIndex + 1;
                        while (testIndex < len && endId + 1 >= this.mRanges.get(testIndex).mStartId) {
                            endIndex2 = testIndex;
                            testIndex++;
                        }
                        if (endIndex2 != startIndex) {
                            IntRange endRange2 = this.mRanges.get(endIndex2);
                            if (!tryAddRanges(range.mEndId + 1, endId <= endRange2.mEndId ? endRange2.mStartId - 1 : endId, true)) {
                                return false;
                            }
                            range.mEndId = endId <= endRange2.mEndId ? endRange2.mEndId : endId;
                            range.insert(new ClientRange(startId, endId, client));
                            int joinIndex4 = startIndex + 1;
                            for (int i4 = joinIndex4; i4 <= endIndex2; i4++) {
                                IntRange joinRange4 = this.mRanges.get(joinIndex4);
                                range.mClients.addAll(joinRange4.mClients);
                                this.mRanges.remove(joinRange4);
                            }
                            return true;
                        } else if (!tryAddRanges(range.mEndId + 1, endId, true)) {
                            return false;
                        } else {
                            range.mEndId = endId;
                            range.insert(new ClientRange(startId, endId, client));
                            return true;
                        }
                    }
                }
            }
            if (!tryAddRanges(startId, endId, true)) {
                return false;
            }
            this.mRanges.add(new IntRange(startId, endId, client));
            return true;
        } else if (!tryAddRanges(startId, endId, true)) {
            return false;
        } else {
            this.mRanges.add(new IntRange(startId, endId, client));
            return true;
        }
    }

    public synchronized boolean disableRange(int startId, int endId, String client) {
        int len;
        int i = startId;
        String str = client;
        synchronized (this) {
            int len2 = this.mRanges.size();
            int i2 = 0;
            while (true) {
                boolean z = false;
                if (i2 >= len2) {
                    return false;
                }
                IntRange range = this.mRanges.get(i2);
                if (i < range.mStartId) {
                    return false;
                }
                if (endId <= range.mEndId) {
                    ArrayList<ClientRange> clients = range.mClients;
                    int crLength = clients.size();
                    boolean z2 = true;
                    if (crLength == 1) {
                        ClientRange cr = clients.get(0);
                        if (cr.mStartId != i || cr.mEndId != endId || !cr.mClient.equals(str)) {
                            return false;
                        }
                        this.mRanges.remove(i2);
                        if (updateRanges()) {
                            return true;
                        }
                        this.mRanges.add(i2, range);
                        return false;
                    }
                    int largestEndId = Integer.MIN_VALUE;
                    boolean updateStarted = false;
                    int crIndex = 0;
                    while (crIndex < crLength) {
                        ClientRange cr2 = clients.get(crIndex);
                        if (cr2.mStartId != i || cr2.mEndId != endId || !cr2.mClient.equals(str)) {
                            if (cr2.mEndId > largestEndId) {
                                largestEndId = cr2.mEndId;
                            }
                            crIndex++;
                            i = startId;
                            boolean z3 = z2 ? 1 : 0;
                            Object[] objArr = z2 ? 1 : 0;
                            Object[] objArr2 = z2 ? 1 : 0;
                            z2 = z3;
                            len2 = len2;
                            clients = clients;
                            z = false;
                            str = client;
                        } else if (crIndex != crLength - 1) {
                            IntRange rangeCopy = new IntRange(range, crIndex);
                            if (crIndex == 0) {
                                int i3 = z2 ? 1 : 0;
                                int i4 = z2 ? 1 : 0;
                                int i5 = z2 ? 1 : 0;
                                int nextStartId = clients.get(i3).mStartId;
                                if (nextStartId != range.mStartId) {
                                    updateStarted = true;
                                    rangeCopy.mStartId = nextStartId;
                                }
                                largestEndId = clients.get(1).mEndId;
                            }
                            ArrayList<IntRange> newRanges = new ArrayList<>();
                            IntRange currentRange = rangeCopy;
                            int nextIndex = crIndex + 1;
                            while (nextIndex < crLength) {
                                ClientRange nextCr = clients.get(nextIndex);
                                if (nextCr.mStartId > largestEndId + 1) {
                                    currentRange.mEndId = largestEndId;
                                    newRanges.add(currentRange);
                                    currentRange = new IntRange(nextCr);
                                    updateStarted = true;
                                } else {
                                    if (currentRange.mEndId < nextCr.mEndId) {
                                        currentRange.mEndId = nextCr.mEndId;
                                    }
                                    currentRange.mClients.add(nextCr);
                                }
                                if (nextCr.mEndId > largestEndId) {
                                    largestEndId = nextCr.mEndId;
                                }
                                nextIndex++;
                                len2 = len2;
                                clients = clients;
                            }
                            if (largestEndId < endId) {
                                updateStarted = true;
                                currentRange.mEndId = largestEndId;
                            }
                            newRanges.add(currentRange);
                            this.mRanges.remove(i2);
                            this.mRanges.addAll(i2, newRanges);
                            if (!updateStarted || updateRanges()) {
                                return true;
                            }
                            this.mRanges.removeAll(newRanges);
                            this.mRanges.add(i2, range);
                            return false;
                        } else if (range.mEndId == largestEndId) {
                            clients.remove(crIndex);
                            return z2;
                        } else {
                            clients.remove(crIndex);
                            range.mEndId = largestEndId;
                            if (updateRanges()) {
                                return z2;
                            }
                            clients.add(crIndex, cr2);
                            range.mEndId = cr2.mEndId;
                            return z;
                        }
                    }
                    len = len2;
                } else {
                    len = len2;
                }
                i2++;
                i = startId;
                str = client;
                len2 = len;
            }
        }
    }

    public boolean updateRanges() {
        startUpdate();
        populateAllRanges();
        return finishUpdate();
    }

    /* access modifiers changed from: protected */
    public boolean tryAddRanges(int startId, int endId, boolean selected) {
        startUpdate();
        populateAllRanges();
        addRange(startId, endId, selected);
        return finishUpdate();
    }

    public boolean isEmpty() {
        return this.mRanges.isEmpty();
    }

    private void populateAllRanges() {
        Iterator<IntRange> itr = this.mRanges.iterator();
        while (itr.hasNext()) {
            IntRange currRange = itr.next();
            addRange(currRange.mStartId, currRange.mEndId, true);
        }
    }

    private void populateAllClientRanges() {
        int len = this.mRanges.size();
        for (int i = 0; i < len; i++) {
            IntRange range = this.mRanges.get(i);
            int clientLen = range.mClients.size();
            for (int j = 0; j < clientLen; j++) {
                ClientRange nextRange = range.mClients.get(j);
                addRange(nextRange.mStartId, nextRange.mEndId, true);
            }
        }
    }
}
