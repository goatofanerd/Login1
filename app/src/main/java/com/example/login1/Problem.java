package com.example.login1;

import android.net.Uri;

import java.util.ArrayList;

public class Problem {
    private String problem;
    private String caption;

    private ArrayList<Uri> images;

    public Problem(String problem, String caption, ArrayList<Uri> images) {
        this.problem = problem;
        this.caption = caption;

        this.images = images;
    }

    public Problem(String problem, String caption) {
        this.problem = problem;
        this.caption = caption;

        images = new ArrayList<>();
    }

    public String getProblem() {
        return problem;
    }

    public String getCaption() {
        return caption;
    }

    public ArrayList<Uri> getImages() {
        return images;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public void setImages(ArrayList<Uri> images) {
        this.images = images;
    }

    public void addImage(Uri uri) {
        images.add(uri);
    }
}
