package com.example.onlinfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    EditText id,pass;
    Button btn_log_in;
    TextView tv;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) //user is already logged in
                {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };

        initialize();

       btn_log_in.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                String str_id=id.getText().toString().trim();
                String str_pass=pass.getText().toString().trim();

               if(check())
               {
                   final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                   pd.setCancelable(false);
                   pd.setMessage("Verifying Credentials");
                   pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                   pd.show();
                   mAuth.signInWithEmailAndPassword(str_id,str_pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           pd.dismiss();
                           if(task.isSuccessful())
                           {
                               Intent i=new Intent(LoginActivity.this,MainActivity.class);
                               startActivity(i);
                               finish();
                           }
                           else
                           {
                               Toast.makeText(LoginActivity.this, "Please check your id or password", Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
               }
           }
       });

       tv.setClickable(true);
       tv.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent i=new Intent(LoginActivity.this,SignupActivity.class);
               startActivity(i);
           }
       });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener!=null)
        {
            if(mAuth==null)
            {mAuth.removeAuthStateListener(mAuthStateListener);}
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean check() {
        if((id.getText().toString().trim()).equals("") || (pass.getText().toString().trim()).equals(""))
        {
            Toast.makeText(this, "Please fill both email and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initialize() {
        id=(EditText)findViewById(R.id.input_email);
        pass=(EditText)findViewById(R.id.input_password);
        btn_log_in=(Button) findViewById(R.id.btn_login);
        tv=(TextView) findViewById(R.id.link_signup);

        mAuth=FirebaseAuth.getInstance();

    }
}
