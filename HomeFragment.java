package com.zhanghuang.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zhanghuang.MainApplication;
import com.zhanghuang.R;
import com.zhanghuang.ZxInfoActivity;
import com.zhanghuang.adapter.MainFragListAdapter;
import com.zhanghuang.base.BaseMainFragment;
import com.zhanghuang.modes.Article;
import com.zhanghuang.bean.ArticleBean;
import com.zhanghuang.db.DaoManager;
import com.zhanghuang.modes.ArticlesMode;
import com.zhanghuang.modes.BaseMode;
import com.zhanghuang.modes.StisticsInfo;
import com.zhanghuang.modes.User;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.netinterface.BaseInterface;
import com.zhanghuang.util.AndroidUtil;
import com.zhanghuang.util.AppUpdate;
import com.zhanghuang.util.Constants;
import com.zhanghuang.view.MainHeadUint;
import com.zhanghuang.ItemSpace;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import lombok.NonNull;

/**
 * Created by yuanlei on 2017/3/14.
 */

public class HomeFragment extends BaseMainFragment implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {

    @BindView(R.id.main_view_refreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.main_view_recycleView)
    RecyclerView recyclerView;

    private RequestData rd;
    private int start = 0;

    private MainFragListAdapter adapter;
    private List<Article> articleList;
    private LayoutInflater inflater;

    private MainHeadUint mainHeadUint;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init(View view, LayoutInflater inflater, Bundle savedInstanceState) {
        this.inflater = inflater;
        initData();
        initView();
        initAdapter();
    }

    @Override
    public int getLayoutId() {
        return R.layout.main_view;
    }

    @Override
    public boolean isHasTitleBar() {
        return false;
    }

