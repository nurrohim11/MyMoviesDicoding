package com.example.nurrohim.movies.model.trailer;

import com.google.gson.annotations.SerializedName;

public class Trailer  {

    @SerializedName("id")
    private int id;

    public Trailer() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
