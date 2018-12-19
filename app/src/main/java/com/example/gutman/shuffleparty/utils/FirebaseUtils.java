package com.example.gutman.shuffleparty.utils;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.espresso.core.internal.deps.guava.base.Joiner;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class FirebaseUtils
{
	public static final String MAIN = "Tracks";
	public static final String TITLE_CHILD = "Title";
	public static final String ARTISTS_CHILD = "Artists";
	public static final String EXPLICITY_CHILD = "Explicity";
	public static final String DURATION_MS_CHILD = "DurationMs";
	public static final String TRACK_URI_CHILD = "TrackUri";
	public static final String IMAGE_URL_CHILD = "ImageUrl";

	public static void saveTrackToDatabase(DatabaseReference ref, Track save)
	{
		ref = ref.child(save.name);
		ref.child(ARTISTS_CHILD).setValue(getAlbumArtists(save));
		ref.child(EXPLICITY_CHILD).setValue(save.explicit);
		ref.child(DURATION_MS_CHILD).setValue(save.duration_ms);
		ref.child(TRACK_URI_CHILD).setValue(save.uri);
		ref.child(IMAGE_URL_CHILD).setValue(save.album.images.get(0).url);
	}

	public static Track getTrackFromDatabase(DatabaseReference ref, final String title) {
		final Track value = new Track();
		ref.addChildEventListener(new ChildEventListener()
		{
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
			{
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					if (ds.getKey() == title) {
						String name = ds.getKey();
						String artists = ds.child(ARTISTS_CHILD).getValue(String.class);
						List<ArtistSimple> artistSimpleList = getAlbumArtists(artists);

						String imageUrl = ds.child(IMAGE_URL_CHILD).getValue(String.class);
						String trackUri = ds.child(TRACK_URI_CHILD).getValue(String.class);

						long trackDurMs = ds.child(DURATION_MS_CHILD).getValue(Long.class);
						boolean explicit = ds.child(EXPLICITY_CHILD).getValue(Boolean.class);

						value.name = name;
						value.artists = artistSimpleList;
						value.album.images.get(0).url = imageUrl;
						value.uri = trackUri;
						value.duration_ms = trackDurMs;
						value.explicit = explicit;
					}
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
			{

			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
			{

			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
			{

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError)
			{

			}
		});

		return value;
	}

	private static String getAlbumArtists(Track item)
	{
		List<String> names = new ArrayList<>();
		for (ArtistSimple i : item.artists)
		{
			names.add(i.name);
		}
		Joiner joiner = Joiner.on(", ");
		return joiner.join(names);
	}

	private static List<String> getAlbumArtistsSplit(String artists) {
		List<String> individualArtists = Arrays.asList(artists.split(","));
		List<String> individualArtistsTrimmed = new ArrayList<>();
		for (int i = 0; i < individualArtists.size(); i++) {
			String current = individualArtists.get(i);
			individualArtistsTrimmed.add(current.trim());
		}
		return individualArtistsTrimmed;
	}

	public static List<ArtistSimple> getAlbumArtists(String artists) {
		List<String> individualArtists = getAlbumArtistsSplit(artists);
		List<ArtistSimple> artistSimpleList = new ArrayList<>();
		for (String s : individualArtists) {
			ArtistSimple current = new ArtistSimple();
			current.name = s;
			artistSimpleList.add(current);
		}
		return artistSimpleList;
	}

}