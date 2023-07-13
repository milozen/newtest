//
//  LoginViewController.swift
//  poodle
//
//  Created by zhl on 2017/2/22.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit
import ManualLayout

class LoginViewController: BaseViewController {
    var sourceViewController:UIViewController?
    var mobile:String = ""
    var user: User = User()
    var loginType = "weixin"
    var privacyAggred = false

    //去除必须登陆
    //var mustLogin = false
    
//    let mustLoginLabel = UILabel().then{
//        $0.text = "您必须登录以后才可以使用本APP"
//        $0.font = LIST_SUMMARY_FONT
//        $0.textColor = LIGHT_TXT_COLOR
//    }
    
    let backView = UIImageView().then{
        $0.image = UIImage(named: "view_back")
    }
    let baseView = UIScrollView().then{
        $0.backgroundColor = UIColor.clear
        $0.showsVerticalScrollIndicator = false;
        $0.showsHorizontalScrollIndicator = false;
    }
    let phoneView = UIView().then{
        $0.backgroundColor = UIColor.white
        $0.layer.masksToBounds = true
        $0.layer.cornerRadius = CORNET_RADIUS
    }
    let phoneLayer = CALayer().then{
        $0.backgroundColor = SHADOW_COLOR
        $0.shadowOffset = SHADOW_SIZE
        $0.shadowOpacity = SHADOW_OPACITY
        $0.cornerRadius = CORNET_RADIUS
    }
    let passwordView = UIView().then{
        $0.backgroundColor = UIColor.white
        $0.layer.masksToBounds = true
        $0.layer.cornerRadius = CORNET_RADIUS
    }
    let passwordLayer = CALayer().then{
        $0.backgroundColor = SHADOW_COLOR
        $0.shadowOffset = SHADOW_SIZE
        $0.shadowOpacity = SHADOW_OPACITY
        $0.cornerRadius = CORNET_RADIUS
    }
    let phone = UITextField().then{
        $0.placeholder = "请输入手机号"
        $0.placeHolderColor = LIGHT_TXT_COLOR
        $0.textColor = SUMMARY_COLOR
        $0.font = UIFont.systemFont(ofSize: 12)
        $0.clearButtonMode = .whileEditing
        $0.keyboardType = .numberPad
        
    }
    let phoneIcon = UIImageView().then{
        $0.image = UIImage(named:"login-user.png")
    }
    let passIcon = UIImageView().then{
        $0.image = UIImage(named:"login-password.png")
    }
    
    let password = UITextField().then {
        $0.placeholder = "请输入密码"
        $0.isSecureTextEntry = true
        $0.font = UIFont.systemFont(ofSize: 12)
        $0.placeHolderColor = LIGHT_TXT_COLOR
        $0.textColor = SUMMARY_COLOR
        $0.clearButtonMode = .whileEditing
    }
    let loginLayer = CALayer().then{
        $0.backgroundColor = SHADOW_COLOR
        $0.shadowOffset = SHADOW_SIZE
        $0.shadowOpacity = SHADOW_OPACITY
        $0.cornerRadius = CORNET_RADIUS
    }
    let loginBtn = ZButton().then{
        $0.setTitle("登录", for: UIControlState.normal)
        $0.layer.masksToBounds = true
        $0.layer.cornerRadius = CORNET_RADIUS
    }
    
    let registBtn:ZButton = {
        let button = ZButton(color: UIColor.white)
        button.setTitle("新用户注册", for: UIControlState.normal)
        button.setTitleColor(UIColor.black, for: UIControlState.normal)
        button.layer.masksToBounds = true
        button.layer.cornerRadius = CORNET_RADIUS
        return button
    }()
    let registLayer = CALayer().then{
        $0.backgroundColor = SHADOW_COLOR
        $0.shadowOffset = SHADOW_SIZE
        $0.shadowOpacity = SHADOW_OPACITY
        $0.cornerRadius = CORNET_RADIUS
    }
    let forgotBtn = UIButton().then{
        $0.setTitle("忘记密码？", for: UIControlState.normal)
    }
    let privacyView = PrivacyView().then{_ in
        
    }
    
    let authView = OtherAuthView().then{_ in
        
    }
    
    

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        self.navigationItem.title="登录"
        self.navigationController?.isNavigationBarHidden = false
        self.view.backgroundColor = ZColorUtil.fromHexString(hexString: "f3f3f3")
        self.view.addSubview(backView)
        backView.snp.makeConstraints{
            $0.width.equalTo(221*ASPECT_RATIO)
            $0.height.equalTo(409*ASPECT_RATIO)
            $0.right.equalTo(self.view)
            $0.bottom.equalTo(self.view).offset(-24)
        }
        self.view.addSubview(baseView);
        baseView.snp.makeConstraints{
            $0.width.equalTo(self.view)
            $0.height.equalTo(self.view)
            $0.right.equalTo(self.view)
            $0.top.equalTo(self.view)
        }
        baseView.layer.addSublayer(phoneLayer)
        baseView.addSubview(phoneView)
        phoneView.snp.makeConstraints{
            $0.centerX.equalTo(baseView)
            $0.top.equalTo(60  * ASPECT_RATIO + topMargin)
            $0.width.equalTo(self.baseView).multipliedBy(3.0/5)
            $0.height.equalTo(40)
        }
        
