package fans.skin.expresstrade.view.fragments;

import android.os.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;

import static fans.skin.expresstrade.view.AppButton.*;

public class MakeOfferDialogFragment extends AppDialogFragment implements View.OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    private ImageView iv_recipient_avatar;
    private TextView tv_recipient_name;
    private AppButton bt_recipient_view;
    private AppButton bt_close;
    private AppButton bt_partner_selected_view;
    private AppButton bt_mine_selected_view;
    private LinearLayout bt_offer_accept;
    private LinearLayout content;

    private LinearLayout ll_rec_not_items_cap;
    private LinearLayout ll_rec_items_info;
    private LinearLayout ll_mine_not_items_cap;
    private LinearLayout ll_mine_items_info;

    private TextView tv_rec_select_cost;
    private TextView tv_mine_select_cost;

    private TextView tv_rec_items_count;
    private TextView tv_rec_keys_count;
    private TextView tv_mine_items_count;
    private TextView tv_mine_keys_count;

    private LinearLayout ll_rec_items;
    private LinearLayout ll_rec_keys;
    private LinearLayout ll_mine_items;
    private LinearLayout ll_mine_keys;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private MakeOfferModel offer;

    private InventoryDialogFragment userInventoryDialog;

    private Handler handler = new Handler();

    private boolean isOpeningInventory;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public AppFragment.FragmentName getFragmentName() {
        return AppFragment.FragmentName.MAKE_OFFER_DIALOG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_offer_make, container, false);

        iv_recipient_avatar = view.findViewById(R.id.iv_recipient_avatar);
        tv_recipient_name = view.findViewById(R.id.tv_recipient_name);
        bt_recipient_view = view.findViewById(R.id.bt_recipient_view);

        bt_partner_selected_view = view.findViewById(R.id.bt_partner_selected_view);
        bt_mine_selected_view = view.findViewById(R.id.bt_mine_selected_view);

        ll_rec_not_items_cap = view.findViewById(R.id.ll_rec_not_items_cap);
        ll_rec_items_info = view.findViewById(R.id.ll_rec_items_info);
        tv_rec_select_cost = view.findViewById(R.id.tv_rec_select_cost);

        ll_mine_not_items_cap = view.findViewById(R.id.ll_mine_not_items_cap);
        ll_mine_items_info = view.findViewById(R.id.ll_mine_items_info);
        tv_mine_select_cost = view.findViewById(R.id.tv_mine_select_cost);

        tv_rec_items_count = view.findViewById(R.id.tv_rec_items_count);
        tv_rec_keys_count = view.findViewById(R.id.tv_rec_keys_count);
        tv_mine_items_count = view.findViewById(R.id.tv_mine_items_count);
        tv_mine_keys_count = view.findViewById(R.id.tv_mine_keys_count);

        ll_rec_items = view.findViewById(R.id.ll_rec_items);
        ll_rec_keys = view.findViewById(R.id.ll_rec_keys);
        ll_mine_items = view.findViewById(R.id.ll_mine_items);
        ll_mine_keys = view.findViewById(R.id.ll_mine_keys);

        bt_offer_accept = view.findViewById(R.id.bt_offer_accept);
        content = view.findViewById(R.id.content);

        bt_close = view.findViewById(R.id.bt_close);
        bt_close.setSize(ButtonSize.SMALL);
        bt_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Закрываем диалог
                getDialog().dismiss();
            }
        });

        bt_partner_selected_view.setSize(ButtonSize.SQUARE);
        bt_mine_selected_view.setSize(ButtonSize.SQUARE);

        bt_recipient_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOpeningInventory) return;
                isOpeningInventory = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isOpeningInventory = false;
                    }
                }, 1000);

                // Открываем окно инвентаря пользователя
                userInventoryDialog = App.dialogViewer.openUserInventory(getChildFragmentManager(), offer.recipient_ops_id);
            }
        });

        bt_partner_selected_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpeningInventory) return;
                isOpeningInventory = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isOpeningInventory = false;
                    }
                }, 1000);
                selectedInventoryViewItems(App.tradeModule.makeOffer.recipient_inventory, R.string.title_offer_make_get);
            }
        });

        bt_mine_selected_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpeningInventory) return;
                isOpeningInventory = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isOpeningInventory = false;
                    }
                }, 1000);
                selectedInventoryViewItems(App.tradeModule.makeOffer.sender_inventory, R.string.title_offer_make_give);
            }
        });

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);

        bt_offer_accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                offer = App.tradeModule.makeOffer;
                if (offer == null) return;

                // Нет никаких товаров для создания оффера
                if (!isMakeExist()) {
                    Toast.makeText(App.context, "No items or keys selected.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AcceptModel model = new AcceptModel();
                model.isMakeOffer = true;

                App.eventManager.doEvent(EventType.ON_DO_2FA_ACCEPT, model);
                dismiss();
            }
        });

        // Рендерим инфу
        render();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    private boolean isMakeExist() {
        return offer.recipient_inventory.itemsSelected.size() != 0 ||
                offer.sender_inventory.itemsSelected.size() != 0 ||
                offer.recipient_inventory.keysSelected != 0 ||
                offer.sender_inventory.keysSelected != 0;
    }

    public void render() {
        offer = App.tradeModule.makeOffer;

        // Получаем объект получателя
        UsersTable recipient = App.usersModule.users.get(offer.recipient_ops_id);
        if (recipient == null) return;

        Picasso.with(getContext())
                .load(CommonUtils.getAvatar(recipient.avatar))
                .resize((int) (40 * App.display.density), (int) (40 * App.display.density))
                .into(iv_recipient_avatar);

        tv_recipient_name.setText(StringUtils.truncate(recipient.name, 17));

        // RECIPIENT
        bt_recipient_view.setSize(ButtonSize.SMALL);

        boolean isRecItems = offer.recipient_inventory.itemsSelected.size() != 0 ||
                offer.recipient_inventory.keysSelected != 0;

        ll_rec_not_items_cap.setVisibility(isRecItems ? GONE : VISIBLE);
        ll_rec_items_info.setVisibility(isRecItems ? VISIBLE : GONE);

        tv_rec_select_cost.setText(!isRecItems ? "$0.00" : CommonUtils.getFormattedAmount(App.accountModule.getCostSelectedInventory(offer.recipient_inventory)));
        tv_rec_select_cost.setTextColor(getResources().getColor(isRecItems ? R.color.gold : R.color.gray));

//        int rec_items_count = offer.recipient_inventory.itemsSelected.size();
//        int rec_keys_count = offer.recipient_inventory.keysSelected;

        tv_rec_items_count.setText(offer.recipient_inventory.itemsSelected.size() + "");
        tv_rec_keys_count.setText(offer.recipient_inventory.keysSelected + "");

        ll_rec_items.setVisibility(offer.recipient_inventory.itemsSelected.size() != 0 ? VISIBLE : GONE);
        ll_rec_keys.setVisibility(offer.recipient_inventory.keysSelected != 0 ? VISIBLE : GONE);


        // MINE
        boolean isMineItems = offer.sender_inventory.itemsSelected.size() != 0 ||
                offer.sender_inventory.keysSelected != 0;

        ll_mine_not_items_cap.setVisibility(isMineItems ? GONE : VISIBLE);
        ll_mine_items_info.setVisibility(isMineItems ? VISIBLE : GONE);

        tv_mine_select_cost.setText(!isMineItems ? "$0.00" : CommonUtils.getFormattedAmount(App.accountModule.getCostSelectedInventory(offer.sender_inventory)));
        tv_mine_select_cost.setTextColor(getResources().getColor(isMineItems ? R.color.gold : R.color.gray));

        tv_mine_items_count.setText(offer.sender_inventory.itemsSelected.size() + "");
        tv_mine_keys_count.setText(offer.sender_inventory.keysSelected + "");

        ll_mine_items.setVisibility(offer.sender_inventory.itemsSelected.size() != 0 ? VISIBLE : GONE);
        ll_mine_keys.setVisibility(offer.sender_inventory.keysSelected != 0 ? VISIBLE : GONE);

        bt_offer_accept.setBackgroundResource(isMakeExist() ? R.drawable.bg_button_big_green : R.drawable.bg_button_red_dark);
    }

    // Сформировать объект предметов и визуализировать его во фрагменте
    private void selectedInventoryViewItems(InventoryModel inventory, int title) {
        List<ItemModel.Item> items = new ArrayList<>();

        App.logManager.debug("selectedInventoryViewItems " + inventory.items.size() + " " + inventory.itemsSelected.size());

        // Обходим все выбранные товары
        for (Long id : inventory.itemsSelected) {
            ItemModel.Item item = inventory.items.get(id);

            App.logManager.debug("TEST FOR " + item);

            if (item == null) continue;

            // Добавляем предмет
            items.add(item);
        }

        // Сортируем массив по цене
        Collections.sort(items, new Comparator<ItemModel.Item>() {
            @Override
            public int compare(ItemModel.Item o1, ItemModel.Item o2) {
                return o2.suggested_price - o1.suggested_price;
            }
        });

        if (inventory.keysSelected != 0) {
            ItemModel.Item model = App.accountModule.getKeyItem(inventory.keys);
            ItemModel.Item key = new ItemModel.Item();
            key.id = model.id;
            key.type = model.type;
            key.sku = 1;
            key.suggested_price = inventory.keyPrice;
            key.count = inventory.keysSelected;
            key.image = model.image;
            key.name = model.name;
            key.color = model.color;

            items.add(0, key);
        }

        App.dialogViewer.viewItems(getChildFragmentManager(), items, title);
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public void onEvent(OnEventModel event) {
        super.onEvent(event);

        switch ((event.id)) {
            // ON_DO_CLOSE_USER_INVENTORY
            case 5270127:
                if (userInventoryDialog == null) return;

                userInventoryDialog.dismiss();
                render();
                break;
        }
    }

    @Override
    public void onClick(View v) {
    }
}
