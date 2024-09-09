# uhf-reader-react-native

## Getting started

`$ npm install uhf-reader-react-native --save`

### Mostly automatic installation

`$ react-native link uhf-reader-react-native`

## Usage

```javascript
import C72RfidScanner from "uhf-reader-react-native";
```

```JSX
const App = () => {

  const [isReading, setIsReading] = React.useState();

  const [powerState, setPowerState] = React.useState('');

  const [tags, setTags] = React.useState([]);
  const [power,setPower]=React.useState(0)
  const showAlert = (title, data) => {
    Alert.alert(
      title,
      data,
      [
        { text: 'OK', onPress: () => console.log('OK Pressed') },
      ],
      { cancelable: false },
    );
  }

  const powerListener = (data) => {
    setPowerState(data);
  }

  const tagListener = (data) => {
    setTags(tags => tags.concat(data[0]));
    C72RFIDScanner.playSoundFunc(1)
  }

  React.useEffect(() => {
    (async () => {
      try{
        const scanner = C72RFIDScanner;
        const result = await scanner.initializeUHF()
        setPowerState(result)
        scanner.tagListener(tagListener);
        scanner.powerListener(powerListener);
      }catch(er){
        alert(er.message)
      }
    })()
    return () => {
      C72RFIDScanner.deinitializeUHF();
      C72RFIDScanner.releaseSoundPool()
    }
  }, []);

  const readPower = async () => {
    try {
      let result = await C72RFIDScanner.readPower();
      showAlert('SUCCESS', `The result is ${result}`);
      console.log(result);
    } catch (error) {
      showAlert('FAILED', error.message);
    }
  }

  const scanSingleTag = async () => {
    try {
      let result = await C72RFIDScanner.readSingleTag();
      showAlert('SUCCESS', `The result is ${result}`);
      C72RFIDScanner.playSoundFunc(1)
      console.log(result);
    } catch (error) {
      showAlert('FAILED', error.message);
    }
  }

  const startReading = () => {
    C72RFIDScanner.startReadingTags(function success(message) {
      setIsReading(message);
    })
  }

  const stopReading = () => {
    C72RFIDScanner.stopReadingTags(function success(message) {
      setIsReading(false);
    });
  }
  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <View>
        <Text>{powerState}
        </Text>
        <TextInput style={{ width: 150, height: 50 }} onChangeText={(val) => setPower(Number(val))} keyboardType='number-pad' maxLength={2} />
      </View>

      <View style={{flexDirection:'row',flexWrap:'wrap'}}>
      <View style={{ marginVertical: 20, marginHorizontal:10 }}>
        <Button style={{ margin: 10 }} onPress={async() => {
          try{
            const scanner = C72RFIDScanner;
            const result = await scanner.changePower(power)
            alert('sucess')
          }catch(er){
            alert(er.message)
          }
        }} title='Change Power' />
      </View>
      <View style={{ marginVertical: 20, marginHorizontal:10 }}>
        <Button style={{ margin: 10 }} onPress={async() => {
          try{
            const scanner = C72RFIDScanner;
            const result = await scanner.initializeUHF()
            setPowerState(result)
          }catch(er){
            alert(er.message)
          }
        }} title='Initialize UHF' />
      </View>
      <View style={{ marginVertical: 20, marginHorizontal:10 }}>
        <Button style={{ margin: 10 }} onPress={async() => {
          try{
            const scanner = C72RFIDScanner;
            const result = await scanner.deinitializeUHF()
            C72RFIDScanner.releaseSoundPool()
            setPowerState(result)
            setTags([])
          }catch(er){
            alert(er.message)
          }
        }} title='DeInitialize UHF' />
      </View>
      <View style={{ marginVertical: 20, marginHorizontal:10 }}>
        <Button style={{ margin: 10 }} onPress={() => readPower()} title='Read Power' />
      </View>

      <View style={{ marginVertical: 20, marginHorizontal:10 }}>
        <Button style={{ margin: 10 }} onPress={() => scanSingleTag()} title='Read Single Tag' />
      </View>

      <View style={{ marginVertical: 20, marginHorizontal:10 }}>
        <Button disabled={isReading} style={{ margin: 10 }} onPress={() => startReading()} title='Start Bulk Scan' />
      </View>

      <View style={{ marginVertical: 20, marginHorizontal:10 }}>
        <Button disabled={!isReading} style={{ margin: 10 }} onPress={() => stopReading()} title='Stop Bulk Scan' />
      </View>
      </View>
      <Text>{tags}</Text>
    </View>
  );

}
export default App;