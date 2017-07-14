package com.example.android.getmustop;

// Generic Android libraries
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.DownloadManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//deezer libraries
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

//Youtube
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

//spotify libraries
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;

import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

//Json libraries
import org.json.JSONException;
import org.json.JSONObject;

//Custom Initialised Listener
import static com.example.android.getmustop.R.id.stopbutton;

public class MainActivity extends YouTubeBaseActivity implements AdapterView.OnItemSelectedListener, Player.NotificationCallback, ConnectionStateCallback  //AdapterView.OnItemSelectedListener
{

    private WebView webview;
    private static final String TAG = "Main";
    private ProgressDialog progressBar;


    private TabHost tabHost;
    private SQLiteDatabase dataBase;
    String applicationID = "231602";
    public DeezerConnect deezerConnect;
    public String url;
    public AlbumPlayer albumPlayer1;

    private ListView mListView;
    ListView istoriko;
    List<Istoriko> istorikoList;
    DBHandler db = new DBHandler(this);
    String playlist = "";

    YouTubePlayer mYoutubePlayer;
    private YouTubePlayerView youtubeplayerview;
    private YouTubePlayer.OnInitializedListener onInitializedListener;
    Button stop;
    Button lipsi;
    Spinner spinnerSiteLists;


    private ProgressDialog pDialog;


