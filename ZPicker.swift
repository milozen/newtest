//
//  ZPicker.swift
//  saluki
//
//  Created by zhl on 2017/4/6.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit

public enum ZPickerStyle : Int {
    
    case nomal    = 0
    
    case date     = 1
    
    case area     = 2
    
    case height   = 3
    
    case weight   = 4
    
    case datetime = 5
    
    case duration = 6
}

protocol PickerDelegate : NSObjectProtocol {
    func chooseElements(picker: ZPicker, content: [Int: ZPickerObject])
    func chooseDate(picker: ZPicker, date: Date)
}

class ZPicker: UIView {
    
    var pickerDelegate : PickerDelegate?
    
    fileprivate let picker_height:CGFloat! = 260
    fileprivate var picker: UIPickerView = UIPickerView()
    fileprivate var datePicker: UIDatePicker = UIDatePicker()
    fileprivate var content: [ZPickerObject]?
    fileprivate var pickerStyle: ZPickerStyle?
    fileprivate var backgroundBtn: UIButton = UIButton()
    fileprivate var tempDic: Dictionary = [Int:Int]()
    fileprivate var numComponents:Int = 0
    var target: UIView?
    
    var contentArray:[ZPickerObject]? {
        get {
            return self.content
        }
        set {
            self.content = newValue
            self.initializeContentArray()
        }
    }
    
