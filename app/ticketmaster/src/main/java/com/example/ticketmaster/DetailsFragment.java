package com.example.ticketmaster;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.Activity.RESULT_CANCELED;

/**
 * Create an instance of this fragment
 */
public class DetailsFragment extends Fragment {

    private Bundle dataFromActivity;
    private AppCompatActivity parentActivity;
    ImageView iv_imgUrl;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container This is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState This fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dataFromActivity = getArguments();
        View result = inflater.inflate(R.layout.fragment_details, container, false);

        TextView tv_eventName = result.findViewById(R.id.eventName);
        TextView tv_minPrice = result.findViewById(R.id.minPrice);
        TextView tv_maxPrice = result.findViewById(R.id.maxPrice);
        TextView tv_url = result.findViewById(R.id.url);
        TextView tv_startDate = result.findViewById(R.id.startDate);
        iv_imgUrl = result.findViewById(R.id.eventImage);

        Ticket ticket = new Ticket(dataFromActivity.getLong("id"),
                dataFromActivity.getString("eventName"),
                dataFromActivity.getString("startDate"),
                dataFromActivity.getString("minPrice"),
                dataFromActivity.getString("maxPrice"),
                dataFromActivity.getString("url"),
                dataFromActivity.getString("imgUrl"));

        tv_eventName.setText(ticket.getEventName());
        tv_minPrice.setText(ticket.getMinPrice());
        tv_maxPrice.setText(ticket.getMaxPrice());
        tv_startDate.setText(ticket.getStartDate());
        tv_url.setText(ticket.getUrl());
        Image img =new Image();
        img.execute(ticket.getImgUrl());

        Boolean isFavourite = dataFromActivity.getBoolean("isFavourite",false);

        Button saveButton = result.findViewById(R.id.resultPageButton);
        if(isFavourite) {
            saveButton.setText(getResources().getString(R.string.delete));
        }else {
            saveButton.setText(getResources().getString(R.string.save));
        }

        saveButton.setOnClickListener(click -> {
            if(isFavourite) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parentActivity);
                String msg = getResources().getString(R.string.deleteFavorite);
                alertDialogBuilder.setTitle(R.string.alerttitle)
                        .setMessage(msg)
                        .setPositiveButton("Yes",(clickButton, arg) -> {
                            DeleteTicket(ticket);
                            parentActivity.setResult(Activity.RESULT_OK);
                            parentActivity.finish();
                        })
                        .setNegativeButton("No", (clickButton, arg) -> {parentActivity.setResult(RESULT_CANCELED);  });
                alertDialogBuilder.create().show();
            }else {
                long rowId = InsertTicket(ticket);
                Context context = parentActivity.getApplicationContext();
                Toast toast = Toast.makeText(context, getResources().getString(R.string.saved_message)+" rowid = "+rowId, Toast.LENGTH_LONG );
                toast.show();
            }
        });

        return result;
    }

    private class Image extends AsyncTask<String, Void, Bitmap> {

        /**
         * Override this method to perform a computation on a background thread.
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            return getBitmapFromURL(url);
        }

        /**
         * Runs on the UI thread after doInBackground.
         * @param result The result of the operation computed by doInBackground.
         */
        @Override
        protected void onPostExecute(Bitmap result) {
            iv_imgUrl.setImageBitmap (result);
            super.onPostExecute(result);
        }

    }

    /**
     * handle the event's image url to bitmap
     * @param imageUrl image url
     * @return The bitmap of event
     */
    private static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete ticket from database
     * @param ticket The ticket will be delete
     */
    public void DeleteTicket(Ticket ticket)
    {
        MyOpener dbOpener = new MyOpener(parentActivity);
        SQLiteDatabase db = dbOpener.getWritableDatabase();
        db.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + "= ?", new String[] {ticket.getId()+""});
        db.close();
    }

    /**
     * Insert ticket from database
     * @param ticket The ticket will be insert
     * @return Row id of the ticket in database
     */
    protected long InsertTicket(Ticket ticket)
    {
        MyOpener dbOpener = new MyOpener(parentActivity);
        SQLiteDatabase db = dbOpener.getWritableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(MyOpener.COL_EVENT_NAME, ticket.getEventName());
        newValues.put(MyOpener.COL_START_DATE, ticket.getStartDate());
        newValues.put(MyOpener.COL_PRICE_MAX, ticket.getMaxPrice());
        newValues.put(MyOpener.COL_PRICE_MIN, ticket.getMinPrice());
        newValues.put(MyOpener.COL_URL, ticket.getUrl());
        newValues.put(MyOpener.COL_IMG_URL, ticket.getImgUrl());
        long rowId = db.insert(MyOpener.TABLE_NAME, null,newValues);
        db.close();
        return rowId;
    }

    /**
     * Called when a fragment is first attached to its context. onCreate() will be called after this.
     * @param context Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentActivity = (AppCompatActivity) context;
    }

}
