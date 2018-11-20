package fans.skin.expresstrade.models;

import java.util.*;

public class OfferModel {
    public static class Trade {
        public Offer offer;
        public Integer status;
    }

    public static class Offer {
        public Long id;
        public UserModel.User sender;
        public UserModel.User recipient;
        public Integer state;
        public String state_name;
        public Long time_created;
        public Long time_updated;
        public Long time_expires;
        public String message;
        public Boolean is_gift;
        public Boolean is_case_opening;
        public Boolean sent_by_you;
    }

    public static class Offers {
        public List<Offer> offers = new ArrayList<>();
        public Integer total;
    }
}