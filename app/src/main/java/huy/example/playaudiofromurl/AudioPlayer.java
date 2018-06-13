package huy.example.playaudiofromurl;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by maidinh on 27-Sep-17.
 */

public class AudioPlayer extends Dialog implements View.OnClickListener, View.OnTouchListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    private TextView songTotalDurationLabel, songCurrentDurationLabel;
    private ImageButton buttonPlayPause;
    private SeekBar seekBarProgress;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
    public int delayMillis = 100;
    String URL = "http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3";
    private final Handler handler = new Handler();
    private String TAG = "StreamingMp3Player";
    private String pathAudio = "";
    private TextView tvName;
    private String fileName = "";
    private ImageView ivClose;

    public AudioPlayer(@NonNull Context context, String pathAudio, String fileName) {
        super(context);
        this.pathAudio = pathAudio;
        this.fileName = fileName;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.player);
//        this.setCanceledOnTouchOutside(false);
        initView();

        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void initView() {
        buttonPlayPause = (ImageButton) findViewById(R.id.ButtonTestPlayPause);
        buttonPlayPause.setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tvName);
        tvName.setText(fileName);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);

        ivClose = (ImageView) findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);

        seekBarProgress = (SeekBar) findViewById(R.id.SeekBarTestPlay);
//        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setProgress(0);
        seekBarProgress.setMax(100);

        seekBarProgress.setOnTouchListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);

        buttonPlayPause.setEnabled(false);
        play();
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
        if (v == buttonPlayPause) {
            play();
        } else if (v == ivClose) {
            dismiss();
        }
    }

    private void play() {
        new processLoading().execute();
    }

    private class processLoading extends AsyncTask<Boolean, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Boolean... strings) {
            try {
                mediaPlayer.setDataSource(pathAudio); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
                mediaPlayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            buttonPlayPause.setEnabled(true);
//            if(!flag){
//                Toast.makeText(getContext(),"FAIL",Toast.LENGTH_SHORT).show();
//                return;
//            }
            if (progressBar != null) progressBar.setVisibility(View.GONE);
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
