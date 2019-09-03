package com.chetantuteja.pocketredditreader.Account;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JSON {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "JSON{" +
                "data=" + data +
                '}';
    }
}
