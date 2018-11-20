package fans.skin.expresstrade.view.fragments;

import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.pref.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.containers.*;
import fans.skin.expresstrade.view.viewers.*;

public class UsersFragment extends AppFragment implements View.OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    private AppButton bt_offer_close;
    private UsersContainer usersContainer;
    private AppRecyclerView recyclerView;
    private View swipeRefresh;
    private AppButton bt_search;
    private EditText et_search_input;
    private LinearLayout ll_search_block;
    private ProgressBar pb_search;
    private ImageView cap_no_friends;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private boolean isUsersLoaded = false;
    private boolean isSearching = false;
    private Long searchOpsId = null;
    private String searchOpsToken = null;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public FragmentName getFragmentName() {
        return FragmentName.FRAME_HISTORY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_users, container, false);

        bt_offer_close = view.findViewById(R.id.bt_offer_close);
        bt_offer_close.setSize(AppButton.ButtonSize.SMALL);
        bt_search = view.findViewById(R.id.bt_search);
        bt_search.setSize(AppButton.ButtonSize.SMALL);

        recyclerView = view.findViewById(R.id.list_users);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        cap_no_friends = view.findViewById(R.id.cap_no_friends);

        ll_search_block = view.findViewById(R.id.ll_search_block);
        et_search_input = view.findViewById(R.id.et_search_input);
        pb_search = view.findViewById(R.id.pb_search);

        usersContainer = new UsersContainer(getContext(), getLoaderManager(), inflater, recyclerView, swipeRefresh);
        usersContainer.setOnDataLoaderListener(new UsersContainer.OnDataEventsListener() {
            @Override
            public List onLoad() {
                App.logManager.debug("Got people " + App.usersModule.users.size());

                // Get all the available records in the database
                List<UsersTable> models = new ArrayList<>();

                // Log all users to table
                for(Map.Entry<Long, UsersTable> entry : App.usersModule.users.entrySet()) {
                    UsersTable user = entry.getValue();

                    // Do not add us
                    if (user.ops_id == App.USER_ID) continue;

                    models.add(user);
                }

                return models;
            }

            @Override
            public void onFinish(AppContainer.LoadStatus data) {
                App.logManager.debug("onFinishData " + data.name());
                isUsersLoaded = true;

                switch (data) {
                    case NO_STUFF:
                        cap_no_friends.setVisibility(View.VISIBLE);
                        break;

                    case COMPLETED:
                        cap_no_friends.setVisibility(View.GONE);
                        break;

                    case NETWORK_ERROR:
                        cap_no_friends.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onRefresh() {
                loadItems();
                App.logManager.debug("onRefresh");
            }
        });

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser();
            }
        });

        et_search_input.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = et_search_input.getText().toString();
                ll_search_block.setBackgroundResource("".equals(input) || isTradeValid(input) ? R.color.bg_content_active : R.color.bg_content_unactive);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        et_search_input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Toast.makeText(App.context, "Searching...", Toast.LENGTH_SHORT).show();

                    searchUser();
                    return true;
                }
                return false;
            }
        });

        loadItems();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    private void loadItems() {
        App.logManager.debug("loadItems users");
        usersContainer.doLoadData();
    }

    private void searchUser() {
        String string = et_search_input.getText().toString();
        String[] split = string.split("/");
        if (split.length != 6) return;

        String token = split[split.length - 1];
        Long ops_id = Long.parseLong(split[split.length - 2]);

        if (isSearching) return;
        setSearchLoadStatus(true);

        searchOpsId = ops_id;
        searchOpsToken = token;

        // Set the current user we are looking for
        usersContainer.setCurrentSearchOpsId(ops_id);

        App.usersModule.reqSearchUser(ops_id, token);

        et_search_input.setText("");
    }

    private boolean isTradeValid(String value) {
        String[] split = value.split("/");
        if (split.length != 6) return false;

        return true;
    }

    // Edit link
    private void doChangeLink(final Long ops_id) {
        App.logManager.debug("doChangeLink " + ops_id);

        App.dialogViewer.createChangeLink(App.mainActivity, new DialogViewer.OnAcceptListener() {
            @Override
            public void onEdit(String value) {
                String[] split = value.split("/");
                if (split.length != 6) return;
                String token = split[split.length - 1];

                App.logManager.debug("SET TOKEN " + token);

                App.usersModule.setTradeUrl(ops_id, token);
                App.eventManager.doEvent(EventManager.EventType.ON_USERS_TOKEN_UPDATED);
            }

            @Override
            public boolean onChange(String value) {
                String[] split = value.split("/");
                if (split.length != 6)
                    return false;

                Long uid = Long.parseLong(split[split.length - 2]);
                if (!uid.equals(ops_id))
                    return false;

                String token = split[split.length - 1];

                return true;
            }
        });
    }

    private void setSearchLoadStatus(boolean state) {
        isSearching = state;
        pb_search.animate().alpha(isSearching ? 1f : 0f).setDuration(300).start();
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public void onEvent(OnEventModel event) {
        super.onEvent(event);

        switch ((event.id)) {
            // ON_TRADE_GET_OFFERS_RECEIVED
            case 2953969:
                if (isUsersLoaded) return;
                loadItems();
                break;

            // ON_DO_USERS_LOAD_LIST
            case 1885712:
//                loadItems();
                App.usersModule.loadUsers();
                break;
            // ON_DO_USERS_UPDATE_LIST
            case 7763295:
            // ON_USERS_TOKEN_UPDATED
            case 5725624:
            // ON_EVENTS_INITIALED
            case 8979295:
            // ON_TRADE_SEND_OFFER_USER_TOKEN_NOT_VALID
            case 8518241:
                loadItems();
                break;

            // ON_DO_USER_CHANGE_LINK
            case 1716222:
                Long ops_id = (Long) event.object;
                if (ops_id == null) return;
                doChangeLink(ops_id);
                break;

            // ON_USERS_GET_USER_NOT_RECEIVED
            case 6525511:
                setSearchLoadStatus(false);
                break;

            // ON_USERS_GET_USER_RECEIVED
            case 6187284:
                setSearchLoadStatus(false);
                loadItems();
                break;
        }
    }

    @Override
    public void onClick(View v) {
    }
}
