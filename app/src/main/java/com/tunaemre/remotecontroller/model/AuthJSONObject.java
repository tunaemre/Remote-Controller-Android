package com.tunaemre.remotecontroller.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthJSONObject extends JSONObject {
    public AuthJSONObject(String secret) throws JSONException {
        super();
        put("Secret", secret);
    }
}
