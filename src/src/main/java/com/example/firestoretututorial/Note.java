package com.example.firestoretututorial;

public class Note {
    private String title;
    private  String description;
    private int priority;
    private String mImageUriFire;



    private String mobileNumber;

    public    Note()
    {
            //empty constructor
    }

    public String getmImageUriFire() {
        return mImageUriFire;
    }

    public void setmImageUriFire(String mImageUriFire) {
        this.mImageUriFire = mImageUriFire;
    }

    Note(String title, String description, int priority, String mobileNumber, String mImageUriFire)
    {
        this.mImageUriFire = mImageUriFire;
        this.mobileNumber=mobileNumber;
        this.title=title;
        this.description=description;
        this.priority=priority;
    }
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }




    public String getTitle() {
        return title;
    }

//    public void setTitle(String title) {
//        this.title = title;
//    }

    public String getDescription() {
        return description;
    }

//    public void setDescription(String description) {
//        this.description = description;
//    }
}
