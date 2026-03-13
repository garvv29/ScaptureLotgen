package com.seedtrac.lotgen.parser.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("mobile1")
    @Expose
    private String mobile1;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("scode")
    @Expose
    private String scode;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("pendingactivate")
    @Expose
    private String pendingactivate;
    @SerializedName("pendingloading")
    @Expose
    private String pendingloading;

    public String getMobile1() {
        return mobile1;
    }

    public void setMobile1(String mobile1) {
        this.mobile1 = mobile1;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPendingactivate() {
        return pendingactivate;
    }

    public void setPendingactivate(String pendingactivate) {
        this.pendingactivate = pendingactivate;
    }

    public String getPendingloading() {
        return pendingloading;
    }

    public void setPendingloading(String pendingloading) {
        this.pendingloading = pendingloading;
    }
}