    init(delegate: PickerDelegate, style: ZPickerStyle) {
        pickerDelegate = delegate
        pickerStyle = style
        let v_frame = CGRect(x: 0, y: UIScreen.height, width: UIScreen.width, height: picker_height)
        super.init(frame: v_frame)
        let view = UIView(frame: CGRect(x: 0, y: 0, width: UIScreen.width, height: 44))
        view.backgroundColor = UIColor.RGBA(230, 230, 230, 1)
        self.addSubview(view)
        
        let cancelBtn = UIButton(type: UIButtonType.system)
        cancelBtn.frame = CGRect(x: 0, y:  0, width: 60, height: 44)
        cancelBtn.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        cancelBtn.setTitle("取 消", for: UIControlState.normal)
        cancelBtn.setTitleColor(UIColor.RGBA(18, 93, 255, 1), for: UIControlState.normal)
        cancelBtn.addTarget(self, action: #selector(cancelButtonClick), for: .touchUpInside)
        self.addSubview(cancelBtn)
        
        let doneBtn = UIButton(type: UIButtonType.system)
        doneBtn.frame = CGRect(x: UIScreen.width - 60, y: 0, width: 60, height: 44)
        doneBtn.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        doneBtn.setTitle("确 定", for: UIControlState.normal)
        doneBtn.setTitleColor(UIColor.RGBA(18, 93, 255, 1), for: UIControlState.normal)
        doneBtn.addTarget(self, action: #selector(doneButtonClick), for: .touchUpInside)
        self.addSubview(doneBtn)
        
        backgroundBtn = UIButton(type: UIButtonType.system)
        backgroundBtn.frame = CGRect(x: 0, y: 0, width: UIScreen.width, height: UIScreen.height)
        backgroundBtn.backgroundColor = UIColor.RGBA(0, 0, 0, 0.0)
        switch style {
        case .weight:
            self.initWeightData()
            self.picker = UIPickerView(frame: CGRect(x: 0, y: 44, width: UIScreen.width, height: picker_height - 44))
            self.picker.delegate = self
            self.picker.dataSource = self
            self.picker.backgroundColor = UIColor.white
            self.addSubview(self.picker)
        case .height:
            self.initHeightData()
            self.picker = UIPickerView(frame: CGRect(x: 0, y: 44, width: UIScreen.width, height: picker_height - 44))
            self.picker.delegate = self
            self.picker.dataSource = self
            self.picker.backgroundColor = UIColor.white
            self.addSubview(self.picker)
        case .area:
            self.initAreaData()
            self.picker = UIPickerView(frame: CGRect(x: 0, y: 44, width: UIScreen.width, height: picker_height - 44))
            self.picker.delegate = self
            self.picker.dataSource = self
            self.picker.backgroundColor = UIColor.white
            self.addSubview(self.picker)
        case .nomal:
            self.picker = UIPickerView(frame: CGRect(x: 0, y: 44, width: UIScreen.width, height: picker_height - 44))
            self.picker.delegate = self
            self.picker.dataSource = self
            self.picker.backgroundColor = UIColor.white
            self.addSubview(self.picker)
        case .date:
            self.datePicker = UIDatePicker(frame: CGRect(x: 0, y: 44, width: UIScreen.width, height: picker_height - 44))
            self.datePicker.datePickerMode = UIDatePickerMode.date
            self.datePicker.locale = Locale(identifier: "zh_CN")
            self.datePicker.backgroundColor = UIColor.white
            self.datePicker.addTarget(self, action: #selector(self.dateChoosePressed(datePicker:)), for: .valueChanged)
            self.addSubview(self.datePicker)
        case .datetime:
            self.datePicker = UIDatePicker(frame: CGRect(x: 0, y: 44, width: UIScreen.width, height: picker_height - 44))
            self.datePicker.datePickerMode = UIDatePickerMode.dateAndTime
            self.datePicker.calendar = NSCalendar.current
            self.datePicker.locale = Locale(identifier: "zh_CN")
            self.datePicker.backgroundColor = UIColor.white
            self.datePicker.addTarget(self, action: #selector(self.dateChoosePressed(datePicker:)), for: .valueChanged)
            self.addSubview(self.datePicker)
        case .duration:
            self.picker = UIPickerView(frame: CGRect(x: 0, y: 44, width: UIScreen.width, height: picker_height - 44))
            self.picker.delegate = self
            self.picker.dataSource = self
            self.picker.backgroundColor = UIColor.white
            self.addSubview(self.picker)
        }
    }
    
    func picker_partingLine(color: UIColor?) {
        let sep_color = color ?? UIColor.lightGray
        if #available(iOS 10.0, *) {
            for view in picker.subviews {
                if view.height < 1 {
                    view.backgroundColor = sep_color
                }
            }
        }
    }
    private func initializeContentArray() {
        
        var temp:Int = 0
        if let array = content {
            if array.count > 0 {
                temp = 1
                tempDic[temp - 1] = 0
                var object = array.first
                while object?.subArray != nil {
                    temp += 1
                    tempDic[temp - 1] = 0
                    let arr = object?.subArray
                    if let temp_arr = arr {
                        if temp_arr.count > 0 {
                            object = temp_arr.first
                        }else {
                            break
                        }
                    }else{
                        break
                    }
                }
            }
        }
        numComponents = temp
        picker.reloadAllComponents()
    }
    
    @objc func dateChoosePressed(datePicker: UIDatePicker) {
        //print("select date \(datePicker.date.string_from(formatter: "yyyy-MM-dd"))")
    }
    @objc func doneButtonClick() {
        if pickerStyle == .date || pickerStyle == .datetime{
            pickerDelegate?.chooseDate(picker: self, date: datePicker.date)
        } else if pickerStyle == .duration {
            var resultDic = [Int: ZPickerObject]()
            for i in 0...2 {
                let value:Int = tempDic[i] ?? 0
                resultDic[i] = ZPickerObject(value.string, code: value.string)
            }
            pickerDelegate?.chooseElements(picker: self, content: resultDic)
        } else {
            var resultDic = [Int: ZPickerObject]()
            
            if let array = content {
                var tempArray:Array = array
                for i in 0...numComponents {
                    let value:Int = tempDic[i] ?? 0
                    if tempArray.count > value {
                        let object = tempArray[value]
                        resultDic[i] = object
                        if let arr = object.subArray {
                            tempArray = arr
                        }else {
                            tempArray = [ZPickerObject]()
                        }
                    }
                }
            }
            pickerDelegate?.chooseElements(picker: self, content: resultDic)
        }
        self.hiddenPicker()
    }
    @objc func cancelButtonClick(btn:UIButton) {
        self.hiddenPicker()
    }
    public func show() {
        UIApplication.shared.keyWindow?.addSubview(self.backgroundBtn)
        UIApplication.shared.keyWindow?.addSubview(self)
        UIView.animate(withDuration: 0.35, animations: {
            self.backgroundBtn.backgroundColor = UIColor.RGBA(0, 0, 0, 0.3)
            self.top = UIScreen.height - self.picker_height
        }) { (finished: Bool) in
        }
    }
    private func hiddenPicker() {
        UIView.animate(withDuration: 0.35, animations: {
            self.backgroundBtn.backgroundColor = UIColor.RGBA(0, 0, 0, 0.0)
            self.top = UIScreen.height
        }) { (finished: Bool) in
            for view in self.subviews {
                view.removeFromSuperview()
            }
            self.removeFromSuperview()
            self.backgroundBtn.removeFromSuperview()
        }
    }
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setDefault(_ val: String?) {
        switch self.pickerStyle! {
        case .date:
            let dateformater = DateFormatter()
            dateformater.dateFormat = "yyyy-MM-dd"
            if val != nil && val != ""{
                let date = dateformater.date(from: val!)
                self.datePicker.setDate(date!, animated: true)
            } else {
                self.datePicker.setDate(Date(), animated: true)
            }
        case .datetime:
            let dateformater = DateFormatter()
            dateformater.dateFormat = "yyyy-MM-dd HH:mm:ss"
            if val != nil {
                let date = dateformater.date(from: val!)
                self.datePicker.setDate(date!, animated: true)
            } else {
                self.datePicker.setDate(Date(), animated: true)
            }
        case .area:
            if val != nil {
                var ddic = [Int: Int]()
                let areas = val?.split(separator: ".")
                let obj1 = ZPickerObject()
                obj1.title = String((areas?.first)!)
                let indexs = self.content?.indexes(of: obj1)
                if (indexs?.count)! > 0 {
                    ddic[0] = indexs?.first
                    let subArr = self.content?[(indexs?.first)!].subArray
                    let obj2 = ZPickerObject()
                    obj2.title = String((areas?.last)!)
                    let indexs2 = subArr?.indexes(of: obj2)
                    if (indexs2?.count)! > 0 {
                        ddic[1] = indexs2?.first
                    }
                }
                self.setDefaultValue(ddic)
            }
            
        case .height:
            var ddic = [Int: Int]()
            var obj = ZPickerObject("180", code: "180")
            if val != nil {
                obj = ZPickerObject(val!, code: val!)
            }
            let indexs = self.content?.indexes(of: obj)
            if (indexs?.count)! > 0 {
                ddic[0] = indexs?.first
                tempDic[0] = indexs?.first
                self.setDefaultValue(ddic)
            }
        case .weight:
            var ddic = [Int: Int]()
            var obj = ZPickerObject("75", code: "75")
            if val != nil {
                obj = ZPickerObject(val!, code: val!)
            }
            let indexs = self.content?.indexes(of: obj)
            if (indexs?.count)! > 0 {
                ddic[0] = indexs?.first
                tempDic[0] = indexs?.first
                self.setDefaultValue(ddic)
            }
        case .duration:
            var ddic = [Int: Int]()
            let countTime = val?.int ?? 0
            let hour = countTime/(60 * 60)
            let minute = (countTime%(60 * 60 ))/60
            let second = countTime%60
            ddic[0] = hour
            ddic[1] = minute
            ddic[2] = second
            tempDic[0] = hour
            tempDic[1] = minute
            tempDic[2] = second
            numComponents = 2
            self.setDefaultValue(ddic)
        default:
            var ddic = [Int: Int]()
            var obj = ZPickerObject()
            if val != nil {
                obj = ZPickerObject(val!, code: val!)
            }
            let indexs = self.content?.indexes(of: obj)
            if (indexs?.count)! > 0 {
                ddic[0] = indexs?.first
            } else {
                ddic[0] = 0
            }
            self.setDefaultValue(ddic)
        }
    }
    func setDefaultValue(_ defaultDic: [Int:Int]) {
        for i in 0...numComponents {
            if defaultDic[i] != nil {
                self.picker.selectRow(defaultDic[i]!, inComponent: i, animated: true)
            }
        }
    }
}
extension ZPicker: UIPickerViewDelegate,UIPickerViewDataSource {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        if pickerStyle == .duration {
            return 3
        }
        return numComponents
    }
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        var row:Int = 0
        if pickerStyle == .duration {
            switch component {
            case 0:
                return 25
            default:
                return 61
            }
        }
        if let array = content {
            var tempArray:Array = array
            for i in 0...numComponents {
                let value:Int = tempDic[i] ?? 0
                if component == i {
                    row = tempArray.count
                }
                if tempArray.count > value {
                    let object = tempArray[value]
                    if let arr = object.subArray {
                        tempArray = arr
                    }else {
                        tempArray = [ZPickerObject]()
                    }
                }
                if component == i {
                    return row
                }
            }
            return 0
        }
        return 0
    }
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerStyle != .date {
            self.picker_partingLine(color: UIColor.lightGray)
        }
        if pickerStyle == .duration {
            if row < 10 && component > 0{
                return "0"+row.string
            }
            return row.string
        }
        var str:String = ""
        if let array = content {
            var tempArray:Array = array
            for i in 0...numComponents {
                let value:Int = tempDic[i] ?? 0
                if component == i {
                    let object = tempArray[row]
                    str = object.title ?? "未知"
                }
                if tempArray.count > value {
                    let object = tempArray[value]
                    if let arr = object.subArray {
                        tempArray = arr
                    }else {
                        tempArray = [ZPickerObject]()
                    }
                }
            }
            return str
        }
        return ""
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        tempDic.updateValue(row, forKey: component)
        if pickerStyle != .duration {
            if (component + 1) < numComponents {
                for i in (component+1)..<numComponents {
                    tempDic.updateValue(0, forKey:i)
                    pickerView.selectRow(0, inComponent: i, animated: false)
                }
            }
            pickerView.reloadAllComponents()
        }
    }
    
}

