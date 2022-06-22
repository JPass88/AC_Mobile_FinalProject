package com.cst2335.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SoccerMainActivity<ListViewAdapter> extends AppCompatActivity {
    /**
     *  lastClickedArticle - saves the latest NewsArticle to be clicked from the listview   */
    NewsArticle lastClickedArticle;

    /**
     *  db - SQLiteDatabase to store user favorites */
    SQLiteDatabase db;

    /**
     *  savedUsername - to store username to and from SharedPreference  */
    String savedUsername;

    /**
     *  savedRating - to store star rating to and from SharedPreference  */
    float savedRating;

    /**
     *  SIGN_IN_REQUEST_CODE - used for SoccerSignInActivity    */
    private static final int SIGN_IN_REQUEST_CODE = 1;

    /**
     *  ARTICLE_COUNT - number of articles included in the soccer rss feed  */
    private static final int ARTICLE_COUNT = 20;

    /**
     *  lvAdapter - list adapter used for list of articles  */
    private SoccerListAdapter lvAdapter = new SoccerListAdapter();

    /**
     *  fullArticles - an ArrayList of NewsArticles downloaded from goal.com    */
    private List<NewsArticle> fullArticles = new ArrayList<NewsArticle>();;

    /**
     *  articles - an ArrayList of NewsArticles, will point either to downloaded or favorited   */
    private List<NewsArticle> articles = fullArticles;

    /**
     *  favorites - an ArrayList of NewsArticles that have been saved as favorites  */
    private List<NewsArticle> favorites = new ArrayList<NewsArticle>();

    /**
     *  viewingFavorites - a boolean to track whether articles is currently
     *  pointing to favorites or fullArticles */
    private boolean viewingFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soccer_main);

        // grab all the layout elements
        Button fav = findViewById(R.id.save_button);
        Button read = findViewById(R.id.read_button);
        Button load = findViewById(R.id.load_favs_button);
        TextView dateView = findViewById(R.id.article_date);
        TextView urlView = findViewById(R.id.article_url);
        TextView descView = findViewById(R.id.article_desc);
        RatingBar rb = findViewById(R.id.ratingBar);
        ProgressBar progressBar = findViewById(R.id.download_progress);
        ListView lv = findViewById(R.id.soccer_listview);
        Toolbar tb = findViewById(R.id.toolbar);

        // set article download progress bar visibility to visible
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // attach adapter
        lv.setAdapter(lvAdapter);

        //set up toolbar
        setSupportActionBar(tb);

        // load shared preferences
        SharedPreferences prefs = getSharedPreferences("soccer_prefs", Context.MODE_PRIVATE);
        savedUsername = prefs.getString("username", "");
        savedRating = prefs.getFloat("rating", -1);

        // load rating into rating bar
        if (savedRating >= 0) {
            rb.setRating(savedRating);
        }

        // ask for username if none loaded, otherwise show a Toast with loaded username
        if (savedUsername.equals("")) {
            // ask for username
            Intent goToSignIn = new Intent(this, SoccerSignInActivity.class);
            startActivityForResult(goToSignIn, SIGN_IN_REQUEST_CODE);
        }
        else {
            Toast toast = Toast.makeText(this, savedUsername + " logged in", Toast.LENGTH_SHORT);
            toast.show();
        }

        // on rating bar changed save new rating to shared preferences
        rb.setOnRatingBarChangeListener((ratingBar, rating, fromUser)->{
            // save the rating to shared preferences
            SharedPreferences.Editor edit = prefs.edit();
            edit.putFloat("rating", rating);
            edit.commit();
        });

        // this button will be Favorite or Remove, depending on viewingFavorites
        fav.setOnClickListener(c-> {

            SoccerDBOpener opener = new SoccerDBOpener(this);
            db = opener.getWritableDatabase();
            // button is Favorite, add lastClickedArticle to favorites and save in database
            if (!viewingFavorites) {

                ContentValues cv = new ContentValues();
                cv.put(SoccerDBOpener.ARTICLE_URL, lastClickedArticle.getUrl());
                cv.put(SoccerDBOpener.ARTICLE_TITLE, lastClickedArticle.getTitle());
                cv.put(SoccerDBOpener.ARTICLE_DATE, lastClickedArticle.getDate());
                cv.put(SoccerDBOpener.ARTICLE_DESC, lastClickedArticle.getDescription());
                cv.put(SoccerDBOpener.IMAGE_URL, lastClickedArticle.getImgLink());

                long id = db.insertOrThrow(SoccerDBOpener.TABLE_NAME, null, cv);

                favorites.add(lastClickedArticle);

                Toast toast = Toast.makeText(this, "Saved to Favorites", Toast.LENGTH_SHORT);
                toast.show();
            }
            // button is Remove, delete lastClickedArticle from favorites and database
            else {
                int removed = db.delete(SoccerDBOpener.TABLE_NAME, "_id = " + lastClickedArticle.getId(), null);
                favorites.remove(lastClickedArticle);
                lvAdapter.notifyDataSetChanged();
            }

        });

        // listener to trigger when a title in the list is clicked
        lv.setOnItemClickListener((a, b, pos, id)-> {
            // the article that was clicked
            lastClickedArticle = articles.get(pos);

            // execute image download
            ImageDownload download = new ImageDownload();
            Snackbar.make(findViewById(R.id.soccer_layout), "Image Loading",
                    BaseTransientBottomBar.LENGTH_SHORT).show();
            download.execute(lastClickedArticle);

            // update views with article info
            dateView.setText(lastClickedArticle.getDate());
            urlView.setText(lastClickedArticle.getUrl());
            descView.setText(lastClickedArticle.getDescription());

            // set buttons visible now that an article has been clicked
            fav.setVisibility(View.VISIBLE);
            read.setVisibility(View.VISIBLE);
        });

        // show lastClickedArticle in browser
        read.setOnClickListener(c-> {
            Intent goToBrowser = new Intent(Intent.ACTION_VIEW);
            goToBrowser.setData(Uri.parse(lastClickedArticle.getUrl()));
            startActivity(goToBrowser);
        });

        // this button will be "Show Favorites" or "Back to Articles" depending on viewingFavorites
        load.setOnClickListener(v-> {
            // currently on normal articles, switch to favorites
            if (!viewingFavorites) {

                // get database to read saved favorites from
                SoccerDBOpener opener = new SoccerDBOpener(this);
                db = opener.getReadableDatabase();

                Cursor c = db.rawQuery("SELECT * FROM " + SoccerDBOpener.TABLE_NAME, null);

                // reload fresh from DB every time we switch to favorites
                favorites.clear();

                // load in all saved favorites
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToNext();

                    long id = c.getLong(c.getColumnIndex("_id"));
                    String title = c.getString(c.getColumnIndex(SoccerDBOpener.ARTICLE_TITLE));
                    String date = c.getString(c.getColumnIndex(SoccerDBOpener.ARTICLE_DATE));
                    String url = c.getString(c.getColumnIndex(SoccerDBOpener.ARTICLE_URL));
                    String desc = c.getString(c.getColumnIndex(SoccerDBOpener.ARTICLE_DESC));
                    String imgUrl = c.getString(c.getColumnIndex(SoccerDBOpener.IMAGE_URL));

                    NewsArticle na = new NewsArticle(id, title, url, date, desc, imgUrl);

                    favorites.add(na);
                }

                c.close();


                articles = favorites;
                viewingFavorites = true;
                fav.setText(R.string.soccer_remove_button);
                load.setText(R.string.soccer_show_articles_button);

                lvAdapter.notifyDataSetChanged();
            }
            // switch back to un-favorited articles
            else {
                fav.setText(R.string.soccer_favorite_button);
                load.setText(R.string.soccer_show_favs_button);

                articles = fullArticles;
                viewingFavorites = false;
                lvAdapter.notifyDataSetChanged();
            }
        });

        // download articles from goal.com
        SoccerNews news = new SoccerNews();
        news.execute("https://www.goal.com/en/feeds/news?fmt=rss");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.soccer_help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // there is only one item, so we know which one was selected
        AlertDialog ad = new AlertDialog.Builder(SoccerMainActivity.this).create();
        ad.setTitle("Help");
        ad.setMessage("Click on an article to view details:\nClick Favorite to save and read later:" +
                        "\nClick Read to open in browser:" +
                        "\nUse View Favorites and Back to Articles to toggle saved articles\n" +
                        "Click stars to change rating at any time!");

        ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (d, w) -> {
            d.dismiss();
        });

        ad.show();

        return true;
    }

    /**
     *  getImageAtURL :  Downloads image at url if not already downloaded, otherwise loads from storage
     *  @param url :  a String containing a valid image url from goal.com
     *  @return : a Bitmap of the retrieved image
      */

    private Bitmap getImageAtURL(String url) {
        Bitmap result = null;

        String filename = extractFileNameFromURL(url);

        if (fileExistance(filename)) {
            FileInputStream fis = null;
            try {    fis = openFileInput(filename);   }
            catch (FileNotFoundException e) {    e.printStackTrace();  }
            result = BitmapFactory.decodeStream(fis);
            Log.i("local", "Image found locally");
        }
        else {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection imgConnection = (HttpURLConnection) urlObj.openConnection();
                imgConnection.connect();
                int responseCode = imgConnection.getResponseCode();
                if (responseCode == 200) {
                    result = BitmapFactory.decodeStream(imgConnection.getInputStream());
                    FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    result.compress(Bitmap.CompressFormat.PNG, 80, outputStream);

                    outputStream.flush();
                    outputStream.close();

                    Log.i("local", "Image downloaded");
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            savedUsername = data.getExtras().getString("new_username");
        }

    }

    /**
     * extractFileNameFromURL : cuts the image filename out of the longer url containing attributes
     * @param url - full url of image including web address and attributes
     * @return the image filename
     */
    public String extractFileNameFromURL(String url) {
        String[] pieces = url.split("/");
        String lastPiece = pieces[pieces.length-1];
        String firstPiece = lastPiece.split("\\?")[0];

        return firstPiece;
    }

    /**
     * fileExistance : tests whether a file already exists on the device
     * @param filename - String representing the name of the file whose existence we are testing for
     * @return true if file exists, false otherwise
     */
    public boolean fileExistance(String filename) {
        return getBaseContext().getFileStreamPath(filename).exists();
    }

    /**
     *  SoccerNews:  an AsyncTask to download news articles from the rss feed at goal.com
     */
    private class SoccerNews extends AsyncTask<String, Integer, List<NewsArticle>> {

        @Override
        protected List<NewsArticle> doInBackground(String ...url) {
            int progress = 0;
            int articlesSoFar = 0;

            try {
                URL soccerURL = new URL(url[0]);
                HttpURLConnection soccerConnection = (HttpURLConnection) soccerURL.openConnection();

                InputStream soccerResponse = soccerConnection.getInputStream();

                XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
                xppFactory.setNamespaceAware(false);
                XmlPullParser parser = xppFactory.newPullParser();

                parser.setInput(soccerResponse, "UTF-8");

                String title = null;
                String date = null;
                String link = null;
                String description = null;
                String imgURL = null;

                int eventType = parser.next();

                boolean isItem = false;
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG: {
                            if (isItem) {
                                if (tagName.equals("title")) {
                                    title = parser.nextText();
                                }
                                else if (tagName.equals("pubDate")) {
                                    date = parser.nextText();
                                }
                                else if(tagName.equals("link")) {
                                    link = parser.nextText();
                                }
                                else if(tagName.equals("description")) {
                                    description = parser.nextText();
                                }
                                else if (tagName.equals("media:thumbnail")) {
                                    imgURL = parser.getAttributeValue(null, "url");
                                }
                            }

                            if (tagName.equals("item")){
                                isItem = true;
                            }
                        }break;

                        case XmlPullParser.END_TAG: {
                            if (tagName.equals("item")){
                                isItem = false;

                                if (title != null && date != null && link != null && description != null) {
                                    articles.add(new NewsArticle(-1, title, link, date, description, imgURL));
                                    title = null;
                                    date = null;
                                    link = null;
                                    description = null;
                                    articlesSoFar++;
                                    progress = (int)(((float)articlesSoFar / (float)ARTICLE_COUNT) * 100.0f);
                                    publishProgress(progress);
                                }
                            }
                        }break;
                    }
                    eventType = parser.next();
                }
            }
            catch (IOException | XmlPullParserException e) {     // error with the URL  or opening the connection
                e.printStackTrace();
            }

            return articles;
        }

        @Override
        protected void onPostExecute(List<NewsArticle> newsArticles) {
            super.onPostExecute(newsArticles);

            ProgressBar progressBar = findViewById(R.id.download_progress);
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            lvAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ProgressBar pb = findViewById(R.id.download_progress);
            pb.setProgress(values[0]);
        }
    }

    /**
     * ImageDownload : an AsyncTask to download images from goal.com
     */
    private class ImageDownload extends AsyncTask <NewsArticle, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(NewsArticle[] args) {
            NewsArticle article = args[0];

            Bitmap bitmap;
            bitmap = getImageAtURL(article.getImgLink());

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            super.onPostExecute(bmp);

            ImageView iv = findViewById(R.id.article_img);
            iv.setImageBitmap(bmp);
        }
    }

    /**
     *  NewsArticle : a class to store details about downloaded news articles from goal.com
     */
    private class NewsArticle {
        // -1 indicates that this article has not been stored in the database as a favorite and so
        // has no database id
        private long id =-1;
        private String title;
        private String url;
        private String date;
        private String description;
        private String imgLink;

        public NewsArticle(long id, String title, String url, String date, String description, String imgLink) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.date = date;
            this.description = description;
            this.imgLink = imgLink;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getImgLink() {
            return imgLink;
        }

        public long getId() { return id; }
    }

    /**
     *  SoccerListAdapter : a listview adapter to list articles
     */
    class SoccerListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return articles.size();
        }

        @Override
        public NewsArticle getItem(int position) {
            return articles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            NewsArticle news = getItem(position);
            View listArticle;
            listArticle= inflater.inflate(R.layout.list_article_row, parent, false);

            NewsArticle current = articles.get(position);
            TextView articleTitle = listArticle.findViewById(R.id.list_row_textview);
            articleTitle.setText(current.getTitle());

            return listArticle;
        }
    }
}