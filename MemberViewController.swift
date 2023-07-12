//
//  MemberViewController.swift
//  saluki
//  更多
//  已无用(20170603)
//
//  Created by zhl on 2017/3/16.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit
import SDWebImage

enum MemberCellType:NSNumber {
    //顶部用户信息
    case member_info = 0
    //按钮_文字居中
    case button = 1
    //通用
    case general = 2
    //空行
    case blank = 3
    //等级
    case level = 4
}
class MemberCellItem:NSObject {
    var title: String?
    var subTitle: String?
    var selector: Selector?
    var target: Any?
    var tag: Int?
    var cellType: MemberCellType = MemberCellType.general
    var infoObj: Any?
}
class MemberViewController: BaseViewController {
    
    var tableView = UITableView().then {
        $0.showsHorizontalScrollIndicator = false
        $0.showsVerticalScrollIndicator = false
        $0.separatorStyle = UITableViewCellSeparatorStyle.none
        $0.backgroundColor = BACK_COLOR
    }
    var cacheSize: Float = 0
    var dataArray: [MemberCellItem] = []
    var user:User = User()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.delegate = self
        tableView.dataSource = self
        self.view.addSubview(tableView)
        tableView.snp.makeConstraints { (make) -> Void in
            make.edges.equalTo(self.view).inset(UIEdgeInsetsMake(0,0,0,0))
        }
        tableView.register(MemberTopCell.classForCoder(), forCellReuseIdentifier: "MemberTopCell")
        tableView.register(MemberCell.classForCoder(), forCellReuseIdentifier: "MemberCell")
        tableView.register(UITableViewCell.classForCoder(), forCellReuseIdentifier: "UITableViewCell")
        tableView.register(MemberLevelCell.classForCoder(), forCellReuseIdentifier: "MemberLevelCell")
        
        user = User()
        
    }
    override func viewWillAppear(_ animated: Bool) {
        initData()
        if ZManager.instance.isLogin {
            user.nickname = ZManager.instance.showName
            user.avatar = ZManager.instance.avatar
        } else {
            user.nickname = "未注册用户"
        }
        cacheSizeAtPath()
    }
}

