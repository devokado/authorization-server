package com.devokado.authServer.util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author Alimodares
 * @since 2020-12-12
 */
public class HttpHelper {

    public static HttpResponse post(String url, List<NameValuePair> urlParameters) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(post);
    }

    public static HttpResponse put(String url, List<NameValuePair> urlParameters) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut put = new HttpPut(url);
        put.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(put);
    }

    public static HttpResponse put(String url, List<NameValuePair> urlParameters, Map<String, String> headers) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut put = new HttpPut(url);
        headers.forEach(put::addHeader);
        put.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(put);
    }

    public static HttpResponse put(String url, String raw, Map<String, String> headers) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut put = new HttpPut(url);
        headers.forEach(put::addHeader);
        put.setEntity(new StringEntity(raw));
        return client.execute(put);
    }
}