extension ZPicker {
    func initAreaData() {
        var dataArray = [ZPickerObject]()
        let plistPath:String = Bundle.main.path(forAuxiliaryExecutable: "area.plist") ?? ""
        let plistArray = NSArray(contentsOfFile: plistPath)
        let proviceArray = NSArray(array: plistArray!)
        for i in 0..<proviceArray.count {
            var subs0 = [ZPickerObject]()
            
            let cityzzz:NSDictionary = proviceArray.object(at: i) as! NSDictionary
            let cityArray:NSArray = cityzzz.value(forKey: "cities") as! NSArray
            for j in 0..<cityArray.count {
//                var subs1 = [ZPickerObject]()
//                let areazzz:NSDictionary = cityArray.object(at: j) as! NSDictionary
//                let areaArray:NSArray = areazzz.value(forKey: "areas") as! NSArray
//                for m in 0..<areaArray.count {
//                    let object = ZPickerObject()
//                    object.title = areaArray.object(at: m) as? String
//                    subs1.append(object)
//                }
                let citymmm:NSDictionary = cityArray.object(at: j) as! NSDictionary
                let cityStr:String = citymmm.value(forKey: "city") as! String
                let object = ZPickerObject()
                object.title = cityStr
                subs0.append(object)
//                object.subArray = subs1
            }
            let provicemmm:NSDictionary = proviceArray.object(at: i) as! NSDictionary
            let proviceStr:String? = provicemmm.value(forKey: "state") as! String?
            let object = ZPickerObject()
            object.title = proviceStr
            object.subArray = subs0
            dataArray.push(object)
            self.contentArray = dataArray
        }
    }
    func initHeightData() {
        var dataArray = [ZPickerObject]()
        for i in 30 ... 255 {
            dataArray.append(ZPickerObject(i.string, code: i.string))
        }
        self.contentArray = dataArray
    }
    func initWeightData() {
        var dataArray = [ZPickerObject]()
        for i in 5 ... 255 {
            dataArray.append(ZPickerObject(i.string, code: i.string))
        }
        self.contentArray = dataArray
    }
}

public class ZPickerObject: NSObject {
    
    var title:String?
    var subArray:[ZPickerObject]?
    var code:String?
    
    init(_ title: String, code: String) {
        super.init()
        self.title = title
        self.code = code
    }
    public override init() {
        super.init()
    }
    
    public override func isEqual(_ object: Any?) -> Bool {
        if self.code != nil {
            return self.code == (object as! ZPickerObject).code
        } else {
            return self.title == (object as! ZPickerObject).title
        }
    }
    
}
