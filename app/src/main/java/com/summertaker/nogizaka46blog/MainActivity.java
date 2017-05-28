package com.summertaker.nogizaka46blog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.nogizaka46blog.common.BaseActivity;
import com.summertaker.nogizaka46blog.common.BaseApplication;
import com.summertaker.nogizaka46blog.common.Config;
import com.summertaker.nogizaka46blog.data.Article;
import com.summertaker.nogizaka46blog.parser.Nogizaka46Parser;
import com.summertaker.nogizaka46blog.util.EndlessScrollListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements BlogInterface {

    private Context mContext;
    private Activity mActivity;

    private ProgressBar mPbLoading;
    private LinearLayout mLoLoadMore;

    private ListView mListView;
    private BlogAdapter mAdapter;
    private ArrayList<Article> mArticles;

    private int mCurrentPage = 1;
    private int mMaxPage = 15;
    private boolean mIsLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = MainActivity.this;
        mActivity = (Activity) mContext;

        mPbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        mPbLoading.getIndeterminateDrawable().setColorFilter(Config.PROGRESS_BAR_COLOR, PorterDuff.Mode.MULTIPLY);

        mLoLoadMore = (LinearLayout) findViewById(R.id.loLoadMore);
        ProgressBar pbLoadMore = (ProgressBar) findViewById(R.id.pbLoadMore);
        pbLoadMore.getIndeterminateDrawable().setColorFilter(Config.PROGRESS_BAR_COLOR_MORE, PorterDuff.Mode.MULTIPLY);

        mArticles = new ArrayList<>();

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new BlogAdapter(mContext, mArticles, this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onContentClick(position);
            }
        });
        mListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                //Log.e(TAG, "onLoadMore().page: " + page + " / " + mMaxPage);
                if (mMaxPage == 0 || mCurrentPage <= mMaxPage) {
                    loadData();
                    return true; // ONLY if more data is actually being loaded; false otherwise.
                } else {
                    return false;
                }
            }
        });

        loadData();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    private void loadData() {
        if (mIsLoading) {
            return;
        }

        mIsLoading = true;


        String url = "http://blog.nogizaka46.com/";
        if (mCurrentPage > 1) {
            url += "?p=" + mCurrentPage;
            mLoLoadMore.setVisibility(View.VISIBLE);
        }

        final String blogUrl = url;

        StringRequest request = new StringRequest(Request.Method.GET, blogUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e(TAG, response);
                        parseData(blogUrl, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", Config.USER_AGENT_WEB);
                return headers;
            }
        };

        // Adding request to request queue
        BaseApplication.getInstance().addToRequestQueue(request);
    }

    private void parseData(String blogUrl, String response) {
        ArrayList<Article> articles = new ArrayList<>();

        Nogizaka46Parser nogizaka46Parser = new Nogizaka46Parser();
        nogizaka46Parser.parseBlogList(response, articles);

        mArticles.addAll(articles);

        renderData();
    }

    private void renderData() {
        if (mCurrentPage == 1) {
            mPbLoading.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mLoLoadMore.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();

        //mBaseToolbar.setTitle(mTitle + " ( " + mCurrentPage + " / " + mMaxPage + " )");

        mIsLoading = false;
        mCurrentPage++;
    }

    @Override
    public void onImageClick(int position, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
            startActivity(intent);
            //startActivityForResult(intent, 100);
            //mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onContentClick(int position) {
        Article article = (Article) mAdapter.getItem(position);
        String url = article.getUrl();
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            //startActivityForResult(intent, 100);
            //mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //mActivity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
