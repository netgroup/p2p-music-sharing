package it.uniroma2.musicplaylistdemo;

import java.io.*;
import java.net.*;
import java.util.*;

import android.app.*;
import android.os.*;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class SearchActivity extends Activity implements OnClickListener {
	//private Song song;
	private String artist, title;
	private Button buttonDownload, buttonCancel;
	private ListView list;
	
	private LinkedList<Song> songs;
	private ArrayAdapter<String> songsAdapter;
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.search);
		 
		 artist = getIntent().getStringExtra("artist");
		 title = getIntent().getStringExtra("title");
		 
		 buttonDownload = (Button)findViewById(R.id.buttonDownload);
		 buttonCancel = (Button)findViewById(R.id.buttonCancel);
		 buttonDownload.setOnClickListener(this);
		 buttonCancel.setOnClickListener(this);
		 list = (ListView)findViewById(R.id.listViewFoundSongs);
		 list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		 
		 String query = "";
		 if(artist!=null && !artist.equals("")) {
			 query += "artist="+artist;
		 }
		 if(artist!=null && title!=null && !artist.equals("") && !title.equals("")) {
			 query += "&&";
		 }
		 if(title!=null && !title.equals("")) {
			 query += "title="+title;
		 }
		 
		 songs = new LinkedList<Song>();
		 
		 new SearchNearbyTask(query).execute();
	}

	@Override
	public void onClick(View view) {
		if(view.equals(buttonCancel)) {
			finish();
		} else if(view.equals(buttonDownload)) {
			SparseBooleanArray selected = list.getCheckedItemPositions();
			LinkedList<Song> filesToDownload = new LinkedList<Song>();
			for(int i=0; i<songs.size(); i++){
				if(selected.get(i)) {
					Song s = songs.get(i);
					filesToDownload.add(s);
				}
			}
			new DownloadFilesTask(filesToDownload).execute();
		}
	}
	
	
	private class SearchNearbyTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		private String query;
		
		public SearchNearbyTask(String query) {
			progressDialog = new ProgressDialog(SearchActivity.this);
			this.query = query;
		}
		
		@Override
		protected void onPreExecute() {
	        progressDialog.setCancelable(true);
	        progressDialog.setMessage(SearchActivity.this.getText(R.string.searching));
	        progressDialog.show();
	    }

		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL url = new URL("http", "localhost", 8080, "//MCP/QUERY");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("x-mcp-query", query);
				connection.setRequestProperty("x-mcp-ttl", "10");
				connection.setRequestProperty("x-mcp-app", "it.uniroma2.musicplaylistdemo");
				
				if(connection.getResponseCode()!=200) return null;
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = reader.readLine();
				if(line!=null) {
					String[] songsArrived = line.split("\t");
					for(String songArrived : songsArrived) {
						Song s = new Song();
						String[] fields = songArrived.split("&&");
						for(String field : fields) {
							String[] parts = field.split("=");
							String key = parts[0];
							String value = parts[1];
							if(key.equals("artist")) {
								s.artist = value;
							} else if(key.equals("title")) {
								s.title = value;
							} else if(key.equals("md5")) {
								s.md5digest = value;
							} else if(key.equals("filename")) {
								s.file = value;
							}
						}
						songs.add(s);
					}
				}
				reader.close();
			} catch(Exception e) {
				Log.e("E", "E", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			String[] titles = new String[songs.size()];
			for(int i=0; i<songs.size(); i++) {
				titles[i] = songs.get(i).artist+" - "+songs.get(i).title;
			}
			songsAdapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_multiple_choice, titles);
			list.setAdapter(songsAdapter);
			if(progressDialog.isShowing()) {
				progressDialog.dismiss();
	        }
		}
	}
	
	
	private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		private LinkedList<Song> filesToDownload;
		
		public DownloadFilesTask(LinkedList<Song> filesToDownload) {
			progressDialog = new ProgressDialog(SearchActivity.this);
			this.filesToDownload = filesToDownload;
		}
		
		@Override
		protected void onPreExecute() {
	        progressDialog.setCancelable(true);
	        progressDialog.setMessage(SearchActivity.this.getText(R.string.downloading));
	        progressDialog.setMax(filesToDownload.size());
	        progressDialog.setIndeterminate(false);
	        progressDialog.setProgress(0);
	        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        progressDialog.show();
	    }
		
		@Override
		protected void onProgressUpdate(Void... value) {
			if(progressDialog.isShowing()) {
				progressDialog.setProgress(progressDialog.getProgress()+1);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			for(Song s : filesToDownload) {
				publishProgress();
				
				try {
					URL url = new URL("http", "localhost", 8080, "//MCP/FILE");
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("x-mcp-digest", s.md5digest);
					connection.setRequestProperty("x-mcp-app", "it.uniroma2.musicplaylistdemo");
					
					if(connection.getResponseCode()!=200) continue;
					
					File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"/"+s.file);
					file.createNewFile();
					FileOutputStream fos = new FileOutputStream(file);
					DataInputStream dis = new DataInputStream(connection.getInputStream());
					byte[] buffer = new byte[100000];
					int read;
					while((read=dis.read(buffer))>-1) {
						fos.write(buffer, 0, read);
						//fos.write(buffer);
					}
					dis.close();
					fos.close();
				} catch(Exception e) {
					Log.e("E", "E", e);
				}
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(progressDialog.isShowing()) {
				progressDialog.dismiss();
	        }
			finish();
		}
	}
}
