package com.example.adrienmorel.rss.model;

import android.util.Log;

import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

public class User extends SugarRecord {

    public static class Feed extends SugarRecord {

        public String url;
        public User user;

        public Feed() {}
    }

    public User() {}

    public String email;
    public String password;
    public List<Feed> feeds = new ArrayList<>();

    public String mkToken() {

        return Jwts.builder()
                .setSubject(email)
                .signWith(SignatureAlgorithm.HS512, "foo")
                .compact();
    }

    public String pullFeed() {

        JSONObject jsonObject = new JSONObject();
        ArrayList<RssItem> rssItems = new ArrayList<>();

        try {

            for (Feed feed: feeds) {

                try {
                    URL url = new URL(feed.url);
                    RssFeed rssFeed = RssReader.read(url);
                    rssItems.addAll(rssFeed.getRssItems());
                } catch (SAXException|IOException e) {
                    Log.d("User", "invalid url " + feed);
                }
            }

            Collections.sort(rssItems, new Comparator<RssItem>() {
                public int compare(RssItem o1, RssItem o2) {
                    return o2.getPubDate().compareTo(o1.getPubDate());
                }
            });

            JSONArray items = new JSONArray();
            jsonObject.put("items", items);

            for (RssItem item: rssItems) {

                JSONObject itemJson = new JSONObject();

                itemJson.put("title", item.getTitle());
                itemJson.put("desription", item.getDescription());
                itemJson.put("link", item.getLink());
                itemJson.put("date", item.getPubDate());
                items.put(itemJson);
            }

        } catch (JSONException e) {
            return null;
        }

        return jsonObject.toString();
    }
}
