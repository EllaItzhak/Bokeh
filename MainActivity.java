package com.example.ellai.bokeh2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_IMAGE_GALLERY = 345;
    private static final String TAG = "Main::Activity";

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private Button btnGallery;
    private FirebaseAuth mAuth;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //MainActivityPermissionsDispatcher.startGalleryIntentWithPermissionCheck(this);
        mAuth = FirebaseAuth.getInstance();
        //facebook login
        callbackManager = CallbackManager.Factory.create();

        initViews();

        checkFirebaseLoggedIn();
    }

    private void initViews() {
        btnGallery = findViewById(R.id.btngallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityPermissionsDispatcher.startGalleryIntentWithPermissionCheck(MainActivity.this);
            }
        });
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInWithFacebook();
            }
        });
    }

    private void checkFirebaseLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            onUserLoggedIn(false);
        } else {
            onUserLoggedIn(true);
        }
    }

    private void logInWithFacebook() {
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "You need to be logged in in order to upload an image", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(MainActivity.this, "Error in log in. Try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            onUserLoggedIn(true);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            onUserLoggedIn(false);
                        }
                    }
                });
    }

    public void onUserLoggedIn(boolean isLoggedIn) {
        if (isLoggedIn) {
            loginButton.setVisibility(View.GONE);
            btnGallery.setVisibility(View.VISIBLE);
        } else {
//            loginButton.setVisibility(View.VISIBLE);
//            btnGallery.setVisibility(View.GONE);
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void startGalleryIntent() {
        // Create an intent.
        Intent intent = new Intent();

        // Set the type of the intent to image files.
        intent.setType("image/*");

        // Set the action to Intent.ACTION_GET_CONTENT.
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Check if there is an Activity component that could be used to handle this intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // An Activity component was found; Start activity for result.
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForGallery() {
        Toast.makeText(this, "user said no", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForGallery() {
        Toast.makeText(this, "user said never ask again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_GALLERY) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                Intent intent = new Intent(this, BordersActivity.class);
                //intent.putExtra("imgV", imageUri.toString());
                intent.putExtra("img", imageUri.toString());
                startActivity(intent);
            }
            Log.d("fail", "" + resultCode);
        } else {//facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onClick(View v) {
        startGalleryIntent();
    }
}
