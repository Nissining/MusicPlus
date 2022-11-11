package nissining.musicplus.music;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.BlockEventPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.TextFormat;
import nissining.musicplus.MusicPlus;
import nissining.musicplus.music.utils.Layer;
import nissining.musicplus.music.utils.NBSDecoder;
import nissining.musicplus.music.utils.Note;
import nissining.musicplus.music.utils.Song;
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
    public boolean stopMusic = false; // 开始/暂停音乐
    public byte volume = 100; // 音量大小
    public short musicTick = -1;
    public long lastPlayed = 0;
    public int musicId = 0;
    public int playMode = 0; // 0=列表顺序 1=列表循环 2=单曲循环 3=随机

    public List<Song> musicList = new ArrayList<>();

    public static String[] playModeStat = new String[]{
            "列表顺序", "列表循环", "单曲循环", "随机"
    };

    public MusicAPI() {
    }

    public byte getVolume() {
        return volume;
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
        }
    }

    // 初始化Music
    public void init(int mode) {
        this.playMode = mode;

        if (this.nowSong == null) {
            this.nextSongByMode();
        }
        this.stopMusic = false;
    }

/*
    public void reset() {
        playMode = 0;
        musicId = 0;
        musicTick = -1;
        nowSong = null;
        stopMusic = false;
    }
*/

    /**
     * 下一首
     */
    public Song nextSong() {
        int index = musicList.indexOf(nowSong);
        int nextId;
        if (index >= musicList.size() - 1) {
            nextId = 0;
        } else {
            nextId = index + 1;
        }
        return setNowSongById(nextId);
    }

    /**
     * 上一首
     */
    public Song lastSong() {
        if (musicId <= 0) {
            musicId = musicList.size() - 1;
        } else {
            musicId--;
        }
        return setNowSongById(musicId);
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
        reloadSong();
        nowSong = musicList.get(id);
        return nowSong;
    }

    public void reloadSong() {
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

    public void tryPlay(List<Player> players) {
        if (stopMusic || nowSong == null)
            return;

        if (System.currentTimeMillis() - lastPlayed < 50 * nowSong.getDelay())
            return;

        musicTick++;
        if (musicTick > nowSong.getLength()) {
            nextSongByMode();
            return;
        }
        playTick(players, musicTick);
        lastPlayed = System.currentTimeMillis();
    }

    /**
     * 解析音轨并发声
     *
     * @param players 需要发声的玩家
     * @param tick    当前songTick
     */
    public void playTick(List<Player> players, int tick) {
        float vol = getVolume() / 100f;
        if (vol <= 0f) return;

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

            for (Player p : players) {
                try {
                    if (p == null)
                        continue;
                    if (sound != null) {
                        //
                        BlockEventPacket particlePk = new BlockEventPacket();
                        particlePk.x = p.getFloorX();
                        particlePk.y = p.getFloorY();
                        particlePk.z = p.getFloorZ();
                        particlePk.case1 = note.getInstrument();
                        particlePk.case2 = key33;
                        particlePk.tryEncode();

                        // 播放声音
                        PlaySoundPacket soundPk = new PlaySoundPacket();
                        soundPk.name = sound.getSound();
                        soundPk.volume = vol;
                        soundPk.pitch = fl;
                        soundPk.x = p.getFloorX();
                        soundPk.y = p.getFloorY();
                        soundPk.z = p.getFloorZ();
                        p.dataPacket(soundPk);
                        p.dataPacket(particlePk);

                    }
                } catch (Exception ignore) {
                }
            }
        }
    }

    public String songStat() {
        if (musicList.isEmpty()) {
            return "播放列表为空！请添加曲目";
        }

        String title = MusicPlus.ins.getConfig().getString("song_status_title");

        StringJoiner sj = new StringJoiner("\n", title, "");
        sj.add("");
        sj.add(playModeStat[playMode] + "模式");
        for (int i = 0; i < 10; i++) {
            if (i > musicList.size() - 1) {
                sj.add("none");
            } else {
                Song song = musicList.get(i);
                boolean isM = song.getSongName().equalsIgnoreCase(nowSong.getSongName());
                sj.add((isM ? TextFormat.BOLD + TextFormat.GREEN.toString() : TextFormat.RESET) + song.getSongName());
            }
        }

        // show par
        String playedTime = Progress.getFt(musicTick);
        String playedMaxTime = Progress.getFt(nowSong.getLength());
        String playedPar = Progress.addPar(musicTick, nowSong.getLength(), true);

        sj.add(playedTime + "/" + playedMaxTime + " " + playedPar);
        return sj.toString();
    }

    @Override
    public String toString() {
        return "MusicAPI{" +
                "nowSong=" + (nowSong == null ? "none" : nowSong.getSongName()) +
                ", stopMusic=" + stopMusic +
                ", musicList=" + musicList.size() +
                ", volume=" + volume +
                ", musicTick=" + musicTick +
                ", musicId=" + musicId +
                ", playMode=" + playMode +
                '}';
    }
}
