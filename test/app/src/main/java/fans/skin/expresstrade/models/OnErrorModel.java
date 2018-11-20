package fans.skin.expresstrade.models;

import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.managers.logger.*;

public class OnErrorModel {
    public EventType error_code;
    public Object data;

    public static final LogManager logManager = LogManager.getInstance();

    public OnErrorModel(EventType error_code) {
        this.error_code = error_code;
    }

    public OnErrorModel(EventType error_code, Object data) {
        this.error_code = error_code;
        this.data = data;
    }
}
