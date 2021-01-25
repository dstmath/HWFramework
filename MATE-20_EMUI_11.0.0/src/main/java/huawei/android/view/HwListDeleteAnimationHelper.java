package huawei.android.view;

import android.annotation.TargetApi;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HwListDeleteAnimationHelper {
    private static final int DELETE_ITEM_DEFAULT_SIZE = 10;
    public static final int DIRECTION_TO_ADAPTER = 1;
    public static final int DIRECTION_TO_LIST = 0;
    private static final int INVALID_POSITION = -1;
    private static final String TAG = "HwListDeleteAnimationHelper";
    private int mRealItemCount = 0;
    private int mRecordItemCount = 0;
    private HwAvlTree<HwPositionPair> mRemovedItemRecords = new HwAvlTree<>();

    @TargetApi(5)
    private void recordOneRange(HwPositionPair positionPair) {
        if (!isPositionPairValid(positionPair, this.mRealItemCount)) {
            Log.e(TAG, "recordRemovedItemRange, input range error, positionPair: " + positionPair);
        } else if (this.mRemovedItemRecords.search(positionPair) != null) {
            int newAddedCount = mergeAllPositionPair(positionPair);
            if (newAddedCount <= 0) {
                Log.e(TAG, "recordRemovedItemRange merge position error, newAddedCount: " + newAddedCount + " positionPair: " + positionPair);
                return;
            }
            this.mRecordItemCount += newAddedCount;
        } else {
            this.mRemovedItemRecords.insert(positionPair);
            this.mRecordItemCount += (((Integer) positionPair.second).intValue() - ((Integer) positionPair.first).intValue()) + 1;
        }
    }

    @TargetApi(5)
    private int mergeAllPositionPair(HwPositionPair positionPair) {
        HwPositionPair mergedPositionPair = positionPair;
        int removeCount = 0;
        for (HwPositionPair pair : this.mRemovedItemRecords.searchAllMatchKey(positionPair)) {
            mergedPositionPair = mergeTwoPositionPair(pair, mergedPositionPair);
            this.mRemovedItemRecords.remove(pair);
            removeCount += (((Integer) pair.second).intValue() - ((Integer) pair.first).intValue()) + 1;
        }
        this.mRemovedItemRecords.insert(mergedPositionPair);
        return ((((Integer) mergedPositionPair.second).intValue() - ((Integer) mergedPositionPair.first).intValue()) + 1) - removeCount;
    }

    @TargetApi(5)
    public void recordRemovedItemRange(HwPositionPair positionPair) {
        List<HwPositionPair> deleteItemRanges = new ArrayList<>((int) DELETE_ITEM_DEFAULT_SIZE);
        int start = -1;
        int end = -1;
        for (int index = ((Integer) positionPair.first).intValue(); index <= ((Integer) positionPair.second).intValue(); index++) {
            int updatedIndex = updateItemPosition(index, 1);
            if (start == -1) {
                start = updatedIndex;
                end = start;
            } else if (updatedIndex == end + 1) {
                end++;
            } else {
                deleteItemRanges.add(new HwPositionPair(Integer.valueOf(start), Integer.valueOf(end)));
                start = updatedIndex;
                end = updatedIndex;
            }
        }
        if (start != -1) {
            deleteItemRanges.add(new HwPositionPair(Integer.valueOf(start), Integer.valueOf(end)));
        }
        for (HwPositionPair pair : deleteItemRanges) {
            recordOneRange(pair);
        }
    }

    @TargetApi(5)
    private boolean isPositionPairValid(HwPositionPair positionPair, int itemCount) {
        if (((Integer) positionPair.first).intValue() >= 0 && ((Integer) positionPair.first).intValue() < itemCount && ((Integer) positionPair.second).intValue() >= 0 && ((Integer) positionPair.second).intValue() < itemCount) {
            return true;
        }
        Log.e(TAG, "isPositionPairValid: position is invalid. positionPair: " + positionPair + " itemCount: " + itemCount);
        return false;
    }

    @TargetApi(5)
    private HwPositionPair mergeTwoPositionPair(HwPositionPair foundPositionPair, HwPositionPair inputPositionPair) {
        return new HwPositionPair(Integer.valueOf(((Integer) (((Integer) foundPositionPair.first).intValue() < ((Integer) inputPositionPair.first).intValue() ? foundPositionPair.first : inputPositionPair.first)).intValue()), Integer.valueOf(((Integer) (((Integer) foundPositionPair.second).intValue() > ((Integer) inputPositionPair.second).intValue() ? foundPositionPair.second : inputPositionPair.second)).intValue()));
    }

    public void clearRemovedItemsRecord() {
        if (!this.mRemovedItemRecords.isEmpty()) {
            this.mRemovedItemRecords = new HwAvlTree<>();
            this.mRecordItemCount = 0;
        }
    }

    @TargetApi(5)
    private int getDeletedItemNumBefore(HwPositionPair positionPair) {
        int itemNum = 0;
        for (HwPositionPair pair : this.mRemovedItemRecords.searchAllMatchKey(positionPair)) {
            itemNum += (((Integer) pair.second).intValue() - ((Integer) pair.first).intValue()) + 1;
        }
        return itemNum;
    }

    public int updateItemPosition(int position, int mappingDirection) {
        if (mappingDirection == 0 || mappingDirection == 1) {
            int i = this.mRealItemCount;
            if (i == 0) {
                Log.e(TAG, "updateItemPosition, mRealItemCount error, position: " + position);
                return -1;
            } else if (position < 0 || position >= i) {
                Log.e(TAG, "updateItemPosition, input error, position: " + position);
                return -1;
            } else if (this.mRecordItemCount == 0) {
                Log.e(TAG, "updateItemPosition, end, mRecordItemCount is zero, position: " + position + " mappingDirection: " + mappingDirection);
                return position;
            } else if (mappingDirection != 0) {
                return findPositionDirectionToAdapter(position);
            } else {
                if (this.mRemovedItemRecords.search(new HwPositionPair(Integer.valueOf(position), Integer.valueOf(position))) != null) {
                    return -1;
                }
                return position - getDeletedItemNumBefore(new HwPositionPair(0, Integer.valueOf(position - 1)));
            }
        } else {
            Log.e(TAG, "updateItemPosition, mappingDirection error, position: " + position + " mappingDirection: " + mappingDirection);
            return -1;
        }
    }

    @TargetApi(5)
    private int findPositionDirectionToAdapter(int position) {
        int currentPosition = -1;
        int lastPairSecond = -1;
        for (HwPositionPair positionPair : this.mRemovedItemRecords.getInOrderNodes()) {
            int newStep = ((Integer) positionPair.first).intValue() == 0 ? 0 : (((Integer) positionPair.first).intValue() - 1) - lastPairSecond;
            if (newStep < 0) {
                Log.e(TAG, "findPositionDirectionToAdapter failed, newStep: " + newStep + " positionPair.first: " + positionPair.first + " lastPairSecond: " + lastPairSecond + " position: " + position);
                return -1;
            }
            currentPosition += newStep;
            if (position <= currentPosition) {
                return (position - currentPosition) + lastPairSecond;
            }
            lastPairSecond = ((Integer) positionPair.second).intValue();
        }
        int updatedPosition = (position - currentPosition) + lastPairSecond;
        if (updatedPosition < this.mRealItemCount) {
            return updatedPosition;
        }
        Log.e(TAG, "findPositionDirectionToAdapter failed, updatedPosition: " + updatedPosition + " exceeds max, lastPairSecond: " + lastPairSecond + " position: " + position + " currentPosition: " + currentPosition);
        return -1;
    }

    public int updateItemCount(int count) {
        this.mRealItemCount = count;
        return count - this.mRecordItemCount;
    }
}
