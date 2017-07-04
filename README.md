# page_sliding
仿网易严选的标签栏滑动，标签下划线带动画效果

标签栏是一个非常常见的控件，似乎也是一个比较简单的控件，但如果在标签下方加个下划线的话，就还是可以玩出挺多花来的。网易严选的标签栏，就做的很不错，里面隐藏着诸多细节：
手动滑动页面，下划线会跟着滑动。
选择一个标签后，下划线会有滑动过去的动画。
选择最左端或最右端的标签，标签栏会进行滑动，使得标签向中间靠拢（如果可以滑的话）。

仔细分析下，需要在简单标签栏的基础上实现以下逻辑：
画出下划线。
监听手动滑动页面事件，实时更新下划线位置。
切换标签时，开始下划线滑动的动画，并判断是否要同时滑动标签栏。

详见代码。