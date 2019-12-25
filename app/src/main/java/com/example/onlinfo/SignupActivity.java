package com.example.onlinfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private ProgressDialog pd;
    EditText fname,lname,id,pass,re_pass,contact;
    Button btn_sign_up;

    private FirebaseAuth mAuth;

    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    private String str_fn;
    private String str_ln;
    private String str_email;
    private String str_pwd;
    private String str_re_pwd;
    private String str_contact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fname=(EditText)findViewById(R.id.edFname);
        lname=(EditText)findViewById(R.id.edLname);
        id=(EditText)findViewById(R.id.mail);
        pass=(EditText)findViewById(R.id.passwrd);
        re_pass=(EditText)findViewById(R.id.confirm_passwrd);
        contact=(EditText)findViewById(R.id.mobphone);
        btn_sign_up=(Button)findViewById(R.id.btn_sign_up);

        mAuth=FirebaseAuth.getInstance();

        mDatabase=FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference("users");

        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check()){
                    pd=new ProgressDialog(SignupActivity.this);
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setMessage("Please wait");
                    pd.setCancelable(false);
                    pd.show();

                    final User user=new User(str_fn,str_ln,str_email,str_contact);

                    mAuth.createUserWithEmailAndPassword(str_email,str_pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SignupActivity.this, "Signed-up successfully", Toast.LENGTH_SHORT).show();
                                Intent in=new Intent(SignupActivity.this,MainActivity.class);
                                saveUserInfoInDB(user);
                                startActivity(in);
                                finish();
                            }
                            else
                            {
                                Toast.makeText(SignupActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                            }
                            pd.dismiss();
                        }
                    });
                }
            }
        });



    }

    private void saveUserInfoInDB(User user) {
            String current_user_uid=mAuth.getCurrentUser().getUid();
            mRef.child(current_user_uid).setValue(user);
    }


    private boolean check() {
        str_fn=fname.getText().toString().trim();
        str_ln=lname.getText().toString().trim();
        str_email=id.getText().toString().trim();
        str_pwd=pass.getText().toString().trim();
        str_re_pwd=re_pass.getText().toString().trim();
        str_contact=contact.getText().toString().trim();

        if(str_fn.equals("") || str_ln.equals("") || str_email.equals("") || str_pwd.equals("") || str_re_pwd.equals("")|| str_contact.equals(""))
        {
            Toast.makeText(SignupActivity.this, "Please fill all the details", Toast.LENGTH_SHORT).show();
            return(false);
        }
        else if(str_pwd.length()<6)
        {
            Toast.makeText(this, "Password length should be atleast 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!str_pwd.equals(str_re_pwd))
        {
            Toast.makeText(this, "Both passwords are not matching", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!isValid(str_email))
        {
            Toast.makeText(this, "Enter a valid email id", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}
