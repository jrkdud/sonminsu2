package com.example.sonminsu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl = "";
    StorageReference storageReference;

    ImageView close, image_added;
    TextView post;
    EditText description;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        // 이미지뷰 클릭 시 이미지 선택 작업 시작
        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        post.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                uploadImage();
            }
        });
    }

    private String getFileExtension(Uri uri){
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
            image_added.setImageURI(imageUri);
        }
    }

    private void uploadImage() {
        pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();

        if(imageUri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()
                    + "."+ getFileExtension(imageUri));

            UploadTask uploadTask = filereference.putFile(imageUri);

            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        filereference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){
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
                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(this, "이미지가 선택되지 않았습니다!", Toast.LENGTH_SHORT).show();
        }
    }
}
