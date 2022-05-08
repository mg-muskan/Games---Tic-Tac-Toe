package com.pmuskan.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        EditText name = findViewById(R.id.name);
        Button start = findViewById(R.id.start);

        start.setOnClickListener(view -> {

            // Getting player name from editText to string variable
            String getPlayerN = name.getText().toString();

            // If empty, print this msg
            if(getPlayerN.isEmpty()) {
                Toast.makeText(PlayerName.this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            }
            else {
                // Moving to the next (TicTac) activity
                Intent i = new Intent(PlayerName.this, TicTac.class);

                // Along with the player
                i.putExtra("Player", getPlayerN);

                // Opening the next activity
                startActivity(i);

                // Destroy this PlayerName activity
                finish();
            }
        });
    }
}