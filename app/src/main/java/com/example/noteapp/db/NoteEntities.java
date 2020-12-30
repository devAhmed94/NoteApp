package com.example.noteapp.db;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes_table")
public class NoteEntities implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "date_time")
    private String date_time;
    @ColumnInfo(name = "sub_title")
    private String sub_tile;
    @ColumnInfo(name = "note_text")
    private String note_text;
    @ColumnInfo(name = "image_path")
    private String image_path;
    @ColumnInfo(name = "color")
    private String color;
    @ColumnInfo(name = "web_link")
    private String web_link;

    public NoteEntities() {
    }

    public NoteEntities(String title, String date_time, String sub_tile,
                        String note_text, String image_path, String color, String web_link) {
        this.title = title;
        this.date_time = date_time;
        this.sub_tile = sub_tile;
        this.note_text = note_text;
        this.image_path = image_path;
        this.color = color;
        this.web_link = web_link;
    }

    protected NoteEntities(Parcel in) {
        id = in.readInt();
        title = in.readString();
        date_time = in.readString();
        sub_tile = in.readString();
        note_text = in.readString();
        image_path = in.readString();
        color = in.readString();
        web_link = in.readString();
    }

    public static final Creator<NoteEntities> CREATOR = new Creator<NoteEntities>() {
        @Override
        public NoteEntities createFromParcel(Parcel in) {
            return new NoteEntities(in);
        }

        @Override
        public NoteEntities[] newArray(int size) {
            return new NoteEntities[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getSub_tile() {
        return sub_tile;
    }

    public void setSub_tile(String sub_tile) {
        this.sub_tile = sub_tile;
    }

    public String getNote_text() {
        return note_text;
    }

    public void setNote_text(String note_text) {
        this.note_text = note_text;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWeb_link() {
        return web_link;
    }

    public void setWeb_link(String web_link) {
        this.web_link = web_link;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Entities{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date_time='" + date_time + '\'' +
                ", sub_tile='" + sub_tile + '\'' +
                ", note_text='" + note_text + '\'' +
                ", image_path='" + image_path + '\'' +
                ", color='" + color + '\'' +
                ", web_link='" + web_link + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(date_time);
        dest.writeString(sub_tile);
        dest.writeString(note_text);
        dest.writeString(image_path);
        dest.writeString(color);
        dest.writeString(web_link);
    }
}
