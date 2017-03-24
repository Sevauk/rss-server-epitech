package com.example.adrienmorel.rss;

import com.example.adrienmorel.rss.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.Jwts;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;
import static org.nanohttpd.protocols.http.response.Status.BAD_REQUEST;
import static org.nanohttpd.protocols.http.response.Status.FORBIDDEN;
import static org.nanohttpd.protocols.http.response.Status.INTERNAL_ERROR;
import static org.nanohttpd.protocols.http.response.Status.LENGTH_REQUIRED;
import static org.nanohttpd.protocols.http.response.Status.METHOD_NOT_ALLOWED;
import static org.nanohttpd.protocols.http.response.Status.NOT_FOUND;
import static org.nanohttpd.protocols.http.response.Status.OK;
import static org.nanohttpd.protocols.http.response.Status.UNAUTHORIZED;

public class Router extends NanoHTTPD {

    MainActivity mMainActivity;
    int port = 8080;

    public Router(MainActivity mainActivity) throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        mainActivity.log("Running on port " + port);
        mMainActivity = mainActivity;
    }

    private Response bad(IStatus status) {
        mMainActivity.log("error: " + status.getDescription());
        return newFixedLengthResponse(status, "plain/text", status.getDescription());
    }

    private Response good() {
        mMainActivity.log("OK 200");
        return newFixedLengthResponse("Success");
    }

    private Response answerJSON(JSONObject obj) {
        return newFixedLengthResponse(OK, "application/json", obj.toString());
    }

    private String readBody(IHTTPSession session) {

        Map<String, String> headers = session.getHeaders();

        int length;

        if (headers.containsKey("content-length"))
            length = Integer.parseInt(headers.get("content-length"));
        else if (headers.containsKey("Content-Length"))
            length = Integer.parseInt(headers.get("Content-Length"));
        else return null;

        try {
            Reader reader = new InputStreamReader(session.getInputStream(), "UTF-8");
            char[] content = new char[length];
            reader.read(content, 0, length);
            return new String(content);
        } catch (IOException e) {
            mMainActivity.log(e.toString());
            return null;
        }
    }

    public static Map<String, String> splitQuery(String url) throws UnsupportedEncodingException {


        Map<String, String> res = new HashMap<>();

        String[] pairs = url.split("\\&");
        for (int i = 0; i < pairs.length; i++) {
            String[] fields = pairs[i].split("=");
            String name = URLDecoder.decode(fields[0], "UTF-8");
            String value = URLDecoder.decode(fields[1], "UTF-8");
            res.put(name, value);
        }
        return res;
    }

    private Response authorization(IHTTPSession session) {


        if (session.getMethod() != Method.POST)
            return bad(NOT_FOUND);

        String body = readBody(session);

        if (body == null)
            return bad(LENGTH_REQUIRED);

        try {
            Map<String, String> args = splitQuery(body);

            List<User> users = User.find(User.class, "email = ?", args.get("email"));

            String jwt;

            if (users.size() == 0) {
                mMainActivity.log("New user " + args.get("email"));
                User newUser = new User();
                newUser.email = args.get("email");
                newUser.password = args.get("password");
                newUser.save();
                jwt = newUser.mkToken();
            } else {
                User u = users.get(0);
                if (!u.password.equals(args.get("password")))
                    return bad(FORBIDDEN);
                jwt = u.mkToken();
                mMainActivity.log("sending token " + jwt);
            }

            JSONObject tokenJSON = new JSONObject();
            try {
                tokenJSON.put("access_token", jwt);
                return answerJSON(tokenJSON);
            } catch (JSONException e) {
                return bad(INTERNAL_ERROR);
            }

        } catch (UnsupportedEncodingException e) {
            return bad(BAD_REQUEST);
        }
    }

    User userFromToken(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();

        try {


            String bearer = headers.get("authorization");
            String jwt = bearer.substring("Bearer ".length());

            String joe = Jwts.parser()
                    .setSigningKey("foo")
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();

            List<User> users = User.find(User.class, "email = ?", joe);

            if (users.isEmpty())
                return null;

            return users.get(0);

        } catch (Exception e) {
            mMainActivity.log(e.toString());
            return null;
        }
    }

    Response getFeed(IHTTPSession session) {
        if (session.getMethod() != Method.DELETE)
            return bad(METHOD_NOT_ALLOWED);

        User user = userFromToken(session);
        if (user == null)
            return bad(UNAUTHORIZED);

        String json = user.pullFeed();
        return newFixedLengthResponse(json);
    }

    Response putFeed(IHTTPSession session) {

        if (session.getMethod() != Method.PUT)
            return bad(METHOD_NOT_ALLOWED);

        User user = userFromToken(session);
        if (user == null)
            return bad(UNAUTHORIZED);

        try {
            Map<String, String> args = splitQuery(readBody(session));
            user.feeds.add(args.get("url"));
            user.update();

        } catch (Exception e) {
            return bad(BAD_REQUEST);
        }

        return good();
    }

    Response deleteFeed(IHTTPSession session) {

        if (session.getMethod() != Method.DELETE)
            return bad(METHOD_NOT_ALLOWED);

        User user = userFromToken(session);
        if (user == null)
            return bad(UNAUTHORIZED);
        return good();
    }

    Response feed(IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return getFeed(session);
            case PUT:
                return putFeed(session);
            case DELETE:
                return deleteFeed(session);
            default:
                return bad(METHOD_NOT_ALLOWED);
        }
    }

    Response route(String uri, IHTTPSession session) {

        String[] pts = uri.split("/");
        uri = "";
        for (String pt: pts)
            if (!pt.isEmpty())
                uri += pt + "/";

        switch (uri) {
            case "authorization/email/":
                return authorization(session);
            case "feed/":
                return feed(session);
            default:
                return newFixedLengthResponse("Invalid endpoint. Check out <a href='https://github.com/Sevauk/rss-server-epitech'>https://github.com/Sevauk/rss-server-epitech</a>");
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        mMainActivity.log("serving someone.");
        return route(session.getUri(), session);
    }

}
