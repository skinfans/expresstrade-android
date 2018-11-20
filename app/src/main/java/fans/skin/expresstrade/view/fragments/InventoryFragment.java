package fans.skin.expresstrade.view.fragments;

import android.annotation.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.managers.pref.*;
import fans.skin.expresstrade.managers.pref.PrefManager.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.modules.trade.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.containers.*;
import fans.skin.expresstrade.view.viewers.*;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static fans.skin.expresstrade.managers.event.EventManager.*;

public class InventoryFragment extends AppFragment implements View.OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    private AppButton bt_filter;
    private AppButton bt_select_all;
    private AppButton bt_cancel;

    private AppSwipeRefreshLayout swipeRefresh;
    private AppRecyclerView recyclerView;
    private ItemsContainer itemsContainer;
    private TextView tv_keys_total;
    private TextView tv_items_count;
    private TextView tv_select_count;
    private TextView tv_keys_select_count;

    private LinearLayout ll_title;
    private LinearLayout ll_select_title;

    private ImageView bt_filter_gold;
    private ImageView bt_filter_red;
    private ImageView bt_filter_pink;
    private ImageView bt_filter_purple;
    private ImageView bt_filter_blue;

    private RelativeLayout select_keys_minus;
    private RelativeLayout select_keys_plus;

    private LinearLayout panel_filter;
    private LinearLayout bt_convert;
    private LinearLayout ll_panel_keys;
    private LinearLayout bt_add_to_offer;

    private ProgressBar pb; // progressBar
    private ImageView panel_keys_scroll;
    private ImageView panel_keys_bar;
    private LinearLayout ll_recipient;
    private TextView tv_recipient_name;
    private ImageView iv_recipient_avatar;
    private ImageView bt_recipient_close;
    private ImageView cap_no_items;
    private ImageView iv_bg_keys;
    private TextView tv_inv_title;
    private ImageView iv_inv_icon;

    public LinearLayout bt_create_offer;

    // USER
    private LinearLayout ll_user;
    private ImageView iv_user_avatar;
    private TextView tv_user_name;
    private AppButton bt_user_back;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Handler handler = new Handler();

    // Инвентарь для отображения и работы с предметами
    private InventoryModel inventory = App.accountModule.inventory;

    private UsersTable recipientUser = null;

    private ArrayList<String> rarityExceptions = new ArrayList<>();

    private boolean isPanelFiltersVisible = true;

    private boolean isScrollKeysActive = false;

    private float scrollKeysPosition = 0;

    private boolean isLoading = false;

    public boolean isOfferAvailable = false;

    public boolean isConvertAvailable = false;

    public boolean isUser = false;

    private boolean isMakeOfferSelected = false;

    private boolean isOpeningMakeOffer = false;

    private Integer appId = 1;

    private Integer itemsCount = 0;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public FragmentName getFragmentName() {
        return FragmentName.INVENTORY;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_inventory, container, false);

        bt_filter = view.findViewById(R.id.bt_filter);
        bt_select_all = view.findViewById(R.id.bt_select_all);
        bt_cancel = view.findViewById(R.id.bt_cancel);

        swipeRefresh = view.findViewById(R.id.body);
        recyclerView = view.findViewById(R.id.list_items);
        tv_keys_total = view.findViewById(R.id.tv_keys_total);
        tv_items_count = view.findViewById(R.id.tv_items_count);

        bt_filter_gold = view.findViewById(R.id.bt_filter_gold);
        bt_filter_red = view.findViewById(R.id.bt_filter_red);
        bt_filter_pink = view.findViewById(R.id.bt_filter_pink);
        bt_filter_purple = view.findViewById(R.id.bt_filter_purple);
        bt_filter_blue = view.findViewById(R.id.bt_filter_blue);
        bt_convert = view.findViewById(R.id.bt_convert);
        panel_filter = view.findViewById(R.id.panel_filter);
        tv_select_count = view.findViewById(R.id.tv_select_count);

        ll_title = view.findViewById(R.id.ll_title);
        ll_select_title = view.findViewById(R.id.ll_select_title);

        ll_panel_keys = view.findViewById(R.id.ll_panel_keys);

        pb = view.findViewById(R.id.pb);
        panel_keys_scroll = view.findViewById(R.id.panel_keys_scroll);
        panel_keys_bar = view.findViewById(R.id.panel_keys_bar);
        tv_keys_select_count = view.findViewById(R.id.tv_keys_select_count);
        select_keys_minus = view.findViewById(R.id.select_keys_minus);
        select_keys_plus = view.findViewById(R.id.select_keys_plus);

        ll_recipient = view.findViewById(R.id.ll_recipient);
        tv_recipient_name = view.findViewById(R.id.tv_recipient_name);
        iv_recipient_avatar = view.findViewById(R.id.iv_recipient_avatar);
        bt_recipient_close = view.findViewById(R.id.bt_recipient_close);
        bt_add_to_offer = view.findViewById(R.id.bt_add_to_offer);
        iv_bg_keys = view.findViewById(R.id.iv_bg_keys);
        tv_inv_title = view.findViewById(R.id.tv_inv_title);
        iv_inv_icon = view.findViewById(R.id.iv_inv_icon);

        cap_no_items = view.findViewById(R.id.cap_no_items);

        // User
        ll_user = view.findViewById(R.id.ll_user);
        iv_user_avatar = view.findViewById(R.id.iv_user_avatar);
        tv_user_name = view.findViewById(R.id.tv_user_name);
        bt_user_back = view.findViewById(R.id.bt_user_back);

        bt_filter.setSize(AppButton.ButtonSize.SMALL);
        bt_select_all.setSize(AppButton.ButtonSize.SMALL);
        bt_cancel.setSize(AppButton.ButtonSize.SMALL);
        bt_user_back.setSize(AppButton.ButtonSize.SMALL);

        Picasso.with(App.context)
                .load("https://skin.fans/media/expresstrade/bg_block_keys.png")
                .resize(App.display.widthPixels, (int) (App.display.widthPixels * 0.2521))
                .centerInside()
                .into(iv_bg_keys);

        bt_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPanelFiltersVisible = !isPanelFiltersVisible;
                setFilterPanelVisible(isPanelFiltersVisible);
            }
        });

        bt_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsContainer.itemsAdapter.selectAll();
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsContainer.itemsAdapter.setCancelSelection();
            }
        });

        select_keys_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int keysCount = inventory.keysCount;
                int keysSelected = inventory.keysSelected;

                if (keysCount == 0) return;
                setSelectKeysCoeff(Math.max(0, keysSelected - 1) / (float) keysCount, true);
                render(false);
            }
        });

        select_keys_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int keysCount = inventory.keysCount;
                int keysSelected = inventory.keysSelected;

                if (keysCount == 0)
                    return;

                if (!isSlotsExists(keysSelected + 1, true))
                    return;

                setSelectKeysCoeff(Math.min(keysCount, keysSelected + 1) / (float) keysCount, true);
                render(false);
            }
        });

        bt_recipient_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecipient(null);
            }
        });

        itemsContainer = new ItemsContainer(getContext(), getLoaderManager(), inflater, recyclerView, swipeRefresh);
        itemsContainer.setCurrentFragment(FragmentName.INVENTORY);
        itemsContainer.setInventoryModel(inventory);
        itemsContainer.setOnEventsListener(new ItemsContainer.OnEventsListener() {
            @Override
            public List onLoad() {
                List list = App.accountModule.findItems(inventory, appId, rarityExceptions);
                itemsCount = list.size();
                return list;
            }

            @Override
            public void onFinish(AppContainer.LoadStatus data) {
                itemsContainer.doResetSpace();

                switch (data) {
                    case NO_STUFF:
                        break;

                    case COMPLETED:
                        break;
                }

                if (!isLoading)
                    updateStatePanel();

//                render();
            }

            @Override
            public void onRefresh() {
                loadItems();
            }

            @Override
            public void onPaging() {

            }

            @Override
            public void onScroll(int y) {
            }

            @Override
            public void onSpace(boolean isSpace) {

            }
        });

        bt_filter_gold.setOnClickListener(this);
        bt_filter_red.setOnClickListener(this);
        bt_filter_pink.setOnClickListener(this);
        bt_filter_purple.setOnClickListener(this);
        bt_filter_blue.setOnClickListener(this);

        // Устанавливаем обработку события
        setOnTouchKeysPanel(ll_panel_keys);

        bt_create_offer = view.findViewById(R.id.bt_create_offer);
        bt_create_offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (recipientUser == null) {
                    isMakeOfferSelected = true;
                    App.eventManager.doEvent(EventType.ON_DO_SELECT_FRAME, FragmentViewer.FRAME_USERS);
                    return;
                }

                if (isOpeningMakeOffer) return;
                isOpeningMakeOffer = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isOpeningMakeOffer = false;
                    }
                }, 1000);

                // Создаем новый оффер
                App.dialogViewer.makeOffer(getChildFragmentManager());
            }
        });

        // Кликнули "Назад"
        bt_user_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.eventManager.doEvent(EventType.ON_DO_CLOSE_USER_INVENTORY);
            }
        });

        // Кликнули "Добавить в оффер"
        bt_add_to_offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.eventManager.doEvent(EventType.ON_DO_CLOSE_USER_INVENTORY);
            }
        });

        // Визуализируем прячим панель фильтров. По умолчанию показываем.
        boolean isFilterDisabled = App.prefManager.getBoolean(PrefKey.INV_FILTER_DISABLED);
        setFilterPanelVisible(!isFilterDisabled);

        // Запрашиваем список предметов
        loadItems();

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                render(false);
//            }
//        }, 500);

        cap_no_items.getLayoutParams().width = (int) (App.display.widthPixels / 2.7);
        cap_no_items.requestLayout();

        ll_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.dialogViewer.createSelectInventory(App.mainActivity, new DialogViewer.OnAcceptListener() {
                    @Override
                    public void onSelect(int index) {

                        setSelectApplication(index);

                    }
                });
            }
        });

        // Визуализируем прячим панель фильтров. По умолчанию показываем.
        Integer lastAppId = App.prefManager.getInt(PrefKey.INV_APP_LAST);
        if (lastAppId != 0)
            appId = lastAppId;

        setSelectApplication(appId);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    private void setSelectApplication(Integer index) {
        switch (index) {
            case 1:
                iv_inv_icon.setBackgroundResource(R.drawable.ic_vgo);
                tv_inv_title.setText("VGO");
                appId = 1;
                break;

            case 12:
                iv_inv_icon.setBackgroundResource(R.drawable.ic_sticker);
                tv_inv_title.setText("Stickers");
                appId = 12;
                break;

            case 7:
                iv_inv_icon.setBackgroundResource(R.drawable.ic_sticker_cat);
                tv_inv_title.setText("CKitties");
                appId = 7;
                break;
        }

        App.prefManager.setInt(PrefKey.INV_APP_LAST, appId);

        loadLocalItems();
    }

    private void updateStatePanel() {
        cap_no_items.setVisibility(itemsContainer.getItemCount() == 0 ? View.VISIBLE : View.GONE);

        // Устанавливаем кол-во ключей
        tv_keys_total.setText(inventory.keysCount.toString());
        tv_items_count.setText(itemsCount.toString());

        bt_filter.setVisibility(appId == 1 ? View.VISIBLE : View.GONE);
        panel_filter.setVisibility(appId == 1 ? (isPanelFiltersVisible ? View.VISIBLE : View.GONE) : View.GONE);

//        boolean isExist = inventory.items.size() != 0;
//        panel_filter.setVisibility(isExist ? View.VISIBLE : View.GONE);
//        bt_filter.setVisibility(isExist ? View.VISIBLE : View.GONE);
//        bt_select_all.setVisibility(isExist ? View.VISIBLE : View.GONE);
//        ll_title.setVisibility(isExist ? View.VISIBLE : View.GONE);
    }

    // Запрашиваем список предметов
    private void loadItems() {
        // Очищать только в нашем профиле. Если будем очищать в инвентаре партнера, то в оффере
        // будут каждый раз при открытии инвентаря сбиваться все выбранные ранее предметы
        if (!isUser) App.accountModule.emptySelectInventory(inventory);

        App.accountModule.loadItems(inventory, 1);
        isLoading = true;
    }

    private void loadLocalItems() {
        itemsContainer.itemsAdapter.lastPosition = -1;
        itemsContainer.doLoad();
    }

    // Указать ops_id юзера для загрузки инвентаря
    public void setUser(Long ops_id) {
        isUser = true;
        inventory = App.usersModule.inventory;
        inventory.ops_id = ops_id;

//        App.accountModule.emptySelectInventory(inventory);

        // Нужно перерендерить окно оффера
//        App.eventManager.doEvent(EventType.ON_DO_MAKE_OFFER_RENDER);
    }

    private void setFilterPanelVisible(boolean visible) {
        isPanelFiltersVisible = visible;
        panel_filter.setVisibility(isPanelFiltersVisible ? View.VISIBLE : View.GONE);
        App.prefManager.setBoolean(PrefKey.INV_FILTER_DISABLED, !visible);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchKeysPanel(LinearLayout ll_panel_keys) {

        final float[] values = {0, 0, 0}; // {start, current, isToastHide}

        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (inventory.keysCount == 0) return true;

                int width = panel_keys_bar.getWidth();

                switch (event.getAction()) {
                    case ACTION_DOWN:
                        // FIXME тут нужно позволять получать событие только при клике на панель

                        App.eventManager.doEvent(EventType.ON_DO_PAGING_DISABLED);
                        App.eventManager.doEvent(EventType.ON_STATE_KEYS_SELECTION_ENABLED);

                        values[0] = event.getX();
                        isScrollKeysActive = true;
                        return true;

                    case ACTION_MOVE:
                        if (!isScrollKeysActive) return true;
                        float value = event.getX() - values[0] + scrollKeysPosition;
                        values[1] = value;
                        values[1] = Math.max(0, values[1]);
                        values[1] = Math.min(width, values[1]);
                        float k = values[1] / width;
                        int count = Math.round(inventory.keysCount * k);

                        if (!isSlotsExists(count, values[2] == 0)) {
                            values[2] = 1;
                            return true;
                        }

                        panel_keys_scroll.setTranslationX(values[1]);

                        // Говорим коэффициент
                        setSelectKeysCoeff(k, false);
                        return true;

                    case ACTION_UP:
                        if (!isScrollKeysActive) return true;
                        scrollKeysPosition = values[1];
                        App.eventManager.doEvent(EventType.ON_STATE_KEYS_SELECTION_DISABLED);
                        App.eventManager.doEvent(EventType.ON_STATE_KEYS_KEY_SELECTED);
                        isScrollKeysActive = false;

                        values[2] = 0;

                        // Рендерим
                        render(false);

//                        if (!isOfferAvailable)
                            App.eventManager.doEvent(EventType.ON_DO_PAGING_ENABLED);
                        return true;
                }

                return false;
            }
        };

        ll_panel_keys.setOnTouchListener(listener);
        swipeRefresh.setOnTouchListener(listener);
    }

    // Установить коэффициент кол-ва ключей
    private void setSelectKeysCoeff(float k, boolean isUpdateBar) {
        k = Math.min(1, k);
        k = Math.max(0, k);

        if (isUpdateBar) {
            scrollKeysPosition = panel_keys_bar.getWidth() * k;
            panel_keys_scroll.setTranslationX(scrollKeysPosition);

            App.logManager.debug("setSelectKeysCoeff " + panel_keys_bar.getWidth() + " " + scrollKeysPosition);
        }

        // Устанавливаем кол-во ключей
        int count = Math.round(inventory.keysCount * k);

        inventory.keysSelected = count;
        tv_keys_select_count.setText(count + "");
    }

    private void emptySelectKeys() {
        inventory.keysSelected = 0;

        scrollKeysPosition = 0;
        tv_keys_select_count.setText("0");
        panel_keys_scroll.setTranslationX(0);
    }

    private void setKeysSelectedCount(int count) {
        int keysCount = inventory.keysCount;

        // TODO нужно учесть, что после перезагрузки страницы часть ключей может пропасть,
        // todo по этому при получении актуальных данных нужно обрезать число выбранных ключей до максимума
        if (keysCount == 0) return;

        App.logManager.debug("setKeysSelectedCount " + count + " " + (float) keysCount);

        setSelectKeysCoeff(count / (float) keysCount, true);
    }

    private Boolean isFilterExcept(String rarity) {
        return rarityExceptions.contains(rarity);
    }

    private void setFilterRarityToggle(String rarity) {
        if (!isFilterExcept(rarity))
            rarityExceptions.add(rarity);
        else
            rarityExceptions.remove(rarity);
    }

    public void setRecipient(UsersTable user) {
        recipientUser = user;

        App.accountModule.emptySelectInventory(App.usersModule.inventory);
        ll_recipient.setVisibility(user == null ? View.GONE : View.VISIBLE);
        if (user == null) {
            render(false);
            return;
        }

        tv_recipient_name.setText(StringUtils.truncate(user.name, 17));

        Picasso.with(getContext())
                .load(CommonUtils.getAvatar(user.avatar))
                .resize((int) (40 * App.display.density), (int) (40 * App.display.density))
                .into(iv_recipient_avatar);

        // Создаем оффер
        App.tradeModule.makeOffer(user.ops_id);

        // Создаем новый оффер
        if (isMakeOfferSelected) {
            App.dialogViewer.makeOffer(getChildFragmentManager());
            isMakeOfferSelected = false;
        }

        render(false);
    }

    public void render() {
        render(true);
    }

    @SuppressLint("SetTextI18n")
    public void render(boolean isNotify) {

        App.logManager.debug("RENDER");

        // Визуализируем предметы инвентаря
        if (isLoading && isNotify) {
//            empty();

            itemsContainer.doLoad();

            isLoading = false;
        }

        boolean isSelItems = inventory.itemsSelected.size() != 0;


//        iv_inv_icon.setVisibility(!isSelItems ? View.VISIBLE : View.GONE);
        tv_inv_title.setVisibility(!isSelItems ? View.VISIBLE : View.GONE);
        tv_items_count.setVisibility(!isSelItems ? View.VISIBLE : View.GONE);

        // Визуализируем кнопку закрытия
//        ll_title.setVisibility(!isSelItems ? View.VISIBLE : View.GONE);
        ll_select_title.setVisibility(isSelItems ? View.VISIBLE : View.GONE);
        bt_cancel.setVisibility(isSelItems ? View.VISIBLE : View.GONE);

        // Установить кол-во выделенных кейсов
        setKeysSelectedCount(inventory.keysSelected);

        tv_select_count.setText(inventory.itemsSelected.size() + "");

        // Включить/выключить перезагрузку контента свайпом
        swipeRefresh.setEnabled(!isSelItems);

        // Получаем пользователя и визуализируем
        if (isUser) {
            UsersTable user = App.usersModule.users.get(inventory.ops_id);

            if (user != null) {
                ll_user.setVisibility(View.VISIBLE);
                tv_user_name.setText(StringUtils.truncate(user.name, 17));

                Picasso.with(getContext())
                        .load(CommonUtils.getAvatar(user.avatar))
                        .resize((int) (40 * App.display.density), (int) (40 * App.display.density))
                        .into(iv_user_avatar);

                ll_panel_keys.setBackgroundColor(getResources().getColor(R.color.bg_content));
            }

            bt_select_all.setText("All");
            tv_inv_title.setVisibility(View.GONE);
        }


//        bt_select_all.setText("All");

        renderButtons();
    }

    // Рендерим нужные кнопки
    public void renderButtons() {

        if (isUser) {
            bt_add_to_offer.setVisibility(View.VISIBLE);
            bt_create_offer.setVisibility(View.GONE);
            bt_convert.setVisibility(View.GONE);
            return;
        }

        // Доступно ли создание оффера
        isOfferAvailable = inventory.keysSelected != 0 ||
                itemsContainer.itemsAdapter.getSelectCount() != 0 ||
                recipientUser != null;

        // Доступна ли конвертация
//        isConvertAvailable = inventory.keysSelected == 0 &&
//                itemsContainer.itemsAdapter.getSelectCount() != 0 &&
//                recipientUser == null;
        isConvertAvailable = false;

        // Устанавливаем визуал
        bt_create_offer.setVisibility(isOfferAvailable ? View.VISIBLE : View.GONE);
        bt_convert.setVisibility(isConvertAvailable ? View.VISIBLE : View.GONE);

        // Показать скрыть панель навигации ниже
        App.eventManager.doEvent(isOfferAvailable || isConvertAvailable ? EventType.ON_DO_NAVIGATION_INVISIBLE :
                EventType.ON_DO_NAVIGATION_VISIBLE);
    }

    public void empty() {
        // Очистить число выбранных ключей
        emptySelectKeys();
    }

    public boolean isSlotsExists(int needCount, boolean isToast) {
        int selectCount = itemsContainer.itemsAdapter.getSelectCount();

        if (selectCount + needCount > TradeModule.MAX_ITEMS) {
            if (isToast)
                Toast.makeText(App.context, TradeModule.MAX_ITEMS + " items max", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public void onClick(View v) {
        String rarity = (String) v.getTag();

        // toggle
        setFilterRarityToggle(rarity);

        switch (rarity) {
            case "gold":
                bt_filter_gold.setImageResource(isFilterExcept(rarity) ? R.drawable.ic_toggle_filter_yellow_disabled : R.drawable.ic_toggle_filter_yellow_enabled);
                break;
            case "red":
                bt_filter_red.setImageResource(isFilterExcept(rarity) ? R.drawable.ic_toggle_filter_red_disabled : R.drawable.ic_toggle_filter_red_enabled);
                break;
            case "pink":
                bt_filter_pink.setImageResource(isFilterExcept(rarity) ? R.drawable.ic_toggle_filter_pink_disabled : R.drawable.ic_toggle_filter_pink_enabled);
                break;
            case "purple":
                bt_filter_purple.setImageResource(isFilterExcept(rarity) ? R.drawable.ic_toggle_filter_purple_disabled : R.drawable.ic_toggle_filter_purple_enabled);
                break;
            case "blue":
                bt_filter_blue.setImageResource(isFilterExcept(rarity) ? R.drawable.ic_toggle_filter_blue_disabled : R.drawable.ic_toggle_filter_blue_enabled);
                break;
        }

        // Перезагрузить предметы
        itemsContainer.itemsAdapter.lastPosition = -1;
        itemsContainer.doLoad();
    }

    @Override
    public void onEvent(OnEventModel object) {
        super.onEvent(object);
        Long id;
        AcceptModel model;

        switch ((object.id)) {
            // ON_DO_INVENTORY_RELOAD
            case 1787995:
            // ON_EVENTS_INITIALED
            case 8979295:
                loadItems();
                break;

            // ON_TRADE_GET_INVENTORY_RECEIVED
            case 5010399:
                id = (Long) object.object;
                if (!id.equals(inventory.ops_id)) return;
                swipeRefresh.animate().alpha(1f).setDuration(300).start();
                pb.setVisibility(View.GONE);

                render();
                break;

            // ON_TRADE_GET_INVENTORY_NOT_RECEIVED
            case 8351887:
                id = (Long) object.object;
                if (!id.equals(inventory.ops_id)) return;

                render();
                break;

            // ON_STATE_ITEMS_SELECT_CHANGE
            case 9755069:
                render();
                break;

            // ON_DO_SELECT_RECIPIENT
            case 3228260:
                Long ops_id = (Long) object.object;
                UsersTable user = App.usersModule.users.get(ops_id);

                setRecipient(user);

                // Отправляем событие на переключение экрана
                App.eventManager.doEvent(EventType.ON_DO_SELECT_FRAME, FragmentViewer.FRAME_INVENTORY);
                break;

            // ON_ACCOUNT_2FA_CODE_CANCELED
            case 6165026:
                model = (AcceptModel) object.object;
                if (!model.isMakeOffer) return;
                if (!isOfferAvailable) return;

                App.dialogViewer.makeOffer(getChildFragmentManager());
                break;

            // ON_ACCOUNT_2FA_CODE_UPDATED
            case 7715232:
                model = (AcceptModel) object.object;
                if (!model.isMakeOffer) return;

//                App.dialogViewer.makeOffer(getChildFragmentManager());

                // Выполняем запрос
                App.tradeModule.reqSendOffer();

                break;

            // ON_TRADE_SEND_OFFER_COMPLETED
            case 1975172:
                emptySelectKeys();
                itemsContainer.itemsAdapter.setCancelSelection();
                renderButtons();
                setRecipient(null);
//                swipeRefresh.setRefreshing(true);
//                loadItems();
                break;

            // ON_TRADE_GET_INVENTORY_PRIVATE_ERROR
            case 8628214:
                if (!isUser) return;
                App.eventManager.doEvent(EventType.ON_DO_CLOSE_USER_INVENTORY);

                break;

            // ON_TRADE_SEND_OFFER_USER_TOKEN_NOT_VALID
            case 8518241:

                break;
        }
    }

    @Override
    public boolean onBackPressed() {

        // Если сейчас активен процесс выделение, отменяем выделение, вместо выхода из приложения
        if (itemsContainer.itemsAdapter.isSelectionState) {
            itemsContainer.itemsAdapter.setCancelSelection();
            return false;
        }

        return super.onBackPressed();
    }
}
