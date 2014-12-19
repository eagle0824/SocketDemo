
package com.eagle.socket;

/**
 * list item object
 * 
 * @author eagle
 */
public class DataObject {

    public enum Type {
        LOCAL, OTHER,
    }

    private Type mType;
    private String mData;
    private String mTitle;

    public DataObject(Type type, String title, String data) {
        mType = type;
        mData = data;
        mTitle = title;
    }

    public Type getType() {
        return mType;
    }

    public String getData() {
        return mData;
    }

    public String getTitle() {
        return mTitle;
    }
}
