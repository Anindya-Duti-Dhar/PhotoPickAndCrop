package anindya.sample.photo_pick_upload;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ImageCroppedActivity extends AppCompatActivity {

    //Defining Variables
    String TAG = getClass().getName();
    FloatingActionButton mFab;
    private ImageCropView imageCropView;
    File mOriginalImageFile;
    Uri mUriFromIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropped);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setActionBarTitle("Your Image");
        ActionBar ctBr = getSupportActionBar();
        ctBr.setDisplayHomeAsUpEnabled(true);
        ctBr.setDisplayShowHomeEnabled(true);

        // initialize the view
        imageCropView = (ImageCropView) findViewById(R.id.image);
        mFab = (FloatingActionButton) findViewById(R.id.cropped_activity_fab);

        // get intent data for image from Send Post Activity
        Intent i = getIntent();
        mUriFromIntent = i.getData();

        // set image into the view to crop
        Bitmap bitmap = null;
        try {
            // convert Uri into bitmap
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mUriFromIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(getImageOrientation(mUriFromIntent.getPath()));
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        imageCropView.setImageBitmap(rotatedBitmap);

        // set image aspect ration
        imageCropView.setAspectRatio(1, 1);

        // fab button listener
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save the cropped image and send to the Sen Post Activity Page
                if (!imageCropView.isChangingScale()) {
                    Bitmap b = imageCropView.getCroppedImage();
                    if (b != null) {
                        bitmapConvertToFile(b);
                    } else {
                        Toast.makeText(ImageCroppedActivity.this, "Image crop falied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            mOriginalImageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    mOriginalImageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    // convert bitmap into file
    public File bitmapConvertToFile(final Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        File bitmapFile = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), "");
            if (!file.exists()) {
                file.mkdir();
            }

            bitmapFile = new File(file, "IMG_" + (new SimpleDateFormat("yyyyMMddHHmmss")).format(Calendar.getInstance().getTime()) + ".jpg");
            fileOutputStream = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            MediaScannerConnection.scanFile(this, new String[]{bitmapFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String path, final Uri uri) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // when crop and converting is done then send it to the SendPostActivity class with data
                            Intent returnIntent = new Intent();
                            returnIntent.setData(uri);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception e) {
                }
            }
        }
        return bitmapFile;
    }

    // Set up the toolbar title
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    // back arrow action
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // back button press method
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
