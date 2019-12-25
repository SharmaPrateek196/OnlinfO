package com.example.onlinfo;

public class Commnt {
    private String post_id;
    private String user_name;
    private String post_time;
    private String url;
    private long time_for_order_by;

    public Commnt(){}

    public Commnt(String post_id, String user_name, String post_time, String url,long time_for_order_by) {
        this.post_id = post_id;
        this.user_name = user_name;
        this.post_time = post_time;
        this.url = url;
        this.time_for_order_by=time_for_order_by;
    }

    public long getTime_for_order_by() {
        return time_for_order_by;
    }

    public void setTime_for_order_by(long time_for_order_by) {
        this.time_for_order_by = time_for_order_by;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
