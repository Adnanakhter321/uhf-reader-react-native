package com.uhfrfidlibrary.uhf;

import android.util.Log;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.IUHF;
import java.util.ArrayList;
import java.util.List;
import android.media.AudioManager;
import android.media.SoundPool;
import java.util.HashMap;
import android.content.Context;
import com.uhfrfidlibrary.uhf.R;

// Removed unused imports that were leftover from the demo app and don't
// belong in a native module: KeyEvent, Fragment, Activity, Bundle,
// ReactActivity, ConfigurationException, and the duplicate
// RFIDWithUHFUART import.

public class C72RfidScannerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String UHF_READER_POWER_ON_ERROR = "UHF_READER_POWER_ON_ERROR";
    private static final String UHF_READER_INIT_ERROR = "UHF_READER_INIT_ERROR";
    private static final String UHF_READER_READ_ERROR = "UHF_READER_READ_ERROR";
    private static final String UHF_READER_RELEASE_ERROR = "UHF_READER_RELEASE_ERROR";
    private static final String UHF_READER_WRITE_ERROR = "UHF_READER_WRITE_ERROR";
    private static final String UHF_READER_OTHER_ERROR = "UHF_READER_OTHER_ERROR";
    private static final String UHF_READER_NOT_READY = "UHF_READER_NOT_READY";

    public RFIDWithUHFUART mReader;

    private Boolean mReaderStatus = false;
    private List<String> scannedTags = new ArrayList<String>();
    private Boolean uhfInventoryStatus = false;
    private SoundPool soundPool;
    private AudioManager am;
    private HashMap<Integer, Integer> soundMap = new HashMap<>();
    private float volumnRatio;

    private final ReactApplicationContext reactContext;

    public C72RfidScannerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.reactContext.addLifecycleEventListener(this);
        initSound(reactContext);
    }

    @Override
    public String getName() {
        return "C72RfidScanner";
    }

    @ReactMethod
    public void playSound(int id) {
        if (soundPool == null || am == null) return;
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumnRatio = audioMaxVolume > 0 ? audioCurrentVolume / audioMaxVolume : 1f;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, volumnRatio, 1, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void releaseSoundPool() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private void initSound(Context context) {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(context, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(context, R.raw.serror, 1));
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @ReactMethod
    public void initializeUHF(Promise promise) {
        try {
            mReader = RFIDWithUHFUART.getInstance();
            mReaderStatus = mReader.init();
            initSound(reactContext);
            promise.resolve("UHF Initialized");
        } catch (Exception e) {
            promise.reject(UHF_READER_INIT_ERROR, e.getMessage());
        }
    }

    @ReactMethod
    public void deinitializeUHF(Promise promise) {
        if (mReader == null) {
            promise.reject(UHF_READER_NOT_READY, "Reader not initialized");
            return;
        }
        try {
            mReader.free();
            promise.resolve("UHF Deinitialized");
        } catch (Exception e) {
            promise.reject(UHF_READER_RELEASE_ERROR, e.getMessage());
        }
    }

    @ReactMethod
    public void readPower(final Promise promise) {
        if (mReader == null) {
            promise.reject(UHF_READER_NOT_READY, "Reader not initialized");
            return;
        }
        try {
            int uhfPower = mReader.getPower();
            if (uhfPower >= 0) {
                promise.resolve(uhfPower);
            } else {
                promise.reject(UHF_READER_OTHER_ERROR, "INVALID POWER VALUE");
            }
            Log.d("UHF_SCANNER", String.valueOf(uhfPower));
        } catch (Exception ex) {
            Log.d("UHF_SCANNER", String.valueOf(ex.getLocalizedMessage()));
            promise.reject(UHF_READER_OTHER_ERROR, ex.getLocalizedMessage());
        }
    }

    @ReactMethod
    public void changePower(int powerValue, final Promise promise) {
        if (mReader == null) {
            promise.reject(UHF_READER_NOT_READY, "Reader not initialized");
            return;
        }
        try {
            Boolean uhfPowerState = mReader.setPower(powerValue);
            if (uhfPowerState) promise.resolve(uhfPowerState);
            else promise.reject(UHF_READER_OTHER_ERROR, "Can't Change Power");
        } catch (Exception ex) {
            promise.reject(UHF_READER_OTHER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Override
    public void onHostDestroy() {
        new UhfReaderPower(false).start();
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    private void sendEvent(String eventName, @Nullable WritableArray array) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, array);
    }

    private void sendEvent(String eventName, @Nullable String status) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, status);
    }

    @ReactMethod
    private void initializeReader() {
        Log.d("UHF Reader", "Initializing Reader");
        new UhfReaderPower().start();
    }

    @ReactMethod
    public void deInitializeReader() {
        Log.d("UHF Reader", "DeInitializing Reader");
        new UhfReaderPower(false).start();
    }

    public static WritableArray convertArrayToWritableArray(String[] tag) {
        WritableArray array = new WritableNativeArray();
        for (String tagId : tag) {
            array.pushString(tagId);
        }
        return array;
    }

    @ReactMethod
    public void readSingleTag(final Promise promise) {
        if (mReader == null) {
            promise.reject(UHF_READER_NOT_READY, "Reader not initialized");
            return;
        }
        try {
            UHFTAGInfo tag = mReader.inventorySingleTag();
            if (tag != null && tag.getEPC() != null && !tag.getEPC().isEmpty()) {
                String[] tagData = {tag.getEPC(), tag.getRssi()};
                promise.resolve(convertArrayToWritableArray(tagData));
            } else {
                promise.reject(UHF_READER_READ_ERROR, "No tags found nearby.");
            }
        } catch (Exception ex) {
            promise.reject(UHF_READER_READ_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void startReadingTags(final Callback callback) {
        if (mReader == null) {
            callback.invoke(false);
            return;
        }
        uhfInventoryStatus = mReader.startInventoryTag();
        new TagThread().start();
        callback.invoke(uhfInventoryStatus);
    }

    @ReactMethod
    public void stopReadingTags(final Callback callback) {
        if (mReader == null) {
            callback.invoke(scannedTags.size());
            return;
        }
        uhfInventoryStatus = !(mReader.stopInventory());
        callback.invoke(scannedTags.size());
    }

    @ReactMethod
    public void writeDataIntoEpc(String epc, final Promise promise) {
        if (mReader == null) {
            promise.reject(UHF_READER_NOT_READY, "Reader not initialized");
            return;
        }
        if (epc.length() == (6 * 4)) {
            epc += "00000000";
            try {
                Boolean uhfWriteState = mReader.writeData("00000000", IUHF.Bank_EPC, 2, 6, epc);
                if (uhfWriteState) promise.resolve(uhfWriteState);
                else promise.reject(UHF_READER_WRITE_ERROR, "Can't Write Data");
            } catch (Exception ex) {
                promise.reject(UHF_READER_WRITE_ERROR, ex.getMessage());
            }
        } else {
            promise.reject(UHF_READER_WRITE_ERROR, "Invalid Data");
        }
    }

    @ReactMethod
    public void clearAllTags() {
        scannedTags.clear();
    }

    // Required so `new NativeEventEmitter(C72RfidScanner)` works without warnings
    // on BOTH architectures. Old Arch's bridge and New Arch's Interop Layer both
    // call these on the native module when JS adds/removes a listener — without
    // them RN logs "`new NativeEventEmitter()` was called with a non-null argument
    // without the required `addListener`/`removeListeners` methods" and, on some
    // New Arch builds, listener registration silently no-ops.
    @ReactMethod
    public void addListener(String eventName) {
        // Keep: required for RN's built-in event emitter, no native listener setup needed here.
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Keep: required for RN's built-in event emitter, no native listener teardown needed here.
    }

    class UhfReaderPower extends Thread {
        Boolean powerOn;

        public UhfReaderPower() {
            this.powerOn = true;
        }

        public UhfReaderPower(Boolean powerOn) {
            this.powerOn = powerOn;
        }

        public void powerOn() {
            if (mReader == null || !mReaderStatus) {
                try {
                    mReader = RFIDWithUHFUART.getInstance();
                    try {
                        mReaderStatus = mReader.init();
                        mReader.setEPCAndTIDMode();
                        sendEvent("UHF_POWER", "success: power on");
                    } catch (Exception ex) {
                        sendEvent("UHF_POWER", "failed: init error");
                    }
                } catch (Exception ex) {
                    sendEvent("UHF_POWER", "failed: power on error");
                }
            }
        }

        public void powerOff() {
            if (mReader != null) {
                try {
                    mReader.free();
                    mReader = null;
                    sendEvent("UHF_POWER", "success: power off");
                } catch (Exception ex) {
                    sendEvent("UHF_POWER", "failed: " + ex.getMessage());
                }
            }
        }

        public void run() {
            if (powerOn) {
                powerOn();
            } else {
                powerOff();
            }
        }
    }

    @ReactMethod
    public void findTag(final String findEpc, final Callback callback) {
        if (mReader == null) {
            callback.invoke(false);
            return;
        }
        uhfInventoryStatus = mReader.startInventoryTag();
        new TagThread(findEpc).start();
        callback.invoke(uhfInventoryStatus);
    }

    class TagThread extends Thread {

        String findEpc;

        public TagThread() {
            findEpc = "";
        }

        public TagThread(String findEpc) {
            this.findEpc = findEpc;
        }

        public void run() {
            UHFTAGInfo res;
            while (uhfInventoryStatus && mReader != null) {
                res = mReader.readTagFromBuffer();
                if (res != null) {
                    if ("".equals(findEpc))
                        addIfNotExists(res);
                    else
                        lostTagOnly(res);
                }
            }
        }

        public void lostTagOnly(UHFTAGInfo tag) {
            String epc = tag.getEPC();
            if (epc != null && epc.equals(findEpc)) {
                String[] tagData = {tag.getEPC(), tag.getRssi()};
                sendEvent("UHF_TAG", C72RfidScannerModule.convertArrayToWritableArray(tagData));
            }
        }

        public void addIfNotExists(UHFTAGInfo tid) {
            if (!scannedTags.contains(tid.getEPC())) {
                scannedTags.add(tid.getEPC());
                String[] tagData = {tid.getEPC(), tid.getRssi()};
                sendEvent("UHF_TAG", C72RfidScannerModule.convertArrayToWritableArray(tagData));
            }
        }
    }
}
