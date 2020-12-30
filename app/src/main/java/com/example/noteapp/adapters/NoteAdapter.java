package com.example.noteapp.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteapp.R;
import com.example.noteapp.activities.MainActivity;
import com.example.noteapp.db.NoteEntities;
import com.example.noteapp.listeners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<NoteEntities> noteList;
    List<NoteEntities> searchList;
    NoteListener noteListener;
    Timer timer;
    public void setNoteListener(NoteListener noteListener){
        this.noteListener = noteListener;
    }
    public NoteAdapter(List<NoteEntities> noteList) {
        this.noteList = noteList;
    }

    public void setNoteList(List<NoteEntities> noteList) {
        this.noteList = noteList;
        this.searchList = noteList;

        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycler_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {

        holder.txt_title.setText(noteList.get(position).getTitle());
        if (noteList.get(position).getSub_tile() !=null) {
            holder.txt_subTitle.setText(noteList.get(position).getSub_tile());
        }else {
            holder.txt_subTitle.setVisibility(View.GONE);
        }
        holder.txt_date.setText(noteList.get(position).getDate_time());
        GradientDrawable gradientDrawable = (GradientDrawable) holder.layout_note.getBackground();
        if (noteList.get(position).getColor() !=null) {
            gradientDrawable.setColor(Color.parseColor(noteList.get(position).getColor()));
        }else {
            gradientDrawable.setColor(Color.parseColor("#333333"));

        }

        if (noteList.get(position).getImage_path() !=null){
            holder.roundedImageView.setImageBitmap(BitmapFactory.decodeFile(noteList.get(position).getImage_path()));
            holder.roundedImageView.setVisibility(View.VISIBLE);
        }else {
            holder.roundedImageView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteListener.OnNoteClickListener(noteList.get(position),position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }


    public void searchMethod(String searchKeyword){
        Log.d("AHMED", "searchMethod: test2 ");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()){
                    noteList = searchList;
                }else {
                    ArrayList<NoteEntities> tempList = new ArrayList<>();
                    for (NoteEntities note:searchList) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                ||note.getSub_tile().toLowerCase().contains(searchKeyword)
                                ||note.getNote_text().toLowerCase().contains(searchKeyword)){
                            tempList.add(note);
                        }
                    }
                    noteList =tempList;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });

                }
            }
        },500);


    }
    public void cancelTimer(){
        if (timer !=null){
            timer.cancel();
        }
    }

    class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView txt_title,txt_subTitle,txt_date;
        LinearLayout layout_note;
        RoundedImageView roundedImageView;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_title = itemView.findViewById(R.id.item_note_title);
            txt_subTitle = itemView.findViewById(R.id.item_note_sub_title);
            txt_date = itemView.findViewById(R.id.item_txt_date);
            layout_note = itemView.findViewById(R.id.layout_item_note);
            roundedImageView = itemView.findViewById(R.id.roundImage);
        }
    }

}
