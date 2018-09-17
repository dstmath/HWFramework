package com.google.android.maps;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.google.android.maps.Overlay.Snappable;
import java.util.ArrayList;

public abstract class ItemizedOverlay<Item extends OverlayItem> extends Overlay implements Snappable {
    private static final int MINIMUM_TOUCH_DIAMETER = (ViewConfiguration.getTouchSlop() * 4);
    private final Drawable mBalloon;
    private int mCurrentlyPressedItemRank;
    private int mCurrentlySelectedItemRank;
    private boolean mDrawFocusedItem = true;
    private Item mFocused = null;
    private int mInGestureMask;
    private int[] mItemState;
    private ArrayList<Item> mItemsOrderedByRank = null;
    protected int mLastFocusedIndex = -1;
    private int mLastSelectedItemRank = -1;
    private int mLatSpanE6;
    private int mLonSpanE6;
    private OnFocusChangeListener mOnFocusChangeListener;
    private int[] mRanksOrderedByLat = null;
    private final Point mTempPoint = new Point();
    private final Rect mTouchableBounds = new Rect();

    public interface OnFocusChangeListener {
        void onFocusChanged(ItemizedOverlay itemizedOverlay, OverlayItem overlayItem);
    }

    protected abstract Item createItem(int i);

    public abstract int size();

    public ItemizedOverlay(Drawable defaultMarker) {
        this.mBalloon = defaultMarker;
    }

    private Rect getTouchableBounds(Rect bounds) {
        int w = bounds.width();
        int h = bounds.height();
        if (w >= MINIMUM_TOUCH_DIAMETER && h >= MINIMUM_TOUCH_DIAMETER) {
            return bounds;
        }
        int cx = bounds.centerX();
        int cy = bounds.centerY();
        int touchW = Math.max(MINIMUM_TOUCH_DIAMETER, w);
        int touchL = cx - (touchW / 2);
        int touchH = Math.max(MINIMUM_TOUCH_DIAMETER, h);
        int touchT = cy - (touchH / 2);
        this.mTouchableBounds.set(touchL, touchT, touchL + touchW, touchT + touchH);
        return this.mTouchableBounds;
    }

    protected static Drawable boundCenterBottom(Drawable balloon) {
        int width = balloon.getIntrinsicWidth();
        int w2 = width / 2;
        balloon.setBounds(-w2, 1 - balloon.getIntrinsicHeight(), width - w2, 1);
        return balloon;
    }

    protected static Drawable boundCenter(Drawable balloon) {
        int width = balloon.getIntrinsicWidth();
        int w2 = width / 2;
        int height = balloon.getIntrinsicHeight();
        int h2 = height / 2;
        balloon.setBounds(-w2, -h2, width - w2, height - h2);
        return balloon;
    }

    public GeoPoint getCenter() {
        if (this.mRanksOrderedByLat.length > 0) {
            return getItem(0).getPoint();
        }
        return null;
    }

