package com.android.internal.policy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Size;
import android.view.Gravity;
import java.io.PrintWriter;
import java.util.ArrayList;

public class PipSnapAlgorithm {
    private static final float CORNER_MAGNET_THRESHOLD = 0.3f;
    private static final int SNAP_MODE_CORNERS_AND_SIDES = 1;
    private static final int SNAP_MODE_CORNERS_ONLY = 0;
    private static final int SNAP_MODE_EDGE = 2;
    private static final int SNAP_MODE_EDGE_MAGNET_CORNERS = 3;
    private static final int SNAP_MODE_LONG_EDGE_MAGNET_CORNERS = 4;
    private final Context mContext;
    private final float mDefaultSizePercent;
    private final int mDefaultSnapMode = 3;
    private final int mFlingDeceleration;
    private boolean mIsMinimized;
    private final float mMaxAspectRatioForMinSize;
    private final float mMinAspectRatioForMinSize;
    private final int mMinimizedVisibleSize;
    private int mOrientation = 0;
    private final ArrayList<Integer> mSnapGravities = new ArrayList<>();
    private int mSnapMode = 3;

    public PipSnapAlgorithm(Context context) {
        Resources res = context.getResources();
        this.mContext = context;
        this.mMinimizedVisibleSize = res.getDimensionPixelSize(17105262);
        this.mDefaultSizePercent = res.getFloat(17104969);
        this.mMaxAspectRatioForMinSize = res.getFloat(17104967);
        this.mMinAspectRatioForMinSize = 1.0f / this.mMaxAspectRatioForMinSize;
        this.mFlingDeceleration = this.mContext.getResources().getDimensionPixelSize(17105261);
        onConfigurationChanged();
    }

    public void onConfigurationChanged() {
        Resources res = this.mContext.getResources();
        this.mOrientation = res.getConfiguration().orientation;
        this.mSnapMode = res.getInteger(17694849);
        calculateSnapTargets();
    }

    public void setMinimized(boolean isMinimized) {
        this.mIsMinimized = isMinimized;
    }

    public Rect findClosestSnapBounds(Rect movementBounds, Rect stackBounds, float velocityX, float velocityY, Point dragStartPosition) {
        Rect intersectStackBounds = new Rect(stackBounds);
        Point intersect = getEdgeIntersect(stackBounds, movementBounds, velocityX, velocityY, dragStartPosition);
        intersectStackBounds.offsetTo(intersect.x, intersect.y);
        return findClosestSnapBounds(movementBounds, intersectStackBounds);
    }

    public Point getEdgeIntersect(Rect stackBounds, Rect movementBounds, float velX, float velY, Point dragStartPosition) {
        int maxDistance;
        double d;
        int i;
        Rect rect = stackBounds;
        Rect rect2 = movementBounds;
        float f = velX;
        float f2 = velY;
        Point point = dragStartPosition;
        boolean isLandscape = this.mOrientation == 2;
        int x = rect.left;
        int y = rect.top;
        float slope = f2 / f;
        float yIntercept = ((float) y) - (((float) x) * slope);
        Point vertPoint = new Point();
        Point horizPoint = new Point();
        vertPoint.x = f > 0.0f ? rect2.right : rect2.left;
        vertPoint.y = findY(slope, yIntercept, (float) vertPoint.x);
        horizPoint.y = f2 > 0.0f ? rect2.bottom : rect2.top;
        horizPoint.x = findX(slope, yIntercept, (float) horizPoint.y);
        if (isLandscape) {
            if (f > 0.0f) {
                i = rect2.right - rect.left;
            } else {
                i = rect.left - rect2.left;
            }
            maxDistance = i;
        } else if (f2 > 0.0f) {
            maxDistance = rect2.bottom - rect.top;
        } else {
            maxDistance = rect.top - rect2.top;
        }
        if (maxDistance > 0) {
            int startPoint = isLandscape ? point.y : point.x;
            int endPoint = isLandscape ? horizPoint.y : horizPoint.x;
            int center = movementBounds.centerX();
            if ((startPoint < center && endPoint < center) || (startPoint > center && endPoint > center)) {
                if (isLandscape) {
                    float f3 = slope;
                    float f4 = yIntercept;
                    d = (double) f;
                } else {
                    float f5 = yIntercept;
                    d = (double) f2;
                }
                int i2 = endPoint;
                int i3 = startPoint;
                int distance = Math.min(((int) (0.0d - Math.pow(d, 2.0d))) / (this.mFlingDeceleration * 2), maxDistance);
                if (isLandscape) {
                    horizPoint.x = rect.left + (f > 0.0f ? distance : -distance);
                } else {
                    horizPoint.y = rect.top + (f2 > 0.0f ? distance : -distance);
                }
                return horizPoint;
            }
        }
        float f6 = yIntercept;
        double distanceVert = Math.hypot((double) (vertPoint.x - x), (double) (vertPoint.y - y));
        double distanceHoriz = Math.hypot((double) (horizPoint.x - x), (double) (horizPoint.y - y));
        if (distanceVert == 0.0d) {
            return horizPoint;
        }
        if (distanceHoriz == 0.0d) {
            return vertPoint;
        }
        return Math.abs(distanceVert) > Math.abs(distanceHoriz) ? horizPoint : vertPoint;
    }

