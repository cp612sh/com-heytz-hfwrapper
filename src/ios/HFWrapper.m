
#import <Cordova/CDV.h>
#import "HFSmartLink.h"
#import "HFSmartLinkDeviceInfo.h"
#import <SystemConfiguration/CaptiveNetwork.h>
@interface HFWrapper : CDVPlugin {
    // Member variables go here.

}
@property (nonatomic, strong) NSTimer *paintingTimer;

- (void)startHFWrapper:(CDVInvokedUrlCommand*)command;
@end

@implementation HFWrapper{
HFSmartLink * smtlk;
BOOL isconnecting;
      CDVPluginResult *pluginResult;
    CDVInvokedUrlCommand *publicCommand;
    NSDictionary  *ret;

    NSString *errorMessage;
}

- (void)startHFWrapper:(CDVInvokedUrlCommand*)command
{

    // Do any additional setup after loading the view, typically from a nib.
    smtlk = [HFSmartLink shareInstence];
    smtlk.isConfigOneDevice = true;
    //smtlk.waitTimers=30;
    isconnecting = false;
    NSString * ssid=command.arguments[0];
    NSString * pwd=command.arguments[1];

    publicCommand=command;

    [smtlk startWithKey:pwd processblock:^(NSInteger process) {
       // self.progress.progress = process/18.0;
    } successBlock:^(HFSmartLinkDeviceInfo *dev) {

        ret =
        [NSDictionary dictionaryWithObjectsAndKeys:
         dev.mac, @"Mac",
         dev.ip, @"ModuleIPc",
         @"",@"Mid",
         @"",@"Info",
         @"",@"error",
         nil];

    } failBlock:^(NSString *failmsg) {
                 errorMessage=failmsg;

        } endBlock:^(NSDictionary *deviceDic) {
//            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:deviceDic];
//            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
//            isconnecting  = false;
    }];
    [self startPainting ];

}






// 定时器执行的方法
- (void)paint:(NSTimer *)paramTimer{

    NSLog(@"定时器执行的方法");


    if(errorMessage!=nil){
        [self stopPainting];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:publicCommand.callbackId];
    }
    if(ret!=nil){
         [self stopPainting];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:ret];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:publicCommand.callbackId];

        }
}

- (void) startPainting{
    //    seconds：需要调用的毫秒数
    //    target：调用方法需要发送的对象。即：发给谁
    //    userInfo：发送的参数
    //    repeats：指定定时器是否重复调用目标方
    // 定义一个NSTimer
    // 定义将调用的方法
    SEL selectorToCall = @selector(paint:);
    // 为SEL进行 方法签名
    NSMethodSignature *methodSignature =[[self class] instanceMethodSignatureForSelector:selectorToCall];
    // 初始化NSInvocation
    NSInvocation *invocation =[NSInvocation invocationWithMethodSignature:methodSignature];
    [invocation setTarget:self];
    [invocation setSelector:selectorToCall];
    self.paintingTimer = [NSTimer timerWithTimeInterval:1.0
                                             invocation:invocation
                                                repeats:YES];

    // 当需要调用时,可以把计时器添加到事件处理循环中
    [[NSRunLoop currentRunLoop] addTimer:self.paintingTimer forMode:NSDefaultRunLoopMode];
}
// 停止定时器
- (void) stopPainting{
    if (self.paintingTimer != nil){
        // 定时器调用invalidate后，就会自动执行release方法。不需要在显示的调用release方法
        [self.paintingTimer invalidate];
    }
}
@end
