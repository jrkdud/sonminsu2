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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    private ExecutorService executorService;
    private ImageButton closeButton;

    private static final int YOUR_REQUEST_CODE = 123;

    // 선택 이력을 저장하는 스택
    private Stack<Integer> navigationStack = new Stack<>();

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

        RecyclerView recyclerView = findViewById(R.id.recycler_gallery);
        List<String> imagePaths = getAllShownImagesPath(this);
        GalleryAdapter adapter = new GalleryAdapter(this, imagePaths);
        recyclerView.setAdapter(adapter);

        mImageView = findViewById(R.id.selected_image);
        mRecyclerView = findViewById(R.id.recycler_gallery);
        mAdapter = new GalleryAdapter(this, imagePaths);
        mRecyclerView.setAdapter(mAdapter);

        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            mImageView.setImageDrawable(null);  // 이미지를 제거합니다.
            imageUri = null;  // 선택한 이미지의 Uri를 제거합니다.
            mImageView.setVisibility(View.GONE);  // ImageView를 숨깁니다.
            closeButton.setVisibility(View.GONE);  // X 버튼을 숨깁니다.
        });

        mAdapter.setOnItemClickListener(imagePath -> {
            mImageView.setImageURI(Uri.parse(imagePath));
            mImageView.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);
        });

        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이전 선택을 SharedPreferences에서 가져옴
                SharedPreferences sharedPref = getSharedPreferences("MY_PREFS", MODE_PRIVATE);
                int previousSelection = sharedPref.getInt("previous_selection", 0);

                // MainActivity로 돌아가며 이전 선택을 전달
                Intent intent = new Intent(PostActivity.this, MainActivity.class);
                intent.putExtra("previous_selection", previousSelection);
                startActivity(intent);
                finish();
            }
        });

        //환경설정
        ImageButton settingsButton = findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

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

        // 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없다면 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, YOUR_REQUEST_CODE);
        } else {
            // 권한이 있다면 이미지를 불러옵니다.
            loadImage();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK);
        }
    }

    //갤러리 최근 사진
    private List<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        List<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage;

        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = activity.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // 이미지 선택 후 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mImageView.setImageBitmap(imageBitmap);
                mImageView.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImage = data.getData();
                imageUri = selectedImage;  // 선택한 이미지의 Uri를 저장합니다.
                mImageView.setImageURI(selectedImage);  // 선택한 이미지를 ImageView에 설정합니다.
                mImageView.setVisibility(View.VISIBLE);  // ImageView를 보이도록 합니다.
                closeButton.setVisibility(View.VISIBLE);  // X 버튼을 보이도록 합니다.
            }
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
