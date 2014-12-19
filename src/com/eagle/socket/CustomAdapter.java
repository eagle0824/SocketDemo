/**
 * list adapter
 */

package com.eagle.socket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eagle.socket.DataObject.Type;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<DataObject> mDatas;
    private LayoutInflater mInflater;

    public CustomAdapter(Context context) {
        mContext = context;
        mDatas = new ArrayList<DataObject>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public DataObject getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DataObject data = mDatas.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
        }
        TextView title = null;
        TextView message = null;

        switch (data.getType()) {
            case LOCAL:
                title = (TextView) convertView.findViewById(R.id.local_title);
                message = (TextView) convertView.findViewById(R.id.local_message);
                convertView.findViewById(R.id.other_title).setVisibility(View.GONE);
                convertView.findViewById(R.id.other_message).setVisibility(View.GONE);
                convertView.findViewById(R.id.local_split).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.other_split).setVisibility(View.GONE);
                break;
            case OTHER:
                title = (TextView) convertView.findViewById(R.id.other_title);
                message = (TextView) convertView.findViewById(R.id.other_message);
                convertView.findViewById(R.id.local_title).setVisibility(View.GONE);
                convertView.findViewById(R.id.local_message).setVisibility(View.GONE);
                convertView.findViewById(R.id.local_split).setVisibility(View.GONE);
                convertView.findViewById(R.id.other_split).setVisibility(View.VISIBLE);
                break;
        }
        if (title != null && message != null) {
            title.setVisibility(View.VISIBLE);
            message.setVisibility(View.VISIBLE);
            title.setText(data.getTitle());
            message.setText(data.getData());
        }

        return convertView;
    }

    public void add(DataObject data) {
        mDatas.add(data);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<DataObject> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void clearAll() {
        mDatas.clear();
        notifyDataSetChanged();
    }
}
