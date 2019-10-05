package com.continuesvoicerecognition;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements View.OnClickListener {

    public final static HashMap<String, String> imageIDs = new HashMap<String, String>();
    public final static HashMap<String, String> audioIDs = new HashMap<String, String>();

    TextView storyText, storyTitle;
    Button terminate;
    ImageView playPauseImageView;

    SpeechRecognizerManager mSpeechManager;

    FirebaseFirestore db;
    String StoryName = "";
    boolean first = true;
    int sessionId = 0, counter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        storyText = (TextView) findViewById(R.id.txtDetails);
        storyTitle = (TextView) findViewById(R.id.StoryTitle);

        terminate = (Button) findViewById(R.id.Terminate);

        playPauseImageView = (ImageView) findViewById(R.id.play_pause_imageView);
        playPauseImageView.setImageResource(R.drawable.play_button);
        playPauseImageView.setVisibility(View.VISIBLE);
        playPauseImageView.setOnClickListener(this);
        counter = 0;

        FirebaseApp.initializeApp(MainActivity.this);
        db = FirebaseFirestore.getInstance();

        StoryName = getIntent().getStringExtra(Manager.STORY_KEY_NAME);
        storyTitle.setText(StoryName);
        sessionId = Integer.parseInt(getIntent().getStringExtra("key"));

        new getSavedSpeech("Loading Story Text...").
                execute(Manager.STORY_NAME_TO_COLLECTION.get(StoryName));

        terminate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (PermissionHandler.checkPermission(this)) {

            switch (v.getId()) {
                case R.id.play_pause_imageView:
                    if(counter % 2 == 0) {
                        playPauseImageView.setImageResource(R.drawable.pause_button);
                        if (first) {
                            first = false;
                            new goFirst().execute();
                        }
                        if (mSpeechManager == null) {
                            SetSpeechListener();
                        } else if (!mSpeechManager.ismIsListening()) {
                            mSpeechManager.destroy();
                            SetSpeechListener();
                        }
                    }else {
                        playPauseImageView.setImageResource(R.drawable.play_button);
                        if (mSpeechManager != null) {
                            mSpeechManager.destroy();
                            mSpeechManager = null;
                        }
                    }
                    counter++;
                    break;
                case R.id.Terminate:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Terminating story session")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    terminateConnection();
                                    finish();
                                }
                            })
                            .setNegativeButton("No", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
            }
        } else {
            PermissionHandler.askForPermission(this);
        }
    }
    public void terminateConnection() {
        db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                .document(Manager.CONNECTION_SUB_NAME + sessionId)
                .collection(Manager.SUB_CONNECTION_COLLECTION)
                .document(Manager.IMG_DOCUMENT_ID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                        .document(Manager.ALL_CONNECTIONS_DOCUMENT_IDS)
                        .collection(Manager.ALL_CONNECTIONS_COLLECTION)
                        .document(Manager.CONNECTION_SUB_NAME + sessionId)
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(MainActivity.this, "Data deleted !",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHandler.RECORD_AUDIO) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    playPauseImageView.performClick();
                }
            }
        }
    }

    private void SetSpeechListener() {
        mSpeechManager = new SpeechRecognizerManager(this, new SpeechRecognizerManager.
                onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {
                if (results != null && results.size() > 0) {
                    new processSpeech().execute(results.get(0));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        if (mSpeechManager != null) {
            mSpeechManager.destroy();
            mSpeechManager = null;
        }
        super.onPause();
    }

    //                                            #imput #progress #result
    private class processSpeech extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            for (final String sentence : imageIDs.keySet()) {
                if (params[0].toLowerCase().contains(sentence.toLowerCase())) {
                    Map<String, Object> map = new HashMap<String, Object>() {{
                        put(Manager.IMG_DOCUMENT_FIELD, imageIDs.get(sentence));
                        put(Manager.AUDIO_DOCUMENT_FIELD, audioIDs.get(sentence));
                    }};
                    db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                            .document(Manager.CONNECTION_SUB_NAME + sessionId)
                            .collection(Manager.SUB_CONNECTION_COLLECTION)
                            .document(Manager.IMG_DOCUMENT_ID)
                            .set(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(" ", "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(" ", "Error writing document", e);
                                }
                            });
                    return sentence;
                }
            }
            return "";
        }
    }


    public class getSavedSpeech extends AsyncTask<String, Void, Void> {
        ProgressDialog d;

        public getSavedSpeech (String message) {
            d = new ProgressDialog(MainActivity.this);
            d.setMessage(message);
            d.setIndeterminate(true);
            d.setCancelable(false);
            playPauseImageView.setVisibility(View.GONE);
            d.show();

        }
        @Override
        protected Void doInBackground(String... params) {
            final String story = params[0];

            db.collection(Manager.MAIN_STORIES_COLLECTION)
                    .document(story)
                    .collection(Manager.SUB_STORY_COLLECTION)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    String docID = documentSnapshot.getId();
                                    Map<String, Object> temp = documentSnapshot.getData();

                                    if (docID.equals(Manager.STORY_TEXT_DOCUMENT_ID)) {
                                        String text = temp.get(Manager.STORY_TEXT_DOCUMENT_FIELD)
                                                .toString()
                                                .replace("\\n", System.getProperty
                                                        ("line.separator")
                                                        + System.getProperty("line.separator"));

                                        storyText.setText(text);

                                    } else {
                                        String encodedImg, encodedAudio;
                                        encodedImg = temp.get(Manager.ENCODED_FIELD).toString();
                                        encodedAudio = temp.get(Manager.ENCODED_AUDIO_FIELD).toString();
                                        imageIDs.put(docID, encodedImg);
                                        audioIDs.put(docID, encodedAudio);
                                    }
                                }
                                Map<String, Object> temp = new HashMap<String, Object>() {{
                                    put(Manager.IMG_DOCUMENT_ID, imageIDs.get(Manager.COVER_ID));
                                    put(Manager.AUDIO_DOCUMENT_FIELD, "");
                                }};
                                db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                                        .document(Manager.CONNECTION_SUB_NAME + sessionId)
                                        .collection(Manager.SUB_CONNECTION_COLLECTION)
                                        .document(Manager.IMG_DOCUMENT_ID)
                                        .set(temp)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(" ", "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(" ", "Error writing document", e);
                                            }
                                        });
                                playPauseImageView.setVisibility(View.VISIBLE);
                                d.dismiss();
                            }
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

        }
    }



    public class goFirst extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Map<String, Object> map = new HashMap<String, Object>() {{
                put(Manager.IMG_DOCUMENT_FIELD, imageIDs.get(Manager.FIRST_ID));
                put(Manager.AUDIO_DOCUMENT_FIELD, audioIDs.get(Manager.FIRST_ID));
            }};
            db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                    .document(Manager.CONNECTION_SUB_NAME + sessionId)
                    .collection(Manager.SUB_CONNECTION_COLLECTION)
                    .document(Manager.IMG_DOCUMENT_ID)
                    .set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(" ", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(" ", "Error writing document", e);
                        }
                    });
            return null;
        }
    }
}
