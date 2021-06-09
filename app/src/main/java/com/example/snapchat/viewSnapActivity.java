package com.example.snapchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.snapchat.model.Snap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class viewSnapActivity extends AppCompatActivity {


    private ImageView snapImageView;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    private Snap currentSnap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_snap);
        System.out.println("ViewSnapActivity - onCreate");

        snapImageView = findViewById(R.id.snapImageView);

        DocumentReference documentReference = db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("snaps").document(getIntent().getStringExtra("docID"));
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // set the currentSnap objects fields to the fields from the db:
                currentSnap = documentSnapshot.toObject(Snap.class);
                // get the image from storage and display it in the imageView
                setSnapImageView();
            }
        });
    }

    public void setSnapImageView(){
        // set maximum size of bytes
        int max = 1024 * 1024;
        // get image from storage, convert to bitmap from byte-array and set the imageView
        storageReference.child("images").child(currentSnap.getImageName()).getBytes(max).addOnSuccessListener( bytes -> {
            Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            snapImageView.setImageBitmap(img);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteSnap(); // deletes image in storage and snap document, clears activity history.
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("ViewSnapActivity - onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("ViewSnapActivity - onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("ViewSnapActivity - onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("ViewSnapActivity - onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("ViewSnapActivity - onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ViewSnapActivity - onDestroy");
    }


    // Start with deleting the image in the storage, if that's successful,
    // we delete the document in the snaps sub-collection.
    // If that is successful, we go back to SnapsActivity after clearing activity history.
    public void deleteSnap(){
        // delete image:
        storageReference.child("images").child(currentSnap.getImageName()).delete().addOnSuccessListener(success -> {
            System.out.println("Deleted successful in the storage " + success);
            // delete snap document:
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .collection("snaps").document(getIntent().getStringExtra("docID")).delete().addOnSuccessListener(successDB -> {
                // Create intent to go to SnapsActivity, but we clear activity history first.
                Intent intent = new Intent(viewSnapActivity.this, SnapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear so that we can't go back and crash the app
                startActivity(intent);
                finish();

            }).addOnFailureListener(e -> {
                System.out.println("Got an error deleting document from db " + e);
            });
        }).addOnFailureListener(exception -> {
            System.out.println("Got an error deleting image from storage, imageName: " + currentSnap.getImageName() + "\nException: " + exception);
        });
    }
}