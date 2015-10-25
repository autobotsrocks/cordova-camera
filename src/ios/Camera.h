#import <Cordova/CDV.h>
#import "PECropViewController.h"

@interface Camera : CDVPlugin<UIImagePickerControllerDelegate, UINavigationControllerDelegate, PECropViewControllerDelegate> {}

@property (assign) BOOL allowCrop;
@property (copy) NSString* callbackId;

- (void)getPicture: (CDVInvokedUrlCommand*) command;

@end