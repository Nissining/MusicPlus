package nissining.musicplus.music;

import nissining.musicplus.music.utils.Song;

import java.util.List;

public class MusicPage {

    public int pageId;
    public List<Song> songs;

    public MusicPage(int pageId) {
        this.pageId = pageId;
    }

    public MusicPage setAllSongs(List<Song> songs) {
        this.songs = songs;
        return this;
    }

    public int getPageIdBySong(Song song) {
        for (Song song1 : songs) {
            if (song1.getSongName().equalsIgnoreCase(song.getSongName()))
                return pageId;
        }
        return -1;
    }

    public List<Song> getSongs() {
        return songs;
    }
}
