package fans.skin.expresstrade.view.adapters;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.widget.*;
import android.support.v7.widget.RecyclerView.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.bumptech.glide.*;
import com.bumptech.glide.load.engine.*;
import com.bumptech.glide.load.model.*;
import com.bumptech.glide.request.*;
import com.caverock.androidsvg.*;
import com.squareup.picasso.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.R;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.modules.trade.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.containers.*;
import fans.skin.expresstrade.view.viewers.*;

import java.io.*;
import java.util.*;

import javax.sql.*;

import static android.support.constraint.Constraints.TAG;
import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;
import static fans.skin.expresstrade.managers.event.EventManager.*;

public class ItemsAdapter extends AppRecyclerAdapter<ItemsAdapter.MyHolder> {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Handler handler = new Handler();
    private Context context;
    private ItemsAdapter mThis;
    private LayoutManager layoutManager;
    private ItemsContainer container;

    private HashMap<Long, MyHolder> holders = new HashMap();

    private int spanCount = 1;
    private boolean isProfileClickable = true;

    public int lastPosition = -1;
    public long lastAnimTime = 0;
    public int firstId;
    public boolean isAnimViewer = false;

    private int frame = 0;

    public boolean isSelectionState = false;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public ItemsAdapter(ItemsContainer container, Context context, LayoutInflater inflater, LayoutManager layoutManager, List data) {
        super(context, inflater, data);
        this.context = context;
        this.layoutManager = layoutManager;
        this.container = container;
        this.mThis = this;

        frame = FragmentViewer.FRAME_INVENTORY;
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public int getSpanCount() {
        return this.spanCount;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_item, parent, false);
        return new MyHolder(v);
    }

    // Заполнение виджетов View данными из элемента списка с номером i
    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        updateSelectState();

        final ItemModel.Item item = (ItemModel.Item) this.data.get(position);
        holder.item.setTag(item.id);
        holder.item.data = item;

        holders.put(item.id, holder);

        float margin = (10 + 10 + 4 + 4) * App.display.density;
        float width = ((container.swipeRefresh.getWidth() - margin) / spanCount);

        // Предмет не выделен, так как только выбран
        holder.item.setSelection(isSelected(holder.item.data.id));
        holder.item.setSelectState(isSelectionState);

        holder.item.view.getLayoutParams().width = (int) width;
        holder.item.view.getLayoutParams().height = (int) (width);

//        if (item.color != null)
//            holder.item.ivLineRarity.setBackgroundColor(Color.parseColor(item.color));

        // Устанавливаем цену
        holder.item.tv_price.setText(CommonUtils.getFormattedAmount(item.suggested_price));

        Integer internal_app_id = holder.item.data.internal_app_id;
        if (internal_app_id == null) internal_app_id = 1;

        switch (internal_app_id) {
            case 1:
                holder.item.tv_title.setText(CommonUtils.getItemName(item.name));
                holder.item.tv_title.setTextColor(Color.parseColor(item.color));

                holder.item.tv_wear.setText(CommonUtils.getItemWear(item.name));
                holder.item.tv_wear.setTextColor(Color.parseColor(item.color));
                break;

            case 12:
                String name = item.market_name.replace("Sticker | ", "");
                holder.item.tv_title.setTextColor(App.context.getResources().getColor(R.color.gray));
                holder.item.tv_title.setText(name);
                holder.item.tv_wear.setText("");
                break;

            case 7:
                holder.item.tv_title.setTextColor(App.context.getResources().getColor(R.color.gray));
                holder.item.tv_title.setText(item.market_name);
                holder.item.tv_wear.setText("");


                break;
        }

        ////
        Uri test = Uri.parse(item.image.toString());
        String url = "https://files.opskins.media" + test.getPath().split(",")[0];

        switch (internal_app_id) {
            case 1:
            case 12:
                Picasso.with(context)
                        .load(url)
                        .resize((int) width, (int) width)
                        .into(internal_app_id == 1 ? holder.item.preview : holder.item.preview_min);

                holder.item.preview_svg.setVisibility(View.GONE);
                holder.item.preview.setVisibility(internal_app_id == 1 || internal_app_id == 7 ? View.VISIBLE : View.GONE);
                holder.item.preview_min.setVisibility(internal_app_id != 1 ? View.VISIBLE : View.GONE);

                break;

            case 7:

                App.logManager.debug("LOAD SVG " + url + " " + internal_app_id);

                // Создаем класс билдера для обработки события загрузки и делаем магию
                RequestBuilder<PictureDrawable> requestBuilder = Glide.with(context)
                        .as(PictureDrawable.class)
                        .listener(new SvgSoftwareLayerSetter());

                // Визуализируем
                requestBuilder
                        .load(url)
                        .into(holder.item.preview_svg);

                holder.item.preview_svg.setVisibility(View.VISIBLE);
                holder.item.preview.setVisibility(View.GONE);
                holder.item.preview_min.setVisibility(View.GONE);
                break;
        }

        ////

        if (item.sku == 1)
            holder.item.tv_count.setText(item.count + "");