    protected int getIndexToDraw(int drawingOrder) {
        return this.mRanksOrderedByLat[drawingOrder];
    }

    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        int size = size();
        int focusedItemRank = -1;
        for (int i = 0; i < size; i++) {
            int rank = getIndexToDraw(i);
            if ((this.mItemState[rank] & 4) == 0) {
                drawItem(canvas, mapView, shadow, rank);
            } else {
                focusedItemRank = rank;
            }
        }
        if (this.mDrawFocusedItem && focusedItemRank >= 0) {
            drawItem(canvas, mapView, shadow, focusedItemRank);
        }
    }

    private void drawItem(Canvas canvas, MapView mapView, boolean shadow, int rank) {
        Item item = getItem(rank);
        Drawable marker = getDrawable(item, rank);
        mapView.getProjection().toPixels(item.getPoint(), this.mTempPoint);
        Overlay.drawAt(canvas, marker, this.mTempPoint.x, this.mTempPoint.y, shadow);
    }

    private Drawable getDrawable(Item item, int rank) {
        int itemState = this.mItemState[rank];
        Drawable drawable = item.getMarker(itemState);
        if (drawable != null) {
            return drawable;
        }
        drawable = this.mBalloon;
        OverlayItem.setState(drawable, itemState);
        return drawable;
    }

    public int getLatSpanE6() {
        return this.mLatSpanE6;
    }

    public int getLonSpanE6() {
        return this.mLonSpanE6;
    }

    protected final void populate() {
        int i;
        int resultCount = size();
        int minLat = 90000000;
        int maxLat = -90000000;
        int minLon = 180000000;
        int maxLon = -180000000;
        ArrayList<Item> itemsOrderedByRank = new ArrayList(resultCount);
        for (i = 0; i < resultCount; i++) {
            Item item = createItem(i);
            itemsOrderedByRank.add(item);
            GeoPoint point = item.getPoint();
            minLat = Math.min(minLat, point.getLatitudeE6());
            maxLat = Math.max(maxLat, point.getLatitudeE6());
            minLon = Math.min(minLon, point.getLongitudeE6());
            maxLon = Math.max(maxLon, point.getLongitudeE6());
        }
        this.mLatSpanE6 = maxLat - minLat;
        this.mLonSpanE6 = maxLon - minLon;
        int[] ranksOrderedByLat = new int[resultCount];
        i = 0;
        while (i < resultCount) {
            int insertRank = i;
            for (int j = 0; j <= i; j++) {
                OverlayItem sorted = (OverlayItem) itemsOrderedByRank.get(ranksOrderedByLat[j]);
                if (j == i || sorted.getPoint().getLatitudeE6() < ((OverlayItem) itemsOrderedByRank.get(i)).getPoint().getLatitudeE6()) {
                    int tmp = ranksOrderedByLat[j];
                    ranksOrderedByLat[j] = insertRank;
                    insertRank = tmp;
                }
            }
            i++;
        }
        this.mRanksOrderedByLat = ranksOrderedByLat;
        this.mItemsOrderedByRank = itemsOrderedByRank;
        this.mItemState = new int[resultCount];
        this.mCurrentlySelectedItemRank = -1;
        this.mCurrentlyPressedItemRank = -1;
        this.mInGestureMask = 0;
    }

    protected void setLastFocusedIndex(int lastFocusedIndex) {
        this.mLastFocusedIndex = lastFocusedIndex;
    }

    private void setFocus(int rank, Item item) {
        boolean notify = (this.mFocused == item || this.mOnFocusChangeListener == null) ? false : true;
        maskHelper(this.mLastFocusedIndex, rank, 4);
        if (rank != -1) {
            this.mLastFocusedIndex = rank;
        }
        this.mFocused = item;
        if (notify) {
            this.mOnFocusChangeListener.onFocusChanged(this, item);
        }
    }

    public void setFocus(Item item) {
        if (item == null) {
            setFocus(this.mLastFocusedIndex, null);
        } else {
            int index = 0;
            for (Item candidate : this.mItemsOrderedByRank) {
                if (candidate == item) {
                    setFocus(index, candidate);
                    return;
                }
                index++;
            }
        }
    }

    public Item getFocus() {
        return this.mFocused;
    }

    public final int getLastFocusedIndex() {
        return this.mLastFocusedIndex;
    }

    public final Item getItem(int position) {
        return (OverlayItem) this.mItemsOrderedByRank.get(position);
    }

    public Item nextFocus(boolean forwards) {
        int rank = this.mLastFocusedIndex + (forwards ? 1 : -1);
        if (rank < 0 || rank >= this.mRanksOrderedByLat.length) {
            return null;
        }
        return getItem(rank);
    }

    public boolean onTap(GeoPoint p, MapView mapView) {
        mapView.getProjection().toPixels(p, this.mTempPoint);
        int hit = getItemAtLocation(this.mTempPoint.x, this.mTempPoint.y, mapView);
        boolean retValue = false;
        if (hit != -1) {
            retValue = onTap(hit);
        }
        focus(hit);
        int selectHit = hit;
        select(hit);
        return retValue;
    }

    public boolean onSnapToItem(int x, int y, Point snapPoint, MapView mapView) {
        int hitRank = getItemAtLocation(x, y, mapView);
        if (hitRank == -1) {
            return false;
        }
        mapView.getProjection().toPixels(((OverlayItem) this.mItemsOrderedByRank.get(hitRank)).getPoint(), snapPoint);
        return true;
    }

    public boolean onTrackballEvent(MotionEvent event, MapView mapView) {
        return handleMotionEvent(true, 1, event, mapView, mapView.getWidth() / 2, mapView.getHeight() / 2);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView) {
        switch (keyCode) {
            case 23:
            case 66:
                if (getFocus() != null) {
                    return onTap(this.mLastFocusedIndex);
                }
                break;
        }
        return super.onKeyUp(keyCode, event, mapView);
    }

    private boolean handleMotionEvent(boolean trackball, int gestureMask, MotionEvent event, MapView mapView, int x, int y) {
        int action = event.getAction();
        boolean isDown = action == 0;
        boolean isDownOrMove = isDown || action == 2;
        int hit = getItemAtLocation(x, y, mapView);
        boolean hitSomething = hit != -1;
        select(trackball ? hit : -1);
        if (isDown) {
            if (hitSomething) {
                this.mInGestureMask |= gestureMask;
            } else {
                this.mInGestureMask &= ~gestureMask;
            }
        }
        boolean inGesture = (this.mInGestureMask & gestureMask) != 0;
        if (inGesture) {
            if (isDownOrMove) {
                press(hit);
            } else if (action == 1) {
                press(-1);
                this.mInGestureMask &= ~gestureMask;
                if (gestureMask != 2) {
                    onTap(hit);
                }
            }
        }
        if (!inGesture || gestureMask == 2) {
            return false;
        }
        return true;
    }

    private void focus(int hit) {
        setFocus(hit, hit != -1 ? getItem(hit) : null);
    }

    private void select(int rank) {
        this.mCurrentlySelectedItemRank = maskHelper(this.mCurrentlySelectedItemRank, rank, 2);
    }

    private void press(int rank) {
        this.mCurrentlyPressedItemRank = maskHelper(this.mCurrentlyPressedItemRank, rank, 1);
    }

    private int maskHelper(int oldRank, int newRank, int mask) {
        if (oldRank != newRank) {
            int[] iArr;
            if (oldRank != -1) {
                iArr = this.mItemState;
                iArr[oldRank] = iArr[oldRank] & (~mask);
            }
            if (newRank != -1) {
                iArr = this.mItemState;
                iArr[newRank] = iArr[newRank] | mask;
            }
        }
        return newRank;
    }

    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        return handleMotionEvent(false, 2, event, mapView, (int) event.getX(), (int) event.getY());
    }

    private int getItemAtLocation(int hitX, int hitY, MapView mapView) {
        int closestRank = -1;
        int closestDistanceSquared = Integer.MAX_VALUE;
        for (Integer intValue : getItemsAtLocation(hitX, hitY, mapView)) {
            int rank = intValue.intValue();
            OverlayItem item = (OverlayItem) this.mItemsOrderedByRank.get(rank);
            mapView.getProjection().toPixels(item.getPoint(), this.mTempPoint);
            int offsetX = hitX - this.mTempPoint.x;
            int offsetY = hitY - this.mTempPoint.y;
            Rect bounds = getTouchableBounds(getDrawable(item, rank).getBounds());
            int dx = bounds.centerX() - offsetX;
            int dy = bounds.centerY() - offsetY;
            int distanceSquared = (dx * dx) + (dy * dy);
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closestRank = rank;
            }
        }
        this.mLastSelectedItemRank = closestRank;
        return this.mLastSelectedItemRank;
    }

    private ArrayList<Integer> getItemsAtLocation(int hitX, int hitY, MapView mapView) {
        ArrayList<Item> itemsByRank = this.mItemsOrderedByRank;
        int[] ranksOrderedByLat = this.mRanksOrderedByLat;
        int length = ranksOrderedByLat.length;
        ArrayList<Integer> hitItemRanks = new ArrayList(length);
        for (int i = length - 1; i >= 0; i--) {
            int rank = ranksOrderedByLat[i];
            int itemState = this.mItemState[rank];
            if (this.mDrawFocusedItem || (itemState & 4) == 0) {
                OverlayItem item = (OverlayItem) itemsByRank.get(rank);
                mapView.getProjection().toPixels(item.getPoint(), this.mTempPoint);
                if (hitTest(item, getDrawable(item, rank), hitX - this.mTempPoint.x, hitY - this.mTempPoint.y)) {
                    hitItemRanks.add(Integer.valueOf(rank));
                }
            }
        }
        return hitItemRanks;
    }

    protected boolean hitTest(Item item, Drawable marker, int hitX, int hitY) {
        return getTouchableBounds(marker.getBounds()).contains(hitX, hitY);
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        this.mOnFocusChangeListener = l;
    }

    public void setDrawFocusedItem(boolean drawFocusedItem) {
        this.mDrawFocusedItem = drawFocusedItem;
    }

    protected boolean onTap(int index) {
        return false;
    }
}
