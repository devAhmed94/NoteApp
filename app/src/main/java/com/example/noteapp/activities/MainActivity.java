package com.example.noteapp.activities;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.noteapp.R;
import com.example.noteapp.adapters.NoteAdapter;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.db.NoteEntities;
import com.example.noteapp.listeners.NoteListener;
import com.example.noteapp.viewModel.NoteViewModel;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements NoteListener {

    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_UPDATE = 1;
    private static final int REQUEST_CODE_PERMISSION = 99;
    private static final int REQUEST_CODE_MAIN_IMAGE = 100;
    private static final int REQUEST_CODE_MAIN_WEB_URL = 101;
    ImageView img_add,main_imageAdd,main_imageGallery,main_imageWebUrl;
    EditText edit_search;
    NoteViewModel noteViewModel;
    RecyclerView recyclerView;
    NoteAdapter noteAdapter;
    List<NoteEntities> noteList;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noteList = new ArrayList<>();
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        img_add = findViewById(R.id.img_add);
        main_imageAdd = findViewById(R.id.main_image_add);
        main_imageGallery = findViewById(R.id.main_image_gallary);
        main_imageWebUrl = findViewById(R.id.main_image_webUrl);
        edit_search = findViewById(R.id.main_et_search);
        img_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CreateNoteActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
        main_imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,CreateNoteActivity.class)
                        ,REQUEST_CODE);
            }
        });
        main_imageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_PERMISSION);
                }else {
                    selectImage();
                }
            }
        });
        main_imageWebUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        recyclerView = findViewById(R.id.main_recycler);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, GridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);
        noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);
        noteAdapter.setNoteListener(this);
        getAllNodes();
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               noteAdapter.cancelTimer();
               noteAdapter.setNoteList(noteList);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
                noteAdapter.setNoteList(noteList);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if ( noteList.size()!=0) {
                    noteAdapter.searchMethod(s.toString());

                }

            }

        });
    }
    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,REQUEST_CODE_MAIN_IMAGE);
    }

    private void getAllNodes(){
        noteViewModel.getAllData();
        noteViewModel.liveDataNode.observe(this, new Observer<List<NoteEntities>>() {
            @Override
            public void onChanged(List<NoteEntities> noteEntities) {
                noteList = noteEntities;
                if (noteEntities !=null) {
                    noteAdapter.setNoteList(noteEntities);
                    noteAdapter.notifyDataSetChanged();

                }
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode==REQUEST_CODE||requestCode==REQUEST_CODE_MAIN_WEB_URL) && resultCode==RESULT_OK){
            getAllNodes();
        }else if (requestCode==REQUEST_CODE_MAIN_IMAGE&&resultCode==RESULT_OK){
            if (data !=null){
                Uri uriPath = data.getData();

                    if (uriPath != null ) {
                        String stringFromUri = getStringFromUri(uriPath);
                        Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                        intent.putExtra("stringUri", stringFromUri);
                        intent.putExtra("openByImage", true);
                        startActivityForResult(intent, 1);
                    }else {
                        Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();

                    }
            }
        }
    }

    @Override
    public void OnNoteClickListener(NoteEntities noteEntities, int position) {
        Intent intent = new Intent(MainActivity.this,CreateNoteActivity.class);
        intent.putExtra("note",noteEntities);
        intent.putExtra("position",position);
        intent.putExtra("update",true);
        startActivityForResult(intent,REQUEST_CODE_UPDATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_PERMISSION&&grantResults.length>0){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                Toast.makeText(this, "didn't take the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getStringFromUri(Uri uri){
        String filePath = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null);
        if (cursor==null){
            filePath =uri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }

        return filePath;
    }

    private void showDialog(){
        if (alertDialog ==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.item_add_url
                            , findViewById(R.id.dialog_container));
            builder.setView(view);
            alertDialog=builder.create();
            if (alertDialog.getWindow()!=null){
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            EditText editText_link = view.findViewById(R.id.dialog_et_url);
            editText_link.requestFocus();

            view.findViewById(R.id.dialog_tv_add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editText_link.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this, " enter your link", Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(editText_link.getText().toString()).matches()){
                        Toast.makeText(MainActivity.this, "enter valid Link", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(MainActivity.this,CreateNoteActivity.class);
                        intent.putExtra("stringLink",editText_link.getText().toString());
                        intent.putExtra("openByWeb",true);
                        startActivityForResult(intent,REQUEST_CODE_MAIN_WEB_URL);
                        alertDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.dialog_tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });


        }
        alertDialog.show();
    }
}