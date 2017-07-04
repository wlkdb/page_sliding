package com.example.lg.page_sliding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SlidingFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private LessonPagerAdapter adapter;
    private SlidingTabView slidingTabView;

    private int lastLabelId;
    private List<Label> labelList = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sliding, null);

        adapter = new LessonPagerAdapter(getChildFragmentManager());
        slidingTabView = (SlidingTabView) view.findViewById(R.id.sliding_tab);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(this);

        lastLabelId = 0;
        for (int i = 0 ;i < 10; i++) {
            labelList.add(new Label(i, "标签" + i));
        }
        renderChannelTabs(labelList);
        return view;
    }

    public void renderChannelTabs(List<Label> channelList) {
        this.labelList = channelList;
        adapter.notifyDataSetChanged();
        slidingTabView.setViewPager(viewPager);
        viewPager.setCurrentItem(0, false);
    }

    //endregion
    //region pager

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (null != labelList) {
            Label channel = labelList.get(position);
            lastLabelId = channel.getId();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void hideLoading() {
    }

    //endregion

    private SparseArrayCompat<Fragment> cachedFragments = new SparseArrayCompat<>();
    private SparseArrayCompat<SubPresenter> cachedPresenters = new SparseArrayCompat<>();

    private class LessonPagerAdapter extends FragmentStatePagerAdapter {

        public LessonPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Label channel = labelList.get(position);
            SubFragment fragment = (SubFragment) Fragment.instantiate(
                    getActivity(), SubFragment.class.getName(), null);
            cachedFragments.put(position, fragment);
            SubPresenter lessonListPresenter = cachedPresenters.get(channel.getId());
            if (lessonListPresenter == null) {
                lessonListPresenter = new SubPresenter();
                cachedPresenters.put(channel.getId(), lessonListPresenter);
            }
            fragment.setPresenter(lessonListPresenter);
            return fragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SubFragment fragment = (SubFragment) super.instantiateItem(
                    container, position);
            fragment.setOnDataLoadListener(new SubFragment.OnDataLoadedListener() {
                @Override
                public void onDataLoaded() {
                    hideLoading();
                }
            });
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return labelList == null ? 0 : labelList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return labelList.get(position).getName();
        }
    }
}