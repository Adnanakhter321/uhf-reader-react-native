module.exports = {
  dependency: {
    platforms: {
      android: {
        sourceDir: './android',
        packageImportPath: 'import com.uhfrfidlibrary.uhf.C72RfidScannerPackage;',
        packageInstance: 'new C72RfidScannerPackage()',
      },
    },
  },
};
