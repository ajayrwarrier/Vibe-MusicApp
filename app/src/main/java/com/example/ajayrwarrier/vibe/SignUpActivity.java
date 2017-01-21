package com.example.ajayrwarrier.vibe;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class SignUpActivity extends AppCompatActivity {
    private static String TAG;
    private Button btnSignUp, btnLinkToLogIn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private EditText signupInputEmail, signupInputPassword;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        auth = FirebaseAuth.getInstance();
        TAG = getString(R.string.signup_activity);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference(getString(R.string.users));
        mFirebaseInstance.getReference(getString(R.string.app_title)).setValue(getString(R.string.app_name));
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        signupInputEmail = (EditText) findViewById(R.id.signup_input_email);
        signupInputPassword = (EditText) findViewById(R.id.signup_input_password);
        btnSignUp = (Button) findViewById(R.id.btn_signup);
        btnLinkToLogIn = (Button) findViewById(R.id.btn_link_login);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
        btnLinkToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void submitForm() {
        String email = signupInputEmail.getText().toString().trim();
        String password = signupInputPassword.getText().toString().trim();
        if (!checkEmail()) {
            return;
        }
        if (!checkPassword()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        //create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, getString(R.string.creatingUserEmail) + task.isSuccessful());
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Log.d(TAG, getString(R.string.failed_auth) + task.getException());
                        } else {
                            createUser(signupInputEmail.getText().toString());
                            startActivity(new Intent(SignUpActivity.this, MusicHomeActivity.class));
                            finish();
                        }
                    }
                });
        Toast.makeText(getApplicationContext(), getString(R.string.succ_reg), Toast.LENGTH_SHORT).show();
    }
    private boolean checkEmail() {
        String email = signupInputEmail.getText().toString().trim();
        if (email.isEmpty() || !isEmailValid(email)) {
            Toast.makeText(this, getString(R.string.err_msg_email), Toast.LENGTH_SHORT).show();
            signupInputEmail.setError(getString(R.string.err_msg_required));
            requestFocus(signupInputEmail);
            return false;
        }
        return true;
    }
    private boolean checkPassword() {
        String password = signupInputPassword.getText().toString().trim();
        if (password.isEmpty() || !isPasswordValid(password)) {
            Toast.makeText(this, getString(R.string.err_msg_password), Toast.LENGTH_SHORT).show();
            signupInputPassword.setError(getString(R.string.err_msg_required));
            requestFocus(signupInputPassword);
            return false;
        }
        return true;
    }
    private static boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private static boolean isPasswordValid(String password) {
        return (password.length() >= 6);
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
    private void createUser(String email) {
        if (TextUtils.isEmpty(userId)) {
            userId = mFirebaseDatabase.push().getKey();
        }
        UserData user = new UserData(email);
        mFirebaseDatabase.child(userId).setValue(user);
    }
}

