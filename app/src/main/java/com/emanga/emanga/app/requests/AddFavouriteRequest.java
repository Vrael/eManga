package com.emanga.emanga.app.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.utils.Internet;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ciro on 27/07/2014.
 */
public class AddFavouriteRequest extends Request<String> {
    private Map<String,String> mParams;
    private Response.Listener<String> mListener;

    public AddFavouriteRequest(String uuid, String manga_id,
                               Response.Listener<String> listener, Response.ErrorListener errorListener){
        super(Method.PUT, Internet.HOST + "user/" + uuid + "/add_favourite", errorListener);
        mListener = listener;
        mParams = new HashMap<String, String>();
        mParams.put("manga_id", manga_id);
    }

    @Override
    public Map<String, String> getParams() {
        return mParams;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        if(response.statusCode == 200 )
            return Response.success("OK", HttpHeaderParser.parseCacheHeaders(response));
        else
            return Response.error(new VolleyError("FAIL"));
    }

    @Override
    protected void deliverResponse(String response) {
        if(mListener != null){
            mListener.onResponse(response);
        }
    }
}
