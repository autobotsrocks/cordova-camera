var Camera = function() {
};

Camera.prototype.getPicture = function(success, fail, allowCrop) {
  allowCrop = allowCrop || true;
  return cordova.exec(success, fail, 'Camera', 'getPicture', [allowCrop]);
};

module.exports = new Camera();