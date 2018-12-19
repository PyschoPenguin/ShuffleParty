package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

public class PlaylistActivity extends Activity
{
	private ConnectionParams connectionParams;
	private SpotifyService spotify;
	private SpotifyAppRemote spotifyAppRemote;
	private PlayerState playerState;

	private int elapsed = 0;
	private int index = 0;
	private List<Track> playlistItems;
	private Track currentTrack;

	private SpotifyItemAdapter adapter;

	private RecyclerView playlistItemsView;
	private Button btnPlayPause;
	private SeekBar seekbarProgress;

	private TextView tvTrackDur;
	private TextView tvTrackElap;

	private Handler seekbarHandler;
	private Runnable updateRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);

		seekbarHandler = new Handler();

		btnPlayPause = findViewById(R.id.btnPlayPause);
		playlistItemsView = findViewById(R.id.playlistItemsView);

		seekbarProgress = findViewById(R.id.seekbarProgress);
		initSeekbar();

		tvTrackDur = findViewById(R.id.tvTrackDur);
		tvTrackElap = findViewById(R.id.tvTrackElap);

		playlistItems = (List<Track>) getIntent().getSerializableExtra("pl");
		currentTrack = playlistItems.get(index);

		adapter = new SpotifyItemAdapter(this, playlistItems);
		adapter.setTrackListener(new SpotifyItemAdapter.TrackItemSelectedListener()
		{
			@Override
			public void onItemSelected(View itemView, Track item, int position)
			{
				currentTrack = item;
				spotifyAppRemote.getPlayerApi().play(currentTrack.uri);
				updateUI(currentTrack);
			}
		});

		playlistItemsView.setHasFixedSize(true);
		playlistItemsView.setLayoutManager(new LinearLayoutManager(this));

		playlistItemsView.setAdapter(adapter);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		connectionParams = new ConnectionParams.Builder(SpotifyConstants.ClientID)
				.setRedirectUri(SpotifyConstants.REDIRECT_URL)
				.showAuthView(true)
				.build();

		SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
			{
				spotifyAppRemote = mSpotifyAppRemote;

				initPlayerOnEvent();

				spotifyAppRemote.getPlayerApi().play(currentTrack.uri);

				initRunnable();
				runOnUiThread(updateRunnable);
				updateUI(currentTrack);
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	public void btnPlayPause_onClick(View view)
	{
		if (spotifyAppRemote != null)
		{
			if (currentTrack != null)
			{
				if (!playerState.isPaused)
				{
					spotifyAppRemote.getPlayerApi().pause();
					btnPlayPause.setBackgroundResource(R.drawable.round_button_play);
					seekbarHandler.removeCallbacks(updateRunnable);
				} else
				{
					spotifyAppRemote.getPlayerApi().resume();
					btnPlayPause.setBackgroundResource(R.drawable.round_button_pause);
					seekbarHandler.post(updateRunnable);
				}
			}
		}
	}

	private void updateUI(Track current) {
		elapsed = 0;
		seekbarProgress.setMax((int)(current.duration_ms / 1000) - 2);
		seekbarProgress.setProgress(elapsed);

		tvTrackDur.setText(SpotifyUtils.formatTimeDuration((int)current.duration_ms / 1000));
		tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsed));
	}

	private void initSeekbar(){
		seekbarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (fromUser)
				{
					elapsed = progress;
					seekBar.setProgress(elapsed);
					spotifyAppRemote.getPlayerApi().seekTo(elapsed * 1000);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});
	}

	private void initRunnable(){
		updateRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				if (currentTrack != null)
				{
					if (elapsed == (currentTrack.duration_ms / 1000) - 2)
					{
						if (index >= playlistItems.size() - 1)
							index = -1;

						index += 1;
						currentTrack = playlistItems.get(index);

						updateUI(currentTrack);

						elapsed = 0;
						spotifyAppRemote.getPlayerApi().play(currentTrack.uri);
					}

					elapsed += 1;
					tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsed));
					seekbarProgress.setProgress(elapsed);
				}
				seekbarHandler.postDelayed(this, 1000);
			}
		};
	}

	private void initPlayerOnEvent(){
		if (spotifyAppRemote != null) {
			spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>()
			{
				@Override
				public void onEvent(PlayerState mPlayerState)
				{
					playerState = mPlayerState;
 				}
			});
		}
	}

}