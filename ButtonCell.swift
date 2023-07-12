//
//  ButtonCell.swift
//  saluki
//
//  Created by zhl on 2017/5/31.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit

class ButtonCell: UITableViewCell {
    
    lazy var backLayer = CALayer().then{
        $0.backgroundColor = SHADOW_COLOR
        $0.shadowOffset = SHADOW_SIZE
        $0.shadowOpacity = SHADOW_OPACITY
        $0.cornerRadius = CORNET_RADIUS
    }
    lazy var loginBtn = ZButton().then{
        $0.setTitle("登录", for: UIControlState.normal)
        $0.layer.masksToBounds = true
        $0.layer.cornerRadius = CORNET_RADIUS
        $0.setBackgroundImage(UIImage(named: "button_back"), for: UIControlState.normal)
        $0.isUserInteractionEnabled = false
    }
    
    lazy var logoutBtn:ZButton = {
        let button = ZButton(color: UIColor.white)
        button.setTitle("退出登录", for: UIControlState.normal)
        button.setTitleColor(TITLE_COLOR, for: UIControlState.normal)
        button.layer.masksToBounds = true
        button.layer.cornerRadius = CORNET_RADIUS
        button.isUserInteractionEnabled = false
        return button
    }()
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String!) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.selectionStyle = UITableViewCellSelectionStyle.none
        //布局
        self.backgroundColor = BACK_COLOR
        self.contentView.layer.addSublayer(backLayer)
        self.contentView.addSubview(loginBtn)
        self.contentView.addSubview(logoutBtn)
        loginBtn.snp.makeConstraints{
            $0.left.equalTo(self.contentView).offset(10)
            $0.right.equalTo(self.contentView).offset(-10)
            $0.top.equalTo(self.contentView).offset(10)
            $0.bottom.equalTo(self.contentView).offset(-10)
        }
        logoutBtn.snp.makeConstraints{
            $0.left.equalTo(self.contentView).offset(10)
            $0.right.equalTo(self.contentView).offset(-10)
            $0.top.equalTo(self.contentView).offset(10)
            $0.bottom.equalTo(self.contentView).offset(-10)
        }
        logoutBtn.isHidden = true
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func layoutSubviews() {
        backLayer.frame = loginBtn.frame
    }
    
    func setLoginStatus(_ status: Bool) {
        loginBtn.isHidden = status
        logoutBtn.isHidden = !status
    }
}
