package it.uniroma2.musicplaylistdemo;

import java.io.*;
import java.security.MessageDigest;
import java.util.LinkedList;

import android.media.MediaMetadataRetriever;
import android.os.Environment;

public class Song implements Serializable {
	private static final long serialVersionUID = 1L;
	String title;
	String artist;
	String genre;
	String year;
	String file;
	String md5digest;
	boolean playing = false;
	
	private static LinkedList<Song> getSongsInFolder(File folder) {
		File[] files = folder.listFiles();
		LinkedList<Song> songs = new LinkedList<Song>();
		
		for(int i=0; i<files.length; i++) {
			if(new File(files[i].toString()).isDirectory()) continue;
			Song song = new Song();
			song.file = files[i].getName();
			song.md5digest = getMD5Digest(files[i]);
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		    mmr.setDataSource(files[i].toString());
		    song.artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		    song.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		    if(song.title==null || song.title.equals("")) song.title = song.file;
		    songs.add(song);
		}
		
		return songs;
	}
	
	public static LinkedList<Song> loadSongs() {
		File musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
		File[] files = musicFolder.listFiles();
		LinkedList<Song> songs = new LinkedList<Song>();
		
		songs.addAll(getSongsInFolder(musicFolder));
		
		for(int i=0; i<files.length; i++) {
			File folder = new File(files[i].toString());
			if(folder.isDirectory()) {
				songs.addAll(getSongsInFolder(folder));
			}
		}
		return songs;
	 }
	
	/**
	 * Calculates the MD5 digest of a file
	 * @param file	The input file
	 * @return		The MD5 digest
	 */
	private static String getMD5Digest(File file) {
		try {
			String md5 = "";
			InputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			fis.close();
			byte[] b = complete.digest();
			for (int i = 0; i < b.length; i++) {
				md5 += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
			}
			return md5;
		} catch(Exception e) {
			return null;
		}
	}
}
