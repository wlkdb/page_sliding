package com.example.lg.page_sliding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

public class SubFragment extends Fragment {

    interface OnDataLoadedListener {

        void onDataLoaded();
    }

    private OnDataLoadedListener onDataLoadListener;
    private SubPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub, null);
        TextView titleView = (TextView) view.findViewById(R.id.content);
        titleView.setText(new Random().nextInt(100) + "");
        return view;
    }

    public void setPresenter(SubPresenter lessonListPresenter) {
        presenter = lessonListPresenter;
    }

    public void setOnDataLoadListener(OnDataLoadedListener onDataLoadListener) {
        this.onDataLoadListener = onDataLoadListener;
    }
}
