package com.example.android.getmustop;

public class Istoriko {

    private int id;
    private String list;
    private String date;
    public Istoriko(){

    }
    public Istoriko(int id, String list, String date) {

        this.id=id;
        this.list=list;
        this.date=date;
    }

    public void setId(int id){ this.id=id; }
    public void setList(String list) { this.list = list; }
    public void setDate(String date) { this.date = date; }

    public int getId() { return id; }
    public String getList() { return list; }
    public String getDate() { return date; }

}
