package com.example.xyzreader.ui;


import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ArticleDetail";
    public static final String ITEM_POSITION = "position";
    private Cursor mCursor;


    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private int mMutedColor = 0xFF333333;
    private ImageView mPhotoView;
    TextView title;
    TextView bylineView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    private final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);
    private final SimpleDateFormat outputFormat = new SimpleDateFormat();
    private int mPosition;
    CollapsingToolbarLayout collapsingToolbarLayout;
    String titleText;
    AppBarLayout appBarLayout;
    FloatingActionButton shareFab;
    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_article_detail);
        mPosition = 0;

        Toolbar toolbar = findViewById(R.id.toolbar_detail_activity);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mPager = findViewById(R.id.pager);

        title = findViewById(R.id.article_title);
        bylineView = findViewById(R.id.article_byline);
        collapsingToolbarLayout = findViewById(R.id.htab_collapse_toolbar);
        appBarLayout = findViewById(R.id.appbar);
        shareFab = findViewById(R.id.fab_share);


        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                if (getIntent().hasExtra(ITEM_POSITION)) {
                    mPosition = getIntent().getExtras().getInt(ITEM_POSITION, 0);
                }
            }
        }

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mPosition = position;
                displayTitleDate();
            }
        });

        mPhotoView = findViewById(R.id.photo);


        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(titleText);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }

            }
        });

        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, titleText);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent,getResources().getString(R.string.share)));
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;


        displayTitleDate();

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),mCursor);

        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setCurrentItem(mPosition);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }


    private class MyPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {

        private final WeakReference<Cursor> weakReference;
        public MyPagerAdapter(android.support.v4.app.FragmentManager fm,Cursor cursor) {
            super(fm);
            weakReference = new WeakReference<>(cursor);
        }


        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            weakReference.get().moveToPosition(position);
            return ArticleDetailFragment.newInstance(weakReference.get().getString(ArticleLoader.Query.BODY));
        }

        @Override
        public int getCount() {
            return weakReference.get().getCount();
        }
    }

    private Date parsePublishedDate() {
            try {
                String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
                return dateFormat.parse(date);
            } catch (ParseException ex) {
                Log.e(TAG, ex.getMessage());
                Log.i(TAG, "passing today's date");
                return new Date();
            }
            }

    public void displayTitleDate() {
        mCursor.moveToPosition(mPosition);

        ImageLoaderHelper.getInstance(this).getImageLoader()
                            .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                    Bitmap bitmap = imageContainer.getBitmap();
                                    if (bitmap != null) {
                                        Palette p = Palette.generate(bitmap, 12);
                                        mMutedColor = p.getDarkMutedColor(0xFF333333);
                                        mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError volleyError) {

                                }
                            });

        titleText = mCursor.getString(ArticleLoader.Query.TITLE);
        title.setText(titleText);


        Date date = parsePublishedDate();
        if (!date.before(START_OF_EPOCH.getTime())) {
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            date.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

        } else {
            // If date is before 1902, just show the string
            bylineView.setText(Html.fromHtml(
                    outputFormat.format(date) + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

        }
    }
}
