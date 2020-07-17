package com.andgmr.blog.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.andgmr.blog.DelayedProgressDialog;
import com.andgmr.blog.Model.Blog;
import com.andgmr.blog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private ImageButton mPostImage;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private Button mSubmitButton;
    private DatabaseReference mPostDatabase;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorage;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private ProgressBar mProgress;
    private Uri mImageUri;
    private static final int GALLERY_CODE = 1;
    private DelayedProgressDialog progressDialog = new DelayedProgressDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        DelayedProgressDialog progressDialog = new DelayedProgressDialog();

        mProgress = new ProgressBar(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mStorage = FirebaseStorage.getInstance().getReference();

        mPostDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        mPostImage = (ImageButton) findViewById(R.id.imageButton);
        mPostTitle = (EditText) findViewById(R.id.postTitleEt);
        mPostDesc = (EditText) findViewById(R.id.descriptionEt);
        mSubmitButton = (Button) findViewById(R.id.submitPost);

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);

            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Posting to out database
                startPosting();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){

            mImageUri = data.getData();
            mPostImage.setImageURI(mImageUri);

        }
    }

    private void startPosting() {

        final String titleVal = mPostTitle.getText().toString().trim();
        final String descVal = mPostDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(titleVal) && ! TextUtils.isEmpty(descVal)
                && mImageUri != null){


            progressDialog.show(getSupportFragmentManager(), "tag");//Asta am instalat-o eu


            //start the uploading...
            //mImageUri.getLastPathSegment() == /image/myphoto.jpeg

            final StorageReference filepath = mStorage.child("Blog_images")
                    .child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadurl = taskSnapshot.getDownloadUrl();

                    DatabaseReference newPost = mPostDatabase.push();

                    Map<String, String> dataToSave = new HashMap<>();
                    dataToSave.put("title", titleVal);
                    dataToSave.put("desc", descVal);
                    dataToSave.put("image", downloadurl.toString());
                    dataToSave.put("timestamp", String.valueOf(java.lang.System.currentTimeMillis()));
                    dataToSave.put("userid", mUser.getUid());

                    newPost.setValue(dataToSave);

                    progressDialog.cancel();

                    startActivity(new Intent(AddPostActivity.this, PostListActivity.class));
                    finish();

                }
            });

        }
    }
}