        phoneView.addSubview(phoneIcon)
        phoneIcon.snp.makeConstraints{
            $0.left.equalTo(phoneView).offset(8)
            $0.centerY.equalTo(phoneView)
            $0.height.equalTo(20)
            $0.width.equalTo(20)
        }
        phoneView.addSubview(phone)
        if self.mobile.count == 11 {
//            phone.text = ZStringUtil.replaceInRange(string: self.mobile, replaceString: "****", start: 3, len: 4)
            phone.text = self.mobile
        }
        
        
        
        phone.snp.makeConstraints{
            $0.left.equalTo(phoneIcon.snp.right).offset(8)
            $0.right.equalTo(phoneView)
            $0.top.equalTo(phoneView)
            $0.height.equalTo(phoneView)
        }
        
        phone.addTarget(self, action: #selector(checkPhoneField(sender:)), for: UIControlEvents.editingChanged)

        //去除必须登陆
//        if self.mustLogin {
//            self.baseView.addSubview(mustLoginLabel)
//            mustLoginLabel.snp.makeConstraints{
//                $0.top.equalTo(10  * ASPECT_RATIO + topMargin)
//                $0.width.equalTo(phoneView)
//                $0.height.equalTo(30)
//                $0.centerX.equalTo(phoneView)
//            }
//        }
        baseView.layer.addSublayer(passwordLayer)
        baseView.addSubview(passwordView)
        passwordView.snp.makeConstraints{
            $0.left.equalTo(phoneView)
            $0.right.equalTo(phoneView)
            $0.height.equalTo(phoneView)
            $0.top.equalTo(phoneView.snp.bottom).offset(20 * ASPECT_RATIO)
        }
        passwordView.addSubview(passIcon)
        
        passIcon.snp.makeConstraints{
            $0.left.equalTo(passwordView).offset(8)
            $0.centerY.equalTo(passwordView)
            $0.height.equalTo(20)
            $0.width.equalTo(20)
        }
        passwordView.addSubview(password)
        password.snp.makeConstraints { (make) -> Void in
            make.left.equalTo(passIcon.snp.right).offset(8)
            make.top.equalTo(passwordView)
            make.right.equalTo(passwordView)
            make.height.equalTo(passwordView)
        }
        
        password.addTarget(self, action: #selector(checkPassField(sender:)), for: UIControlEvents.editingChanged)
        loginBtn.setBackgroundImage(UIImage(named: "button_back"), for: UIControlState.normal)
        loginBtn.addTarget(self, action: #selector(login(sender:)), for: UIControlEvents.touchUpInside)
        forgotBtn.setTitleColor(SUMMARY_COLOR, for: UIControlState.normal)
        baseView.addSubview(forgotBtn)
        forgotBtn.snp.makeConstraints { (make) -> Void in
            make.top.equalTo(password.snp.bottom).offset(20 * ASPECT_RATIO)
            make.right.equalTo(password.snp.right)
            make.height.equalTo(password)
        }
        baseView.layer.addSublayer(loginLayer)
        baseView.addSubview(loginBtn)
        loginBtn.snp.makeConstraints { (make) -> Void in
            make.left.equalTo(phoneView)
            make.top.equalTo(forgotBtn.snp.bottom).offset(20 * ASPECT_RATIO)
            make.right.equalTo(phoneView)
            make.height.equalTo(phone)
        }
        baseView.layer.addSublayer(registLayer)
        registBtn.addTarget(self, action: #selector(toRegist), for: UIControlEvents.touchUpInside)
        baseView.addSubview(registBtn)
        registBtn.snp.makeConstraints { (make) -> Void in
            make.top.equalTo(loginBtn.snp.bottom).offset(20 * ASPECT_RATIO)
            make.width.equalTo(loginBtn)
            make.height.equalTo(loginBtn)
            make.left.equalTo(loginBtn)
        }
        registBtn.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        
        
        forgotBtn.addTarget(self, action: #selector(forgotPassword), for: UIControlEvents.touchUpInside)
        forgotBtn.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        
        authView.delegate = self
        baseView.addSubview(authView)
        authView.snp.makeConstraints{
            $0.left.equalTo(phoneView)
            $0.width.equalTo(phoneView)
            $0.height.equalTo(120 * ASPECT_RATIO)
            $0.top.equalTo(registBtn.snp.bottom).offset(55 * ASPECT_RATIO)
            $0.bottom.equalTo(baseView.snp.bottom).offset(-20)
        }
        //不隐藏 authView
//        if GlobalCfg.shared.review {
//            authView.isHidden = true
//        }
        
        
        let tapGestureRecognizer = UITapGestureRecognizer.init(target: self, action: #selector(keyboardHide(tap:)))
        tapGestureRecognizer.cancelsTouchesInView = false
        baseView.addGestureRecognizer(tapGestureRecognizer)
        
        baseView.addSubview(privacyView)
        privacyView.delegate = self
        privacyView.snp.makeConstraints{
            $0.centerX.equalTo(baseView)
            $0.height.equalTo(40*ASPECT_RATIO)
            $0.top.equalTo(authView.snp.bottom).offset(20)
        }
        
    }
    @objc func keyboardHide(tap: UITapGestureRecognizer) {
        phone.resignFirstResponder()
        password.resignFirstResponder()
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    override func viewDidLayoutSubviews() {
        phoneLayer.frame = phoneView.frame
        passwordLayer.frame = passwordView.frame
        loginLayer.frame = loginBtn.frame
        registLayer.frame = registBtn.frame
    }
}

extension LoginViewController {
    /**
     登陆成功后，返回前界面
     改为返回主界面。
     */
    func successToSource() {
        if self.presentLogined {
            self.dismiss(animated: true, completion: nil)
        } else {
            _ = self.navigationController?.popToViewController(sourceViewController!, animated: true)
        }
        
    }
    @objc func login(sender: AnyObject) {
        
        let phoneNo = phone.text!
        let authCode:String = password.text!
        if (!ZStringUtil.checkPhone(phone: phoneNo)){
            alert(title: "友情提醒",message: "你输入的手机号码不正确",result: VlResult.WARNING)
        } else if (!ZStringUtil.checkPwdLength(pwd: authCode)){
            alert(title: "友情提醒",message: "你输入的密码不正确",result: VlResult.WARNING)
        } else if !self.privacyAggred {
            alert(title: "友情提醒",message: "请阅读《用户协议》和《隐私政策》后勾选同意",result: VlResult.WARNING)
        } else {
            webApi.userLogin(phoneNo: phoneNo, password: authCode, finish: finishUserLogin)
        }
    }
    
    @objc func checkPhoneField(sender: UITextField) {
        let phoneNo:String=sender.text!
        sender.text = phoneNo.substring(to: 11)
    }
    @objc func checkPassField(sender: UITextField) {
        let pass:String=sender.text!
        sender.text=pass.substring(to: 15)
    }
    @objc func forgotPassword() {
        let resetPasswordController = ResetPasswordController()
        if phone.text != nil && phone.text != "" {
            resetPasswordController.mobile = phone.text!
        }
        self.navigationController?.pushViewController(resetPasswordController, animated: true)
    }
    
    func getUserInfoForPlatform(_ platformType: UMSocialPlatformType) {
        UMSocialManager.default().getUserInfo(with: platformType, currentViewController: self) { (result, error) in
            if error != nil {
                self.alert(message: "登录失败，请选择其他方式")
            } else {
                let resp = result as! UMSocialUserInfoResponse
                
                self.user.uid = resp.uid
                self.user.nickname = resp.name
                self.user.token = resp.accessToken
                self.user.avatar = resp.iconurl
                self.user.sex = resp.gender
                
                self.oauthCheck()
                vlog("uid:\(resp.uid ?? "")")
                vlog("openid:\(resp.openid ?? "")")
                vlog("accessToken:\(resp.accessToken ?? "")")
                vlog("refreshToken:\(resp.refreshToken ?? "")")
                vlog("expiration:\(String(describing: resp.expiration))")
                
                vlog("name:\(resp.name ?? "")")
                vlog("iconurl:\(resp.iconurl ?? "")")
                vlog("gender:\(resp.gender ?? "")")
            }
        }
    }
    func toAccountBind() {
        let accountBindController = AccountBindController()
        accountBindController.user = self.user
        accountBindController.loginType = self.loginType
        self.navigationController?.pushViewController(accountBindController, animated: true)
    }
}

//MARK:: WebDelegate
private extension LoginViewController {
    func finishUserLogin(result: Bool,resultStr:String) {
        if(result){
            alert(message: resultStr,alertDismiss: successToSource)
        } else {
            let errStr = String(format: "%@",resultStr)
            alert(title: "出错了",message: errStr,result: VlResult.ERROR)
        }
    }
    func oauthCheck() {
        webApi.oauthCheck(user.uid!, type: self.loginType, token: user.token) {
            if $0 {
                self.alert(message: "登录成功",alertDismiss: self.successToSource)
            } else {
                self.toAccountBind()
            }
        }
    }
    
}

extension LoginViewController: OtherAuthDelegate {
    func didWechatClick() {
        loginType = "weixin"
        getUserInfoForPlatform(UMSocialPlatformType.wechatSession)
    }
    
    func didWeiboClick() {
        loginType = "weibo"
        getUserInfoForPlatform(UMSocialPlatformType.sina)
    }
    
    func didQQClick() {
        loginType = "qq"
        getUserInfoForPlatform(UMSocialPlatformType.QQ)
    }
}

extension LoginViewController: PrivacyDelegate {
    func privacyClicked() {
        self.toWebPage(GlobalCfg.shared.infoUrl, present: true)
    }
    
    func userProtoClicked() {
        self.toWebPage(GlobalCfg.shared.userUrl, present: true)
    }
    
    func termsofUseClicked() {
        self.toWebPage(GlobalCfg.shared.termOfUseUrl, present: true)
    }
    
    func onAgreeChange(result: Bool) {
        self.privacyAggred = result
    }

}
