//
//  HomeViewController.swift
//  saluki
//
//  Created by zhl on 2017/3/16.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit
import GRDB
import SwifterSwift

class HomeViewController: BaseViewController {
    lazy var tableView = UITableView().then {
        $0.showsHorizontalScrollIndicator = false
        $0.showsVerticalScrollIndicator = false
        $0.separatorStyle = UITableViewCellSeparatorStyle.none
        $0.backgroundColor = UIColor.clear
    }
    var requestCount:Int         = 0
    var isLoad                   = false
    var currentCount: Int        = 0
    var maxCount: Int            = 0
    var firstLaunch:Bool         = true
    var dataArray: [ListContent] = []
    var actCount                 = ActCount()
    var user:User?
    var adobj: ImageData?
    var launchObj: ImageData?
    
    private var splashAd: GDTSplashAd!
    
    lazy var launchView = CustomLaunchView().then{_ in
    }
    lazy var adView = CustomAdView().then{_ in
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        self.loadSplashAD()
        tableView.delegate = self
        tableView.dataSource = self
        self.view.addSubview(tableView)
        tableView.snp.makeConstraints { (make) -> Void in
            make.edges.equalTo(self.view).inset(UIEdgeInsetsMake(0,0,0,0))
        }
        tableView.register(PicTextViewCell.classForCoder(), forCellReuseIdentifier: "PicTextViewCell")
        tableView.register(UserInfoCell.classForCoder(), forCellReuseIdentifier: "UserInfoCell")
        tableView.register(MainAdCell.classForCoder(), forCellReuseIdentifier: "MainAdCell")
        tableView.register(ActivityStatisticsCell.classForCoder(), forCellReuseIdentifier: "ActivityStatisticsCell")
        let refreshControl:UIRefreshControl = UIRefreshControl()
        refreshControl.addTarget(self, action: #selector(refreshData), for: UIControlEvents.valueChanged)
        
        let attributes = [kCTForegroundColorAttributeName : REFRESH_COLOR]
        refreshControl.attributedTitle = NSAttributedString(string: "正在拼命加载", attributes: attributes as [NSAttributedStringKey : Any])
        refreshControl.tintColor = REFRESH_COLOR
        
        refreshControl.tag = TABLE_REFRESH_TAG
        tableView.addSubview(refreshControl)
        user = User()
        self.navigationItem.title = self.user?.nickname
        self.getAppCfg()
//        getMainAd()
        getLaunch()
        self.navigationController?.isNavigationBarHidden = true
    }
    
    override func viewWillAppear(_ animated: Bool) {
        if GlobalCfg.shared.review && !ZManager.instance.isLogin{
            self.forceLogin()
        }
        //隐藏顶部标题栏
        getContentList(CacheManager.🐄.isExpired("main"), pageNum: 0)
        getActCount()
        self.navigationController?.isNavigationBarHidden = true
        if self.isLogin {
            user?.nickname = ZManager.instance.showName
            user?.avatar = ZManager.instance.avatar
            user?.id = ZManager.instance.userId
            if NetStatusManager.instance.status != .unreachable {
                getUserInfo()
            }
        } else {
            user?.nickname = "未注册用户"
            self.navigationItem.title = self.user?.nickname
            self.tableView.reloadData()
        }
    }
    override func requestError(errStr: String) {
        
    }
    override func networkError(_ url: String) {
        
    }
    
    override func finishGetMainList(_ pageNum: Int, count: Int, dataList: [ListContent]) {
        self.finishRefresh()
        if (pageNum == 0){
            self.dataArray.removeAll(keepingCapacity: false)
            self.currentCount = dataList.count
            self.dataArray += dataList
        } else {
            if self.dataArray.count >= pageNum * Z_PAGE_SIZE {
                let slice = self.dataArray[0...pageNum*Z_PAGE_SIZE-1]
                self.dataArray = slice + dataList
                self.currentCount = pageNum * Z_PAGE_SIZE + dataList.count
            } else {
                self.dataArray += dataList
                self.currentCount += dataList.count
            }
        }
        self.maxCount = count
        //        self.tableView.reloadSections([3], with: UITableViewRowAnimation.none)
        self.tableView.reloadData()
    }
    
    override func finishGetActCount(_ data: ActCount) {
        self.actCount = data
        self.tableView.reloadSections([1], with: UITableViewRowAnimation.none)
    }
}

//数据
extension HomeViewController {
    func getContentList(_ refresh: Bool = false, pageNum: Int = 0 ){
        requestCount += 1
        vlog("load content list,pageNum is:\(pageNum), refresh:\(refresh)")
        webApi.getMainList(refresh, pageNum: pageNum){
            self.requestCount -= 1
            self.maxCount = self.maxCount > $0 ? self.maxCount : $0
            self.finishRefresh()
            if (refresh||pageNum==0){
                self.dataArray.removeAll(keepingCapacity: false)
                self.currentCount = $1.count
            } else {
                self.currentCount += $1.count
            }
            self.dataArray += $1
            //            self.tableView.reloadSections([3], with: UITableViewRowAnimation.none)
            self.tableView.reloadData()
            self.isLoad=false
        }
    }
    func getUserInfo() {
        requestCount += 1
        webApi.getUserInfo{
            self.requestCount -= 1
            self.user = $0
            self.navigationItem.title = self.user?.nickname
            self.tableView.reloadData()
            //            self.tableView.reloadSections([0], with: UITableViewRowAnimation.none)
        }
    }
    func getActCount() {
        requestCount += 1
        webApi.getActCount(true){
            self.requestCount -= 1
            self.actCount = $0
            self.tableView.reloadData()
            //            self.tableView.reloadSections([2], with: UITableViewRowAnimation.none)
        }
    }
    @objc func refreshData(){
        isLoad=true
        self.getContentList(true, pageNum: 0)
        
    }
    func loadMore(_ beginPage:Int) {
        vlog("一共:\(maxCount),当前:\(currentCount)")
        if(currentCount<maxCount){
            isLoad=true
            self.getContentList(false, pageNum: beginPage)
        }
    }
    func getMainAd() {
        webApi.getMainAd(true) {
            self.adobj = $0
            self.tableView.reloadData()
        }
    }
    func getLaunch() {
        webApi.getLaunch{
            if $0.count > 0 {
                self.launchObj = $0.first!
                self.adView.loadImg(url: ($0.first?.src)!)
            }
        }
    }
    func getAppCfg() {
        webApi.fetchCfg{
            if GlobalCfg.shared.review && !ZManager.instance.isLogin{
                self.forceLogin()
            }
        }
    }
    
    
}
//界面
extension HomeViewController {
    func showLoginAlert(){
        let alert = UIAlertController(title: "提示" , message: "该操作需要登录，是否立即登录？",  preferredStyle: .alert)
        let action = UIAlertAction(title: "取消", style: .cancel,  handler: nil)
        let okaction = UIAlertAction(title: "登录", style: .default) { _ in
            self.toLogin()
        }
        alert.addAction(action)
        alert.addAction(okaction)
        present(alert, animated: true, completion: nil)
    }
    func finishRefresh(){
        if let view = tableView.viewWithTag(TABLE_REFRESH_TAG){
            (view as! UIRefreshControl).endRefreshing()
        }
    }
    func toActivityList() {
        let activityListController = ActivityListController()
        activityListController.hidesBottomBarWhenPushed = true
        self.navigationController?.pushViewController(activityListController, animated: true)
    }
    func toEditInfo() {
        if self.isLogin {
            let memberInfoController = EditUserInfoController()
            memberInfoController.hidesBottomBarWhenPushed = true
            memberInfoController.user = self.user!
            self.navigationController?.pushViewController(memberInfoController, animated: true)
        } else {
            showLoginAlert()
        }
        
    }
    /**
     显示LaunchScreen
     */
    func showLaunchView(){
        launchView.delegate=self
        self.view.addSubview(launchView)
        let height:CGFloat = LONG_MODE ? 0 : -49
        launchView.snp.makeConstraints { (make) -> Void in
            make.edges.equalTo(self.view).inset(UIEdgeInsetsMake(0,0,height,0))
        }
        self.view.bringSubview(toFront: launchView)
        self.hideTabBar(true)
        Timer.after(3.seconds){
            if self.firstLaunch {
                self.hideLaunchView()
            }
        }
    }
    /**
     显示自定义的adview
     */
    func showCustomAdView() {
        if (!firstLaunch || ZManager.instance.isVip() || GlobalCfg.shared.review || !GlobalCfg.shared.showVipTip){
            return
        }
//        self.navigationController?.isNavigationBarHidden = true
        adView.delegate = self
        self.hideTabBar(true)
        self.view.addSubview(adView)
        adView.beginCountDown()
        let height:CGFloat = LONG_MODE ? 0 : -49
        adView.snp.makeConstraints { (make) -> Void in
            make.edges.equalTo(self.view).inset(UIEdgeInsetsMake(0,0,height,0))
        }
        self.view.bringSubview(toFront: adView)
        
        let sec = GlobalCfg.shared.countDown
        Timer.after(TimeInterval(sec)){
            if self.firstLaunch {
                self.hideLaunchView()
            }
        }
    }
    
    
    /**
     隐藏LaunchScreen
     */
    func hideLaunchView() {
        self.launchView.isHidden = true
        self.adView.isHidden = true
        self.hideTabBar(false)
        self.firstLaunch = false
//        self.navigationController?.isNavigationBarHidden = false
    }
}

extension HomeViewController: UITableViewDelegate, UITableViewDataSource {
    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        return 4
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 3 {
            return dataArray.count
        } else {
            return 1
        }
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        switch section {
        case 2:
            if self.adobj != nil && self.adobj?.src != nil && !(self.adobj?.hidden)!{
                return 5
            } else {
                return 0
            }
            
        default:
            return 5
        }
    }
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = UIView()
        view.backgroundColor = UIColor.clear
        return view
    }
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell:UITableViewCell?
        let idx = indexPath.row
        switch indexPath.section {
        case 0:
            cell = tableView.dequeueReusableCell(withIdentifier: "UserInfoCell", for: indexPath)
            (cell as! UserInfoCell).setData(self.user!)
            
        case 1:
            cell = tableView.dequeueReusableCell(withIdentifier: "ActivityStatisticsCell", for: indexPath)
            (cell as! ActivityStatisticsCell).setData(self.actCount)
        case 2:
            cell = tableView.dequeueReusableCell(withIdentifier: "MainAdCell", for: indexPath)
            (cell as! MainAdCell).delegate = self
            (cell as! MainAdCell).setData("", vc: self)
            if ZManager.instance.isVip() || ZManager.instance.mainBannerCount >=  GlobalCfg.shared.mainBannerCount {
                (cell as! MainAdCell).isHidden = true
            } else {
                (cell as! MainAdCell).isHidden = false
            }
//            if self.adobj != nil {
//                (cell as! MainAdCell).setData((self.adobj?.src!)!, vc: self)
//            }
//            if self.adobj != nil && self.adobj?.src != nil && !(self.adobj?.hidden)!{
//                (cell as! MainAdCell).isHidden = false
//            } else {
//                (cell as! MainAdCell).isHidden = true
//            }
        default:
            cell = tableView.dequeueReusableCell(withIdentifier: "PicTextViewCell", for: indexPath)
            (cell as! PicTextViewCell).setData(dataArray[idx], index: idx)
        }
        return cell!
    }
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        switch indexPath.section {
        case 0:
            return 240 * ASPECT_RATIO
        case 1:
            return 66 * ASPECT_RATIO
        case 2:
            if ZManager.instance.isVip() || ZManager.instance.mainBannerCount >= GlobalCfg.shared.mainBannerCount {
                return 0
            } else {
                return (SCREEN_WIDTH - 20) / 6.4 + 10
            }
            
//            if self.adobj != nil && self.adobj?.src != nil && !(self.adobj?.hidden)!{
//                return 55 * ASPECT_RATIO + 5
//            } else {
//                return 0
//            }
        default:
            var height = 133 * ASPECT_RATIO
            height = height < 133 ? 133 : height
            return height
        }
        
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.section == 3 {
            let data = dataArray[indexPath.row]
            self.showNews(data)
        } else if indexPath.section == 1 {
            self.toActivityList()
        } else if indexPath.section == 0 {
            self.toEditInfo()
        } else if indexPath.section == 2 {
            self.toWebPage((self.adobj?.link)!, title: "")
        }
    }
    
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let currentHeight:CGFloat = scrollView.contentOffset.y+scrollView.frame.size.height+INCREASE_LOAD_SCROLL_DISTANCE
        if ((scrollView.contentOffset.y>0)&&(currentHeight > scrollView.contentSize.height)&&(!isLoad)) {
            isLoad = true
            let beginPage:Int = currentCount/Z_PAGE_SIZE
            self.loadMore(beginPage)
        }
        
    }
}
extension HomeViewController: AdCellDeleagte {
    func didCloseBtnClick() {
//        self.adobj?.hidden = true
        ZManager.instance.mainBannerCount += 1
        ZManager.instance.saveDate()
        self.tableView.reloadData()
//        do {
//            try ImageDao().insert(self.adobj!)
//        } catch {
//            vlog(error)
//        }
    }
}

