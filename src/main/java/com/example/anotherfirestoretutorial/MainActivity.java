package com.example.anotherfirestoretutorial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
TextView textView;
    EditText editText;
    ImageView imageView;
    private static  final String KEY_TITLE = "title";
    private static  final String KEY_Description = "description";
private FirebaseFirestore db = FirebaseFirestore.getInstance();
private FirebaseStorage storage = FirebaseStorage.getInstance();
private StorageReference storageReference =storage.getReference();
private CollectionReference Notebookref=db.collection("Notebook");
private DocumentReference noteref = db.collection("Notebook").document(
        "uZv0hbALWopnDQeAtMqC");
private ListenerRegistration notelistner;
int otp;

String str;
private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
private DatabaseReference mDatabaseref = firebaseDatabase.getReference();
private  DatabaseReference  childreference = mDatabaseref.child("uploads");
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       textView= findViewById(R.id.textView);
        editText=(EditText) findViewById(R.id.enterotp);
        imageView = findViewById(R.id.imageView);



    }

    @Override
    protected void onStart() {
        super.onStart();
        notelistner=noteref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e !=null)
                {
                    Toast.makeText(MainActivity.this, "error while loading", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(documentSnapshot.exists())
                {
                    Note note = documentSnapshot.toObject(Note.class);// revise this
                    String title= note.getTitle();
                    String description = note.getDescription();
                    int priority=note.getPriority();
                    textView.setText("title:"+title+"\n"+"description:"+description+"\nPriority:- "+priority);
                }
            }
        });
//       notelistner= noteref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                if (e!= null)
//                {
//                    Toast.makeText(MainActivity.this, "error occured", Toast.LENGTH_SHORT).show();
////                    Log.d(TAG, e.toString());
//                    return;
//                }
//                if  ( documentSnapshot != null && documentSnapshot.exists())
//                {
//                    Note note = documentSnapshot.toObject(Note.class);
//                    String title= note.getTitle();
//                    String description = note.getDescription();
//                    textView.setText("title:"+title+"description:"+description);
//                }
//            }
//        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        notelistner.remove();
    }

    public void loadNote(View view) {
       //  otp =(int) (Math.random()*9000)+1000;
        str=editText.getText().toString();
        otp=Integer.parseInt(str);
        if (str.isEmpty())
        {
            Toast.makeText(this, "Enter OTP", Toast.LENGTH_LONG).show();
        }
        else
            {
                //Picasso.get().load("gs://firestoretututorial.appspot.com/uploads/1599560758790.jpg").into(imageView);
         Notebookref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
             @Override
             public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                 String data="";
                 for (QueryDocumentSnapshot documentSnapshots: queryDocumentSnapshots)
                 {

                        Note note =documentSnapshots.toObject(Note.class);
                        note.setDocumentID(documentSnapshots.getId());
                        String documentId = note.getDocumentID();
                        String title = note.getTitle();
                        String description = note.getDescription();
                        int priority=note.getPriority();

                        if(otp == priority){
                        data +="title :" +title +"\nDescription :" + description+"\n" +
                                "OTP:-"+priority+"\n\n";
                            retrieve(priority);
                        }


                 }
                 textView.setText(data);
             }
         });
        Toast.makeText(this, "pressed", Toast.LENGTH_SHORT).show();
        Query query=db.collection("Notebook").whereEqualTo("priority",str);

    }
     }

    private void retrieve(int priority) {
         String str=String.valueOf(priority);
         String imageName=str+".jpg";
        storageReference.child("uploads/"+imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
