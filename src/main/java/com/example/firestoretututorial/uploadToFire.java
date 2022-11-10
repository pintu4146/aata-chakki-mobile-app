package com.example.firestoretututorial;

public class uploadToFire {
    private  String mName;
    private String mImageUrl;

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public  uploadToFire(){

    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public  uploadToFire(String mName, String mImageUrl)
    {
        this.mImageUrl=mImageUrl;
        this.mName = mName;
    }
}

