package com.example.wenzhao.helpinghand.ble.pro.Database;


public class ChildInfo {
    private long id;
    private String activity;
    private double ratio;
    private double finishtime;


    public ChildInfo(){

    }

    public ChildInfo( String activity, double finalRatio, double finishtime){
        this.activity = activity;
        this.ratio = finalRatio;
        this.finishtime = finishtime;
    }

    public ChildInfo(long id, String activity,double finalRatio ,double finishtime){
        this.id = id;
        this.activity = activity;
        this.ratio = finalRatio;
        this.finishtime = finishtime;
    }
    //setter

    public void setId(long id) {
        this.id = id;
    }

    public void setActivity(String activityName) {
        this.activity = activityName;
    }

    public void setFinalRatio(double finalRatio ){
        this.ratio = finalRatio;
    }

    public void setFinishtime(double finishtime ){
        this.finishtime = finishtime;
    }
    //getter



    public long getId() {
        return id;
    }

    public String getActivity(){
        return activity;
    }

    public double getFinalRatio() {
        return ratio;
    }

    public  double  getFinishtime(){
        return finishtime;
    }
}
