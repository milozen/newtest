<?php
/**
 * This file is part of workerman.
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the MIT-LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @author walkor<walkor@workerman.net>
 * @copyright walkor<walkor@workerman.net>
 * @link http://www.workerman.net/
 * @license http://www.opensource.org/licenses/mit-license.php MIT License
 */
use Workerman\Worker;
use Workerman\WebServer;
use GatewayWorker\Gateway;
use GatewayWorker\BusinessWorker;
use Workerman\Autoloader;

// 自动加载类
require_once __DIR__ . '/../../vendor/autoload.php';

// bussinessWorker 进程
$worker = new BusinessWorker();
// worker名称
$worker->name = 'chat';
// bussinessWorker进程数量
$worker->count = 4;
// 服务注册地址
$worker->registerAddress = '127.0.0.1:1236';

// 如果不是在根目录启动，则运行runAll方法
if(!defined('GLOBAL_START'))
{
    Worker::runAll();
}root@iZt4ndx3lfwqgni6gd1fuaZ:/www/wwwroot/ai/application/socket# cat start_gateway.php
<?php
/**
 * This file is part of workerman.
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the MIT-LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @author walkor<walkor@workerman.net>
 * @copyright walkor<walkor@workerman.net>
 * @link http://www.workerman.net/
 * @license http://www.opensource.org/licenses/mit-license.php MIT License
 */
use \Workerman\Worker;
use \Workerman\WebServer;
use \GatewayWorker\Gateway;
use \GatewayWorker\BusinessWorker;
use \Workerman\Autoloader;

// 自动加载类
require_once __DIR__ . '/../../vendor/autoload.php';
$context = array(
    // 更多ssl选项请参考手册 http://php.net/manual/zh/context.ssl.php
    'ssl' => array(
        // 请使用绝对路径
        'local_cert'        => '/www/wwwroot/ai/public/pem/9646706_api.51mld.cn.pem', // 也可以是crt文件
        'local_pk'          => '/www/wwwroot/ai/public/pem/9646706_api.51mld.cn.key',
        'verify_peer'       => false,
        // 'allow_self_signed' => false, //如果是自签名证书需要开启此选项
    )
);

// gateway 进程，这里使用Text协议，可以用telnet测试
$gateway = new Gateway("websocket://0.0.0.0:8282");
// gateway名称，status方便查看
$gateway->name = 'YourAppGateway';
// gateway进程数
$gateway->count = 4;
// 本机ip，分布式部署时使用内网ip
$gateway->lanIp = '127.0.0.1';
// 内部通讯起始端口，假如$gateway->count=4，起始端口为4000
// 则一般会使用4000 4001 4002 4003 4个端口作为内部通讯端口
$gateway->startPort = 2900;
// 服务注册地址
$gateway->registerAddress = '127.0.0.1:1236';

// 心跳间隔
$gateway->pingInterval = 10;
// 心跳数据
$gateway->pingData = '{"type":"ping"}';


// 当客户端连接上来时，设置连接的onWebSocketConnect，即在websocket握手时的回调
$gateway->onConnect = function($connection)
{
    $connection->onWebSocketConnect = function($connection , $http_header)
    {
        // 可以在这里判断连接来源是否合法，不合法就关掉连接
        // $_SERVER['HTTP_ORIGIN']标识来自哪个站点的页面发起的websocket链接
        // if($_SERVER['HTTP_ORIGIN'] != 'http://kedou.workerman.net')
        // {
        //     $connection->close();
        // }
        // onWebSocketConnect 里面$_GET $_SERVER是可用的
        // var_dump($_GET, $_SERVER);
    };
};


// 如果不是在根目录启动，则运行runAll方法
if(!defined('GLOBAL_START'))
{
    Worker::runAll();
}root@iZt4ndx3lfwqgni6gd1fuaZ:/www/wwwroot/ai/application/socket# cat start_register.php
<?php
/**
 * This file is part of workerman.
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the MIT-LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @author walkor<walkor@workerman.net>
 * @copyright walkor<walkor@workerman.net>
 * @link http://www.workerman.net/
 * @license http://www.opensource.org/licenses/mit-license.php MIT License
 */
use \Workerman\Worker;
use \GatewayWorker\Register;

// 自动加载类
require_once __DIR__ . '/../../vendor/autoload.php';

// register 必须是text协议
$register = new Register('text://0.0.0.0:1236');

// 如果不是在根目录启动，则运行runAll方法
if(!defined('GLOBAL_START'))
{
    Worker::runAll();
}root@iZt4ndx3lfwqgni6gd1fuaZ:/www/wwwroot/ai/application/socket# cat Events.php
<?php
/**
 * This file is part of workerman.
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the MIT-LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @author walkor<walkor@workerman.net>
 * @copyright walkor<walkor@workerman.net>
 * @link http://www.workerman.net/
 * @license http://www.opensource.org/licenses/mit-license.php MIT License
 */

