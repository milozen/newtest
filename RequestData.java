package com.zhanghuang.net;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.squareup.haha.guava.collect.Maps;
import com.zhanghuang.MainApplication;
import com.zhanghuang.bean.ArticleBean;
import com.zhanghuang.bean.RecordBean;
import com.zhanghuang.bean.SectionBean;
import com.zhanghuang.bean.YearRecordBean;
import com.zhanghuang.db.DaoManager;
import com.zhanghuang.entity.ArticleBeanDao;
import com.zhanghuang.entity.RecordBeanDao;
import com.zhanghuang.entity.SectionBeanDao;
import com.zhanghuang.entity.YearRecordBeanDao;
import com.zhanghuang.modes.Article;
import com.zhanghuang.modes.ArticlesMode;
import com.zhanghuang.modes.BaseMode;
import com.zhanghuang.modes.ChannerItem;
import com.zhanghuang.modes.ChannersMode;
import com.zhanghuang.modes.Fav;
import com.zhanghuang.modes.FavsMode;
import com.zhanghuang.modes.PayResultMode;
import com.zhanghuang.modes.PreOrderMode;
import com.zhanghuang.modes.Product;
import com.zhanghuang.modes.ProductMode;
import com.zhanghuang.modes.Record;
import com.zhanghuang.modes.RecordsMode;
import com.zhanghuang.modes.Splash;
import com.zhanghuang.modes.SplashMode;
import com.zhanghuang.modes.StisticsInfo;
import com.zhanghuang.modes.StringMode;
import com.zhanghuang.modes.User;
import com.zhanghuang.modes.VsnMode;
import com.zhanghuang.modes.YearRecord;
import com.zhanghuang.modes.YearRecordsMode;
import com.zhanghuang.netinterface.BaseInterface;
import com.zhanghuang.netinterface.RespDataInterface;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.FormImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanlei on 2017/3/16.
 * 请求数据的方法都在此
 */

public class RequestData {
    private final Context context;

    private final String DEFAULT_ERROR_MSG = "服务器君开小差了！";

    public RequestData(Context context) {
        this.context = context;
    }

