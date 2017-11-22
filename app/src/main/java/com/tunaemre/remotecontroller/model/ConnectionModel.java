package com.tunaemre.remotecontroller.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ConnectionModel implements Serializable {
    public String ip;
    public String pin;
    public int port;
    private String token;

    public ConnectionModel() {

    }

    public ConnectionModel(JSONObject obj) throws JSONException {
        this.ip = obj.getString("IP");
        this.pin = obj.getString("PIN");
        this.port = obj.getInt("Port");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