    // The set of Deezer Permissions needed by the app
    String[] permissions = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.MANAGE_LIBRARY,
            Permissions.LISTENING_HISTORY};

    public static final String API_KEY = "a8cf789881fb40869519552f1c811d02";// YOUTUBE API KEY
    private static final String SPOTIFY_ID = "cb0f2690a5fe406581b038071e4992f7"; //"7ec367e8dffe424da7d87264416fe98c";
    private static final String REDIRECT_URI = "getmustop://callback";
    private SpotifyPlayer spotPlayer;

    private static final String TEST_SONG_MONO_URI = "spotify:track:1FqY3uJypma5wkYw66QOUi";
    @SuppressWarnings("SpellCheckingInspection")
    private static final int REQUEST_CODE = 1337;
    private BroadcastReceiver mNetworkStateReceiver;

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Toast.makeText(MainActivity.this, "OK!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Error error) {
            Toast.makeText(MainActivity.this, "ERROR" + error, Toast.LENGTH_SHORT).show();
        }
    };

    DialogListener listener = new DialogListener() {

        public void onComplete(Bundle values) {
        }

        public void onCancel() {
        }

        public void onException(Exception e) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        spinnerSiteLists = (Spinner) findViewById(R.id.spinner_site_lists);


        DeezerConnect deezerConnect = new DeezerConnect(this, applicationID);
        deezerConnect.authorize(MainActivity.this, permissions, listener);

        youtubeplayerview = (YouTubePlayerView) findViewById(R.id.youtube_view);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    youTubePlayer.cuePlaylist(playlist);
                    //Save reference of initialized player in class level attribute
                    mYoutubePlayer = youTubePlayer;
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }

        };
        youtubeplayerview.initialize(API_KEY, onInitializedListener);
        youtubeplayerview.setVisibility(View.INVISIBLE);

        stop = (Button) findViewById(stopbutton);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mYoutubePlayer != null) {
                    mYoutubePlayer.pause();
                }
                if (albumPlayer1 != null) {
                    albumPlayer1.stop();
                    albumPlayer1.release();
                }
                stopPlaying();

            }
        });


        lipsi = (Button) findViewById(R.id.button_get_list);
        lipsi.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);
        lipsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String btntext = spinnerSiteLists.getSelectedItem().toString();
                Uri uri = Uri.parse(""); //Initialise uri
                final String param = "http://www.youtubeinmp3.com/fetch/?format=JSON&video=" + url;
                Log.d("param: ", param);

                String mtitle = null;
                if (btntext.equals("Top 100 Greek songs")) {
                    uri = Uri.parse(param); // missing 'http://' will cause crash
                    String url1 = "http://www.youtubeinmp3.com/fetch/?format=JSON&video=" + url;
                    HttpHandler sh = new HttpHandler();
                    Log.d("uri", url1);
                    String jsonStr = sh.makeServiceCall(url1);
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        String mJsonObject = jsonObj.getString("link");
                        mtitle = jsonObj.getString("title");

                        Log.d("OutPut", mJsonObject.toString());
                        Log.d("tittle", mtitle.toString());

                        Uri finalurlparse = uri.parse(mJsonObject);
                        Log.d("finalurlparse", finalurlparse.toString());

                        Intent intent = new Intent(Intent.ACTION_VIEW, finalurlparse);
                        startActivity(intent);

                    } catch (JSONException e) {
                        Log.e("MYAPP", "unexpected JSON exception", e);
                    }


                }
                else if(btntext.equals("Top 100 Pop songs")) {

                    uri = Uri.parse(param); // missing 'http://' will cause crashed
                    Log.d("tttt: ", param);
                    String url1 = "http://www.youtubeinmp3.com/fetch/?format=JSON&video=" + url;
                    HttpHandler sh = new HttpHandler();
                    Log.d("uri", url1);
                    String jsonStr = sh.makeServiceCall(url1);
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        String mJsonObject = jsonObj.getString("link");
                        mtitle = jsonObj.getString("title");

                        Log.d("OutPut", mJsonObject.toString());
                        Log.d("tittle", mtitle.toString());

                        Uri finalurlparse = uri.parse(mJsonObject);
                        Log.d("finalurlparse", finalurlparse.toString());

                        Intent intent = new Intent(Intent.ACTION_VIEW, finalurlparse);
                        startActivity(intent);

                    } catch (JSONException e) {
                        Log.e("MYAPP", "unexpected JSON exception", e);
                    }
                } else if(btntext.equals("Top 100 Rock songs"))

                {
                    uri = Uri.parse(param); // missing 'http://' will cause crashed
                    Log.d("tttt: ", param);
                    String url1 = "http://www.youtubeinmp3.com/fetch/?format=JSON&video=" + url;
                    HttpHandler sh = new HttpHandler();
                    Log.d("uri", url1);
                    String jsonStr = sh.makeServiceCall(url1);
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        String mJsonObject = jsonObj.getString("link");
                        mtitle = jsonObj.getString("title");

                        Log.d("OutPut", mJsonObject.toString());
                        Log.d("tittle", mtitle.toString());

                        Uri finalurlparse = uri.parse(mJsonObject);
                        Log.d("finalurlparse", finalurlparse.toString());

                        Intent intent = new Intent(Intent.ACTION_VIEW, finalurlparse);
                        startActivity(intent);

                    } catch (JSONException e) {
                        Log.e("MYAPP", "unexpected JSON exception", e);
                    }
                }

                else if (btntext.equals("Traditional Greek Music")) {
                    uri = Uri.parse("http://www.youtubeinmp3.com/download/?video=https://www.youtube.com/watch?v=woNcegfZbbA"); // missing 'http://' will cause crashed
                }
                else if (btntext.equals("Spotify Top 100 Pop songs")) {
                    uri = Uri.parse("http://www.youtubeinmp3.com/download/get/?i=0EkTC4EORcfuLXHgBKObjicTFq4yUzMC&e=52"); // missing 'http://' will cause crashed
                }
                else if (btntext.equals("Spotify Top 100 Rock songs")) {
                    uri = Uri.parse("http://www.youtubeinmp3.com/download/get/?i=0EkTC4EORcfuLXHgBKObjicTFq4yUzMC&e=52"); // missing 'http://' will cause crashed

                } else if (btntext.equals("Deezer Top 100 Greek songs")) {
                    uri = Uri.parse("http://www.youtubeinmp3.com/download/get/?i=SOUcupKBobavw8pGuYHWDVqCfyDw637D&e=71");

                } else if (btntext.equals("Deezer Top 100 Pop songs")) {
                    uri = Uri.parse("http://www.youtubeinmp3.com/download/get/?i=0EkTC4EORcfuLXHgBKObjicTFq4yUzMC&e=52"); // missing 'http://' will cause crashed

                } else if (btntext.equals("Deezer Top 100 Rock songs")) {
                    uri = Uri.parse("http://www.youtubeinmp3.com/download/get/?i=0EkTC4EORcfuLXHgBKObjicTFq4yUzMC&e=52"); // missing 'http://' will cause crashed

                    Log.e("spinnerlist", btntext);
                }

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c.getTime());

                db.addIstoriko(new Istoriko(1, mtitle, formattedDate)); //playlist.toString();
                istorikoList = db.getAllIstoriko();

                for (Istoriko i : istorikoList) {
                    String log = "id: " + i.getId() + " , List: " + i.getList() + " , Date: " + i.getDate();
                    ArrayList<String> mylist = new ArrayList<String>();
                    mylist.add(log);
                    Log.d("Item: ", log);
                }

                istoriko = (ListView) findViewById(R.id.contentlist);
                istoriko.invalidate();

            }


        });

        //ΔΗΜΙΟΥΡΓΙΑ ΓΡΑΦΙΚΟΥ ΠΕΡΙΒΑΛΟΝΤΟΣ
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        // TAB ΛΙΣΤΕΣ
        TabHost.TabSpec spec1 = tabHost.newTabSpec("ΛΙΣΤΕΣ");
        spec1.setIndicator("ΛΙΣΤΕΣ");
        spec1.setContent(R.id.lists);

        // TAB ΙΣΤΟΡΙΚΟ
        TabHost.TabSpec spec2 = tabHost.newTabSpec("ΙΣΤΟΡΙΚΟ");
        spec2.setIndicator("ΙΣΤΟΡΙΚΟ");
        spec2.setContent(R.id.contentlist);

        // ΑΠΟΤΕΛΕΣΜΑΤΑ ΒΔ
        istorikoList = db.getAllIstoriko();


        for (Istoriko i : istorikoList) {
            String log = +i.getId() + i.getList() + i.getDate();
            List<String> istlist = new ArrayList<String>();
            istlist.add(log);
            Log.d("Lista", istlist.toString());
        }


        DBHandler db = new DBHandler(this); //ΔΗΜΙΟΥΡΓΙΑ ΑΝΤΙΚΕΙΜΕΝΟΥ DBHANDLER
        SQLiteDatabase db1 = db.getWritableDatabase();
        Cursor todoCursor = db1.rawQuery("SELECT * FROM istoriko", null);

        istoriko = (ListView) findViewById(R.id.contentlist);
        CustomCursorAdapter todoAdapter = new CustomCursorAdapter(this, todoCursor);
        istoriko.setAdapter(todoAdapter);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);

        //dropdown μενού
        Spinner spinnerSites = (Spinner) findViewById(R.id.spinner_sites);
        Spinner spinnerSiteLists = (Spinner) findViewById(R.id.spinner_site_lists);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sites_array, android.R.layout.simple_spinner_item);   //παροχή υπηρεσιών απο strings.xml
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSites.setAdapter(adapter);
        spinnerSites.setOnItemSelectedListener(this);
        spinnerSiteLists.setOnItemSelectedListener(this);

    }

    @Override
    // ΠΕΡΙΠΤΩΣΗ ΣΥΝΕΧΙΣΗΣ Android Εφαρμογής απο παρασκήνιο!
    protected void onResume() {
        super.onResume();


        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (spotPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    // Toast.makeText(MainActivity.this, "Network state changed: " + connectivity.toString(), Toast.LENGTH_SHORT).show();
                    spotPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (spotPlayer != null) {
            spotPlayer.addNotificationCallback(MainActivity.this);
            spotPlayer.addConnectionStateCallback(MainActivity.this);
        }
    }

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(SPOTIFY_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), SPOTIFY_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        spotPlayer = spotifyPlayer;
                        spotPlayer.addConnectionStateCallback(MainActivity.this);
                        spotPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        Toast.makeText(this, "Got authentication token", Toast.LENGTH_SHORT).show();
        if (spotPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), SPOTIFY_ID);
            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            spotPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    spotPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(MainActivity.this));
                    spotPlayer.addNotificationCallback(MainActivity.this);
                    spotPlayer.addConnectionStateCallback(MainActivity.this);
                    // Trigger UI refresh
                    //updateView();
                }

                @Override
                public void onError(Throwable error) {
                }
            });
        } else {
            spotPlayer.login(authResponse.getAccessToken());
        }
    }

    private boolean isLoggedIn() {
        return spotPlayer != null && spotPlayer.isLoggedIn();
    }

    public void onLoginButtonClicked(View view) {
        if (!isLoggedIn()) {
            openLoginWindow();
        } else {
            spotPlayer.logout();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        final String siteSelected = parent.getItemAtPosition(pos).toString();
        Spinner spinnerSiteLists = (Spinner) findViewById(R.id.spinner_site_lists);

        switch (parent.getId()) {
            case R.id.spinner_sites:

                if (siteSelected.equals("Spotify")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spotify_array, android.R.layout.simple_spinner_item);  //παροχή υπηρεσιών απο strings.xml

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    youtubeplayerview.setVisibility(View.INVISIBLE);  // "κρύψιμο" επιλογής YoutubePlayer
                    spinnerSiteLists.setAdapter(adapter);
                    String listSelected = spinnerSiteLists.getSelectedItem().toString();    //μετατροπή επιλογής σε String
                    Log.d("Spinner1", listSelected);

                } else if (siteSelected.equals("Youtube")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.youtube_array, android.R.layout.simple_spinner_item);  //παροχή υπηρεσιών απο strings.xml
                    youtubeplayerview.setVisibility(View.VISIBLE);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSiteLists.setAdapter(adapter);


                } else if (siteSelected.equals("Deezer")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.deezer_array, android.R.layout.simple_spinner_item);   //παροχή υπηρεσιών απο strings.xml
                    youtubeplayerview.setVisibility(View.INVISIBLE);  // "κρύψιμο" επιλογής YoutubePlayer
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinnerSiteLists.setAdapter(adapter);
                    String listSelected = spinnerSiteLists.getSelectedItem().toString();  //μετατροπή επιλογής σε String
                    Log.d("Spinner3", listSelected);
                }
                break;

            case R.id.spinner_site_lists:
                String btntext = spinnerSiteLists.getSelectedItem().toString();

                if (btntext.equals("Top 100 Greek songs")) {
                    lipsi.setVisibility(View.VISIBLE);
                    playlist = "PLFgquLnL59amLUQzbEhACbArpoHPiymHr";

                    if (albumPlayer1!=null) {
                        albumPlayer1.stop();
                        albumPlayer1.release();
                    }
                    stopPlaying();


                    if (mYoutubePlayer != null) {
                        mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        mYoutubePlayer.loadPlaylist(playlist);
                        mYoutubePlayer.play();
                        mYoutubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {

                            @Override
                            public void onVideoStarted() {
                            }

                            @Override
                            public void onVideoEnded() {
                            }

                            @Override
                            public void onLoading() {
                            }

                            @Override
                            public void onLoaded(String mVideoId) {
                                url = "http://www.youtube.com/watch?v=" + mVideoId;
                                Log.d("song", url);

                            }

                            @Override
                            public void onError(YouTubePlayer.ErrorReason reason) {
                            }

                            @Override
                            public void onAdStarted() {
                            }
                        });
                    }

                } else if (btntext.equals("Top 100 Pop songs")) {
                    playlist = "RDQMY31wyvd5Xx4";
                    lipsi.setVisibility(View.VISIBLE);
                    if (albumPlayer1!=null) {
                        albumPlayer1.stop();
                        albumPlayer1.release();
                    }
                    stopPlaying();

                    if (mYoutubePlayer != null) {

                        mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        mYoutubePlayer.loadPlaylist(playlist);
                        mYoutubePlayer.play();
                        mYoutubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {

                            @Override
                            public void onVideoStarted() {
                            }

                            @Override
                            public void onVideoEnded() {
                            }

                            @Override
                            public void onLoading() {
                            }

                            @Override
                            public void onLoaded(String mVideoId) {
                                url = "http://www.youtube.com/watch?v=" + mVideoId;
                                Log.d("song", url);

                            }

                            @Override
                            public void onError(YouTubePlayer.ErrorReason reason) {
                            }

                            @Override
                            public void onAdStarted() {
                            }
                        });

                    }

                } else if (btntext.equals("Top 100 Rock songs")) {
                    playlist = "PLz1ThN-w-t_zGtVNm9xtNXyPfwUJ1EfyZ";
                    lipsi.setVisibility(View.VISIBLE);
                    if (albumPlayer1!=null) {
                        albumPlayer1.stop();
                        albumPlayer1.release();
                    }
                    stopPlaying();

                    if (mYoutubePlayer != null) {
                        mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        mYoutubePlayer.loadPlaylist(playlist);
                        mYoutubePlayer.play();
                        mYoutubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {

                            @Override
                            public void onVideoStarted() {
                            }

                            @Override
                            public void onVideoEnded() {
                            }

                            @Override
                            public void onLoading() {
                            }

                            @Override
                            public void onLoaded(String mVideoId) {
                                url = "http://www.youtube.com/watch?v=" + mVideoId;
                                Log.d("song", url);

                            }

                            @Override
                            public void onError(YouTubePlayer.ErrorReason reason) {
                            }

                            @Override
                            public void onAdStarted() {
                            }
                        });
                    }

                } else if (btntext.equals("Traditional Greek Music")) {
                    stop.setVisibility(View.VISIBLE);
                    if (mYoutubePlayer != null) {
                        if (albumPlayer1!=null) {
                            albumPlayer1.stop();
                            albumPlayer1.release();
                        }
                        mYoutubePlayer.pause();

                    }
                    spotPlayer.playUri(mOperationCallback, "spotify:track:667NLNK3iovjorjiJ6FAY0", 0, 0);


                } else if (btntext.equals("Spotify Top 100 Pop songs")) {
                    stop.setVisibility(View.VISIBLE);
                    if (mYoutubePlayer != null) {
                        if (albumPlayer1!=null) {
                            albumPlayer1.stop();
                            albumPlayer1.release();
                        }
                        mYoutubePlayer.pause();
                    }
                    Toast.makeText(this, "Starting playback for spotify:track:3kGfazcbVvVkuZunzlLgTD", Toast.LENGTH_SHORT).show();

                    spotPlayer.playUri(null, "spotify:track:3kGfazcbVvVkuZunzlLgTD", 0, 0);

                } else if (btntext.equals("Spotify Top 100 Rock songs")) {
                    stop.setVisibility(View.VISIBLE);
                    if (mYoutubePlayer != null) {
                        if (albumPlayer1!=null) {
                            albumPlayer1.stop();
                            albumPlayer1.release();
                        }
                        mYoutubePlayer.pause();
                    }
                    Toast.makeText(this, "Spotify Top 100 Rock songs", Toast.LENGTH_SHORT).show();
                    String uri = TEST_SONG_MONO_URI;
                    Toast.makeText(this, "Starting playback for spotify:track:7gXdAqJLCa5aYUeLVxosOz\n", Toast.LENGTH_SHORT).show();
                    spotPlayer.playUri(mOperationCallback, "spotify:track:7gXdAqJLCa5aYUeLVxosOz", 0, 0);

                } else if (btntext.equals("Deezer Top 100 Greek songs")) {
                    stop.setVisibility(View.VISIBLE);
                    if (albumPlayer1==null) {

                        DeezerConnect deezerConnect = new DeezerConnect(this, applicationID);
                        deezerConnect.authorize(MainActivity.this, permissions, listener);


                        try {
                            albumPlayer1 = new AlbumPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                            tooManyPlayersExceptions.printStackTrace();
                        } catch (DeezerError deezerError) {
                            deezerError.printStackTrace();
                        }
                          long albumId= 6894384;
                        albumPlayer1.playAlbum(albumId);

                    }
                    else {
                        albumPlayer1.stop();
                        albumPlayer1.release();

                        DeezerConnect deezerConnect = new DeezerConnect(this, applicationID);
                        deezerConnect.authorize(MainActivity.this, permissions, listener);


                        try {
                            albumPlayer1 = new AlbumPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                            tooManyPlayersExceptions.printStackTrace();
                        } catch (DeezerError deezerError) {
                            deezerError.printStackTrace();
                        }
                        long albumId= 6894384;
                        albumPlayer1.playAlbum(albumId);
                    }


                    Toast.makeText(this, "Deezer Top 100 Greek songs  " + albumPlayer1, Toast.LENGTH_SHORT).show();


                } else if (btntext.equals("Deezer Top 100 Pop songs")) {
                    stop.setVisibility(View.VISIBLE);
                    stopPlaying();
                    if (albumPlayer1==null){

                        DeezerConnect deezerConnect = new DeezerConnect(this, applicationID);
                        deezerConnect.authorize(MainActivity.this, permissions, listener);

                        try {
                            albumPlayer1 = new AlbumPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                            tooManyPlayersExceptions.printStackTrace();
                        } catch (DeezerError deezerError) {
                            deezerError.printStackTrace();
                        }
                        long albumId =  14581500;
                        albumPlayer1.playAlbum(albumId);

                    }else {
                        albumPlayer1.stop();
                        albumPlayer1.release();

                        DeezerConnect deezerConnect = new DeezerConnect(this, applicationID);
                        deezerConnect.authorize(MainActivity.this, permissions, listener);

                        try {

                            albumPlayer1 = new AlbumPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                            tooManyPlayersExceptions.printStackTrace();
                        } catch (DeezerError deezerError) {
                            deezerError.printStackTrace();
                        }

                        long albumId =  14581500;
                        albumPlayer1.playAlbum(albumId);
                    }

                } else if (btntext.equals("Deezer Top 100 Rock songs")) {
                    stop.setVisibility(View.VISIBLE);
                    stopPlaying();
                    if (albumPlayer1 == null) {
                        DeezerConnect deezerConnect = new DeezerConnect(this, applicationID);
                        deezerConnect.authorize(MainActivity.this, permissions, listener);


                        try {
                            albumPlayer1 = new AlbumPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                            tooManyPlayersExceptions.printStackTrace();
                        } catch (DeezerError deezerError) {
                            deezerError.printStackTrace();
                        }
                        long albumId =  1262014;
                        albumPlayer1.playAlbum(albumId);
                    } else {
                        albumPlayer1.stop();
                        albumPlayer1.release();

                        DeezerConnect deezerConnect = new DeezerConnect(this, applicationID);
                        deezerConnect.authorize(MainActivity.this, permissions, listener);

                        try {

                            albumPlayer1 = new AlbumPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                            tooManyPlayersExceptions.printStackTrace();
                        } catch (DeezerError deezerError) {
                            deezerError.printStackTrace();
                        }
                        long albumId =  1262014;
                        albumPlayer1.playAlbum(albumId);
                    }
                    Log.e("spinnerlist", btntext);
                    break;
                }
        }
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

    }

    @Override
    public void onPlaybackError(Error error) {

    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        //updateView();
    }

    public void onLoginFailed(Error error) {
        Toast.makeText(this, "Login error " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTemporaryError() {
        Toast.makeText(this, "Temporary error occurred", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionMessage(final String message) {
        Toast.makeText(this, "Incoming connection message: " + message, Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onDestroy()

    {

        if (mYoutubePlayer!=null) {
            mYoutubePlayer.pause();
        }
        if (albumPlayer1!=null) {
            albumPlayer1.stop();
            albumPlayer1.release();
        }

        stopPlaying();

        //spotPlayer.destroy(); // !ΜΠΟΡΕΙ ΝΑ ΠΡΟΚΑΛΕΣΕΙ ΚΡΑΣΑΡΙΣΜΑΤΑ!
        super.onDestroy();
    }

    @Override
    public void onStop()
    {
        if (mYoutubePlayer!=null) {
            mYoutubePlayer.pause();
        }
        if (albumPlayer1!=null) {
            albumPlayer1.stop();
            albumPlayer1.release();
        }
        stopPlaying();
        super.onStop();
    }


    public String aplbumPlays(String tempor)  {
        String apotelesma="0";
        if(tempor.equals("0")){
            Log.d("WRONG", tempor);
        }else{
            apotelesma = tempor;
            Log.d("temptest", tempor);
        }
        return apotelesma;
    }

    private void stopPlaying() {
        if (spotPlayer != null) {
            spotPlayer.pause(mOperationCallback);
        }
    }


    public void showDownload(View view) {
        Intent i = new Intent();
        i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        startActivity(i);
    }


}







