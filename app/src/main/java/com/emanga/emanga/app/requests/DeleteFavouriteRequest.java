package com.emanga.emanga.app.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.emanga.emanga.app.utils.Internet;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ciro on 27/07/2014.
 */
public class DeleteFavouriteRequest extends Request<String> {
    private Map<String,String> mParams;
    private Response.Listener<String> mListener;

    public DeleteFavouriteRequest(String uuid, String manga_id,
                                  Response.Listener<String> listener, Response.ErrorListener errorListener){
        super(Method.PUT, Internet.HOST + "user/" + uuid + "/del_favourite", errorListener);
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
