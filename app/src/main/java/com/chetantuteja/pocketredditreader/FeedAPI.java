package com.chetantuteja.pocketredditreader;

import com.chetantuteja.pocketredditreader.Account.VerifyLogin;
import com.chetantuteja.pocketredditreader.Comments.CheckComment;
import com.chetantuteja.pocketredditreader.datamodels.Feed;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface FeedAPI {
    String BASE_URL = "https://www.reddit.com/r/";

    @GET("{feed_name}/.rss")
    Call<Feed> getFeed(@Path("feed_name")String feed_name);

    @POST("{user}")
    Call<VerifyLogin> signIN(
            @HeaderMap Map<String,String> headers,
            @Path("user") String username,
            @Query("user") String user,
            @Query("passwd") String password,
            @Query("api_type") String type
            );

    @POST("{comment}")
    Call<CheckComment> submitComment(
            @HeaderMap Map<String,String> headers,
            @Path("comment") String comment,
            @Query("parent") String parent,
            @Query("amp;text") String text
    );
}
