package it.uniroma2.musicplaylistdemo;

import android.app.*;
import android.content.Intent;
import android.media.*;
import android.media.MediaPlayer.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.SeekBar.*;

public class MainActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, OnItemClickListener {
	private MediaPlayer mp = new MediaPlayer();
	private ImageButton buttonPlay, buttonPause, buttonPrevious, buttonNext, buttonRefresh;
	
	private TextView textArtist, textTitle, textPosition;
	private SeekBar seekbar;
	
	private ListView listViewSongs;

	private boolean running = true;
	private int currentPosition;
	
	private SongsArrayAdapter songsAdapter;
	//Song[] songs;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.main);
		 
		buttonPlay = (ImageButton)findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(this);
        buttonPause = (ImageButton)findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(this);
        buttonPrevious = (ImageButton)findViewById(R.id.buttonPrevious);
        buttonPrevious.setOnClickListener(this);
        buttonNext = (ImageButton)findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(this);
        buttonRefresh = (ImageButton)findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(this);
        
        textArtist = (TextView)findViewById(R.id.textArtist);
        textTitle = (TextView)findViewById(R.id.textTitle);
        textPosition = (TextView)findViewById(R.id.textPosition);
        
        seekbar = (SeekBar)findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.setEnabled(false);
        
        listViewSongs = (ListView)findViewById(R.id.listViewSongs);
        listViewSongs.setOnItemClickListener(this);
        
        updateList();
        
        running = true;
        Thread t = new TimeUpdater();
    	t.start();
	 }
	 
	 @Override
	 public void onResume() {
		 super.onResume();
		 updateList();
	 }
	 
	 @Override
	 public void onDestroy() {
		 super.onDestroy();
		 running = false;
		 if(mp.isPlaying()) mp.stop();
		 mp.release();
	 }
	 
	private void updateList() {
		new SongLoaderTask().execute();
	}
	
	private void newSong(Song song) {
		for(int i=0; i<songsAdapter.getCount(); i++) {
			Song s = songsAdapter.getItem(i);
			s.playing = false;
		}
    	textArtist.setText(song.artist);
    	textTitle.setText(song.title);
    	textPosition.setText("");
    	seekbar.setEnabled(true);
    	song.playing = true;
    	updateList();
    	
    	try {
    		mp.reset();
			mp.setDataSource(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()+"/"+song.file);
    		mp.prepare();
			mp.start();
			mp.setOnCompletionListener(new OnCompletionListener() {
	    		public void onCompletion(MediaPlayer arg0) {
	    			if(currentPosition<songsAdapter.getCount()-1) {
	    				currentPosition++;
	    				newSong(songsAdapter.getItem(currentPosition));
	    			}
				}
			});
			seekbar.setMax(mp.getDuration());
		} catch(Exception e) {
			Log.e("ERROR", "", e);
			Toast.makeText(this, "Error playing: "+song.file.toString(), Toast.LENGTH_LONG).show();
		}
	}
	 
	 private class TimeUpdater extends Thread {
	    	public void run() {
	    		while(running) {
		    		if(mp.isPlaying()) {
						final int currentPosition = mp.getCurrentPosition();
						seekbar.setProgress(currentPosition);
						runOnUiThread(new Runnable() {
							public void run() {
								textPosition.setText(formatTime(currentPosition)+" / "+formatTime(mp.getDuration()));
							}
						});
		    			
		    		}
		    		try { Thread.sleep(1000); } catch(Exception e) {}
	    		}
	    	}
	    	
	    	private String formatTime(int milliseconds) {
	    		String ret = "";
	    		int seconds = (int) (milliseconds / 1000) % 60 ;
	    		int minutes = (int) ((milliseconds / (1000*60)) % 60);
	    		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
	    		if(hours>0) ret+= hours+":";
	    		ret += minutes<10 ? "0"+minutes+":" : minutes+":";
	    		ret += seconds<10 ? "0"+seconds : seconds+"";
	    		return ret;
	    	}
	    }
	
	@Override
	public void onClick(View view) {
		if(view.equals(buttonPlay)) {
			mp.start();
		} else if(view.equals(buttonPause)) {
			if(mp.isPlaying()) mp.pause();
		} else if(view.equals(buttonPrevious)) {
			if(currentPosition>0) {
				currentPosition--;
			} else {
				currentPosition = songsAdapter.getCount()-1;
			}
			newSong(songsAdapter.getItem(currentPosition));
		} else if(view.equals(buttonNext)) {
			if(currentPosition<songsAdapter.getCount()-1) {
				currentPosition++;
			} else {
				currentPosition = 0;
			}
			newSong(songsAdapter.getItem(currentPosition));
		} else if(view.equals(buttonRefresh)) {
			updateList();
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			if(mp.isPlaying()) mp.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		currentPosition = position;
		Song song = songsAdapter.getItem(position);
		newSong(song);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:
			Intent intent = new Intent(this, SearchInputActivity.class);
			startActivity(intent);
			return super.onOptionsItemSelected(item);
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	
	
	private class SongLoaderTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		
		private SongLoaderTask() {
			progressDialog = new ProgressDialog(MainActivity.this);
		}
		
		@Override
		protected void onPreExecute() {
	        progressDialog.setCancelable(true);
	        progressDialog.setMessage(MainActivity.this.getText(R.string.loading));
	        progressDialog.show();
	    }

		@Override
		protected Void doInBackground(Void... params) {
			songsAdapter = new SongsArrayAdapter(MainActivity.this, Song.loadSongs());
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			listViewSongs.setAdapter(songsAdapter);
			if(progressDialog.isShowing()) {
				progressDialog.dismiss();
	        }
		}
	}
}
