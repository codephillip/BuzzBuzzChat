package com.devfest.mbale18.buzzbuzzchat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseFirestore db;
    private static final String CHAT = "chat";
    private static final String NAME = "name";
    private static final String MESSAGE = "message";
    private TextView messageTextView;
    private EditText inputMessageEditView;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageTextView = findViewById(R.id.message);
        inputMessageEditView = findViewById(R.id.input_messsage);
        sendButton = findViewById(R.id.send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = inputMessageEditView.getText().toString();
                Log.d(TAG, "onClick: " + message);
                saveDocumentData(message);
            }
        });

        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        db.collection(CHAT)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        displayMessagesFromServer(queryDocumentSnapshots);
                    }
                });
    }

    private void displayMessagesFromServer(@Nullable QuerySnapshot queryDocumentSnapshots) {
        Log.d(TAG, "displayMessagesFromServer: started");
        String messages = "";
        List<Chat> chats = queryDocumentSnapshots.toObjects(Chat.class);

        for (Chat chat : chats) {
            if (chat != null) {
                messages += "\n\n#\n" + chat.getName() + " : " + chat.getMessage();
                Log.d(TAG, "Current data: " + messages);
            } else {
                Log.d(TAG, "Current data: null");
            }
        }
        messageTextView.setText(messages);
    }

    private void saveDocumentData(String message) {
        Log.d(TAG, "saveDocumentData: started");
        // TODO integrate authentication and get username
        Chat chat = new Chat("Sharon", message);

        db.collection(CHAT)
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void readDocumentData() {
        Log.d(TAG, "readDocumentData: started");
        db.collection(CHAT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            displayMessagesFromServer(task.getResult());
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
