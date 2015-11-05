package common.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import play.mvc.Http;
import play.mvc.Http.MultipartFormData.FilePart;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpUtil {

    public static List<File> getMultipartFormDataFiles(Http.MultipartFormData multipartFormData, String prefix, int count) {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            File file = getMultipartFormDataFile(multipartFormData, prefix+i);
            if (file == null) {
                break;
            }
            files.add(file);
        }
        return files;
    }
    
    public static File getMultipartFormDataFile(Http.MultipartFormData multipartFormData, String key) {
        FilePart filePart = multipartFormData.getFile(key);
        if (filePart != null) {
            return filePart.getFile();
        }
        return null;
    }
    
    public static String getMultipartFormDataString(Http.MultipartFormData multipartFormData, String key) {
        String[] data = multipartFormData.asFormUrlEncoded().get(key);
        if (data != null) {
            return data[0];
        }
        return null;
    }
    
    public static Long getMultipartFormDataLong(Http.MultipartFormData multipartFormData, String key) {
        String str = getMultipartFormDataString(multipartFormData, key);
        if (str != null) {
            try {
                return Long.valueOf(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static Double getMultipartFormDataDouble(Http.MultipartFormData multipartFormData, String key) {
        String str = getMultipartFormDataString(multipartFormData, key);
        if (str != null) {
            try {
                return Double.valueOf(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    public static Boolean getMultipartFormDataBoolean(Http.MultipartFormData multipartFormData, String key) {
        String str = getMultipartFormDataString(multipartFormData, key);
        if (str != null) {
            try {
                return Boolean.valueOf(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlToRead);
        HttpResponse response = client.execute(request);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
