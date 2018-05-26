package com.example.xyzreader.ui;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment {
    private static final String TAG = "ArticleDetailFragment";

    private View mRootView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(String body) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString("body", body);
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        TextView bodyView = mRootView.findViewById(R.id.article_body);

        String article = "";
        if (getArguments() != null && getArguments().containsKey("body")) {
            article = getArguments().getString("body");
            bodyView.setText(article);

        }
        return mRootView;
    }


}
