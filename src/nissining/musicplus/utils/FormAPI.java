package nissining.musicplus.utils;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * FormAPI By anseEND
 */

public abstract class FormAPI extends FormWindow {

    private boolean closed = false;

    public Player player;
    private FormWindow window;
    private HashMap<Integer, Object> hashMap = new HashMap<>();

    public FormAPI(Player player, FormWindow window) {
        this.player = player;
        this.window = window;
    }

    public void setHashMap(HashMap<Integer, Object> hashMap) {
        this.hashMap = hashMap;
    }

    public HashMap<Integer, Object> getHashMap() {
        return this.hashMap;
    }

    public int getButtonId() {
        return Integer.parseInt(String.valueOf(this.hashMap.get(0)));
    }

    public String getButtonText() {
        return String.valueOf(this.hashMap.get(1));
    }

    public void sendToPlayer(Player player) {
        player.showFormWindow(this);
    }

    /**
     * 处理数据
     */
    public void setResponse(String data) {
        if (data.equals("null")) {
            this.closed = true;
        } else {
            this.window.setResponse(data);
            FormResponse response = this.window.getResponse();

            if (response instanceof FormResponseCustom) {
                /*
                 * ElementSlider Float
                 * ElementToggle Boolean
                 * 其他Element都是: String
                 */
                this.hashMap = ((FormResponseCustom) response).getResponses();
            } else if (response instanceof FormResponseModal) {
                this.hashMap.put(0, ((FormResponseModal) response).getClickedButtonId());
                this.hashMap.put(1, ((FormResponseModal) response).getClickedButtonText());
            } else if (response instanceof FormResponseSimple) {
                ElementButton elementButton = ((FormResponseSimple) response).getClickedButton();
                this.hashMap.put(0, ((FormResponseSimple) response).getClickedButtonId());
                this.hashMap.put(1, elementButton.getText());
            }

            try {
                this.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void call();

    public FormResponse getResponse() {
        return this.window.getResponse();
    }

    @Override
    public String getJSONData() {
        return (new Gson()).toJson(this.window);
    }

    @Override
    public boolean wasClosed() {
        return this.closed;
    }
}
