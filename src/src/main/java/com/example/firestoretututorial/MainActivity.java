package com.example.firestoretututorial;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainACtivity";
    private static final String KEY_TITLE = "title";
    private static final String KEY_Description = "description";
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int PICK_IMAGE_REQUEST =1 ;
    private EditText editText_tittle;
    private EditText editText_Description;
    private EditText mobileno;
    private ImageView photo;
    private Button camera, upload,file;
    private Bitmap bimage;
    String SENT = "SMS SENT ";
    String DELIVERED = "SMS DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    TextView textView_display;
    //firebase

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseReference;
 //firestore begai
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference Notebookref = db.collection("Notebook");
    private DocumentReference noteref = db.collection("Notebook").document("my note");
    private String priority;
    int otp1;
    int MY_PERMISSION_REQUEST_SEND = 1;
    int CAMERA_PERMISSION_CODE = 1;
    String currentPhotoPath;
    private ProgressBar mprogressBar;
    private  Uri mImageUri;
    private String mImageUriFirebase;
    private StorageTask muploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText_tittle = findViewById(R.id.edittext_title);
        editText_Description = findViewById(R.id.edittext_description);
        mobileno = findViewById(R.id.mobileNo);
        photo = findViewById(R.id.photo);
        camera = findViewById(R.id.camera);
        upload = findViewById(R.id.upload);
        file = findViewById(R.id.file);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("uploads");
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reqest permission for camera
                askCameraPermission();

            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (muploadTask != null && muploadTask.isInProgress())
                {
                    Toast.makeText(MainActivity.this, "Wait upload is in progress", Toast.LENGTH_SHORT).show();
                }else {
                    fileUpload();
                }
            }
        });
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileChooser();
            }
        });
//        file.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fileChooser();
//            }
//        });

    }
    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void fileUpload() {
        if(mImageUri!=null){
            StorageReference fileReference = mStorageRef.child(priority+ "."+getFileExtension(mImageUri)) ;

            muploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mImageUriFirebase=taskSnapshot.getUploadSessionUri().toString();

                    uploadToFire loadToFire = new uploadToFire(editText_tittle.getText().toString().trim(),mImageUriFirebase);

String uploadId = mDatabaseReference.push().getKey();
mDatabaseReference.child(uploadId).setValue(loadToFire);
                    Toast.makeText(MainActivity.this, "uploadSuccessful", Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                }
            });
        }else
        {
            Toast.makeText(this, "no file Selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void fileChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,PICK_IMAGE_REQUEST);
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

        } else {

           openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openCamera();
                Toast.makeText(this, "open camesa method cacllled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void openCamera() {

        Toast.makeText(this, "open camera method callled", Toast.LENGTH_SHORT).show();
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera,CAMERA_REQUEST_CODE);
        Toast.makeText(this, "camera open requested", Toast.LENGTH_SHORT).show();
    }



//    private void upload(Uri uri) {
//
//StorageReference filepath = mStorageRef.child("photos").child(uri.getLastPathSegment());
//filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//    @Override
//    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//        Toast.makeText(MainActivity.this, "UPLOAD finished", Toast.LENGTH_SHORT).show();
//    }
//}).addOnFailureListener(new OnFailureListener() {
//    @Override
//    public void onFailure(@NonNull Exception e) {
//        Toast.makeText(MainActivity.this, "Image loading Failed", Toast.LENGTH_SHORT).show();
//    }
//});
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() != null  ){
//            bimage = (Bitmap) data.getExtras().get("data");
//            photo.setImageBitmap(bimage);

                 mImageUri = data.getData();
               Picasso.get().load(mImageUri).into(photo);


            }

//        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK )
//        {
//            Uri uri =data.getData();
//            Picasso.get().load(uri).into(photo);
//        }
//        else {
//            Toast.makeText(this, "data empty", Toast.LENGTH_SHORT).show();
//        }

//            StorageReference filepath = mStorageRef.child("photos").child(uri.getLastPathSegment());
//            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Toast.makeText(MainActivity.this, "UPLOAD finished", Toast.LENGTH_SHORT).show();
//                }
//            });
           // upload( uri);

        }




    //    @Override
