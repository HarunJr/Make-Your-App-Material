package com.example.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by HARUN on 3/14/2018.
 */

public class ArticleRvAdapter extends RecyclerView.Adapter<ArticleRvAdapter.ArticleViewHolder>{
    private static final String TAG = ArticleListActivity.class.toString();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private Cursor mCursor;
    public static Context mContext;

    public ArticleRvAdapter(Cursor cursor, Context context) {
        mCursor = cursor;
        mContext = context;
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        final ArticleViewHolder vh = new ArticleViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long position = vh.getAdapterPosition();
                Log.w(TAG, "onClick "+position);
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(position)));

            }
        });
        return vh;
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder{
        ImageView thumbnailView;
        TextView titleView;
        TextView subtitleView;
        TextView authorView;

        ArticleViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.article_image_item);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
            authorView = (TextView) view.findViewById(R.id.article_author);
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

    @Override
    public void onBindViewHolder(ArticleRvAdapter.ArticleViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();
        holder.subtitleView.setText(Html.fromHtml(
                DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()));
        holder.authorView.setText(mCursor.getString(ArticleLoader.Query.AUTHOR));

        if (holder.thumbnailView != null){
            loadImage(mCursor.getString(ArticleLoader.Query.THUMB_URL), holder.thumbnailView, mContext);
        }
    }

    public static void loadImage(String url, ImageView imageView, Context mContext) {
        Log.w(TAG, "loadImage " + url);

        Picasso.with(mContext)
                .load(url)
                .networkPolicy(isNetworkAvailable(mContext)? NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                .error(android.R.drawable.stat_notify_error)
                .fit()
                .into(imageView);
    }

    public static boolean isNetworkAvailable(Context application) {
        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        Log.w(TAG, "isNetworkAvailable" + networkInfo);
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }


}
