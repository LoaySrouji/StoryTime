package com.continuesvoicerecognition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayView extends AppCompatActivity implements View.OnClickListener {

    HashMap<String, MediaPlayer> arrayList = new HashMap<String, MediaPlayer>();
    HashMap<String, String> imageArrayList = new HashMap<String, String>();


    String story_name;
    ImageView play_pause, imageView;
    MediaPlayer player;
    FirebaseFirestore db;
    int counter_play_pause, counter_appearance, length = 0, last = 0;
    boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_play_view);

        FirebaseApp.initializeApp(PlayView.this);

        db = FirebaseFirestore.getInstance();
        counter_play_pause = 0;
        counter_appearance = 0;

        play_pause = (ImageView) findViewById(R.id.play_view_play_pause_button);
        imageView = (ImageView) findViewById(R.id.play_view_image_view);
        play_pause.setOnClickListener(this);

        imageView.setOnClickListener(this);

        story_name = getIntent().getStringExtra(Manager.STORY_KEY_NAME);
        player = new MediaPlayer();

        if(story_name.equals(Manager.STORY_NAME_SNOW_WHITE))
        {
            imageView.setImageResource(R.drawable.snow_white_cover);
        }else if(story_name.equals(Manager.STORY_NAME_LITTLE_RED_RIDING_HOOD))
        {
            imageView.setImageResource(R.drawable.little_red_riding_hood_cover);
        }else if(story_name.equals(Manager.STORY_NAME_NIGHT_BEFORE_CHRISTMAS))
        {
            imageView.setImageResource(R.drawable.night_before_chirstmas_cover);
        }else{
            imageView.setImageResource(R.drawable.ginger_bread_man_cover);
        }

        new getAudioFromRemoteDB("Loading Story Please Wait...").execute(story_name);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_view_play_pause_button:
                if (counter_play_pause % 2 == 0) {
                    play_pause.setImageResource(R.drawable.pause_button);
                    if(first)
                    {
                        first = false;
                        arrayList.get(0 + "").start();
                        imageView.setImageBitmap(Utility.
                                StringToBitMap(imageArrayList.get(0 + "")));
                    } else {

                        arrayList.get(last + "").seekTo(length);
                        arrayList.get(last + "").start();
                    }

                } else {
                    play_pause.setImageResource(R.drawable.play_button);
                    for(String key: arrayList.keySet())
                    {
                        if(arrayList.get(key).isPlaying())
                        {
                            arrayList.get(key).pause();
                            length = arrayList.get(key).getCurrentPosition();
                            last = Integer.parseInt(key);

                        }
                    }
                }
                counter_play_pause++;
                break;
            case R.id.play_view_image_view:
                if (counter_appearance % 2 == 0) {
                    play_pause.animate()
                            .translationY(view.getHeight())
                            .alpha(0.0f)
                            .setDuration(600)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);

                                }
                            });
                } else {
                    play_pause.animate()
                            .translationY(-1)
                            .alpha(1.0f)
                            .setDuration(600)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);

                                }
                            });
                }
                counter_appearance++;
                break;

        }
    }

    public class getAudioFromRemoteDB extends AsyncTask<String, Void, Void> {

        ProgressDialog d;

        public getAudioFromRemoteDB (String message) {
            d = new ProgressDialog(PlayView.this);
            d.setMessage(message);
            d.setIndeterminate(true);
            d.setCancelable(false);
            d.show();
            play_pause.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Void doInBackground(String... strings) {
            String s_name = strings[0];
            db.collection(Manager.AUDIO_COLLECTION_NAME)
                    .document(Manager.STORY_NAME_TO_COLLECTION.get(s_name))
                    .collection("StoryAudioFiles")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    String docID = documentSnapshot.getId();
                                    Map<String, Object> temp = documentSnapshot.getData();

                                    String encodedAudio, img = "";

                                    encodedAudio = temp.get("audio").toString();
                                    img = temp.get("EncodedImg").toString();

                                    byte[] decoded = Base64.decode(encodedAudio, 0);
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

                                        arrayList.put(docID, mediaPlayer);
                                        imageArrayList.put(docID, img);


                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }

                                }
                                for(int i = 0; i < arrayList.size() - 1; i++)
                                {
                                    arrayList.get(i + "").setNextMediaPlayer(arrayList.
                                            get((i + 1) + ""));
                                    final int j = i;
                                    arrayList.get(i + "").setOnCompletionListener
                                            (new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                            imageView.setImageBitmap(Utility.
                                                    StringToBitMap(imageArrayList.
                                                            get((j + 1) + "")));
                                        }
                                    });
                                }

                                d.dismiss();
                                play_pause.setVisibility(View.VISIBLE);
                            }
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Void v)
        {

        }
    }
}
