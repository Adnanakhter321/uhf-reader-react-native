import { NativeEventEmitter, NativeModules } from "react-native";

const { C72RfidScanner } = NativeModules;

// This module uses the classic NativeModules API on purpose (no TurboModule
// spec / codegen). React Native's Interop Layer bridges this kind of module
// onto the New Architecture automatically, so the exact same code here works
// unmodified on both the Old Architecture and the New Architecture — no
// separate build or JS branch needed.
if (!C72RfidScanner) {
  throw new Error(
    "uhf-reader-react-native: native module 'C72RfidScanner' is not linked. " +
      "Rebuild the Android app after installing this package (autolinking " +
      "picks it up from android/react-native.config.js — no manual linking step needed)."
  );
}

const eventEmitter = new NativeEventEmitter(C72RfidScanner);

type initializeReader = () => void;
type deInitializeReader = () => void;
type readSingleTag = () => Promise<any>;
type readPower = () => Promise<any>;
type playSoundTy = (number: 1 | 2) => void;
type releaseSoundPoolTy = () => void;
type initializeUHFType = () => Promise<any>;
type changePower = (powerValue: any) => Promise<any>;
type AddListener = (cb: (args: any[]) => void) => void;
type clearTags = () => void;

const playSoundFunc: playSoundTy = (number: 1 | 2) =>
  C72RfidScanner.playSound(number);

const releaseSoundPool: releaseSoundPoolTy = () =>
  C72RfidScanner.releaseSoundPool();

const initializeReader: initializeReader = () =>
  C72RfidScanner.initializeReader();

const deInitializeReader: deInitializeReader = () =>
  C72RfidScanner.deInitializeReader();

const readSingleTag: readSingleTag = () => C72RfidScanner.readSingleTag();

const startReadingTags = (callback: (args: any[]) => any) =>
  C72RfidScanner.startReadingTags(callback);

const stopReadingTags = (callback: (args: any[]) => any) =>
  C72RfidScanner.stopReadingTags(callback);

const readPower: readPower = () => C72RfidScanner.readPower();

const changePower: changePower = (powerValue: any) =>
  C72RfidScanner.changePower(powerValue);

const powerListener: AddListener = (listener) =>
  eventEmitter.addListener("UHF_POWER", listener);

const tagListener: AddListener = (listener) =>
  eventEmitter.addListener("UHF_TAG", listener);

const clearTags: clearTags = () => C72RfidScanner.clearAllTags();

const initializeUHF: initializeUHFType = () => C72RfidScanner.initializeUHF();

const deinitializeUHF = () => C72RfidScanner.deinitializeUHF();

export default {
  releaseSoundPool,
  playSoundFunc,
  initializeUHF,
  deinitializeUHF,
  powerListener,
  tagListener,
  initializeReader,
  readSingleTag,
  startReadingTags,
  stopReadingTags,
  readPower,
  changePower,
  deInitializeReader,
  clearTags,
};
