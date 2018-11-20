package fans.skin.expresstrade.models;

import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.managers.logger.*;


public class OnEventModel {
    public Integer id;
    public Object object;
    public EventType code;

    public static final LogManager logManager = LogManager.getInstance();

    public OnEventModel(EventType code) {
        this.code = code;
        this.id = code.getId();
    }

    public OnEventModel(EventType code, Object object) {
        this.code = code;
        this.id = code.getId();
        this.object = object;
    }
}
