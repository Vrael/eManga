package com.emanga.emanga.app.deserializers;

import com.emanga.emanga.app.models.Page;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Created by Ciro on 10/03/14.
 */
public class PageDeserializer extends JsonDeserializer<Page> {
    @Override
    public Page deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        String _id = ((ObjectNode) node.get("_id")).get("$oid").asText();

        Integer number = null;
        JsonNode numberNode = node.get("number");
        if(numberNode != null && !(numberNode instanceof NullNode)){
            number = (Integer) ((IntNode) numberNode).asInt();
        }

        String url = null;
        JsonNode urlNode = node.get("url");
        if(urlNode != null && !(urlNode instanceof NullNode)){
            url = urlNode.asText();
        }

        return new Page(_id, number, url, null);
    }
}
