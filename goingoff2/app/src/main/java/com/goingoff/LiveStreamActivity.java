package com.goingoff;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LiveStreamActivity extends AppCompatActivity {

    private EditText streamTitleEditText;
    private Switch privateStreamSwitch;
    private Button startStreamButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

        streamTitleEditText = findViewById(R.id.streamTitleEditText);
        privateStreamSwitch = findViewById(R.id.privateStreamSwitch);
        startStreamButton = findViewById(R.id.startStreamButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        startStreamButton.setOnClickListener(v -> startStream());
    }

    private void startStream() {
        String streamTitle = streamTitleEditText.getText().toString();
        boolean isPrivate = privateStreamSwitch.isChecked();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference streamRef = mDatabase.child("streams").push();
            streamRef.setValue(new LiveStream(userId, streamTitle, isPrivate))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LiveStreamActivity.this, "Live stream started", Toast.LENGTH_SHORT).show();
                            // Add code to start streaming using your preferred method/library
                        } else {
                            Toast.makeText(LiveStreamActivity.this, "Failed to start stream", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    public static class LiveStream {
        public String userId;
        public String title;
        public boolean isPrivate;

        public LiveStream() {}

        public LiveStream(String userId, String title, boolean isPrivate) {
            this.userId = userId;
            this.title = title;
            this.isPrivate = isPrivate;
        }
    }
}
