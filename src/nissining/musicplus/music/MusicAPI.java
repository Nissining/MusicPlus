package nissining.musicplus.music;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;

import nissining.musicplus.MusicPlus;
import nissining.musicplus.music.utils.Layer;
import nissining.musicplus.music.utils.NBSDecoder;
import nissining.musicplus.music.utils.Note;
import nissining.musicplus.music.utils.Song;
import nissining.musicplus.player.MusicPlayer;
import nissining.musicplus.utils.Progress;

import java.io.File;
import java.util.*;

/**
 * MusicAPI By Nissining
 * <p>
 * 2022/9/25 PM 2:45
 */

public class MusicAPI {

    public Song nowSong = null; // 正在播放的nbs文件
    public short musicTick = -1;
    public long lastPlayed = 0;
    public int musicId = 0;
    public int playMode = 0;

    public List<Song> musicList = new ArrayList<>();

    public static String[] playModeStat = new String[]{
            "列表顺序", "列表循环", "单曲循环", "随机"
    };

    public MusicAPI() {
    }

    public String getNowSongName() {
        return nowSong.getSongName();
    }

    public String setPlayMode(int playMode) {
        this.playMode = playMode;
        return getPlayMode();
    }

    public String getPlayMode() {
        return playModeStat[playMode] + "模式";
    }

    public void loadAllSong(File musicFiles) {
        if (musicFiles == null)
            return;

        File[] files = musicFiles.listFiles();
        if (files == null)
            return;

        List<Song> songs = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory() || !f.getName().trim().toLowerCase().endsWith(".nbs"))
                continue;

