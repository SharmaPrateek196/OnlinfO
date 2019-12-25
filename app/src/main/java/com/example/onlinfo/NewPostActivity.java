package com.example.onlinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class NewPostActivity extends AppCompatActivity {
    ImageView iv;
    TextView et_title,et_desc;
    Button btn_post;
    Uri uri=null;

    private StorageReference mRef;
    private FirebaseStorage mStorageReference;

    private FirebaseDatabase mDatabase;
    private DatabaseReference m_db_ref;
    private DatabaseReference m_user_ref;

    private FirebaseAuth mAuth;

    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth=FirebaseAuth.getInstance();

        mDatabase=FirebaseDatabase.getInstance();
        m_db_ref=mDatabase.getReference("posts");
        m_user_ref=mDatabase.getReference("users").child(mAuth.getCurrentUser().getUid()).child("user_posts");

        mStorageReference=FirebaseStorage.getInstance();
        mRef=mStorageReference.getReference("docs/");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        iv=(ImageView)findViewById(R.id.iv_new_post);
        et_title=(TextView)findViewById(R.id.et_title);
        et_desc=(TextView)findViewById(R.id.et_desc);
        btn_post=(Button)findViewById(R.id.btn_post);

        iv.setClickable(true);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PermissionChecker.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(NewPostActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                else {
                    Intent gallery_intent = new Intent();
                    gallery_intent.setType("image/*");
                    gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(gallery_intent, 101);
                }
            }
        });

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(check())
                {
                    pd=new ProgressDialog(NewPostActivity.this);
                    pd.setCancelable(false);
                    pd.setMessage("Please wait, it's uploading...");
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.show();
                    Toast.makeText(NewPostActivity.this, "Posting...", Toast.LENGTH_SHORT).show();
                    mRef.child(uri.getLastPathSegment()).putFile(uri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            if(taskSnapshot.getBytesTransferred() == taskSnapshot.getTotalByteCount())
                            {pd.dismiss();}
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                             mRef.child(uri.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri mUri) {
                                    //photo uploaded successfully, then update data
                                    fun(mUri.toString());
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(NewPostActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void fun(final String toString) {
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser cur_user=mAuth.getCurrentUser();
        String cur_user_uid=cur_user.getUid();
        DatabaseReference temp_ref=FirebaseDatabase.getInstance().getReference("users").child(cur_user_uid);
        final String[] full_name = {""};
        temp_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> hmap=(Map<String, Object>)dataSnapshot.getValue();
                String fname=(String)hmap.get("fname");
                String lname=(String)hmap.get("lname");
                full_name[0] =fname+" "+lname;
                update(full_name[0],toString);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NewPostActivity.this, "Error: "+databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void update(String s,String toString) {
        String timeStamp=timeConvertor();
        String key = m_db_ref.push().getKey();
        long time_for_order_by=1000000000-((System.currentTimeMillis())%1000000000);
        //setting in root->posts->this post
        Post pObj = new Post(key,s,timeStamp,toString, et_title.getText().toString().trim(), et_desc.getText().toString().trim(),time_for_order_by);
        m_db_ref.child(key).setValue(pObj);

        //setting root->user->user_posts->this post
        m_user_ref.child(key).setValue(pObj);

        Toast.makeText(NewPostActivity.this, "Posted successfully", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(i);
        finish();

    }

    private String timeConvertor() {

        Calendar calendar=Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        String hr=Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        String min=Integer.toString(calendar.get(Calendar.MINUTE));
        String am_pm="";
     //am-pm setting
        if(Integer.parseInt(hr)==0){ hr="12"; am_pm="AM";}
        else if(Integer.parseInt(hr)<=11)
        {am_pm="AM";}
        else{am_pm="PM";}
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String times="Posted on "+date+" at "+hr+":"+min+" "+am_pm;

        return times;

    }

    private boolean check() {
        if(uri==null)
        {
            Toast.makeText(NewPostActivity.this, "Please choose an image", Toast.LENGTH_SHORT).show();
            return(false);
        }
        else if(et_title.getText().toString().trim().equals(""))
        {
            Toast.makeText(NewPostActivity.this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(et_desc.getText().toString().trim().equals(""))
        {
            Toast.makeText(NewPostActivity.this, "Please enter description", Toast.LENGTH_SHORT).show();
            return(false);
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK)
        {
            uri=data.getData();
            try {
                Bitmap bitmap=decodeBitmap(uri);
                iv.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public  Bitmap decodeBitmap(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }
}

