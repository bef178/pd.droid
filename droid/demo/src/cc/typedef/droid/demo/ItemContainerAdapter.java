package cc.typedef.droid.demo;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItemContainerAdapter extends BaseAdapter {

    private Context context;
    private List<Item> items = new LinkedList<>();

    public ItemContainerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = (Item) getItem(position);
        if (convertView == null) {
            convertView = View.inflate(context,
                    android.R.layout.simple_list_item_1, null);
        }
        TextView text1 = (TextView) convertView
                .findViewById(android.R.id.text1);
        text1.setText(item.caption);
        return convertView;
    }

    public void addItems(Item... items) {
        addItems(Arrays.asList(items));
    }

    public void addItems(Collection<Item> items) {
        this.items.addAll(items);
    }

    public void setItems(Item... items) {
        setItems(Arrays.asList(items));
    }

    public void setItems(Collection<Item> items) {
        this.items.clear();
        this.items.addAll(items);
    }

}
