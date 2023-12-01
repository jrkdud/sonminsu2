package com.example.sonminsu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonminsu.Adapter.GalleryAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl = "";
    StorageReference storageReference;

    ImageView image_added, image_gallery;
    TextView post;
    EditText description;
    ProgressDialog pd;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private GalleryAdapter mAdapter;
    private ImageButton closeButton;

    private static final int YOUR_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        image_added = findViewById(R.id.image_added);
        image_gallery = findViewById(R.id.image_gallery);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        List<String> imagePaths = getAllShownImagesPath(this);
        mAdapter = new GalleryAdapter(this, imagePaths);

        mImageView = findViewById(R.id.selected_image);
        mRecyclerView = findViewById(R.id.recycler_gallery);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setBackgroundColor(Color.TRANSPARENT);

        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            mImageView.setImageDrawable(null);
            imageUri = null;
            mImageView.setVisibility(View.GONE);
            closeButton.setVisibility(View.GONE);
        });

        mAdapter.setOnItemClickListener(imagePath -> {
            imageUri = Uri.parse(imagePath);
            mImageView.setImageURI(imageUri);
            mImageView.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);
        });

        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        image_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, YOUR_REQUEST_CODE);
        } else {
            loadImage();
        }
    }

    private List<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        int column_index_id;
        List<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;

        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID};

        cursor = activity.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            long id = cursor.getLong(column_index_id);
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            listOfAllImages.add(contentUri.toString());
        }

        return listOfAllImages;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImage();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                } else {
                    Toast.makeText(this, "권한이 거부되었습니다. 앱 설정에서 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadImage() {
        ArrayList<String> imageUris = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );

            while (cursor != null && cursor.moveToNext()) {
                int id = cursor.getInt(0);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageUris.add(contentUri.toString()); // Uri를 String으로 변환
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        mAdapter = new GalleryAdapter(this, imageUris);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Save the Uri of the captured image
                imageUri = getImageUri(getApplicationContext(), imageBitmap);
                mImageView.setImageBitmap(imageBitmap);
                mImageView.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImage = data.getData();
                imageUri = selectedImage;
                mImageView.setImageURI(selectedImage);
                mImageView.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImage() {
        pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();

        if (imageUri != null) {
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            UploadTask uploadTask = filereference.putFile(imageUri);

            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filereference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    myUrl = downloadUri.toString();

                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                                    String postid = reference.push().getKey();

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("postid", postid);
                                    hashMap.put("postimage", myUrl);
                                    hashMap.put("description", description.getText().toString());
                                    hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                    reference.child(postid).setValue(hashMap);

                                    pd.dismiss();

                                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    pd.dismiss();
                                    Toast.makeText(PostActivity.this, "실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        pd.dismiss();
                        Toast.makeText(PostActivity.this, "업로드 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(this, "이미지가 선택되지 않았습니다!", Toast.LENGTH_SHORT).show();
        }
    }
}