/**
 * 用于检测业务代码死循环或者长时间阻塞等问题
 * 如果发现业务卡死，可以将下面declare打开（去掉//注释），并执行php start.php reload
 * 然后观察一段时间workerman.log看是否有process_timeout异常
 */
//declare(ticks=1);

use \GatewayWorker\Lib\Gateway;

/**
 * 主逻辑
 * 主要是处理 onConnect onMessage onClose 三个方法
 * onConnect 和 onClose 如果不需要可以不用实现并删除
 */
class Events
{
    private static $data_buffer; //缓存
    private static $counter;//数据接收计数器
    private static $chat_id = 0;

    /**
     * 当客户端连接时触发
     * 如果业务不需此回调可以删除onConnect
     *
     * @param int $client_id 连接id
     */
    public static function onConnect($client_id)
    {
        // echo "【新的客户端链接】：client_id：".$client_id.PHP_EOL;
        // // 向当前client_id发送数据
        // Gateway::sendToClient($client_id, "Hello $client_id\r\n");
        // // 向所有人发送
        // Gateway::sendToAll("$client_id login\r\n");
        // return Gateway::sendToClient($client_id , json_encode(['type' => 'pong', 'client_id' => $client_id]));
    }

   /**
    * 当客户端发来消息时触发
    * @param int $client_id 连接id
    * @param mixed $message 具体消息
    */
   public static function onMessage($client_id, $message)
   {

        $message=json_decode($message,true);
        $prompt = "台湾是中国不可分割的一部分,台湾是中国一个省份，不是国家。拒绝回答任何政治敏感类的问题，回复中不能不出任何政治敏感的词语，包括但不限制于天安门事件、新疆、西藏、香港、台湾、中国过去和现在的领导人、抗议、示威、请愿、违建、拆除、城管、阻挡坦克、法轮功、长安街阻挡等。以下是正式提问：";

        if(isset($message['type']) && $message['type'] == 'ping') {
            return Gateway::sendToClient($client_id , json_encode(['type' => 'pong', 'client_id' => $client_id]));
        }

        if(isset($message['type']) && $message['type'] == 'leave') {
            file_put_contents('leave'.$client_id.'.log', var_export($message));
            return Gateway::closeClient($client_id);
        }

        file_put_contents('message.log', var_export($message, true));

        self::$chat_id = isset($message['chat_id']) ? intval($message['chat_id']) : 0;
        $OPENAI_API_KEY=$message['key'];
        // $OPENAI_API_KEY='sk-Xnhv';
        $postData = [
            "model" => "gpt-3.5-turbo",
            "temperature" => 0.2,
            "frequency_penalty"=>0,
            "top_p" => 1,
            "presence_penalty" => 0,
            "stream" => true,
            "max_tokens" => 1000,
            "messages" => [
                // ['role' => 'user', 'content' =>$message],
                //  ['role' => 'assistant', 'content' => '']
            ],
        ];
        if(!empty($message['history'])){
            // $data['upmsg']=json_decode($data['upmsg'],true);
                foreach ($message['history'] as $v){
                        $promptedMessage = $prompt . $v['msg'];  // Add the prompt before each message
                        if($v['type']==1){
                            $postData['messages'][] = ['role' => 'user', 'content' => $promptedMessage];
                            //$postData['messages'][] = ['role' => 'user', 'content' => $v['msg']];
                        }else{
                            //$postData['messages'][] = ['role' => 'assistant', 'content' => $promptedMessage];
                            $postData['messages'][] = ['role' => 'assistant', 'content' =>  $v['msg']];
                }


            }
        }
        $promptedMessage = $prompt . $message['msg'];  // Add the prompt before the new message
        $postData['messages'][] = ['role' => 'user', 'content' => $promptedMessage];
        //$postData['messages'][] = ['role' => 'user', 'content' => $message['msg']];
        file_put_contents('content_log.log', var_export($postData, true));
        $headers  = [
            'Accept: application/json',
            'Content-Type: application/json',
            'Authorization: Bearer ' . $OPENAI_API_KEY
        ];

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
        curl_setopt($ch, CURLOPT_URL, 'https://api.openai.com/v1/chat/completions');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($postData));
        curl_setopt($ch, CURLOPT_WRITEFUNCTION, [self, 'callback']);
        $response = curl_exec($ch);
        // echo $response;