    private int findY(float slope, float yIntercept, float x) {
        return (int) ((slope * x) + yIntercept);
    }

    private int findX(float slope, float yIntercept, float y) {
        return (int) ((y - yIntercept) / slope);
    }

    public Rect findClosestSnapBounds(Rect movementBounds, Rect stackBounds) {
        Rect rect = movementBounds;
        Rect rect2 = stackBounds;
        Rect pipBounds = new Rect(rect.left, rect.top, rect.right + stackBounds.width(), rect.bottom + stackBounds.height());
        Rect newBounds = new Rect(rect2);
        int i = 0;
        if (this.mSnapMode == 4 || this.mSnapMode == 3) {
            Rect tmpBounds = new Rect();
            Point[] snapTargets = new Point[this.mSnapGravities.size()];
            while (i < this.mSnapGravities.size()) {
                Gravity.apply(this.mSnapGravities.get(i).intValue(), stackBounds.width(), stackBounds.height(), pipBounds, 0, 0, tmpBounds);
                snapTargets[i] = new Point(tmpBounds.left, tmpBounds.top);
                i++;
            }
            Point snapTarget = findClosestPoint(rect2.left, rect2.top, snapTargets);
            if (distanceToPoint(snapTarget, rect2.left, rect2.top) < ((float) Math.max(stackBounds.width(), stackBounds.height())) * CORNER_MAGNET_THRESHOLD) {
                newBounds.offsetTo(snapTarget.x, snapTarget.y);
            } else {
                snapRectToClosestEdge(rect2, rect, newBounds);
            }
        } else if (this.mSnapMode == 2) {
            snapRectToClosestEdge(rect2, rect, newBounds);
        } else {
            Rect tmpBounds2 = new Rect();
            Point[] snapTargets2 = new Point[this.mSnapGravities.size()];
            while (true) {
                int i2 = i;
                if (i2 >= this.mSnapGravities.size()) {
                    break;
                }
                Gravity.apply(this.mSnapGravities.get(i2).intValue(), stackBounds.width(), stackBounds.height(), pipBounds, 0, 0, tmpBounds2);
                snapTargets2[i2] = new Point(tmpBounds2.left, tmpBounds2.top);
                i = i2 + 1;
            }
            Point snapTarget2 = findClosestPoint(rect2.left, rect2.top, snapTargets2);
            newBounds.offsetTo(snapTarget2.x, snapTarget2.y);
        }
        return newBounds;
    }

    public void applyMinimizedOffset(Rect stackBounds, Rect movementBounds, Point displaySize, Rect stableInsets) {
        if (stackBounds.left <= movementBounds.centerX()) {
            stackBounds.offsetTo((stableInsets.left + this.mMinimizedVisibleSize) - stackBounds.width(), stackBounds.top);
        } else {
            stackBounds.offsetTo((displaySize.x - stableInsets.right) - this.mMinimizedVisibleSize, stackBounds.top);
        }
    }

    public float getSnapFraction(Rect stackBounds, Rect movementBounds) {
        Rect tmpBounds = new Rect();
        snapRectToClosestEdge(stackBounds, movementBounds, tmpBounds);
        float widthFraction = ((float) (tmpBounds.left - movementBounds.left)) / ((float) movementBounds.width());
        float heightFraction = ((float) (tmpBounds.top - movementBounds.top)) / ((float) movementBounds.height());
        if (tmpBounds.top == movementBounds.top) {
            return widthFraction;
        }
        if (tmpBounds.left == movementBounds.right) {
            return 1.0f + heightFraction;
        }
        if (tmpBounds.top == movementBounds.bottom) {
            return 2.0f + (1.0f - widthFraction);
        }
        return 3.0f + (1.0f - heightFraction);
    }

    public void applySnapFraction(Rect stackBounds, Rect movementBounds, float snapFraction) {
        if (snapFraction < 1.0f) {
            stackBounds.offsetTo(movementBounds.left + ((int) (((float) movementBounds.width()) * snapFraction)), movementBounds.top);
        } else if (snapFraction < 2.0f) {
            stackBounds.offsetTo(movementBounds.right, movementBounds.top + ((int) (((float) movementBounds.height()) * (snapFraction - 1.0f))));
        } else if (snapFraction < 3.0f) {
            stackBounds.offsetTo(movementBounds.left + ((int) ((1.0f - (snapFraction - 2.0f)) * ((float) movementBounds.width()))), movementBounds.bottom);
        } else {
            stackBounds.offsetTo(movementBounds.left, movementBounds.top + ((int) ((1.0f - (snapFraction - 3.0f)) * ((float) movementBounds.height()))));
        }
    }

