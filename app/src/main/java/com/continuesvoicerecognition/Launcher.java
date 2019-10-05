package com.continuesvoicerecognition;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.firestore.FirebaseFirestore;

public class Launcher extends AppCompatActivity implements View.OnClickListener {

    FirebaseFirestore db;
    ImageView read, view, play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_launcher);

        read = (ImageView) findViewById(R.id.imageView_Reader);
        read.setImageResource(R.drawable.read);
        view = (ImageView) findViewById(R.id.imageView_Viewer);
        view.setImageResource(R.drawable.viewer);
        play = (ImageView) findViewById(R.id.imageView_Listen);
        play.setImageResource(R.drawable.play);

        db = FirebaseFirestore.getInstance();

        read.setOnClickListener(this);
        view.setOnClickListener(this);
        play.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_Reader:
                Intent intent = new Intent(Launcher.this, StoryList.class);
                intent.putExtra(Manager.PLAY_VIEW_KEY, Manager.READ_STORY);
                startActivity(intent);
                break;

            case R.id.imageView_Viewer:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Launcher.this);
                final EditText input = new EditText(Launcher.this);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setTitle("Starting A Connection")
                        .setMessage("Enter The Connection Code")
                        .setPositiveButton("GO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String insertedID = input.getText().toString();
                                Intent intent = new Intent(Launcher.this,
                                        Viewing.class);
                                intent.putExtra("key", insertedID);
                                startActivity(intent);

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(false)
                        .show();
                break;

            case R.id.imageView_Listen:
                Intent intent1 = new Intent(Launcher.this, StoryList.class);
                intent1.putExtra(Manager.PLAY_VIEW_KEY, Manager.PLAY_AUDIO);
                startActivity(intent1);
                break;

        }
    }
}


