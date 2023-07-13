//
//  AppDelegate.swift
//  saluki
//
//  Created by zhl on 2017/3/15.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit
import AVFoundation
import StoreKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, DYFStoreAppStorePaymentDelegate {

    var window: UIWindow?
    var backgroundTask:UIBackgroundTaskIdentifier! = nil


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        /**begin of generate deviceID**/
        var deviceIdExist = false
        let deviceId = ZManager.instance.deviceId
        
        deviceIdExist = !(deviceId.isEmpty)
        
        if(!deviceIdExist){
            ZManager.instance.deviceId = UIDevice.current.identifierForVendor!.uuidString.lowercased()
            ZManager.instance.saveDate()
        }
        /**end of generate deviceID **/
        /**begin of 检查数据库**/

        DispatchQueue.global().async {
            do {
                try Dao().checkDbExist()
                try ImageDao().createTable()
                try ActivityDao().createTable()
                try ListContentDao().createTable()
                try CategoryDao().createTable()
                try CacheDao().createTable()
                try ActivityPlanDao().createTable()
                DispatchQueue.main.async {
                    vlog("成功初始化数据库")
                }
            } catch {
                DispatchQueue.main.async {
                    vlog(error)
                    vlog("初始化数据库失败")
                }
            }
        }
        
        CacheManager.🐄.initData()
        
        //设置title bar的背景
        UINavigationBar.appearance().barTintColor = ColorConsts.topNavBackColor();
        UINavigationBar.appearance().tintColor = ColorConsts.topNavFontColor();
        //设置title bar文字颜色
        let dict:Dictionary<NSAttributedStringKey,AnyObject> = [NSAttributedStringKey.foregroundColor: ColorConsts.topNavFontColor()]
//        UINavigationBar.appearance().titleTextAttributes = dict
//        UINavigationBar.appearance().barStyle = UIBarStyle.black
        
        if #available(iOS 13.0, *) {
            let barApp:UINavigationBarAppearance = UINavigationBarAppearance.init();
            
            barApp.configureWithOpaqueBackground();
            barApp.titleTextAttributes = dict;
            barApp.backgroundColor = ColorConsts.topNavBackColor();
            
            
            UINavigationBar.appearance().standardAppearance = barApp;
            UINavigationBar.appearance().scrollEdgeAppearance = barApp;
            
        } else {
            // Fallback on earlier versions
            UINavigationBar.appearance().titleTextAttributes = dict;

            
        }
        if #available(iOS 15.0, *) {
            let appearance = UITabBarAppearance()
            appearance.configureWithOpaqueBackground()
            appearance.backgroundColor = ZColorUtil.fromHexString(hexString: "f7c04b")
            

            appearance.stackedLayoutAppearance.normal.titleTextAttributes = [.foregroundColor : ZColorUtil.fromHexString(hexString: "996600")]
            appearance.stackedLayoutAppearance.selected.titleTextAttributes = [.foregroundColor : ColorConsts.tabBarColorSelect()]
            
            UITabBar.appearance().standardAppearance = appearance
            UITabBar.appearance().scrollEdgeAppearance = UITabBar.appearance().standardAppearance;
        } else {
            UITabBar.appearance().barTintColor = ZColorUtil.fromHexString(hexString: "f7c04b")
            UITabBar.appearance().isTranslucent = false
        }
        
