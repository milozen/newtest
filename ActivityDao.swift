//
//  ActivityDao.swift
//  saluki
//
//  Created by zhl on 2017/4/13.
//  Copyright © 2017年 zhl. All rights reserved.
//

import GRDB
import SwifterSwift
enum DataType: Int {
    case local = 0, server = 1, all = 2
}

class ActivityDao: Dao{
    let dayformat = DateFormatter()
    let timeformat = DateFormatter()
    override init() {
        super.init()
        do {
            try self.dbQueue = self.getDbQueue()
            dayformat.dateFormat = "yyyy-MM-dd"
            timeformat.dateFormat = "yyyy-MM-dd HH:mm:ss"
        } catch _ {
            
        }
    }
    
    
    func createTable() throws{
        try dbQueue?.inDatabase { db in
            let isExist = try db.tableExists("activity")
            if !isExist {
                try db.create(table: "activity") { t in
                    t.column("local_id", .integer).primaryKey()
                    t.column("id", .text)
                    t.column("title", .text)
                    t.column("beginTime", .datetime)
                    t.column("endTime", .datetime)
                    t.column("duration", .integer)
                    t.column("desc", .text)
                    t.column("year", .integer)
                    t.column("month", .integer)
                    t.column("day", .text)
                    t.column("uploaded", .boolean).notNull().defaults(to: false)
                }
            }
        }
    }
    
    /**
     插入一个数据
     */
    func insert(_ data: Activity) throws {
        if data.title == nil {
            data.title = timeformat.string(from: data.beginTime!)
        }
        if data.day == nil {
            data.day = dayformat.string(from: data.beginTime!)
        }
        if data.year == nil {
            data.year = data.beginTime?.year
        }
        if data.month == nil {
            data.month = data.beginTime?.month
        }
        try dbQueue?.inDatabase { db in
            try data.insert(db)
        }
    }
    
    func update(_ data: Activity) throws {
        try dbQueue?.inDatabase { db in
            try data.update(db)
        }
    }
    
    func delete(_ data: Activity ) throws {
        try dbQueue?.inDatabase { db in
            _ = try data.delete(db)
        }
    }
    
    func getList(_ dataType: DataType, year: Int, month: Int, limit: Int, offset: Int, result: (Int, [Activity])-> ()) throws {
        try dbQueue?.inDatabase { db in
            let timeColumn = Column("beginTime")
            var queryObj = Activity.order(timeColumn.desc)
            if dataType != .all {
                let uploadColumn = Column("uploaded")
                queryObj = queryObj.filter(uploadColumn == dataType.rawValue)
            }
            if year > 0 {
                let yearColumn = Column("year")
                queryObj = queryObj.filter(yearColumn == year)
            }
            if month > 0 {
                let monthColumn = Column("month")
                queryObj = queryObj.filter(monthColumn == month)
            }
            if limit > 0 {
                queryObj = queryObj.limit(limit, offset: offset)
            }
            let datas = try queryObj.fetchAll(db)
            let count = try queryObj.fetchCount(db)
            result(count, datas)
        }
    }
    
    func statisticsAll(_ dataType: DataType, result: (ActCount)-> ())throws {
        try dbQueue?.inDatabase { db in
            var sumSql = "SELECT SUM(duration) FROM activity"
            var countSql = "SELECT COUNT(local_id) FROM activity"
            var lastSql = "SELECT duration FROM activity"
            if dataType != .all {
                sumSql = sumSql + " WHERE uploaded = " + dataType.rawValue.string
                countSql = countSql + " WHERE uploaded = " + dataType.rawValue.string
                lastSql = lastSql + " WHERE uploaded = " + dataType.rawValue.string
            }
            lastSql = lastSql + " ORDER BY beginTime DESC LIMIT 1"
            let sum = try Int.fetchOne(db, sumSql)
            let count = try Int.fetchOne(db, countSql)!
            let last = try Int.fetchOne(db, lastSql)
            var actCount = ActCount()
            actCount.count = count
            actCount.countTime = sum ?? 0
            actCount.lastTime = last ?? 0
            result(actCount)
        }
    }
    
    func statisticsYear(_ dataType: DataType, result: ([StatObj])-> ()) throws{
        try dbQueue?.inDatabase { db in
            var dataList = [StatObj]()
            var sql = "SELECT year,SUM(duration),COUNT(local_id) FROM activity GROUP BY year"
            if dataType != .all {
                sql = "SELECT year,SUM(duration),COUNT(local_id) FROM activity WHERE uploaded = "+dataType.rawValue.string+" GROUP BY year"
            }
            let rows = try Row.fetchCursor(db, sql)
            while let row = try rows.next() {
                let title = row[0] as String
                let sum = row[1] as Int
                let count = row[2] as Int
                dataList.push(StatObj(title: title, count: count, countTime: sum))
            }
            result(dataList)
        }
    }
    func statisticsMonth(_ dataType: DataType, year:Int, result: ([StatObj])-> ()) throws{
        try dbQueue?.inDatabase { db in
            var dataList = [StatObj]()
            var sqlStr = "SELECT month,SUM(duration),COUNT(local_id) FROM activity WHERE year = "+year.string+" GROUP BY month"
            if dataType != .all {
                sqlStr = "SELECT month,SUM(duration),COUNT(local_id) FROM activity WHERE year = "+year.string+" AND uploaded = "+dataType.rawValue.string+" GROUP BY month"
            }
            let rows = try Row.fetchCursor(db, sqlStr )
            while let row = try rows.next() {
                let title = row[0] as String
                let sum = row[1] as Int
                let count = row[2] as Int
                dataList.push(StatObj(title: title, count: count, countTime: sum))
            }
            result(dataList)
        }
    }
    func statisticsDay(_ dataType: DataType,  year:Int, month:Int, result: ([StatObj])-> ()) throws{
        try dbQueue?.inDatabase { db in
            var dataList = [StatObj]()
            var sqlStr = "SELECT day,SUM(duration),COUNT(local_id) FROM activity WHERE year = "+year.string+" AND month="+month.string+" GROUP BY day"
            if dataType != .all {
                sqlStr = "SELECT day,SUM(duration),COUNT(local_id) FROM activity WHERE year = "+year.string+" AND month="+month.string+" AND uploaded = "+dataType.rawValue.string+" GROUP BY day"
            }
            let rows = try Row.fetchCursor(db, sqlStr )
            while let row = try rows.next() {
                let title = row[0] as String
                let sum = row[1] as Int
                let count = row[2] as Int
                dataList.push(StatObj(title: title, count: count, countTime: sum))
            }
            result(dataList)
        }
    }
    func deleteLocal() throws{
        try self.delete(.local, year:0, month:0)
    }
    
    func delete(_ dataType: DataType, year: Int = 0, month: Int = 0 ) throws {
        try dbQueue?.inDatabase { db in
            let timeColumn = Column("beginTime")
            var queryObj = Activity.filter(timeColumn != nil)
            if dataType != .all {
                let uploadColumn = Column("uploaded")
                queryObj = queryObj.filter(uploadColumn == dataType.rawValue)
            }
            if year > 0 {
                let yearColumn = Column("year")
                queryObj = queryObj.filter(yearColumn == year)
            }
            if month > 0 {
                let monthColumn = Column("month")
                queryObj = queryObj.filter(monthColumn == month)
            }
            _ = try queryObj.deleteAll(db)
        }
    }
}
