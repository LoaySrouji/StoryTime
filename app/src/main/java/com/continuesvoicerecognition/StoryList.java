package com.continuesvoicerecognition;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class StoryList extends AppCompatActivity implements View.OnClickListener {

    Button snow_white, little_red_riding_hood, bread_man, night_before_christmas;
    FirebaseFirestore db;
    int sessionID = 0;
    String play_read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story_list);
        db = FirebaseFirestore.getInstance();

        snow_white = (Button) findViewById(R.id.button_snow_white_story);
        snow_white.setBackgroundResource(R.drawable.snow_white_cover);

        little_red_riding_hood = (Button) findViewById(R.id.little_red_riding_hood);
        little_red_riding_hood.setBackgroundResource(R.drawable.little_red_riding_hood_cover);

        bread_man = (Button) findViewById(R.id.bread_man);
        bread_man.setBackgroundResource(R.drawable.ginger_bread_man_cover);

        night_before_christmas = (Button) findViewById(R.id.night_before_christmas);
        night_before_christmas.setBackgroundResource(R.drawable.night_before_chirstmas_cover);


        snow_white.setOnClickListener(this);
        little_red_riding_hood.setOnClickListener(this);
        bread_man.setOnClickListener(this);
        night_before_christmas.setOnClickListener(this);

        play_read = getIntent().getStringExtra(Manager.PLAY_VIEW_KEY);
    }

    @Override
    public void onClick(View v) {
        String story_name = "";

        switch (v.getId()) {
            case R.id.button_snow_white_story:
                story_name = Manager.STORY_NAME_SNOW_WHITE;
                break;

            case R.id.little_red_riding_hood:
                story_name = Manager.STORY_NAME_LITTLE_RED_RIDING_HOOD;
                break;

            case R.id.bread_man:
                story_name = Manager.STORY_NAME_THE_GINGERBREAD_MAN;
                break;

            case R.id.night_before_christmas:
                story_name = Manager.STORY_NAME_NIGHT_BEFORE_CHRISTMAS;
                break;

            default:
                break;

        }
        if(play_read.equals(Manager.PLAY_AUDIO))
        {
            Intent intent = new Intent(StoryList.this, PlayView.class);
            intent.putExtra(Manager.STORY_KEY_NAME, story_name);
            startActivity(intent);
        }else {
            createConnection(story_name);
        }
    }

    public void createConnection(final String story_name) {
        final ProgressDialog d;
        d = new ProgressDialog(StoryList.this);
        d.setMessage("Setting Connection Please Wait...");
        d.setIndeterminate(true);
        d.setCancelable(false);
        d.show();
        final Set<String> ids = new HashSet<String>();
        db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                .document(Manager.ALL_CONNECTIONS_DOCUMENT_IDS)
                .collection(Manager.ALL_CONNECTIONS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                String docID = documentSnapshot.getId();

                                if (!docID.equals(Manager.BASE_CONNECTION_ID)) {
                                    Map<String, Object> temp = documentSnapshot.getData();
                                    String text = temp.get(Manager.CONNECTION_ID)
                                            .toString();
                                    ids.add(text);

                                }
                            }
                            Random rand = new Random();
                            int rand_id = rand.nextInt(90000) + 10000;
                            while (ids.contains(rand_id + ""))
                            {
                                rand_id = rand.nextInt(90000) + 10000;
                            }
                            sessionID = rand_id;
                            Map<String, Object> map = new HashMap<String, Object>() {{
                                put(Manager.IMG_DOCUMENT_FIELD, "");
                            }};
                            db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                                    .document(Manager.CONNECTION_SUB_NAME + sessionID)
                                    .collection(Manager.SUB_CONNECTION_COLLECTION)
                                    .document(Manager.IMG_DOCUMENT_ID)
                                    .set(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {// HERE
                                            Map<String, Object> temp_map = new HashMap<String, Object>() {{
                                                put(Manager.CONNECTION_ID, sessionID);
                                            }};
                                            db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                                                    .document(Manager.ALL_CONNECTIONS_DOCUMENT_IDS)
                                                    .collection(Manager.ALL_CONNECTIONS_COLLECTION )
                                                    .document(Manager.CONNECTION_SUB_NAME + sessionID)
                                                    .set(temp_map)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            d.dismiss();
                                                            showAlertDialog(story_name);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(" ", "Error writing document", e);
                                                        }
                                                    });


                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(" ", "Error writing document", e);
                                        }
                                    });
                        }
                    }
                });


    }

    public void terminateConnection() {
        db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                .document(Manager.CONNECTION_SUB_NAME + sessionID)
                .collection(Manager.SUB_CONNECTION_COLLECTION)
                .document(Manager.IMG_DOCUMENT_ID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection(Manager.MAIN_CONNECTION_COLLECTION)
                        .document(Manager.ALL_CONNECTIONS_DOCUMENT_IDS)
                        .collection(Manager.ALL_CONNECTIONS_COLLECTION)
                        .document(Manager.CONNECTION_SUB_NAME + sessionID)
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                Toast.makeText(StoryList.this, "Data deleted !",
                        Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void showAlertDialog(final String story_name) {
        new AlertDialog.Builder(StoryList.this)
                .setTitle("Starting Story")
                .setMessage("Connection Number " + sessionID)
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(StoryList.this, MainActivity.class);
                        intent.putExtra("key", sessionID + "");
                        intent.putExtra(Manager.STORY_KEY_NAME, story_name);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        terminateConnection();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }
}
