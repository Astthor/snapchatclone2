package com.example.snapchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class CreateSnapActivity2 extends Activity {

    // User set variables:
    private ImageView imageView2;
    private EditText editText;

    private String imageName = UUID.randomUUID().toString() + ".jpg";
    // Generate random ID for the image with jpg extension:
    //private String imageName = UUID.randomUUID().toString() + ".jpg";

    private ViewGroup mainLayout;
    private int xDelta;
    private int yDelta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snap2);

        mainLayout = (RelativeLayout) findViewById(R.id.main);

        imageView2 = (ImageView) findViewById(R.id.myImageView);
        editText = (EditText) findViewById(R.id.editText15);


        Intent intent = getIntent();
        System.out.println("_________________________________________________We got the intent here ");
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("image");
        System.out.println("_________________________________________________We've got the image into a bitmap here");
        imageView2.setImageBitmap(bitmap);
        System.out.println("_________________________________________________We've set the image here ");
        editText.setText(getIntent().getStringExtra("text"));
        System.out.println("_________________________________________________We've set the text here");
        editText.setOnTouchListener(onTouchListener());
    }

    private View.OnTouchListener onTouchListener (){
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams layoutParams =
                                (RelativeLayout.LayoutParams) v.getLayoutParams();
                        xDelta = x - layoutParams.leftMargin;
                        yDelta = y - layoutParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        System.out.println("hu");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams1 =
                                (RelativeLayout.LayoutParams) v.getLayoutParams();
                        layoutParams1.leftMargin = x - xDelta;
                        layoutParams1.topMargin = y - yDelta;
                        layoutParams1.rightMargin = 0;
                        layoutParams1.bottomMargin = 0;
                        v.setLayoutParams(layoutParams1);
                        break;
                }
                mainLayout.invalidate();
                return true;
                //return false;
            }
        };
    }

    public void nextClicked2(View view){
        imageView2.setDrawingCacheEnabled(true);
        imageView2.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();

        bitmap = drawTextToBitmap(bitmap);
        // getting our image into the correct format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // convert to bytes:
        byte[] data = baos.toByteArray();

        // set the location for our storage to upload
        StorageReference snapImageRef = FirebaseStorage.getInstance().getReference().child("images").child(imageName);

        // upload the image:
        snapImageRef.putBytes(data).addOnFailureListener(e -> {
            Toast.makeText(CreateSnapActivity2.this, "upload failed", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(snapshot -> {
            snapImageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                Intent intent = new Intent(CreateSnapActivity2.this, ChooseUserActivity.class);
                intent.putExtra("imageURL", downloadUri.toString());
                intent.putExtra("imageName", imageName);
                intent.putExtra("message", editText.getText().toString());
                startActivity(intent);
            });
        });
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
        canvas.drawText(editText.getText().toString(), xPos, yPos, textPaint);

        return bitmap;
    }


}