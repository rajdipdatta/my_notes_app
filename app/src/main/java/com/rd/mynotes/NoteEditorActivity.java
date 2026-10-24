package com.rd.mynotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashSet;

public class NoteEditorActivity extends AppCompatActivity {
    int noteId;
    LinedEditText editText;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.share_menu){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey!! Want to take notes? Try this amazing app now. Link: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);

            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_note_editor);

        // 1. Find the toolbar from your XML layout
        Toolbar toolbar = findViewById(R.id.toolbar);

        // 2. Tell the activity to use this toolbar as the Action Bar
        setSupportActionBar(toolbar);

        // 3. Now you can safely configure the back button
        if (getSupportActionBar() != null) {
            //enable back button to main activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            //Setting the title
            getSupportActionBar().setTitle("Compose");
        }
        //Implement menu Inflater


        editText = (LinedEditText) findViewById(R.id.lined_edit_text);
        // 1. Change text configuration dynamically
        editText.setPaperTextColor(Color.BLACK); // Standard android.graphics.Color
        editText.setPaperTextSize(18.0f);        // Set text scale to 18sp

        // 2. Change page line aesthetics dynamically
        editText.setLineColor(0xFFE0E0E0);       // Solid light grey lines
        editText.setMarginLineColor(0xFFFF8A80); // Bright pastel pink margin line

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId", -1);

        if (noteId != -1){

            editText.setText(MainActivity.notes.get(noteId));

        }
        /*else{
            MainActivity.notes.add("");
            noteId = MainActivity.notes.size() - 1;
        }*/

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = String.valueOf(charSequence).trim();
                if(!text.isEmpty()) {
                    if(noteId == -1) { // for new note
                        MainActivity.notes.add("");
                        noteId = MainActivity.notes.size() - 1;
                    }
                    MainActivity.notes.set(noteId, String.valueOf(charSequence).trim());
                    // MainActivity.adapter.notifyDataSetChanged();

                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.rd.mynotes", Context.MODE_PRIVATE);

                    HashSet<String> set = new HashSet<>(MainActivity.notes);

                    sharedPreferences.edit().putStringSet("notes", set).apply();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                MainActivity.adapter.notifyDataSetChanged();
            }
        });

        // 1. Create the back press callback
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // 2. Put your code on back press logic here
                // sendDataBackToMainActivity();;

                // If you want to fall back to the system default back action inside here:
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        };

        // 3. Register the callback with the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // sendDataBackToMainActivity();
        getOnBackPressedDispatcher().onBackPressed(); // for newer API levels
        return true;
    }

    /*
    @Override
    public void onBackPressed() {
        sendDataBackToMainActivity();
        super.onBackPressed();
    }
    */

    // Helper method to encapsulate data delivery logic
    private void sendDataBackToMainActivity() {
        String updatedText = editText.getText().toString().trim();

        Intent dataIntent = new Intent();
        dataIntent.putExtra("UPDATED_NOTE_TEXT", updatedText);
        dataIntent.putExtra("NOTE_POSITION", noteId);

        // Set result code to OK and pass data framework back to MainActivity's onActivityResult
        setResult(RESULT_OK, dataIntent);

        // Terminate activity instance layout execution safely
        finish();
    }
}