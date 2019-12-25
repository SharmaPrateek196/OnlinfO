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

public class PostComment extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference m_db_ref;

    private StorageReference mRef;
    private FirebaseStorage mStorageReference;

    ImageView iv_new_comment;
    Button btn_post_commnt;

    Uri uri=null;

    private ProgressDialog pd;

    private FirebaseAuth mAuth;
    String post_id=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_comment);

        mDatabase=FirebaseDatabase.getInstance();
        m_db_ref=mDatabase.getReference("comments");

        mStorageReference= FirebaseStorage.getInstance();
        mRef=mStorageReference.getReference("comments/");

        Intent got_intent=getIntent();
        post_id=got_intent.getStringExtra("post_id");

        iv_new_comment=(ImageView)findViewById(R.id.iv_new_commnt);
        btn_post_commnt=(Button)findViewById(R.id.btn_post_commnt);

        iv_new_comment.setClickable(true);
        iv_new_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(PostComment.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PermissionChecker.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(PostComment.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                else {
                    Intent gallery_intent = new Intent();
                    gallery_intent.setType("image/*");
                    gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(gallery_intent, 101);
                }
            }
        });

        btn_post_commnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(check())
                {
                    pd=new ProgressDialog(PostComment.this);
                    pd.setCancelable(false);
                    pd.setMessage("Please wait, it's uploading...");
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.show();
                    Toast.makeText(PostComment.this, "Posting...", Toast.LENGTH_SHORT).show();
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
                                    fun(mUri.toString());
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(PostComment.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

    }

    private boolean check() {
        if(uri==null)
        {
            Toast.makeText(PostComment.this, "Please choose an image", Toast.LENGTH_SHORT).show();
            return(false);
        }
        return true;
    }

    private void fun(final String toString) {
        mAuth= FirebaseAuth.getInstance();
        FirebaseUser cur_user=mAuth.getCurrentUser();
        String cur_user_uid=cur_user.getUid();
        DatabaseReference temp_ref= FirebaseDatabase.getInstance().getReference("users").child(cur_user_uid);
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
            }
        });

    }

    //s is full name and toString is uri of the image selected
    private void update(String s,String toString) {
        String timeStamp=timeConvertor();
        long time_for_order_by=1000000000-((System.currentTimeMillis())%1000000000);
        Commnt cObj = new Commnt(post_id,s,timeStamp,toString,time_for_order_by);
        String key=m_db_ref.child(post_id).push().getKey();
        m_db_ref.child(post_id).child(key).setValue(cObj);

        Toast.makeText(PostComment.this, "Posted successfully", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(PostComment.this, CommentActivity.class);
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
        String times="Commented on "+date+" at "+hr+":"+min+" "+am_pm;

        return times;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK)
        {
            uri=data.getData();
            try {
                Bitmap bitmap=decodeBitmap(uri);
                iv_new_comment.setImageBitmap(bitmap);
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
