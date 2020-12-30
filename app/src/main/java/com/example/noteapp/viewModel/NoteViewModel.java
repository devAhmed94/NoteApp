package com.example.noteapp.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.noteapp.activities.CreateNoteActivity;
import com.example.noteapp.activities.MainActivity;
import com.example.noteapp.db.NoteDao;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.db.NoteEntities;

import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import io.reactivex.schedulers.Schedulers;

public class NoteViewModel extends AndroidViewModel {
    public MutableLiveData<List<NoteEntities>> liveDataNode;
    public NoteDao noteDao;
    CompositeDisposable disposable = new CompositeDisposable();

    public NoteViewModel(@NonNull Application application) {
        super(application);
        NoteDatabase noteDatabase = NoteDatabase.getInstance(application.getApplicationContext());
        this.noteDao = noteDatabase.noteDao();
        liveDataNode = new MutableLiveData<>();
    }

    public void insertNode(NoteEntities noteEntities) {

        Log.d("MY NOTES", "onComplete: test ");
        noteDao.insertNote(noteEntities).subscribeOn(Schedulers.computation())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.d("MY NOTES", "onComplete: done insert ");
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.d("MY NOTES", "onError: " + e.getMessage());
                    }
                });

    }

    public void deleteNode(NoteEntities noteEntities) {
        noteDao.deleteNote(noteEntities).subscribeOn(Schedulers.computation()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                disposable.add(d);
            }

            @Override
            public void onComplete() {

                Log.d("MY NOTES", "onComplete: delete done ");
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                Log.d("MY NOTES", "onError " + e.getMessage());
            }
        });
    }

    public void getAllData() {
        noteDao.getAllData()
                .subscribeOn(Schedulers.computation())
                .subscribe(new SingleObserver<List<NoteEntities>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull List<NoteEntities> noteEntities) {

                        liveDataNode.postValue(noteEntities);

                        if ((noteEntities != null && noteEntities.size() > 0)) {
                            Log.d("MY NOTES", "onSuccess: " + noteEntities.get(0).getTitle());
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                        Log.d("MY NOTES", "onError: " + e.getMessage());
                    }
                });


    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
