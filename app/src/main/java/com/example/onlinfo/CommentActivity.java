package com.example.onlinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    private RecyclerView rv;
    private CommentAdapter adp;
    private ArrayList<Commnt> list;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent got_intent=getIntent();
        final String post_id=got_intent.getStringExtra("post_id");

        sharedPreferences=getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);

        mDatabase=FirebaseDatabase.getInstance();

        rv=(RecyclerView)findViewById(R.id.rv_comment);
        list=new ArrayList<Commnt>();

        String current_user_uid=sharedPreferences.getString("current_user_uid","error");

        adp=new CommentAdapter(CommentActivity.this,list,current_user_uid);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adp);

        mRef=mDatabase.getReference("comments").child(post_id);
        Query comment_query=mRef.orderByChild("time_for_order_by");
        comment_query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Commnt commnt=dataSnapshot.getValue(Commnt.class);
                list.add(commnt);
                adp.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adp.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                adp.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adp.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CommentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_commnt);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent new_comment_int=new Intent(CommentActivity.this,PostComment.class);
                new_comment_int.putExtra("post_id",post_id);
                startActivity(new_comment_int);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
