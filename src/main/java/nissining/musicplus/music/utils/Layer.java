package nissining.musicplus.music.utils;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class Layer {
    private HashMap<Integer, Note> hashMap = new HashMap<>();
    private byte volume = 100;
    private String name = "";

    public void setVolume(byte volume) {
        if (this.volume != volume) {
            this.volume = volume;
        }
    }

    public void setNote(int ticks, Note note) {
        this.hashMap.put(ticks, note);
    }

    public Note getNote(int tick) {
        return this.hashMap.get(tick);
    }
}
