package nissining.musicplus;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.MainLogger;
import nissining.musicplus.entity.SongStatus;
import nissining.musicplus.music.MusicAPI;
import nissining.musicplus.music.MusicTask;
import nissining.musicplus.music.utils.Song;
import nissining.musicplus.utils.MessageSend;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicPlus extends PluginBase implements Listener {

    public static String pluginName = "MusicPlus";

    private Config config;

    private final ExecutorService service = Executors.newWorkStealingPool();

    public MusicAPI musicAPI;

    public static MusicPlus ins;

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
            put("play_mode", 3);
            put("song_status_title", "--- MusicPlus MusicList ---");
        }});

        this.creSongStatus();

        this.musicAPI = new MusicAPI();
        // 初始化MusicAPI
        this.musicAPI.loadAllSong(new File(getDataFolder(), "/music"));
        this.musicAPI.init(config.getInt("play_mode"));

        this.startPlay();

        getServer().getPluginManager().registerEvents(this, this);

        MessageSend.sendMail();
    }

    public List<Player> getSongPlayers() {
        List<String> musicWorlds = config.getStringList("music_worlds");

        if (musicWorlds.isEmpty()) {
            return new ArrayList<>(getServer().getOnlinePlayers().values());
        } else {
            List<Player> ps = new ArrayList<>();
            for (String ln : musicWorlds) {
                Level level = getServer().getLevelByName(ln);
                if (level != null) {
                    ps.addAll(level.getPlayers().values());
                }
            }
            return ps;
        }

    }

    private void startPlay() {
        service.execute(() -> new MusicTask(this).start());
    }

    private void creSongStatus() {
        List<Double> pos = config.getDoubleList("song_status_pos");
        Level level = getServer().getLevelByName(config.getString("song_status_level"));
        if (pos.isEmpty() || level == null)
            return;

        Position position = new Position(pos.get(0), pos.get(1) + 3, pos.get(2), level);
        CompoundTag nbt = Entity.getDefaultNBT(position);
        SongStatus songStatus = new SongStatus(position.getChunk(), nbt);
        songStatus.spawnToAll();
    }


    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender se, Command command, String s, String[] args) {
        // /rmp spawn
        if (args.length >= 1) {
            switch (args[0]) {
                case "help":
                    StringJoiner sj = new StringJoiner("\n- ", "--- MusicPlus HelpList ---", "");
                    sj.add("")
                            .add("/mplus <args> - 主要命令")
                            .add("")
                            .add("args:")
                            .add("spawn - 创建SongStatus")
                            .add("next - 下一首")
                            .add("last - 上一首")
                            .add("play <id> - 指定播放")
                            .add("mode <1|2|3> - 切换模式(1=列表循环播放 2=列表顺序播放 3=随机播放)")
                            .add("reload - 热重载配置")
                            .add("reloadSong - 重载音乐列表")
                            .add("")
                            .add("Plugin By Nissining!");

                    se.sendMessage(sj.toString());
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
                        p.sendMessage("Song status created and saved!");
                    }
                    break;
                case "play": // mplus play <page> <0-9>
                    if (args.length == 2) {
                        Song song = musicAPI.setNowSongById(Integer.parseInt(args[1]));
                        se.sendMessage("已切换为Song: " + song.getSongName());
                    }
                    break;
                case "next":
                    se.sendMessage("已切换到下一首！" + musicAPI.nextSong().getSongName());
                    break;
                case "last":
                    se.sendMessage("已切换到上一首！" + musicAPI.lastSong().getSongName());
                    break;
                case "mode":
                    if (args.length == 2) {
                        se.sendMessage("播放模式切换为：" + musicAPI.setPlayMode(Integer.parseInt(args[1])));
                    }
                    break;
                case "reload":
                    this.config = new Config(getDataFolder() + "/config.yml", 2);
                    se.sendMessage("config reloaded!");
                    break;
                case "reloadSong":
                    musicAPI.loadAllSong(new File(getDataFolder(), "music"));
                    musicAPI.init(config.getInt("play_mode"));
                    se.sendMessage("已重载音乐列表！");
                    break;
            }
        } else {
            return false;
        }
        return true;
    }

    @EventHandler
    public void onloadChunk(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities().values()) {
            if (entity instanceof SongStatus) {
                event.setCancelled();
                break;
            }
        }
    }

    public static void debug(String debug) {
        MainLogger.getLogger().notice(debug);
    }
}
