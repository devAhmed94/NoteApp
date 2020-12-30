package com.example.noteapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertNote(NoteEntities entities);

    @Delete
    Completable deleteNote(NoteEntities entities);

    @Query("select * from notes_table order by id desc")
   Single< List<NoteEntities>> getAllData();

}
