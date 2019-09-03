package com.chetantuteja.pocketredditreader;

public class Post {
    private String title;
    private String author;
    private String postDate;
    private String postURL;
    private String thumbnailURL;
    private String id;

    public Post(String title, String author, String postDate, String postURL, String thumbnailURL, String id) {
        this.title = title;
        this.author = author;
        this.postDate = postDate;
        this.postURL = postURL;
        this.thumbnailURL = thumbnailURL;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getPostURL() {
        return postURL;
    }

    public void setPostURL(String postURL) {
        this.postURL = postURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    @Override
    public String toString() {
        return "Post{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", postDate='" + postDate + '\'' +
                ", postURL='" + postURL + '\'' +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
