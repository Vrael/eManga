package com.emanga.emanga.app.deserializers;

import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Page;
import com.emanga.emanga.app.utils.Dates;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Ciro on 25/02/14.
 */
public class ChapterDeserializer extends JsonDeserializer<Chapter> {
    @Override
    public Chapter deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);

        String _id = null;
        JsonNode _idNode = node.get("_id");
        if(_idNode != null && !(_idNode instanceof NullNode)){
           _id = ((ObjectNode) _idNode).get("$oid").asText();
        }

        Integer number = null;
        JsonNode numberNode = node.get("number");
        if(numberNode != null && !(numberNode instanceof NullNode)){
            number = (Integer) ((IntNode) numberNode).asInt();
        }


        Date created_at = null;
        JsonNode created_atNode = node.get("created_at");
        if(created_atNode != null && !(created_atNode instanceof NullNode)){
            try {
                created_at = Dates.sdf.parse(created_atNode.asText());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Chapter chapter = new Chapter(_id, number, created_at, null);

        JsonNode pagesNode = node.get("pages");
        if(pagesNode != null && !(pagesNode instanceof NullNode)){
            chapter.pages = App.getInstance().mMapper.readValue(pagesNode.toString(), new TypeReference<Collection<Page>>(){});
            if(chapter.pages != null) {
                Iterator<Page> it = chapter.pages.iterator();
                while(it.hasNext()){
                    it.next().chapter = chapter;
                }
            }
        }

        return chapter;
    }
}