    @Override
    public boolean isHasStatuBarSpace() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        //检查升级，检查标志位
        if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            boolean hasCheckedUpdate = MainApplication._pref.getBoolean(Constants.PREF_HAS_CHECKED_UPDATE, false);
            if (!hasCheckedUpdate) {
                checkUpdate();
                SharedPreferences.Editor editor = MainApplication._pref.edit();
                editor.putBoolean(Constants.PREF_HAS_CHECKED_UPDATE, true);
                editor.apply();
            }
        }
        
        if (!MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            if (mainHeadUint != null) {
                mainHeadUint.setUser(null);
                mainHeadUint.setRecord(new StisticsInfo());
            }
        } else {
            if (mainHeadUint != null) {
                SharedPreferences preferences = MainApplication._pref;
//                mhu.setUserTwo(preferences.getString(Constants.PREF_USER_AVATAR,""),preferences.getString(Constants.PREF_USER_SHOWID,""),
//                        preferences.getString(Constants.PREF_USER_LEVEL,""),preferences.getString(Constants.PREF_USER_NICK,""));
                mainHeadUint.setRecord(preferences.getString(Constants.PREF_ALL_COUNT, "0"), preferences.getString(Constants.PREF_ALL_COUNT_TIME, "0"));
            }
        }
        start = 0;
        articleList.clear();

        if (AndroidUtil.checkNet(getContext())) {
            getArticles();
        } else {
            getActivitiesFromDb();
        }
    }

    private void checkUpdate() {
        int per = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (PackageManager.PERMISSION_GRANTED != per) {
            //申请读sd卡权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1000);
        } else {
            AppUpdate.getInstance().checkAppVer(getActivity(), results);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            checkUpdate();
        }
    }

    @Override
    public String getPageName() {
        return "推荐";
    }

    private void initData() {
        rd = new RequestData(getContext());
    }

    private void initView() {
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        articleList = new ArrayList<>();
        mainHeadUint = (MainHeadUint) View.inflate(getContext(), R.layout.main_head_unit, null);
//        mainHeadUint.setAd(MainApplication._pref.getString(Constants.PREF_ADURL, ""));
    }

    private void initAdapter() {
        adapter = new MainFragListAdapter(getContext(), null);
        adapter.openLoadAnimation();
        adapter.setOnLoadMoreListener(this);
        adapter.openLoadMore(true);
        adapter.setOnRecyclerViewItemClickListener((view, position) -> {
            Article article = articleList.get(position);
            Intent in = new Intent(getContext(), ZxInfoActivity.class);
            in.putExtra(Constants.ZXID, article.getId());
            in.putExtra(Constants.ZXTITLE, article.getTitle());
            in.putExtra(Constants.ZXSRC, article.getSrc());
            in.putExtra(Constants.ZXIMG, article.getImg());
            in.putExtra(Constants.ZXDESC, article.getDesc());
            startActivity(in);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemSpace(10f, 8f, 10f, false));
        adapter.addHeaderView(mainHeadUint);
    }

    public void getArticles() {
        rd.getClassicArticles(start, Constants.DEFAULT_PAGE_SIZE, getClassicArticlesIf);
    }

    private void getActivitiesFromDb() {
        List<ArticleBean> articleBeanList = DaoManager.getInstance().getDaoSession().getArticleBeanDao().queryBuilder().build().list();
        if (articleBeanList != null && articleBeanList.size() > 0) {
            Article article;
            for (ArticleBean ab : articleBeanList) {
                article = new Article();
                article.setId(ab.getId());
                article.setTitle(ab.getTitle());
                article.setStamp(ab.getStamp());
                article.setSrc(ab.getSrc());
                article.setNick(ab.getNick());
                article.setAvatar(ab.getAvatar());
                article.setDesc(ab.getDesc());
                article.setImg(ab.getImg());
                article.setIsad(ab.getIsad());
                articleList.add(article);
            }
            start = articleBeanList.size();
            adapter.setNewData(articleList);
            swipeRefreshLayout.setRefreshing(false);
            if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                rd.getUserInfo(getUserInfoIf);
            }
        } else {
            getArticles();
        }
    }

    @Override
    public void onRefresh() {
        start = 0;
        articleList.clear();
        if (!AndroidUtil.checkNet(getContext())) {
            getActivitiesFromDb();
        } else {
            getArticles();
        }

    }

    @Override
    public void onLoadMoreRequested() {
        getArticles();
    }

    public BaseInterface getUserInfoIf = new BaseInterface() {
        @Override
        public void response(boolean success, BaseMode result, String message, String err) {
            if (success) {
                User u = (User) result;
                mainHeadUint.setUser(u);
            } else {
                Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
            }
            rd.getAllRecords(getAllRecordsIf);
        }
    };

    public BaseInterface getClassicArticlesIf = new BaseInterface() {
        @Override
        public void response(boolean success, BaseMode result, String message, String err) {
            MainApplication._pref.edit().putLong(Constants.PREF_MAIN_REQUEST_TIME, SystemClock.elapsedRealtime()).apply();
            if (success) {
                if (!(result instanceof ArticlesMode)) {  //TODO  此处为临时解决办法，需网络请求大改后再处理
                    return;
                }
                ArticlesMode am = (ArticlesMode) result;
                List<Article> inList = am.getArticleList();
                int count = am.getCount();
                articleList.addAll(inList);
                if (start == 0) {
                    adapter.setNewData(inList);
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    adapter.notifyDataChangedAfterLoadMore(inList, true);
                }
                start = start + inList.size();
                if (start == count) {
                    adapter.notifyDataChangedAfterLoadMore(false);
                    View view = inflater.inflate(R.layout.not_loading, (ViewGroup) recyclerView.getParent(), false);
                    adapter.addFooterView(view);
                } else {
                    adapter.openLoadMore(true);
                }
            } else {
                Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
            }
            if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                rd.getUserInfo(getUserInfoIf);
            }
        }
    };


    public BaseInterface getAllRecordsIf = new BaseInterface() {
        @Override
        public void response(boolean success, BaseMode result, String message, String err) {
            if (success) {
                mainHeadUint.setRecord((StisticsInfo) result);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = MainApplication._pref.edit();
        editor.putBoolean(Constants.PREF_HAS_CHECKED_UPDATE, false);
        editor.apply();
    }
}
