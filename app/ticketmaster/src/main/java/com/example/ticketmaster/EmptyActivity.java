package com.example.ticketmaster;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * An empty activity to host details fragment
 */
public class EmptyActivity extends AppCompatActivity {

    /**
     * Create empty activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        Bundle dataToPass = getIntent().getExtras();

        DetailsFragment dFragment = new DetailsFragment();
        dFragment.setArguments( dataToPass );
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentLocation, dFragment)
                .commit();
    }

}

