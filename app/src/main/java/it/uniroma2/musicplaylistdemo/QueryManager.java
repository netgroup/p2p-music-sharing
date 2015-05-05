package it.uniroma2.musicplaylistdemo;

import java.util.*;
import android.app.*;
import android.content.*;
import android.os.*;
import it.uniroma2.mobilecollaborationplatform.querymanager.*;

public class QueryManager extends Service {
	LinkedList<Song> songs;
	
	@Override
	public IBinder onBind(Intent intent) {
		return addBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		songs = Song.loadSongs();
	}

	private final QueryManagerOp.Stub addBinder = new QueryManagerOp.Stub() {
		
		public String getQueryResponse(String query) {
			String response = "";
			
			String artist=null, title=null;
			
			String[] fields = query.split("&&");
			for(String field : fields) {
				String parts[] = field.split("=");
				String key = parts[0];
				String value = parts[1];
				
				if(key.equals("artist") && !value.equals("")) {
					artist = value;
				} else if(key.equals("title") && !value.equals("")) {
					title = value;
				}
			}
			
			for(Song s : songs) {
				if(artist!=null && title!=null) {
					if(s.artist!=null && s.title!=null && s.artist.toLowerCase().contains(artist.toLowerCase()) && s.title.toLowerCase().contains(title.toLowerCase())) {
						response += "artist="+s.artist+"&&title="+s.title+"&&md5="+s.md5digest+"&&filename="+s.file+"\t";
					}
				} else if(artist!=null && s.artist!=null && s.artist.toLowerCase().contains(artist.toLowerCase())) {
					response += "artist="+s.artist+"&&title="+s.title+"&&md5="+s.md5digest+"&&filename="+s.file+"\t";
				} else if(title!=null && s.title!=null && s.title.toLowerCase().contains(title.toLowerCase())) {
					response += "artist="+s.artist+"&&title="+s.title+"&&md5="+s.md5digest+"&&filename="+s.file+"\t";
				}
			}
			
			if(response.equals("")) return null;
			return response;
		}
		
		public String getFile(String digest) {
			for(Song s : songs) {
				if(s.md5digest.equals(digest)) {
					return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"/"+s.file;
				}
			}
			return null; // We return null when the required file is not available
		}
	};
}