extension HomeViewController: LaunchViewDelegate {
    func didLaunchImageClick() {
        if self.launchObj != nil {
            if self.launchObj!.link != nil {
                self.toWebPage(self.launchObj!.link!)
            }
        }
    }
    func didLaunchCloseBtnClick() {
        hideLaunchView()
    }
}

extension HomeViewController: GDTSplashZoomOutViewDelegate, GDTSplashAdDelegate{
    func loadSplashAD() {
        if ZManager.instance.isVip() {
            return
        }
        self.splashAd = GDTSplashAd.init(placementId: SPLASH_AD_ID)
        self.splashAd.delegate = self
        self.splashAd.needZoomOut = true
        self.splashAd.fetchDelay = 5
        let splashImage = UIImage.init(named: "launch-part-up.png")
        self.splashAd.backgroundImage = splashImage
        self.splashAd.load()
        
    }
    func showSplashAD() {
        if ZManager.instance.isVip() {
            return
        }
        let width = UIScreen.main.bounds.size.width
        let height = (width / 640 ) * 138
        let bottomView = UIView.init(frame: CGRect(origin: CGPoint.zero, size: CGSize(width: width, height: height)))
        bottomView.backgroundColor = .white

        let logo = UIImageView.init(image: UIImage.init(named: "launch-part-down.png"))
        logo.accessibilityIdentifier = "splash_ad"
        logo.frame = CGRect(origin: CGPoint.zero, size: CGSize(width: width, height: height))
        logo.center = bottomView.center
        bottomView.addSubview(logo)
        let window = UIApplication.shared.keyWindow
        self.splashAd.show(in: window, withBottomView: bottomView, skip: nil)
    }
    
