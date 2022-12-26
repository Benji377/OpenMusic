package com.musicplayer.openmusic.data.base

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class PlaylistSongDao {
    @Insert
    abstract fun insert(playlistSong: PlaylistSong)

    @Query("DELETE FROM Playlist_Song WHERE song_id=:songId AND playlist_id=:playlistId")
    protected abstract fun deleteSong(playlistId: String, songId: String)

    @Query("DELETE FROM Playlist_Song WHERE song_id=:songId AND playlist_id=:playlistId AND song_index=:songIndex")
    protected abstract fun deleteSong(playlistId: String, songId: String, songIndex: Int)

    @Query("DELETE FROM Playlist_Song WHERE playlist_id=:playlistId")
    abstract fun deletePlaylist(playlistId: String)

    @Query("SELECT * FROM Playlist_Song WHERE playlist_id=:playlistId ORDER BY song_index")
    protected abstract fun getPlaylistSongs(playlistId: String): MutableList<PlaylistSong>

    @Query("SELECT * FROM Playlist_Song WHERE song_id=:songId ORDER BY playlist_id, song_index")
    protected abstract fun getSongPlaylists(songId: String): MutableList<PlaylistSong>

    @Query("UPDATE Playlist_Song SET song_index=:songIndex WHERE playlist_id=:playlistId AND song_id=:songId")
    protected abstract fun updateIndex(playlistId: String, songId: String, songIndex: Int)

    @Transaction
    open fun removeSongFromPlaylist(
        playlistId: String,
        songId: String,
        songIndex: Int,
        allCopies: Boolean
    ) {
        if (allCopies) deleteSong(playlistId, songId) else deleteSong(playlistId, songId, songIndex)
        val songsInSamePlaylist = getPlaylistSongs(playlistId)
        for (i in songIndex until songsInSamePlaylist.size) updateIndex(
            playlistId,
            songsInSamePlaylist[i].songID.toString(),
            i
        )
    }

    @Transaction
    open fun removeAllSongRefs(songId: String) {
        val playlistsContainingSong = getSongPlaylists(songId)
        for (i in playlistsContainingSong.indices) {
            val p = playlistsContainingSong[i]
            removeSongFromPlaylist(p.playlistID.toString(), songId, p.index, true)
        }
    }
}