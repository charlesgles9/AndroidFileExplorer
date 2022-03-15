package com.file.manager.helpers;

import android.content.Context;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.security.keystore.KeyProperties.*;

public class AuthenticationHelper  {

    private KeyStore keyStore;
    private Cipher cipher;
    private String KEY="FileEcp0_32";
    public AuthenticationHelper(){


    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void biometricAuthentication(Context context){
        BiometricPrompt builder= new BiometricPrompt.Builder(context)
                .setTitle("Biometric Login")
                .setSubtitle("Login using your biometric credentials")
                .setDeviceCredentialAllowed(true)
                .setConfirmationRequired(false).build();

    }

    public boolean isFingerPrintEnrolled(Context context){
        FingerprintManager fingerprintManager=(FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        return fingerprintManager!=null&&fingerprintManager.isHardwareDetected()&fingerprintManager.hasEnrolledFingerprints();
    }

    public void requestAuthentication(final Context context, final OnAuthSuccess onAuthSuccess){
        FingerprintManager fingerprintManager=(FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        if(fingerprintManager!=null&&fingerprintManager.isHardwareDetected()&fingerprintManager.hasEnrolledFingerprints()
                &PermissionsHelper.getInstance().isFingerPrintPermissionGranted()){
            generateKey();
            if(cipherInit()){
                FingerprintManager.CryptoObject cryptoObject= new FingerprintManager.CryptoObject(cipher);
                FingerprintManager.AuthenticationCallback callback= new FingerprintManager.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        onAuthSuccess.onError("An Error Occurred!");
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        onAuthSuccess.onSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        onAuthSuccess.onFailed("Invalid credentials!");
                    }
                };

                fingerprintManager.authenticate(cryptoObject,new CancellationSignal(),FingerprintManager.FINGERPRINT_ACQUIRED_GOOD,callback,null);

            }
         }
        }


  private void generateKey(){
      try {
           keyStore=KeyStore.getInstance("AndroidKeyStore");
      } catch (KeyStoreException e) {
          e.printStackTrace();
      }
      KeyGenerator keyGenerator = null;
      try {
          keyGenerator= KeyGenerator.getInstance(KEY_ALGORITHM_AES);
      } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
      }
      try {
      keyStore.load(null);
      keyGenerator.init(new KeyGenParameterSpec.Builder(KEY, PURPOSE_ENCRYPT).setBlockModes(BLOCK_MODE_CBC).setUserAuthenticationRequired(true)
      .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7).build());
      keyGenerator.generateKey();
      } catch (CertificateException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IOException |NullPointerException e) {
          e.printStackTrace();
      }
  }

  private boolean cipherInit(){
      try {
          cipher=Cipher.getInstance(KEY_ALGORITHM_AES+"/"+BLOCK_MODE_CBC+"/"+ENCRYPTION_PADDING_PKCS7);
      } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
          e.printStackTrace();
          return false;
      }
      try {
      keyStore.load(null);
      SecretKey key=(SecretKey)keyStore.getKey(KEY,null);
      cipher.init(Cipher.ENCRYPT_MODE,key);
      } catch (CertificateException | IOException | NoSuchAlgorithmException | InvalidKeyException | KeyStoreException | UnrecoverableKeyException e) {
          e.printStackTrace();
          return false;
      }
      return true;
  }

  public interface OnAuthSuccess{
        void onSuccess();
        void onFailed(String message);
        void onError(String message);
  }
}