//        UITabBarItem.appearance().setTitleTextAttributes([NSForegroundColorAttributeName : UIColor.black], for: .normal)
        ZStoreManager.shared.beginSth()
        // begin of 优量汇
        GDTSDKConfig.registerAppId(AD_APPID)
        // end of 优量汇
        
        self.window!.rootViewController = TabBarController()
        self.window!.makeKeyAndVisible()
        self.window?.backgroundColor = BACK_COLOR
        //MARK: 友盟统计
        //在发布到AppStore的时候，如果没有特殊的情况，一定要设置为false
        MobClick.setLogEnabled(false)
        let obj = UMAnalyticsConfig.init()
        obj.appKey = UMENG_APPKEY
        MobClick.start(withConfigure: obj);
        let version =  Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! String
        MobClick.setAppVersion(version)
        //end of 友盟统计
        
        UMSocialManager.default().openLog(true)
        UMSocialManager.default().umSocialAppkey = UMENG_APPKEY

        UMSocialManager.default().setPlaform(UMSocialPlatformType.wechatSession, appKey: WX_APIKEY, appSecret: WX_APPSECRET, redirectURL: "http://mobile.umeng.com/social")

        UMSocialManager.default().setPlaform(UMSocialPlatformType.QQ, appKey: QQ_APPID, appSecret: nil, redirectURL: "http://mobile.umeng.com/social")

        UMSocialManager.default().setPlaform(UMSocialPlatformType.sina, appKey: SINA_APPKEY, appSecret: SINA_APPSECRET, redirectURL: "https://sns.whalecloud.com/sina2/callback")

        UMSocialGlobal.shareInstance().isUsingHttpsWhenShareContent = false
        
        // begin of 支付
        // Wether to allow the logs output to console.
        DYFStore.default.enableLog = true
        // If more than one transaction observer is attached to the payment queue, no guarantees are made as to the order they will be called in. It is recommended that you use a single observer to process and finish the transaction.
        DYFStore.default.addPaymentTransactionObserver()

            // Sets the delegate processes the purchase which was initiated by user from the App Store.
        DYFStore.default.delegate = self
        // end of 支付
        //添加网络检测代码
        do {
            Network.reachability = try Reachability(hostname: "www.baidu.com")
            do {
                try Network.reachability?.start()
            } catch let error as Network.Error {
                vlog(error)
            } catch {
                vlog(error)
            }
        } catch {
            vlog(error)
        }
        //添加监听
        NotificationCenter.default.addObserver(self, selector: #selector(netStatusManager), name: .flagsChanged, object: Network.reachability)
        
//        do {
//            try AVAudioSession.sharedInstance().setActive(true)
//        } catch {
//            vlog(error)
//        }
        
//        WXApi.registerApp("YOUR_APP_ID", universalLink: "YOUR_UNIVERSAL_LINKS")
        return true
    }
    //网络状态更改
    @objc func netStatusManager(_ notification: NSNotification) {
        guard let status = Network.reachability?.status else {
            NetStatusManager.instance.status = .unreachable
            return
        }
        vlog("==============================================")
        vlog("net work change, status is:\(status)")
        NetStatusManager.instance.status = status
        if status != .unreachable {
            let webapi = WebApi.shared
            webapi.uploadLocalActivities()
        }
    }
    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        NotificationCenter.default.post(name: NSNotification.Name.UIApplicationDidEnterBackground, object: nil)
        
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
        NotificationCenter.default.post(name: NSNotification.Name.UIApplicationWillEnterForeground, object: nil)
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
    
    //MARK: 分享微信回调
    func application(_ application: UIApplication, handleOpen url: URL) -> Bool {
        return UMSocialManager.default().handleOpen(url)
    }
    func application(_ application: UIApplication, open url: URL, sourceApplication: String?, annotation: Any) -> Bool {
        //微信支付
//        if(sourceApplication!.contains("com.tencent.xin")){
        return UMSocialManager.default().handleOpen(url)
    }
    
   
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplicationOpenURLOptionsKey : Any] = [:]) -> Bool {
//        let urlKey: String = options[UIApplicationOpenURLOptionsKey.sourceApplication] as! String
//        if urlKey == "com.tencent.xin" {
        return UMSocialManager.default().handleOpen(url, options: options)
    
    }
    
    override var canBecomeFirstResponder: Bool{
        return true
    }
//    override func remoteControlReceived(with event: UIEvent?) {
//        if event?.type == .remoteControl {
//            print(event?.subtype)
//        }
//    }
    
    // 处理用户从应用商店发起的购买
    func didReceiveAppStorePurchaseRequest(_ queue: SKPaymentQueue, payment: SKPayment, forProduct product: SKProduct) {
            
        if !DYFStore.canMakePayments() {
            self.showTipsMessage("Your device is not able or allowed to make payments!")
            return
        }
        if !ZManager.instance.isLogin{
            return
        }
                
        // This algorithm is negotiated with server developer.
        let userIdentifier = Z_SHA256_HashValue(ZManager.instance.userId) ?? ""
//        let userIdentifier = ZManager.instance.userId
        DYFStoreLog("appdelegate userIdentifier: \(userIdentifier)")
        if payment.applicationUsername != userIdentifier {
            return
        }
        ZStoreManager.shared.addPayment(product.productIdentifier, userIdentifier: userIdentifier)
    }
}

