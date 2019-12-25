package com.example.onlinfo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

    Context mCtx;
    ArrayList<Post> pal;
    String curr_user_uid="";

    public MyAdapter(Context mCtx, ArrayList<Post> pal,String curr_user_uid) {
        this.mCtx = mCtx;
        this.pal = pal;
        this.curr_user_uid=curr_user_uid;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf=LayoutInflater.from(mCtx);
        View v=inf.inflate(R.layout.card_view,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Post post=pal.get(position);
        String url=post.getUrl();
        final String post_id=post.getPost_id();
        Glide.with(mCtx).load(url).into(holder.img);
        holder.tv1.setText(post.getTitle());
        holder.tv2.setText(post.getDesc());
        image_click(holder,mCtx,url);
        holder.tv_name.setText(post.getUser_name());
        holder.tv_time.setText(post.getPost_time());
        name_click(holder,mCtx);
        holder.btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_commnt_click(mCtx,view,post_id);
            }
        });
    }

    private void btn_commnt_click(Context mCtx,View view,String post_id) {
        Intent intent_comment=new Intent(mCtx,CommentActivity.class);
        intent_comment.putExtra("post_id",post_id);
        mCtx.startActivity(intent_comment);
    }

    private void name_click(MyViewHolder holder, final Context mCtx) {
        holder.tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_profile=new Intent(mCtx,PosterProfileActivity.class);
                mCtx.startActivity(intent_profile);
            }
        });
    }


    private void image_click(MyViewHolder holder, final Context context, final String url) {
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inte=new Intent(context,DownloadActivity.class);
                inte.putExtra("url",url);
                context.startActivity(inte);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pal.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView img; TextView tv1,tv2,tv_name,tv_time,btn_comment;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            btn_comment=(TextView) itemView.findViewById(R.id.btn_comment);
            img=(ImageView) itemView.findViewById(R.id.iv_card_view);
            tv1=(TextView) itemView.findViewById(R.id.title_card_view);
            tv2=(TextView) itemView.findViewById(R.id.desc_card_view);
            tv_name=(TextView) itemView.findViewById(R.id.tv_poster_name);
            tv_time=(TextView) itemView.findViewById(R.id.tv_post_time);
        }
    }
}
