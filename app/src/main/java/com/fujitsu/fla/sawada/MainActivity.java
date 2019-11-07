package com.fujitsu.fla.sawada;

import java.util.*;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final String ALGOLIA_QUERY = "https://hn.algolia.com/api/v1/search_by_date?tags=story&page=";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CREATED_AT = "created_at";

    ListView m_lstAlgoria;
    // constrain another loading while getting data
    private boolean m_isLoading = false;

    ArrayList<HashMap<String, String>> m_vData = new ArrayList<HashMap<String, String>>();
    int m_pageNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        updateTitle();

        // Set up the listview
        m_lstAlgoria = (ListView) this.findViewById(R.id.lstAlgolia);
        m_lstAlgoria.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (m_isLoading)
                    return;

                // If list is scrolled to the bottom, load next page
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    m_pageNo++;
                    feedFromAlgolia();
                }
            }
        });

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                m_vData,
                android.R.layout.simple_list_item_2,
                new String[]{TAG_TITLE, TAG_CREATED_AT},
                new int[]{android.R.id.text1, android.R.id.text2});

        m_lstAlgoria.setAdapter(adapter);
        // End of setup for the listview
    }

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

    /**
     * Change the title in the navibar
     */
    private void updateTitle() {
        setTitle("Count:" + m_vData.size());
    }

    /**
     * Get data from Algolia page
     */
    private void feedFromAlgolia() {

        m_isLoading = true;
        final AsyncHttpClient client = new AsyncHttpClient();
        client.get(ALGOLIA_QUERY + m_pageNo, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {

                    JSONObject resp = new JSONObject(response.toString());
                    JSONArray data = resp.getJSONArray("hits");

                    // read data
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject obj = data.getJSONObject(i);
                        HashMap map = new HashMap();
                        map.put(TAG_TITLE, obj.getString(TAG_TITLE));
                        map.put(TAG_CREATED_AT, obj.getString(TAG_CREATED_AT));
                        m_vData.add(map);
                    }

                    m_lstAlgoria.invalidateViews();

                    updateTitle();

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    m_isLoading = false;
                }
            }
        });
    }
}
