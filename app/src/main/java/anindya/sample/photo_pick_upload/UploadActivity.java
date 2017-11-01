package anindya.sample.photo_pick_upload;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private Uri mCroppedImageUri;
    String  croppedImageFile;

    Context context = UploadActivity.this;
    String mCurrentPhotoPath;
    Uri photoURIGlobal;

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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d("duti", "photoFile: "+photoFile.toString());
                Log.d("duti", " photoURI file created");
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("duti","photoFile create ERROR: "+ex.toString());
                Log.d("duti", "photoURI error creation");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d("duti","** photoURI **");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "anindya.sample.photo_pick_upload.fileprovider",
                        photoFile);
                Log.d("duti", "photoURI: "+photoURI.toString());
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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir3 = new File(storageDir2,"PickNCrop");
        /**
         * create the folder for the app if it does not exist
         */
        if( !storageDir3.exists() ){
            storageDir3.mkdirs();
        }
        Log.d("storageDir",storageDir.toString());
        Log.d("storageDir2",storageDir2.toString());
        Log.d("storageDir3",storageDir3.toString());
        /**
         * not full control over a filename,
         * if I want to have full control, I need to create a new file and give it a name
         *
         * e.g File imageFile = new File(MyApplication.getAlbumDir(), imageFileName + MyApplication.JPEG_FILE_SUFFIX);
         *
         */
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir3      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("duti", "image: "+image.toString());
        Log.d("duti","mCurrentPhotoPath: "+mCurrentPhotoPath.toString());
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
                    Uri imageUri = Uri.parse(mCurrentPhotoPath);
                    File file = new File(imageUri.getPath());
                    // ScanFile so it will be appeared on Gallery
                    MediaScannerConnection.scanFile(UploadActivity.this,
                            new String[]{imageUri.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {

                                }
                            });
                    Log.d("duti", "imageUri: "+imageUri.toString());
                    Log.d("duti", "file: "+file.toString());

                    startCrop(photoURIGlobal, "camera", mCurrentPhotoPath);
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
