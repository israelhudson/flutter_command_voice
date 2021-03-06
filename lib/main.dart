import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Command Voice',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const platform_invoke_recognizer = const MethodChannel('voice_invoke_recognizer');
  static const platform_voice_speech_text = const MethodChannel('voice_speech_text');

  String _textVoice = '---';

  @override
  void initState() {
    super.initState();

    getVoiceText();
  }

  void getVoiceText() {
    platform_voice_speech_text.setMethodCallHandler((call){
      if(call.method == "voice_text"){
        setState(() {
          _textVoice = call.arguments;
        });
      }
      return null;
    });
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Texto de voz'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            RaisedButton(
              child: Text('Texto da frase dita'),
              onPressed: openSpeechRecognizer,
            ),
            Text("Texto dito: ${_textVoice}"),
          ],
        ),
      ),
    );
  }

  Future openSpeechRecognizer() async => await platform_invoke_recognizer.invokeMethod('displaySpeechRecognizer');
}