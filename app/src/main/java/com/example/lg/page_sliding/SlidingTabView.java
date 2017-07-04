package com.example.lg.page_sliding;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlidingTabView extends HorizontalScrollView {

    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;
    private LinearLayout.LayoutParams fixedTabLayoutParams;

    private final PageListener pageListener = new PageListener();
    private ViewPager.OnPageChangeListener delegatePageListener;

    private LinearLayout tabsContainer;
    private ViewPager pager;

    private int tabCount;

    private int currentPosition = 0;
    private float currentPositionOffset = 0f;

    private Paint paint;

    private int indicatorColor = 0xFF666666;
    private int underlineColor = 0x1A000000;

    private ExpandMode expandMode = ExpandMode.WRAP;
    private int tabFixedWidth = 60;

    private int scrollOffset = 52;
    private int indicatorHeight = 8;
    private int underlineHeight = 2;
    private int tabPadding = 24;

    private int tabTextSize = 12;
    private int tabSelectedTextSize = 12;
    private ColorStateList tabTextColor = ColorStateList.valueOf(0xFF666666);

    private IndicatorMode indicatorMode = IndicatorMode.MATCH;

    private int currentSelected = -1;

    private int tabBackgroundResId;

    public SlidingTabView(Context context) {
        this(context, null);
    }

    public SlidingTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ResourceType")
    public SlidingTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        addView(tabsContainer);

        scrollOffset = convertToDip(scrollOffset);
        indicatorHeight = convertToDip(indicatorHeight);
        underlineHeight = convertToDip(underlineHeight);
        tabFixedWidth = convertToDip(tabFixedWidth);
        tabPadding = convertToDip(tabPadding);
        tabTextSize = convertToDip(tabTextSize);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                android.R.attr.textSize,
                android.R.attr.textColor
        });
        tabTextSize = typedArray.getDimensionPixelSize(0, tabTextSize);
        if (typedArray.hasValue(1)) {
            tabTextColor = typedArray.getColorStateList(1);
        }
        typedArray.recycle();

        typedArray = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTab);
        tabSelectedTextSize = typedArray.getDimensionPixelSize(
                R.styleable.PagerSlidingTab_pstSelectedTextSize,
                tabSelectedTextSize);
        indicatorColor = typedArray.getColor(
                R.styleable.PagerSlidingTab_pstIndicatorColor, indicatorColor);
        indicatorHeight = typedArray.getDimensionPixelSize(
                R.styleable.PagerSlidingTab_pstIndicatorHeight, indicatorHeight);
        underlineColor = typedArray.getColor(
                R.styleable.PagerSlidingTab_pstUnderlineColor, underlineColor);
        underlineHeight = typedArray.getDimensionPixelSize(
                R.styleable.PagerSlidingTab_pstUnderlineHeight, underlineHeight);
        tabPadding = typedArray.getDimensionPixelSize(
                R.styleable.PagerSlidingTab_pstTabPadding, tabPadding);
        tabBackgroundResId = typedArray.getResourceId(
                R.styleable.PagerSlidingTab_pstBackground, tabBackgroundResId);
        int expand = typedArray.getInt(R.styleable.PagerSlidingTab_pstExpandMode,
                0);
        expandMode = ExpandMode.fromId(expand);
        tabFixedWidth = typedArray.getDimensionPixelSize(
                R.styleable.PagerSlidingTab_pstFixedWidth, tabFixedWidth);
        scrollOffset = typedArray.getDimensionPixelOffset(
                R.styleable.PagerSlidingTab_pstScrollOffset, scrollOffset);
        int indicatorModeId = typedArray
                .getInt(R.styleable.PagerSlidingTab_pstIndicatorMode, 0);
        indicatorMode = IndicatorMode.fromId(indicatorModeId);
        typedArray.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        fixedTabLayoutParams = new LinearLayout.LayoutParams(tabFixedWidth,
                LayoutParams.MATCH_PARENT);
        fixedTabLayoutParams.gravity = Gravity.CENTER;
        expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
    }

    private int convertToDip(int value) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm);
    }

    public void setViewPager(ViewPager pager) {
        this.pager = pager;

        if (null == pager.getAdapter()) {
            throw new IllegalStateException("ViewPager dose not have adapter instance.");
        }

        pager.addOnPageChangeListener(pageListener);
        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged() {
        tabsContainer.removeAllViews();
        tabCount = pager.getAdapter().getCount();
        for (int i = 0; i < tabCount; i++) {
            addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
        }

        updateTabStyles();

        getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        setIndicatorPosition(currentPosition, 0);
                        setCurrentSelected(pager.getCurrentItem());
                        scrollToAnchorArea(currentPosition);

                    }
                });
    }

    private void addTextTab(int position, String title) {
        TextView tab = new TextView(getContext());
        tab.setText(title);
        tab.setGravity(Gravity.CENTER);
        tab.setMaxLines(1);
        tab.setEllipsize(TextUtils.TruncateAt.END);
        tab.setSingleLine();

        addTab(position, tab);
    }

    private void addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Float, Float> targetLeftRight = getIndicatorTargetLeftRight(position, 0);
                startIndicatorAnimate(targetLeftRight.first, targetLeftRight.second);
                pager.setCurrentItem(position, false);
            }
        });
        switch (expandMode) {
            case WRAP:
                tab.setPadding(tabPadding, 0, tabPadding, 0);
                tabsContainer.addView(tab, position, defaultTabLayoutParams);
                break;
            case FIXED:
                tabsContainer.addView(tab, position, fixedTabLayoutParams);
                break;
            case EXPANDED:
                tab.setPadding(tabPadding, 0, tabPadding, 0);
                tabsContainer.addView(tab, position, expandedTabLayoutParams);
                break;
            case REACT:
                if (tab instanceof TextView) {
                    int length = ((TextView) tab).getText().length();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            convertToDip(30 + length * 14), ViewGroup.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.CENTER;
                    tabsContainer.addView(tab, position, params);
                } else {
                    tabsContainer.addView(tab, position, fixedTabLayoutParams);
                }
                break;
            default:
                break;
        }
    }

    private void updateTabStyles() {
        for (int i = 0; i < tabCount; ++i) {
            View v = tabsContainer.getChildAt(i);
            v.setBackgroundResource(tabBackgroundResId);
            if (v instanceof TextView) {
                TextView tab = (TextView) v;
                tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
                tab.setTextColor(tabTextColor);
            }
        }
    }

    private final static int SCROLL_DURATION = 200;

    // 当所选标签不在锚定区域内，即currentTab离屏幕左边/右边的距离小于scrollOffset时，需要进行滑动，把currentTab移回锚定区域内
    private void scrollToAnchorArea(int position) {
        if (tabCount == 0) {
            return;
        }
        int anchorLeft = scrollOffset;
        // 锚定区域右边界的位置 = 屏幕宽度 - 右边间距
        int anchorRight = DimenUtil.getScreenWidth(getContext()) - scrollOffset;
        int maxScrollX = Math.max(tabsContainer.getChildAt(position).getLeft() - anchorLeft, 0);
        int minScrollX = tabsContainer.getChildAt(position).getRight() - anchorRight;
        if (this.getScrollX() > maxScrollX) {
            ObjectAnimator.ofInt(this, "scrollX", maxScrollX).setDuration(SCROLL_DURATION).start();
        } else if (this.getScrollX() < minScrollX) {
            ObjectAnimator.ofInt(this, "scrollX", minScrollX).setDuration(SCROLL_DURATION).start();
        }
    }

    private void setCurrentSelected(int position) {
        if (null == tabsContainer) {
            return;
        }
        View oldSelected = tabsContainer.getChildAt(currentSelected);
        if (currentSelected != position) {
            View newSelected = tabsContainer.getChildAt(position);
            if (null != oldSelected) {
                oldSelected.setSelected(false);
                if (oldSelected instanceof TextView) {
                    ((TextView) oldSelected).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            tabTextSize);
                }
            }
            if (null != newSelected) {
                newSelected.setSelected(true);
                if (newSelected instanceof TextView) {
                    ((TextView) newSelected).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            tabSelectedTextSize);
                }
            }
            currentSelected = position;
        } else {
            oldSelected.setSelected(true);
            if (oldSelected instanceof TextView) {
                ((TextView) oldSelected).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        tabSelectedTextSize);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || tabCount == 0) {
            return;
        }

        int height = getHeight();

        // draw indicator
        renderIndicator(canvas);

        // draw underline
        paint.setColor(underlineColor);
        canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, paint);
    }

    //region indicator

    private float indicatorCurrentLeft;
    private float indicatorCurrentRight;

    private void renderIndicator(Canvas canvas) {
        // 在onGlobalLayout中可能拿不到indicator的正确位置，故改为在此计算无动画时indicator的位置

        paint.setColor(indicatorColor);
        int height = getHeight();
        canvas.drawRect(indicatorCurrentLeft, height - indicatorHeight, indicatorCurrentRight, height, paint);
    }

    private void setIndicatorPosition(int position, float positionOffset) {
        Pair<Float, Float> targetLeftRight = getIndicatorTargetLeftRight(position, positionOffset);
        indicatorCurrentLeft = targetLeftRight.first;
        indicatorCurrentRight = targetLeftRight.second;
        invalidate();
    }

    // 根据当前选定的tab，得到indicator应该移动到的位置
    private Pair<Float, Float> getIndicatorTargetLeftRight(int position, float positionOffset) {
        View tab = tabsContainer.getChildAt(position);
        Pair<Float, Float> indicator = getIndicatorLeftRight(tab);
        float targetLeft = indicator.first;
        float targetRight = indicator.second;
        // 如果positionOffset不为0，indicator正处于两个tab之间，需进行加权计算得到它的位置
        if (positionOffset > 0f && position < tabCount - 1) {
            View nextTab = tabsContainer.getChildAt(position + 1);
            Pair<Float, Float> indicatorForNextTab = getIndicatorLeftRight(nextTab);
            float left = indicatorForNextTab.first;
            float right = indicatorForNextTab.second;
            targetLeft = (positionOffset * left + (1f - positionOffset) * targetLeft);
            targetRight = (positionOffset * right + (1f - positionOffset) * targetRight);
        }
        return new Pair<>(targetLeft, targetRight);
    }

    private Pair<Float, Float> getIndicatorLeftRight(View tab) {
        float left = tab.getLeft();
        float right = tab.getRight();
        if (indicatorMode == IndicatorMode.WRAP && tab instanceof TextView) {
            TextView tabTextView = (TextView) tab;
            paint.setTextSize(tabTextView.getTextSize());
            float textLength = paint.measureText(tabTextView.getText().toString());
            float middle = (left + right) / 2f;
            left = middle - textLength / 2f;
            right = middle + textLength / 2f;
        }
        return new Pair<>(left, right);
    }

    private boolean isAnimateRunning = false;
    private static final String TARGET_LEFT = "targetLeft";
    private static final String TARGET_RIGHT = "targetRight";

    private void startIndicatorAnimate(final float targetLeft, final float targetRight) {
        // 在indicator超出屏幕范围时，让其从屏幕边界处开始移动
        float move = 0;
        if (indicatorCurrentRight < getScrollX()) {
            move = getScrollX() - indicatorCurrentRight;
        } else if (indicatorCurrentLeft > getScrollX() + DimenUtil.getScreenWidth(getContext())) {
            move = getScrollX() + DimenUtil.getScreenWidth(getContext()) - indicatorCurrentLeft;
        }
        indicatorCurrentLeft += move;
        indicatorCurrentRight += move;

        PropertyValuesHolder valuesHolderLeft = PropertyValuesHolder.ofFloat(
                TARGET_LEFT, indicatorCurrentLeft, targetLeft);
        PropertyValuesHolder valuesHolderRight = PropertyValuesHolder.ofFloat(
                TARGET_RIGHT, indicatorCurrentRight, targetRight);
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(valuesHolderLeft, valuesHolderRight)
                .setDuration(SCROLL_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (indicatorCurrentLeft != targetLeft) {
                    indicatorCurrentLeft = (float) animation.getAnimatedValue(TARGET_LEFT);
                }
                if (indicatorCurrentRight != targetRight) {
                    indicatorCurrentRight = (float) animation.getAnimatedValue(TARGET_RIGHT);
                }
                if (indicatorCurrentLeft == targetLeft && indicatorCurrentRight == targetRight) {
                    isAnimateRunning = false;
                }
                invalidate();
            }
        });
        animator.start();
        isAnimateRunning = true;
    }

    // endregion

    private class PageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentPosition = position;
            currentPositionOffset = positionOffset;
            if (!isAnimateRunning) {
                setIndicatorPosition(currentPosition, currentPositionOffset);
            }
            if (null != delegatePageListener) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            setCurrentSelected(position);
            currentPosition = position;
            currentPositionOffset = 0;
            scrollToAnchorArea(position);
            if (null != delegatePageListener) {
                delegatePageListener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (null != delegatePageListener) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(state);
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public enum ExpandMode {
        WRAP(0), FIXED(1), EXPANDED(2), REACT(3);

        int id;

        ExpandMode(int id) {
            this.id = id;
        }

        public static ExpandMode fromId(int id) {
            for (ExpandMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return WRAP;
        }
    }

    public enum IndicatorMode {
        MATCH(0), WRAP(1);

        int id;

        IndicatorMode(int id) {
            this.id = id;
        }

        public static IndicatorMode fromId(int id) {
            for (IndicatorMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return MATCH;
        }
    }
}
