package nissining.musicplus.music;

import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;

import cn.nukkit.utils.TextFormat;
import nissining.musicplus.MusicPlus;
import nissining.musicplus.music.utils.Layer;
import nissining.musicplus.music.utils.NBSDecoder;
import nissining.musicplus.music.utils.Note;
import nissining.musicplus.music.utils.Song;
import nissining.musicplus.player.MusicPlayer;
import nissining.musicplus.utils.MyUtils;
import nissining.musicplus.utils.PageBean;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nissining
 * @date 2022/9/25 PM 2:45
 **/
public class MusicApi {

    public Song nowSong = null;
    public short musicTick = -1;
    public long lastPlayed = 0;
    public int musicId = 0;
    public int playMode = 0;

    public List<Song> musicList = new ArrayList<>();

    public static String[] playModeStat = new String[]{
            "列表顺序", "列表循环", "单曲循环", "随机"
    };

    public MusicApi() {
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
        if (musicFiles == null) {
            return;
        }
        File[] files = Objects.requireNonNull(musicFiles.listFiles(),
                "music文件夹不存在！请手动添加！");
        musicList = Arrays.stream(files)
                .map(file -> {
                    String fn = file.getName().trim();
                    boolean isTrack = !file.isDirectory() && fn.endsWith(".nbs");
                    if (isTrack) {
                        return NBSDecoder.parse(file);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        MusicPlus.debug("已加载共计 " + musicList.size() + " 首 Track!");
    }

    /**
     * 初始化Music
     *
     * @param mode 0:列表顺序 1:列表循环 2:单曲循环 3:随机
     */
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
            // 列表顺序播放
            if (playMode == 0) {
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
        musicId = MyUtils.rand(0, musicList.size() - 1);
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
            // 列表循环播放
            // 列表顺序播放
            case 1:
            case 0:
            default:
                nextSong();
                break;
            // 单曲循环
            case 2:
                setNowSongById(this.musicId);
                break;
            // 随机播放
            case 3:
                randomSong();
                break;
        }
    }

    public void setMusicTick(short i) {
        this.musicTick = i;
    }

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

    public static final HashMap<Integer, Sound> SOUNDS = new HashMap<>() {{
        put(0, Sound.NOTE_HARP);
        put(1, Sound.NOTE_BASS);
        put(2, Sound.NOTE_BD);
        put(3, Sound.NOTE_SNARE);
        put(4, Sound.NOTE_HAT);
        put(5, Sound.NOTE_GUITAR);
        put(6, Sound.NOTE_FLUTE);
        put(7, Sound.NOTE_BELL);
        put(8, Sound.NOTE_CHIME);
        put(9, Sound.NOTE_XYLOPHONE);
    }};

    private static final HashMap<Integer, Float> KEYS = new HashMap<>() {{
        put(0, 0.5f);
        put(1, 0.529732f);
        put(2, 0.561231f);
        put(3, 0.594604f);
        put(4, 0.629961f);
        put(5, 0.667420f);
        put(6, 0.707107f);
        put(7, 0.749154f);
        put(8, 0.793701f);
        put(9, 0.840896f);
        put(10, 0.890899f);
        put(11, 0.943874f);
        put(12, 1.0f);
        put(13, 1.059463f);
        put(14, 1.122462f);
        put(15, 1.189207f);
        put(16, 1.259921f);
        put(17, 1.334840f);
        put(18, 1.414214f);
        put(19, 1.498307f);
        put(20, 1.587401f);
        put(21, 1.681793f);
        put(22, 1.781797f);
        put(23, 1.887749f);
        put(24, 2.0f);
    }};

    public void playTick(List<MusicPlayer> mps, int tick) {
        for (Layer l : nowSong.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) {
                continue;
            }
            Sound sound = SOUNDS.getOrDefault((int) note.getInstrument(), null);
            float fl = KEYS.getOrDefault(note.getKey() - 33, 0F);
            mps.stream()
                    .filter(mp -> !mp.isStopMusic())
                    .map(MusicPlayer::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(p -> {
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
                    });
        }
    }

    public String songStat() {
        if (musicList.isEmpty()) {
            return TextFormat.RED + "你的播放列表是空的！";
        }
        if (nowSong == null) {
            return TextFormat.RED + "没有正在播放的音乐!";
        }

        PageBean<Song> pageBean = new PageBean<>();
        int maxShow = MusicPlus.getInstance().getConfig().getInt("song_status_maxShow");
        int index = (musicList.indexOf(nowSong) / maxShow) + 1;
        List<Song> queryPager = pageBean.queryPager(index, maxShow, musicList);
        if (queryPager.isEmpty()) {
            return TextFormat.RED + "不存在的页数！";
        }

        String title = MusicPlus.ins.getConfig().getString("song_status_title");
        StringJoiner sj = new StringJoiner("\n", title + "\n", "");
        sj.add(index + "/" + pageBean.getTotalPages());
        sj.add(playModeStat[playMode] + "模式");

        for (int i = 0; i < maxShow; i++) {
            String name = "none";
            if (i < queryPager.size()) {
                Song song = queryPager.get(i);
                name = song.getFormatSongName(getNowSongName());
            }
            sj.add((i + 1) + "." + name);
        }

        // show par
        String playedTime = MyUtils.getFt(musicTick);
        String playedMaxTime = MyUtils.getFt(nowSong.getLength());
        String playedPar = MyUtils.addPar(musicTick, nowSong.getLength(), true);

        sj.add(playedTime + "/" + playedMaxTime + " " + playedPar);
        return sj.toString();
    }

}
