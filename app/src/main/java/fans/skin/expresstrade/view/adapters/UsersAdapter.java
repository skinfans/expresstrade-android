package fans.skin.expresstrade.view.adapters;

import android.content.*;
import android.os.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.containers.*;
import fans.skin.expresstrade.view.viewers.*;

import java.util.*;

import static fans.skin.expresstrade.managers.event.EventManager.*;

public class UsersAdapter extends AppRecyclerAdapter<UsersAdapter.MyHolder> {

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
    private List<ItemUser> items;
    private UsersContainer container;

    private String user_name = "";

    private boolean isAnimProfile = false;
    public boolean isSearchActive = false;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public UsersAdapter(UsersContainer container, Context context, LayoutInflater inflater, List data) {
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);

        return new MyHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        ItemUser item = items.get(position);


        if (item.isHeader) {
            holder.tv_p.setText(item.string);
        } else {
            holder.user.data = item.user;
            holder.user.tv_name.setText(StringUtils.truncate(item.user.name, 17));

            Picasso.with(context)
                    .load(CommonUtils.getAvatar(item.user.avatar))
                    .resize((int) (40 * App.display.density), (int) (40 * App.display.density))
                    .into(holder.user.iv_avatar);


            // Set the state of favorites
            holder.user.setFeaturedState(holder.user.data.is_favorite);
            holder.user.bt_change_link.setVisibility(item.user.token == null ? View.VISIBLE : View.GONE);
            holder.user.bt_select.setVisibility(item.user.token != null ? View.VISIBLE : View.GONE);
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

        List<UsersTable> searchUsers = new ArrayList<>();
        List<UsersTable> featureUsers = new ArrayList<>();
        List<UsersTable> lastUsers = new ArrayList<>();

        // We form two arrays
        for (int i = 0; i < data.size(); i++) {
            UsersTable user = (UsersTable) data.get(i);

            if (container.isUserSearching(user.ops_id))
                searchUsers.add(user);
            else if (user.is_favorite)
                featureUsers.add(user);
            else
                lastUsers.add(user);
        }

        if (searchUsers.size() != 0) {
            items.add(new ItemUser(context.getResources().getString(R.string.title_users_search)));

            for (int i = 0; i < searchUsers.size(); i++)
                items.add(new ItemUser(searchUsers.get(i)));
        }

        if (featureUsers.size() != 0) {
            items.add(new ItemUser(context.getResources().getString(R.string.title_users_featured)));

            for (int i = 0; i < featureUsers.size(); i++)
                items.add(new ItemUser(featureUsers.get(i)));
        }

        if (lastUsers.size() != 0) {
            items.add(new ItemUser(context.getResources().getString(R.string.title_users_last)));

            for (int i = 0; i < lastUsers.size(); i++)
                items.add(new ItemUser(lastUsers.get(i)));
        }

        notifyDataSetChanged();
    }

    // =============================================================================================
    // LINEUSER CLASS
    // =============================================================================================

    private static class ItemUser {
        public UsersTable user;
        public String string;
        // ***
        public boolean isHeader;

        // title
        public ItemUser(String text) {
            this.isHeader = true;
            this.string = text;
        }

        // user
        public ItemUser(UsersTable user) {
            this.isHeader = false;
            this.user = user;
        }
    }

    // =============================================================================================
    // HOLDER CLASS
    // =============================================================================================

    public class MyHolder extends RecyclerView.ViewHolder {
        public UserView user;
        public TextView tv_p;

        MyHolder(View view, int viewType) {
            super(view);

            if (viewType == VIEW_TYPE_HEADER) {
                this.tv_p = view.findViewById(R.id.tv_p);
            } else {
                this.user = new UserView(context, view);
                this.user.setOnActionListener(new UserView.OnActionListener() {
                    @Override
                    public void onFeatureChanged(boolean isFollow) {

                        // save state
                        App.usersModule.setUserFeatureState(user.data.ops_id, isFollow);

                        // re-render data
                        container.doLoadData();
                    }

                    @Override
                    public void onSelect(long ops_id) {
                        App.eventManager.doEvent(EventType.ON_DO_SELECT_RECIPIENT, ops_id);
                    }

                    @Override
                    public void onChangeLink(final long ops_id) {
                        App.eventManager.doEvent(EventType.ON_DO_USER_CHANGE_LINK, ops_id);
                    }

                });
            }
        }
    }
}




