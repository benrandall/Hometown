package dcogburn.hometown;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class ShuffleArtists extends AppCompatActivity implements RateFavoriteDialog.NoticeDialogListener  {

    static Context context;

    // Various display
    private TextView mArtist;
    private TextView mAlbum;
    private String city;
    private ImageView mImage;
    private Drawable defaultImage;

    private static RatingBar ratingBar;

    // Firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    // Buttons
    ImageButton generateAlbum;
    ImageButton favoriteAlbum;
    ImageButton saveAlbum;
    ImageButton playButton;

    // album info
    AlbumInfo thisAlbum;
    private ArrayList<AlbumInfo> albumQueue = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        city = intent.getStringExtra("city");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuffle_artists);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(city);
        ShuffleArtists.context = getApplicationContext();
        setSupportActionBar(toolbar);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);

        // firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // album details listed on screen
        mArtist = (TextView) findViewById(R.id.artist);
        mAlbum = (TextView) findViewById(R.id.album);
        mImage = (ImageView) findViewById(R.id.art);
        defaultImage = getResources().getDrawable(R.drawable.albumcover);
        try {
            generateAlbum();
            displayAlbum();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // new album button
        generateAlbum = (ImageButton) findViewById(R.id.generatealbum);
        generateAlbum.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    try {
                        mImage.setImageDrawable(defaultImage);
                        generateAlbum();
                        displayAlbum();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        createButtons();
    }

    @Override
    public void onDialogPositiveClick(float rating) {
        // User touched the dialog's positive button
        thisAlbum.setRating((int) rating);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        if (user != null) {
            Log.d("SHUFFLE", uid);
        } else {
            // No user is signed in
        }
        Toast.makeText(ShuffleArtists.context, "Album saved to favorites", Toast.LENGTH_LONG).show();
        databaseReference.child("users").child(uid).child("favorites").child(thisAlbum.getAlbumName() + " - " + thisAlbum.getArtistName()).setValue(thisAlbum);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }

    public void createButtons(){
        ImageButton button = (ImageButton) findViewById(R.id.generatealbum);
        favoriteAlbum = (ImageButton) findViewById(R.id.favoriteAlbum);
        saveAlbum = (ImageButton) findViewById(R.id.saveAlbum);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mImage.setImageDrawable(defaultImage);
                    generateAlbum();
                    displayAlbum();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // save album button
        favoriteAlbum = (ImageButton) findViewById(R.id.favoriteAlbum);
        favoriteAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.

                DialogFragment rfg = new RateFavoriteDialog();
                rfg.show(ft, "dialog");

            }
        });

        saveAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlbumInfo album = thisAlbum;
                saveAlbum.setImageResource(R.drawable.ic_action_play_dark);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                if (user != null) {
                    Log.d("SHUFFLE", uid);
                } else {
                    // No user is signed in
                }
                Toast.makeText(ShuffleArtists.context, "Album saved to Saved for Later", Toast.LENGTH_LONG).show();
                databaseReference.child("users").child(uid).child("save").child(album.getAlbumName() + " - " + album.getArtistName()).setValue(album);
            }
        });

        playButton = (ImageButton) findViewById(R.id.playSong);
    }

    public void playSong(View view){
        Spotify spotify = new Spotify();
        spotify.makeURL(thisAlbum);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        city = intent.getStringExtra("city");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(city);
    }

    // display album on page
    public void displayAlbum() {
        thisAlbum = albumQueue.remove(0);
        mArtist.setText(thisAlbum.getArtistName());
        mAlbum.setText(thisAlbum.getAlbumName());
        new AlbumURL().execute(thisAlbum.getAlbumArt());
    }

    // generate new AlbumInfo
    public void generateAlbum() throws IOException, InterruptedException {
        AlbumGenerator gen = new AlbumGenerator();
        AlbumInfo album = null;
        InputStream is = null;

        // get text file of artists
        switch (city.toLowerCase()) {
            case "austin":
                is = getResources().openRawResource(R.raw.austin);
                break;
            case "dallas":
                is = getResources().openRawResource(R.raw.dallas);
                break;
            case "denton":
                is = getResources().openRawResource(R.raw.denton);
                break;
            case "el paso":
                is = getResources().openRawResource(R.raw.el_paso);
                break;
            case "fort worth":
                is = getResources().openRawResource(R.raw.fort_worth);
                break;
            case "houston":
                is = getResources().openRawResource(R.raw.houston);
                break;
            case "lubbock":
                is = getResources().openRawResource(R.raw.lubbock);
                break;
        }

        // generate album
        try {
            album = gen.generateAlbum(city, new Scanner(is));
            albumQueue.add(album);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.d("ALBUM", "artist: " + album.getArtistName());
        Log.d("ALBUM", "album: " + album.getAlbumName());
        Log.d("ALBUM", "art: " + album.getAlbumArt());
    }

    // image request
    class AlbumURL extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... image) {
            URL url = null;
            try {
                url = new URL(image[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            Bitmap bmp = null;
            try {
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            mImage.setImageBitmap(bmp);
        }
    }

}

