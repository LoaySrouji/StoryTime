package com.continuesvoicerecognition;

import android.app.ProgressDialog;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Viewing extends AppCompatActivity {

    FirebaseFirestore db;
    String sessionId;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_viewing);

        sessionId = getIntent().getStringExtra("key");
        imageView = (ImageView) findViewById(R.id.imageView);
        db = FirebaseFirestore.getInstance();
        final ProgressDialog d;

        d = new ProgressDialog(Viewing.this);
        d.setMessage("Connecting To Reader ...");
        d.setIndeterminate(true);
        d.setCancelable(false);
        d.show();


        db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                .document("Connection" + sessionId)
                .collection("StoryTime")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
//                                    Log.d("TAG", "New Msg: " + dc.getDocument().toObject(Message.class));
                                    break;
                                case MODIFIED:
                                    if(d.isShowing())
                                    {
                                        d.dismiss();
                                    }
                                    String str = dc.getDocument().getString("img");
                                    imageView.setImageBitmap(Utility.StringToBitMap(str));
                                    String audio = dc.getDocument().getString("music");
                                    if(!audio.equals("") && !audio.equals("drawable")) {
                                        byte[] decoded = Base64.decode(audio, 0);
                                        try {
                                            File myTemp = File.createTempFile("TCL", "mp3",
                                                    getCacheDir());
                                            myTemp.deleteOnExit();
                                            FileOutputStream fos = new FileOutputStream(myTemp);
                                            fos.write(decoded);
                                            fos.close();

                                            MediaPlayer mediaPlayer = new MediaPlayer();
                                            FileInputStream myFile = new FileInputStream(myTemp);
                                            mediaPlayer.setDataSource(myFile.getFD());

                                            mediaPlayer.prepare();
                                            mediaPlayer.start();

                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                    else if(audio.equals("drawable"))
                                    {
                                        AssetFileDescriptor afd = null;
                                        try {
                                            afd = getAssets().openFd("raw/night_before_christmas_background.mp3");
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                        MediaPlayer player = new MediaPlayer();
                                        try {
                                            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }

                                        player.setVolume(1f,1f);
                                        try {
                                            player.prepare();
                                            player.start();
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }

                                    break;
                                case REMOVED:
                                    finish();
                                    break;
                            }
                        }

                    }
                });
    }
}
