package it.uniroma2.musicplaylistdemo;

import java.util.LinkedList;

import android.app.*;
import android.content.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class SongsArrayAdapter extends ArrayAdapter<Song> {
	private final Context context;
	private final LinkedList<Song> values;
 
	public SongsArrayAdapter(Context context, LinkedList<Song> values) {
		super(context, android.R.layout.simple_list_item_1, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		final Song song = values.get(position);
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		view = inflater.inflate(R.layout.songslistrow, parent, false);
		TextView songslistrowTextViewArtist = (TextView)view.findViewById(R.id.songslistrowTextViewArtist);
		TextView songslistrowTextViewTitle = (TextView)view.findViewById(R.id.songslistrowTextViewTitle);
		if(song.artist!=null) songslistrowTextViewArtist.setText(song.artist);
		if(song.title!=null) songslistrowTextViewTitle.setText(song.title);
		if(song.playing) {
			ImageView songslistrowImageViewPlay = (ImageView)view.findViewById(R.id.songslistrowImageViewPlay);
			songslistrowImageViewPlay.setImageResource(android.R.drawable.ic_media_play);
		}
		
		ImageButton buttonSearch = (ImageButton)view.findViewById(R.id.songslistrowButtonSearch);
		buttonSearch.setFocusable(false);
		buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, SearchActivity.class);
				intent.putExtra("artist", song.artist);
				context.startActivity(intent);
			}
		});
		return view;
	}
}

