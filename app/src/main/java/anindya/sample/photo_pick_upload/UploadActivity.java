package anindya.sample.photo_pick_upload;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
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
import java.util.Calendar;
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

    private Uri mCameraImageUri;
    String CameraImageFileName;
    File CameraImagesFolder;
    File CameraPhotoFile;
    String croppedImageFile;
    Uri mCroppedImageUri;

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
                //do here
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
                if (mCroppedImageUri != null) {
                    startCrop(mCroppedImageUri, "gallery", "");
                }
            }
        });

        mIconImageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLayoutImageView.getVisibility() == View.VISIBLE) {
                    if (mIconImageEdit.getVisibility() == View.VISIBLE) {
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
        String deviceName =  android.os.Build.MANUFACTURER;
        Log.d("duti", "Device Name: "+deviceName);
        if(deviceName.equalsIgnoreCase("samsung")) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, ACTION_REQUEST_CAMERA);
        }
        else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    makeToast("Error creating Image File");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    // create file mUriFromIntent
                    mCameraImageUri = FileProvider.getUriForFile(UploadActivity.this,
                            BuildConfig.APPLICATION_ID + ".fileprovider",
                            photoFile);
                    // add uri to intent
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);

                    // grant read uri permission
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, mCameraImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    // start intent
                    startActivityForResult(takePictureIntent, ACTION_REQUEST_CAMERA);
                }
            }
        }
    }


    // create file object method
    private File createImageFile() throws IOException {
        //folder stuff
        CameraImagesFolder = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), "");
        if (!CameraImagesFolder.exists()) {
            CameraImagesFolder.mkdir();
        }
        // Create an image file name
        String timeStamp = (new SimpleDateFormat("yyyyMMddHHmmss")).format(Calendar.getInstance().getTime());
        CameraImageFileName = "IMG_" + timeStamp + ".jpg";
        // create file with name
        CameraPhotoFile = new File(CameraImagesFolder, CameraImageFileName);
        return CameraPhotoFile;
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
                    String deviceName =  android.os.Build.MANUFACTURER;
                    if(deviceName.equalsIgnoreCase("samsung")) {
                        // Describe the columns you'd like to have returned. Selecting from the Thumbnails location gives you both the Thumbnail Image ID, as well as the original image ID
                        String[] projection = {
                                MediaStore.Images.Thumbnails._ID,  // The columns we want
                                MediaStore.Images.Thumbnails.IMAGE_ID,
                                MediaStore.Images.Thumbnails.KIND,
                                MediaStore.Images.Thumbnails.DATA};
                        String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select only mini's
                                MediaStore.Images.Thumbnails.MINI_KIND;

                        String sort = MediaStore.Images.Thumbnails._ID + " DESC";

                        //At the moment, this is a bit of a hack, as I'm returning ALL images, and just taking the latest one. There is a better way to narrow this down I think with a WHERE clause which is currently the selection variable
                        Cursor myCursor = this.managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);

                        long imageId = 0l;
                        long thumbnailImageId = 0l;
                        String thumbnailPath = "";

                        try {
                            myCursor.moveToFirst();
                            imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
                            thumbnailImageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
                            thumbnailPath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                        } finally {
                            myCursor.close();
                        }

                        //Create new Cursor to obtain the file Path for the large image

                        String[] largeFileProjection = {
                                MediaStore.Images.ImageColumns._ID,
                                MediaStore.Images.ImageColumns.DATA
                        };

                        String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
                        myCursor = this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
                        String largeImagePath = "";

                        try {
                            myCursor.moveToFirst();
                            //This will actually give you the file path location of the image.
                            largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                        } finally {
                            myCursor.close();
                        }
                        // These are the two URI's you'll be interested in. They give you a handle to the actual images
                        Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
                        Uri uriThumbnailImage = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(thumbnailImageId));

                        Log.d("duti", "camera image uri (file path): " + uriLargeImage);
                        if (uriLargeImage != null) {
                            startCrop(uriLargeImage, "camera", largeImagePath);
                        }
                        else if (uriLargeImage==null){
                            makeToast("Unable to take photo");
                        }
                    }
                    else{
                        Uri selectedImage = mCameraImageUri;
                        Log.d("duti", "camera image uri (file path): " + mCameraImageUri);
                        if (selectedImage != null) {
                            startCrop(selectedImage, "camera", CameraPhotoFile.getPath());
                        }
                        else if (selectedImage == null){
                            makeToast("Unable to take photo");
                        }
                    }
                }
                break;

            case ACTION_REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("duti", "gallery image uri (file path): " + uri);
                    if (uri != null) {
                        startCrop(uri, "gallery", "");
                    }
                }
                break;

            case ACTION_REQUEST_CROPPED:
                if (resultCode == Activity.RESULT_OK) {
                    mCroppedImageUri = data.getData();
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

    // delete cropped image file from storage
    private void deleteExternalStoragePublicPicture() {
        File file = new File(croppedImageFile);
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
