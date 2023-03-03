import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:typed_data';
import 'constants.dart' as constants;

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _log = "";
  String _Status = "";
  String deviceStatus = "";
  String closeStatus = "";
  //
  String _data = "";
  String thName = "";
  String enName = "";
  String gender = "";
  String birthDate = "";
  String cardIssuer = "";
  String issueDate = "";
  String expireDate = "";
  String address = "";
  //
  String resetCard = "";
  String requestStatus = "";
  String setProtocal = "";
  List<int> image = [];
  String imageStatus = "";
  Image? photo;
  String imageForDebug = "";

  @override
  void initState() {
    super.initState();
    setState(() {
      _log = "start";
    });
  }

  Future<List<int>> invokeToGetImage() async {
    MethodChannel _channel =
        MethodChannel('com.example.esignaturecardreader.usb_channel');
    List<int> dataBuffer = [];
    try {
      //reset card
      String _resetCard = await _channel.invokeMethod('resetCard');
      setState(() {
        resetCard = _resetCard;
      });
      //set protocol
      String _setProtocal = await _channel.invokeMethod('setProtocol', {
        "selectHex": constants.SELECT,
      });
      setState(() {
        setProtocal = _setProtocal;
      });
      //request
      int i = 0;
      for (Uint8List req in constants.PHOTO) {
        await _channel.invokeMethod('send', {
          "requestHex": req,
        });
        i++;
        setState(() {
          imageStatus =
              imageStatus + " list no:" + dataBuffer.length.toString() + "\r\n";
        });
        //response
        List<int> data = await _channel.invokeMethod(
            'sendGetByteArray', {"requestHex": constants.PHOTO_RESP});
        if (dataBuffer.length > 0) {
          dataBuffer.insertAll(dataBuffer.length - 2, data);
        } else {
          dataBuffer.insertAll(0, data);
        }
        setState(() {
          imageForDebug =
              imageForDebug + "no " + i.toString() + data.toString() + "\r\n";

          imageStatus = imageStatus +
              "on process no:" +
              i.toString() +
              " list no:" +
              dataBuffer.length.toString() +
              " lenght data:" +
              data.length.toString() +
              "\r\n";
        });
      }
    } catch (e) {
      setState(() {
        imageStatus = e.toString();
      });
    }

    return dataBuffer;
  }

  Future<String> invokeToSent(String type) async {
    MethodChannel _channel =
        MethodChannel('com.example.esignaturecardreader.usb_channel');

    Uint8List req;
    Uint8List resp;
    switch (type) {
      case "cid":
        {
          req = constants.CID;
          resp = constants.CID_RESP;
        }
        break;
      case "th_name":
        {
          req = constants.TH_NAME;
          resp = constants.TH_NAME_RESP;
        }
        break;
      case "en_name":
        {
          req = constants.EN_NAME;
          resp = constants.EN_NAME_RESP;
        }
        break;
      case "birth_date":
        {
          req = constants.BIRTH_DATE;
          resp = constants.BIRTH_DATE_RESP;
        }
        break;
      case "gender":
        {
          req = constants.GENDER;
          resp = constants.GENDER_RESP;
        }
        break;
      case "card_issuer":
        {
          req = constants.CARD_ISSUER;
          resp = constants.CARD_ISSUER_RESP;
        }
        break;
      case "issue_date":
        {
          req = constants.ISSUE_DATE;
          resp = constants.ISSUE_DATE_RESP;
        }
        break;
      case "expire_date":
        {
          req = constants.EXPIRE_DATE;
          resp = constants.EXPIRE_DATE_RESP;
        }
        break;
      case "address":
        {
          req = constants.ADDRESS;
          resp = constants.ADDRESS_RESP;
        }
        break;

      default:
        {
          req = constants.CID;
          resp = constants.CID_RESP;
        }
        break;
    }
    var data;
    try {
      //reset card
      String _resetCard = await _channel.invokeMethod('resetCard');
      setState(() {
        resetCard = _resetCard;
      });
      //set protocol
      String _setProtocal = await _channel.invokeMethod('setProtocol', {
        "selectHex": constants.SELECT,
      });
      setState(() {
        setProtocal = _setProtocal;
      });
      //request
      String _reqStatus = await _channel.invokeMethod('send', {
        "requestHex": req,
      });
      setState(() {
        requestStatus = _reqStatus;
      });
      //response
      data = await _channel.invokeMethod('send', {"requestHex": resp});
    } catch (e) {
      data = e;
    }

    return data;
  }

  Future<String> invokeToCreate() async {
    MethodChannel _channel =
        MethodChannel('com.example.esignaturecardreader.usb_channel');

    var data = await _channel.invokeMethod('create');
    return data;
  }

  Future<String> invokeToClose() async {
    MethodChannel _channel =
        MethodChannel('com.example.esignaturecardreader.usb_channel');

    var data = await _channel.invokeMethod('close');
    setState(() {
      deviceStatus = "";
      _data = "";
      thName = "";
      address = "";
    });
    return data;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        debugShowCheckedModeBanner: false,
        home: Scaffold(
          appBar: AppBar(
            title: const Text('USB Serial Plugin example app'),
          ),
          body: SingleChildScrollView(
              child: Center(
            child: Column(children: <Widget>[
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToCreate();
                  setState(() {
                    deviceStatus = data;
                  });
                },
                child: Text("Connect"),
              ),
              // Text('Device: $deviceStatus\n'),
              deviceStatus.length != 0
                  ? Text("เชื่อมต่อ USB แล้ว")
                  : Text("กรุณาเชื่อมต่อ USB"),
              Text('$resetCard'),
              Text('$setProtocal'),
              Text('$requestStatus'),
              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("cid");
                  setState(() {
                    _data = data;
                  });
                },
                child: Text("ดึงเลขบัตรประชาชน"),
              ),

              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("th_name");
                  setState(() {
                    thName = data;
                  });
                },
                child: Text("ดึงชื่อ (TH)"),
              ),

              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("en_name");
                  setState(() {
                    enName = data;
                  });
                },
                child: Text("ดึงชื่อ (EN)"),
              ),

              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("birth_date");
                  setState(() {
                    birthDate = data;
                  });
                },
                child: Text("ดึงวันเกิด"),
              ),

              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("gender");
                  setState(() {
                    gender = data;
                  });
                },
                child: Text("ดึงเพศ"),
              ),

              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("card_issuer");
                  setState(() {
                    cardIssuer = data;
                  });
                },
                child: Text("ดึงสถานที่ออกบัตร"),
              ),

              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("issue_date");
                  setState(() {
                    issueDate = data;
                  });
                },
                child: Text("ดึงวันออกบัตร"),
              ),

              //
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("expire_date");
                  setState(() {
                    expireDate = data;
                  });
                },
                child: Text("ดึงวันหมดอายุบัตร"),
              ),

              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToSent("address");
                  setState(() {
                    address = data;
                  });
                },
                child: Text("ดึงที่อยู่"),
              ),
              Text(
                'cid : $_data',
                textAlign: TextAlign.center,
              ),
              Text(
                'thName : $thName',
                textAlign: TextAlign.center,
              ),
              Text(
                'enName : $enName',
                textAlign: TextAlign.center,
              ),
              Text(
                'birthDate : $birthDate',
                textAlign: TextAlign.center,
              ),
              Text(
                'gender : $gender',
                textAlign: TextAlign.center,
              ),
              Text(
                'cardIssuer : $cardIssuer',
                textAlign: TextAlign.center,
              ),
              Text(
                'issueDate : $issueDate',
                textAlign: TextAlign.center,
              ),
              Text(
                'expireDate : $expireDate',
                textAlign: TextAlign.center,
              ),
              Text(
                'address : $address',
                textAlign: TextAlign.center,
              ),
              //
              ElevatedButton(
                onPressed: () async {
                  List<int> data = await invokeToGetImage();
                  setState(() {
                    image = data;
                  });
                },
                child: Text("ดึงรูป"),
              ),
              image.isNotEmpty
                  ? Image.memory(Uint8List.fromList(image))
                  : Text("no Image"),
              Text('$imageForDebug'),
              Text('$image'),
              Text('$imageStatus'),
              ElevatedButton(
                onPressed: () async {
                  String data = await invokeToClose();
                  setState(() {
                    closeStatus = data;
                  });
                },
                child: Text("Close"),
              ),
              Text('$closeStatus'),
            ]),
          )),
        ));
  }
}

class Boolean {}
