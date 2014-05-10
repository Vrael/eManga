package com.emanga.emanga.app.deserializers;

import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.models.Author;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.utils.Dates;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ciro on 24/02/14.
 */
public class MangaDeserializer extends JsonDeserializer<Manga> {

    @Override
    public Manga deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);

        String _id = node.get("_id").get("$oid").asText();

        String title = null;
        JsonNode titleNode = node.get("title");
        if(titleNode != null && !(titleNode instanceof NullNode)){
          title = titleNode.asText();
        }

        List<Author> authors = null;
        JsonNode authorsNode = node.get("authors");
        if(authorsNode != null && !(authorsNode instanceof NullNode)){
            ArrayNode authorsNames = (ArrayNode) authorsNode;
            authors = new ArrayList<Author>(authorsNames.size());
            for(int i = 0; i < authorsNames.size(); i++){
                authors.add(new Author(authorsNames.get(i).asText()));
            }
        }

        String summary = null;
        JsonNode summaryNode = node.get("summary");
        if(summaryNode != null && !(summaryNode instanceof NullNode)){
          summary = summaryNode.asText();
        }

        String cover = null;
        JsonNode coverNode = node.get("cover");
        if(coverNode != null && !(coverNode instanceof NullNode)){
           cover = coverNode.asText();
        }

        List<Genre> genres = null;
        JsonNode genresNode = node.get("genres");
        if(genresNode != null && !(genresNode instanceof NullNode)){
          ArrayNode genresNames = (ArrayNode) genresNode;
          genres = new ArrayList<Genre>(genresNames.size());
          for(int i = 0; i < genresNames.size(); i++){
            genres.add(new Genre(genresNames.get(i).asText()));
          }
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

        Date modified_at = null;
        JsonNode modified_atNode = node.get("modify_at");
        if(modified_atNode != null && !(modified_atNode instanceof NullNode)){
            try {
                modified_at = Dates.sdf.parse(modified_atNode.asText());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int numberChapters = 0;
        JsonNode n_chapters_atNode = node.get("n_chapters");
        if(n_chapters_atNode != null && !(n_chapters_atNode instanceof NullNode)){
            numberChapters = n_chapters_atNode.asInt();
        }

        Manga manga = new Manga(_id, title, authors, cover, summary, created_at, modified_at,
                genres, numberChapters);

        JsonNode chaptersNode = node.get("chapters");
        if(chaptersNode != null && !(chaptersNode instanceof NullNode)){
            manga.chapters = App.getInstance().mMapper.readValue(chaptersNode.toString(),
                    new TypeReference<Collection<Chapter>>(){});
            if(manga.chapters != null) {
                Iterator<Chapter> it = manga.chapters.iterator();
                while(it.hasNext()){
                    it.next().manga = manga;
                }
            }
        }

        return manga;
    }
}
