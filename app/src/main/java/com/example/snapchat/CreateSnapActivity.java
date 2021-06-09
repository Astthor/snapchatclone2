package com.example.snapchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class CreateSnapActivity extends AppCompatActivity {

    // User set variables:
    private ImageView createSnapImageView;
    private EditText messageEditText;
    private TextView messageTextView;

    // Generate random ID for the image with jpg extension:
    private String imageName = UUID.randomUUID().toString() + ".jpg";

    private ViewGroup mainLayout;
    private int xDelta;
    private int yDelta;

    /*
    Methods in this Activity:

    public void nextClicked(View view)
    public Bitmap drawTextToBitmap(Bitmap bitmap)
    public void chooseImageClicked(View view)
    public void getPhoto()
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    */

    //private EditText messageEditText2;
    //@SuppressLint("ClickableViewAccessibility")
    // https://stackoverflow.com/questions/47107105/android-button-has-setontouchlistener-called-on-it-but-does-not-override-perform
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snap);
        System.out.println("CreateSnapActivity - onCreate");

        getSupportActionBar().setTitle(mAuth.getCurrentUser().getEmail());

        createSnapImageView = findViewById(R.id.createSnapImageView);
        messageEditText = findViewById(R.id.messageEditText);
        messageTextView = findViewById(R.id.messageTextView);
        //messageEditText2 = findViewById(R.id.messageEditText2);
        messageEditText.addTextChangedListener(textWatcher);
        Button b = findViewById(R.id.nextButton);
        b.callOnClick();

        /*createSnapImageView.setOnTouchListener((v, event) -> {
            messageEditText2.setAlpha(1);
            v.requestFocus();
            messageEditText2.bringToFront();
            //createSnapImageView.requestFocus();
            return false;
        });*/


    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // DO THE CALCULATIONS HERE AND SHOW THE RESULT AS PER YOUR CALCULATIONS
            messageTextView.setText(s.toString());
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void afterTextChanged(Editable s) { }
    };

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("CreateSnapActivity - onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("CreateSnapActivity - onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("CreateSnapActivity - onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("CreateSnapActivity - onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("CreateSnapActivity - onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("CreateSnapActivity - onDestroy");
    }

   /* public void clickedImage(View view){
        messageEditText2.setAlpha(1);
        messageEditText2.requestFocus();


    }
*/
    // set the imageView, call drawTextOnBitmap to add text to the image, upload the image.
    // if successful, create intent and start next activity where user chooses a user to send to
    public void nextClicked(View view){
        // Enable the drawing cache and draw the view in a bitmap
        System.out.println("------------------------------------------------Before try catch ------------------------------------------------");
        try {
            createSnapImageView.setDrawingCacheEnabled(true);
            createSnapImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) createSnapImageView.getDrawable()).getBitmap();
            System.out.println("Before drawtobitmap");
            bitmap = drawTextToBitmap(bitmap);
            System.out.println("after drawtobitmap");
            // getting our image into the correct format
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            // convert to bytes:
            byte[] data = baos.toByteArray();

            // set the location for our storage to upload
            StorageReference snapImageRef = FirebaseStorage.getInstance().getReference().child("images").child(imageName);

            // upload the image:
            snapImageRef.putBytes(data).addOnFailureListener(e -> {
                Toast.makeText(CreateSnapActivity.this, "upload failed", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(snapshot -> {
                System.out.println("We got into the success listener here and are about to send the photo to choose user");
                snapImageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    Intent intent = new Intent(CreateSnapActivity.this, ChooseUserActivity.class);
                    intent.putExtra("imageURL", downloadUri.toString());
                    intent.putExtra("imageName", imageName);
                    intent.putExtra("message", messageEditText.getText().toString());
                    startActivity(intent);
                });
            });
        } catch (IllegalStateException e) {
            Toast.makeText(CreateSnapActivity.this, "Please select a picture",
                    Toast.LENGTH_SHORT).show();
        }

    }

    // Setting text to our bitmap/image:
    public Bitmap drawTextToBitmap(Bitmap bitmap){
        Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null){
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are immutable, so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap); // new canvas for our bitmap

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // new anti-aliased paint
        textPaint.setColor(Color.BLACK); // set text color
        textPaint.setTextSize((int)(40)); // text size in pixels
        textPaint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow

        // Align our text to the center, variables:
        textPaint.setTextAlign(Paint.Align.CENTER);
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) /2 ));
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
        // ---> stackOverflow: https://stackoverflow.com/questions/11120392/android-center-text-on-canvas

        // draw the text with textPaint values set:
        canvas.drawText(messageEditText.getText().toString(), xPos, yPos, textPaint);

        return bitmap;
    }

    // our Choose Image button was clicked:
    public void chooseImageClicked(View view){
        // Check if we have permission to access external storage, if not, ask the user.
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            // if we do, call getPhoto()
            getPhoto();
        }
    }

    // Calls an Activity using an intent provided by Android that lets us browse and pick an image from the phones Media.
    // Automatically calls onActivityResult upon a result.
    public void getPhoto() {
        System.out.println("Picking a new photo here!");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    // The method called when user picks a photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // location of our selected image:
        Uri selectedImage = data.getData();

        // check if the requestcode matches the requestcode set in getPhoto,
        // that the result is okay and that we have some data to work with.
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            try {
                // Get an instance of the bitmap and set it to the imageView:
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                createSnapImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // When a request for a permission is made the following method will be called:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check if proper requestCode :
        if (requestCode == 1) {
            // check if grantResults is PERMISSION_GRANTED - if so, then call the getPhoto method.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }
    }

}