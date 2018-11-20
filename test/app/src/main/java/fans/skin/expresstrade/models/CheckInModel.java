package fans.skin.expresstrade.models;

public class CheckInModel {
    public Notify notify;

    public static class Notify {
        public Boolean always;
        public String image;
        public String link;
        public String title;
        public String titleSize;
        public String message;
        public String messageSize;
    }
}
