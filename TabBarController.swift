//
//  FirstViewController.swift
//  poodle
//
//  Created by zhl on 2017/2/19.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit
import ESTabBarController_swift

let CAMERA_TAG = 0

class TabBarController: ESTabBarController,UITabBarControllerDelegate {
    let tabFont=UIFont.systemFont(ofSize: 10 * ASPECT_RATIO)
    
    
    convenience init(){
        self.init(nibName: nil, bundle: nil)
        
        let homeNav = UINavigationController(rootViewController: HomeViewController())
        homeNav.tabBarItem = self.createTabBarItem(title: "首页",imgName: "tab_home.png",imgNameSelect: "tab_home_s.png",tag:1)
        let recordNav = UINavigationController(rootViewController: BlankViewController())
        
        let zTabBarItem = ZTabBarItemContentView()
        zTabBarItem.itemContentMode = ESTabBarItemContentMode.alwaysOriginal
        zTabBarItem.titleLabel.font = tabFont
        let recordTabBarItem = ESTabBarItem.init(zTabBarItem, title: "开始", image: UIImage(named: "tab_add"), selectedImage: UIImage(named: "tab_add_s"))
        recordTabBarItem.tag = CAMERA_TAG
        recordNav.tabBarItem = recordTabBarItem
        
        let newsNav = UINavigationController(rootViewController: NewsListViewController())
        newsNav.tabBarItem = self.createTabBarItem(title: "专栏",imgName: "tab_zixun.png",imgNameSelect: "tab_zixun_s.png",tag:2)
        
        let diaryNav = UINavigationController(rootViewController: DiaryViewController())
        diaryNav.tabBarItem = self.createTabBarItem(title: "日记",imgName: "tab_diary.png",imgNameSelect: "tab_diary_s.png",tag:1)

        let memberNav = UINavigationController(rootViewController: MemberCenterController())
        memberNav.tabBarItem = self.createTabBarItem(title: "我",imgName: "tab_more.png",imgNameSelect: "tab_more_s.png",tag:3)
        self.viewControllers = [homeNav, newsNav, recordNav, diaryNav, memberNav];
        self.selectedIndex = 0
        self.delegate = self
        UITabBar.appearance().shadowImage = UIImage()
        UITabBar.appearance().backgroundImage = UIImage()
        
    }

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?)   {
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
//        let barItemSize = CGSize(width: self.tabBar.bounds.size.width / 5, height: self.tabBar.bounds.size.height)
//        self.tabBar.selectionIndicatorImage = ZColorUtil.getImageWithColor(color: ZColorUtil.fromHexString(hexString: "fae196"), size: barItemSize)
        self.tabBar.shadowImage = nil
        
        
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    


    func createTabBarItem(title:String,imgName:String,imgNameSelect:String,tag:Int)-> UITabBarItem {
        var selectTitleDict: Dictionary<NSAttributedStringKey,AnyObject> = [NSAttributedStringKey.foregroundColor: ColorConsts.tabBarColorSelect()]
        selectTitleDict[NSAttributedStringKey.font] = tabFont
        var normaalTitleDict:Dictionary<NSAttributedStringKey,AnyObject>=[NSAttributedStringKey.foregroundColor: ZColorUtil.fromHexString(hexString: "996600")]
        normaalTitleDict[NSAttributedStringKey.font] = tabFont
        
        var infoTabIcon = UIImage(named: imgName)
        infoTabIcon = infoTabIcon!.withRenderingMode(UIImageRenderingMode.alwaysOriginal)
        //这里将title设为空，在界面上不显示标题
        let infoBarItem:UITabBarItem = UITabBarItem(title: title, image: infoTabIcon, tag: tag)
        var infoTabIconSelect = UIImage(named: imgNameSelect)
        infoTabIconSelect = infoTabIconSelect!.withRenderingMode(UIImageRenderingMode.alwaysOriginal)
        infoBarItem.selectedImage = infoTabIconSelect
        infoBarItem.setTitleTextAttributes(normaalTitleDict, for: UIControlState.normal)
        infoBarItem.setTitleTextAttributes(selectTitleDict, for: UIControlState.selected)
        //将UITabBarItem的图片下移7，以调整空标题造成的间隙
//        let offset = 7.f;
//        let imageInset = UIEdgeInsets(top: offset, left: 0, bottom: -offset, right: 0);
//        infoBarItem.imageInsets = imageInset;
        return infoBarItem
    }

    
    //如果选中的是开始站桩按钮，则改变tarbarController的默认行为，将站桩的ViewController直接push进当前的UINavigationController
    func tabBarController(_ tabBarController: UITabBarController, shouldSelect viewController: UIViewController) -> Bool {
        if viewController.tabBarItem.tag == CAMERA_TAG {
            vlog("CAMERA_TAG selected")
            let startViewController =  StartViewController()
            self.present(startViewController, animated: true) {
                vlog("结束")
            }
            return false
//            cameraViewController.hidesBottomBarWhenPushed = true
//            (tabBarController.selectedViewController as! UINavigationController).pushViewController(cameraViewController, animated: true)
//            return false
            
        }
        return true
    }
    
}
