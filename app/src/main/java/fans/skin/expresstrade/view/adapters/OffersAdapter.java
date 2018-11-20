package fans.skin.expresstrade.view.adapters;

import android.content.*;
import android.os.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.containers.*;

import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_ACCEPTED;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_ACTIVE;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_CANCELED;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_DECLINED;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_EXPIRED;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_EXPIRED_CASE_OPEN;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_FAILED_CASE_OPEN;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_INVALID_ITEMS;
import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_PENDING_CASE_OPEN;

public class OffersAdapter extends AppRecyclerAdapter<OffersAdapter.MyHolder> {

    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Handler handler = new Handler();

    private final Context context;
    private List<ItemOffer> items;
    private OffersContainer container;

    private String user_name = "";

    private boolean isAnimProfile = false;
    public boolean isSearchActive = false;

    public int lastPosition = -1;
    public long lastAnimTime = 0;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public OffersAdapter(OffersContainer container, Context context, LayoutInflater inflater, List data) {
        super(context, inflater, data);
        this.container = container;
        this.context = context;
        this.data = data;
        this.items = new ArrayList<>();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_HEADER)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);

        return new MyHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        ItemOffer item = items.get(position);

        if (item.isHeader)
            return;

        UserModel.User mine = !item.offer.sent_by_you ? item.offer.recipient : item.offer.sender;
        UserModel.User partner = item.offer.sent_by_you ? item.offer.recipient : item.offer.sender;

        holder.offer.data = item.offer;

        // default
        holder.offer.ll_state.setVisibility(View.GONE);
        holder.offer.ll_buttons.setVisibility(View.GONE);
        holder.offer.bt_cancel.setVisibility(View.GONE);
        holder.offer.bt_accept.setVisibility(View.GONE);
        holder.offer.ll_give_info.setVisibility(View.VISIBLE);
        holder.offer.ll_get_info.setVisibility(View.VISIBLE);
        holder.offer.ll_get_cases.setVisibility(View.GONE);

        // set values
        holder.offer.tv_title.setText(StringUtils.truncate(partner.display_name, 14));
        holder.offer.tv_title.setTextColor(App.context.getResources().getColor(partner.verified ? R.color.green : (item.offer.is_case_opening ? R.color.blue : R.color.white)));

        holder.offer.ic_verified.setVisibility(partner.verified ? View.VISIBLE : View.GONE);
        holder.offer.ic_vgosite.setVisibility(item.offer.is_case_opening ? View.VISIBLE : View.GONE);

        // Give info
        holder.offer.tv_give_total.setTextColor(App.context.getResources().getColor(mine.items.size() == 0 ? R.color.gray : R.color.gold));
        holder.offer.tv_give_total.setText(CommonUtils.getFormattedAmount(App.accountModule.getCostItems(mine.items)));
        holder.offer.iv_arrow_give.setAlpha(mine.items.size() == 0 ? 0.3f : 1f);
        holder.offer.iv_give_eye.setAlpha(mine.items.size() == 0 ? 0.1f: 0.8f);
        holder.offer.iv_give_name.setAlpha(mine.items.size() == 0 ? 0.1f: 0.8f);

        // Get info
        holder.offer.tv_get_total.setTextColor(App.context.getResources().getColor(partner.items.size() == 0 ? R.color.gray : R.color.gold));
        holder.offer.tv_get_total.setText(CommonUtils.getFormattedAmount(App.accountModule.getCostItems(partner.items)));
        holder.offer.iv_arrow_get.setAlpha(partner.items.size() == 0 ? 0.3f : 1f);
        holder.offer.iv_get_eye.setAlpha(partner.items.size() == 0 ? 0.1f: 0.8f);
        holder.offer.iv_get_name.setAlpha(partner.items.size() == 0 ? 0.1f: 0.8f);

        switch (item.offer.state) {
            case STATE_CANCELED:
            case STATE_DECLINED:
            case STATE_EXPIRED:
            case STATE_INVALID_ITEMS:
            case STATE_EXPIRED_CASE_OPEN:
            case STATE_FAILED_CASE_OPEN:
                holder.offer.tv_give_total.setTextColor(App.context.getResources().getColor(R.color.gray_dark));
                holder.offer.tv_get_total.setTextColor(App.context.getResources().getColor(R.color.gray_dark));
                holder.offer.iv_arrow_give.setAlpha(0.1f);
                holder.offer.iv_give_eye.setAlpha(0.1f);
                holder.offer.iv_give_name.setAlpha(0.1f);
                holder.offer.iv_arrow_get.setAlpha(0.1f);
                holder.offer.iv_get_eye.setAlpha(0.1f);
                holder.offer.iv_get_name.setAlpha(0.1f);
                break;
        }

        switch (item.offer.state) {
            case STATE_ACCEPTED:
                holder.offer.ll_state.setVisibility(View.VISIBLE);
                holder.offer.tv_state_value.setText("Accepted");
                holder.offer.tv_state_value.setTextColor(App.context.getResources().getColor(R.color.accent));
                break;

            case STATE_DECLINED:
                holder.offer.ll_state.setVisibility(View.VISIBLE);
                holder.offer.tv_state_value.setText("Declined");
                holder.offer.tv_state_value.setTextColor(App.context.getResources().getColor(R.color.red));
                break;

            case STATE_EXPIRED:
                holder.offer.ll_state.setVisibility(View.VISIBLE);
                holder.offer.tv_state_value.setText("Expired");
                holder.offer.tv_state_value.setTextColor(App.context.getResources().getColor(R.color.gray));
                break;

            case STATE_CANCELED:
                holder.offer.ll_state.setVisibility(View.VISIBLE);
                holder.offer.tv_state_value.setText("Canceled");
                holder.offer.tv_state_value.setTextColor(App.context.getResources().getColor(R.color.gray));
                break;

            case STATE_INVALID_ITEMS:
            case STATE_EXPIRED_CASE_OPEN:
            case STATE_FAILED_CASE_OPEN:
                holder.offer.ll_state.setVisibility(View.VISIBLE);
                holder.offer.tv_state_value.setText("Invalid");
                holder.offer.tv_state_value.setTextColor(App.context.getResources().getColor(R.color.red));
                break;

            case STATE_PENDING_CASE_OPEN:
                holder.offer.ll_state.setVisibility(View.VISIBLE);
                holder.offer.tv_state_value.setText("Canceled");
                holder.offer.tv_state_value.setTextColor(App.context.getResources().getColor(R.color.blue));
                break;

            case STATE_ACTIVE:
                holder.offer.ll_buttons.setVisibility(View.VISIBLE);
                holder.offer.bt_cancel.setVisibility(View.VISIBLE);

                if (!item.offer.sent_by_you)
                    holder.offer.bt_accept.setVisibility(View.VISIBLE);

                if (item.offer.is_case_opening) {
                    holder.offer.ll_get_cases.setVisibility(View.VISIBLE);
                    holder.offer.tv_get_cases_count.setText(mine.items.size() + "");
                }


                break;
        }

        if (container.isAnim)
            doAnimate(holder.itemView, position);
    }

    private void doAnimate(final View view, final int position) {
        final long time = 200;

        if (position > lastPosition) {

            if (position >= 10) {
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


    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public void clearAll() {
        data = new ArrayList();
        items = new ArrayList<>();
    }

    // ***
    public void notifyItems() {
        items = new ArrayList<>();

        for (int i = 0; i < data.size(); i++)
            items.add(new ItemOffer((OfferModel.Offer) data.get(i)));

        notifyDataSetChanged();
    }

    // =============================================================================================
    // LINEUSER CLASS
    // =============================================================================================

    private static class ItemOffer {
        public OfferModel.Offer offer;
        public String string;
        // ***
        public boolean isHeader;

        // title
        public ItemOffer(String text) {
            this.isHeader = true;
            this.string = text;
        }

        // item
        public ItemOffer(OfferModel.Offer offer) {
            this.isHeader = false;
            this.offer = offer;
        }
    }

    // =============================================================================================
    // HOLDER CLASS
    // =============================================================================================

    public class MyHolder extends RecyclerView.ViewHolder {
        public OfferView offer;
        public TextView tv_p;

        MyHolder(View view, int viewType) {
            super(view);

            if (viewType == VIEW_TYPE_HEADER) {
                this.tv_p = view.findViewById(R.id.tv_p);
            } else {
                this.offer = new OfferView(context, view);
                this.offer.setOnActionListener(new OfferView.OnActionListener() {
                    @Override
                    public void onViewGiveItems() {
                        UserModel.User mine = !offer.data.sent_by_you ? offer.data.recipient : offer.data.sender;
                        if (mine.items.size() == 0) return;
                        App.dialogViewer.viewItems(container.fragmentManager, App.accountModule.getKeysSplit(mine.items), R.string.title_offer_give);
                    }

                    @Override
                    public void onViewGetItems() {
                        UserModel.User partner = offer.data.sent_by_you ? offer.data.recipient : offer.data.sender;
                        if (partner.items.size() == 0) return;
                        App.dialogViewer.viewItems(container.fragmentManager, App.accountModule.getKeysSplit(partner.items), R.string.title_offer_get);
                    }

                    @Override
                    public void onCancel() {
                        App.tradeModule.reqCancelOffer(offer.data.id);
                    }

                    @Override
                    public void onAccept() {
                        if (offer.data.is_gift) {
                            App.tradeModule.reqAcceptOffer(offer.data.id, false);
                            return;
                        }

                        AcceptModel model = new AcceptModel();
                        model.isAcceptOffer = true;
                        model.id = offer.data.id;
                        App.eventManager.doEvent(EventType.ON_DO_2FA_ACCEPT, model);
                    }
                });
            }
        }
    }
}




