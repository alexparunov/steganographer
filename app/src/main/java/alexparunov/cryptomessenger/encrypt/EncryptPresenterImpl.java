package alexparunov.cryptomessenger.encrypt;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import alexparunov.cryptomessenger.R;
import alexparunov.cryptomessenger.utils.Constants;

class EncryptPresenterImpl implements EncryptPresenter, EncryptInteractorImpl.EncryptInteractorListener {

  private EncryptView mView;
  private EncryptInteractor mInteractor;
  private static final int IMAGE_SIZE = 600;
  private int whichImage = -1;

  EncryptPresenterImpl(EncryptView encryptView) {
    this.mView = encryptView;
    mInteractor = new EncryptInteractorImpl((Activity) encryptView, this);
  }

  @Override
  public void selectImage(int whichImage, String tempPath) {
    mView.showProgressDialog();

    Bitmap bitmap;
    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    bitmap = BitmapFactory.decodeFile(tempPath, bitmapOptions);

    int dimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
    bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, false);

    String path = Environment.getExternalStorageDirectory() + File.separator + "CryptoMessenger" + File.separator + "CoverImage";

    File folder = new File(path);
    if (!folder.exists()) {
      if (folder.mkdirs()) {
        File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
        this.whichImage = whichImage;
        compressFile(file, scaledBitmap);
      } else {
        showParsingImageError();
      }
    } else {
      File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
      this.whichImage = whichImage;
      compressFile(file, scaledBitmap);
    }
  }

  @Override
  public void selectImageCamera(int whichImage) {
    mView.showProgressDialog();

    File file = new File(Environment.getExternalStorageDirectory().toString());
    for (File temp : file.listFiles()) {
      if (temp.getName().equals("temp.jpg")) {
        file = temp;
        break;
      }
    }

    Bitmap bitmap;
    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);
    file.delete();

    int dimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
    bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, false);

    String path = Environment.getExternalStorageDirectory() + File.separator + "CryptoMessenger" + File.separator + "CoverImage";
    File folder = new File(path);

    if (!folder.exists()) {
      if (folder.mkdirs()) {
        file = new File(path, String.valueOf(System.currentTimeMillis() + ".jped"));
        this.whichImage = whichImage;
        compressFile(file, scaledBitmap);
      } else {
        showParsingImageError();
      }
    } else {
      file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
      this.whichImage = whichImage;
      compressFile(file, scaledBitmap);
    }
  }

  private void compressFile(File file, Bitmap bitmap) {
    try {
      OutputStream outputStream;
      outputStream = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
      outputStream.flush();
      outputStream.close();

      if (whichImage == Constants.COVER_IMAGE) {
        mView.setCoverImage(file);
      } else if (whichImage == Constants.SECRET_IMAGE) {
        mView.setSecretImage(file);
      }
    } catch (Exception e) {
      e.printStackTrace();
      showParsingImageError();
    }
  }

  private void showParsingImageError() {
    mView.stopProgressDialog();
    mView.showToast(R.string.compress_error);
  }

  @Override
  public void encryptText() {
    mView.showProgressDialog();

    mInteractor.performSteganography(mView.getSecretMessage(),mView.getCoverImage(),null);
  }

  @Override
  public void encryptImage() {
    mView.showProgressDialog();

    mInteractor.performSteganography(null, mView.getCoverImage(), mView.getSecretImage());
  }

  @Override
  public void onPerformSteganographySuccessful() {

    mView.stopProgressDialog();
  }

  @Override
  public void onPerformSteganographyFailure() {

    mView.stopProgressDialog();
  }
}