package fans.skin.expresstrade.models;

public class MakeOfferModel {
    // *** Recipient
    public Long recipient_ops_id;
    public InventoryModel recipient_inventory;

    // *** Sender
    public Long sender_ops_id;
    public InventoryModel sender_inventory;
}
