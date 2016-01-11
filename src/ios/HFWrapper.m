
#import <Cordova/CDV.h>
#import "HFSmartLink.h"
#import "HFSmartLinkDeviceInfo.h"
#import <SystemConfiguration/CaptiveNetwork.h>
@interface HFWrapper : CDVPlugin {
    // Member variables go here.

}
@property (nonatomic, strong) NSTimer *paintingTimer;

- (void)start:(CDVInvokedUrlCommand*)command;
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
    //smtlk.waitTimers=30;
    isconnecting = false;
    NSString * ssid=command.arguments[0];
    NSString * pwd=command.arguments[1];

    publicCommand=command;

    [smtlk startWithKey:pwd processblock:^(NSInteger process) {
       // self.progress.progress = process/18.0;
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

    } failBlock:^(NSString *failmsg) {
                 errorMessage=failmsg;

        } endBlock:^(NSDictionary *deviceDic) {
         if(errorMessage!=nil){
                 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
                 [self.commandDelegate sendPluginResult:pluginResult callbackId:publicCommand.callbackId];
         }
         if(ret!=nil){
                 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:ret];
                 [self.commandDelegate sendPluginResult:pluginResult callbackId:publicCommand.callbackId];

          }
          isconnecting  = false;

//            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:deviceDic];
//            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
//            isconnecting  = false;
    }];


}
@end
