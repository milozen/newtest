const express = require('express')
const router = express.Router()
const mongoose = require('mongoose')
const PayRecord = require('../../models/pay_record')
const TenPay = require('../../pay/tenPay')
const logger = require('../../utils/logger-app')
const security = require('../../utils/security')
const VipRecord = require('../../models/vip_history')
const User = mongoose.model('User')
const iosPay = require('../../pay/iosPay')
const moment = require('moment')


const PRODUCT_LIST =  [
  {
    id: '1month',
    name: '一个月会员',
    desc: '',
    unit: '月',
    price: 101
  },
  {
    id: '1year',
    name: '一年会员',
    desc: '',
    unit: '年',
    price: 102
  },
  {
    id: 'month_auto',
    name: '自动续费',
    desc: '每月自动续费',
    unit: '月',
    price: 1000
  },
]
const PRODUCT_LIST_ANDROID =  [
  {
    id: '1month',
    name: '一个月会员',
    desc: '一个月会员',
    unit: '月',
    price: 1
  },
  {
    id: '1year',
    name: '一年会员',
    desc: '一年会员',
    unit: '年',
    price: 2
  }
]
module.exports = function (app) {
  app.use('/', router);
};
const updateVip = async function (record) {
  const vipRecord = new VipRecord({
    userid: record.userid,
    type: 1,
    source: 'wxpay',
    pay_record: record.id
  })
  const user = await User.findById(record.userid)
  user.vip = user.vip !== 9 ? 1 : 9
  const now = Date.now()
  if (!user.vip_exp_time) {user.vip_exp_time = now}
  let expTime = user.vip_exp_time < now ? now : user.vip_exp_time
  vipRecord.time_start = expTime
  let days = record.type === 3 ? 365 : 31;
  expTime += (days * 24 * 60 * 60 * 1000)
  vipRecord.time_end = expTime
  user.vip_exp_time = expTime
  await user.save()
  await vipRecord.save();
}
const payConfig = {
    appid: 'wx5846c59e6f1304a9',
    mchid: '1614198199',
    partnerKey: 'c62079e01cf2ca690be4be182e923df7',
    // pfx: fs.readFileSync('./config/apiclient_cert.p12'), // 微信商户平台证书
    spbill_create_ip: '127.0.0.1',
    notify_url: 'https://app.51zhanzhuang.com/pay/wx/cb',
  };
const wxpay = new TenPay(payConfig, false);
// const wxpay = TenPay.sandbox(payConfig, true);

router.post('/pay/preorder', security.keeper, async function (req,res, next) {
  const body = req.body;
  const pid = body.pid;
  let money = body.money;
  if (!pid) {
    return res.json({success: false, errmsg: 'product id not specify'});
  }
  const data = PRODUCT_LIST_ANDROID.find(o=> o.id === pid);
  if (!data) {
    return res.json({success: false, errmsg: 'product data not found'});
  }
  const user = req.user;
  const showName = data.name;
  money = money || data.price;
  let type = 1;
  if (data.id === 'month_auto') {
    type = 2
  } else if (data.id === '1year') {
    type = 3
  }

  try {
    const record = new PayRecord({
      userid: user.id,
      money: money || data.price,
      product_name: showName,
      type,
    });
    await record.save();
    const result = await wxpay.getAppParams({
      body: showName,
      out_trade_no: record.id,
      total_fee: money
    });
    res.json({
      success: true,
      data: result,
      pname: showName,
      pmoney: money,
      tid: record.id
    });
  } catch (err) {
    console.log(err);
    next(err);
  }
});

router.post('/pay/wx/cb', wxpay.middleware('pay'), async function(req, res, next) {
  logger.info('[pay] receive wx pay result');
  const params = req.payData;
  try {
    const tid = params.out_trade_no;
    const record = await PayRecord.findById(tid);
    if (!record) {
      return res.reply('签名失败');
    }

    if (params.sign !== wxpay._getSign(params, params.sign_type )) {
      return res.reply('签名失败');
    }
    delete params['sign'];
    record.query_msg = params;
    record.status = 1;
    await updateVip(record);
    await record.save();
    res.reply();
  } catch (err) {
    res.reply('签名失败');
  }
});

// 微信支付-查询订单
router.post('/pay/wx/query_order', async function (req, res, next) {
  const tid = req.body.tid;
  try {
    const record = await PayRecord.findById(tid);
    if (!record) {
      return res.json({success: false, errmsg: 'pay history not found'});
    }
    if (record.status === 1) {
      let repData = {
        product_name: record.product_name,
        tid: record.id,
        money: record.money,
        trade_time: moment(record.updatedAt).format('YYYY-MM-DD HH:mm:ss')
      };

      return res.json({success: true, result: 1, data: repData});
    }

    const order = await wxpay.orderQuery({out_trade_no: record.id});
    logger.info(order);
    if (order.return_code === 'SUCCESS' && order.return_msg === 'OK' && order.result_code === 'SUCCESS') {
      record.status = 1;
    }
    await updateVip(record);
    await record.save();
    let repData = {
        product_name: record.product_name,
        tid: record.id,
        money: record.money,
        trade_time: moment(record.updatedAt).format('YYYY-MM-DD HH:mm:ss')
      };
    if (record.status === 1) {
      res.json({success: true, result: 1, data: repData});
    } else {
      res.json({success: false, result: 0, data: repData});
    }
  } catch (err) {
    logger.error(err);
    next(err);
  }
});
// iOS内购买验证订单
router.post('/pay/ios/verify_order', security.keeper, async function (req, res, next) {
  const {orderid, token} = req.body
  const user = req.user;
  try {
    let datas = await iosPay.iosPayVerify(token, orderid)
    // console.log(data)
    for (let data of datas) {
      if (data.inAppOwnershipType === 'PURCHASED' && !data.isTrial) {
        let exists = await PayRecord.find({
          userid: user.id,
          out_id: data.transactionId,
          product_name: data.productId
        }).limit(1)
        if (exists && exists.length > 0) {
          continue;
        }
        let type = 1;
        if (data.productId === 'month_auto') {
          type = 2
        } else if (data.productId === '1year') {
          type = 3
        }
        const record = new PayRecord({
          userid: user.id,
          product_name: data.productId,
          out_id: data.transactionId,
          status: 1,
          type,
          query_msg: data,
          channel: 'ios'
        });
        await record.save();
        await updateVip(record);
      }
    }
    res.json({success: true, result: 1, datas})
  } catch(err) {
    next(err)
  }
})

router.get('/pay/ios/list', async function(req, res, next){
  let records = PRODUCT_LIST
  res.json({success: true, records})
})
router.get('/pay/android/list', async function(req, res, next){
  let records = PRODUCT_LIST_ANDROID
  res.json({success: true, records})
})

//增加用户看广告后，赠送1天VIP时间
