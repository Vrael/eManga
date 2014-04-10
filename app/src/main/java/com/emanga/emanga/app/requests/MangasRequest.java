package com.emanga.emanga.app.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.models.Manga;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Ciro on 1/03/14.
 */
public class MangasRequest extends JsonRequest<Manga[]> {

    public MangasRequest(int method, String url, Response.Listener<Manga[]> listener, Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    @Override
    protected Response<Manga[]> parseNetworkResponse(NetworkResponse response) {
        try {
            Manga[] parsedData = App.getInstance().mMapper.readValue(response.data, Manga[].class);
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