    private void httpGet(String url, BaseInterface iface, RespDataInterface rdface) {
        AppRequestUtil.getInstance().getRequest(context, url, (JSONObject jsonObject) -> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    BaseMode data = rdface.parseData(jsonObject);
                    iface.response(true, data, "ok", null);
                } else {
                    iface.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iface.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        });
    }

    private void httpPost(String url, Map<String, String> map, BaseInterface iface, RespDataInterface rdface) {
        AppRequestUtil.getInstance().postRequest(context, url, map, (JSONObject jsonObject) -> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    BaseMode data = rdface.parseData(jsonObject);
                    iface.response(true, data, "ok", null);
                } else {
                    iface.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iface.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        });
    }
    public void getSplash(BaseInterface iface) {
        String url = Constants.BASEURL_ZZ + "/app/launch";
        httpGet(url, iface, (JSONObject jsonObject) -> {
            SplashMode sm = new SplashMode();
            Gson g = new Gson();
            JSONArray launchJa = jsonObject.getJSONArray("launch");
            Splash sLaunch = g.fromJson(launchJa.getJSONObject(0).toString(), Splash.class);
            sm.setLaunch(sLaunch);

            if (jsonObject.has("mainAd")) {
                JSONArray adJa = jsonObject.getJSONArray("mainAd");
                Splash sAds = g.fromJson(adJa.getJSONObject(0).toString(), Splash.class);
                sm.setMainAds(sAds);
            }

            if (jsonObject.has("guilds")) {
                JSONArray ja = jsonObject.getJSONArray("guilds");
                List<Splash> list = new ArrayList<>();
                Splash sGuild;
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    sGuild = g.fromJson(jo.toString(), Splash.class);
                    list.add(sGuild);
                }
                sm.setGulids(list);
            }
            return sm;
        });
    }

    //2:登录
    public void login(String phone, String pass, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/auth ";
        Map<String, String> map = new HashMap<>();
        map.put("mobile", phone);
        map.put("password", pass);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("token")));
    }


    //3:获取验证码
    public void getCode(String phone, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/captcha";
        Map<String, String> map = new HashMap<>();
        map.put("mobile", phone);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("code")));
    }

    //4:注册
    public void regist(String phone, String pass, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/register";
        Map<String, String> map = new HashMap<>();
        map.put("mobile", phone);
        map.put("password", pass);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("token")));
    }

    //5:获取用户信息
    public void getUserInfo(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/user-info";
        httpPost(url, null, iFace, (JSONObject jsonObject)->{
            SharedPreferences.Editor editor = MainApplication._pref.edit();
            User u = new User();
            u.setId(jsonObject.getString("id"));
            u.setMobile(jsonObject.getString("mobile"));
            if (jsonObject.has("nick")) {
                u.setNick(jsonObject.getString("nick"));
                editor.putString(Constants.PREF_USER_NICK, jsonObject.getString("nick"));
            }
            if (jsonObject.has("avatar")) {
                u.setAvatar(jsonObject.getString("avatar"));
                editor.putString(Constants.PREF_USER_AVATAR, jsonObject.getString("avatar"));
            }
            JSONObject jo = jsonObject.getJSONObject("level_info");
            u.setLevel(jo.getString("level"));
            u.setName(jo.getString("name"));
            editor.putString(Constants.PREF_USER_LEVEL, jo.getString("name"));
            if (jsonObject.has("sex")) {
                u.setSex(jsonObject.getString("sex"));
            }
            if (jsonObject.has("height")) {
                u.setHeight(jsonObject.getString("height"));
            }
            if (jsonObject.has("weight")) {
                u.setWeight(jsonObject.getString("weight"));
            }
            if (jsonObject.has("birth")) {
                u.setBirth(jsonObject.getString("birth"));
            }
            if (jsonObject.has("location")) {
                u.setLocation(jsonObject.getString("location"));
            }
            if (jsonObject.has("showid")) {
                u.setShowid(jsonObject.getString("showid"));
                editor.putString(Constants.PREF_USER_SHOWID, jsonObject.getString("showid"));
            }
            if (jsonObject.has("weixin")) {
                u.setWeixin(jsonObject.getString("weixin"));
            }
            if (jsonObject.has("qq")) {
                u.setQq(jsonObject.getString("qq"));
            }
            if (jsonObject.has("weibo")) {
                u.setWeibo(jsonObject.getString("weibo"));
            }
            if (jsonObject.has("vip")) {
                int vip = jsonObject.getInt("vip");
                u.setVip(vip);
                editor.putInt(Constants.PREF_ZZ_IS_VIP, vip);
            }
            if (jsonObject.has("vip_exp_str")) {
                u.setVipExpTimeStr(jsonObject.getString("vip_exp_str"));
                editor.putString(Constants.PREF_ZZ_VIP_EXP_STR, u.getVipExpTimeStr());
            }
            if (jsonObject.has("vip_exp_time")) {
                u.setVipExpTime(jsonObject.getLong("vip_exp_time"));
                editor.putLong(Constants.PREF_ZZ_VIP_EXP_TIME, u.getVipExpTime());
            }
            editor.apply();
            return u;
        });
    }

    //6:获取我的收藏
    public void getFav(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/fav";
        httpPost(url, null, iFace, (JSONObject jsonObject) -> {
            List<Fav> list = new ArrayList<>();
            Fav f;
            Gson g = new Gson();
            JSONArray ja = jsonObject.getJSONArray("fav");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                f = g.fromJson(jo.toString(), Fav.class);
                list.add(f);
            }
            FavsMode fms = new FavsMode();
            fms.setFavList(list);
            return fms;
        });
    }

    //7:收藏资讯
    public void addFav(String id, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/fav/add";
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        httpPost(url, map, iFace, (JSONObject jsonObject) -> null);
    }

    //8:删除收藏
    public void delFav(String id, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/fav/delete";
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        httpPost(url, map, iFace, (JSONObject jsonObject) -> null);
    }

    //9:判断收藏
    public void checkFav(String id, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/fav/" + id;
        httpPost(url, null, iFace, (JSONObject jsonObject) -> {
            BaseMode bm = new BaseMode();
            boolean isFav = jsonObject.getBoolean("fav");
            bm.setStatus(isFav);
            return bm;
        });
    }



    //10:上传头像
    public void uploadAvatar(List<FormImage> list, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/upload/avatar";
        PostUploadRequest request = new PostUploadRequest(url, list, (JSONObject jsonObject)-> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    iFace.response(true, null, jsonObject.getString("avatar"), null);
                } else {
                    iFace.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iFace.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        }, (VolleyError volleyError) -> iFace.response(false, null, null, volleyError.getMessage()));
        //request.setTag(context);
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        request.setShouldCache(true);
        VolleyUtil.getQueue(context).add(request);
    }

    //11:获取头像
    public void getAvatar(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/avatar";
        httpPost(url, null, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("avatar")));
    }

    //12:设置昵称
    public void setNick(String nick, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/nickname/set";
        Map<String, String> map = new HashMap<>();
        map.put("nick", nick);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("nick")));
    }

    //13:获取昵称
    public void getNick(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/nickname";
        AppRequestUtil.getInstance().postRequest(context, url, null, (JSONObject jsonObject) -> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    iFace.response(true, null, jsonObject.getString("nick"), null);
                } else {
                    iFace.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iFace.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        });
    }


    //14:获取所有栏目
    public void getSections(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/sections";
        httpGet(url, iFace, (JSONObject jsonObject) -> {
            List<ChannerItem> list = new ArrayList<>();
            ChannerItem ci;
            Gson g = new Gson();
            JSONArray ja = jsonObject.getJSONArray("sections");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                ci = g.fromJson(jo.toString(), ChannerItem.class);
                list.add(ci);
                saveSectionToDb(ci);
            }
            ChannersMode cm = new ChannersMode();
            cm.setChannerList(list);
            return cm;
        });
    }

    private void saveSectionToDb(ChannerItem ci) {
        SectionBeanDao sectionBeanDao = DaoManager.getInstance().getDaoSession().getSectionBeanDao();
        List<SectionBean> list = sectionBeanDao.queryBuilder().where(SectionBeanDao.Properties.Id.eq(ci.getId())).list();
        if (list != null && list.size() > 0) {
            return;
        }

        SectionBean sb = new SectionBean();
        sb.setId(ci.getId());
        sb.setName(ci.getName());
        sectionBeanDao.insert(sb);
    }

    //15:获取栏目的资讯列表
    public void getArticles(String sectionId, int start, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/articles/" + sectionId + "/%7b" + start + "," + 20 + "%7d";
        httpGet(url, iFace, (JSONObject jsonObject)-> {
            int count = jsonObject.getInt("count");
            List<Article> list = new ArrayList<>();
            JSONArray ja = jsonObject.getJSONArray("articles");
            Article ac;
            Gson g = new Gson();
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                ac = g.fromJson(jo.toString(), Article.class);
                list.add(ac);
            }
            ArticlesMode am = new ArticlesMode();
            am.setCount(count);
            am.setArticleList(list);
            return am;
        });
    }

    //16:获取精选的资讯列表
    public void getClassicArticles(int start, int length, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/articles/index/%7b" + start + "," + length + "%7d";
        AppRequestUtil.getInstance().getRequest(context, url, (JSONObject jsonObject)-> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    int count = jsonObject.getInt("count");
                    int _start = jsonObject.getInt("start");
                    List<Article> list = new ArrayList<>();
                    JSONArray ja = jsonObject.getJSONArray("articles");
                    Article ac;
                    Gson g = new Gson();
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = ja.getJSONObject(i);
                        ac = g.fromJson(jo.toString(), Article.class);
                        if (_start == 0) {
                            saveArticleToDb(ac);
                        }
                        list.add(ac);
                    }
                    ArticlesMode am = new ArticlesMode();
                    am.setCount(count);
                    am.setArticleList(list);
                    iFace.response(true, am, null, null);
                } else {
                    iFace.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iFace.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        });
    }

    /**
     */
    private void saveArticleToDb(Article article) {
        ArticleBeanDao articleBeanDao = DaoManager.getInstance().getDaoSession().getArticleBeanDao();

        List<ArticleBean> list = articleBeanDao.queryBuilder().where(ArticleBeanDao.Properties.Id.eq(article.getId())).list();
        if (list != null && list.size() > 0) {
            return;
        }

        ArticleBean ab = new ArticleBean();
        ab.setId(article.getId());
        ab.setAvatar(article.getAvatar());
        ab.setDesc(article.getDesc());
        ab.setImg(article.getImg());
        ab.setIsad(article.isIsad());
        ab.setNick(article.getNick());
        ab.setSrc(article.getSrc());
        ab.setStamp(article.getStamp());
        ab.setTitle(article.getTitle());

        articleBeanDao.insert(ab);
    }

    //17:保存站桩记录
    public void saveZz(String id, String title, String begin_time, String end_time, String duration, String desc, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/activity/add";
        Map<String, String> map = new HashMap<>();
        if (id != null && !id.equals("")) {
            map.put("id", id);
        }
        map.put("title", title);
        map.put("begin_time", begin_time);
        map.put("end_time", end_time);
        map.put("duration", duration);
        if (desc != null && !desc.equals("")) {
            map.put("desc", desc);
        }
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("id")));
    }

    //18:获取站桩记录列表
    public void getZzList(int year, int month, int start, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/activity/" + year + "/" + month + "/%7b" + start + "," + 20 + "%7d";
        httpGet(url, iFace, (JSONObject jsonObject) -> {
            RecordsMode rm = new RecordsMode();
            int count = jsonObject.getInt("count");
            rm.setCount(count);
            List<Record> list = new ArrayList<>();
            if (count > 0) {
                JSONArray ja = jsonObject.getJSONArray("activitys");
                Gson g = new Gson();
                Record record;
                for (int i = 0; i < ja.length(); i++) {
                    record = g.fromJson(ja.getJSONObject(i).toString(), Record.class);
                    record.setHasUpload(true);
                    saveRecord(record);
                    list.add(record);
                }
            }
            rm.setRecordList(list);
            return rm;
        });
    }



    private void saveRecord(Record rd) {
        RecordBeanDao recordBeanDao = DaoManager.getInstance().getDaoSession().getRecordBeanDao();
        List<RecordBean> list = recordBeanDao.queryBuilder().where(RecordBeanDao.Properties.Rid.eq(rd.getId())).list();
        if (list != null && list.size() > 0) {
            return;
        }
        RecordBean rb = new RecordBean();
        rb.setRid(rd.getId());
        rb.setBegin_time(rd.getBegin_time());
        rb.setDesc(rd.getDesc());
        rb.setDuration(rd.getDuration());
        rb.setEnd_time(rd.getEnd_time());
        rb.setHasUpload(true);
        rb.setMonth(rd.getMonth());
        rb.setTitle(rd.getTitle());
        rb.setYear(rd.getYear());
        recordBeanDao.insert(rb);
    }

    //19:删除站桩记录
    public void delZz(String id, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/activity/delete";
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("id")));
    }

    //20:保存用户信息
    public void saveUserInfo(BaseInterface iFace, String nick, String sex, String height, String weight, String birth, String location) {
        String url = Constants.BASEURL_ZZ + "/user-info/set";
        Map<String, String> map = new HashMap<>();
        if (nick != null && !nick.equals("")) {
            map.put("nick", nick);
        }
        if (sex != null && !sex.equals("")) {
            map.put("sex", sex);
        }
        if (height != null && !height.equals("")) {
            map.put("height", height);
        }
        if (weight != null && !weight.equals("")) {
            map.put("weight", weight);
        }
        if (birth != null && !birth.equals("")) {
            map.put("birth", birth);
        }
        if (location != null && !location.equals("")) {
            map.put("location", location);
        }
        httpPost(url, map, iFace, jsonObject -> {
            SharedPreferences.Editor editor = MainApplication._pref.edit();
            User u = new User();
            u.setId(jsonObject.getString("id"));
            u.setMobile(jsonObject.getString("mobile"));
            if (jsonObject.has("nick")) {
                u.setNick(jsonObject.getString("nick"));
                editor.putString(Constants.PREF_USER_NICK, jsonObject.getString("nick"));
            }
            if (jsonObject.has("avatar")) {
                u.setAvatar(jsonObject.getString("avatar"));
                editor.putString(Constants.PREF_USER_AVATAR, jsonObject.getString("avatar"));
            }
            JSONObject jo = jsonObject.getJSONObject("level_info");
            u.setLevel(jo.getString("level"));
            u.setName(jo.getString("name"));
            editor.putString(Constants.PREF_USER_LEVEL, jo.getString("name"));
            if (jsonObject.has("sex")) {
                u.setSex(jsonObject.getString("sex"));
            }
            if (jsonObject.has("height")) {
                u.setHeight(jsonObject.getString("height"));
            }
            if (jsonObject.has("weight")) {
                u.setWeight(jsonObject.getString("weight"));
            }
            if (jsonObject.has("showid")) {
                u.setShowid(jsonObject.getString("showid"));
                editor.putString(Constants.PREF_USER_SHOWID, jsonObject.getString("showid"));
            }
            if (jsonObject.has("birth")) {
                u.setBirth(jsonObject.getString("birth"));
            }
            if (jsonObject.has("location")) {
                u.setLocation(jsonObject.getString("location"));
            }
            editor.apply();
            return u;
        });
    }



    //21:获取年度记录
    public void getYearRecords(String year, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/statistics/" + year;
        httpGet(url, iFace, jsonObject -> {
            List<YearRecord> list = new ArrayList<>();
            JSONArray ja = jsonObject.getJSONArray("results");
            Gson g = new Gson();
            YearRecord record;
            for (int i = 0; i < ja.length(); i++) {
                record = g.fromJson(ja.getJSONObject(i).toString(), YearRecord.class);
                saveYearRecord(record);
                list.add(record);
            }
            YearRecordsMode yrm = new YearRecordsMode();
            yrm.setYearRecordList(list);
            return yrm;
        });
    }


    private void saveYearRecord(YearRecord yr) {
        YearRecordBeanDao yearRecordBeanDao = DaoManager.getInstance().getDaoSession().getYearRecordBeanDao();
        List<YearRecordBean> list = yearRecordBeanDao.queryBuilder().where(YearRecordBeanDao.Properties.Yid.eq(yr.get_id())).list();
        if (list != null && list.size() > 0) {
            YearRecordBean yrb1 = list.get(0);
            yrb1.setCount_time(yr.getCount_time());
            yrb1.setCount(yr.getCount());
            yearRecordBeanDao.update(yrb1);
        } else {
            YearRecordBean yrb = new YearRecordBean();
            yrb.setYid(yr.get_id());
            yrb.setCount(yr.getCount());
            yrb.setCount_time(yr.getCount_time());
            yearRecordBeanDao.insert(yrb);
        }
    }

    //22:获取月度记录
    public void getMonthRecords(BaseInterface iFace, String year) {
        String url = Constants.BASEURL_ZZ + "/statistics/" + year;
        httpGet(url, iFace, jsonObject -> {
            List<YearRecord> list = new ArrayList<>();
            JSONArray ja = jsonObject.getJSONArray("results");
            Gson g = new Gson();
            YearRecord record;

            for (int i = 0; i < ja.length(); i++) {
                record = g.fromJson(ja.getJSONObject(i).toString(), YearRecord.class);
                saveYearRecord(record);
                list.add(record);
            }
            YearRecordsMode yrm = new YearRecordsMode();
            yrm.setYearRecordList(list);
            return yrm;
        });
    }

    //23:获取所有记录
    public void getAllRecords(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/statistics";
        AppRequestUtil.getInstance().getRequest(context, url, (JSONObject jsonObject)-> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    String count = jsonObject.getString("count");
                    String count_time = jsonObject.getString("count_time");
                    SharedPreferences.Editor editor = MainApplication._pref.edit();
                    editor.putString(Constants.PREF_ALL_COUNT, count);
                    editor.putString(Constants.PREF_ALL_COUNT_TIME, count_time);
                    editor.apply();
                    StisticsInfo info = new Gson().fromJson(jsonObject.toString(), StisticsInfo.class);
                    iFace.response(true, info, count, count_time);
                } else {
                    iFace.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iFace.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        });
    }

    //24:修改密码
    public void changePass(BaseInterface iFace, String pass, String newPass, String confirmPass) {
        String url = Constants.BASEURL_ZZ + "/password/change";
        Map<String, String> map = new HashMap<>();
        map.put("password", pass);
        map.put("password_new", newPass);
        map.put("password_confirm", confirmPass);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("token")));
    }

    //25:忘记密码
    public void forgetPass(BaseInterface iFace, String pass, String token) {
        String url = Constants.BASEURL_ZZ + "/password/reset";
        Map<String, String> map = new HashMap<>();
        map.put("password", pass);
        map.put("token", token);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("token")));
    }

    //26:忘记密码-获取验证码
    public void forgetPassGetCode(BaseInterface iFace, String mobile) {
        String url = Constants.BASEURL_ZZ + "/password/forgot";
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        AppRequestUtil.getInstance().postRequest(context, url, map, (JSONObject jsonObject) -> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    String token = jsonObject.getString("token");
                    String code = jsonObject.getString("code");
                    iFace.response(true, null, code, token);
                } else {
                    iFace.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iFace.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        });
    }

    //27:获取日记录
    public void getDaysRecords(BaseInterface iFace, String year, String month) {
        String url = Constants.BASEURL_ZZ + "/statistics/" + year + "/" + month;
        httpGet(url, iFace, (JSONObject jsonObject)-> {
            List<YearRecord> list = new ArrayList<>();
            JSONArray ja = jsonObject.getJSONArray("results");
            Gson g = new Gson();
            YearRecord record;

            for (int i = 0; i < ja.length(); i++) {
                record = g.fromJson(ja.getJSONObject(i).toString(), YearRecord.class);
                list.add(record);
            }
            YearRecordsMode yrm = new YearRecordsMode();
            yrm.setYearRecordList(list);
            return yrm;
        });
    }

    //28:第三方登录：检查是否已经绑定
    public void checkHasBind(BaseInterface iFace, String uid, String accessToken, String type) {
        String url = Constants.BASEURL_ZZ + "/oauth/check";
        Map<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("accessToken", accessToken);
        map.put("type", type);
        httpPost(url, map, iFace, (JSONObject jsonObject) -> {
            boolean isBind = jsonObject.getBoolean("isBind");
            BaseMode bm = new BaseMode();
            bm.setStatus(isBind);
            if (isBind) {
                String mobile = jsonObject.getString("mobile");
                String token = jsonObject.getString("token");//如果已经绑定，则直接登录，并返回token
                bm.setMessage(mobile);
                bm.setErr(token);
            }
            return bm;
        });
    }

    //29:第三方登录：发送短信验证码
    public void thirdSendCode(BaseInterface iFace, String mobile) {
        String url = Constants.BASEURL_ZZ + "/oauth/sendsms";
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        httpPost(url, map, iFace, (JSONObject jsonObject)-> new StringMode(jsonObject.getString("code")));
    }

    //30:第三方登录：绑定账号
    public void bindAccount(BaseInterface iFace, String uid, String type, String mobile, String password, String isNew, String avatar, String nick, String sex) {
        String url = Constants.BASEURL_ZZ + "/oauth/bind";
        Map<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("type", type);
        map.put("mobile", mobile);
        map.put("password", password);
        map.put("isnew", isNew);
        map.put("avatar", avatar);
        map.put("nick", nick);
        if (sex != null && !sex.equals("")) {
            if (sex.equals("男")) {
                map.put("sex", "M");
            } else {
                map.put("sex", "F");
            }
        }
        AppRequestUtil.getInstance().postRequest(context, url, map, (JSONObject jsonObject) -> {
            try {
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    String token = jsonObject.getString("token");
                    iFace.response(true, null, token, null);
                } else {
                    iFace.response(false, null, null, jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                iFace.response(false, null, null, DEFAULT_ERROR_MSG);
            }
        });
    }

    //31:取消绑定
    public void delBind(BaseInterface iFace, String type) {
        String url = Constants.BASEURL_ZZ + "/user-info/set";
        Map<String, String> map = new HashMap<>();
        if (type.equals("weixin")) {
            map.put("weixin", "delete");
        } else if (type.equals("qq")) {
            map.put("qq", "delete");
        } else {
            map.put("weibo", "delete");
        }
        httpPost(url, map, iFace, (JSONObject jsonObject) -> null);
    }

    //32:获取版本信息
    public void getVersion(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/app/version/android";
        httpGet(url, iFace, (JSONObject jsonObject) -> {
            VsnMode vm = new VsnMode();
            vm.setVer(jsonObject.getString("version"));
            vm.setOldUse(jsonObject.getBoolean("isOldUse"));
            vm.setUpIntro(jsonObject.getString("upintro"));
            vm.setUrl(jsonObject.getString("url"));
            return vm;
        });
    }

    // 33: 微信预支付
    public void preOrder(String pid, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/pay/preorder";
        Map<String, String> requestData = Maps.newHashMap();
        requestData.put("pid", pid);
        httpPost(url, requestData, iFace, (JSONObject jsonObject) -> {
            PreOrderMode data = PreOrderMode.parseResponseObj(jsonObject);
            data.setTid(jsonObject.getString("tid"));
            return data;
        });
    }

    // 34: 支付-产品列表
    public void productList(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/pay/android/list";
        httpGet(url, iFace, (JSONObject jsonObject) -> {
            JSONArray ja = jsonObject.getJSONArray("records");
            List<Product> list = new ArrayList<>();
            Gson g = new Gson();
            Product record;
            for (int i = 0; i < ja.length(); i++) {
                record = g.fromJson(ja.getJSONObject(i).toString(), Product.class);
                list.add(record);
            }
            ProductMode data = new ProductMode();
            data.setList(list);
            return data;
        });
    }

    public void queryOrder(String tid, BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/pay/wx/query_order";
        Map<String, String> requestData = Maps.newHashMap();
        requestData.put("tid", tid);
        httpPost(url, requestData, iFace, (JSONObject jsonObject) -> {
            Gson g = new Gson();
            PayResultMode data = new PayResultMode();
            if (jsonObject.has("data")) {
                data = g.fromJson(jsonObject.get("data").toString(), PayResultMode.class);
            }
            return data;
        });
    }

    public void fetchCfg(BaseInterface iFace) {
        String url = Constants.BASEURL_ZZ + "/app/cfg";
        httpGet(url, iFace, jsonObject -> {
            SharedPreferences.Editor editor = MainApplication._pref.edit();
            editor.putBoolean(Constants.PREF_IS_REVIEW, jsonObject.getBoolean("review"));
            editor.putBoolean(Constants.PREF_SHOW_VIP, jsonObject.getBoolean("showVipTip"));
            editor.putBoolean(Constants.PREF_SHOW_BUY_VIP, jsonObject.getBoolean("showVipBuy"));
            editor.putInt(Constants.PREF_SAVE_AD_COUNT, jsonObject.getInt("saveAdCount"));
            editor.putInt(Constants.PREF_COUNT_DOWN, jsonObject.getInt("countDown"));
            editor.putInt(Constants.PREF_MAIN_BANNER_COUNT, jsonObject.getInt("mainBannerCount"));
            editor.apply();
            return null;
        });
    }
}
