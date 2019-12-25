package com.example.onlinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPostsFragment extends Fragment {

    private RecyclerView rv;
    private MyAdapter adp;
    private ArrayList<Post> list;

    private SharedPreferences sharedPreferences;

    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_posts,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv=(RecyclerView) view.findViewById(R.id.rv_my_posts);
        list=new ArrayList<Post>();
        sharedPreferences=getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String current_user_uid=sharedPreferences.getString("current_user_uid","wrong");
        adp=new MyAdapter(getActivity(),list,current_user_uid);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adp);
        mDatabase=FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference("users").child(current_user_uid);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("user_posts"))
                {
                    Query query=mRef.child("user_posts").orderByChild("time_for_order_by");
                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Post post=dataSnapshot.getValue(Post.class);
                            list.add(post);
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
                            adp.notifyDataSetChanged();
                        }
                    });
                }
                else
                {
                    Toast.makeText(getActivity(), "No Posts yet", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error:"+databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
