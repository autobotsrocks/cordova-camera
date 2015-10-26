#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "Camera.h"

@implementation Camera

-(void) getPicture:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        if (![self checkCameraAuthorizationStatus]) {
            return;
        }

        self.callbackId = command.callbackId;
        self.allowCrop = [[command argumentAtIndex:0 withDefault:@(YES)] boolValue];

        UIImagePickerController* picker = [[UIImagePickerController alloc] init];
        picker.delegate = self;
        picker.sourceType = UIImagePickerControllerSourceTypeCamera;
        [self.viewController presentViewController:picker animated:YES completion:nil];
    }];
}

-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    [picker dismissViewControllerAnimated:YES completion:^{
        UIImage* image = [info objectForKey:UIImagePickerControllerOriginalImage];
        if (self.allowCrop) {
            PECropViewController* imageCropVC = [[PECropViewController alloc] init];
            imageCropVC.delegate = self;
            imageCropVC.image = image;
            imageCropVC.toolbarHidden = YES;
            UINavigationController* navigationController = [[UINavigationController alloc] initWithRootViewController:imageCropVC];
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
                navigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            }
            [self.viewController presentViewController:navigationController animated:YES completion:nil];
        } else {
            NSString* path = [self parseImagePath:image fileName:@"jpg"];
            CDVPluginResult* pluginResult = nil;
            if (path) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:path];
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Can't capture picture"];
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        }
    }];
}

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:^{
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"User cancelled"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }];
}

- (void)cropViewController:(PECropViewController *)picker didFinishCroppingImage:(UIImage *)croppedImage {
    [picker dismissViewControllerAnimated:YES completion:^{
        NSString* path = [self parseImagePath:croppedImage fileName:@"crop.jpg"];
        CDVPluginResult* pluginResult = nil;
        if (path) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:path];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Can't crop picture"];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }];
}

- (void)cropViewControllerDidCancel:(PECropViewController *)picker {
    [picker dismissViewControllerAnimated:YES completion:^{
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"User canceled"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }];
}

-(NSString*) parseImagePath:(UIImage*) image fileName:(NSString*) fileName {
    NSData* imageData = UIImageJPEGRepresentation(image, 1.0);
    if (!imageData) {
        return nil;
    }

    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSMutableString* filePath = [NSMutableString stringWithString: [paths objectAtIndex:0]];
    [filePath appendString:@"/"];
    [filePath appendFormat:@"%f.%@", [[NSDate date] timeIntervalSince1970], fileName];

    NSError *error = nil;
    bool written = [imageData writeToFile:filePath options:NSDataWritingAtomic error:&error];
    if (written) {
        return filePath;
    }

    return nil;
}

-(BOOL) checkCameraAuthorizationStatus {
    if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        [self showAlart:@"The camera is disable."];
        return NO;
    }

    if ([AVCaptureDevice respondsToSelector:@selector(authorizationStatusForMediaType:)]) {
        AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
        if (AVAuthorizationStatusDenied == authStatus ||
            AVAuthorizationStatusRestricted == authStatus) {
            [self showAlart:@"Access to the camera has been prohibited, please enable it in the Settings app to continue."];
            return NO;
        }
    }
    return YES;
}

-(void) showAlart:(NSString*)message {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[[UIAlertView alloc] initWithTitle:[[NSBundle mainBundle]
            objectForInfoDictionaryKey:@"CFBundleDisplayName"]
            message:NSLocalizedString(message, nil)
            delegate:self
            cancelButtonTitle:NSLocalizedString(@"OK", nil)
            otherButtonTitles:nil, nil] show];
    });
}

@end