package pl.droidsonroids.selfieapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 101;
    private static final int REQUEST_CODE_PHOTO_GALLERY = 102;

    public static final String INTENT_TYPE_TEXT = "text/plain";
    public static final String INTENT_TYPE_IMAGE = "image";
    public static final String INTENT_TYPE_IMAGE_ANY = "image/*";
    public static final String AVATAR_FILENAME = "avatar.jpg";
    public static final String EXTRA_DATA = "data";

    private ImageView photoImageView;
    private Button selfieButton;
    private Button galleryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoImageView = ((ImageView) findViewById(R.id.photoImageView));
        selfieButton = ((Button) findViewById(R.id.selfieButton));
        galleryButton = ((Button) findViewById(R.id.galleryButton));

        setSelfieButton();
        setGalleryButton();

        handleIntentAction();
    }

    private void handleIntentAction() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action.equals(Intent.ACTION_SEND)) {
            if (type.startsWith(INTENT_TYPE_IMAGE)) {
                showPhotoFromImageIntent(intent);
            } else if (type.equals(INTENT_TYPE_TEXT)) {
                showPhotoFromTextIntent(intent);
            }
        }
    }

    private void showPhotoFromImageIntent(final Intent intent) {
        Uri photoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        photoImageView.setImageURI(photoUri);
    }

    private void showPhotoFromTextIntent(final Intent intent) {
        String photoURL = intent.getStringExtra(Intent.EXTRA_TEXT);
        Picasso.with(this)
                .load(photoURL)
                .fit()
                .centerCrop()
                .error(R.mipmap.ic_launcher)
                .transform(new CropCircleTransformation())
                .into(photoImageView);
    }

    private void setSelfieButton() {
        selfieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent capturePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                capturePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoUri());
                if (capturePhotoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(capturePhotoIntent, REQUEST_CODE_CAPTURE_IMAGE);
                }
            }
        });
    }

    private void setGalleryButton() {
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType(INTENT_TYPE_IMAGE_ANY);
                Intent photoChooser = Intent.createChooser(galleryIntent, getString(R.string.choose_photo));
                startActivityForResult(photoChooser, REQUEST_CODE_PHOTO_GALLERY);
            }
        });
    }

    private Uri getPhotoUri() {
        //        String uniqueFileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date());
        File photoFile = new File(Environment.getExternalStorageDirectory(), AVATAR_FILENAME);
        return Uri.fromFile(photoFile);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                handleCaptureImageWithThumbnail(data);
            } else {
                handleCaptureImageWithoutThumbnail();
            }
        } else if (requestCode == REQUEST_CODE_PHOTO_GALLERY && resultCode == RESULT_OK) {
            showPhotoFromGallery(data);
        }
    }

    private void handleCaptureImageWithThumbnail(final Intent data) {
        Bitmap photoBitmap = (Bitmap) data.getExtras().get(EXTRA_DATA);
        if (photoBitmap != null) {
            photoImageView.setImageBitmap(photoBitmap);
        }
    }

    private void handleCaptureImageWithoutThumbnail() {
        photoImageView.setImageURI(getPhotoUri());
        broadcastNewMediaForScanner();
    }

    private void broadcastNewMediaForScanner() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(getPhotoUri());
        sendBroadcast(intent);
    }

    private void showPhotoFromGallery(final Intent data) {
        Uri photoImageUri = data.getData();
        photoImageView.setImageURI(photoImageUri);
    }
}
