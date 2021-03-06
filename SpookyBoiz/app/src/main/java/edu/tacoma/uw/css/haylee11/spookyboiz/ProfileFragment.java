package edu.tacoma.uw.css.haylee11.spookyboiz;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import edu.tacoma.uw.css.haylee11.spookyboiz.Profile.Profile;

import static android.app.Activity.RESULT_OK;

/**
 * Builds the current user's profile for viewing
 *
 * @author Haylee Ryan, Matthew Frazier, Kai Stansfield
 */
public class ProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBERfa
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    /**
     * URL for gathering information on current user for profile
     */
    private static final String PROFILE_URL = "http://spookyscarysightings.000webhostapp.com/userProfile.php?cmd=profile";

    /**
     * URL used to change the current user's favorite monster
     */
    private static final String FAV_URL = "http://spookyscarysightings.000webhostapp.com/userProfile.php?cmd=fav&current=";

    /**
     * URL used to change the current user's bio
     */
    private static final String BIO_URL = "http://spookyscarysightings.000webhostapp.com/userProfile.php?cmd=bio&current=";

    private String mParam1;
    private String mParam2;

    private TextView mUsername;
    private TextView mName;
    private TextView mSightings;
    private TextView mFavorite;
    private TextView mBio;
    private String mURL;

    private boolean mImageFlag;

    private ImageButton mPic;
    private Bitmap mImage;


    private Profile mProfile;

    SharedPreferences mSharedPref;


    private OnFragmentInteractionListener mListener;

    private View mView;

    private static String mChangeFav;
    private static String mChangeBio;

    /**
     * Required empty constructor
     */
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * When the fragment is created, this method instantiates it
     * @param savedInstanceState The saved instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * When the fragment is create, this instantiates the view. Also instantiates the
     * RecyclerView and calls the AsyncTask
     * @param inflater The layout inflater
     * @param container The container the fragment is in
     * @param savedInstanceState The saved instance state
     * @return The view to be presented
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        getActivity().setTitle("My Profile");

        mImageFlag = false;
        mSharedPref = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS),
                Context.MODE_PRIVATE);

        mUsername = (TextView) v.findViewById(R.id.username_text);
        mName = (TextView) v.findViewById(R.id.name);
        mSightings = (TextView) v.findViewById(R.id.sightings);
        mFavorite = (TextView) v.findViewById(R.id.favorite);
        mBio = (TextView) v.findViewById(R.id.bio);
        mPic = (ImageButton) v.findViewById(R.id.profile_pic);

        getActivity().setTitle("Profile");

        String url = buildProfileURL(v, "profile");
        mListener.profileView(url);

        mUsername.setText(mSharedPref.getString(getString(R.string.CURRENT_USER), "user"));
        mName.setText(mSharedPref.getString(getString(R.string.NAME), "first last"));
        mSightings.setText(Integer.toString(mSharedPref.getInt(getString(R.string.SIGHTINGS), 0)));
        mFavorite.setText(mSharedPref.getString(getString(R.string.FAVORITE), "None"));
        mBio.setText(mSharedPref.getString(getString(R.string.BIO), "None"));
        mURL = mSharedPref.getString(getString(R.string.URL), "https://spookyscarysightings.000webhostapp.com/images/no_image.png");

        //Set up buttons for changing attributes (opens dialog fragment)
        mPic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);



            }
        });

        Button fav = (Button) v.findViewById(R.id.fav_change);
        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeDialogFragment newFragment = new ChangeDialogFragment(1, ProfileFragment.this);
                newFragment.show(getActivity().getSupportFragmentManager(), "fav_change");
            }
        });


        Button bio = (Button) v.findViewById(R.id.bio_change);
        bio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeDialogFragment newFragment = new ChangeDialogFragment(2, ProfileFragment.this);
                newFragment.show(getActivity().getSupportFragmentManager(), "bio_change");
            }
        });


        mView = v;
        return v;
    }



    /**
     * Builds URl to either get profile, change favorite monster, or change bio
     * @param v The current view
     * @param action Action to take place
     * @return The URL
     */
    private String buildProfileURL(View v, String action) {
        StringBuilder sb;
        String user = mSharedPref.getString(getString(R.string.CURRENT_USER), null);
        if (action.equals("profile")) { //If we are getting profile info
            sb = new StringBuilder(PROFILE_URL);
            try {
                sb.append("&username=");
                sb.append(URLEncoder.encode(user, "UTF-8"));

            } catch(Exception e) {
                Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        } else if (action.equals("profile_image")) { //If we are getting profile info
            sb = new StringBuilder(PROFILE_URL);
            try {
                sb.append("_image&username=");
                sb.append(URLEncoder.encode(user, "UTF-8"));

            } catch(Exception e) {
                Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        } else if (action.equals("fav")) { //If we are changing favorite
            sb = new StringBuilder(FAV_URL);
            try {
                sb.append(URLEncoder.encode(user, "UTF-8"));
                sb.append("&favorite=");
                sb.append(URLEncoder.encode(mChangeFav, "UTF-8"));

            } catch(Exception e) {
                Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        } else {                        //If we are changing bio
            sb = new StringBuilder(BIO_URL);
            try {
                sb.append(URLEncoder.encode(user, "UTF-8"));
                sb.append("&bio=");
                sb.append(URLEncoder.encode(mChangeBio, "UTF-8"));

            } catch(Exception e) {
                Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }
        return sb.toString();
    }

    /**
     * Processes requests for changing a profile picture.
     *
     * @param RQC Request ode for action.
     * @param RC A returned code for if the request went though
     * @param data Intent Holds access to the data.
     */
    @Override
    public void onActivityResult(int RQC, int RC, Intent data) {
        super.onActivityResult(RQC, RC, data);

        if (RQC == 1 && RC == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                mImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                mPic.setImageBitmap(mImage);
                mImageFlag = true;

                String url = buildProfileURL(mView, "profile_image");
                mListener.profileImage(url, mImage);

            } catch (IOException e) {
                Log.d("Tag", Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        DownloadAsync asyync = new DownloadAsync();
        asyync.execute();

        if (mImageFlag) {
            String url2 = buildProfileURL(mView, "profile_image");
            mListener.profileImage(url2, mImage);
            mImageFlag = false;
        }
    }

    /**
     * This method will push the current profile picture to the database, so it may be used when
     * returning to the app, and for others to see.
     */
    @Override
    public void onPause() {
        super.onPause();
    }
    /**
     * When the fragment is attached to the app, this instantiates the listener
     * @param context The context the fragment is in
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Handles when the fragment is detached, nullifying the listener
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * Dialog pop up that allows user to enter new attribute which is then changed in profile
     * and in database
     *
     * @author Haylee Ryan, Matthew Frazier, Kai Stansfield
     */
    public static class ChangeDialogFragment extends DialogFragment {

        int mChange;

        ProfileFragment mProf;

        /**
         * Constructs a new Profile Fragment to use for method calls
         */
        public ChangeDialogFragment() {
            mProf = new ProfileFragment();
        }

        /**
         * Constructs new local Profile Fragment with the given fragment
         * @param change Flag determining what we are changing
         * @param prof Instance of Profile Fragment to set up
         */
        @SuppressLint("ValidFragment")
        public ChangeDialogFragment(int change, ProfileFragment prof) {
            mChange = change;
            mProf = prof;
        }

        /**
         * When the button is pressed to create the dialog, this method is
         * called to instantiate the dialog and it's contents.
         * @param savedInstanceState The saved instance
         * @return The Dialog box to pop up
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final EditText input = new EditText(this.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
//                    AlertDialog alert = builder.create();
            //About message
            if (mChange == 0) {
                builder.setMessage("Enter new username:");
            } else if (mChange == 1) {
                builder.setMessage("Enter new favorite monster:");
            } else {
                builder.setMessage("Enter new bio:");
            }
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (mChange == 1) { //If we are changing the favorite monster
                                mChangeFav = input.getText().toString();
                                String url = mProf.buildProfileURL(mProf.mView, "fav");
                                mProf.mSharedPref
                                        .edit()
                                        .putString(getString(R.string.FAVORITE), mChangeFav)
                                        .apply();
                                mProf.mListener.profileView(url);

                                mProf.mFavorite.setText(mProf.mSharedPref.getString(getString(R.string.FAVORITE), "error"));
                            } else {    //if we are changing the bio
                                mChangeBio = input.getText().toString();
                                String url = mProf.buildProfileURL(mProf.mView, "bio");
                                mProf.mSharedPref
                                        .edit()
                                        .putString(getString(R.string.BIO), mChangeBio)
                                        .apply();
                                mProf.mListener.profileView(url);

                                mProf.mBio.setText(mProf.mSharedPref.getString(getString(R.string.BIO), "error"));
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });



            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    /**
     * Class that uploads photos to the database
     *
     * @author Haylee Ryan, Kai Stansfield, Matt Frazier
     */
    private class UploadTask extends AsyncTask<String, Void, String> {

        /**
         * Sends a POST request to the database in order to save the profile picture
         * @param urls The URL
         * @return A String
         */
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to download the list of courses, Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }
    }

    /**
     * Takes the url and downloads the image from the file system in order to display the image.
     *
     * @author Haylee Ryan, Matt Frazier, Kai Stansfield
     */
    class DownloadAsync extends AsyncTask<Void,Void,String> {

        private Bitmap bmp;

        @Override
        protected String doInBackground(Void... params) {
            try {
                InputStream in = new URL(mURL).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.d("Tag", Log.getStackTraceString(e));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String string1) {
            if (bmp != null)
                mPic.setImageBitmap(bmp);
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void profileView(String url);
        void profileImage(String url, Bitmap image);
    }
}
