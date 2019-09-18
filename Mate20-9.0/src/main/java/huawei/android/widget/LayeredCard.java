package huawei.android.widget;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import java.util.ArrayList;

public class LayeredCard extends FrameLayout {
    public ArrayList<SingleCard> mCardList = new ArrayList<>();

    public enum Direction {
        Left,
        Right
    }

    public static class SingleCard {
        public View mContent;
        public TimeInterpolator mLeftInInterpolator;
        public TimeInterpolator mLeftOutInterpolator;
        public TimeInterpolator mRightInInterpolator;
        public TimeInterpolator mRightOutInterpolator;

        public static class Build {
            private SingleCard singleCard;

            public Build(View content) {
                this.singleCard = new SingleCard(content);
            }

            public Build append(TimeInterpolator interpolator, TransitionMode mode) {
                switch (mode) {
                    case LeftIn:
                        this.singleCard.mLeftInInterpolator = interpolator;
                        break;
                    case LeftOut:
                        this.singleCard.mLeftOutInterpolator = interpolator;
                        break;
                    case RightIn:
                        this.singleCard.mRightInInterpolator = interpolator;
                        break;
                    case RightOut:
                        this.singleCard.mRightOutInterpolator = interpolator;
                        break;
                }
                return this;
            }

            public SingleCard create() {
                return this.singleCard;
            }
        }

        public enum TransitionMode {
            LeftIn,
            LeftOut,
            RightIn,
            RightOut
        }

        private SingleCard(View content) {
            this.mContent = content;
        }
    }

    public LayeredCard(Context context) {
        super(context);
    }

    public LayeredCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayeredCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LayeredCard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addCard(SingleCard card) {
        this.mCardList.add(card);
    }

    public void removeCard(SingleCard card) {
        this.mCardList.remove(card);
    }

    public void clearCard() {
        this.mCardList.clear();
    }

    public void updateTranslationX(float positionOffset, float pageOffset, boolean in, Direction direction) {
        int w = getWidth();
        int size = this.mCardList.size();
        for (int i = 0; i < size; i++) {
            SingleCard card = this.mCardList.get(i);
            if (!(card == null || card.mContent == null)) {
                TimeInterpolator timeInterpolator = getInterpolation(card, in, direction);
                if (timeInterpolator != null) {
                    card.mContent.setTranslationX((float) (((int) (timeInterpolator.getInterpolation(positionOffset) * ((float) w))) + ((int) (((float) w) * pageOffset))));
                }
            }
        }
    }

    private TimeInterpolator getInterpolation(SingleCard card, boolean in, Direction direction) {
        switch (direction) {
            case Left:
                return in ? card.mLeftInInterpolator : card.mLeftOutInterpolator;
            case Right:
                return in ? card.mRightInInterpolator : card.mRightOutInterpolator;
            default:
                throw new RuntimeException("UnKnow");
        }
    }

    public void updateX(float positionOffset, float pageOffset, boolean in, Direction direction) {
        int w = getWidth();
        int size = this.mCardList.size();
        for (int i = 0; i < size; i++) {
            SingleCard card = this.mCardList.get(i);
            if (!(card == null || card.mContent == null)) {
                TimeInterpolator timeInterpolator = getInterpolation(card, in, direction);
                if (timeInterpolator != null) {
                    card.mContent.setX((float) (((int) (timeInterpolator.getInterpolation(positionOffset) * ((float) w))) + ((int) (((float) w) * pageOffset))));
                }
            }
        }
    }
}
