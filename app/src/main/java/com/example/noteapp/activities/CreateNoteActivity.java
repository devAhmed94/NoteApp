package com.example.noteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noteapp.R;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.db.NoteEntities;
import com.example.noteapp.viewModel.NoteViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CreateNoteActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_IMAGE = 2;
    ImageView image_back,image_save,imageNode,removeImage,removeLink;
    EditText inputNoteTitle,inputNoteSubTitle,inputNoteText;
    LinearLayout linear_link;
    TextView tv_link;
    TextView txtDate;
    NoteViewModel noteViewModel;
    String select_note_color;
    View subTitleIndicator;
    String uriString ;
    AlertDialog alertDialog;
    AlertDialog alertDeleteDialog;
    NoteEntities update_note =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        uriString ="";
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);

        image_back = findViewById(R.id.arrow_img);
        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.input_note_title);
        inputNoteSubTitle = findViewById(R.id.input_sub_title);
        inputNoteText = findViewById(R.id.input_note_text);
        subTitleIndicator = findViewById(R.id.subTitleIndicator);
        imageNode =findViewById(R.id.imageNote);
        linear_link = findViewById(R.id.linear_link);
        tv_link = findViewById(R.id.tv_link);

        txtDate = findViewById(R.id.txt_date_time);
        txtDate.setText(
                new SimpleDateFormat("EEEE ,dd MMMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );

       image_save = findViewById(R.id.image_save);
       image_save.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               saveNote();
           }
       });

       removeImage = findViewById(R.id.remove_image);
       removeImage.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               imageNode.setImageBitmap(null);
               imageNode.setVisibility(View.GONE);
               removeImage.setVisibility(View.GONE);
               uriString="";

           }
       });

       removeLink = findViewById(R.id.remove_link);
       removeLink.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               tv_link.setText("");
               linear_link.setVisibility(View.GONE);
           }
       });
       if (getIntent().getBooleanExtra("update", false)){
            update_note = (NoteEntities) getIntent().getExtras().get("note");
            updateMethods(update_note);
       }
       if (getIntent().getBooleanExtra("openByImage",false)){
           uriString = getIntent().getStringExtra("stringUri");
           imageNode.setImageBitmap(BitmapFactory.decodeFile(uriString));
           imageNode.setVisibility(View.VISIBLE);
           removeImage.setVisibility(View.VISIBLE);
       }
       if (getIntent().getBooleanExtra("openByWeb",false)){
           String stringLink = getIntent().getStringExtra("stringLink");
           tv_link.setText(stringLink);
           linear_link.setVisibility(View.VISIBLE);
       }

       select_note_color ="#333333";//default color
        initMiscellaneous();
        initIndicator();
    }

    private void updateMethods(NoteEntities update_note) {
        inputNoteTitle.setText(update_note.getTitle());
        inputNoteSubTitle.setText(update_note.getSub_tile());
        inputNoteText.setText(update_note.getNote_text());
        txtDate.setText(update_note.getDate_time());
        if (update_note.getImage_path()!=null && !update_note.getImage_path().trim().isEmpty()){
            imageNode.setImageBitmap(BitmapFactory.decodeFile(update_note.getImage_path().trim()));
            imageNode.setVisibility(View.VISIBLE);
            removeImage.setVisibility(View.VISIBLE);
            uriString = update_note.getImage_path();
        }
        if (update_note.getWeb_link() !=null && !update_note.getWeb_link().trim().isEmpty()){
            tv_link.setText(update_note.getWeb_link().trim());
            linear_link.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote(){
        String inputTitle = inputNoteTitle.getText().toString().trim();
        String inputSubTitle = inputNoteSubTitle.getText().toString().trim();
        String inputText = inputNoteText.getText().toString().trim();
        String string_date = txtDate.getText().toString().trim();

        if (inputTitle.isEmpty()){
            Toast.makeText(this, "Title cant be empty", Toast.LENGTH_SHORT).show();
        }else if (inputSubTitle.isEmpty()&&inputText.isEmpty()){
            Toast.makeText(this, "your Note cant be empty", Toast.LENGTH_SHORT).show();
        }else {
            NoteEntities note = new NoteEntities();
            note.setTitle(inputTitle);
            note.setSub_tile(inputSubTitle);
            note.setDate_time(string_date);
            note.setNote_text(inputText);
            note.setColor(select_note_color);
            note.setImage_path(uriString);
            if (linear_link.getVisibility()==View.VISIBLE){
                note.setWeb_link(tv_link.getText().toString());
            }

            if (update_note !=null){
                note.setId(update_note.getId());
            }
            noteViewModel.insertNode(note);


            setResult(RESULT_OK);
            finish();
        }
    }
    private void initMiscellaneous(){
        LinearLayout layout_miscellaneous =findViewById(R.id.layout_miscellaneous);
        BottomSheetBehavior<LinearLayout> sheetBehavior = BottomSheetBehavior.from(layout_miscellaneous);
       layout_miscellaneous.findViewById(R.id.txt_miscellaneous).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (sheetBehavior.getState() !=BottomSheetBehavior.STATE_EXPANDED){
                   sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
               }else {
                   sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
               }
           }
       });

       ImageView imageView1 = layout_miscellaneous.findViewById(R.id.imageColor1);
       ImageView imageView2 = layout_miscellaneous.findViewById(R.id.imageColor2);
       ImageView imageView3 = layout_miscellaneous.findViewById(R.id.imageColor3);
       ImageView imageView4 = layout_miscellaneous.findViewById(R.id.imageColor4);
       ImageView imageView5 = layout_miscellaneous.findViewById(R.id.imageColor5);

       layout_miscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               select_note_color ="#333333";
               imageView1.setImageResource(R.drawable.ic_done);
               imageView2.setImageResource(0);
               imageView3.setImageResource(0);
               imageView4.setImageResource(0);
               imageView5.setImageResource(0);
               initIndicator();
           }
       });

        layout_miscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_note_color ="#fdbe38";
                imageView1.setImageResource(0);
                imageView2.setImageResource(R.drawable.ic_done);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                initIndicator();
            }
        });

        layout_miscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_note_color ="#ff4842";
                imageView1.setImageResource(0);
                imageView2.setImageResource(0);
                imageView3.setImageResource(R.drawable.ic_done);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                initIndicator();
            }
        });
        layout_miscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_note_color ="#3a52fc";
                imageView1.setImageResource(0);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(R.drawable.ic_done);
                imageView5.setImageResource(0);
                initIndicator();
            }
        });
        layout_miscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_note_color ="#000000";
                imageView1.setImageResource(0);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(R.drawable.ic_done);
                initIndicator();
            }
        });


        if (update_note !=null &&update_note.getColor()!=null && !update_note.getColor().trim().isEmpty()){
            switch (update_note.getColor()){
                case "#fdbe38":
                    layout_miscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#ff4842":
                    layout_miscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3a52fc":
                    layout_miscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layout_miscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;
                default:
                    layout_miscellaneous.findViewById(R.id.viewColor1).performClick();

            }
        }
         layout_miscellaneous.findViewById(R.id.txt_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
               if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                       != PackageManager.PERMISSION_GRANTED){
                   ActivityCompat.requestPermissions(CreateNoteActivity.this,
                           new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
               }else {
                   getImage();
               }
            }
        });
         layout_miscellaneous.findViewById(R.id.layout_add_url).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                 showDialog();
             }
         });
         if (update_note==null){
             layout_miscellaneous.findViewById(R.id.layout_delete).setVisibility(View.GONE);
         }else{
             layout_miscellaneous.findViewById(R.id.layout_delete).setVisibility(View.VISIBLE);
             layout_miscellaneous.findViewById(R.id.layout_delete).setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                     showDeleteDialog();
                 }
             });
         }

    }

    private void showDeleteDialog() {
       AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
       View view = LayoutInflater.from(CreateNoteActivity.this).inflate(R.layout.item_delete_layout,
               (ViewGroup) findViewById(R.id.layout_container_dialog_delete));
       builder.setView(view);
       alertDeleteDialog = builder.create();
       if (alertDeleteDialog.getWindow() !=null){
           alertDeleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
       }
       view.findViewById(R.id.dialog_tv_delete).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               noteViewModel.deleteNode(update_note);
               Intent intent = new Intent(CreateNoteActivity.this,MainActivity.class);
               intent.putExtra("DeleteNote",true );
               setResult(RESULT_OK,intent);
               alertDeleteDialog.dismiss();
               finish();
           }
       });
       view.findViewById(R.id.dialog_tv_delete_cancel).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               alertDeleteDialog.dismiss();
           }
       });
       alertDeleteDialog.show();
    }

    private void getImage(){
        Intent  intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) !=null){
            startActivityForResult(intent,REQUEST_IMAGE);
        }

    }
    private void initIndicator(){
        GradientDrawable gradientDrawable = (GradientDrawable) subTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(select_note_color));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode ==RESULT_OK){
            if (data != null){

                Uri path = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(path);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageNode.setImageBitmap(bitmap);
                    imageNode.setVisibility(View.VISIBLE);
                    removeImage.setVisibility(View.VISIBLE);
                    uriString = getStringFromUri(path);
                } catch (Exception e) {
                    Toast.makeText(CreateNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==REQUEST_PERMISSION && grantResults.length>0){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getImage();
            }else {
                Toast.makeText(this, " the permission denay !", Toast.LENGTH_SHORT).show();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(CreateNoteActivity.this)
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
                        Toast.makeText(CreateNoteActivity.this, " enter your link", Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(editText_link.getText().toString()).matches()){
                        Toast.makeText(CreateNoteActivity.this, "enter valid Link", Toast.LENGTH_SHORT).show();
                    }else {
                        tv_link.setText(editText_link.getText().toString());
                        linear_link.setVisibility(View.VISIBLE);
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
