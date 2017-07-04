# page_sliding

仿网易严选的标签栏滑动，标签下划线带动画效果。
![](http://img.blog.csdn.net/20170704180158533?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2xrZGI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
 
标签栏是一个非常常见的控件，似乎也是一个比较简单的控件，但如果在标签下方加个下划线的话，就还是可以玩出挺多花来的。网易严选的标签栏，就做的很不错，里面隐藏着诸多细节：
手动滑动页面，下划线会跟着滑动。
选择一个标签后，下划线会有滑动过去的动画。
选择最左端或最右端的标签，标签栏会进行滑动，使得标签向中间靠拢（如果可以滑的话）。

仔细分析下，需要在简单标签栏的基础上实现以下逻辑：
画出下划线。
监听手动滑动页面事件，实时更新下划线位置。
切换标签时，开始下划线滑动的动画，并判断是否要同时滑动标签栏。

![](http://img.blog.csdn.net/20170704182631977?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2xrZGI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
我做了一个样例程序，其中的较难点在于计算下划线的位置，和下划线的动画效果。 

```
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
    }```
    
    上面是计算下划线位置的代码，通过传入在onPageScrolled()中获得的position和positionOffset，计算下划线是在某一个标签下，或者某两个标签之间的位置。需要注意的是，由于各标签的长度可能不一，所以下划线的长度在滑动中也可能发生变化，所以需分别计算下划线的left和right。 
    ```
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
        ```
    }
    这是切换标签时下划线运行滑动动画的代码，使用ValueAnimator实现，并且对下划线超出边界的情况做了特殊处理，以防止滑动距离过大时，滑动速度过快。
    
    更多细节，请下载项目文件。
