package com.emanga.emanga.app.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.models.Page;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Ciro on 27/03/2014.
 */
public class PageRequest extends JsonRequest<Page> {

    public static final String TAG = MangasRequest.class.getSimpleName();

    public PageRequest(int method, String url, Response.Listener<Page> listener, Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    @Override
    protected Response<Page> parseNetworkResponse(NetworkResponse response) {
        try {
            Page parsedData = App.getInstance().mMapper.readValue(response.data, Page.class);
            return Response.success(parsedData, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonMappingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonParseException e) {
            return Response.error(new ParseError(e));
        } catch (IOException e) {
            return Response.error(new ParseError(e));
        }
    }
}
