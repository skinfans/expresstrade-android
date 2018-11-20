package fans.skin.expresstrade.view.viewers;

import android.app.*;
import android.content.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import com.squareup.picasso.*;

import org.w3c.dom.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.R;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.AppButton.*;
import fans.skin.expresstrade.view.fragments.*;

import java.util.*;

public class DialogViewer {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private List<Object> alertDialogs;

    private Handler handler = new Handler();

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final DialogViewer ourInstance = new DialogViewer();

    public static DialogViewer getInstance() {
        return ourInstance;
    }

    private DialogViewer() {
        alertDialogs = new ArrayList<>();
    }

    // =============================================================================================
    // INTERFACES
    // =============================================================================================

    public static abstract class OnAcceptListener {
        public void onDestroy() {
        }

        public void onYes() {
        }

        public void onRepeat() {
        }

        public void onNo() {
        }

        public void onCancel() {
        }

        public void onAllow() {
        }

        public void onExit() {
        }

        public void onEnable() {

        }

        public void onOk() {
        }

        public void onBuy() {

        }

        public void onClose() {

        }

        public boolean onChange(String value) {
            return false;
        }

        public void onEdit() {
        }

        public void onEdit(int value) {
        }

        public void onEdit(String value) {
        }

        public void onEdit(String value, String value2) {
        }

        public void onSelect(int index) {

        }

        public void onRate() {

        }

        public void onLater() {

        }

        public void onNotAsk() {

        }
    }


    public interface OnMenuUserListener {
        void onBan(boolean isBan);

        void onFollow(boolean isFollow);
    }

    public interface OnMenuSwineListener {
        void onOpenProfile();

        void onSave();

        void onShare();
    }

    // =============================================================================================
    // PERMISSIONS METHODS
    // =============================================================================================

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public AlertDialog createDialog(AlertDialog.Builder dialogBuilder) {
        if (dialogBuilder == null) return null;

        final AlertDialog b = dialogBuilder.create();
        b.show();

        alertDialogs.add(b);

        App.logManager.debug("Dialog created. Size array " + alertDialogs.size());

        b.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                alertDialogs.remove(b);

                App.logManager.debug("Dialog onDismiss. Size array " + alertDialogs.size());
            }
        });

        return b;
    }

    public boolean checkIsActive() {
        return alertDialogs.size() != 0;
    }

    // =============================================================================================
    // PERMISSION
    // =============================================================================================

    // =============================================================================================
    // GENERAL
    // =============================================================================================

    public Dialog createAuthWebview(Context context) {
        View view = App.authActivity.getLayoutInflater().inflate(R.layout.popup_auth, null);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        alertDialogs.add(dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().width = App.display.widthPixels;

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                alertDialogs.remove(dialog);
            }
        });

        return dialog;
    }

    public MakeOfferDialogFragment makeOffer(FragmentManager manager) {
        MakeOfferDialogFragment dialog = new MakeOfferDialogFragment();
        dialog.setCancelable(true);
        dialog.show(manager, "offer_fragment");

        return dialog;
    }

    public InventoryDialogFragment openUserInventory(FragmentManager manager, Long ops_id) {
        InventoryDialogFragment dialog = new InventoryDialogFragment();
        dialog.setUser(ops_id);
        dialog.setCancelable(true);
        dialog.show(manager, "user_inventory");

        return dialog;
    }

    public ItemsDialogFragment viewItems(FragmentManager manager, List<ItemModel.Item> items, int title) {
        ItemsDialogFragment dialog = new ItemsDialogFragment();
        dialog.setItems(items);
        dialog.setTitle(title);
        dialog.setCancelable(true);
        dialog.show(manager, "items");

        return dialog;
    }

    public Dialog createChangeLink(Context context, final OnAcceptListener onAcceptListener) {
        View view = App.mainActivity.getLayoutInflater().inflate(R.layout.popup_change_link, null);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        alertDialogs.add(dialog);

        final EditText et_input = view.findViewById(R.id.et_input);
        final AppFullButton button = view.findViewById(R.id.bt_edit);

        final boolean[] isValid = {false};

        et_input.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (onAcceptListener == null) return;

                isValid[0] = onAcceptListener.onChange(et_input.getText().toString());

                // todo
                button.setBackgroundResource(isValid[0] ? R.drawable.bg_button_big_green : R.drawable.bg_button_red);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().width = App.display.widthPixels;

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                alertDialogs.remove(dialog);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValid[0]) {
                    Toast.makeText(App.context, "Make sure the link is correct and belongs to this friend.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (onAcceptListener != null)
                    onAcceptListener.onEdit(et_input.getText().toString());

                dialog.dismiss();
            }
        });

        AppButton bt_close = view.findViewById(R.id.bt_close);
        bt_close.setSize(ButtonSize.SMALL);
        bt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public Dialog setNotifyDialog(Context context, final CheckInModel.Notify notify) {
        View view = App.mainActivity.getLayoutInflater().inflate(R.layout.popup_notify, null);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(view);
//        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        alertDialogs.add(dialog);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                App.mainActivity.finish();
            }
        });

        ImageView iv_head = view.findViewById(R.id.iv_head);
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_message = view.findViewById(R.id.tv_message);
        AppFullButton bt_link = view.findViewById(R.id.bt_link);

        if (notify.image != null) {
            iv_head.setVisibility(View.VISIBLE);

            iv_head.getLayoutParams().height = (int) (App.display.widthPixels * 0.9 * 0.5625);
            view.requestLayout();

            Picasso.with(App.context)
                    .load(notify.image)
                    .resize(App.display.widthPixels, (int) (App.display.widthPixels * 0.5625))
                    .centerInside()
                    .into(iv_head);
        }

        if (notify.title != null) {
            tv_title.setVisibility(View.VISIBLE);
            tv_title.setText(notify.title);
        }

        if (notify.message != null) {
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText(notify.message);
        }

        if (notify.link != null) {
            bt_link.setVisibility(View.VISIBLE);
            bt_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (App.mainActivity != null && App.mainActivity.isActive) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(notify.link));
                        App.mainActivity.startActivity(browserIntent);
                        App.mainActivity.finish();
                    }
                }
            });
        }

        return dialog;
    }

    public Dialog createSelectInventory(Context context, final OnAcceptListener onAcceptListener) {
        View view = App.mainActivity.getLayoutInflater().inflate(R.layout.popup_select_inventory, null);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        alertDialogs.add(dialog);

        LinearLayout ll_vgo = view.findViewById(R.id.ll_vgo);
        LinearLayout ll_stickers = view.findViewById(R.id.ll_stickers);
        LinearLayout ll_kitties = view.findViewById(R.id.ll_kitties);
        AppButton bt_close = view.findViewById(R.id.bt_close);

        ll_vgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onAcceptListener.onSelect(1);
            }
        });

        ll_stickers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onAcceptListener.onSelect(12);
            }
        });

        ll_kitties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onAcceptListener.onSelect(7);
            }
        });

        bt_close.setSize(ButtonSize.SMALL);
        bt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }
}
