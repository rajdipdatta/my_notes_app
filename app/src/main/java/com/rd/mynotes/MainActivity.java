package com.rd.mynotes;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> notes = new ArrayList<>();

    private RecyclerView recyclerView;
    static NotesAdapter adapter;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey!! Want to take notes? Try this amazing app now.\nLink: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);

            return true;
        }

        else if (item.getItemId() == R.id.rate){
            Toast.makeText(this, "Please wait", Toast.LENGTH_LONG).show();

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));

            return true;
        }

        else if (item.getItemId() == R.id.about_us){
            aboutDialog();

            return true;
        }

        else if (item.getItemId() == R.id.feedback){
            sendFeedbackEmail(MainActivity.this);

            return true;
        }

        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // 1. Find the toolbar from your XML layout
        Toolbar toolbar = findViewById(R.id.toolbar);

        // 2. Tell the activity to use this toolbar as the Action Bar
        setSupportActionBar(toolbar);

        // 3. Now you can safely set title text
        if (getSupportActionBar() != null) {
            //Setting the title
            getSupportActionBar().setTitle("My Notes");
        }

        recyclerView = findViewById(R.id.recyclerView);

        FloatingActionButton fab = findViewById(R.id.fab);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.rd.mynotes", Context.MODE_PRIVATE);
        HashSet<String> set = (HashSet<String>) sharedPreferences.getStringSet("notes", null);

        if (set == null){
            notes.add("This is an Example note");
        }
        else {
            // Set full notes as retrieved from the SharedPref, to use it in Note Editor Screen
            notes = new ArrayList(set);
        }

        // Pass the click logic right into the constructor
        adapter = new NotesAdapter(notes, new NotesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String noteText, int position) {
                // This code runs whenever any item in your list is tapped!

                // Open a new activity to update the note
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteId", position);
                startActivity(intent);
                // startActivityForResult(intent, 101); // 101 is a custom request code
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // add divider between the list items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.list_divider);
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                deleteItem(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();

                // 1. Draw Red Background (Only when swiping left)
                if (dX < 0) {
                    paint.setColor(Color.parseColor("#E53935")); // Premium Material Red
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                    // 2. Draw Trash Can Icon
                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    if (icon != null) {
                        int itemHeight = itemView.getBottom() - itemView.getTop();
                        int iconIntrinsicWidth = icon.getIntrinsicWidth();
                        int iconIntrinsicHeight = icon.getIntrinsicHeight();

                        // Calculate vertical alignment centered to row
                        int iconTop = itemView.getTop() + (itemHeight - iconIntrinsicHeight) / 2;
                        int iconBottom = iconTop + iconIntrinsicHeight;

                        // Calculate horizontal spacing from the right margin
                        int margin = (itemHeight - iconIntrinsicHeight) / 2; // Uniform margin
                        int iconRight = itemView.getRight() - margin;
                        int iconLeft = itemView.getRight() - margin - iconIntrinsicWidth;

                        // Only draw icon if the user has swiped far enough to reveal it
                        if (itemView.getRight() + dX < iconLeft) {
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }
                    }
                }

                // Super class keeps the original item moving seamlessly over our canvas drawings
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "Hey! Tap the screen to start writing.", Toast.LENGTH_LONG).show();
            }
        });

        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        boolean firstStart = preferences.getBoolean("firstStart", true);
        if (firstStart) {
            showStartDialog();
        }
    }

    //for Exit menu Item
    public void exit(MenuItem item){
        Toast.makeText(this, "Thanks for using My Notes", Toast.LENGTH_LONG).show();
        finish();
    }

    private void deleteItem(int i) {
        final int itemToDelete = i;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.warning_icon)
                .setTitle("Are You Sure...")
                .setMessage("Do you want to delete this note...?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                notes.remove(itemToDelete);
                                //notesList.remove(itemToDelete);
                                adapter.notifyItemRemoved(itemToDelete);
                                //arrayAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "Note has been deleted.", Toast.LENGTH_LONG).show();

                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.rd.mynotes", Context.MODE_PRIVATE);
                                HashSet<String> set = new HashSet<>(MainActivity.notes);
                                sharedPreferences.edit().putStringSet("notes", set).apply();
                            }
                        }
                )
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(itemToDelete);
                        dialog.dismiss();
                    }
                });

        // Create the dialog instance
        AlertDialog dialog = builder.create();

        // Prevent dismissal when tapping outside the alert dialog
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }

    private void showStartDialog(){
        new AlertDialog.Builder(this)
                .setTitle("We are Updated!")
                .setMessage("Hello Buddies, We have updated our app." +
                        "\nThis update includes" +
                        "\n * Minor bug fixes" +
                        "\n * Performance enhancements" +
                        "\nPlease rate us on Google Play :)")
                .setPositiveButton("Thank You", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();

        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    private void aboutDialog(){
        new AlertDialog.Builder(this)
                .setTitle("MY NOTES")
                .setMessage(R.string.about_us_text) // getText preserves <b> tags automatically
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    //for sending Feedback email
    public void sendFeedbackEmail(Context context) {
        String developerEmail = "rajdipdatta@gmail.com"; // ✉️ Replace with your real email
        String subject = "My Notes App: Feedback & Support";

        // 1. Dynamically retrieve the App Version Name safely across OS versions
        String versionName = "Unknown";
        try {
            PackageManager pm = context.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
                versionName = pm.getPackageInfo(context.getPackageName(),
                        PackageManager.PackageInfoFlags.of(0)).versionName;
            } else { // Older API versions
                versionName = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 2. Append the dynamic App Version to the device details
        String bodyTemplate = "Hi Raj Initiatives,\n" +
                "Please find below the feedback details regarding My Notes App:\n" +
                "\n\n\n--- Debug Info ---\n" +
                "App Version: " + versionName + "\n" +
                "Device: " + Build.MODEL + "\n" +
                "Android OS: " + Build.VERSION.RELEASE +
                "\n\n\nFeedback:\n";

        // 3. Build and launch the Intent
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{developerEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, bodyTemplate);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send Feedback Via:"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No email applications installed on this device.", Toast.LENGTH_LONG).show();
        }
    }


    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if returning from the update screen successfully
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            String updatedText = data.getStringExtra("UPDATED_NOTE_TEXT");
            int position = data.getIntExtra("NOTE_POSITION", -1);

            // Toast.makeText(this, "Note #" + position + " updated", Toast.LENGTH_SHORT).show();

            if (position != -1 && updatedText != null) {
                // Update data source
                notes.set(position, updatedText);

                // Notify the adapter to refresh that specific item on screen smoothly
                adapter.notifyItemChanged(position);
            }
        }
    }
    */
}