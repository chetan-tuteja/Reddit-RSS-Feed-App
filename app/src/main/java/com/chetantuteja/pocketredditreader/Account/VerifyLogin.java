package com.chetantuteja.pocketredditreader.Account;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VerifyLogin {

    @SerializedName("json")
    @Expose
    private JSON json;

    public JSON getJson() {
        return json;
    }

    public void setJson(JSON json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "VerifyLogin{" +
                "json=" + json +
                '}';
    }
}
