package com.winzhibin.alipaydemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alipay.sdk.app.PayTask
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.net.URL


class MainActivity : AppCompatActivity() {

    //懒加载初始化view
    val btnPay by lazy {
        btn_pay
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPay.onClick {
            doAsync {


                /**
                 *  //----放在服务端代码(仅供参考)---start----------------------------------------
                    //在真实 App 中，私钥（如 RSA2_PRIVATE 等）数据严禁放在客户端，同时加签过程务必要放在服务端完成！！！！
                    //下面是生成加签过程的代码，先拷贝orderSingn包到服务端，可供服务端参考。
                    val RSA2_PRIVATE = "fgfgfdgdfgdfgdfgdfg87df7g897df89g798798789739843h"
                    val APPID = "fghfd3456refddffdf34=755"
                    val params: Map<String, String> =
                    OrderInfoUtil.buildOrderParamMap(APPID, true)
                    val orderParam: String = OrderInfoUtil.buildOrderParam(params)
                    //对支付参数信息进行签名
                    val sign: String =
                    OrderInfoUtil.getSign(params,RSA2_PRIVATE, true)
                    val orderInfo = orderParam + "&" + sign;
                    //----放在服务端代码(仅供参考)-------end----------------------------------------
                 *
                 */


                // 注意！！！：真实开发中 我们只要调公司接口就可以返回orderInfo了
                //真实开发中 调用商户支付接口应该是https的post请求并传参，这里为了方便演示直接调了类似服务器返回的json数据

                //第一步1.调用商户支付接口（即：公司的接口），传递参数（商品信息，用户信息，支付信息等），
                val result= URL("http://192.168.254.159:8080/yanhuomatou2015Pay.json").readText()
                  Log.e("服务器返回=",result)
                //第二步2.解析服务器返回来的"支付串码" （调用第三方支付SDK需要的核心参数）
                val json=JSONObject(result)
                val orderInfo=json.optString("orderInfo")  //支付的核心参数
                if(checkAliPayInstalled(applicationContext)){
                    sendRequest(orderInfo)
                }else{
                    runOnUiThread { toast("请先安装支付宝客户端！") }

                }

            }

        }

    }

    private fun sendRequest(orderInfo:String){
       // 第三步3.调用第三方支付SDK的支付方法（例如支付宝支付）
        val payTask=PayTask(this)
        val result=payTask.payV2(orderInfo,true)//参数2代表是否显示加载进度的长条
        //第四步4.处理支付结果：成功、失败、取消
        runOnUiThread{
             // 使用支付宝提供的工具类解析支付结果
            val payResult=PayResult(result)
            /**
             *注意：
             *下面代码是同步通知，仅作为支付结束通知用户。对于支付结果，请商户依赖服务端的异步通知结果。。
             */
            val resultInfo = payResult.result // 同步返回需要验证的信息
            val resultStatus = payResult.resultStatus// 同步返回支付结果的状态码
            // 判断resultStatus 为9000则代表支付成功，更多状态码参考支付宝接口文档
            if (TextUtils.equals(resultStatus, "9000")) { // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                         toast("支付成功")
                    } else if(TextUtils.equals(resultStatus, "6001")) {
                         toast("支付取消")
                    }else if(TextUtils.equals(resultStatus, "8000")){
                         toast("支付结果确认中")
                    }else{
                         toast("支付失败")
                    }

        }


    }

    /**
     * 检测是否安装支付宝
     * @param context
     * @return
     */
   private fun checkAliPayInstalled(context: Context): Boolean {
        val uri: Uri = Uri.parse("alipays://platformapi/startApp")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val componentName = intent.resolveActivity(context.getPackageManager())
        return componentName != null
    }
}
