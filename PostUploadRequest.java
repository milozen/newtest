package com.zhanghuang.net;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.zhanghuang.MainApplication;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.FormImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanlei on 2017/3/23.
 */

public class PostUploadRequest extends Request<String> {
    /**
     * 正确数据的时候回掉用
     */
    private RequestListener mListener ;
    /*请求 数据通过参数的形式传入*/
    private List<FormImage> mListItem ;

    private String BOUNDARY = "--------------"; //数据分隔线
    private String MULTIPART_FORM_DATA = "multipart/form-data";

    public PostUploadRequest(String url, List<FormImage> listItem, RequestListener response_listener,Response.ErrorListener error_listener) {
        super(Method.POST, url, error_listener);
        this.mListener = response_listener ;
        setShouldCache(false);
        mListItem = listItem ;
        //设置请求的响应事件，因为文件上传需要较长的时间，所以在这里加大了，设为5秒
        setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    /**
     * 这里开始解析数据
     * @param response Response from the network
     * @return
     */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String mString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(mString,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    /**
     * 回调正确的数据
     * @param response The parsed response returned by
     */
    @Override
    protected void deliverResponse(String response) {
        try {
            JSONObject jo = new JSONObject(response);
            this.mListener.response(jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map head_map = new HashMap();
//        head_map.put("Content-Type","multipart/form-data");
        head_map.put("token", MainApplication._pref.getString(Constants.PREF_TOKEN,""));
        return head_map;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mListItem == null||mListItem.size() == 0){
            return super.getBody() ;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int N = mListItem.size() ;
        FormImage formImage ;
        for (int i = 0; i < N ;i++){
            formImage = mListItem.get(i) ;
            StringBuffer sb= new StringBuffer() ;
            /*第一行*/
            //`"--" + BOUNDARY + "\r\n"`
            sb.append("--"+BOUNDARY);
            sb.append("\r\n") ;
            /*第二行*/
            //Content-Disposition: form-data;filename="上传的文件名" + "\r\n"
            sb.append("Content-Disposition: form-data;");
            sb.append(" name=\"");
            sb.append("avatar-file");
            sb.append("\"") ;
            sb.append("; filename=\"") ;
            sb.append(formImage.getFileName()) ;
            sb.append("\"");
            sb.append("\r\n") ;
            /*第三行*/
            //Content-Type: 文件的 mime 类型 + "\r\n"
            sb.append("Content-Type: ");
            sb.append(formImage.getMime()) ;
            sb.append("\r\n") ;
            /*第四行*/
            //"\r\n"
            sb.append("\r\n") ;
            try {
                bos.write(sb.toString().getBytes("utf-8"));
                /*第五行*/
                //文件的二进制数据 + "\r\n"
                bos.write(formImage.getValue());
                bos.write("\r\n".getBytes("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        /*结尾行*/
        //`"--" + BOUNDARY + "--" + "\r\n"`
        String endLine = "--" + BOUNDARY + "--" + "\r\n" ;
        try {
            bos.write(endLine.toString().getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Log.v("zgy", "=====formImage====\n" + bos.toString()) ;
        return bos.toByteArray();
    }
    //Content-Type: multipart/form-data; boundary=----------8888888888888
    @Override
    public String getBodyContentType() {
        return MULTIPART_FORM_DATA+"; boundary="+BOUNDARY;
    }
}
