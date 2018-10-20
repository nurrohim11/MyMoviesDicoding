package com.example.nurrohim.movies.service;

import com.example.nurrohim.movies.model.detail.DetailModel;
import com.example.nurrohim.movies.model.search.SearchModel;
import com.example.nurrohim.movies.model.trailer.Trailer;
import com.example.nurrohim.movies.model.upcoming.UpcomingModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APICall {

    @GET("movie/popular?")
    Call<SearchModel> getPopularMovie(@Query("page") int page);

    @GET("search/movie")
    Call<SearchModel> getSearchMovie(@Query("page") int page, @Query("query") String query);

    @GET("movie/{movie_id}")
    Call<DetailModel> getDetailMovie(@Path("movie_id") String movie_id);

    @GET("movie/upcoming")
    Call<UpcomingModel> getUpcomingMovie();

    @GET("movie/{movie_id}")
    Call<Trailer> getTrailer(@Path("movie_id") String movie_id);

}
