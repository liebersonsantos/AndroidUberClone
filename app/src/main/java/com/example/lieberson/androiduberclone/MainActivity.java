package com.example.lieberson.androiduberclone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.lieberson.androiduberclone.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private Button btnSignIn, btnRegister;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private RelativeLayout rootLayout;
    private android.app.AlertDialog waitingDialog;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                        .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                        .setFontAttrId(R.attr.fontPath)
                                        .build());
        setContentView(R.layout.activity_main);

        //init firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");

        initViews();

        //Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showRegisterDialog();

            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showLoginDialog();
            }
        });

    }

    private void showLoginDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login, null);

        final MaterialEditText editEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText editPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        //set Button
        dialog.setPositiveButton("SING IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //set disable button signIn if processing
                        btnSignIn.setEnabled(false);

                        //Validation
                        if (TextUtils.isEmpty(editEmail.getText().toString())) {

                            Snackbar.make(rootLayout, "Please, enter email address", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(editPassword.getText().toString())) {

                            Snackbar.make(rootLayout, "Please, enter password", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        if (editPassword.getText().toString().length() < 6) {

                            Snackbar.make(rootLayout, "Password too short!", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        showWaitingDialog();

                        //Login
                        auth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        dismissWaitingDialog();

                                        startActivity(new Intent(MainActivity.this, Welcome.class));
                                        finish();
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dismissWaitingDialog();
                                Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                //active button
                                btnSignIn.setEnabled(true);
                            }
                        });
                    }
                });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void showRegisterDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText editEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText editPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText editName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText editPhone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        //set Button
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Validation
                if (TextUtils.isEmpty(editEmail.getText().toString())){

                    Snackbar.make(rootLayout, "Please, enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(editPhone.getText().toString())){

                    Snackbar.make(rootLayout, "Please, enter phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(editPassword.getText().toString())){

                    Snackbar.make(rootLayout, "Please, enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (editPassword.getText().toString().length() < 6){

                    Snackbar.make(rootLayout, "Password too short!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //Register new user
                auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                //Save user to db
                                User user = new User();
                                user.setEmail(editEmail.getText().toString());
                                user.setName(editName.getText().toString());
                                user.setPhone(editPhone.getText().toString());
                                user.setPassword(editPassword.getText().toString());

                                //Use email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Snackbar.make(rootLayout, "Register success fully", Snackbar.LENGTH_SHORT).show();

                                            }
                                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void initViews() {

        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);
        rootLayout = findViewById(R.id.rootLayout);
    }

    private void showWaitingDialog() {
        waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
        waitingDialog.show();
    }

    private void dismissWaitingDialog() {
        waitingDialog.dismiss();
    }

}
