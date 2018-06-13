package huy.example.playaudiofromurl;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


public class StreamingMp3Player extends Activity implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {
    private TextView songTotalDurationLabel, songCurrentDurationLabel;
    private ImageButton buttonPlayPause;
    private SeekBar seekBarProgress;
    public EditText editTextSongURL;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
    public int delayMillis = 100;
    String URL = "http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3";
    private final Handler handler = new Handler();
    private String TAG ="StreamingMp3Player";
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * This method initialise all the views in project
     */
    private void initView() {
        buttonPlayPause = (ImageButton) findViewById(R.id.ButtonTestPlayPause);
        buttonPlayPause.setOnClickListener(this);

        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);

        seekBarProgress = (SeekBar) findViewById(R.id.SeekBarTestPlay);
//        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setProgress(0);
        seekBarProgress.setMax(100);

        seekBarProgress.setOnTouchListener(this);
        editTextSongURL = (EditText) findViewById(R.id.EditTextSongURL);
        editTextSongURL.setText(R.string.testsong_20_sec);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    /**
     * Method which updates the SeekBar primary progress by current song playing position
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            songTotalDurationLabel.setText("" + Utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText("" + Utils.milliSecondsToTimer(currentDuration));
//        seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"

            // Updating progress bar
            int progress = Utils.getProgressPercentage(currentDuration, totalDuration);
            //Log.d("Progress", ""+progress);
            seekBarProgress.setProgress(progress);

            if (mediaPlayer.isPlaying()) {
                handler.postDelayed(this, delayMillis);
            }
        }
    };


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ButtonTestPlayPause) {
            new AudioPlayer(this,URL,"file name").show();
//                        play();
        }
    }

    private void play() {
        new processLoading().execute();
    }

    private class processLoading extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                mediaPlayer.setDataSource(editTextSongURL.getText().toString()); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
                mediaPlayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                buttonPlayPause.setImageResource(R.drawable.button_pause);
            } else {
                mediaPlayer.pause();
                buttonPlayPause.setImageResource(R.drawable.button_play);
            }
            handler.postDelayed(mUpdateTimeTask, delayMillis);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.SeekBarTestPlay) {
            /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
            if (mediaPlayer.isPlaying()) {
                SeekBar sb = (SeekBar) v;
                int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMillisecconds);
            }
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
        buttonPlayPause.setImageResource(R.drawable.button_play);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        /** Method which updates the SeekBar secondary progress by current song loading from URL position*/
//        Log.d(TAG,"onBufferingUpdate:"+percent);
        seekBarProgress.setSecondaryProgress(percent);
    }

}

