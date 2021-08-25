package com.musicplayer.SocyMusic.data.base;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;

import java.util.List;

@Dao
public abstract class PlaylistSongDao {
    @Insert
    public abstract void insert(PlaylistSong playlistSong);

    @Query("DELETE FROM Playlist_Song WHERE song_id=:songId AND playlist_id=:playlistId")
    protected abstract void deleteSong(String playlistId, String songId);

    @Query("DELETE FROM Playlist_Song WHERE song_id=:songId AND playlist_id=:playlistId AND song_index=:songIndex")
    protected abstract void deleteSong(String playlistId, String songId, int songIndex);

    @Query("SELECT * FROM Playlist_Song WHERE playlist_id=:playlistId ORDER BY song_index")
    protected abstract List<PlaylistSong> getPlaylistSongs(String playlistId);

    @Query("SELECT * FROM Playlist_Song WHERE song_id=:songId ORDER BY playlist_id, song_index")
    protected abstract List<PlaylistSong> getSongPlaylists(String songId);


    @Query("UPDATE Playlist_Song SET song_index=:songIndex WHERE playlist_id=:playlistId AND song_id=:songId")
    protected abstract void updateIndex(String playlistId, String songId, int songIndex);

    @Transaction
    public void removeSongFromPlaylist(String playlistId, String songId, int songIndex, boolean allCopies) {
        if (allCopies)
            deleteSong(playlistId, songId);
        else
            deleteSong(playlistId, songId, songIndex);
        List<PlaylistSong> songsInSamePlaylist = getPlaylistSongs(playlistId);
        for (int i = songIndex; i < songsInSamePlaylist.size(); i++)
            updateIndex(playlistId, songsInSamePlaylist.get(i).songID.toString(), i);
    }

    @Transaction
    public void removeAllSongRefs(String songId) {
        List<PlaylistSong> playlistsContainingSong = getSongPlaylists(songId);

        for (int i = 0; i < playlistsContainingSong.size(); i++) {
            PlaylistSong p = playlistsContainingSong.get(i);
            removeSongFromPlaylist(p.playlistID.toString(), songId, p.index, true);
        }

    }

}
