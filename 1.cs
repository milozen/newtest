// 导入System.Windows.Forms命名空间
using System.Windows.Forms;
// 导入阿里云c# sdk核心库
using aliyun.acs.core;
using aliyun.acs.core.profile;
// 导入轻量应用服务器相关模块
using aliyun.acs.simpleapplicationserver.model.v20180601;
// 导入system.net.http命名空间
using system.net.http;

namespace sample
{
    class program
    {
        static void main(string[] args)
        {
            // 创建iclientprofile实例并设置参数
            iclientprofile profile = defaultprofile.getprofile(
                "<your-region-id>", // 您的地域id
                "<your-access-key-id>", // 您的accesskey id
                "<your-access-key-secret>" // 您的accesskey secret
            );

            // 创建defaultacsclient实例并初始化
            defaultacsclient client = new defaultacsclient(profile);

            // 获取本机的公网ip地址
            httpclient httpclient = new httpclient();
            httpresponsemessage responsemessage = httpclient.getasync("https://api.ipify.org").result;
            string publicip = responsemessage.content.readasstringasync().result;

            // 创建并显示窗口实例
            window window = new window(client, publicip);
            window.showdialog();
        }
    }

    class window : form 
    {
        private defaultacsclient client; // 阿里云客户端对象 
        private string publicip; // 本机公网ip地址 
        private label label1; // 显示当前防火墙规则允许防护的ip 
        private label label2; // 显示本机互联网ip 
        private textbox textbox1; // 输入新的防火墙规则允许防护的ip 
        private button button1; // 点击修改防火墙规则 

        public window(defaultacsclient client, string publicip) 
        {
            this.client = client;
            this.publicip = publicip;

            this.text = "修改80端口防火墙规则"; // 设置窗口标题 
            this.size = new system.drawing.size(400, 200); // 设置窗口大小 
            this.startposition = formstartposition.centerparent; // 设置窗口居中显示 

            label1 = new label(); // 创建标签对象 
            label1.text = "当前80端口防火墙规则允许防护的ip: " + getfirewallrule(); // 设置标签文本为当前80端口防火墙规则允许防护的ip 
            label1.location = new system.drawing.point(10, 10); // 设置标签位置 

            label2 = new label(); // 创建标签对象 
            label2.text = "本机互联网ip: " + publicip; // 设置标签文本为本机互联网ip
            label2.location = new system.drawing.point(10, 40); // 设置标签位置 

            textbox1 = new textbox(); // 创建输入框对象 
            textbox1.text = publicip; // 设置输入框默认文本为本机公网ip 
            textbox1.location = new system.drawing.point(10, 70); // 设置输入框位置 

            button1 = new button(); // 创建按钮对象 
            button1.text = "修改防火墙规则"; // 设置按钮文本 
            button1.location = new system.drawing.point(10, 100); // 设置按钮位置 
            button1.click += new eventhandler(button1_click); // 设置按钮点击事件处理方法 

            this.controls.add(label1); // 将标签添加到窗口中 
            this.controls.add(label2); // 将标签添加到窗口中 
            this.controls.add(textbox1); // 将输入框添加到窗口中 
            this.controls.add(button1); // 将按钮添加到窗口中
        }

        private string getfirewallrule() 
        {
            try
            {
                // 创建describefirewallrulesrequest实例并设置参数
                describefirewallrulesrequest request = new describefirewallrulesrequest();
                request.setprotocol(aliyun.acs.core.protocol.type.https);
                request.setacceptformat(formattype.json);
                request.setregionid("<your-region-id>"); // 您的地域id
                request.setinstanceid("<your-instance-id>"); // 您的轻量应用服务器实例id

                // 调用describeFirewallRules方法发起请求，并得到返回结果
                describefirewallrulesresponse response = client.getaresponse(request);

                if (response != null && response.firewallrules != null && response.firewallrules.count > 0)
                {
                    foreach (var rule in response.firewallrules)
                    {
                        if (rule.port == "80") return rule.ip; // 如果找到80端口的防火墙规则，返回允许防护的ip
                    }
                    return "无"; // 如果没有找到80端口的防火墙规则，返回无
                }
                else
                {
                    return "无"; // 如果没有获取到任何防火墙规则，返回无
                }
                
            }
            catch (exception ex)
            {
                return "错误: " + ex.message; // 如果发生异常，返回错误信息
            }
        }

        private void button1_click(object sender, eventargs e) 
        {
            try
            {
                // 获取输入框中的ip地址
                string ip = textbox1.text;

                // 创建deletefirewallrulesrequest实例并设置参数
                deletefirewallrulesrequest request1 = new deletefirewallrulesrequest();
                request1.setprotocol(aliyun.acs.core.protocol.type.https);
                request1.setacceptformat(formattype.json);
                request1.setregionid("<your-region-id>"); // 您的地域id
                request1.setinstanceid("<your-instance-id>"); // 您的轻量应用服务器实例id

                // 调用deleteFirewallRules方法发起请求，并得到返回结果
                deletefirewallrulesresponse response1 = client.getaresponse(request1);

                if (response1 != null && response1.requestid != null)
                {
                    // 创建createfirewallrulesrequest实例并设置参数
                    createfirewallrulesrequest request2 = new createfirewallrulesrequest();
                    request2.setprotocol(aliyun.acs.core.protocol.type.https);
                    request2.setacceptformat(formattype.json);
                    request2.setregionid("<your-region-id>"); // 您的地域id
                    request2.setinstanceid("<your-instance-id>"); // 您的轻量应用服务器实例id

                    // 创建一个防火墙规则对象，并设置端口和ip属性
                    createfirewallrulesrequest.firewallrule firewallrule = new createfirewallrulesrequest.firewallrule();
                    firewallrule.port = "80";
                    firewallrule.ip = ip;

                    // 将防火墙规则对象添加到请求中
                    request2.addfirewallrule(firewallrule);

                    // 调用createFirewallRules方法发起请求，并得到返回结果
                    createfirewallrulesresponse response2 = client.getaresponse(request2);

                    if (response2 != null && response2.requestid != null)
                    {
                        label1.text = "当前80端口防火墙规则允许防护的ip: " + ip; // 修改标签文本为新的防火墙规则允许防护的ip 
                        messagebox.show("修改成功！"); // 弹出提示框显示修改成功
                    }
                    else
                    {
                        messagebox.show("修改失败！"); // 弹出提示框显示修改失败
                    }
                }
                else
                {
                    messagebox.show("修改失败！"); // 弹出提示框显示修改失败
                }
            }
            catch (exception ex)
            {
                messagebox.show("错误: " + ex.message); // 弹出提示框显示错误信息
            }
        }
    }
}
