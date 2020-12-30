package com.example.noteapp.listeners;

import com.example.noteapp.db.NoteEntities;

public interface NoteListener {
    void OnNoteClickListener(NoteEntities noteEntities, int position);
}
