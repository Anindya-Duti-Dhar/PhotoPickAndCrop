package anindya.sample.photo_pick_upload;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UploadActivity extends AppCompatActivity {

    //Defining Variables
    String TAG = getClass().getName();
    ImageView mIconCamera, mIconGallery, mImageView, mIconImageEdit;
    FloatingActionButton mFab;

    LinearLayout mLayoutButtons;
    RelativeLayout mLayoutImageView;

    private static final int ACTION_REQUEST_CAMERA = 99;
    private static final int ACTION_REQUEST_CROPPED = 100;
    private static final int ACTION_REQUEST_GALLERY = 101;

    String  croppedImageFile;

    Context context = UploadActivity.this;
    String mCurrentPhotoPath;
    Uri photoURIGlobal;
    boolean hasPictureInTheBox = false;
    boolean isCapturedByCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setActionBarTitle("Upload");
        ActionBar ctBr = getSupportActionBar();
        ctBr.setDisplayHomeAsUpEnabled(true);
        ctBr.setDisplayShowHomeEnabled(true);

        // view initialization
        mLayoutButtons = (LinearLayout) findViewById(R.id.button_layout);
        mLayoutImageView = (RelativeLayout) findViewById(R.id.image_layout);
        mIconCamera = (ImageView) findViewById(R.id.camera_btn);
        mIconGallery = (ImageView) findViewById(R.id.gallery_btn);
        mImageView = (ImageView) findViewById(R.id.image);
        mIconImageEdit = (ImageView) findViewById(R.id.image_edit);
        mFab = (FloatingActionButton) findViewById(R.id.send_fab);

        // fab button listener
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasPictureInTheBox){
                    if(isCapturedByCamera){
                        deleteCameraPicture();
                    }
                    makeToast("Upload SuccessFully");
                    onBackPressed();
                }
                else{
                    makeToast("Select One Image");
                }
            }
        });

        mIconCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCamera();
            }
        });

        mIconGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGallery();
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoURIGlobal != null) {
                    if(isCapturedByCamera){
                        startCrop(photoURIGlobal, "camera", mCurrentPhotoPath);
                    }
                    else{
                        startCrop(photoURIGlobal, "gallery", "");
                    }
                }
            }
        });

        mIconImageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLayoutImageView.getVisibility() == View.VISIBLE) {
                    if (mIconImageEdit.getVisibility() == View.VISIBLE) {
                        if(isCapturedByCamera){
                            isCapturedByCamera = false;
                            deleteCameraPicture();
                        }
                        mIconImageEdit.setVisibility(View.GONE);
                    }
                    if (mImageView.getVisibility() == View.VISIBLE) {
                        mImageView.setImageBitmap(null);
                        mImageView.setVisibility(View.GONE);
                    }
                    mLayoutImageView.setVisibility(View.GONE);
                }
                if (mLayoutButtons.getVisibility() == View.GONE) {
                    mLayoutButtons.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    // method to make toast
    public void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void goToCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                makeToast("Failed to create file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "anindya.sample.photo_pick_upload.fileprovider",
                        photoFile);
                photoURIGlobal = photoURI;
                // add uri to intent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // grant read uri permission
                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                // start intent
                startActivityForResult(takePictureIntent, ACTION_REQUEST_CAMERA);
            }
        }
    }


    // create file object method
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_H_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir3 = new File(storageDir2,"PickNCrop");
        /**
         * create the folder for the app if it does not exist
         */
        if( !storageDir3.exists() ){
            storageDir3.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir3      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void goToGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        Intent chooser = Intent.createChooser(intent, "Choose a Picture");
        startActivityForResult(chooser, ACTION_REQUEST_GALLERY);
    }

    // go to activity to crop the selected image
    private void startCrop(Uri imageUri, String cropType, String fileName) {
        Intent intent = new Intent(UploadActivity.this, ImageCroppedActivity.class);
        intent.putExtra("type", cropType);
        intent.putExtra("file", fileName);
        intent.setData(imageUri);
        startActivityForResult(intent, ACTION_REQUEST_CROPPED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTION_REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("duti", "camera image uri (file path): " + photoURIGlobal);
                    isCapturedByCamera = true;
                    startCrop(photoURIGlobal, "camera", mCurrentPhotoPath);
                }
                break;

            case ACTION_REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    isCapturedByCamera = false;
                    Uri uri = data.getData();
                    photoURIGlobal = uri;
                    Log.d("duti", "gallery image uri (file path): " + uri);
                    if (uri != null) {
                        startCrop(uri, "gallery", "");
                    }
                }
                break;

            case ACTION_REQUEST_CROPPED:
                if (resultCode == Activity.RESULT_OK) {
                    Uri mCroppedImageUri = data.getData();
                    Bundle b = data.getExtras();
                    croppedImageFile = (String) b.get("picture");
                    // convert ImageFile to Byte[] Array
                    try {
                        byte[] byteArray = readFile(croppedImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCroppedImageUri);
                        if (mLayoutButtons.getVisibility() == View.VISIBLE) {
                            mLayoutButtons.setVisibility(View.GONE);
                        }
                        if (mLayoutImageView.getVisibility() == View.GONE) {
                            mLayoutImageView.setVisibility(View.VISIBLE);
                        }
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageBitmap(bitmap);
                        hasPictureInTheBox = true;
                        if (mLayoutImageView.getVisibility() == View.VISIBLE) {
                            if (mIconImageEdit.getVisibility() == View.GONE) {
                                mIconImageEdit.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    // delete Camera image file from storage
    private void deleteCameraPicture() {
        File file = new File(mCurrentPhotoPath);
        file.delete();
    }

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
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
        startActivity(new Intent(UploadActivity.this, MainActivity.class));
        finish();
    }

}
