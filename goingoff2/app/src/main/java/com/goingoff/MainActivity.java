package com.goingoff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button startLiveStreamButton, signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startLiveStreamButton = findViewById(R.id.startLiveStreamButton);
        signOutButton = findViewById(R.id.signOutButton);

        startLiveStreamButton.setOnClickListener(v -> startLiveStream());
        signOutButton.setOnClickListener(v -> signOut());
    }

    private void startLiveStream() {
        Intent intent = new Intent(MainActivity.this, LiveStreamActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
