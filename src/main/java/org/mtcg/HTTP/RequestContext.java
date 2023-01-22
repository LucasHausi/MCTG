package org.mtcg.HTTP;

import java.util.ArrayList;
import java.util.List;

public class RequestContext {

    //static because it's the same for every class instance
    private static final String CONTENT_LENGTH_HEADER_NAME = "Content-Length";
    private String httpVerb;
    private String path;
    private List<Header> headers;
    private String body;

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }
    public String getAuthToken(){
        String s =  this.headers.stream()
                .filter(tempHeader -> "Authorization".equals(tempHeader.getName()))
                .findFirst().map(Header::getValue)
                .orElse(null);
        return s;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getContentLength(){
        return headers.stream()
                .filter(header -> CONTENT_LENGTH_HEADER_NAME.equals(header.getName()))
                .findFirst()
                .map(Header::getValue)
                .map(Integer::parseInt)
                .orElse(0);
    }

    //DEV Functions
    /*
    public void print(){
        System.out.println("Method: " + this.httpVerb);
        System.out.println("Path: "+ this.path);
        System.out.println("Body: "+ this.body);
        System.out.println("Headers:" + this.headers);
    }*/
}
