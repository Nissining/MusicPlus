package nissining.musicplus;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.MainLogger;
import nissining.musicplus.entity.SongStatus;
import nissining.musicplus.music.MusicApi;
import nissining.musicplus.music.MusicTask;
import nissining.musicplus.music.utils.Song;
import nissining.musicplus.player.MusicPlayer;
import nissining.musicplus.player.MusicPlayerMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.*;

/**
 * @author Nissining
 **/
public class MusicPlus extends PluginBase implements Listener {

    private Config config;
    public static final ScheduledThreadPoolExecutor SCHEDULED = new ScheduledThreadPoolExecutor(10);
    public MusicApi musicApi;
    public List<String> musicWorlds;

    public static MusicPlus ins;

    public static MusicPlus getInstance() {
        return ins;
    }

    @Override
    public void onLoad() {
        ins = this;
    }

    @Override
    public void onEnable() {
        if (getDataFolder().mkdirs()) {
            getLogger().notice("MusicPlus By Nissining");
        }
        if (new File(getDataFolder(), "music").mkdirs()) {
            getLogger().warning("music file");
        }

        this.config = new Config(getDataFolder() + "/config.yml", 2, new ConfigSection() {{
            put("music_worlds", new ArrayList<String>());
            put("song_status_pos", new ArrayList<Double>());
            put("song_status_level", "");
            put("song_status_title", "--- MusicPlus MusicList ---");
            put("song_status_maxShow", 10);
            put("play_mode", 3);
        }});
        this.musicWorlds = config.getStringList("music_worlds");

        this.creSongStatus();

        this.musicApi = new MusicApi();
        // 初始化MusicAPI
        this.musicApi.loadAllSong(new File(getDataFolder(), "/music"));
        this.musicApi.init(config.getInt("play_mode"));

        this.startPlay();

        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean isInMusicWorld(Player player) {
        if (musicWorlds.isEmpty()) {
            return true;
        }
        for (String musicWorld : musicWorlds) {
            if (player.level.getFolderName().equalsIgnoreCase(musicWorld)) {
                return true;
            }
        }
        return false;
    }

    private void startPlay() {
        MusicTask musicTask = new MusicTask(this);
        SCHEDULED.scheduleWithFixedDelay(musicTask, 0, 15, TimeUnit.MILLISECONDS);
    }

    private void creSongStatus() {
        List<Double> pos = config.getDoubleList("song_status_pos");
        Level level = getServer().getLevelByName(config.getString("song_status_level"));
        if (pos.isEmpty() || level == null) {
            return;
        }
        Position position = new Position(pos.get(0), pos.get(1) + 3, pos.get(2), level);
        CompoundTag nbt = Entity.getDefaultNBT(position);
        SongStatus songStatus = new SongStatus(position.getChunk(), nbt);
        songStatus.spawnToAll();
    }

    private static final String[] OPLABEL = new String[]{
            "spawn", "next", "last", "play", "mode", "add", "reload", "reloadSong"
    };

    private boolean isOpLabel(String label) {
        for (String s : OPLABEL) {
            if (s.equalsIgnoreCase(label)) {
                return true;
            }
        }
        return false;
    }

    public static final StringJoiner HELP_LIST = new StringJoiner("\n- ",
            "--- MusicPlus HelpList ---", "Plugin by Nissining")
            .add("")
            .add("/mplus <args> - 主要命令")
            .add("")
            .add("-------- args --------")
            .add("spawn - 创建SongStatus")
            .add("next - 下一首")
            .add("last - 上一首")
            .add("play <id> - 指定播放")
            .add("mode <1|2|3> - 切换模式(1=列表循环播放 2=列表顺序播放 3=随机播放)")
            .add("my - 打开个人设置")
            .add("add - 快进15s")
            .add("")
            .add("reload - 热重载配置")
            .add("reloadSong - 重载音乐列表")
            .add("----------------------");

    @Override
    public boolean onCommand(CommandSender se, Command command, String s, String[] args) {
        String t = "";
        if (args.length >= 1) {

            if (isOpLabel(args[0]) && !se.isOp()) {
                t = "需要OP权限";

            } else {
                switch (args[0]) {
                    default:
                    case "help":
                        t = HELP_LIST.toString();
                        break;
                    case "spawn":
                        if (se instanceof Player) {
                            Player p = (Player) se;
                            List<Double> pos = new ArrayList<>() {{
                                add(p.getX());
                                add(p.getY());
                                add(p.getZ());
                            }};
                            config.set("song_status_pos", pos);
                            config.set("song_status_level", p.level.getFolderName());
                            config.save();
                            creSongStatus();
                            t = "Song status created and saved!";
                        }
                        break;
                    // mplus play <page> <0-9>
                    case "play":
                        if (args.length == 2) {
                            Song song = musicApi.setNowSongById(Integer.parseInt(args[1]));
                            if (song == null) {
                                t = "播放失败！Song不存在！";
                            } else {
                                t = "已切换为Song: " + song.getSongName();
                            }
                        }
                        break;
                    case "next":
                        if (musicApi.nextSong()) {
                            t = "已切换到下一首！ " + musicApi.getNowSongName();
                        } else {
                            t = "下一首失败！可能是列表顺序播放导致！";
                        }
                        break;
                    case "last":
                        if (musicApi.lastSong()) {
                            t = "已切换到上一首！ " + musicApi.getNowSongName();
                        } else {
                            t = "上一首失败！可能是列表顺序播放导致！";
                        }
                        break;
                    case "mode":
                        if (args.length == 2) {
                            t = "播放模式切换为：" + musicApi.setPlayMode(Integer.parseInt(args[1]));
                        }
                        break;
                    case "reload":
                        this.config = new Config(getDataFolder() + "/config.yml", 2);
                        t = "config reloaded!";
                        break;
                    case "reloadSong":
                        musicApi.loadAllSong(new File(getDataFolder(), "music"));
                        musicApi.init(config.getInt("play_mode"));
                        t = "已重载音乐列表！";
                        break;
                    case "my":
                        MusicPlayerMenu.openMenu((Player) se);
                        break;
                    case "add":
                        musicApi.addMusicTick((short) 15);
                        t = "快进15s";
                        break;
                    case "addI":
                        musicApi.addMusicTick(Short.parseShort(args[1]));
                        break;
                }
            }

        } else {
            t = "Usage: /mplus help";
        }

        if (!t.isEmpty()) {
            se.sendMessage(t);
        }

        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isInMusicWorld(player)) {
            MusicPlayer.addMusicPlayer(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MusicPlayer.removeMusicPlayer(event.getPlayer());
    }

    public static void debug(String debug) {
        MainLogger.getLogger().notice(debug);
    }
}
