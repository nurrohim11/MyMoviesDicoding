package com.example.nurrohim.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nurrohim.movies.adapter.SearchAdapter;
import com.example.nurrohim.movies.service.APIClient;
import com.example.nurrohim.movies.model.search.ResultsItem;
import com.example.nurrohim.movies.model.search.SearchModel;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity  implements MaterialSearchBar.OnSearchActionListener, SwipeRefreshLayout.OnRefreshListener, PopupMenu.OnMenuItemClickListener{
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;

    @BindView(R.id.search_bar)
    MaterialSearchBar search_bar;

    @BindView(R.id.rv_movielist)
    RecyclerView rv_movielist;

    private SearchAdapter adapter;
    private List<ResultsItem> list = new ArrayList<>();

    private Call<SearchModel> apiCall;
    private APIClient apiClient = new APIClient();

    private String movie_title = "";
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        search_bar.setOnSearchActionListener(this);
        swipe_refresh.setOnRefreshListener(this);

        search_bar.inflateMenu(R.menu.menu_main);
        search_bar.getMenu().setOnMenuItemClickListener(this);
        search_bar.setCardViewElevation(10);
        search_bar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + search_bar.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });


        setupList();
        setupListScrollListener();
        startRefreshing();

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

   /* @Override
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
    }*/

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_refresh:
                onRefresh();
                break;
        }

        return false;
    }
    private void setupList() {
        adapter = new SearchAdapter();
        rv_movielist.addItemDecoration(new DividerItemDecoration(this, VERTICAL));
        rv_movielist.setLayoutManager(new LinearLayoutManager(this));
        rv_movielist.setAdapter(adapter);
    }

    private void setupListScrollListener() {
        rv_movielist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int totalItems = layoutManager.getItemCount();
                int visibleItems = layoutManager.getChildCount();
                int pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition();

                if (pastVisibleItems + visibleItems >= totalItems) {
                    if (currentPage < totalPages) currentPage++;
                    startRefreshing();
                }
            }
        });
    }
    private void loadData(final String movie_title) {
        getSupportActionBar().setSubtitle("");

        if (movie_title.isEmpty()) apiCall = apiClient.getService().getPopularMovie(currentPage);
        else apiCall = apiClient.getService().getSearchMovie(currentPage, movie_title);

        apiCall.enqueue(new Callback<SearchModel>() {
            @Override
            public void onResponse(Call<SearchModel> call, Response<SearchModel> response) {
                if (response.isSuccessful()) {
                    totalPages = response.body().getTotalPages();
                    List<ResultsItem> items = response.body().getResults();
                    showResults(response.body().getTotalResults());

                    if (currentPage > 1) adapter.updateData(items);
                    else adapter.replaceAll(items);

                    stopRefrehing();
                } else loadFailed();
            }

            @Override
            public void onFailure(Call<SearchModel> call, Throwable t) {
                loadFailed();
            }
        });
    }

    private void loadFailed() {
        stopRefrehing();
        Toast.makeText(MainActivity.this, "Failed to load data.\nPlease check your Internet connections!", Toast.LENGTH_SHORT).show();
    }

    private void startRefreshing() {
        if (swipe_refresh.isRefreshing()) return;
        swipe_refresh.setRefreshing(true);

        loadData(movie_title);
    }

    private void stopRefrehing() {
        if (swipe_refresh.isRefreshing()) swipe_refresh.setRefreshing(false);
    }

    private void showResults(int totalResults) {
        String results;

        String formatResults = NumberFormat.getIntegerInstance().format(totalResults);

        if (totalResults > 0) {
            results = "I found " + formatResults + " movie" + (totalResults > 1 ? "s" : "") + " for you :)";
        } else results = "Sorry! I can't find " + movie_title + " everywhere :(";

        getSupportActionBar().setSubtitle(results);
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        movie_title = String.valueOf(text);
        onRefresh();
    }

    @Override
    public void onButtonClicked(int buttonCode) {

    }
    @Override
    public void onRefresh() {
        currentPage = 1;
        totalPages = 1;

        stopRefrehing();
        startRefreshing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiCall != null) apiCall.cancel();
    }

}
