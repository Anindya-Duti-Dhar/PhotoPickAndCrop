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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ImageCroppedActivity extends AppCompatActivity {

    //Defining Variables
    String TAG = getClass().getName();
    FloatingActionButton mFab;
    private ImageCropView imageCropView;
    Uri mUriFromIntent;

    String cropType;
    String incomingCameraFilePath;

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
        Log.d("duti", "Image uri (file path) for crop: " + mUriFromIntent);

        Bundle b = i.getExtras();
        cropType = (String) b.get("type");

        if (cropType.equals("camera")) {
            incomingCameraFilePath = (String) b.get("file");
            Uri imageUri = Uri.parse(incomingCameraFilePath);
            // set image into the view to crop
            Bitmap bitmap = null;
            try {
                // convert Uri into bitmap
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mUriFromIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // rotate the bitmap if it's getting wrong rotation
            Matrix matrix = new Matrix();
            matrix.postRotate((float)getExif(imageUri));
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);

            // set bitmap into the image view
            imageCropView.setImageBitmap(rotatedBitmap);

            //deleteExternalStoragePublicPicture();
        } else {
            // set image into the view to crop
            Bitmap bitmap = null;
            try {
                // convert Uri into bitmap
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mUriFromIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // set bitmap into the image view
            imageCropView.setImageBitmap(bitmap);
        }

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

    // delete camera image file from storage
    private void deleteExternalStoragePublicPicture() {
        File file = new File(incomingCameraFilePath);
        file.delete();
    }

    /**
     * check exif of the image taken
     */
    public int getExif(Uri imageUri){
        ExifInterface exif;
        int rotation;
        int  rotationInDegrees =0;
        try {
            exif = new ExifInterface(imageUri.getPath());
            rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            rotationInDegrees = exifToDegrees(rotation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("duti", "correct exif rotation: "+ Integer.toString(rotationInDegrees) );
        return rotationInDegrees;
    }

    /**
     * Transform exif integer into degree
     * @param exifOrientation
     * @return
     */
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    // convert bitmap into file
    public File bitmapConvertToFile(final Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        File image = null;
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "IMG_" + timeStamp + "_H_";
            File storageDir2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File storageDir3 = new File(storageDir2,"PickNCrop");
            if( !storageDir3.exists() ){
                storageDir3.mkdirs();
            }
            image = File.createTempFile(imageFileName, ".jpg", storageDir3);
            fileOutputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            final File finalBitmapFile = image;
            MediaScannerConnection.scanFile(this, new String[]{image.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String path, final Uri uri) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("duti", "cropped image uri (file path): " + uri);
                            // when crop and converting is done then send it to the SendPostActivity class with data
                            Intent returnIntent = new Intent();
                            returnIntent.setData(uri);
                            returnIntent.putExtra("picture", finalBitmapFile.getPath());
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
        return image;
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
