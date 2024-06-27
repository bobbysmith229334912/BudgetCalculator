
package com.example.thespot102;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        Button streamButton = findViewById(R.id.streamButton);
        streamButton.setOnClickListener(v -> startActivity(new Intent(this, StreamingActivity.class)));

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        Button groupButton = findViewById(R.id.groupButton);
        groupButton.setOnClickListener(v -> startActivity(new Intent(this, GroupActivity.class)));

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
