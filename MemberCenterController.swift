//
//  MemberCenterController.swift
//  saluki
//
//  Created by zhl on 2017/5/31.
//  Copyright © 2017年 zhl. All rights reserved.
//  新版的个人中心
//  20170531
//

import UIKit
import SDWebImage

class MemberCenterController: BaseViewController {
    lazy var tableView = UITableView().then {
        $0.showsHorizontalScrollIndicator = false
        $0.showsVerticalScrollIndicator = false
        $0.separatorStyle = UITableViewCellSeparatorStyle.none
        $0.backgroundColor = BACK_COLOR
    }
    
    var cacheSize: Float = 0
    var user:User = User()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = "个人中心"
        tableView.delegate = self
        tableView.dataSource = self
        self.view.addSubview(tableView)
        tableView.snp.makeConstraints { (make) -> Void in
            make.edges.equalTo(self.view).inset(UIEdgeInsetsMake(0,0,0,0))
        }
        tableView.register(MemberTopCell.classForCoder(), forCellReuseIdentifier: "MemberTopCell")
        tableView.register(VipInfoCell.classForCoder(), forCellReuseIdentifier: "VipInfoCell")
        tableView.register(MemberIconCell.classForCoder(), forCellReuseIdentifier: "MemberIconCell")
        tableView.register(ButtonCell.classForCoder(), forCellReuseIdentifier: "ButtonCell")
    }
    
    override func viewWillAppear(_ animated: Bool) {
        initData()
        if self.isLogin {
            user.nickname = ZManager.instance.showName
            user.avatar = ZManager.instance.avatar
            user.levelName = ZManager.instance.levelName
        } else {
            user.nickname = "未注册用户"

        }
        self.tableView.reloadData()
        cacheSizeAtPath()
    }

}

extension MemberCenterController {
    func initData() {
        
        
        if ZManager.instance.isLogin {
            
        }
        self.tableView.reloadData()
        
    }
}
extension MemberCenterController {
    func showLoginAlert() {
        let alert = UIAlertController(title: "提示" , message: "该操作需要登录，是否立即登录？",  preferredStyle: .alert)
        let action = UIAlertAction(title: "取消", style: .cancel,  handler: nil)
        let okaction = UIAlertAction(title: "登录", style: .default) { _ in
            self.toLogin()
        }
        alert.addAction(action)
        alert.addAction(okaction)
        present(alert, animated: true, completion: nil)
    }
    func toEditInfo() {
        if self.isLogin {
            let memberInfoController = EditUserInfoController()
            memberInfoController.hidesBottomBarWhenPushed = true
            memberInfoController.user = user
            self.navigationController?.pushViewController(memberInfoController, animated: true)
        } else {
            self.showLoginAlert()
        }
    }
    func toBuyVip() {
        if self.isLogin {
            self.showProductList()
        } else {
            self.showLoginAlert()
        }
    }
    func toViewLevel() {
        let url = levelinfo_url
        self.toWebPage(url, title: "我的等级")
    }
    func toMyCollect() {
        if self.isLogin {
            let myCollectController = MyCollectController()
            myCollectController.hidesBottomBarWhenPushed = true
            self.navigationController?.pushViewController(myCollectController, animated: true)
        } else {
            self.showLoginAlert()
        }
        
    }
    func toAccountBinding() {
        
        if self.isLogin {
            let bindAccListController = BindAccListController()
            bindAccListController.hidesBottomBarWhenPushed = true
            bindAccListController.user = self.user
            self.navigationController?.pushViewController(bindAccListController, animated: true)
        } else {
            self.showLoginAlert()
        }
    }
    func toClearCache() {
        let alertController = UIAlertController(title: "确定注销账号吗？",
                message: "按照有关法律条文规定，您的账号资料将被保存六个月，六个月内登录账号即可恢复登录；六个月后，您的资料将被永久删除！",
                preferredStyle: UIAlertControllerStyle.alert)
        let cancelAction = UIAlertAction(title: "取消", style: UIAlertActionStyle.cancel, handler: nil)
        alertController.addAction(cancelAction)
        let okAction = UIAlertAction(title: "确认", style: UIAlertActionStyle.default) {
            (action: UIAlertAction) -> Void in
            ZManager.instance.isLogin=false
            ZManager.instance.userId=""
            ZManager.instance.userToken=""
            ZManager.instance.saveDate()
            NetworkManager.instance.setDefaultManage()
            self.initData()
            self.toLogin() // 跳转到登录页面
        }
        alertController.addAction(okAction)
        self.present(alertController, animated: true, completion: nil)
        
    }
    func toAppRecommend() {
        let url = viphelp_url
        self.toWebPage(url, title: "联系客服")
    }
    //登出
    func toLoginOut(){
        if (ZManager.instance.isLogin){
            let alertController = UIAlertController(title: "确定登出?", message: "", preferredStyle: UIAlertControllerStyle.alert)
            let cancelAction = UIAlertAction(title: "取消", style: UIAlertActionStyle.cancel, handler: nil)
            alertController.addAction(cancelAction)
            let okAction = UIAlertAction(title: "确认", style: UIAlertActionStyle.default) {
                (action: UIAlertAction) -> Void in
                ZManager.instance.isLogin=false
                ZManager.instance.userId=""
                ZManager.instance.userToken=""
                ZManager.instance.saveDate()
                NetworkManager.instance.setDefaultManage()
                self.initData()
            }
            alertController.addAction(okAction)
            self.present(alertController, animated: true, completion: nil)
        }
    }
    func fileSizeAtPath(path: String)-> Float{
        let fileManager = FileManager.default
        var size: Float = 0
        if fileManager.fileExists(atPath: path) {
            let floder = try! FileManager.default.attributesOfItem(atPath: path)
            for (abc, bcd) in floder {
                if abc == FileAttributeKey.size {
                    size += (bcd as AnyObject).floatValue
                }
            }
        }
        return size / 1024 / 1024
    }
    func cacheSizeAtPath() {
        SDImageCache.shared.calculateSize { (fileCount, totalSize) in
            self.cacheSize = Float(Double(totalSize) / (1024.0 * 1024.0))
            DispatchQueue.main.async {
                self.tableView.reloadData()
            }
        }
    }

}


