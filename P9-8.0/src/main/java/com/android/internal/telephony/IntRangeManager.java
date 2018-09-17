package com.android.internal.telephony;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class IntRangeManager {
    private static final int INITIAL_CLIENTS_ARRAY_SIZE = 4;
    private ArrayList<IntRange> mRanges = new ArrayList();

    private class ClientRange {
        final String mClient;
        final int mEndId;
        final int mStartId;

        ClientRange(int startId, int endId, String client) {
            this.mStartId = startId;
            this.mEndId = endId;
            this.mClient = client;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == null || !(o instanceof ClientRange)) {
                return false;
            }
            ClientRange other = (ClientRange) o;
            if (this.mStartId == other.mStartId && this.mEndId == other.mEndId) {
                z = this.mClient.equals(other.mClient);
            }
            return z;
        }

        public int hashCode() {
            return (((this.mStartId * 31) + this.mEndId) * 31) + this.mClient.hashCode();
        }
    }

    private class IntRange {
        final ArrayList<ClientRange> mClients;
        int mEndId;
        int mStartId;

        IntRange(int startId, int endId, String client) {
            this.mStartId = startId;
            this.mEndId = endId;
            this.mClients = new ArrayList(4);
            this.mClients.add(new ClientRange(startId, endId, client));
        }

        IntRange(ClientRange clientRange) {
            this.mStartId = clientRange.mStartId;
            this.mEndId = clientRange.mEndId;
            this.mClients = new ArrayList(4);
            this.mClients.add(clientRange);
        }

        IntRange(IntRange intRange, int numElements) {
            this.mStartId = intRange.mStartId;
            this.mEndId = intRange.mEndId;
            this.mClients = new ArrayList(intRange.mClients.size());
            for (int i = 0; i < numElements; i++) {
                this.mClients.add((ClientRange) intRange.mClients.get(i));
            }
        }

        void insert(ClientRange range) {
            int len = this.mClients.size();
            int insert = -1;
            for (int i = 0; i < len; i++) {
                ClientRange nextRange = (ClientRange) this.mClients.get(i);
                if (range.mStartId <= nextRange.mStartId) {
                    if (!range.equals(nextRange)) {
                        if (range.mStartId == nextRange.mStartId && range.mEndId > nextRange.mEndId) {
                            insert = i + 1;
                            if (insert >= len) {
                                break;
                            }
                        } else {
                            this.mClients.add(i, range);
                        }
                    }
                    return;
                }
            }
            if (insert == -1 || insert >= len) {
                this.mClients.add(range);
            } else {
                this.mClients.add(insert, range);
            }
        }
    }

    protected abstract void addRange(int i, int i2, boolean z);

    protected abstract boolean finishUpdate();

    protected abstract void startUpdate();

    protected IntRangeManager() {
    }

    public synchronized boolean enableRange(int startId, int endId, String client) {
        int len = this.mRanges.size();
        if (len != 0) {
            int startIndex = 0;
            while (startIndex < len) {
                IntRange range = (IntRange) this.mRanges.get(startIndex);
                if (startId < range.mStartId || endId > range.mEndId) {
                    if (startId - 1 == range.mEndId) {
                        int newRangeEndId = endId;
                        IntRange nextRange = null;
                        if (startIndex + 1 < len) {
                            nextRange = (IntRange) this.mRanges.get(startIndex + 1);
                            if (nextRange.mStartId - 1 <= endId) {
                                if (endId <= nextRange.mEndId) {
                                    newRangeEndId = nextRange.mStartId - 1;
                                }
                            } else {
                                nextRange = null;
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
                    }
                    int endIndex;
                    IntRange endRange;
                    int joinIndex;
                    int i;
                    IntRange joinRange;
                    if (startId < range.mStartId) {
                        if (endId + 1 >= range.mStartId) {
                            if (endId > range.mEndId) {
                                endIndex = startIndex + 1;
                                while (endIndex < len) {
                                    endRange = (IntRange) this.mRanges.get(endIndex);
                                    if (endId + 1 >= endRange.mStartId) {
                                        if (endId > endRange.mEndId) {
                                            endIndex++;
                                        } else if (!tryAddRanges(startId, endRange.mStartId - 1, true)) {
                                            return false;
                                        } else {
                                            range.mStartId = startId;
                                            range.mEndId = endRange.mEndId;
                                            range.mClients.add(0, new ClientRange(startId, endId, client));
                                            joinIndex = startIndex + 1;
                                            for (i = joinIndex; i <= endIndex; i++) {
                                                joinRange = (IntRange) this.mRanges.get(joinIndex);
                                                range.mClients.addAll(joinRange.mClients);
                                                this.mRanges.remove(joinRange);
                                            }
                                            return true;
                                        }
                                    } else if (!tryAddRanges(startId, endId, true)) {
                                        return false;
                                    } else {
                                        range.mStartId = startId;
                                        range.mEndId = endId;
                                        range.mClients.add(0, new ClientRange(startId, endId, client));
                                        joinIndex = startIndex + 1;
                                        for (i = joinIndex; i < endIndex; i++) {
                                            joinRange = (IntRange) this.mRanges.get(joinIndex);
                                            range.mClients.addAll(joinRange.mClients);
                                            this.mRanges.remove(joinRange);
                                        }
                                        return true;
                                    }
                                }
                                if (!tryAddRanges(startId, endId, true)) {
                                    return false;
                                }
                                range.mStartId = startId;
                                range.mEndId = endId;
                                range.mClients.add(0, new ClientRange(startId, endId, client));
                                joinIndex = startIndex + 1;
                                for (i = joinIndex; i < len; i++) {
                                    joinRange = (IntRange) this.mRanges.get(joinIndex);
                                    range.mClients.addAll(joinRange.mClients);
                                    this.mRanges.remove(joinRange);
                                }
                                return true;
                            } else if (!tryAddRanges(startId, range.mStartId - 1, true)) {
                                return false;
                            } else {
                                range.mStartId = startId;
                                range.mClients.add(0, new ClientRange(startId, endId, client));
                                return true;
                            }
                        } else if (!tryAddRanges(startId, endId, true)) {
                            return false;
                        } else {
                            this.mRanges.add(startIndex, new IntRange(startId, endId, client));
                            return true;
                        }
                    }
                    if (startId + 1 <= range.mEndId) {
                        if (endId <= range.mEndId) {
                            range.insert(new ClientRange(startId, endId, client));
                            return true;
                        }
                        endIndex = startIndex;
                        for (int testIndex = startIndex + 1; testIndex < len; testIndex++) {
                            if (endId + 1 < ((IntRange) this.mRanges.get(testIndex)).mStartId) {
                                break;
                            }
                            endIndex = testIndex;
                        }
                        if (endIndex != startIndex) {
                            endRange = (IntRange) this.mRanges.get(endIndex);
                            if (!tryAddRanges(range.mEndId + 1, endId <= endRange.mEndId ? endRange.mStartId - 1 : endId, true)) {
                                return false;
                            }
                            range.mEndId = endId <= endRange.mEndId ? endRange.mEndId : endId;
                            range.insert(new ClientRange(startId, endId, client));
                            joinIndex = startIndex + 1;
                            for (i = joinIndex; i <= endIndex; i++) {
                                joinRange = (IntRange) this.mRanges.get(joinIndex);
                                range.mClients.addAll(joinRange.mClients);
                                this.mRanges.remove(joinRange);
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
                    startIndex++;
                } else {
                    range.insert(new ClientRange(startId, endId, client));
                    return true;
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
        int len = this.mRanges.size();
        for (int i = 0; i < len; i++) {
            IntRange range = (IntRange) this.mRanges.get(i);
            if (startId < range.mStartId) {
                return false;
            }
            if (endId <= range.mEndId) {
                ArrayList<ClientRange> clients = range.mClients;
                int crLength = clients.size();
                ClientRange cr;
                if (crLength == 1) {
                    cr = (ClientRange) clients.get(0);
                    if (cr.mStartId != startId || cr.mEndId != endId || !cr.mClient.equals(client)) {
                        return false;
                    }
                    this.mRanges.remove(i);
                    if (updateRanges()) {
                        return true;
                    }
                    this.mRanges.add(i, range);
                    return false;
                }
                int largestEndId = Integer.MIN_VALUE;
                boolean updateStarted = false;
                int crIndex = 0;
                while (crIndex < crLength) {
                    cr = (ClientRange) clients.get(crIndex);
                    if (cr.mStartId != startId || cr.mEndId != endId || !cr.mClient.equals(client)) {
                        if (cr.mEndId > largestEndId) {
                            largestEndId = cr.mEndId;
                        }
                        crIndex++;
                    } else if (crIndex != crLength - 1) {
                        IntRange rangeCopy = new IntRange(range, crIndex);
                        if (crIndex == 0) {
                            int nextStartId = ((ClientRange) clients.get(1)).mStartId;
                            if (nextStartId != range.mStartId) {
                                updateStarted = true;
                                rangeCopy.mStartId = nextStartId;
                            }
                            largestEndId = ((ClientRange) clients.get(1)).mEndId;
                        }
                        ArrayList<IntRange> newRanges = new ArrayList();
                        IntRange currentRange = rangeCopy;
                        for (int nextIndex = crIndex + 1; nextIndex < crLength; nextIndex++) {
                            ClientRange nextCr = (ClientRange) clients.get(nextIndex);
                            if (nextCr.mStartId > largestEndId + 1) {
                                updateStarted = true;
                                currentRange.mEndId = largestEndId;
                                newRanges.add(currentRange);
                                currentRange = new IntRange(nextCr);
                            } else {
                                if (currentRange.mEndId < nextCr.mEndId) {
                                    currentRange.mEndId = nextCr.mEndId;
                                }
                                currentRange.mClients.add(nextCr);
                            }
                            if (nextCr.mEndId > largestEndId) {
                                largestEndId = nextCr.mEndId;
                            }
                        }
                        if (largestEndId < endId) {
                            updateStarted = true;
                            currentRange.mEndId = largestEndId;
                        }
                        newRanges.add(currentRange);
                        this.mRanges.remove(i);
                        this.mRanges.addAll(i, newRanges);
                        if (!updateStarted || (updateRanges() ^ 1) == 0) {
                            return true;
                        }
                        this.mRanges.removeAll(newRanges);
                        this.mRanges.add(i, range);
                        return false;
                    } else if (range.mEndId == largestEndId) {
                        clients.remove(crIndex);
                        return true;
                    } else {
                        clients.remove(crIndex);
                        range.mEndId = largestEndId;
                        if (updateRanges()) {
                            return true;
                        }
                        clients.add(crIndex, cr);
                        range.mEndId = cr.mEndId;
                        return false;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public boolean updateRanges() {
        startUpdate();
        populateAllRanges();
        return finishUpdate();
    }

    protected boolean tryAddRanges(int startId, int endId, boolean selected) {
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
            IntRange currRange = (IntRange) itr.next();
            addRange(currRange.mStartId, currRange.mEndId, true);
        }
    }

    private void populateAllClientRanges() {
        int len = this.mRanges.size();
        for (int i = 0; i < len; i++) {
            IntRange range = (IntRange) this.mRanges.get(i);
            int clientLen = range.mClients.size();
            for (int j = 0; j < clientLen; j++) {
                ClientRange nextRange = (ClientRange) range.mClients.get(j);
                addRange(nextRange.mStartId, nextRange.mEndId, true);
            }
        }
    }
}
