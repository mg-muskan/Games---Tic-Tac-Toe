package com.pmuskan.games;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TicTac extends AppCompatActivity {

    private LinearLayout p1Layout, p2Layout;
    private ImageView im1, im2, im3, im4, im5, im6, im7, im8, im9;
    private TextView pt1, pt2;

    // Winning combinations
    private final List<int[]> combination = new ArrayList<>();

    // Done boxes position by users
    private final List<String> doneBox = new ArrayList<>();

    // Player Unique ID
    private String playerUniqueId = "0";

    // Getting firebase database reference from URL
    DatabaseReference dataRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://classic-games-ed300-default-rtdb.firebaseio.com/");

    // True when opponent is found
    private boolean opponentFound = false;

    // Unique ID of opponent
    private String opponentUniqueID = "0";

    // Value must be matching or waiting. when a user create a new connection/room, value is waiting.
    private String status = "matching";

    // Player turn
    String playerTurn = "";

    // Which connection ID player joined
    String connectionID = "";

    // Generating ValueEventListener for firebase database
    // Turns keeping track of the players turn and won of the player who won.
    ValueEventListener turns, won;

    // Selected boxes by player, empty fields will be replaced by the player's id
    private final String[] boxSelected = {"", "", "", "", "", "", "", "", ""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac);

        // Defining all layouts
        p1Layout = findViewById(R.id.pl1Layout);
        p2Layout = findViewById(R.id.pl2Layout);

        // Images
        im1 = findViewById(R.id.im1);
        im2 = findViewById(R.id.im2);
        im3 = findViewById(R.id.im3);
        im4 = findViewById(R.id.im4);
        im5 = findViewById(R.id.im5);
        im6 = findViewById(R.id.im6);
        im7 = findViewById(R.id.im7);
        im8 = findViewById(R.id.im8);
        im9 = findViewById(R.id.im9);

        // Player's names texts
        pt1 = findViewById(R.id.pText1);
        pt2 = findViewById(R.id.pText2);

        // Getting playerName from the PlayerName.class activity
        String getPlayerName = getIntent().getStringExtra("Player");

        // Adding all the possible winning combinations into the list
        combination.add(new int[]{0, 1, 2});
        combination.add(new int[]{3, 4, 5});
        combination.add(new int[]{6, 7, 8});
        combination.add(new int[]{0, 3, 6});
        combination.add(new int[]{1, 4, 7});
        combination.add(new int[]{2, 5, 8});
        combination.add(new int[]{0, 4, 8});
        combination.add(new int[]{2, 4, 6});

        // Progress Dialog to let the user know to wait for the opponent
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Waiting for opponent.");
        progressDialog.show();

        // Generating the unique id to the players to be identified by it.
        playerUniqueId = String.valueOf(System.currentTimeMillis());

        // Giving input the name of the player to the textView
        pt1.setText(getPlayerName);

        dataRef.child("Connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Check if the opponent is present, if not then find.
                if(opponentFound) {

                    // Checking if others are present in firebase realtime database
                    if(snapshot.hasChildren()) {

                        // Checking all connections if the other users are also waiting to match
                        for(DataSnapshot conn: snapshot.getChildren()) {

                            // Getting connection unique ID
                            String conId = (conn.getKey());

                            // 2 players are required to play the game
                            // If count is 1 then the player is waiting for the opponent
                            // else the connection is completed with 2 players
                            int getPlayersCount = (int)conn.getChildrenCount();

                            if(status.equals("waiting")) {
                                if(getPlayersCount == 2) {
                                    playerTurn = playerUniqueId;
                                    Turn(playerTurn);

                                    // False until found one
                                    boolean playerFound = false;

                                    // Getting players in connection
                                    for(DataSnapshot players: conn.getChildren()) {
                                        String getPlayerUniqueID = players.getKey();

                                        // If player ID match with user (who created this connection)
                                        if(getPlayerUniqueID.equals(playerUniqueId)) {
                                            playerFound = true;
                                        }
                                        else if(playerFound) {
                                            String getOpponentName = players.child("Player Name").getValue(String.class);
                                            opponentUniqueID = players.getKey();

                                            // Set opponent name to the textView
                                            pt2.setText(getOpponentName);

                                            // Assigning connection ID
                                            connectionID = conId;
                                            opponentFound = true;

                                            // Adding turns and won event listener
                                            dataRef.child("Turn").child(connectionID).addValueEventListener(turns);
                                            dataRef.child("Won").child(connectionID).addValueEventListener(won);

                                            // After player is found
                                            if(progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }

                                            // Once the connection has established, remove connectionListener dataRef
                                            dataRef.child("Connections").removeEventListener(this);

                                        }
                                    }
                                }
                            }

                            // In case user has not created connection b'coz other are already available to join
                            else {
                                if(getPlayersCount == 1) {
                                    // Add player to the connection
                                    conn.child(playerUniqueId).child("Player Name").getRef().setValue(getPlayerName);

                                    // Getting both players
                                    for(DataSnapshot players: conn.getChildren()) {
                                        String getOpName = players.child("Player Name").getValue(String.class);
                                        opponentUniqueID = players.getKey();

                                        // First turn will be of who created the connection
                                        playerTurn = opponentUniqueID;

                                        Turn(playerTurn);

                                        // Setting player name tot eh textView
                                        pt2.setText(getOpName);

                                        // Assigning connection ID
                                        connectionID = conId;
                                        opponentFound = true;

                                        // Adding turns and won event listener
                                        dataRef.child("Turn").child(connectionID).addValueEventListener(turns);
                                        dataRef.child("Won").child(connectionID).addValueEventListener(won);
                                    }
                                }
                            }

                        }

                        // If opponent is not found and user is not waiting anymore, create a new room
                        if(!opponentFound && status.equals("waiting")) {

                            // Generating unique ID for the connection
                            String connectionUID = String.valueOf(System.currentTimeMillis());

                            // Adding first player and waiting for other
                            snapshot.child(connectionUID).child(playerUniqueId).child("Player Name").getRef().setValue(getPlayerName);

                            status = "waiting";

                        }
                    }

                    // If there is no connection available in the firebase then create a new connection
                    // like creating a room and waiting for other players to join the room
                    else {

                        // Generating unique ID for the connection
                        String connectionUID = String.valueOf(System.currentTimeMillis());

                        // Adding first player and waiting for other
                        snapshot.child(connectionUID).child(playerUniqueId).child("Player Name").getRef().setValue(getPlayerName);

                        status = "waiting";

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        turns = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Getting all turns of the connection
                for(DataSnapshot snap: snapshot.getChildren()) {

                    if(snap.getChildrenCount() == 2) {

                        // Getting box position selected by the user
                        final int boxPosition = Integer.parseInt(snap.child("Box Position").getValue(String.class));

                        // Getting player ID who selected the box
                        final String getPlayerID = snap.child("Player ID").getValue(String.class);

                        // Checking if thr user has not selected the box before
                        if(doneBox.contains(String.valueOf(boxPosition))) { // True skip, false enter

                            // Select the box
                            doneBox.add(String.valueOf(boxPosition));

                            if(boxPosition == 1) {
                                SelectBox(im1, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 2) {
                                SelectBox(im2, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 3) {
                                SelectBox(im3, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 4) {
                                SelectBox(im4, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 5) {
                                SelectBox(im5, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 6) {
                                SelectBox(im6, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 7) {
                                SelectBox(im7, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 8) {
                                SelectBox(im8, boxPosition, getPlayerID);
                            }
                            else if(boxPosition == 9) {
                                SelectBox(im9, boxPosition, getPlayerID);
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        won  = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // If user has won the game
                if(snapshot.hasChild("Player ID")) {
                    String getWinnerID = snapshot.child("Player ID").getValue(String.class);

                    final WinDialog winDialog;

                    if(getWinnerID.equals(playerUniqueId)) {

                        // Show the message
                        winDialog = new WinDialog(TicTac.this, "You Won!");

                    }
                    else {
                        winDialog = new WinDialog(TicTac.this, "You Lose.");
                    }

                    winDialog.setCancelable(false);
                    winDialog.show();

                    // Remove listeners from database
                    dataRef.child("Turn").child(connectionID).removeEventListener(turns);
                    dataRef.child("Won").child(connectionID).removeEventListener(won);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        im1.setOnClickListener(v -> {

            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("1");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }

        });

        im1.setOnClickListener(v -> {

            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("1");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }

        });

        im2.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("2");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

        im3.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("3");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

        im4.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("4");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

        im5.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("5");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

        im6.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("6");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

        im7.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("7");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

        im8.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("8");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

        im9.setOnClickListener(v -> {
            // If the box has not already been selected and players the one created the room
            if(!doneBox.contains("1") && playerTurn.equals(playerUniqueId)) {
                ((ImageView) v).setImageResource(R.drawable.cross_svgrepo_com);

                // Send selected box position and user's id
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Box Position").setValue("9");
                dataRef.child("Turn").child(connectionID).child(String.valueOf(doneBox.size() + 1)).child("Player ID")
                        .setValue(playerUniqueId);

                // Change player turn
                playerTurn = opponentUniqueID;
            }
        });

    }

    private void Turn(String turn) {

        if(turn.equals(playerUniqueId)) {
            p1Layout.setBackgroundResource(R.drawable.round_dark);
            p2Layout.setBackgroundResource(R.drawable.round);
        }
        else {
            p2Layout.setBackgroundResource(R.drawable.round_dark);
            p1Layout.setBackgroundResource(R.drawable.round);
        }

    }

    private void SelectBox(ImageView image, int selectedBoxPosition, String selectedByPlayer) {

        boxSelected[selectedBoxPosition - 1] = selectedByPlayer;

        if(selectedByPlayer.equals(playerUniqueId)) {
            image.setImageResource(R.drawable.cross_svgrepo_com);
            playerTurn = opponentUniqueID;
        }
        else {
            image.setImageResource(R.drawable.circle_svgrepo_com);
            playerTurn = playerUniqueId;
        }

        Turn(playerTurn);

        // If player has won
        if(checkPlayerWin(selectedByPlayer)) {
            // Sending won player ID to the firebase for the opponent to be notified
            dataRef.child("Won").child(connectionID).child("Player ID").setValue(selectedByPlayer);
        }

        // Over the game if no boxes left to be selected
        if(doneBox.size() == 9) {

            final WinDialog Win = new WinDialog(TicTac.this, "It ia a Draw!");
            Win.setCancelable(false);
            Win.show();

        }

    }

    private boolean checkPlayerWin(String ID) {

        boolean playerWon = false;

        // Compare player turns with every combinations
        for(int i = 0; i < combination.size(); i++) {

            final int[] combo = combination.get(i);

            // Checking last three turns by user
            if(boxSelected[combo[0]].equals(ID) && boxSelected[combo[1]].equals(ID) && boxSelected[combo[2]].equals(ID)) {
                playerWon = true;
            }
        }

        return playerWon;
    }

}