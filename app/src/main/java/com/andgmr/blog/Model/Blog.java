package com.andgmr.blog.Model;

/**
 * Created by drago on 9/16/2017.
 */

public class Blog {
    public String title, desc, image, timestamp, userid;

    public Blog() {

    }

    public Blog(String title, String desc, String image, String timestamp, String userid) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.timestamp = timestamp;
        this.userid = userid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return desc;
    }

    public void setDescription(String description) {
        this.desc = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