extension MemberViewController {
    func initData() {
        self.dataArray.removeAll()
        self.dataArray = []
        if !ZManager.instance.isLogin {
            let login = MemberCellItem().then{
                $0.cellType = .button
                $0.target = self
                $0.selector = #selector(toLogin)
                $0.title = "登录"
            }
            dataArray.append(login)
            let blankLogin = MemberCellItem().then{
                $0.cellType = .blank
            }
            dataArray.append(blankLogin)
        }
        let data:MemberCellItem = MemberCellItem()
        data.cellType = .member_info
        data.target = self
        data.selector = #selector(toEditInfo)
        dataArray.append(data)
        
        let level:MemberCellItem = MemberCellItem()
        level.cellType = .level
        level.target = self
        level.selector = #selector(toViewLevel)
        dataArray.append(level)
        
        
        let data1:MemberCellItem = MemberCellItem()
        data1.cellType = .general
        data1.title = "我的收藏"
        data1.target = self
        data1.selector = #selector(toMyCollect)
        dataArray.append(data1)
        
        let data2:MemberCellItem = MemberCellItem()
        data2.cellType = .general
        data2.title = "账号绑定"
        data2.target = self
        data2.selector = #selector(toAccountBinding)
        dataArray.append(data2)
        
        let data5:MemberCellItem = MemberCellItem()
        data5.cellType = .general
        data5.title = "注销账号"
        data5.subTitle = "共0.0M"
        data5.target = self
        data5.selector = #selector(toClearCache)
        dataArray.append(data5)
        
        let data3:MemberCellItem = MemberCellItem()
        data3.cellType = .general
        data3.title = "应用推荐"
        data3.target = self
        data3.selector = #selector(toAppRecommend)
        dataArray.append(data3)
        
        if ZManager.instance.isLogin {
            let blank = MemberCellItem()
            blank.cellType = .blank
            dataArray.append(blank)
            
            let data4:MemberCellItem = MemberCellItem()
            data4.cellType = .general
            data4.title = "退出登录"
            data4.target = self
            data4.selector = #selector(toLoginOut)
            dataArray.append(data4)
        }
        self.tableView.reloadData()
        
    }
}
extension MemberViewController {
    @objc func toEditInfo() {
        if self.isLogin {
            let memberInfoController = MemberInfoController()
            memberInfoController.hidesBottomBarWhenPushed = true
            self.navigationController?.pushViewController(memberInfoController, animated: true)
        } else {
            let alert = UIAlertController(title: "提示" , message: "该操作需要登录，是否立即登录？",  preferredStyle: .alert)
            let action = UIAlertAction(title: "取消", style: .cancel,  handler: nil)
            let okaction = UIAlertAction(title: "登录", style: .default) { _ in
                self.toLogin()
            }
            alert.addAction(action)
            alert.addAction(okaction)
            present(alert, animated: true, completion: nil)
        }
    }
    @objc func toViewLevel() {
        let url = levelinfo_url
        self.toWebPage(url, title: "我的等级")
    }
    @objc func toMyCollect() {
        if self.isLogin {
            let myCollectController = MyCollectController()
            myCollectController.hidesBottomBarWhenPushed = true
            self.navigationController?.pushViewController(myCollectController, animated: true)
        } else {
            let alert = UIAlertController(title: "提示" , message: "该操作需要登录，是否立即登录？",  preferredStyle: .alert)
            let action = UIAlertAction(title: "取消", style: .cancel,  handler: nil)
            let okaction = UIAlertAction(title: "登录", style: .default) { _ in
                self.toLogin()
            }
            alert.addAction(action)
            alert.addAction(okaction)
            present(alert, animated: true, completion: nil)
        }
        
    }
    @objc func toAccountBinding() {
        
        if self.isLogin {
            let bindAccListController = BindAccListController()
            bindAccListController.hidesBottomBarWhenPushed = true
            bindAccListController.user = self.user
            self.navigationController?.pushViewController(bindAccListController, animated: true)
        } else {
            let alert = UIAlertController(title: "提示" , message: "该操作需要登录，是否立即登录？",  preferredStyle: .alert)
            let action = UIAlertAction(title: "取消", style: .cancel,  handler: nil)
            let okaction = UIAlertAction(title: "登录", style: .default) { _ in
                self.toLogin()
            }
            alert.addAction(action)
            alert.addAction(okaction)
            present(alert, animated: true, completion: nil)
        }
    }
    @objc func toClearCache() {
        let alertController = UIAlertController(title: "确定清除缓存?", message: "", preferredStyle: UIAlertControllerStyle.alert)
        let cancelAction = UIAlertAction(title: "取消", style: UIAlertActionStyle.cancel, handler: nil)
        alertController.addAction(cancelAction)
        let okAction = UIAlertAction(title: "确认", style: UIAlertAction.Style.default) {
            (action: UIAlertAction) -> Void in
            SDImageCache.shared.clearDisk {
                self.cacheSizeAtPath()
            }
        }
        alertController.addAction(okAction)
        self.present(alertController, animated: true, completion: nil)
        
    }
    @objc func toAppRecommend() {
        let url = viphelp_url
        self.toWebPage(url, title: "联系客服")
    }
    //登出
    @objc func toLoginOut(){
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


extension MemberViewController: UITableViewDelegate, UITableViewDataSource {
    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        let cellItem = dataArray[section]
        switch cellItem.cellType {
        case .member_info:
            return 10
        case .level:
            return 1
        case .button:
            return 10
        case .blank:
            return 1
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
        let cellItem = dataArray[indexPath.section]
        switch cellItem.cellType {
        case .member_info:
            cell = tableView.dequeueReusableCell(withIdentifier: "MemberTopCell", for: indexPath)
            (cell as! MemberTopCell).setData(user)
        case .button:
            cell = tableView.dequeueReusableCell(withIdentifier: "UITableViewCell", for: indexPath)
            cell?.textLabel?.text = cellItem.title
            cell?.textLabel?.textColor = TITLE_COLOR
            cell?.backgroundColor = LIGHT_BACK_COLOR
        case .blank:
            cell = tableView.dequeueReusableCell(withIdentifier: "UITableViewCell", for: indexPath)
            cell?.textLabel?.text = ""
            cell?.backgroundColor = BACK_COLOR
        case .level:
            cell = tableView.dequeueReusableCell(withIdentifier: "MemberLevelCell", for: indexPath)
        default:
            cell = tableView.dequeueReusableCell(withIdentifier: "MemberCell", for: indexPath)
            if cellItem.subTitle != nil {
                let cacheStr = String(format: "共%0.2fM", self.cacheSize)
                (cell as! MemberCell).setDataWithSub(cellItem.title!, subTitle: cacheStr )
            } else {
                (cell as! MemberCell).setData(cellItem.title!)
            }
            
        }
        
        return cell!
    }
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let cellItem = dataArray[indexPath.section]
        switch cellItem.cellType {
        case .member_info:
            return 60 * ASPECT_RATIO
        case .level:
            return 50 * ASPECT_RATIO
        case .button:
            return 45 * ASPECT_RATIO
        case .blank:
            return 15 * ASPECT_RATIO
        default:
            return 45 * ASPECT_RATIO
        }
        
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cellData = dataArray[indexPath.section]
        if cellData.selector != nil {
            self.perform(cellData.selector!)
        }
    }
}