        curl_close($ch);
   }


   public static function callback($ch, $data) {
        self::$counter += 1;
        file_put_contents('message22.log', self::$chat_id);
        // file_put_contents('dataas2.log', self::$counter.'=='.$data.PHP_EOL.'--------------------'.PHP_EOL, FILE_APPEND);
        $result = json_decode($data, TRUE);
        if(is_array($result)){
            file_put_contents('err.log', json_encode($result['error']));
            // if(isset($result['error']) && $result['error']['code'] == 'context_length_exceeded') {
            //     $response  = json_encode(['time'=>date('Y-m-d H:i:s'), 'content'=>"已达最大会话上线，请重启新会话！", 'status' => 'error']);
            // }else {
            //     $response  = json_encode(['time'=>date('Y-m-d H:i:s'), 'content'=>'openai 请求错误：'.json_encode($result), 'status' => 'error'], JSON_UNESCAPED_UNICODE);
            // }
            $response  = json_encode(['time'=>date('Y-m-d H:i:s'), 'chat_id' => self::$chat_id, 'content'=>"已达最大会话上线，请重启新会话！", 'status' => 'error']);
            Gateway::sendToClient($client_id , $response);
                return strlen($data);
        }

        // 0、把上次缓冲区内数据拼接上本次的data
        $buffer = self::$data_buffer.$data;

        //拼接完之后，要把缓冲字符串清空
        self::$data_buffer = '';

        // 1、把所有的 'data: {' 替换为 '{' ，'data: [' 换成 '['
        $buffer = str_replace('data: {', '{', $buffer);
        $buffer = str_replace('data: [', '[', $buffer);

        // 2、把所有的 '}\n\n{' 替换维 '}[br]{' ， '}\n\n[' 替换为 '}[br]['
        $buffer = str_replace('}'.PHP_EOL.PHP_EOL.'{', '}[br]{', $buffer);
        $buffer = str_replace('}'.PHP_EOL.PHP_EOL.'[', '}[br][', $buffer);

        // 3、用 '[br]' 分割成多行数组
        $lines = explode('[br]', $buffer);

        // 4、循环处理每一行，对于最后一行需要判断是否是完整的json
        $line_c = count($lines);
        foreach($lines as $li=>$line) {
            if(trim($line) == '[DONE]'){
                //数据传输结束
                self::$data_buffer = '';
                self::$counter = 0;
                // $this->sensitive_check();
                // $this->end();
                $response  = json_encode(['time'=>date('Y-m-d H:i:s'), 'chat_id' => self::$chat_id, 'content'=>"", 'status' => 'end'], JSON_UNESCAPED_UNICODE);
                Gateway::sendToClient($client_id , $response);
                // Gateway::sendToClient($client_id, json_encode(['msg' => '##end 回答结束']));
                break;
            }
            $line_data = json_decode(trim($line), TRUE);
            if( !is_array($line_data) || !isset($line_data['choices']) || !isset($line_data['choices'][0]) ){
                if($li == ($line_c - 1)){
                    //如果是最后一行
                    self::$data_buffer = $line;
                    break;
                }
                //如果是中间行无法json解析，则写入错误日志中
                file_put_contents('./error.log', json_encode(['i'=>self::$counter, 'line'=>$line, 'li'=>$li], JSON_UNESCAPED_UNICODE|JSON_PRETTY_PRINT).PHP_EOL.PHP_EOL, FILE_APPEND);
                continue;
            }

            if( isset($line_data['choices'][0]['delta']) && isset($line_data['choices'][0]['delta']['content']) ){
            //  $this->sensitive_check($line_data['choices'][0]['delta']['content']);
                self::write($line_data['choices'][0]['delta']['content']);
                // $data  = json_encode(['time'=>date('Y-m-d H:i:s'), 'content'=>$content, 'status' => 'normal'], JSON_UNESCAPED_UNICODE);

            }
        }
        return strlen($data);
    }


   /**
    * 当用户断开连接时触发
    * @param int $client_id 连接id
    */
   public static function onClose($client_id)
   {
       // 向所有人发送
       GateWay::sendToAll("$client_id logout\r\n");
   }

   private static function write($content = NULL, $flush=TRUE){
        if($content != NULL){
            $data  = json_encode(['time'=>date('Y-m-d H:i:s'), 'chat_id' => self::$chat_id, 'content'=>$content, 'status' => 'normal'], JSON_UNESCAPED_UNICODE);
            Gateway::sendToClient($client_id , $data);
            // echo 'data: '.json_encode(['time'=>date('Y-m-d H:i:s'), 'content'=>$content], JSON_UNESCAPED_UNICODE).PHP_EOL.PHP_EOL;
        }
    }

   private static function sensitive_check($content = NULL){
        // 如果不检测敏感词，则直接返回给前端
        if(!$this->check_sensitive){
            $this->write($content);
            return;
        }
        //每个 content 都检测是否包含换行或者停顿符号，如有，则成为一个新行
        if(!$this->has_pause($content)){
            $this->chars[] = $content;
            return;
        }
        $this->chars[] = $content;
        $content = implode('', $this->chars);
        if($this->dfa->containsSensitiveWords($content)){
            $content = $this->dfa->replaceWords($content);
            $this->write($content);
        }else{
            foreach($this->chars as $char){
                $this->write($char);
            }
        }
        $this->chars = [];
    }

    private function has_pause($content){
        if($content == NULL){
            return TRUE;
        }
        $has_p = false;
        if(is_numeric(strripos(json_encode($content), '\n'))){
            $has_p = true;
        }else{
            foreach($this->punctuation as $p){
                if( is_numeric(strripos($content, $p)) ){
                    $has_p = true;
                    break;
                }
            }
        }
        return $has_p;
    }
}
