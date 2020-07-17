package com.andgmr.blog.Activities;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.andgmr.blog.DelayedProgressDialog;
import com.andgmr.blog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText firstName, lastName, email,password;
    private Button createAccountBtn;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private DelayedProgressDialog delayedProgressDialog;
    private ImageButton proficePic;
    private StorageReference mFirebaseStorage;
    private Uri resultUri = null;
    private  final static int GALLERY_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();

        mFirebaseStorage = FirebaseStorage.getInstance().getReference().child("Blog_Profile_Pics");

        delayedProgressDialog = new DelayedProgressDialog();

        firstName = (EditText) findViewById(R.id.firstNameAct);
        lastName = (EditText) findViewById(R.id.lastNameAct);
        email = (EditText) findViewById(R.id.emailAct);
        proficePic = (ImageButton) findViewById(R.id.profilePic);
        password = (EditText) findViewById(R.id.passwordAct);
        createAccountBtn = (Button) findViewById(R.id.submitAct);

        proficePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);

            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {

        final String name = firstName.getText().toString().trim();
        final String lname = lastName.getText().toString().trim();
        String em = email.getText().toString().trim();
        String pwd = password.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(lname)
                && !TextUtils.isEmpty(em) && !TextUtils.isEmpty(pwd)){

            delayedProgressDialog.show(getSupportFragmentManager(), "tag");

            mAuth.createUserWithEmailAndPassword(em, pwd)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if(authResult != null){

                                StorageReference imagePath = mFirebaseStorage.child("Blog_Profile_Pics")
                                        .child(resultUri.getLastPathSegment());

                                imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String userid = mAuth.getCurrentUser().getUid();
                                        DatabaseReference currentUserDB = mDatabaseReference.child(userid);
                                        currentUserDB.child("firstname").setValue(name);
                                        currentUserDB.child("lastname").setValue(lname);
                                        currentUserDB.child("image").setValue(resultUri.toString());

                                delayedProgressDialog.cancel();

                                //send users to postlist

                                Intent intent = new Intent(CreateAccountActivity.this, PostListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                    }
                                });

//                                String userid = mAuth.getCurrentUser().getUid();
//                                DatabaseReference currentUserDB = mDatabaseReference.child(userid);
//                                currentUserDB.child("firstname").setValue(name);
//                                currentUserDB.child("lastname").setValue(lname);
//                                currentUserDB.child("image").setValue("none");
//
//                                delayedProgressDialog.cancel();
//
//                                //send users to postlist
//
//                                Intent intent = new Intent(CreateAccountActivity.this, PostListActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                startActivity(intent);

                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){

            Uri mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();

                proficePic.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