    public void getMovementBounds(Rect stackBounds, Rect insetBounds, Rect movementBoundsOut, int bottomOffset) {
        movementBoundsOut.set(insetBounds);
        movementBoundsOut.right = Math.max(insetBounds.left, insetBounds.right - stackBounds.width());
        movementBoundsOut.bottom = Math.max(insetBounds.top, insetBounds.bottom - stackBounds.height());
        movementBoundsOut.bottom -= bottomOffset;
    }

    public Size getSizeForAspectRatio(float aspectRatio, float minEdgeSize, int displayWidth, int displayHeight) {
        int height;
        int width;
        int minSize = (int) Math.max(minEdgeSize, ((float) Math.min(displayWidth, displayHeight)) * this.mDefaultSizePercent);
        if (aspectRatio > this.mMinAspectRatioForMinSize && aspectRatio <= this.mMaxAspectRatioForMinSize) {
            float radius = PointF.length(this.mMaxAspectRatioForMinSize * ((float) minSize), (float) minSize);
            height = (int) Math.round(Math.sqrt((double) ((radius * radius) / ((aspectRatio * aspectRatio) + 1.0f))));
            width = Math.round(((float) height) * aspectRatio);
        } else if (aspectRatio <= 1.0f) {
            width = minSize;
            height = Math.round(((float) width) / aspectRatio);
        } else {
            height = minSize;
            width = Math.round(((float) height) * aspectRatio);
        }
        return new Size(width, height);
    }

    private Point findClosestPoint(int x, int y, Point[] points) {
        Point closestPoint = null;
        float minDistance = Float.MAX_VALUE;
        for (Point p : points) {
            float distance = distanceToPoint(p, x, y);
            if (distance < minDistance) {
                closestPoint = p;
                minDistance = distance;
            }
        }
        return closestPoint;
    }

    private void snapRectToClosestEdge(Rect stackBounds, Rect movementBounds, Rect boundsOut) {
        int shortest;
        int boundedLeft = Math.max(movementBounds.left, Math.min(movementBounds.right, stackBounds.left));
        int boundedTop = Math.max(movementBounds.top, Math.min(movementBounds.bottom, stackBounds.top));
        boundsOut.set(stackBounds);
        if (this.mIsMinimized) {
            boundsOut.offsetTo(boundedLeft, boundedTop);
            return;
        }
        int fromLeft = Math.abs(stackBounds.left - movementBounds.left);
        int fromTop = Math.abs(stackBounds.top - movementBounds.top);
        int fromRight = Math.abs(movementBounds.right - stackBounds.left);
        int fromBottom = Math.abs(movementBounds.bottom - stackBounds.top);
        if (this.mSnapMode != 4) {
            shortest = Math.min(Math.min(fromLeft, fromRight), Math.min(fromTop, fromBottom));
        } else if (this.mOrientation == 2) {
            shortest = Math.min(fromTop, fromBottom);
        } else {
            shortest = Math.min(fromLeft, fromRight);
        }
        if (shortest == fromLeft) {
            boundsOut.offsetTo(movementBounds.left, boundedTop);
        } else if (shortest == fromTop) {
            boundsOut.offsetTo(boundedLeft, movementBounds.top);
        } else if (shortest == fromRight) {
            boundsOut.offsetTo(movementBounds.right, boundedTop);
        } else {
            boundsOut.offsetTo(boundedLeft, movementBounds.bottom);
        }
    }

    private float distanceToPoint(Point p, int x, int y) {
        return PointF.length((float) (p.x - x), (float) (p.y - y));
    }

    private void calculateSnapTargets() {
        this.mSnapGravities.clear();
        switch (this.mSnapMode) {
            case 0:
            case 3:
            case 4:
                break;
            case 1:
                if (this.mOrientation != 2) {
                    this.mSnapGravities.add(19);
                    this.mSnapGravities.add(21);
                    break;
                } else {
                    this.mSnapGravities.add(49);
                    this.mSnapGravities.add(81);
                    break;
                }
            default:
                return;
        }
        this.mSnapGravities.add(51);
        this.mSnapGravities.add(53);
        this.mSnapGravities.add(83);
        this.mSnapGravities.add(85);
    }

    public void dump(PrintWriter pw, String prefix) {
        String innerPrefix = prefix + "  ";
        pw.println(prefix + PipSnapAlgorithm.class.getSimpleName());
        pw.println(innerPrefix + "mSnapMode=" + this.mSnapMode);
        pw.println(innerPrefix + "mOrientation=" + this.mOrientation);
        pw.println(innerPrefix + "mMinimizedVisibleSize=" + this.mMinimizedVisibleSize);
        pw.println(innerPrefix + "mIsMinimized=" + this.mIsMinimized);
    }
}
