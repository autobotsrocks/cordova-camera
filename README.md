# cordova-camera
A cordova plugin that provides the functionality to capture & crop picture from camera for android and ios.

# Installing the plugin

```shell
cordova plugin add https://github.com/autobotsrocks/cordova-camera
```

# Usage

```javascript
window.autobots.camera.getPicture(
  function(filePath) {
    alert(filePath);
  },
  function(error) {
    alert(error);
  },
  true // Whether allow crop picutre, default value is true
);
```

# Dependencies

iOS: [PEPhotoCropEditor](https://github.com/kishikawakatsumi/PEPhotoCropEditor)

# License

MIT
