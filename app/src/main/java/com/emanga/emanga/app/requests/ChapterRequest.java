package com.emanga.emanga.app.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.models.Chapter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Ciro on 01/04/2014.
 */
public class ChapterRequest extends JsonRequest<Chapter> {

    public static final String TAG = MangasRequest.class.getSimpleName();

    public ChapterRequest(int method, String url, Response.Listener<Chapter> listener, Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    @Override
    protected Response<Chapter> parseNetworkResponse(NetworkResponse response) {
        try {
            Chapter parsedData = App.getInstance().mMapper.readValue(response.data, Chapter.class);
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
