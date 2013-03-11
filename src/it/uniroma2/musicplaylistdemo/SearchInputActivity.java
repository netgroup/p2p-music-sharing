package it.uniroma2.musicplaylistdemo;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class SearchInputActivity extends Activity implements OnClickListener {
	private EditText editTextArtist, editTextTitle;
	private Button searchButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchinput);
		
		editTextArtist = (EditText) findViewById(R.id.editTextArtist);
		editTextTitle = (EditText) findViewById(R.id.editTextTitle);
		searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(searchButton)) {
			Song song = new Song();
			
			song.artist = editTextArtist.getText().toString();
		    song.title =  editTextTitle.getText().toString();
		    
		    Intent intent = new Intent(this, SearchActivity.class);
			intent.putExtra("artist", song.artist);
			intent.putExtra("title", song.title);
			startActivity(intent);
		}
	}
}
