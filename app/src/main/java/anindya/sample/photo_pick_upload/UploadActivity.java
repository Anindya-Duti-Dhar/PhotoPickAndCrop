package anindya.sample.photo_pick_upload;


import android.app.Activity;
import android.content.Intent;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadActivity extends AppCompatActivity {

    //Defining Variables
    String TAG = getClass().getName();
    ImageView mIconCamera, mIconGallery, mImageView, mIconImageEdit;
    FloatingActionButton mFab;
    boolean isKeyboardOpen = false;

    LinearLayout mLayoutButtons;
    RelativeLayout mLayoutImageView;

    private static final int ACTION_REQUEST_CAMERA = 99;
    private static final int ACTION_REQUEST_CROPPED = 100;
    private static final int ACTION_REQUEST_GALLERY = 101;
    private Uri imageUri;

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

    public void goToCamera() {
        //folder stuff
        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), "");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdir();
        }
        // create file with name
        File photo = new File(imagesFolder, "IMG_" + (new SimpleDateFormat("yyyyMMddHHmmss")).format(Calendar.getInstance().getTime()) + ".jpg");
        // create file mUriFromIntent
        imageUri = FileProvider.getUriForFile(UploadActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                photo);
        // create camera intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                imageUri);
        // start device camera
        startActivityForResult(intent, ACTION_REQUEST_CAMERA);
    }

    public void goToGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        Intent chooser = Intent.createChooser(intent, "Choose a Picture");
        startActivityForResult(chooser, ACTION_REQUEST_GALLERY);
    }

    private void startCrop(Uri imageUri) {
        Intent intent = new Intent(UploadActivity.this, ImageCroppedActivity.class);
        intent.setData(imageUri);
        startActivityForResult(intent, ACTION_REQUEST_CROPPED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTION_REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    if (selectedImage != null) {
                        startCrop(selectedImage);
                    }
                }
                break;

            case ACTION_REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        startCrop(uri);
                    }
                }
                break;

            case ACTION_REQUEST_CROPPED:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
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
