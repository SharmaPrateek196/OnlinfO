package com.example.onlinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    private RecyclerView rv;
    private MyAdapter adp;
    private ArrayList<Post> list;

    TextView tv_current_user_name,tv_current_user_id;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FloatingActionButton fab;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String current_user_uid="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.frame_layout);
        frameLayout.setVisibility(View.GONE);

        sharedPreferences=getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        mDatabase=FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference("users");

        mAuth=FirebaseAuth.getInstance();
        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                tv_current_user_id=(TextView)findViewById(R.id.tv_logged_in_user_id);
                tv_current_user_name=(TextView) findViewById(R.id.tv_logged_in_user_name);
                if(user!=null) //user is already logged in
                {
                    String str_current_user_id=user.getEmail();
                    String temp_uid;
                    temp_uid = user.getUid();

                    mRef=mDatabase.getReference("users");
                    mRef.child(temp_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String,Object> obj=(Map<String,Object>)dataSnapshot.getValue();
                            String fn="";
                            String ln="";
                            fn= (String) obj.get("fname");
                            ln= (String) obj.get("lname");
                            String full_name=fn+" "+ln;
                            tv_current_user_name.setText(full_name);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    tv_current_user_id.setText(str_current_user_id);
                }
                else
                { finish();
                    Intent sign_out_int=new Intent(MainActivity.this,LoginActivity.class);
                       startActivity(sign_out_int);
                }
            }
          };
        mAuth.addAuthStateListener(mAuthStateListener);

        //update shared pref
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            current_user_uid=firebaseUser.getUid();
            //update shared pref
            editor.putString("current_user_uid",current_user_uid);
            editor.apply();

            Log.v("shared_id==",sharedPreferences.getString("current_user_uid","wrong"));
        }

        rv=(RecyclerView)findViewById(R.id.recycler_view);
        list=new ArrayList<Post>();

        adp=new MyAdapter(MainActivity.this,list,current_user_uid);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adp);

        mRef=mDatabase.getReference("posts");

        //'order by' query
        Query post_query=mRef.orderByChild("time_for_order_by");
        post_query.addChildEventListener(new ChildEventListener() {
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
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


        //floating action bar
         fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("onResume()==","a");
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener!=null)
        {
            if(mAuth==null)
            {mAuth.removeAuthStateListener(mAuthStateListener);
                editor.clear();
                editor.apply();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.options_sign_out) {
            mAuth.signOut();
            editor.clear();
            editor.apply();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void killActivity() {
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment=null;
        if (id == R.id.nav_my_posts) {
            rv.setVisibility(View.GONE);
            FrameLayout frameLayout=(FrameLayout)findViewById(R.id.frame_layout);
            frameLayout.setVisibility(View.VISIBLE);
            fragment=new MyPostsFragment();
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout,fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_news_feed) {
            FrameLayout frameLayout=(FrameLayout)findViewById(R.id.frame_layout);
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_about) {
            rv.setVisibility(View.GONE);
            FrameLayout frameLayout=(FrameLayout)findViewById(R.id.frame_layout);
            frameLayout.setVisibility(View.VISIBLE);
            fragment=new AboutDevFrag();
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout,fragment);
            fragmentTransaction.commit();
        }
        else if(id== R.id.nav_paytm)
        {
            rv.setVisibility(View.GONE);
            FrameLayout frameLayout=(FrameLayout)findViewById(R.id.frame_layout);
            frameLayout.setVisibility(View.VISIBLE);
            fragment=new PaytmFragment();
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout,fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
