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

class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    Context mCtx;
    ArrayList<Commnt> pal;
    String curr_user_uid="";

    public CommentAdapter(Context mCtx, ArrayList<Commnt> pal,String curr_user_uid) {
        this.mCtx = mCtx;
        this.pal = pal;
        this.curr_user_uid=curr_user_uid;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf=LayoutInflater.from(mCtx);
        View v=inf.inflate(R.layout.comment_card_view,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.MyViewHolder holder, int position) {
        Commnt commnt = pal.get(position);
        String url = commnt.getUrl();
        final String post_id = commnt.getPost_id();
        Glide.with(mCtx).load(url).into(holder.img);
        image_click(holder, mCtx, url);
        holder.tv_name.setText(commnt.getUser_name());
        holder.tv_time.setText(commnt.getPost_time());
        name_click(holder, mCtx);
    }

    private void name_click(CommentAdapter.MyViewHolder holder, final Context mCtx) {
        holder.tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_profile=new Intent(mCtx,PosterProfileActivity.class);
                mCtx.startActivity(intent_profile);
            }
        });
    }

    private void image_click(CommentAdapter.MyViewHolder holder, final Context context, final String url) {
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
        ImageView img; TextView tv_name,tv_time;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img=(ImageView) itemView.findViewById(R.id.iv_card_view);
            tv_name=(TextView) itemView.findViewById(R.id.tv_poster_name);
            tv_time=(TextView) itemView.findViewById(R.id.tv_post_time);
        }
    }
}
