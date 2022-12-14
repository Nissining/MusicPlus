package nissining.musicplus;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkUnloadEvent;
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
import nissining.musicplus.music.MusicAPI;
import nissining.musicplus.music.MusicTask;
import nissining.musicplus.music.utils.Song;
import nissining.musicplus.player.MusicPlayer;
import nissining.musicplus.player.MusicPlayerMenu;

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
    public List<String> musicWorlds;

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
            put("song_status_title", "--- MusicPlus MusicList ---");
            put("song_status_maxShow", 10);
            put("play_mode", 3);
        }});
        this.musicWorlds = config.getStringList("music_worlds");

        this.creSongStatus();

        this.musicAPI = new MusicAPI();
        // ?????????MusicAPI
        this.musicAPI.loadAllSong(new File(getDataFolder(), "/music"));
        this.musicAPI.init(config.getInt("play_mode"));

        this.startPlay();

        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean isInMusicWorld(Player player) {
        if (musicWorlds.isEmpty())
            return true;
        for (String musicWorld : musicWorlds) {
            if (player.level.getFolderName().equalsIgnoreCase(musicWorld))
                return true;
        }
        return false;
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

    private static final String[] oplabel = new String[]{
            "spawn", "next", "last", "play", "mode", "add", "reload", "reloadSong"
    };

    private boolean isOpLabel(String label) {
        for (String s : oplabel) {
            if (s.equalsIgnoreCase(label))
                return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender se, Command command, String s, String[] args) {
        String t = "";
        if (args.length >= 1) {

            if (isOpLabel(args[0]) && !se.isOp()) {
                t = "??????OP??????";

            } else {
                switch (args[0]) {
                    case "help":
                        StringJoiner sj = new StringJoiner("\n- ", "--- MusicPlus HelpList ---", "");
                        sj.add("")
                                .add("/mplus <args> - ????????????")
                                .add("")
                                .add("-------- args --------")
                                .add("spawn - ??????SongStatus")
                                .add("next - ?????????")
                                .add("last - ?????????")
                                .add("play <id> - ????????????")
                                .add("mode <1|2|3> - ????????????(1=?????????????????? 2=?????????????????? 3=????????????)")
                                .add("my - ??????????????????")
                                .add("add - ??????15s")
                                .add("")
                                .add("reload - ???????????????")
                                .add("reloadSong - ??????????????????")
                                .add("----------------------")
                                .add("Plugin By Nissining!");

                        t = sj.toString();

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
                    case "play": // mplus play <page> <0-9>
                        if (args.length == 2) {
                            Song song = musicAPI.setNowSongById(Integer.parseInt(args[1]));
                            if (song == null) {
                                t = "???????????????Song????????????";
                            } else {
                                t = "????????????Song: " + song.getSongName();
                            }
                        }
                        break;
                    case "next":
                        if (musicAPI.nextSong()) {
                            t = "???????????????????????? " + musicAPI.getNowSongName();
                        } else {
                            t = "??????????????????????????????????????????????????????";
                        }
                        break;
                    case "last":
                        if (musicAPI.lastSong()) {
                            t = "???????????????????????? " + musicAPI.getNowSongName();
                        } else {
                            t = "??????????????????????????????????????????????????????";
                        }
                        break;
                    case "mode":
                        if (args.length == 2) {
                            t = "????????????????????????" + musicAPI.setPlayMode(Integer.parseInt(args[1]));
                        }
                        break;
                    case "reload":
                        this.config = new Config(getDataFolder() + "/config.yml", 2);
                        t = "config reloaded!";
                        break;
                    case "reloadSong":
                        musicAPI.loadAllSong(new File(getDataFolder(), "music"));
                        musicAPI.init(config.getInt("play_mode"));
                        t = "????????????????????????";
                        break;
                    case "my": // ui
                        MusicPlayerMenu.openMenu((Player) se);
                        break;
                    case "add":
                        musicAPI.addMusicTick((short) 15);
                        t = "??????15s";
                        break;
                    case "addI":
                        musicAPI.addMusicTick(Short.parseShort(args[1]));
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
    public void onloadChunk(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities().values()) {
            if (entity instanceof SongStatus) {
                event.setCancelled();
                break;
            }
        }
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
