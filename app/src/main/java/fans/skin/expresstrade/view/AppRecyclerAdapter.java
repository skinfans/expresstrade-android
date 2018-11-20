package fans.skin.expresstrade.view;

import android.content.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

public abstract class AppRecyclerAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public Context context;
    public LayoutInflater inflater;
    public List data;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public AppRecyclerAdapter() {
    }

    public AppRecyclerAdapter(Context context) {
        this.context = context;
    }

    public AppRecyclerAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
        this.data = new ArrayList();
    }

    public AppRecyclerAdapter(Context context, LayoutInflater inflater, List data) {
        this.context = context;
        this.inflater = inflater;
        this.data = data;
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void updateData(List data) {
        if (data == null) return;
        this.data = data;
    }

    public void pushData(List data) {
        this.data.addAll(data);
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(T holder, int position) {

    }

    // position id
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    /*@Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }*/
}