//    protected void onStop() {
//        super.onStop();//        Map<String, String> note = new HashMap<>();
////        note.put(KEY_TITLE, title);
//        super.onPause();
//        String title = editText_tittle.getText().toString();
//        String 6 = editText_Description.getText().toString();
//        note.put(KEY_Description, description);
//        noteref.set(note)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(MainActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MainActivity.this, "note saving failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

    //saveing notes to the databse;
    public void saveNote(View view) {
     String title = editText_tittle.getText().toString();
        String description = editText_Description.getText().toString();
        String mobileNum=mobileno.getText().toString();
       priority= genRandomotp();


       int otp1=Integer.parseInt(priority.toString());

        if (title.trim().isEmpty() || description.trim().isEmpty() || mobileNum.trim().isEmpty()) {
            Toast.makeText(this, "PLEASE FILL ALL THE DETAILS", Toast.LENGTH_LONG).show();
        } else {
            Note note = new Note(title,description,otp1,mobileNum,mImageUriFirebase);
            Notebookref.add(note).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(MainActivity.this, "NOTE SAVED ", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "FAILED TO SAVE", Toast.LENGTH_SHORT).show();
                }
            });
//                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                        @Override
//                        public void onSuccess(DocumentReference documentReference) {
//                            Toast.makeText(MainActivity.this, "NOTE SAVED", Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(MainActivity.this, "Failed to save ", Toast.LENGTH_SHORT).show();
//                }
//            });
            sendOtp();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        smsDeliveredReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS SENT", Toast.LENGTH_SHORT).show();break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "Generic Failure", Toast.LENGTH_SHORT).show();break;

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.this, "NO service", Toast.LENGTH_SHORT).show();break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.this, "Null PDU", Toast.LENGTH_SHORT).show();break;
                    case  SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.this, "RADIO OFF", Toast.LENGTH_SHORT).show();break;
                }

            }
        };
        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "DELIVRED", Toast.LENGTH_SHORT).show();break;
                    case  Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "SMS NOT DELIVERED", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(smsSentReceiver,new IntentFilter(SENT));
        registerReceiver(smsSentReceiver,new IntentFilter(DELIVERED) );


    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(smsSentReceiver);
//        unregisterReceiver(smsDeliveredReceiver);
//    }

    private void sendOtp() {
        String s="welcome "+"\n"+"your otp is \n"+priority;
        String mobileNumForOtp = mobileno.getText().toString();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_SEND);
        }
        else {
            SmsManager sms=SmsManager.getDefault();
            sms.sendTextMessage(mobileNumForOtp,null,s,sentPI,deliveredPI);
            Toast.makeText(this, "message Sent", Toast.LENGTH_SHORT).show();
        }

    }

    private String genRandomotp() {
        int randomPin   =(int) (Math.random()*900000)+100000;
        String otp2  = String.valueOf(randomPin);
        return otp2;
    }

    public void sendsms(View view) {
        String title = editText_tittle.getText().toString();
        String description = editText_Description.getText().toString();

        String s=title +"\n"+ description+"\n"+priority;
        String mobileNumForOtp =String.format("smsto: %s",mobileno.getText().toString()) ;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_SEND);
        }
        else {
            Intent smsIntent=new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse(mobileNumForOtp));
            smsIntent.putExtra("sms_body",s);
            if (smsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(smsIntent);
            } else {
                Log.d(TAG, "Can't resolve app for ACTION_SENDTO Intent");
            }

//            SmsManager sms=SmsManager.getDefault();
//            sms.sendTextMessage(mobileNumForOtp,null,s,null,null);
//            Toast.makeText(this, "message Sent", Toast.LENGTH_SHORT).show();
        }
    }



    //LOading notes from firestore;
   // public void loadNote(View view) {
//        noteref.get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists())
//                        {
//                            Note note = new Note();
//                             String title= note.getTitle();
//                            String description = note.getDescription();
//                            textView_display.setText("title:"+title+"description:"+description);
//                        }
//                        else {
//                            Toast.makeText(MainActivity.this, "document/notes not exits", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MainActivity.this, "Error in loading", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, e.toString());
//                    }
//                });
//        Notebookref.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        String data="";
//for (QueryDocumentSnapshot queryDocumentSnapshot: QueryDocumentSnapshot){
//   Note note =queryDocumentSnapshot.toObject(Note.class);
//   String title =note.getTitle();
//   String description = note.getDescription();
//   data=data+"title: " +title +"\n " + "Description :" +description+ "\n\n";
//
//
//}textView_display.setText(data);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MainActivity.this, "erroe found", Toast.LENGTH_SHORT).show();
//            }
//        });
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MainActivity.this, "smthing went wrong", Toast.LENGTH_SHORT).show();
//            }
//        });
//    };

//   public void update(View view) {
//   String description_updated =editText_Description.getText().toString();
//  Note note=new Note("null",description_updated);
//  String str=note.getDescription();
//  Notebookref.add(note);
//    Map<String ,Object>note = new HashMap<>();
//    note.put(KEY_Description,description_updated);
//    noteref.set(note);
   //}
}