        holder.item.tv_count.setVisibility(item.sku == 1 ? View.VISIBLE : View.GONE);
        holder.item.tv_price.setVisibility(item.sku != 1 && internal_app_id == 1 ? View.VISIBLE : View.GONE);


        holder.item.rl_timer.setVisibility(item.trade_hold_expires != null ? View.VISIBLE : View.GONE);

        doAnimate(holder.itemView, position);
    }

    private void doAnimate(final View view, final int position) {
        final long time = 200 / spanCount;

        if (position > lastPosition) {

            if (position >= 15) {
                return;
            }

            final long currentTime = System.currentTimeMillis();

            if (position == 0) lastAnimTime = currentTime;

            long diff = currentTime - lastAnimTime;

            if (diff < 40) lastAnimTime += 40;
            else lastAnimTime = currentTime;

            lastPosition = position;

            view.setTranslationY(20 * App.display.density);
            view.setAlpha(0f);

            view.animate().setStartDelay(lastAnimTime - currentTime).setDuration(time).translationY(0).alpha(1f).start();
        }
    }

    // Выбрать предмет
    public boolean selectItem(Long id, boolean state, boolean isAnim) {
        if (container.inventory == null) return true;

        // Предмет уже имеет данный статус
        if (isSelected(id) == state) return true;

        // Добавляем предмет в массив выделенных
        // Удаляем из массива выделенных
        if (state) {
            if (container.inventory.keysSelected + getSelectCount() + 1 > TradeModule.MAX_ITEMS) {
                Toast.makeText(App.context, TradeModule.MAX_ITEMS + " items max", Toast.LENGTH_SHORT).show();
                return false;
            }

            container.inventory.itemsSelected.add(id);
        } else {
            container.inventory.itemsSelected.remove(id);
        }

        // Устанавливаем общее состояние выделения
        updateSelectState();

        MyHolder holder = holders.get(id);
        if (holder == null) return true;

        // Выделить конкретный предмет
        holder.item.setSelection(state, isAnim);

        return true;
    }

    // Установить состояние выбора элементов
    public void updateSelectState() {
        if (container.inventory == null) return;

        boolean state = container.inventory.itemsSelected.size() != 0;
        if (isSelectionState == state) return;
        isSelectionState = state;

        // Отправляем событие
        App.eventManager.doEvent(isSelectionState ? EventType.ON_STATE_ITEMS_SELECTION_ENABLED :
                EventType.ON_STATE_ITEMS_SELECTION_DISABLED);

        // Обходим все товары и устанавливаем статус выделения
        for(int i = 0; i < data.size(); i++) {
            ItemModel.Item item = (ItemModel.Item) data.get(i);
            MyHolder holder = holders.get(item.id);

            if (holder == null)
                continue;

            // Устанавливаем состояние выделения
            holder.item.setSelectState(isSelectionState, true);
        }
    }

    // Выделить все элементы/снять выделение всех элементов
    public void selectAll() {
        if (container.inventory == null) return;

        for(int i = 0; i < data.size(); i++) {
            ItemModel.Item item = (ItemModel.Item) data.get(i);

            // Устанавливаем состояние выделения
            boolean isStop = !selectItem(item.id, true, true);
            if (isStop) break;
        }

        // Отправляем событие
        App.eventManager.doEvent(EventType.ON_STATE_ITEMS_SELECT_CHANGE);
    }

    // Отменить выделение
    public void setCancelSelection() {
        if (container.inventory == null) return;

        for(int i = 0; i < data.size(); i++) {
            ItemModel.Item item = (ItemModel.Item) data.get(i);

            // Устанавливаем состояние выделения
            selectItem(item.id, false, true);
        }

        // Очищаем массив
        container.inventory.itemsSelected.clear();

        // Отправляем событие
        App.eventManager.doEvent(EventType.ON_STATE_ITEMS_SELECT_CHANGE);
    }

    public boolean isSelected(Long id) {
        if (container.inventory == null) return false;
        return container.inventory.itemsSelected.contains(id);
    }


    public Integer getSelectCount() {
        if (container.inventory == null) return 0;
        return container.inventory.itemsSelected.size();
    }

    // =============================================================================================
    // HOLDER CLASS
    // =============================================================================================

    public class MyHolder extends RecyclerView.ViewHolder {
        public ItemView item;

        // Реализация класса MyHolder, хранящего ссылки на виджеты
        public MyHolder(View view) {
            super(view);

            this.item = new ItemView(context, view);
            this.item.setOnActionListener(new ItemView.OnActionListener() {
                @Override
                public void onOpenDialog() {

                }

                @Override
                public void onLike() {

                }

                @Override
                public void onClick() {

                    // Устанавливаем состояние выделения
                    Long id = item.data.id;
                    boolean state = !isSelected(id);

                    selectItem(item.data.id, state, false);

                    // Отправляем событие
                    App.eventManager.doEvent(EventType.ON_STATE_ITEMS_SELECT_CHANGE);
                }

                @Override
                public void onOpenProfile() {
                }
            });
        }
    }
}
