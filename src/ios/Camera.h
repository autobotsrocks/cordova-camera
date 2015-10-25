#import <Cordova/CDV.h>

@interface Camera : CDVPlugin<UIImagePickerControllerDelegate, UINavigationControllerDelegate> {}

@property (copy) NSString* callbackId;

- (void)getPicture: (CDVInvokedUrlCommand*) command;

@end