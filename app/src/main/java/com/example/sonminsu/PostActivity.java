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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonminsu.Adapter.ImageAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl = "";
    StorageReference storageReference;

    ImageView image_added, image_gallery;
    TextView post;
    EditText description;
    ProgressDialog pd;

    private ExecutorService executorService;

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

        executorService = Executors.newSingleThreadExecutor();

        // 이미지뷰 클릭 시 카메라 열기
        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        // 이미지뷰 클릭 시 갤러리 열기
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

        // 저장소 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            loadImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImage();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // 사용자가 '다시 묻지 않음'을 선택하지 않고 거부한 경우, 권한을 다시 요청합니다.
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                } else {
                    // '다시 묻지 않음'을 선택하고 거부한 경우, 토스트 메시지를 띄웁니다.
                    Toast.makeText(this, "권한이 거부되었습니다. 앱 설정에서 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void loadImage() {
        // Uri를 저장할 ArrayList를 생성합니다.
        ArrayList<Uri> imageUris = new ArrayList<>();
        Cursor cursor = null;

        try {
            // MediaStore를 통해 모든 이미지를 가져옵니다.
            cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );

            // 이미지를 모두 선택합니다.
            while (cursor != null && cursor.moveToNext()) {
                int id = cursor.getInt(0);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                // ArrayList에 Uri를 추가합니다.
                imageUris.add(contentUri);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // RecyclerView에 어댑터를 설정합니다.
        RecyclerView recyclerView = findViewById(R.id.recycler_gallery);
        ImageAdapter adapter = new ImageAdapter(this, imageUris);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    // 카메라 열기를 위한 메서드
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // 이미지 선택을 위한 메서드
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1); // 이 숫자는 요청 코드로 나중에 onActivityResult에서 사용
    }

    // 이미지 선택 후 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            ImageView selectedImage = findViewById(R.id.selected_image);
            selectedImage.setImageURI(imageUri);
            selectedImage.setVisibility(View.VISIBLE);
        }
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
