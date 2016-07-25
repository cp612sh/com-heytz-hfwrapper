
#import <Cordova/CDV.h>
#import "HFSmartLink.h"
#import "HFSmartLinkDeviceInfo.h"
#import <SystemConfiguration/CaptiveNetwork.h>
@interface HFWrapper : CDVPlugin {
    // Member variables go here.

}
@property (nonatomic, strong) NSTimer *paintingTimer;

- (void)start:(CDVInvokedUrlCommand*)command;
- (void)deallocate:(CDVInvokedUrlCommand*)command;
@end

@implementation HFWrapper{
HFSmartLink * smtlk;
BOOL isconnecting;
      CDVPluginResult *pluginResult;
    CDVInvokedUrlCommand *publicCommand;
    //NSDictionary  *ret;
    NSString *ret;
    NSString *errorMessage;
}

- (void)start:(CDVInvokedUrlCommand*)command
{

    // Do any additional setup after loading the view, typically from a nib.
    smtlk = [HFSmartLink shareInstence];
    smtlk.isConfigOneDevice = true;
    smtlk.waitTimers=30;
    isconnecting = false;
    NSString * ssid=command.arguments[4];
    NSString * pwd=command.arguments[5];

    publicCommand=command;

    [smtlk startWithKey:pwd processblock:^(NSInteger process) {
    } successBlock:^(HFSmartLinkDeviceInfo *dev) {

        //ret =
        //[NSDictionary dictionaryWithObjectsAndKeys:
         //dev.mac, @"Mac",
         //dev.ip, @"ModuleIPc",
         //@"",@"Mid",
         //@"",@"Info",
         //@"",@"error",
         //nil];
        ret =dev.mac;
        if(ret!=nil){
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:ret];
            [smtlk closeWithBlock:nil];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:publicCommand.callbackId];
        }

    } failBlock:^(NSString *failmsg) {
            errorMessage=failmsg;
        
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
        [smtlk closeWithBlock:nil];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:publicCommand.callbackId];
        
        } endBlock:^(NSDictionary *deviceDic) {
        
        isconnecting  = false;
        errorMessage=nil;
        ret=nil;
    }];


}
-(void)deallocate:(CDVInvokedUrlCommand*)command{
    [smtlk closeWithBlock:nil];
}
@end