            Song song = NBSDecoder.parse(f);
            if (song != null) {
                songs.add(song);
            }
        }

        if (!songs.isEmpty()) {
            musicList = songs;
            MusicPlus.debug("已加载共计 " + musicList.size() + " 首Song");
        }
    }

    // 初始化Music
    public void init(int mode) {
        this.playMode = mode;

        if (this.nowSong == null) {
            this.nextSongByMode();
        }
    }

    /**
     * 下一首
     */
    public boolean nextSong() {
        int index = musicList.indexOf(nowSong);
        int nextId;
        if (index >= musicList.size() - 1) {
            if (playMode == 0) { // 列表顺序播放
                return false;
            }
            nextId = 0;
        } else {
            nextId = index + 1;
        }
        setNowSongById(nextId);
        return true;
    }

    /**
     * 上一首
     */
    public boolean lastSong() {
        int nextId;
        if (musicId <= 0) {
            if (playMode == 0) {
                return false;
            }
            nextId = musicList.size() - 1;
        } else {
            nextId = musicId - 1;
        }
        setNowSongById(nextId);
        return true;
    }

    public void randomSong() {
        musicId = Progress.rand(0, musicList.size() - 1);
        setNowSongById(musicId);
    }

    /**
     * 设置音乐
     *
     * @param id 列表id
     * @return 音乐
     */
    public Song setNowSongById(int id) {
        if (musicList.isEmpty()) {
            MusicPlus.debug("播放列表为空！");
            return null;
        }
        if (id > musicList.size() - 1) {
            MusicPlus.debug("超出范围！ID:" + id);
            return null;
        }
        resetSong();
        musicId = id;
        nowSong = musicList.get(id);
        return nowSong;
    }

    public void resetSong() {
        nowSong = null;
        musicTick = -1;
        lastPlayed = System.currentTimeMillis() + 3000;
    }

    /**
     * 根据播放模式进行下一首音乐
     */
    public void nextSongByMode() {
        if (musicList.isEmpty()) {
            MusicPlus.debug("播放列表为空！添加曲目再进行播放！");
            return;
        }

        switch (playMode) {
            case 1:  // 列表循环播放
            case 0: // 列表顺序播放
            default:
                nextSong();
                break;
            case 2: // 单曲循环
                setNowSongById(this.musicId);
                break;
            case 3: // 随机播放
                randomSong();
                break;
        }
    }

    //
    public void setMusicTick(short i) {
        this.musicTick = i;
    }

    // 快进
    public void addMusicTick(short i) {
        setMusicTick((short) Math.min(this.musicTick + i, nowSong.getLength()));
    }

    public void tryPlay(List<MusicPlayer> mps) {
        if (nowSong == null) {
            return;
        }

        if (System.currentTimeMillis() - lastPlayed < 50 * nowSong.getDelay()) {
            return;
        }

        boolean isFinish = musicTick > nowSong.getLength();

        // 顺序播放
        if (isFinish && musicId >= musicList.size() && playMode == 0) {
            resetSong();
            MusicPlus.debug("播放失败！原因： 模式是顺序播放！已播放完毕");
            return;
        }

        musicTick++;
        if (isFinish) {
            nextSongByMode();
            return;
        }
        playTick(mps, musicTick);
        lastPlayed = System.currentTimeMillis();
    }

    public void playTick(List<MusicPlayer> mps, int tick) {
        Sound sound = null;
        float fl = 0;

        for (Layer l : nowSong.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) {
                continue;
            }

            switch (note.getInstrument()) {
                case 0:
                    sound = Sound.NOTE_HARP;
                    break;
                case 1:
                    sound = Sound.NOTE_BASS;
                    break;
                case 2:
                    sound = Sound.NOTE_BD;
                    break;
                case 3:
                    sound = Sound.NOTE_SNARE;
                    break;
                case 4:
                    sound = Sound.NOTE_HAT;
                    break;
                case 5:
                    sound = Sound.NOTE_GUITAR;
                    break;
                case 6:
                    sound = Sound.NOTE_FLUTE;
                    break;
                case 7:
                    sound = Sound.NOTE_BELL;
                    break;
                case 8:
                    sound = Sound.NOTE_CHIME;
                    break;
                case 9:
                    sound = Sound.NOTE_XYLOPHONE;
                    break;
            }

            int key33 = note.getKey() - 33;
            switch (key33) {
                case 0:
                    fl = 0.5f;
                    break;
                case 1:
                    fl = 0.529732f;
                    break;
                case 2:
                    fl = 0.561231f;
                    break;
                case 3:
                    fl = 0.594604f;
                    break;
                case 4:
                    fl = 0.629961f;
                    break;
                case 5:
                    fl = 0.667420f;
                    break;
                case 6:
                    fl = 0.707107f;
                    break;
                case 7:
                    fl = 0.749154f;
                    break;
                case 8:
                    fl = 0.793701f;
                    break;
                case 9:
                    fl = 0.840896f;
                    break;
                case 10:
                    fl = 0.890899f;
                    break;
                case 11:
                    fl = 0.943874f;
                    break;
                case 12:
                    fl = 1.0f;
                    break;
                case 13:
                    fl = 1.059463f;
                    break;
                case 14:
                    fl = 1.122462f;
                    break;
                case 15:
                    fl = 1.189207f;
                    break;
                case 16:
                    fl = 1.259921f;
                    break;
                case 17:
                    fl = 1.334840f;
                    break;
                case 18:
                    fl = 1.414214f;
                    break;
                case 19:
                    fl = 1.498307f;
                    break;
                case 20:
                    fl = 1.587401f;
                    break;
                case 21:
                    fl = 1.681793f;
                    break;
                case 22:
                    fl = 1.781797f;
                    break;
                case 23:
                    fl = 1.887749f;
                    break;
                case 24:
                    fl = 2.0f;
                    break;
            }

            for (MusicPlayer mp : mps) {
                Player p = mp.getPlayer();

                l.setVolume((byte) mp.getVol());

                if (p == null || mp.stopMusic) {
                    continue;
                }

                if (sound != null) {
                    // 播放声音
                    PlaySoundPacket soundPk = new PlaySoundPacket();
                    soundPk.name = sound.getSound();
                    soundPk.volume = l.getVolume();
                    soundPk.pitch = fl;
                    soundPk.x = p.getFloorX();
                    soundPk.y = p.getFloorY();
                    soundPk.z = p.getFloorZ();
                    p.dataPacket(soundPk);
                }
            }

        }
    }

    public String songStat() {
        if (musicList.isEmpty()) {
            return "播放列表为空！请添加曲目";
        }
        if (nowSong == null) {
            return "没有正在播放的音乐!";
        }

        String title = MusicPlus.ins.getConfig().getString("song_status_title");

        StringJoiner sj = new StringJoiner("\n", title, "");
        sj.add("");
        sj.add(playModeStat[playMode] + "模式");

        int minPage = 0;
        int maxPage = 10;
        if (musicId > 10) {
            // 如果 id=13 则区间为 10 - 20
            // id=130 则区间为 100 - 110
            // id=210 200 - 210
            String si = String.valueOf(musicId);
            int keyId = Integer.parseInt(si.substring(0, 1));
            String repeat = "0".repeat(si.length() - 1);

            minPage = Integer.parseInt(keyId + repeat);
            maxPage = Integer.parseInt((keyId + 1) + repeat);
        }

        for (int i = minPage; i < maxPage; i++) {
            String s = i + ".none";
            if (i < musicList.size()) {
                Song song = musicList.get(i);
                s = i + "." + song.getFormatSongName(getNowSongName());
            }
            sj.add(s);
        }

        // show par
        String playedTime = Progress.getFt(musicTick);
        String playedMaxTime = Progress.getFt(nowSong.getLength());
        String playedPar = Progress.addPar(musicTick, nowSong.getLength(), true);

        sj.add(playedTime + "/" + playedMaxTime + " " + playedPar);
        return sj.toString();
    }

}