extension MemberCenterController: UITableViewDelegate, UITableViewDataSource {
    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        if GlobalCfg.shared.review && self.isLogin{
            return 3
        } else {
            return 4
        }
        
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        switch section {
        case 1:
            return 0
        case 2:
            return 26
        case 3:
            return 10
        default:
            return 0
        }
    }
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = UIView()
        view.backgroundColor = UIColor.clear
        return view
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell:UITableViewCell?
        switch indexPath.section {
        case 0:
            cell = tableView.dequeueReusableCell(withIdentifier: "MemberTopCell", for: indexPath)
            (cell as! MemberTopCell).setData(user)
        case 1:
            cell = tableView.dequeueReusableCell(withIdentifier: "VipInfoCell", for: indexPath)
            (cell as! VipInfoCell).setData(user)
        case 2:
            cell = tableView.dequeueReusableCell(withIdentifier: "MemberIconCell", for: indexPath)
            (cell as! MemberIconCell).delegate = self
            (cell as! MemberIconCell).setLoginStatus(self.isLogin)
        default:
            cell = tableView.dequeueReusableCell(withIdentifier: "ButtonCell", for: indexPath)
            (cell as! ButtonCell).setLoginStatus(self.isLogin)
        }
        return cell!
    }
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        switch indexPath.section {
        case 0:
            return 110 * ASPECT_RATIO
        case 2:
            return 140 * ASPECT_RATIO
        default:
            return 70 * ASPECT_RATIO
        }
        
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.section == 0 {
            toEditInfo()
        } else if indexPath.section == 1 {
            toBuyVip()
        } else if indexPath.section == 3 {
            if self.isLogin {
                toLoginOut()
            } else {
                toLogin()
            }
        }
    }
}

extension MemberCenterController: MemberIconCellDelegate {
    func didBtnClick(_ btnType: MemberIconType) {
        switch btnType {
        case .collect:
            toMyCollect()
        case .bind:
            toAccountBinding()
        case .cache:
            toClearCache()
        default:
            toAppRecommend()
        }
    }
}

extension MemberCenterController {
    func showProductList() {
        let productListController =  ProductListController()
        productListController.delegate = self
        self.present(productListController, animated: true) {
        }
    }
}

extension MemberCenterController: ProductListDelegate{
    func onClose() {
        self.webApi.getUserInfo{_ in
            self.tableView.reloadData()
        }
    }
}
