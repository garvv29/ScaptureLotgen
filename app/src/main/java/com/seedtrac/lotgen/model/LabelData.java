package com.seedtrac.lotgen.model;

public class LabelData {
    public String crop;
    public String variety1;
    public String variety2;
    public String weight;
    public String date;
    public String lotNo;
    public String qrcode;
    public String Nob;


    public LabelData(String crop, String variety1, String variety2,
                     String weight, String date, String lotNo,String Nob, String qrcode) {
        this.crop = crop;
        this.variety1 = variety1;
        this.variety2 = variety2;
        this.weight = weight;
        this.date = date;
        this.lotNo = lotNo;
        this.Nob=Nob;
        this.qrcode = qrcode;
    }
}
