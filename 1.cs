// 导入阿里云C# SDK核心库
using Aliyun.Acs.Core;
using Aliyun.Acs.Core.Profile;
// 导入轻量应用服务器相关模块
using Aliyun.Acs.SimpleApplicationServer.Model.V20180601;
// 导入System.Net.Http命名空间
using System.Net.Http;

namespace Sample
{
    class Program
    {
        static void Main(string[] args)
        {
            // 创建IClientProfile实例并设置参数
            IClientProfile profile = DefaultProfile.GetProfile(
                "<your-region-id>", // 您的地域ID
                "<your-access-key-id>", // 您的AccessKey ID
                "<your-access-key-secret>" // 您的AccessKey Secret
            );

            // 创建DefaultAcsClient实例并初始化
            DefaultAcsClient client = new DefaultAcsClient(profile);

            // 获取本机的公网IP地址
            HttpClient httpClient = new HttpClient();
            HttpResponseMessage responseMessage = httpClient.GetAsync("https://api.ipify.org").Result;
            string myIp = responseMessage.Content.ReadAsStringAsync().Result; // 保存本机IP地址

            // 创建ListFirewallRulesRequest实例并设置参数 
            ListFirewallRulesRequest request = new ListFirewallRulesRequest();
            request.FirewallId = "<your-firewall-id>"; // 设置防火墙ID

            // 发起请求并处理返回结果 
            ListFirewallRulesResponse response = client.GetAcsResponse(request);
            
             遍历防火墙规则列表，找到端口80的规则，并取出允许访问的IP地址和规则ID，如果与本机IP不同，则删除该条规则，并添加新规则允许本机IP访问80端口。
             foreach (var rule in response.FirewallRules)
             {
                 if (rule.Port == "80")  判断端口是否为80 
                 {
                     string ipAddress = rule.Ip;  取出IP地址 
                     string ruleId = rule.RuleId;  取出规则ID 
                     if (ipAddress != myIp)  判断IP地址是否与本机IP不同 
                     {
                         Console.WriteLine("Deleting firewall rule for IP address: " + ipAddress);  打印删除信息
                        
                         // 创建DeleteFirewallRuleRequest实例并设置参数 
                         DeleteFirewallRuleRequest requestDelete = new DeleteFirewallRuleRequest();
                         requestDelete.FirewallId = "<your-firewall-id>";  设置防火墙ID 
                         requestDelete.RuleId = ruleId;  设置要删除的规则ID 

                         // 发起请求并处理返回结果 
                         DeleteFirewallRuleResponse responseDelete = client.GetAcsResponse(requestDelete);
                         Console.WriteLine(responseDelete.RequestId);  打印返回结果中的RequestId
                        
                         Console.WriteLine("Creating firewall rule for IP address: " + myIp);  打印创建信息
                        
                        // 创建CreateFirewallRuleRequest实例并设置参数 
                        CreateFirewallRuleRequest requestCreate = new CreateFirewallRuleRequest();
                        requestCreate.FirewallId = "<your-firewall-id>";  设置防火墙ID 
                        requestCreate.ProtocolType = "TCP";  设置协议类型为TCP 
                        requestCreate.PortRangeFrom= "80";   设置端口范围起始值为80  
                        requestCreate.PortRangeTo= "80";   设置端口范围结束值为80  
                        requestCreate.IpAddress= myIp;   设置授权对象为本机IP 

                        发起请求并处理返回结果 
                        CreateFirewallRuleResponse responseCreate= client.GetAcsResponse(requestCreate);
                        Console.WriteLine(responseCreate.RequestId);   打印返回结果中的RequestId

                     }
                 }
             }

        }
    }
}
