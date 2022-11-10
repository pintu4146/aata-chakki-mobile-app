package com.example.anotherfirestoretutorial;

import com.google.firebase.firestore.Exclude;

public class Note {
    private  String DocumentID;
    private String  title;
    private String description;
    int priority;
    private String mImageUriFire;

    public Note()
    {

    }




    Note(String title, String description, int priority)
    {
        this.priority=priority;
        this.title=title;
        this.description=description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }



@Exclude
    public String getDocumentID() {
        return DocumentID;
    }

    public void setDocumentID(String documentID) {
        DocumentID = documentID;
    }



   public String getDescription() {
        return description;
    }

   public    String getTitle() {
        return title;
    }
}