    //    MARK:GDTSplashAdDelegate
    func splashAdSuccessPresentScreen(_ splashAd: GDTSplashAd!) {
        print(#function)
    }
    
    func splashAdDidLoad(_ splashAd: GDTSplashAd!) {
        print(#function)
        if ((splashAd.splashZoomOutView) != nil) {
            print(">>>>>>>>>>>>>>>> v+")
            self.view.addSubview(splashAd.splashZoomOutView)
            splashAd.splashZoomOutView.rootViewController = self
//                [splashAd.splashZoomOutView supportDrag]; // 如果想让开屏V+支持拖拽，需要引入优量汇开源类头文件"GDTSplashZoomOutView+GDTDraggable.h"，并进行设置
        }
        self.showSplashAD()
        
    }

    func splashAdFail(toPresent splashAd: GDTSplashAd!, withError error: Error!) {
        print(#function,error)
    }

    func splashAdExposured(_ splashAd: GDTSplashAd!) {
        print(#function)
    }

    func splashAdClicked(_ splashAd: GDTSplashAd!) {
        print(#function)
    }

    func splashAdApplicationWillEnterBackground(_ splashAd: GDTSplashAd!) {
        print(#function)
    }

    func splashAdWillClosed(_ splashAd: GDTSplashAd!) {
        print(#function)
        self.splashAd = nil
        self.showCustomAdView()
    }

    func splashAdClosed(_ splashAd: GDTSplashAd!) {
        print(#function)
    }

    func splashAdDidPresentFullScreenModal(_ splashAd: GDTSplashAd!) {
        print(#function)
    }

    func splashAdWillDismissFullScreenModal(_ splashAd: GDTSplashAd!) {
        print(#function)
    }

    func splashAdDidDismissFullScreenModal(_ splashAd: GDTSplashAd!) {
        print(#function)
    }
}
extension HomeViewController: AdViewDelegate{
    func didMainImageClick() {
        self.showProductList()
    }
    func didAdCloseBtnClick() {
        hideLaunchView()
    }
    
}

extension HomeViewController: ProductListDelegate{
    func showProductList() {
        let productListController =  ProductListController()
        productListController.delegate = self
        self.present(productListController, animated: true) {
        }
    }
    func onClose() {
        self.getUserInfo()
    }
}
