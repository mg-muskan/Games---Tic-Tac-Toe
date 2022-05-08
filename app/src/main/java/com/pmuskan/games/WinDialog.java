package com.pmuskan.games;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WinDialog extends Dialog {

    String message;
    TicTac tic;

    public WinDialog(@NonNull Context context, String message) {
        super(context);
        this.message = message;
        this.tic = (TicTac)context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_dialog);

        final TextView msg = findViewById(R.id.draw);
        final Button start = findViewById(R.id.playAgain);

        start.setOnClickListener(view -> {
            dismiss();
            getContext().startActivity(new Intent(getContext(), PlayerName.class));
            tic.finish();
        });

    }
}